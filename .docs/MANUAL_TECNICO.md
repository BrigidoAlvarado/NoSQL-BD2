# Manual técnico

## Instalación

Requisitos: Git, Java 25, Node.js compatible con `frontend/package.json`, npm 10 o posterior, Docker Engine y Compose v2. Python 3 es opcional para los validadores. Maven se descarga con `backend/mvnw`.

1. Clonar el repositorio y copiar `.env.example` a `.env`. Elegir contraseñas locales; no versionar `.env`.
2. Desde la raíz: `docker compose up -d --wait`. Aplicar los índices con los comandos de la sección «Esquemas de datos» del README.
3. Desde `backend/`: `./mvnw spring-boot:run`, con `JAVA_HOME` apuntando a Java 25.
4. Desde `frontend/`: `npm ci` y `npm start`.
5. Abrir `http://localhost:4200` y comprobar ambos motores. Seleccionar los CSV desde Carga de datos.

Puertos predeterminados: Angular 4200, API 8080, MongoDB 27017, Neo4j Browser 7474 y Bolt 7687. Los puertos y credenciales se configuran en `.env`. El backend importa ese archivo al arrancar desde `backend/`. Reiniciarlo después de cambios de configuración.

## Arquitectura y decisiones

Angular → HTTP `/api` → controladores Spring Boot → servicios Java → MongoDB/Neo4j. El navegador recibe resultados y nunca credenciales de las bases. Las rutas Angular se cargan bajo demanda. Las gráficas usan CSS y el grafo SVG nativo: no requieren Chart.js ni Cytoscape. Esta decisión reduce dependencias y permite explicar el código en la evaluación.

MongoDB conserva documentos por evaluación para agregaciones e historial. Neo4j permite recorrer las relaciones de esos mismos registros. Una evaluación no equivale a un aspirante: el identificador anónimo puede aparecer en varias materias, oportunidades y cohortes.

## ETL y modelos

`CsvRecordReader` lee CSV; `CsvEvaluationProcessor` valida encabezados, UTF-8 y tipos. `CsvImportService` coordina `EtlMongoStore` y `EtlNeo4jStore`. Los Python de `scripts/` sirven para perfilado y comprobaciones de desarrollo.

Fecha: ISO a fecha tipada. Oportunidad: entero positivo. Sexo y resultado se normalizan; categorías de carrera y procedencia conservan sus textos para evitar fusionar nombres truncados. `SIN REGISTRO` produce edad ausente; edades fuera de 0–100 también quedan ausentes. La edad es año de ingreso menos nacimiento. `NINGUNO` se conserva marcado como carrera desconocida. Los rechazos guardan línea, causa y valores.

MongoDB usa `evaluaciones`, `cargas`, `rechazos_carga` y `resumen_carrera`. Cada evaluación tiene `_id` SHA-256 de sus doce valores canónicos, `aspiranteId`, fecha, sexo, nacimiento, edad, materia, oportunidad, cohorte, aprobación, carrera, procedencia y fuentes. `upsert` evita duplicados; fuentes se acumulan sin repetir. Los índices ejecutables están en `backend/src/main/resources/db/mongodb/01_indices.js`.

Neo4j usa Aspirante, Evaluacion, Materia, Carrera, Resultado, ProcedenciaEducativa, TipoInstitucion, Municipio y Departamento. Recorrido: Aspirante–REALIZO→Evaluacion; esta apunta a materia, carrera, resultado y procedencia; procedencia apunta a tipo y municipio; municipio apunta a departamento. Las claves únicas y los índices están en `backend/src/main/resources/db/neo4j/01_esquema.cypher`. `MERGE` evita duplicados de nodos y relaciones. Procedencia representa tipo y ubicación, pues el CSV no identifica colegios concretos.

El hash del archivo identifica una carga. Si ya está COMPLETA se reutiliza. Si MongoDB terminó y Neo4j falló, queda PENDIENTE_NEO4J: reenviar el mismo archivo reintenta la sincronización. No hay transacción distribuida entre ambos motores. M06 recalcula resúmenes al solicitar el dashboard y los guarda por cohorte/carrera; no es una publicación atómica de toda la colección.

## API y consultas

| Endpoint | Uso |
| --- | --- |
| `GET /api/health` | Estado independiente de ambos motores; 200 o 503. |
| `POST /api/cargas` | Multipart `files`; hasta 30 MB por archivo y 100 MB por solicitud. |
| `GET /api/cargas/{hash}/rechazos?limit=100` | Rechazos registrados. |
| `GET /api/estadisticas/dashboard` | 18 consultas estadísticas, sin filtros HTTP. |
| `GET /api/busqueda?criterio=carrera&termino=INGENIERIA&limite=50` | Búsqueda; criterios aspirante, carrera, institucion, materia, municipio, departamento. |
| `GET /api/busqueda/aspirantes/{correlativo}` | M11 y M15: historial y detalle. |
| `GET /api/grafos?anio=2024&limite=40` | Subgrafo; filtros adicionales carrera, resultado y departamento exactos. |
| `GET /api/grafos/consultas/G01` | Análisis completo; IDs G01–G10, sin filtros del subgrafo. |

Las implementaciones consultables están en `MongoStatisticsService`, `ApplicantSearchService` y `GraphExplorerService`. El catálogo e interpretación están en [CONSULTAS.md](CONSULTAS.md).

## Verificaciones y operación

Desde `backend/`: `./mvnw verify`. Las integraciones están desactivadas por defecto: con bases activas y CSV cargados ejecutar `./mvnw -Detl.integration=true -Dsearch.integration=true -Dstatistics.integration=true -Dgraph.integration=true test`.

Desde `frontend/`: `npm test` y `npm run build`. Desde la raíz: `python3 -m unittest discover -s scripts -p 'test_*.py'`.

`docker compose stop` conserva datos. `docker compose up -d` reinicia servicios. Los volúmenes persisten; no eliminarlos al comprobar reinicios. Para producción servir `frontend/dist/admisiones-usac/browser` con fallback a `index.html` y proxy `/api` al backend. El proxy de desarrollo no se aplica en producción.

Los errores se traducen a mensajes en español con reintentos; el estado de carga distingue parcialidad de éxito. Una respuesta vacía puede ser válida. Consultar [MANUAL_USUARIO.md](MANUAL_USUARIO.md) y conservar evidencia de pruebas reales antes de declarar la entrega validada.
