import csv
from pathlib import Path
import tempfile
import unittest

from perfilar_csv import ALIAS, CAMPOS, perfilar


class PerfilCsvTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.path = Path(self.temp.name) / 'muestra.csv'
        self.base = dict(zip(CAMPOS, [
            '2021-08-01', 'F', '2004', 'MATEMATICA', '1', '2022',
            'APROBADO', 'CARRERA', 'GUATEMALA', 'MUNICIPIO', 'PUBLICO', 'id-anonimo',
        ]))

    def escribir(self, filas, encabezados=CAMPOS):
        with self.path.open('w', encoding='utf-8', newline='') as stream:
            writer = csv.writer(stream)
            writer.writerow(encabezados)
            for fila in filas:
                writer.writerow([fila[ALIAS.get(campo, campo)] for campo in encabezados])

    def test_alias_y_orden_no_cambian_datos_ni_exigen_id_de_32_caracteres(self):
        alias = {v: k for k, v in ALIAS.items()}
        encabezados = [alias.get(c, c) for c in reversed(CAMPOS)]
        self.escribir([self.base], encabezados)
        original = self.path.read_bytes()
        perfil, filas = perfilar(self.path)
        self.assertEqual(filas, [self.base])
        self.assertEqual(perfil['aspirantes_distintos_no_vacios'], 1)
        self.assertEqual(perfil['filas_estructura_completa'], 1)
        self.assertEqual(original, self.path.read_bytes())

    def test_duplicados_no_eliminan_otras_oportunidades_o_fechas(self):
        otra_oportunidad = {**self.base, 'numero_de_fecha_de_evaluacion': '2'}
        otra_fecha = {**self.base, 'fecha_asignacion': '2021-08-02'}
        self.escribir([self.base, self.base, otra_oportunidad, otra_fecha])
        perfil, _ = perfilar(self.path)
        self.assertEqual(perfil['filas_leidas'], 4)
        self.assertEqual(perfil['registros_distintos_por_12_campos'], 3)
        self.assertEqual(perfil['duplicados_exactos_excedentes'], 1)
        self.assertEqual(perfil['claves_aspirante_anio_materia_oportunidad_con_variantes'], 1)
        self.assertEqual(perfil['aspirantes_con_multiples_filas'], 1)

    def test_no_fusiona_carreras_con_espacios_distintos(self):
        self.escribir([self.base, {**self.base, 'carrera_objetivo': 'CARRERA '}])
        perfil, _ = perfilar(self.path)
        carreras = perfil['columnas']['carrera_objetivo']
        self.assertEqual(carreras['distintos'], 2)
        self.assertEqual(carreras['colisiones_si_se_recortan_espacios']['CARRERA'],
                         {'CARRERA': 1, 'CARRERA ': 1})

    def test_edades_desconocidas_inconsistentes_y_referencia_de_ingreso(self):
        self.escribir([{**self.base, 'anio_nacimiento': valor} for valor in
                       ['SIN REGISTRO', '1900', '2030', 'no-numero', '2004']])
        perfil, _ = perfilar(self.path)
        edad = perfil['edad_referencia_ingreso']
        self.assertEqual(edad['nacimiento_desconocido'], 1)
        self.assertEqual(edad['nacimiento_o_ingreso_no_convertible'], 1)
        self.assertEqual(edad['edad_fuera_0_a_100'], 2)
        self.assertEqual(edad['filas_edad_valida'], 1)
        self.assertEqual(edad['minima'], 18)

    def test_registra_fila_incompleta_y_no_la_confunde_con_evaluacion_completa(self):
        self.escribir([self.base])
        with self.path.open('a') as stream:
            stream.write('una,sola,fila\n')
        perfil, _ = perfilar(self.path)
        self.assertEqual(perfil['filas_leidas'], 2)
        self.assertEqual(perfil['filas_estructura_completa'], 1)
        self.assertEqual(perfil['errores_estructura'], [{'linea_final': 3, 'columnas': 3}])

    def test_rechaza_encabezados_desconocidos(self):
        self.path.write_text('desconocido\nvalor\n')
        with self.assertRaisesRegex(ValueError, 'encabezados'):
            perfilar(self.path)


if __name__ == '__main__':
    unittest.main()
