# Frontend de Admisiones USAC

Aplicación Angular de estadísticas, carga CSV, búsqueda y exploración de grafos.

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
- `src/app/features/`: estadísticas, carga CSV, búsqueda y grafos.
- `src/app/shared/`: filtros de categorías y reglas de presentación reutilizables.
- `src/app/layout/`: elementos de la estructura y diagnóstico de conexiones.
- `src/app/core/`: cliente HTTP e interceptor con mensajes de error en español. Los consumidores deben mostrar `ApiError.message`; `ApiError.response` conserva los detalles HTTP.
- `src/environments/`: URL base de la API por entorno. No almacenes credenciales aquí: el código del frontend es público.
- Interfaz adaptable a escritorio y móvil, navegación con teclado y estados vacíos explícitos.
- Se fija la dependencia transitiva `@modelcontextprotocol/sdk` del CLI en `1.31.0` para corregir la alerta `GHSA-6qxp-vccf-f47h`. Es una herramienta de desarrollo, no una integración MCP de la aplicación.

## Integración con el backend

El cliente HTTP usa `/api`. Durante `npm start`, `proxy.conf.json` redirige `/api/**` a `http://localhost:8080`, sin eliminar el prefijo. El backend implementa `GET /api/health`; el panel «Estado de los servicios» lo consulta al iniciar la aplicación y permite reintentar. Muestra los estados independientes de MongoDB y Neo4j incluso con HTTP 503 y mensajes claros ante errores de red o respuestas inválidas. La espera del cliente está limitada a 45 segundos. Las pantallas consultan los endpoints funcionales de la API.

En producción, el proxy de Angular no se utiliza. Configura el servidor web para reenviar `/api/**` al backend y servir `index.html` para las rutas del frontend. Si despliegas la API en otro origen, actualiza `environment.ts` y configura CORS en el backend.

La selección CSV verifica únicamente extensión y tamaño no vacío. Acepta varios archivos y permite limpiar la selección. La validación de estructura y registros corresponde al ETL del backend; el botón Cargar y procesar envía los archivos.

## Funcionalidad disponible

- **Carga de datos:** selección múltiple de CSV, resumen inmediato e historial persistente con válidos y rechazos paginados.
- **Estadísticas:** 23 consultas MongoDB con selector Mxx, filtros, tabla y gráfica; M21–M25 están en Análisis adicionales.
- **Búsqueda:** coincidencias e historial completo del aspirante, correspondientes a M11/M15. En total, la aplicación implementa 25 consultas MongoDB.
- **Explorador de grafos:** muestra de 5 a 40 evaluaciones, selectores de carrera y departamento, zoom, detalle y visibilidad por tipo de entidad. Incluye 10 consultas Cypher, G01–G10, con selección individual, filtros, tabla y gráficas.

Todo acceso a los datos pasa por la API. Los filtros de MongoDB se aplican antes de agrupar y recalculan las métricas; los de las consultas Cypher seleccionan los grupos calculados y conservan sus conteos. Las opciones de los selectores se conservan para permitir combinar filtros o limpiarlos.

Las gráficas de barras y proporciones se dibujan con HTML/CSS. El lienzo interactivo de relaciones utiliza `vis-network`; las gráficas de las consultas Cypher reutilizan el componente de barras. No se conecta el navegador directamente a MongoDB ni a Neo4j.

El logo de la universidad está en `src/app/logo-usac/` y Angular lo publica en `assets/logo-usac/` mediante la configuración de recursos de `angular.json`.
