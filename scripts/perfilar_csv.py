#!/usr/bin/env python3
"""Perfil reproducible y de solo lectura de los CSV; no es el ETL de la API."""

import argparse
from collections import Counter, defaultdict
import csv
from datetime import date
import hashlib
import io
import json
from pathlib import Path
import re
import sys


CAMPOS = (
    'fecha_asignacion', 'sexo', 'anio_nacimiento', 'materia',
    'numero_de_fecha_de_evaluacion', 'anio_de_ingreso', 'aprobacion',
    'carrera_objetivo', 'departamento_institucion_educativa',
    'municipio_institucion_educativa', 'tipo_institucion_educativa',
    'correlativo_aspirante',
)
ALIAS = {
    'numero_de_fecha_de_evaluaci': 'numero_de_fecha_de_evaluacion',
    'departamento_institucion_ed': 'departamento_institucion_educativa',
    'municipio_institucion_': 'municipio_institucion_educativa',
}
ESPECIALES = {'SIN REGISTRO', 'SIN DATOS', 'NINGUNO'}
EDAD_MAXIMA_ANALITICA = 100  # Decisión de calidad; no es un requisito del enunciado.


def entero(valor):
    return int(valor) if re.fullmatch(r'[0-9]+', valor) else None


def fecha(valor):
    try:
        return date.fromisoformat(valor) if re.fullmatch(r'\d{4}-\d{2}-\d{2}', valor) else None
    except ValueError:
        return None


def perfilar(ruta):
    contenido = ruta.read_bytes()
    texto = contenido.decode('utf-8-sig', errors='strict')
    lector = csv.reader(io.StringIO(texto, newline=''), delimiter=',', strict=True)
    original = next(lector)
    encabezado = [ALIAS.get(nombre, nombre) for nombre in original]
    if len(set(encabezado)) != len(encabezado) or set(encabezado) != set(CAMPOS):
        raise ValueError(f'{ruta}: encabezados desconocidos, duplicados o incompletos')
    filas = []
    errores_estructura = []
    leidas = 0
    for valores in lector:
        leidas += 1
        if len(valores) != len(encabezado):
            errores_estructura.append({'linea_final': lector.line_num, 'columnas': len(valores)})
            continue
        filas.append(dict(zip(encabezado, valores)))
    if not filas:
        raise ValueError(f'{ruta}: no contiene registros con estructura completa')

    columnas = {campo: Counter(f[campo] for f in filas) for campo in CAMPOS}
    huellas = Counter(tuple(f[c] for c in CAMPOS) for f in filas)
    por_aspirante = defaultdict(list)
    claves_oportunidad = defaultdict(set)
    for fila in filas:
        por_aspirante[fila['correlativo_aspirante']].append(fila)
        clave = tuple(fila[c] for c in ('correlativo_aspirante', 'anio_de_ingreso', 'materia', 'numero_de_fecha_de_evaluacion'))
        claves_oportunidad[clave].add(tuple(fila[c] for c in CAMPOS))
    fechas = [fecha(f['fecha_asignacion']) for f in filas]
    edades = []
    nacimiento_desconocido = nacimiento_invalido = edad_invalida = 0
    for fila in filas:
        nacimiento = entero(fila['anio_nacimiento'])
        ingreso = entero(fila['anio_de_ingreso'])
        if fila['anio_nacimiento'] in ESPECIALES or not fila['anio_nacimiento'].strip():
            nacimiento_desconocido += 1
        elif nacimiento is None or len(fila['anio_nacimiento']) != 4 or ingreso is None:
            nacimiento_invalido += 1
        elif not 0 <= ingreso - nacimiento <= EDAD_MAXIMA_ANALITICA:
            edad_invalida += 1
        else:
            edades.append(ingreso - nacimiento)

    perfil_columnas = {}
    for campo, valores in columnas.items():
        perfil = {
            'distintos': len(valores),
            'vacios': sum(n for v, n in valores.items() if not v.strip()),
            'especiales': {v: n for v, n in sorted(valores.items()) if v in ESPECIALES},
            'filas_con_espacios_externos': sum(n for v, n in valores.items() if v != v.strip()),
            'longitud_maxima': max(map(len, valores)),
        }
        if campo != 'correlativo_aspirante':
            perfil['valores'] = dict(sorted(valores.items()))
        grupos_trim = defaultdict(list)
        for valor in valores:
            grupos_trim[valor.strip()].append(valor)
        if campo != 'correlativo_aspirante':
            perfil['colisiones_si_se_recortan_espacios'] = {
                k: {v: valores[v] for v in variantes}
                for k, variantes in sorted(grupos_trim.items()) if len(variantes) > 1
            }
        perfil_columnas[campo] = perfil

    distintos = {campo: sum(len({f[campo] for f in grupo}) > 1 for grupo in por_aspirante.values())
                 for campo in ('sexo', 'anio_nacimiento', 'carrera_objetivo',
                               'tipo_institucion_educativa', 'municipio_institucion_educativa')}
    oportunidades = columnas['numero_de_fecha_de_evaluacion']
    longitudes_id = Counter()
    for identificador, cantidad in columnas['correlativo_aspirante'].items():
        longitudes_id[len(identificador)] += cantidad
    perfil = {
        'archivo': ruta.name,
        'bytes': len(contenido),
        'sha256': hashlib.sha256(contenido).hexdigest(),
        'codificacion_comprobada': 'UTF-8 sin BOM' if not contenido.startswith(b'\xef\xbb\xbf') else 'UTF-8 con BOM',
        'delimitador': ',',
        'saltos_crlf': contenido.count(b'\r\n'),
        'saltos_lf_sin_cr': contenido.count(b'\n') - contenido.count(b'\r\n'),
        'encabezados_originales': original,
        'encabezados_canonicos': encabezado,
        'filas_leidas': leidas,
        'filas_estructura_completa': len(filas),
        'errores_estructura': errores_estructura,
        'aspirantes_distintos_no_vacios': sum(bool(v.strip()) for v in por_aspirante),
        'aspirantes_con_multiples_filas': sum(len(g) > 1 for v, g in por_aspirante.items() if v.strip()),
        'distribucion_filas_por_aspirante': dict(sorted(Counter(map(len, por_aspirante.values())).items())),
        'registros_distintos_por_12_campos': len(huellas),
        'duplicados_exactos_excedentes': sum(n - 1 for n in huellas.values()),
        'claves_aspirante_anio_materia_oportunidad_con_variantes': sum(len(g) > 1 for g in claves_oportunidad.values()),
        'aspirantes_con_valores_distintos': distintos,
        'identificadores_no_hexadecimales': sum(n for v, n in columnas['correlativo_aspirante'].items() if not re.fullmatch('[0-9a-f]+', v)),
        'longitudes_identificador': dict(sorted(longitudes_id.items())),
        'fechas_invalidas': sum(f is None for f in fechas),
        'fecha_minima': min(f for f in fechas if f is not None).isoformat() if any(fechas) else None,
        'fecha_maxima': max(f for f in fechas if f is not None).isoformat() if any(fechas) else None,
        'oportunidades_invalidas': sum(n for v, n in oportunidades.items() if entero(v) is None or entero(v) < 1),
        'edad_referencia_ingreso': {
            'nacimiento_desconocido': nacimiento_desconocido,
            'nacimiento_o_ingreso_no_convertible': nacimiento_invalido,
            'edad_fuera_0_a_100': edad_invalida,
            'filas_edad_valida': len(edades),
            'minima': min(edades) if edades else None,
            'maxima': max(edades) if edades else None,
            'filas_menores_14': sum(e < 14 for e in edades),
        },
        'columnas': perfil_columnas,
    }
    return perfil, filas


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('archivos', type=Path, nargs='+')
    parser.add_argument('--salida', type=Path, help='JSON de evidencia; usar una ruta dentro de .docs/')
    args = parser.parse_args()
    try:
        perfiles, conjuntos, registros = [], {}, Counter()
        nacimientos_por_id = defaultdict(set)
        for ruta in args.archivos:
            if str(ruta.resolve()) in conjuntos:
                raise ValueError(f'Archivo repetido: {ruta}')
            perfil, filas = perfilar(ruta)
            perfiles.append(perfil)
            conjuntos[str(ruta.resolve())] = {f['correlativo_aspirante'] for f in filas if f['correlativo_aspirante'].strip()}
            registros.update(tuple(f[c] for c in CAMPOS) for f in filas)
            for fila in filas:
                if entero(fila['anio_nacimiento']) is not None:
                    nacimientos_por_id[fila['correlativo_aspirante']].add(fila['anio_nacimiento'])
        grupos = list(conjuntos.values())
        resultado = {
            'version_perfil': 1,
            'archivos': perfiles,
            'conjunto': {
                'filas_leidas': sum(p['filas_leidas'] for p in perfiles),
                'aspirantes_distintos': len(set().union(*grupos)),
                'registros_distintos_por_12_campos': len(registros),
                'duplicados_exactos_excedentes': sum(n - 1 for n in registros.values()),
                'aspirantes_con_nacimientos_numericos_distintos': sum(len(v) > 1 for v in nacimientos_por_id.values()),
                'intersecciones_identificador': [
                    {'archivos': [perfiles[i]['archivo'], perfiles[j]['archivo']], 'aspirantes': len(grupos[i] & grupos[j])}
                    for i in range(len(grupos)) for j in range(i + 1, len(grupos))
                ],
            },
        }
        salida = json.dumps(resultado, ensure_ascii=False, indent=2) + '\n'
        if args.salida:
            if args.salida.resolve() in [p.resolve() for p in args.archivos]:
                raise ValueError('La salida no puede sobrescribir un CSV de origen')
            args.salida.write_text(salida, encoding='utf-8')
        else:
            print(salida, end='')
    except (OSError, UnicodeError, csv.Error, ValueError, StopIteration) as error:
        print(f'No se pudo perfilar: {error}', file=sys.stderr)
        return 1
    return 0


if __name__ == '__main__':
    sys.exit(main())
