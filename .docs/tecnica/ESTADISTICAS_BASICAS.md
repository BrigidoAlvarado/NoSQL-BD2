# Dashboard inicial y consultas MongoDB

Alcance: C06-01, C06-02, C06-04, C06-12, C06-18, C06-20 a C06-23. El dashboard consume `GET /api/estadisticas/dashboard`; la interfaz no consulta MongoDB directamente.

| Consulta | Resultado y unidad |
| --- | --- |
| M01 | Correlativos distintos agrupados por `procedencia.tipoInstitucion`. `SIN DATOS` se conserva como grupo. |
| M02 | Evaluaciones con `aprobacion = APROBADO` agrupadas por materia. |
| M04 | Porcentaje `evaluaciones aprobadas / evaluaciones` por materia. |
| M12 | Correlativos distintos por combinación de sexo y tipo de institución. Una persona puede aparecer en más de una combinación si el origen registra contextos diferentes. |
| M18 | Porcentaje de evaluaciones aprobadas por tipo de institución. |
| M20 | Evaluaciones aprobadas, no aprobadas y porcentaje por año de ingreso. |

Se utilizan tablas porque las categorías y etiquetas provienen del CSV y pueden cambiar entre archivos; la tabla conserva valores truncados, espacios o `SIN DATOS` sin fusionarlos. Las tarjetas superiores muestran correlativos únicos globales, evaluaciones distintas y el porcentaje global.

El resultado sin datos devuelve listas vacías y resumen en cero; Angular muestra una invitación a cargar CSV. El frontend muestra una recuperación explícita si la API no responde.

## Filtros y presentación actuales

El dashboard admite filtros HTTP opcionales que restringen documentos antes de agrupar y recalculan porcentajes y rankings. M06 filtrada es de lectura y no sobrescribe `resumen_carrera`. Las tablas se acompañan de barras de hasta ocho grupos por métrica. M21–M25 se solicitan por separado en Análisis adicionales. Véase [FILTROS_ANALITICOS.md](FILTROS_ANALITICOS.md) y el [catálogo](../manuales/CONSULTAS.md).

## Verificación

`MongoStatisticsIntegrationTest`, ejecutado el 23 de octubre de 2026 contra las cargas locales, confirmó que existen evaluaciones y que:

- la suma de M02 coincide con el total global de evaluaciones aprobadas;
- la suma de M20 coincide con el total de evaluaciones y aprobadas;
- la suma de los denominadores de M18 coincide con el total de evaluaciones.

La prueba no modifica MongoDB. `StatisticsControllerTests` valida el contrato HTTP y `dashboard.spec.ts` valida la visualización Angular con datos de prueba.

## Consultas avanzadas

El mismo endpoint incorpora M03, M05–M10, M13–M14 y M16–M17, M19. M03/M08/M16/M17/M19 cuentan correlativos distintos; M09 cuenta evaluaciones públicas por mes de **asignación**; M10 usa la primera cohorte observada por aspirante/carrera; M14 cuenta oportunidades distintas por aspirante, cohorte y materia. M05 y M07 incluyen cuántas observaciones no disponen de edad.

M06 recalcula `resumen_carrera` desde `evaluaciones` durante la solicitud del dashboard sin filtros y hace `replaceOne` idempotente por `{anioIngreso, carreraObjetivo}`. No suma cargas incrementalmente, por lo que reimportar un archivo no infla resultados. La tabla del dashboard se sirve del resultado recalculado y la colección conserva la misma información para consultas posteriores.

M17 devuelve los cinco municipios conocidos con mayor cantidad de aspirantes; M19 devuelve todas las carreras empatadas en el máximo de cada departamento. Las tablas preservan etiquetas originales y no fusionan espacios finales o valores desconocidos.
