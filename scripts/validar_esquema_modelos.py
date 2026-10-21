#!/usr/bin/env python3
"""Valida invariantes de los scripts de modelos sin requerir bases en ejecución."""

from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[1]
MONGO = ROOT / 'backend/src/main/resources/db/mongodb/01_indices.js'
NEO4J = ROOT / 'backend/src/main/resources/db/neo4j/01_esquema.cypher'


def assert_contains(text, fragment):
    if fragment not in text:
        raise AssertionError(f'Falta: {fragment}')


def main():
    mongo = MONGO.read_text(encoding='utf-8')
    neo4j = NEO4J.read_text(encoding='utf-8')
    for name in ('historial_aspirante', 'aprobacion_por_materia', 'carrera_anio_aspirante',
                 'institucion_sexo_aspirante', 'procedencia_aspirante',
                 'fecha_institucion_materia', 'primer_intento_por_carrera',
                 'archivo_unico', 'estado_reciente', 'cohorte_carrera_unica'):
        assert_contains(mongo, f"name: '{name}'")
    assert_contains(mongo, "{ sha256Archivo: 1 }")
    assert_contains(mongo, "unique: true")
    for label in ('Aspirante', 'Evaluacion', 'Materia', 'Carrera', 'Resultado',
                  'TipoInstitucion', 'Departamento', 'Municipio', 'ProcedenciaEducativa'):
        if not re.search(rf'CREATE CONSTRAINT .+?FOR \(n:{label}\) REQUIRE n\.(id|clave|codigo) IS UNIQUE', neo4j, re.S):
            raise AssertionError(f'Falta restricción única para {label}')
    for index in ('evaluacion_cohorte_materia', 'evaluacion_resultado', 'evaluacion_fecha_asignacion'):
        assert_contains(neo4j, f'CREATE INDEX {index} IF NOT EXISTS')
    print('Esquemas MongoDB y Neo4j: invariantes estáticas correctas.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
