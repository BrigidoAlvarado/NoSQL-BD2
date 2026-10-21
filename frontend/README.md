# Frontend de Admisiones USAC

Implementación inicial basada en [el brief del proyecto](../.docs/BRIEF_PROYECTO.md).

## Ejecutar

Requiere Node.js `^20.19.0`, `^22.12.0` o `^24.0.0` y npm 10 o posterior. Se eligió Angular 21 por su [compatibilidad con el entorno](https://angular.dev/reference/versions). Las versiones instaladas quedan fijadas en `package-lock.json`.

Desde la raíz del repositorio:

```bash
cd frontend
npm ci
npm start
```

Abre `http://localhost:4200`.

Si tu entorno tiene npm 9, puedes instalar sin actualizarlo globalmente con `npm exec --yes --package=npm@10 -- npm ci`. La instalación inicial se verificó con npm 10.9.9 porque npm 9.2.0 falló internamente al resolver dependencias.

Para verificar y compilar:

```bash
npm test
npm run build
```

La compilación de producción genera `dist/admisiones-usac/browser/`.

## Estructura y decisiones de diseño

- Angular con componentes standalone, detección de cambios sin Zone.js, TypeScript estricto y rutas cargadas bajo demanda.
- `src/app/features/`: estadísticas y selección de CSV.
- `src/app/shared/`: vista inicial de búsqueda y grafos.
- `src/app/core/`: cliente HTTP e interceptor con mensajes de error en español. Los consumidores deben mostrar `ApiError.message`; `ApiError.response` conserva los detalles HTTP.
- `src/environments/`: URL base de la API por entorno. No almacenes credenciales aquí: el código del frontend es público.
- Interfaz adaptable a escritorio y móvil, navegación con teclado y estados vacíos explícitos.
- Se fija la dependencia transitiva `@modelcontextprotocol/sdk` del CLI en `1.31.0` para corregir la alerta `GHSA-6qxp-vccf-f47h`. Es una herramienta de desarrollo, no una integración MCP de la aplicación.

## Integración con el backend

El cliente HTTP usa `/api`. Durante `npm start`, `proxy.conf.json` redirige `/api/**` a `http://localhost:8080`, sin eliminar el prefijo. El backend implementa `GET /api/health`; el panel «Estado de los servicios» lo consulta al iniciar la aplicación y permite reintentar. Muestra los estados independientes de MongoDB y Neo4j incluso con HTTP 503 y mensajes claros ante errores de red o respuestas inválidas. La espera del cliente está limitada a 45 segundos. Las pantallas funcionales todavía no consultan evaluaciones ni simulan resultados.

En producción, el proxy de Angular no se utiliza. Configura el servidor web para reenviar `/api/**` al backend y servir `index.html` para las rutas del frontend. Si despliegas la API en otro origen, actualiza `environment.ts` y configura CORS en el backend.

La selección CSV verifica únicamente extensión y tamaño no vacío. Acepta varios archivos y permite limpiar la selección. La validación de estructura y registros corresponde al ETL del backend; seleccionar un archivo no lo envía ni lo almacena.

## Alcance pendiente

Esta base todavía requiere implementar la carga y sus resúmenes, las 20 consultas MongoDB y sus visualizaciones, la búsqueda y el explorador de grafos con filtros y al menos cinco consultas Cypher. El frontend accederá a estos servicios exclusivamente mediante la API.

La [arquitectura](../.docs/ARQUITECTURA.md) documenta la elección de Chart.js y Cytoscape.js para las próximas visualizaciones. Aún no están instaladas. Las [verificaciones de la base](../.docs/VERIFICACION_BASE.md) incluyen evidencia y un recorrido manual.
