#!/usr/bin/env python3
"""Valida ambos modelos con una fila real y limpia sus datos temporales al terminar.

Requiere Docker Compose, los servicios iniciados y los índices/restricciones aplicados.
No carga el CSV completo ni reemplaza el ETL de la API.
"""

import argparse
import csv
import hashlib
import json
from pathlib import Path
import subprocess
import sys

from perfilar_csv import ALIAS, CAMPOS, entero


ROOT = Path(__file__).resolve().parents[1]
MARCADOR = 'VALIDACION_MODELO_B01'


def ejecutar(command, *, stdin=None):
    result = subprocess.run(command, cwd=ROOT, input=stdin, text=True,
                            capture_output=True, check=False)
    if result.returncode:
        raise RuntimeError(result.stderr.strip() or result.stdout.strip())
    return result.stdout


def fila_real(path):
    with path.open(encoding='utf-8-sig', newline='') as stream:
        reader = csv.DictReader(stream)
        raw = next(reader)
    fila = {ALIAS.get(k, k): v for k, v in raw.items()}
    if set(fila) != set(CAMPOS):
        raise ValueError('La primera fila no contiene los doce campos canónicos')
    return fila


def key(fila):
    contenido = '\x1f'.join(fila[c] for c in CAMPOS)
    return hashlib.sha256(contenido.encode()).hexdigest()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('csv', type=Path, help='CSV real, por ejemplo .data/pruebas_especificas_2024.csv')
    args = parser.parse_args()
    try:
        fila = fila_real(args.csv)
        # Prefijos dedicados: jamás se marca ni borra un nodo/registro de una carga real.
        identificador = f'{MARCADOR}:{key(fila)}'
        departamento = fila['departamento_institucion_educativa']
        municipio = fila['municipio_institucion_educativa']
        tipo = fila['tipo_institucion_educativa']
        carrera = fila['carrera_objetivo']
        aspirante_key = f"{MARCADOR}:{fila['correlativo_aspirante']}"
        materia_key = f"{MARCADOR}:{fila['materia']}"
        carrera_key = f'{MARCADOR}:{carrera}'
        resultado_key = f"{MARCADOR}:{fila['aprobacion']}"
        tipo_key = f'{MARCADOR}:{tipo}'
        departamento_key = f'{MARCADOR}:{hashlib.sha256(departamento.encode()).hexdigest()}'
        municipio_material = f'{departamento}\x1f{municipio}'
        procedencia_material = f'{tipo}\x1f{departamento}\x1f{municipio}'
        municipio_key = f'{MARCADOR}:{hashlib.sha256(municipio_material.encode()).hexdigest()}'
        procedencia_key = f'{MARCADOR}:{hashlib.sha256(procedencia_material.encode()).hexdigest()}'
        documento = {
            '_id': identificador, 'aspiranteId': aspirante_key,
            'fechaAsignacion': {'$date': f"{fila['fecha_asignacion']}T00:00:00.000Z"},
            'sexo': fila['sexo'], 'anioNacimiento': entero(fila['anio_nacimiento']),
            'materia': fila['materia'], 'oportunidad': entero(fila['numero_de_fecha_de_evaluacion']),
            'anioIngreso': entero(fila['anio_de_ingreso']), 'aprobacion': fila['aprobacion'],
            'carreraObjetivo': carrera, 'carreraObjetivoConocida': carrera != 'NINGUNO',
            'procedencia': {'departamento': departamento, 'municipio': municipio,
                            'tipoInstitucion': tipo},
            'fuentes': [{'sha256Archivo': hashlib.sha256(args.csv.read_bytes()).hexdigest(), 'fila': 2}],
            'esquemaVersion': 1, 'marcaValidacion': MARCADOR,
        }
        mongo_script = "\n".join([
            f"const d = {json.dumps(documento, ensure_ascii=False)};",
            "d.fechaAsignacion = new Date(d.fechaAsignacion.$date);",
            "db.evaluaciones.replaceOne({_id: d._id}, d, {upsert: true});",
            "const r = db.evaluaciones.findOne({_id: d._id, marcaValidacion: 'VALIDACION_MODELO_B01'});",
            "if (!r || !(r.fechaAsignacion instanceof Date) || r.oportunidad < 1) throw new Error('Documento Mongo inválido');",
            "db.evaluaciones.deleteOne({_id: d._id, marcaValidacion: 'VALIDACION_MODELO_B01'});",
            "print('MongoDB: muestra real validada y limpiada');",
        ])
        cypher = "\n".join([
            f"WITH {json.dumps(identificador)} AS evaluacionId, {json.dumps(aspirante_key)} AS aspiranteId,",
            f"     {json.dumps(materia_key)} AS materia, {json.dumps(carrera_key)} AS carrera, {json.dumps(resultado_key)} AS resultado,",
            f"     {json.dumps(tipo_key)} AS tipo, {json.dumps(departamento_key)} AS departamentoKey, {json.dumps(departamento)} AS departamento,",
            f"     {json.dumps(municipio_key)} AS municipioKey, {json.dumps(municipio)} AS municipio, {json.dumps(procedencia_key)} AS procedenciaKey",
            "MERGE (a:Aspirante {id: aspiranteId}) SET a.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (e:Evaluacion {id: evaluacionId}) SET e.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (m:Materia {clave: materia}) SET m.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (c:Carrera {clave: carrera}) SET c.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (r:Resultado {codigo: resultado}) SET r.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (t:TipoInstitucion {codigo: tipo}) SET t.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (d:Departamento {clave: departamentoKey}) SET d.nombre = departamento, d.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (u:Municipio {clave: municipioKey}) SET u.nombre = municipio, u.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (p:ProcedenciaEducativa {clave: procedenciaKey}) SET p.marcaValidacion = 'VALIDACION_MODELO_B01'",
            "MERGE (a)-[:REALIZO]->(e) MERGE (e)-[:EVALUO_MATERIA]->(m) MERGE (e)-[:PARA_CARRERA]->(c)",
            "MERGE (e)-[:OBTUVO_RESULTADO]->(r) MERGE (e)-[:TIENE_PROCEDENCIA]->(p)",
            "MERGE (p)-[:ES_DE_TIPO]->(t) MERGE (p)-[:UBICADA_EN]->(u) MERGE (u)-[:PERTENECE_A]->(d);",
            f"MATCH (e:Evaluacion {{id: {json.dumps(identificador)}}})<-[:REALIZO]-(:Aspirante)-[:REALIZO]->(e) RETURN count(e) AS modeloValido;",
            "MATCH (n {marcaValidacion: 'VALIDACION_MODELO_B01'}) DETACH DELETE n;",
        ])
        ejecutar(['docker', 'compose', 'exec', '-T', 'mongodb', 'sh', '-c',
                  'mongosh --quiet --username "$MONGO_INITDB_ROOT_USERNAME" --password "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin admisiones_usac'],
                 stdin=mongo_script)
        ejecutar(['docker', 'compose', 'exec', '-T', 'neo4j', 'sh', '-c',
                  'cypher-shell --non-interactive --username neo4j --password "${NEO4J_AUTH#*/}" --database neo4j'],
                 stdin=cypher)
        print('MongoDB y Neo4j: muestra real validada y limpiada.')
        return 0
    except (OSError, ValueError, RuntimeError, StopIteration) as error:
        print(f'Validación de modelo no completada: {error}', file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
