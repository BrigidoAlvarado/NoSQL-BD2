# Carga de CSV y trazabilidad desde la interfaz

Alcance: C05-02 a C05-11 y mejoras de historial. La pantalla `/carga` envía archivos mediante `POST /api/cargas`, en partes multipart llamadas `files`. Angular solo consume `/api`; las credenciales de los motores permanecen en el backend.

## Carga y recuperación

`CsvUploadController` recibe HTTP; `CsvUploadService` valida archivos y adapta la respuesta; `CsvImportService` coordina el ETL. Cada archivo se procesa en el orden recibido y conserva su resultado. La petición admite 30 MB por archivo y 100 MB en total.

La respuesta HTTP 200 contiene `archivos`, con nombre, conteos leídos/procesados/rechazados, duración, estado, detalle y hasta los primeros 100 rechazos. Un HTTP 200 no implica éxito de todos los archivos: revisar su estado individual (`COMPLETA`, `SIN_FILAS_VALIDAS`, `RECHAZADA`, `FALLO_MONGO` o `PENDIENTE_NEO4J`).

El hash identifica el contenido. Una carga completa con reglas vigentes se reutiliza; con reglas anteriores o sin versión se reprocesa al reenviar el CSV. La versión actual es 2 y conserva sexo `SIN REGISTRO` como desconocido. Si falta sincronizar Neo4j, reenviar el mismo archivo tras recuperar el motor. Véase [ETL_CARGA.md](ETL_CARGA.md).

## Lecturas persistentes

| Endpoint | Resultado |
| --- | --- |
| `GET /api/cargas?page=0` | Historial de cargas por contenido, ordenado por inicio descendente y hash. |
| `GET /api/cargas/{hash}/detalle/rechazos?page=0` | Rechazos con línea, código, causa y valores disponibles. |
| `GET /api/cargas/{hash}/detalle/validos?page=0` | Evaluaciones distintas de MongoDB con las fuentes del archivo seleccionado. |
| `GET /api/cargas/{hash}/rechazos?limit=100` | Lectura acotada anterior; el límite se ajusta entre 1 y 500. |

Los endpoints paginados devuelven `items`, `total`, `page` y `size`. El tamaño es 20 y `page` comienza en cero; admite de 0 a 100000. Hash debe tener 64 caracteres hexadecimales y el detalle solo admite `rechazos` o `validos`; las entradas inválidas producen HTTP 400.

El historial tiene una entrada por contenido, no por intento. Los reintentos actualizan la misma carga. Los documentos válidos pueden ser menos que las filas válidas por la eliminación de duplicados exactos. El detalle no constituye una copia original de cada fila ni una comparación antes/después. La duración histórica no está disponible. Los fallos internos de motores se presentan mediante mensajes controlados.

## Verificación registrada

`CsvUploadControllerTests` y `upload.spec.ts` cubren cargas y respuestas por archivo. Las pruebas del historial cubren paginación, reintento y respuestas tardías. `AdditionalAnalysisIntegrationTest` registró 121 rechazos paginados sin pérdida ni duplicación y fuentes limitadas al archivo seleccionado, en una base temporal aislada. Evidencia y pendientes operativos: [VERIFICACION_ENTREGA.md](../evidencias/VERIFICACION_ENTREGA.md). Esta actualización documental no vuelve a ejecutar esas pruebas.
