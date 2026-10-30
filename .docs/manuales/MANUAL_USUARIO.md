# Manual de usuario

1. Iniciar los componentes siguiendo el manual técnico. Abrir `http://localhost:4200`; comprobar API, MongoDB y Neo4j en Estado de los servicios.
2. En Carga de datos seleccionar uno o varios CSV UTF-8 y pulsar Cargar y procesar. Esperar los resultados por archivo: filas leídas, procesadas, rechazadas, duración y estado. Consultar las causas de rechazo antes de corregir el archivo. Se muestran hasta los primeros 100 rechazos.
3. Si aparece PENDIENTE_NEO4J, recuperar Neo4j y reenviar exactamente el archivo. Una carga completa repetida reutiliza su resultado; no duplica evaluaciones.
4. En Estadísticas elegir Panorama, Aprobación, Carreras, Perfil y edades o Procedencia. Abrir el selector de consulta: se muestra una tabla y su gráfica. La tabla contiene las cifras completas. La gráfica actual muestra hasta ocho categorías, por lo que no debe interpretarse como ranking completo.
5. En Búsqueda elegir criterio y escribir el término. Correlativo exige coincidencia exacta; los demás criterios admiten coincidencias parciales sin distinguir mayúsculas. Abrir el aspirante para consultar todas sus evaluaciones. Tipo de institución significa público/privado u otra categoría declarada, no nombre de colegio.
6. En Explorar grafo introducir filtros opcionales y aplicarlos. La vista usa una muestra de hasta 40 evaluaciones. Limpiar elimina los filtros. Los análisis de Carreras, Trayectorias, Resultados y Procedencia consultan el conjunto completo y tienen actualización independiente.

## Interpretación

Una aprobación corresponde a una evaluación y no demuestra admisión universitaria. Una persona puede pertenecer a varios grupos: sumar aspirantes de carreras distintas puede contar a alguien varias veces. Los porcentajes de aprobación usan evaluaciones como denominador. La edad es aproximada al año de ingreso y excluye edades ausentes. Mes corresponde a asignación, no necesariamente a realización del examen.

## Recuperación

Ante un error de conexión comprobar los servicios y pulsar Reintentar. Si el archivo está vacío, excede los límites o tiene formato inválido, corregirlo y volver a seleccionarlo. No borrar volúmenes de las bases para resolver una conexión. La navegación permanece disponible aunque una consulta falle.

## Preparación de la demostración

Mostrar una carga múltiple y sus rechazos, repetir una carga, consultar las veinte consultas MongoDB y diez Cypher, buscar un correlativo con varias evaluaciones y cambiar un filtro del grafo. Comparar los totales antes y después de reiniciar los servicios. Practicar explicar el hash, `upsert`, `MERGE`, los denominadores y el tratamiento de desconocidos. La comprensión y la entrega deben ser confirmadas por el estudiante.


## Cierre de mejoras del panel — 2026-10-29

Completados los comportamientos pendientes de interacción de grafos y pruebas de M21–M25 y paginación ETL. Evidencia: prueba `AdditionalAnalysisIntegrationTest` contra base MongoDB temporal aislada, eliminada al finalizar; 121 rechazos paginados sin pérdida ni duplicación, fuentes filtradas y resultados conocidos para cinco agregaciones. Angular: selección y visualización de las cinco consultas; Chromium: grafo 5/10/40 evaluaciones, detalle y cinco consultas sin errores JavaScript. Capturas locales `.docs/evidencias/grafos/`. Biblioteca e interacción documentadas en `.docs/tecnica/BIBLIOTECA_GRAFOS.md`. El archivo temporal conserva el historial y contiene una sección final de cierre. Ninguna evidencia local se incorpora a Git.


## Selección individual de consultas por sección

Las secciones Carreras, Trayectorias, Resultados y Procedencia ofrecen un selector accesible con código Gxx y nombre completo. Se muestra una sola tabla; Angular solicita únicamente la consulta seleccionada y conserva sus resultados al regresar. El código también aparece en el encabezado de la tabla. La selección se guarda en `consulta` de la URL, por ejemplo `/grafos?seccion=procedencia&consulta=G08`. Un código ajeno a la sección utiliza la primera consulta válida. Cada consulta conserva sus propios estados de carga, error y reintento; una respuesta tardía se guarda sin sustituir la tabla seleccionada. Explorar grafo mantiene su funcionamiento independiente.

Verificación: `graph.spec.ts` cubre las diez opciones, una tabla visible, selección mediante el control, URL, caché, enlace directo G08, código de otra sección, reintento y respuestas tardías. Suite completa: 37 pruebas Angular correctas y compilación de producción correcta.


## Filtros parametrizados del backend

Avance anterior de filtros locales guardado en `5a0cd95`, antes de cambiar el enfoque por instrucción del usuario. Los selectores ahora envían parámetros HTTP opcionales y las consultas existentes los aplican sin crear variantes por combinación. MongoDB: `$match` antes de agrupar, con recálculo de totales/porcentajes/rankings; M06 filtrada no sobrescribe resumen_carrera. Neo4j: filtros parametrizados sobre grupos de cada Gxx, conservando sus conteos. Se mantienen opciones base para limpiar y combinar filtros. Documentación y ejemplos en `.docs/tecnica/FILTROS_ANALITICOS.md`.

Verificación: 38 pruebas Angular y compilación de producción correctas. Backend: 21 pruebas ejecutadas sin errores ni fallos y ocho opcionales omitidas; incluye pruebas de filtros MongoDB en base temporal aislada y filtros de categorías sobre G01–G10 cuando hay resultados, más valores literales semejantes a inyección.
