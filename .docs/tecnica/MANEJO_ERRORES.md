# Manejo de errores y continuidad

La API devuelve errores controlados en la forma `{ codigo, mensaje, ocurridoEn }`. Los mensajes no incluyen credenciales, URI, consultas ni trazas internas. Angular los convierte mediante su interceptor HTTP en mensajes visibles por pantalla y mantiene los controles de reintento disponibles.

| Situación | HTTP / código | Comportamiento visible y recuperable |
| --- | --- | --- |
| Archivo ausente, demasiado grande o solicitud inválida | 400 | Se explica el problema; la selección se conserva cuando ya existe. |
| CSV vacío, extensión errónea o ilegible | Resultado por archivo | La carga señala el archivo y su estado; las demás filas/archivos siguen siendo visibles. |
| Estructura o filas CSV inválidas | `RECHAZADA` / `SIN_FILAS_VALIDAS` | Se muestran estado, conteos y causas de rechazo por línea. |
| MongoDB no disponible | 503 / `MONGODB_NO_DISPONIBLE` | No se declara éxito de la operación. Se pide conservar el trabajo y reintentar. En carga puede quedar el estado `FALLO_MONGO`. |
| Neo4j no disponible | 503 / `NEO4J_NO_DISPONIBLE` | El explorador informa la indisponibilidad y el resto de la aplicación sigue navegable. En carga queda `PENDIENTE_NEO4J`; MongoDB ya se completó y el mismo archivo permite reintento. |
| Consulta, búsqueda o visualización con respuesta inválida | 4xx/5xx o validación de interfaz | La pantalla conserva navegación y controles; explica el problema y permite reintentar. |

`/api/health` conserva la verificación independiente de MongoDB y Neo4j: entrega 503 y el estado de ambos si uno falla. No sustituye las verificaciones que realizan los flujos funcionales.

## Evidencia de QA

- `ApiExceptionHandlerTests`: clasifica fallos simulados del driver de MongoDB y Neo4j como 503 y comprueba que no se filtren detalles internos.
- Pruebas Angular: simulan MongoDB no disponible durante una carga y Neo4j no disponible durante la consulta del grafo; verifican el mensaje y que se pueda reintentar.
- Las validaciones de CSV previas y el detalle de rechazos se cubren en `upload.spec.ts` y en las pruebas del ETL.

Para la comprobación operativa antes de la demostración: iniciar contenedores, consultar `/api/health`, detener temporalmente solo un motor desde una terminal de prueba, actualizar la página y comprobar el mensaje correspondiente; luego iniciar el motor y pulsar **Comprobar conexión** o **Reintentar**. No detener ambos motores ni eliminar volúmenes.
