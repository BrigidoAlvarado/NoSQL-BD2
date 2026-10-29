# Catálogo de consultas

M01–M10, M12–M14 y M16–M20 se obtienen juntos en `/api/estadisticas/dashboard`. M11 y M15 se obtienen por correlativo en `/api/busqueda/aspirantes/{correlativo}`. Código: `backend/src/main/java/com/nosql/apiadmisionesusac/statistics/MongoStatisticsService.java` y paquete `search`.

| ID | Resultado e interpretación |
| --- | --- |
| M01 | Aspirantes distintos por tipo de institución. |
| M02 | Evaluaciones aprobadas por materia. |
| M03 | Aspirantes con alguna aprobación por carrera y cohorte. |
| M04 | 100 × aprobadas / evaluaciones de la materia. |
| M05 | Edad promedio por aspirante/carrera, usando la primera cohorte observada. |
| M06 | Resumen materializado por carrera/cohorte: evaluaciones, aspirantes y aprobación. |
| M07 | Edad promedio de aprobados por carrera/tipo de institución, sin ponderar intentos. |
| M08 | Aspirantes aprobados por departamento, municipio y carrera. |
| M09 | Evaluaciones públicas por mes de asignación y materia. |
| M10 | Cinco carreras conocidas con mayor demanda de aspirantes de 16–18 años. |
| M11 | Historial del correlativo: materias, oportunidades y resultados. |
| M12 | Aspirantes distintos por sexo/tipo de institución. |
| M13 | Porcentaje de evaluaciones aprobadas por edad válida. |
| M14 | Promedio de oportunidades distintas por aspirante/cohorte/materia. |
| M15 | Información disponible de todas las evaluaciones del correlativo. |
| M16 | Aspirantes con no aprobación en oportunidad calendario 1, por carrera. |
| M17 | Cinco municipios conocidos con más aspirantes, conservando departamento. |
| M18 | Porcentaje de evaluaciones aprobadas por tipo de institución. |
| M19 | Carrera de máxima demanda por departamento; incluye empates. |
| M20 | Evaluaciones aprobadas/no aprobadas y tasa por cohorte. |

Las edades desconocidas no se sustituyen por cero en los registros; consultar columnas de observaciones con/sin edad al interpretar promedios. Los rankings excluyen categorías desconocidas según el filtro de cada implementación. Una persona puede figurar en varios grupos. Los resultados son descriptivos y no establecen causalidad.

## Neo4j

Se ejecutan individualmente en `/api/grafos/consultas/{id}`. No aceptan filtros; los filtros de la muestra visual son independientes. Código Cypher completo en `GraphExplorerService.query`, dentro del paquete `graph` del backend.

| ID | Recorrido y resultado |
| --- | --- |
| G01 | Evaluación → carrera conocida: cantidad de evaluaciones por carrera. |
| G02 | Materia ← evaluación → resultado: distribución de resultados. |
| G03 | Aspirante → evaluación aprobada → carrera y procedencia → municipio → departamento conocido: aspirantes distintos. |
| G04 | Aspirante → evaluaciones → carreras conocidas: pares de carreras compartidas. No demuestra el orden temporal del cambio. |
| G05 | Evaluación → carrera/materia/resultado: distribución por cohorte. |
| G06 | Evaluación → procedencia → tipo de institución y materia/resultado: evaluaciones por combinación. |
| G07 | Aspirante → evaluaciones → materia: grupos con más de una oportunidad distinta en la misma cohorte; promedio entre quienes reintentaron. |
| G08 | Aspirante → evaluación → carrera y procedencia → municipio/departamento: demanda territorial. Excluye departamentos y carreras desconocidos. |
| G09 | Aspirante → evaluación → carrera/materia/resultado: evaluaciones por sexo declarado en el registro. |
| G10 | Aspirante → evaluaciones → carrera/resultado: personas con ambos resultados para la misma carrera, considerando todas las cohortes. No implica mejora temporal. |

El grafo interactivo muestra entidades de una muestra de evaluaciones; las tablas consultan todo el conjunto. No comparar su cantidad de nodos directamente con totales de las tablas. Una respuesta sin filas es válida cuando no existen coincidencias.
