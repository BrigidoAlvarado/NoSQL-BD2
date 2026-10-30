# Temporal — Implementación de la visualización del ETL

Estado: implementado y verificado. Esta nota es local, está fuera de Git y puede eliminarse después de trasladar el alcance y la evidencia al seguimiento definitivo.

## Objetivo

Permitir consultar desde Carga de datos la trazabilidad almacenada por el ETL, incluso después de recargar la página. Apoya C05-04 a C05-10 y los requisitos de ETL del brief, sección 7. Las vistas adicionales de historial y muestra de válidos son decisiones de diseño; no constituyen nuevos requisitos del enunciado.

## Funcionalidad que ya existe

- Carga de uno o varios CSV mediante `POST /api/cargas`.
- Resumen inmediato por archivo: estado, registros leídos, procesados, rechazados y duración.
- Desplegable con línea, código y causa de los primeros 100 rechazos.
- Consulta de rechazos mediante `GET /api/cargas/{sha256Archivo}/rechazos?limit=100`.
- MongoDB conserva `cargas`, `rechazos_carga` y `evaluaciones.fuentes` con hash del archivo, nombre y línea.

No volver a implementar esas funciones: ampliar su acceso y presentación.

## 1. Historial persistente de cargas — prioridad alta

- Añadir `GET /api/cargas` para listar cargas guardadas, con paginación y orden por fecha reciente. Definir y documentar límites y orden estable.
- Mostrar archivo, fecha de inicio/actualización, estado, filas leídas, válidas y rechazadas. Permitir abrir el detalle usando el hash, no el nombre del archivo.
- Conservar el resumen inmediato y actualizar el historial al terminar una carga.
- Mantener el historial disponible al recargar o volver a entrar en la sección.
- Contemplar estados intermedios y parciales: RECIBIDA, MONGO_LISTO, PENDIENTE_NEO4J, FALLO_MONGO, RECHAZADA, SIN_FILAS_VALIDAS y COMPLETA.
- La duración actual se calcula al responder HTTP; no está guardada en `cargas`. Si se quiere mostrar una duración histórica, persistirla y definir su significado para reintentos. Para cargas anteriores sin duración mostrar «No disponible».
- La colección identifica el contenido por hash y actualiza el mismo documento al reintentar: el historial será de cargas por contenido, no una bitácora de todos los intentos. Explicarlo en la interfaz; una bitácora por intento sería un alcance adicional.

## 2. Detalle de rechazos — prioridad alta

- Presentar una tabla con línea, código, causa y valores originales disponibles.
- Incluir `values` en el modelo Angular de rechazo; el backend ya dispone de esos valores para filas interpretables con errores de validación.
- Añadir paginación real al endpoint. Aumentar únicamente `limit` no permite consultar todos los rechazos ni navegar de forma estable.
- Mostrar total y cantidad visible; permitir avanzar y retroceder sin cargar toda la colección.
- Diferenciar «sin rechazos» de un error al consultar. Permitir reintentar sin perder la selección.
- Para columnas incorrectas los valores actualmente se guardan como mapa vacío: mostrar «Valores no disponibles». Si se desea conservar la fila original, modificar expresamente su captura y almacenamiento; no inventar información.
- Si el CSV completo es ilegible o tiene encabezado/comillas inválidos, mostrar el motivo de rechazo del archivo desde `cargas.detalleError`; no simular una tabla de rechazos por fila.

## 3. Muestra de registros válidos — prioridad media

- Añadir `GET /api/cargas/{sha256Archivo}/validos` con límite/paginación y orden estable para consultar evaluaciones vinculadas al archivo en `fuentes`.
- Mostrar archivo y línea de origen, correlativo, fecha, materia, oportunidad, año de ingreso, resultado, carrera y procedencia. Presentar nacimiento y edad ausentes de forma explícita.
- Usar únicamente las fuentes del archivo seleccionado; una evaluación puede estar vinculada a varios archivos o líneas.
- Explicar que «filas válidas» puede superar «evaluaciones distintas»: los duplicados exactos se almacenan como una evaluación con varias fuentes. No presentar una diferencia de conteos como pérdida de registros.
- Indicar si la muestra corresponde a documentos guardados en MongoDB y si la sincronización Neo4j sigue pendiente. Una carga parcial no debe aparecer como éxito completo.
- Los datos válidos originales completos no se guardan actualmente como una copia separada de la fila. No ofrecer comparación «antes/después» sin ampliar primero la captura y persistencia. Archivo/línea y datos procesados sí tienen trazabilidad disponible.

## Estructura de implementación

- Backend dentro de la funcionalidad `upload`: controlador HTTP, servicio de consulta del historial, acceso a MongoDB y DTO separados por responsabilidad. No convertir `CsvImportService` en un servicio de presentación.
- La lectura del historial no debe modificar datos, repetir el ETL ni recalcular estadísticas.
- Frontend dentro de `features/upload/`: modelos en `upload.models.ts`, componentes de historial, detalle de rechazos y muestra de válidos. La página coordina selección y solicitudes.
- Tablas y detalles desplegables; no hace falta una biblioteca de gráficas para esta mejora.
- Aplicar las reglas de AGENTS: archivos pequeños, comentarios en español, código directo y pruebas con resultados conocidos.
- Mantener Angular → API → bases; no exponer credenciales ni detalles internos de excepciones. Revisar `detalleError` antes de devolverlo al navegador, pues puede contener mensajes internos.
- Preservar los contratos existentes o documentar y actualizar todos los consumidores afectados por la paginación.

## Criterios de aceptación

- [x] Una carga anterior aparece después de recargar Angular.
- [x] Dos archivos con el mismo nombre y distinto contenido se distinguen por su hash.
- [x] Repetir el mismo contenido respeta la idempotencia y no aparenta una carga nueva independiente.
- [x] Se pueden consultar más de 100 rechazos mediante páginas sin omisiones ni repeticiones.
- [x] Un rechazo muestra línea, causa y valores cuando estén disponibles.
- [x] Un rechazo de archivo completo muestra el motivo sin inventar filas.
- [x] La muestra válida corresponde al archivo seleccionado y explica duplicados/fuentes.
- [x] Los estados parciales y las duraciones ausentes se interpretan correctamente.
- [x] Seleccionar otra carga no permite que una respuesta tardía muestre datos de la selección anterior.
- [x] Las vistas vacías, errores de conexión y reintentos mantienen la aplicación utilizable.
- [x] Pruebas del backend con datos temporales aislados y limpieza segura.
- [x] Pruebas Angular del historial, selección, paginación, errores y respuestas tardías.
- [x] Compilaciones correctas e inspección visual del flujo completo.
- [x] Actualizar checklist, plan y manuales locales con la evidencia real.

## Orden recomendado

1. Consultas y API del historial; visualización Angular y pruebas.
2. Paginación y detalle completo de rechazos; pruebas de límites y recuperación.
3. Muestra de válidos y explicación de duplicados/sincronización.
4. Validación visual y actualización de documentación. Guardar cambios de código en commits coherentes; conservar esta nota y el resto de `.docs` fuera de Git.

## Adición solicitada — Identificadores visibles de consultas MongoDB

Estado: implementado. El usuario solicita identificar fácilmente cada consulta del dashboard mediante su ID del enunciado. Usar el formato estable `M01`–`M20`, coherente con el código y el checklist, junto con su nombre descriptivo.

### Cambios de interfaz

- En el selector de cada tema, mostrar el identificador antes del nombre: «M03 · Aprobados por carrera y año».
- En el encabezado de la tabla seleccionada, mostrar una etiqueta visible con el mismo ID junto al título completo. Debe ser legible y no depender de pasar el cursor o interpretar un color.
- Identificar también la gráfica asociada, por ejemplo «M03 · Lectura visual», para relacionarla con la tabla sin ambigüedad.
- Actualmente el ID aparece en el texto de ayuda (`hint`); conservar la explicación de la consulta y evitar repetir el identificador innecesariamente en esa misma tarjeta.
- Declarar un campo explícito para el identificador en `DashboardQuery`, por ejemplo `mongoId`. Mantener el `id` actual de selección/URL (como `aprobados-carrera`) para no romper enlaces. No obtener el ID analítico analizando el texto de ayuda.
- Actualizar los cinco catálogos en `features/dashboard/queries/` y las plantillas correspondientes. No cambiar numeración, consultas, resultados ni agrupaciones.
- El dashboard contiene M01–M10, M12–M14 y M16–M20: 18 consultas. M11 y M15 permanecen en Búsqueda; no crear consultas duplicadas o entradas vacías para completar una secuencia de veinte opciones.

### Criterios de aceptación

- [x] Las dieciocho opciones muestran su identificador Mxx correcto y su nombre.
- [x] El ID de la opción elegida coincide con la etiqueta visible de su tabla y su gráfica.
- [x] Cada ID corresponde a la consulta del brief y aparece una sola vez en el catálogo del dashboard.
- [x] Cambiar tema, seleccionar consulta o abrir un enlace directo mantiene el ID correcto.
- [x] Los identificadores son accesibles por teclado y lectores de pantalla; no se comunican únicamente mediante color.
- [x] La prueba del dashboard verifica los IDs explícitos de las dieciocho consultas, además de los títulos y componentes asociados.
- [x] Pruebas, compilación y revisión visual correctas. Registrar evidencia local al completar la mejora.

Este cambio puede realizarse en un commit de interfaz independiente del historial y la trazabilidad del ETL. La adición está implementada.

## Adición solicitada — Cinco consultas MongoDB adicionales

Estado: implementado. El usuario solicita ampliar las veinte consultas del enunciado a veinticinco. M21–M25 son mejoras del proyecto, no requisitos del PDF. Conservar M01–M20 y sus resultados sin renumerarlos.

### Consultas propuestas

| ID | Análisis | Unidad y reglas | Visualización propuesta |
| --- | --- | --- | --- |
| M21 | Aprobación por oportunidad y materia, separada por año de ingreso. | Contar evaluaciones por cohorte/materia/número de oportunidad; devolver aprobadas, no aprobadas, total y porcentaje. Denominador: evaluaciones del grupo. No confundir número calendario de oportunidad con cantidad de intentos personales usados. | Tabla y barras de porcentaje. |
| M22 | Aspirantes que aprueban una materia después de haberla reprobado en una oportunidad anterior de la misma cohorte. | Agrupar primero por aspirante/cohorte/materia. Identificar al menos un NO APROBADO con oportunidad menor que alguna evaluación APROBADO. Contar una sola vez a cada aspirante por cohorte/materia. Devolver aspirantes con algún fallo, aspirantes recuperados y porcentaje de recuperación sobre los que tuvieron algún fallo. | Tabla y barras por materia/cohorte. |
| M23 | Distribución de aspirantes según cantidad de materias distintas evaluadas por cohorte. | Una observación por aspirante/cohorte; contar materias distintas sin inflar el total por reintentos. Agrupar por cohorte y cantidad de materias; devolver aspirantes y porcentaje sobre aspirantes de esa cohorte. No implica haber aprobado las materias ni haber sido admitido. | Tabla y barras de distribución. |
| M24 | Intervalo entre primera y última asignación de aspirantes por cohorte. | Calcular días entre fechas mínima y máxima por aspirante/cohorte. Para el promedio usar solo aspirantes con al menos dos fechas de asignación distintas; informar cuántos se incluyeron y cuántos quedaron fuera. Devolver promedio, mínimo y máximo por cohorte. El origen registra asignación, no fecha efectiva de examen ni tiempo de preparación. | Tabla y barras de promedio en días. |
| M25 | Calidad y disponibilidad de información por cohorte. | Contar evaluaciones totales, sin edad válida, con carrera desconocida y con alguna dimensión de procedencia desconocida; publicar cantidad y porcentaje de cada condición sobre evaluaciones de la cohorte. Usar campos derivados/marcas del ETL. Las condiciones pueden superponerse: una fila puede pertenecer a varias. | Tabla y barras independientes; evitar pastel porque los porcentajes no son categorías excluyentes. |

### Implementación e integración

- Una clase por consulta, con prefijo M21–M25, dentro de `statistics/queries/`, siguiendo el patrón simple `execute()` y los auxiliares existentes cuando corresponda.
- Construir agregaciones sobre `evaluaciones`; estas cinco consultas serán de lectura y no crearán nuevas colecciones auxiliares.
- Definir DTO con nombres claros y unidades explícitas. Para promedios sin observaciones válidas mostrar «Sin datos»; no inventar un promedio de cero. Definir también la representación de porcentajes sin denominador.
- Integrar las cinco respuestas en el API de estadísticas y actualizar `DashboardResponse`, modelos y validación Angular. Mantener los campos y contratos de las consultas existentes.
- Crear una subsección «Análisis adicionales» en Estadísticas con cinco opciones y selección de una sola consulta. Cada opción, tabla y gráfica debe mostrar su ID M21–M25 conforme a la adición anterior.
- La aplicación tendrá 25 consultas MongoDB: 23 en Estadísticas y M11/M15 en Búsqueda. Las diez Cypher permanecen como análisis Neo4j independientes.
- Documentar filtros, unidad de conteo, denominadores, desconocidos e interpretación. Los resultados no permiten afirmar admisión ni relaciones causales.

### Verificación y criterios de aceptación

- [x] Cinco consultas diferentes de M01–M20, implementadas, accesibles por API y visibles con ID.
- [x] M21: repetir una persona en varias evaluaciones cuenta evaluaciones, no personas; porcentajes con denominadores conocidos.
- [x] M22: un fallo seguido de aprobación en oportunidad mayor cuenta una recuperación; dos fallos o aprobación seguida solo de fallo no. No cruzar materias o cohortes; no duplicar a quien aprueba varias veces. Una nueva aprobación posterior a un fallo debe reconocerse aunque exista también una aprobación previa.
- [x] M23: varios reintentos de la misma materia cuentan una materia; una segunda materia aumenta la cantidad. Los grupos de una cohorte suman sus aspirantes distintos.
- [x] M24: probar fechas iguales, fechas distintas y aspirantes de cohortes diferentes; comprobar días exactos e incluidos/excluidos. Una sola fecha no produce un intervalo observado válido para esta métrica.
- [x] M25: probar una evaluación con varias condiciones simultáneas; porcentajes individuales correctos sin forzar que sumen 100 %.
- [x] Tratar ausencia de registros, valores desconocidos y fallos de consulta con estados claros y recuperables.
- [x] Pruebas de integración con muestra pequeña y resultados esperados, datos aislados y limpieza segura.
- [x] Pruebas Angular de selección, identificación y representación de las cinco consultas nuevas; preservar las dieciocho actuales del dashboard y M11/M15.
- [x] Compilaciones y revisión visual correctas; actualizar catálogo, manuales, checklist y evidencia local.

Las cinco consultas están implementadas, con API, visualización y pruebas de integración.

## Adición solicitada — Municipios sin repetición del departamento

Estado: implementado. Mejora de legibilidad acordada con el usuario; cambia únicamente la presentación, sin modificar el ETL, los valores almacenados en MongoDB/Neo4j ni las agrupaciones o resultados de las consultas.

### Reglas de presentación

- Cuando el departamento esté visible en la misma fila o etiqueta, quitar del municipio solo el prefijo que repita exactamente ese departamento seguido del separador « - ». Comparar sin distinguir mayúsculas/minúsculas y tolerar espacios alrededor del separador.
- Ejemplos con departamento «GUATEMALA»: «GUATEMALA - CIUDAD» se muestra como «CIUDAD»; «GUATEMALA - CIUDAD CAPITAL ZONA 1» se muestra como «CIUDAD CAPITAL ZONA 1».
- Conservar zonas y demás detalles. No equiparar «CIUDAD» con «CIUDAD CAPITAL ZONA 1», unir categorías ni sumar sus resultados.
- Si el municipio se muestra sin contexto de departamento, conservar su nombre completo o acompañarlo explícitamente del departamento.
- Conservar nombres sin ese prefijo, prefijos de otro departamento y valores desconocidos. No quitar «GUATEMALA» cuando sea todo el nombre del municipio ni producir etiquetas vacías.
- Mantener las identidades originales para claves, selección, filtros, solicitudes HTTP y seguimiento de filas. La simplificación visual no cambia los términos de búsqueda enviados al backend.

### Alcance y estructura

- Centralizar la regla en un auxiliar pequeño de presentación compartido en Angular, con municipio y departamento como entradas explícitas. Evitar reemplazos globales sobre textos arbitrarios.
- Aplicarla a las tablas de procedencia M08/M17, sus gráficas, la tabla G08 y el detalle del aspirante cuando el departamento esté disponible y visible.
- Revisar las etiquetas del grafo antes de aplicarla: si un nodo puede verse sin su departamento, conservar el contexto territorial y el título completo. No recortar automáticamente todos los nombres de nodos.
- Conservar las respuestas originales del API y documentar esta decisión como presentación, no como normalización geográfica de los datos.

### Criterios de aceptación

- [x] Los dos ejemplos muestran el municipio abreviado y el departamento correspondiente.
- [x] Tabla y gráfica usan la misma regla y conservan el contexto territorial.
- [x] Las zonas, nombres sin prefijo, valores desconocidos y municipios homónimos de departamentos distintos siguen siendo distinguibles.
- [x] Los valores originales, las claves, los filtros, los conteos y las agrupaciones permanecen iguales.
- [x] Comprobar la regla con ejemplos conocidos y revisar visualmente tablas, gráficas y detalle; compilación Angular correcta.

Esta adición está implementada en la presentación Angular. El archivo permanece local y fuera de Git.

## Adición solicitada — Legibilidad e interacción del explorador de grafos

Estado: implementado. La distribución circular actual acumula etiquetas y cruza muchas relaciones. Ampliar el explorador para controlar la muestra y navegar sus conexiones. Son mejoras de diseño sobre el grafo y los filtros existentes del brief, secciones 8 y 10; no nuevos requisitos obligatorios del PDF.

### Primera etapa propuesta

- Añadir un control deslizante que sustituya el límite fijo de 40 evaluaciones. Rango inicial propuesto: 5–40, con valor inicial de 10, sujeto a verificación de rendimiento y legibilidad. Identificarlo como «Evaluaciones de la muestra» y permitir ajustarlo por teclado.
- El límite controla evaluaciones, no un recorte arbitrario de nodos. Una evaluación puede generar varios nodos compartidos y relaciones. Mantener sus conexiones completas dentro de la muestra devuelta.
- Mostrar el valor elegido junto al control y los conteos reales de la respuesta por separado: «10 evaluaciones · 34 nodos» es un ejemplo, no una equivalencia fija. Informar cuando los filtros produzcan menos evaluaciones de las solicitadas.
- Aplicar el tamaño y los filtros mediante «Aplicar filtros», para no ejecutar una consulta por cada movimiento del deslizador. Enviar el valor seleccionado como parámetro `limite` y validar el rango también en el backend.
- Sustituir los campos de texto exacto de carrera y departamento por selectores con «Todas»/«Todos». Obtener opciones reales y sin duplicados mediante la API, no desde los nodos de la muestra ni desde listas manuales. Conservar los filtros de año y resultado.
- Mostrar las carreras completas usando las equivalencias de presentación acordadas, pero conservar los valores originales como valores de selección y parámetros de consulta. Si varias claves originales tienen la misma etiqueta visible, distinguirlas sin fusionarlas silenciosamente.
- Sustituir la posición circular fija por una distribución por fuerzas con separación de nodos. Elegir y justificar una biblioteca de grafos mantenida que resuelva distribución e interacción; revisar compatibilidad y licencia al implementar. Evitar desarrollar un motor físico propio.
- Incorporar zoom, desplazamiento y un botón «Ajustar vista». Mantener un área de grafo utilizable en tamaños de pantalla habituales, sin depender de reducir el zoom del navegador.
- Mostrar inicialmente etiquetas de carreras y ubicaciones; revelar las demás al seleccionar un nodo o al acercarse. Conservar acceso al nombre completo sin depender únicamente del cursor.
- Al seleccionar un nodo, resaltar sus vecinos directos y relaciones, atenuar el resto y mostrar un panel de detalle con tipo, identificador y propiedades disponibles. Permitir cerrar el detalle y restablecer la vista.
- Indicar claramente que el grafo representa una muestra filtrada. Los conteos visibles no equivalen a totales del dataset y sus filtros no modifican las consultas analíticas G01–G10.

### Segunda etapa opcional

- Añadir controles por tipo de nodo si la primera etapa no ofrece suficiente claridad, por ejemplo para concentrarse en aspirantes, evaluaciones y carreras.
- Ocultar un tipo solo afecta a la vista: ocultar también sus relaciones incidentes y mostrar los conteos visibles frente a los recibidos. No crear conexiones artificiales entre nodos para reemplazar los ocultos.
- Mantener colores y leyenda coherentes, acompañados de texto para que el tipo o la selección no dependan solo del color.

### Organización y manejo de estados

- La página de grafos coordina carga de opciones, filtros, tamaño, solicitudes y estados. Mantener el dibujo y la interacción en el componente del lienzo; separar el panel de detalle si su responsabilidad lo justifica.
- El backend obtiene las opciones y ejecuta consultas parametrizadas. Conservar el contrato del grafo cuando sea posible, las identidades de nodos y relaciones y los datos originales.
- Gestionar la carga y los fallos de los selectores con mensajes y reintento. No confundir una lista vacía con un error de conexión.
- Evitar que una respuesta anterior sustituya una muestra solicitada después. Al cambiar los datos, limpiar o validar la selección y liberar los recursos de la biblioteca al destruir el componente.
- Mantener el código simple conforme a AGENTS; no concentrar consultas, filtros, cálculo de posiciones y detalle en un único archivo.

### Criterios de aceptación

- [x] La vista inicial solicita 10 evaluaciones y muestra los conteos reales de evaluaciones y nodos.
- [x] El deslizador permite seleccionar el rango acordado y «Aplicar filtros» envía ese límite junto con los demás filtros; el backend rechaza valores fuera de rango de forma controlada.
- [x] Carreras y departamentos se seleccionan desde opciones reales de la API; «Todas»/«Todos» omite el filtro correspondiente y «Limpiar» restablece el tamaño inicial y los filtros.
- [x] Las etiquetas visibles no cambian los valores originales enviados al API.
- [x] Distribución, zoom, desplazamiento y ajuste de vista permiten inspeccionar la muestra sin la acumulación de etiquetas del círculo actual.
- [x] Seleccionar un nodo permite reconocer sus conexiones directas y consultar sus datos completos; cerrar el detalle recupera la vista general.
- [x] Los estados de carga, sin resultados, error y reintento son claros; una respuesta tardía no reemplaza la selección vigente.
- [x] La interfaz comunica que se explora una muestra y las consultas analíticas G01–G10 conservan su alcance.
- [x] Si se implementan filtros de tipos, no quedan relaciones colgantes ni se crean conexiones ficticias.
- [x] Verificar límites, filtros y opciones mediante API; probar la coordinación Angular y revisar visualmente muestras de 5, 10 y 40 evaluaciones, con y sin filtros. Compilaciones correctas.
- [x] Registrar evidencia y actualizar manuales, checklist y plan locales al implementar.

Implementar primero el tamaño de muestra, selectores, distribución por fuerzas, navegación y detalle. Evaluar después los filtros por tipo. La implementación utiliza vis-network y está documentada en BIBLIOTECA_GRAFOS.md.



## Evidencia final y decisiones de implementación

- Historial, rechazos y válidos: páginas de 20 registros, selección por hash, estados parciales, valores ausentes y descarte de respuestas tardías. El detalle paginado se ofrece en `/api/cargas/{hash}/detalle/{validos|rechazos}`; se conserva el endpoint previo de rechazos para compatibilidad.
- M21–M25: API `/api/estadisticas/adicionales/{id}` independiente del dashboard original, DTO explícitos por consulta, modelos y validadores Angular. Esta decisión evita ejecutar las cinco consultas cuando solo se necesita una y conserva los contratos previos.
- Trazabilidad: DTO de carga, rechazo, evaluación, fuente y página; modelos en `upload.models.ts`. No se exponen campos MongoDB nuevos por accidente.
- Identificadores, municipios e interacción del grafo implementados, incluidos controles opcionales por tipo. Biblioteca: `.docs/tecnica/BIBLIOTECA_GRAFOS.md`. Capturas: `.docs/evidencias/grafos/`.
- Pruebas contra MongoDB en bases temporales aisladas y eliminadas: M21–M25, separación de materias/cohortes, recuperación única aunque haya varias aprobaciones, fallo posterior a aprobación, promedios nulos y 121 rechazos en siete páginas sin duplicación.
- Validación Angular: respuestas incompletas, valores fuera de rango y promedios nulos, además de pruebas de selección, paginación y respuestas tardías.
- Revisión Chromium ya realizada: 5/10/40 evaluaciones → 21/33/98 nodos, selección de detalle y consultas M21–M25 sin errores JavaScript.
- Duración histórica: se muestra no disponible; su persistencia era condicional. No se crean valores retrospectivos.

El alcance funcional y los ajustes técnicos del archivo quedan completados. Este documento permanece fuera de Git.
