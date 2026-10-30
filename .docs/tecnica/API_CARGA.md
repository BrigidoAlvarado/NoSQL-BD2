# Carga de CSV desde la interfaz

Alcance: C05-02 a C05-11. La pantalla `/carga` envía los archivos elegidos con `POST /api/cargas` como partes multipart llamadas `files`. Angular solo se comunica con `/api`; no conoce ni usa credenciales de MongoDB o Neo4j.

El backend procesa cada archivo en el orden recibido usando `CsvImportService`, de modo que el resultado de un archivo no reemplaza el de otro. La respuesta HTTP 200 contiene `archivos`, con nombre, conteos leídos/procesados/rechazados, duración, estado, detalle y los primeros 100 rechazos. Las causas completas permanecen en MongoDB y se consultan con `GET /api/cargas/{sha256Archivo}/rechazos?limit=100`.

La interfaz muestra el detalle de cada rechazo con línea, código y causa. Puede comunicar `COMPLETA`, `SIN_FILAS_VALIDAS`, `RECHAZADA`, `FALLO_MONGO`, `PENDIENTE_NEO4J` y errores locales de archivo. La petición limita cada archivo a 30 MB y la petición completa a 100 MB.

Evidencia: la prueba de Angular simula dos archivos y verifica `FormData`, resultado y causa; `CsvUploadControllerTests` verifica dos resultados diferentes del backend y su rechazo identificado.
