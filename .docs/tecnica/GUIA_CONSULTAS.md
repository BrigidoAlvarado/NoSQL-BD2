# Guía para explicar y modificar consultas

## Dónde buscar

- MongoDB M01–M10, M12–M14 y M16–M20: `backend/src/main/java/com/nosql/apiadmisionesusac/statistics/queries/`.
- Historial M11 e información M15: `backend/src/main/java/com/nosql/apiadmisionesusac/search/queries/`.
- Neo4j G01–G10: `backend/src/main/resources/queries/neo4j/`. `explorar_grafo.cypher` corresponde a la muestra visual filtrada.
- Selección y gráfica Angular: `frontend/src/app/features/dashboard/queries/<tema>.queries.ts`.
- Tabla Angular: `frontend/src/app/features/dashboard/components/<tema>-tables.html`.

## Responsabilidades

Cada archivo Mxx contiene una consulta y su conversión a resultados. `execute()` recibe la colección MongoDB y devuelve filas tipadas. Son clases sin estado con métodos estáticos: no necesitan interfaces, fábricas ni reflexión. `MongoStatisticsService` las llama explícitamente y construye el dashboard.

`QueryValues` reúne conversiones numéricas, porcentajes y operaciones pequeñas compartidas. `AgeQuerySupport` contiene la regla compartida de edad de referencia de M05/M07. Cambiarla afecta ambas consultas. `CareerSummaryStore` guarda los resultados calculados por M06.

`M11ApplicantHistory` recupera las evaluaciones ordenadas. M15 usa ese historial y `ApplicantResultMapper` para construir la información completa. La búsqueda general permanece en `ApplicantSearchService`.

`GraphQueryCatalog` registra ID, título, descripción y ruta de cada Cypher. `GraphQueryService` lee el recurso y ejecuta la consulta; obtiene las columnas del resultado real. `GraphExplorerService` coordina la muestra visual y `GraphResponseMapper` convierte los registros en nodos y aristas.

`CsvUploadController` recibe HTTP; `CsvUploadService` valida el archivo y adapta la respuesta. El paquete `etl` mantiene lectura, transformación, coordinación y persistencia. Los DTO de carga están en `upload/dto`.

En Angular, cada página gestiona el estado de su pantalla; `.models.ts` declara los datos; `.html` presenta el contenido. El dashboard tiene catálogos y tablas por tema. `MetricChart` dibuja la gráfica, `GraphCanvas` posiciona y dibuja el grafo, `ApplicantDetailView` presenta M11/M15. `layout/connection-status` muestra el diagnóstico general; `core` conserva HTTP y errores.

## Modificar una consulta MongoDB

1. Buscar el archivo por identificador, por ejemplo `M04ApprovalBySubject.java`.
2. Identificar colección, etapas de `pipeline` y unidad de conteo.
3. Cambiar el filtro o agrupación. Mantener claro el denominador si devuelve un porcentaje.
4. Si cambia la forma de salida, ajustar su record en `DashboardResponse`, el modelo Angular y la tabla/gráfica del tema.
5. Ejecutar las pruebas. Para un filtro sobre un parámetro nuevo, añadir el argumento de forma explícita al método, coordinador y controlador; no concatenar entradas del usuario en código de consulta.

Para crear otra consulta: copiar el archivo más parecido, asignarle un identificador nuevo, adaptar `execute()` y registrar la llamada en el coordinador. Si se publica en el dashboard, agregar su campo a `DashboardResponse`, `DashboardData`, validación de respuesta, catálogo y tabla del tema. No es necesario crear una interfaz ni una clase base.

## Modificar una consulta Cypher

1. Abrir su archivo `Gxx_*.cypher` y cambiar `MATCH`, `WHERE`, `RETURN` u `ORDER BY`.
2. Ejecutar en Neo4j Browser o consultar `/api/grafos/consultas/Gxx`.
3. Las columnas devueltas por `RETURN` aparecen automáticamente en la tabla. Para un título amigable de columna nueva, editar `graph.presentation.ts`.
4. No renombrar ID ni recurso si solo se modifica su lógica.

El ejecutor soporta parámetros mediante `execute(id, Map<String,Object>)`. El endpoint actual solo recibe ID: para recibir un filtro HTTP también hay que declararlo en el controlador y pasarlo al ejecutor. Usar `$parametro` en Cypher.

Para crear otra consulta: agregar el `.cypher`, registrarlo en `GraphQueryCatalog`, agregar título en `graph.presentation.ts` y ubicar su ID en `graph.sections.ts`. La tabla ya es dinámica.

Los recursos se leen en cada ejecución; en desarrollo Maven/IDE debe copiar el recurso actualizado al classpath. Un JAR empaquetado requiere reconstruirse para incluir los cambios.

## Comprobaciones

Desde `backend/` con Java 25 y bases disponibles:

```bash
./mvnw -Dstatistics.integration=true -Dgraph.integration=true -Dsearch.integration=true -Detl.integration=true test
```

`IndividualQueriesIntegrationTest` usa cuatro evaluaciones en una colección exclusiva y la elimina al terminar. Comprueba las veinte consultas MongoDB con resultados conocidos. `CypherQueriesIntegrationTest` usa nodos marcados con un identificador único y los elimina en `finally`; verifica G01–G10. No son pruebas de rendimiento.

Desde `frontend/`: `npm test` y `npm run build`. La prueba del dashboard selecciona sus dieciocho consultas y comprueba que cada elección presenta su título, un contenedor de tabla y una gráfica.

## Decisiones conservadas

Esta refactorización conserva contratos, unidades y reglas del proyecto. M06 sigue recalculándose al abrir el dashboard. Las gráficas mantienen el límite de ocho categorías. Los cambios funcionales sobre estas decisiones deben hacerse en otro commit y verificarse aparte.
