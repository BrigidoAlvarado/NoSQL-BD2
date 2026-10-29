# Análisis y conclusiones

## Evidencia y alcance

El perfilado local de los CSV confirmados de 2015, 2022 y 2024 registró 58 377 filas, 16 032 correlativos distintos y 15 duplicados exactos excedentes. Son mediciones del origen registradas en `.docs/DATOS_ORIGEN.md`; no deben confundirse con el total almacenado tras validación. El perfil se reproduce con `python3 scripts/perfilar_csv.py .data/*.csv --salida /tmp/perfil-admisiones.json`.

| Cohorte | Filas | Correlativos distintos | Nacimiento SIN REGISTRO |
| --- | ---: | ---: | ---: |
| 2015 | 20 277 | 6 148 | 14 192 |
| 2022 | 24 237 | 6 380 | 73 |
| 2024 | 13 863 | 3 861 | 15 |

## Interpretación

El volumen de evaluaciones supera ampliamente al de personas: contar filas como aspirantes inflaría la demanda. Además, hay 357 correlativos compartidos entre 2022 y 2024, por lo que sumar aspirantes por cohorte tampoco produce el total global.

En 2015, 20 275 filas declaran NINGUNO como carrera. Un ranking que lo tratase como carrera real estaría dominado por ausencia de información. Las comparaciones de demanda entre años deben reconocer esa diferencia de cobertura.

La ausencia de nacimiento es mucho mayor en 2015. Las edades promedio describen a quienes tienen información válida, no necesariamente a toda la población. Dos registros de 2015 tienen nacimiento 1900; se preservan las evaluaciones y se excluyen esas edades del análisis.

Los nombres truncados y categorías con espacios impiden reconstruir carreras o municipios con seguridad. Se conservan las etiquetas para evitar fusiones arbitrarias. Los patrones observados son descriptivos; no prueban que la institución, el sexo o el municipio causen diferencias de aprobación.

## Comparación de motores

MongoDB permite guardar cada evaluación con su procedencia y fuentes, consultar historiales y agrupar resultados. Neo4j expresa explícitamente recorridos entre aspirantes, evaluaciones y dimensiones, útiles para carreras compartidas y trayectorias. Ambos representan los mismos datos desde perspectivas diferentes.

La integración duplica almacenamiento y exige coordinar escrituras. No existe transacción distribuida: una carga puede completar MongoDB y quedar pendiente en Neo4j. Los identificadores deterministas, `upsert`, `MERGE` y el reintento del mismo archivo permiten recuperar esa situación. Esto aumenta la complejidad respecto de usar un solo motor y debe justificarse por el propósito comparativo del proyecto.

## Conclusiones

1. La identidad de evaluación y aspirante debe distinguirse antes de interpretar cualquier indicador.
2. La calidad y cobertura del CSV condicionan las conclusiones; los desconocidos deben explicarse.
3. MongoDB y Neo4j aportan representaciones complementarias, a cambio de sincronización y mantenimiento adicionales.
4. La carga idempotente facilita reintentos y preserva intentos legítimos con datos diferentes.

## Resultados procesados comprobados el 29 de octubre de 2026

`GET /api/estadisticas/dashboard` devolvió 44 505 evaluaciones distintas, 12 528 aspirantes únicos y 6 407 evaluaciones aprobadas (14,40 %). La base consultada contiene las cohortes 2015 y 2022; el archivo 2024 está disponible localmente pero no participa en estas cifras.

| Cohorte | Evaluaciones | Aprobadas | No aprobadas | Aprobación |
| --- | ---: | ---: | ---: | ---: |
| 2015 | 20 274 | 4 746 | 15 528 | 23,41 % |
| 2022 | 24 231 | 1 661 | 22 570 | 6,85 % |

Los totales coinciden con las filas de ambos archivos menos sus nueve duplicados exactos excedentes. La tasa observada de 2022 es 16,56 puntos porcentuales menor que la de 2015. Esto describe evaluaciones cargadas; no permite atribuir la diferencia a dificultad, enseñanza o capacidad, pues no se controlan composición, materias ni oportunidades. La tasa global se calcula ponderando por evaluaciones, no promediando las dos tasas anuales.
