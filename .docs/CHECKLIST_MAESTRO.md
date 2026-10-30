# Checklist maestro — Proyecto Final Bases de Datos 2

Fuente principal: [BRIEF_PROYECTO.md](BRIEF_PROYECTO.md). Documento de origen: [enunciado_proyecto.pdf](enunciado_proyecto.pdf).

Estado inicial revisado: 20 de octubre de 2026. Entrega indicada en el brief: viernes 30 de octubre de 2026. Calificación: pendiente de definir en el enunciado.

## Cómo utilizar este checklist

Refactorización de apoyo académico (29 de octubre): las consultas conservan los identificadores M01–M20/G01–G10 y los contratos existentes. Se separan por archivo y responsabilidad; véase `tecnica/GUIA_CONSULTAS.md`. Las tareas funcionales completadas mantienen su historial; esta reorganización no cierra las verificaciones operativas o personales pendientes.

Estado revisado el 30 de octubre de 2026: `.docs/` se versionó por primera vez en la rama temporal `tmp/docs`, commit `14808c1` (`documentacion publica temporal`), confirmado por el usuario. Las menciones a documentación exclusivamente local en el historial describen etapas anteriores. Los manuales están en `manuales/`, la evidencia en `evidencias/`, los datos en `datos/` y las notas de implementación en `tecnica/`. Este versionado no confirma publicación en GitHub ni entrega final.

- `[x]` indica una tarea existente y respaldada por código o verificaciones anteriores de esta conversación. No implica que se haya repetido la prueba el día de esta revisión.
- `[ ]` indica trabajo pendiente o cuyo cumplimiento todavía necesita evidencia. Una pantalla vacía, una conexión al IDE o una consulta ejecutada únicamente en consola no completan la funcionalidad solicitada.
- **R**: requisito del brief. **D**: decisión de diseño que debe definirse y justificarse. **V**: comprobación propuesta para demostrar el cumplimiento; no es una tecnología o requisito adicional impuesto por el enunciado.
- Marca una tarea al completar su alcance y registrar su evidencia: archivo, consulta, resultado, captura o prueba. Actualiza el documento durante el desarrollo.
- Para cerrar una consulta funcional deben existir su implementación, acceso mediante el backend y visualización en la aplicación; documenta parámetros, interpretación y un resultado comprobado.

## 1. Base del proyecto y arquitectura

Referencia: brief, secciones 3, 4 y 11.

- [x] **C01-01** **R** Separar interfaz gráfica, lógica de aplicación y bases de datos: `frontend/`, `backend/` y servicios de bases de datos.
- [x] **C01-02** **D** Elegir Angular como frontend y Spring Boot como backend; configuración y ejecución inicial documentadas en los README.
- [x] **C01-03** **R** Mantener el frontend sin conexiones directas a MongoDB ni Neo4j; cliente HTTP dirigido a la API.
- [x] **C01-04** **R** Configurar MongoDB y Neo4j e integrarlos con el backend. Evidencia: `backend/src/main/resources/application.yaml` y paquete `health`.
- [x] **C01-05** **V** Comprobar ambas conexiones desde `/api/health`: en la verificación anterior respondió HTTP 200 con ambos motores en `UP`.
- [x] **C01-06** **V** Configurar contenedores con almacenamiento persistente y variables externas. Evidencia: `compose.yaml`, `.env.example` y README.
- [x] **C01-07** **V** Crear navegación inicial para estadísticas, carga, búsqueda y grafos. Las rutas actuales incluyen estadísticas, carga e historial, búsqueda y exploración de grafos.
- [x] **C01-08** **R** Backend funcional de carga, transformación, consultas, búsqueda y errores. Evidencia: 21 pruebas con integraciones correctas el 29 de octubre de 2026; `.docs/evidencias/VERIFICACION_ENTREGA.md`.
- [x] **C01-09** **D** Documentar y justificar la arquitectura definitiva y las bibliotecas de gráficas y grafos elegidas. Evidencia: `.docs/tecnica/ARQUITECTURA.md`; gráficas Angular/CSS y grafo canvas con `vis-network`, documentados también en `.docs/tecnica/BIBLIOTECA_GRAFOS.md`. Verificaciones de infraestructura: `.docs/evidencias/VERIFICACION_BASE.md`.
- [ ] **C01-10** **V** Verificar el flujo completo Angular → API → MongoDB/Neo4j → API → interfaz con datos reales. Hay pruebas de integración y evidencia visual parcial del grafo. Falta registrar la aceptación conjunta del recorrido de carga múltiple, consultas y visualización; el diagnóstico o una prueba parcial no cierran esta tarea.

## 2. Datos de origen y comprensión del problema

Referencia: brief, secciones 5 y 6.

- [x] **C02-01** **R** Identificar y obtener los CSV indicados para el proyecto desde la fuente oficial señalada en el brief; confirmar los archivos y años que corresponden. Evidencia: tres archivos locales confirmados por el usuario (2015, 2022 y 2024), recursos identificados en el catálogo oficial y `.docs/datos/PROCEDENCIA_CSV.json`. La comparación byte a byte remota no se completó por HTTP 500; no es criterio adicional de cierre.
- [x] **C02-02** **V** Registrar procedencia, nombre, año, cantidad de filas, encabezados, codificación y delimitador de cada archivo utilizado. Evidencia: `.docs/datos/DATOS_ORIGEN.md`, `.docs/datos/PERFIL_CSV.json` y `scripts/perfilar_csv.py`.
- [x] **C02-03** **R** Considerar los doce campos del dataset: `fecha_asignacion`, `sexo`, `anio_nacimiento`, `materia`, `numero_de_fecha_de_evaluacion`, `anio_de_ingreso`, `aprobacion`, `carrera_objetivo`, `departamento_institucion_educativa`, `municipio_institucion_educativa`, `tipo_institucion_educativa` y `correlativo_aspirante`. Evidencia: `.docs/datos/DATOS_ORIGEN.md`, `.docs/datos/PERFIL_CSV.json` y `scripts/perfilar_csv.py`.
- [x] **C02-04** **R** Reconocer que una fila es una evaluación y que un aspirante puede tener varias evaluaciones o intentos. Evidencia: `.docs/datos/DATOS_ORIGEN.md`, `.docs/datos/PERFIL_CSV.json` y `scripts/perfilar_csv.py`.
- [x] **C02-05** **R** Utilizar `correlativo_aspirante` como identificador anónimo del aspirante. Evidencia: `.docs/datos/DATOS_ORIGEN.md`, `.docs/datos/PERFIL_CSV.json` y `scripts/perfilar_csv.py`.
- [x] **C02-06** **V** Revisar aspirantes repetidos, evaluaciones repetidas, valores especiales, campos vacíos y diferencias entre archivos antes de cerrar los modelos. Evidencia: `.docs/datos/DATOS_ORIGEN.md`, `.docs/datos/PERFIL_CSV.json` y `scripts/perfilar_csv.py`.
- [x] **C02-07** **D** Definir cómo se contará cada métrica: aspirantes únicos, evaluaciones, intentos y resultados; documentar los denominadores de los porcentajes. Decisión documentada en `.docs/datos/DATOS_ORIGEN.md`; aplicación funcional en ETL y consultas implementada; véanse `tecnica/ETL_CARGA.md` y `manuales/CONSULTAS.md`.
- [x] **C02-08** **D** Definir el cálculo de edad y la fecha o año de referencia, incluyendo el tratamiento de años de nacimiento desconocidos o inconsistentes. Decisión documentada en `.docs/datos/DATOS_ORIGEN.md`; aplicación funcional en ETL y consultas implementada; véanse `tecnica/ETL_CARGA.md` y `manuales/CONSULTAS.md`.

## 3. Modelos de MongoDB y Neo4j

Referencia: brief, secciones 6, 9, 10 y 11.

- [x] **C03-01** **D** Diseñar y documentar el esquema documental: colecciones, documentos, tipos, referencias o datos embebidos y ejemplos. Evidencia: `.docs/tecnica/MODELOS_DATOS.md` y `backend/src/main/resources/db/mongodb/01_indices.js`.
- [x] **C03-02** **D** Justificar cómo el modelo permite resolver las 20 consultas MongoDB y distinguir evaluaciones de aspirantes únicos. Evidencia: `.docs/tecnica/MODELOS_DATOS.md` y criterios de `.docs/datos/DATOS_ORIGEN.md`.
- [x] **C03-03** **D** Definir los índices MongoDB necesarios y justificar las consultas que apoyan. Evidencia: `.docs/tecnica/MODELOS_DATOS.md`; aplicados y comprobados con `01_indices.js`.
- [x] **C03-04** **D** Definir la estructura y estrategia de actualización de la colección auxiliar `resumen_carrera`. Evidencia: `.docs/tecnica/MODELOS_DATOS.md`; la materialización se implementará en B04.
- [x] **C03-05** **R** Diseñar un grafo que represente los elementos relevantes del problema y las evaluaciones de un mismo aspirante. Evidencia: `.docs/tecnica/MODELOS_DATOS.md` y `backend/src/main/resources/db/neo4j/01_esquema.cypher`.
- [x] **C03-06** **D** Definir etiquetas, propiedades, relaciones, identificadores, restricciones e índices de Neo4j y documentar un ejemplo de recorrido. Evidencia: `.docs/tecnica/MODELOS_DATOS.md`; restricciones e índices aplicados y comprobados.
- [x] **C03-07** **D** Definir cómo representar aspirantes, carreras, instituciones educativas, municipios, departamentos, materias y resultados según la información real disponible. No inventar identificadores de instituciones que el CSV no proporcione. Evidencia: `.docs/tecnica/MODELOS_DATOS.md`; se usa `ProcedenciaEducativa`, no un colegio inventado.
- [x] **C03-08** **R** Evitar la creación innecesaria de nodos duplicados en Neo4j. Implementación: `MERGE` por claves y relaciones en `EtlNeo4jStore`; restricciones únicas en el esquema. El protocolo de comprobación del estado real está en `tecnica/ETL_CARGA.md`.
- [x] **C03-09** **D** Documentar la estrategia para evitar duplicados e implementar cargas repetibles en ambos motores, sin eliminar intentos legítimos.
- [x] **C03-10** **V** Validar ambos modelos con una muestra real antes de realizar la carga completa. Evidencia: `scripts/validar_modelos_reales.py` con una fila de 2024; datos temporales aislados y limpiados, verificado cero remanentes en ambos motores.

## 4. ETL y calidad de los registros

Referencia: brief, sección 7.

- [x] **C04-01** **R** Implementar lectura de CSV desde el backend.
- [x] **C04-02** **R** Validar la estructura y los campos de los registros.
- [x] **C04-03** **R** Convertir fechas, años y números a los tipos definidos para el modelo cuando corresponda.
- [x] **C04-04** **R** Identificar y tratar valores faltantes o especiales.
- [x] **C04-05** **R** Definir, documentar e implementar el tratamiento de `SIN REGISTRO` en `anio_nacimiento`.
- [x] **C04-06** **R** Definir, documentar e implementar el tratamiento de `APROBADO` y `NO APROBADO` en `aprobacion`.
- [x] **C04-07** **R** Definir, documentar e implementar el tratamiento de `NINGUNO` en `carrera_objetivo`.
- [x] **C04-08** **R** Estandarizar los valores que lo necesiten y documentar las reglas aplicadas.
- [x] **C04-09** **D** Justificar qué registros incompletos se conservan, transforman o excluyen. El enunciado no obliga a eliminar todos los incompletos.
- [x] **C04-10** **R** Registrar cuáles registros son válidos y cuáles son inválidos.
- [x] **C04-11** **R** Permitir identificar los registros rechazados y registrar la causa de cada rechazo.
- [x] **C04-12** **R** Almacenar los datos procesados en MongoDB.
- [x] **C04-13** **R** Generar y almacenar los nodos, propiedades y relaciones correspondientes en Neo4j.
- [x] **C04-14** **D** Definir cómo reportar y recuperar una carga si un motor se actualiza y el otro falla; evitar presentar esa operación como exitosa.
- [x] **C04-15** **V** Comprobar registros válidos, fechas o números inválidos, valores especiales, filas repetidas y archivos sin registros útiles.
- [x] **C04-16** **V** Repetir una carga y verificar que la estrategia documentada evita duplicados sin perder evaluaciones diferentes de un mismo aspirante. Evidencia existente: integración de repetición del mismo archivo y prueba unitaria de identidad ante cambios de fecha. Falta ampliar la integración con archivos diferentes que compartan filas y comprobaciones de relaciones/fuentes; no interpretar este cierre histórico como una auditoría exhaustiva. Véase `tecnica/ETL_CARGA.md`.

## 5. Carga de archivos desde la interfaz

Referencia: brief, secciones 7 y 8.

- [x] **C05-01** **V** Permitir la selección local de varios `.csv`, detectar archivos vacíos y limpiar la selección. Evidencia: `frontend/src/app/features/upload/`. Todavía no hay envío ni ETL.
- [x] **C05-02** **R** Enviar uno o varios CSV seleccionados desde Angular al backend.
- [x] **C05-03** **R** Procesar los archivos recibidos mediante el ETL y cargar ambos motores.
- [x] **C05-04** **R** Mostrar por archivo el nombre.
- [x] **C05-05** **R** Mostrar por archivo el total de registros leídos.
- [x] **C05-06** **R** Mostrar por archivo los registros procesados correctamente.
- [x] **C05-07** **R** Mostrar por archivo los registros rechazados o con error.
- [x] **C05-08** **R** Mostrar por archivo el tiempo de procesamiento.
- [x] **C05-09** **R** Mostrar por archivo el estado final de la operación.
- [x] **C05-10** **R** Hacer identificables los rechazos y sus causas mediante los resultados de la carga.
- [x] **C05-11** **V** Verificar una carga de varios archivos con resultados distintos y que cada resumen corresponda al archivo correcto.

## 6. Dashboard y las 20 consultas MongoDB

Referencia: brief, secciones 8 y 9. Cada casilla cubre la consulta, su acceso por API, su visualización y su documentación. La forma de agregación y de visualización es una decisión de diseño. Las veinte consultas se visualizan en la aplicación: dieciocho en Estadísticas y M11/M15 en Búsqueda; no se afirma que las veinte estén en la misma pantalla del dashboard.

- [x] **C06-01** **R · M01** Cantidad de aspirantes agrupados por tipo de institución educativa.
- [x] **C06-02** **R · M02** Cantidad de evaluaciones aprobadas agrupadas por materia.
- [x] **C06-03** **R · M03** Cantidad de aspirantes aprobados agrupados por carrera objetivo y año de ingreso.
- [x] **C06-04** **R · M04** Porcentaje de aprobación por materia.
- [x] **C06-05** **R · M05** Edad promedio de aspirantes agrupada por carrera objetivo.
- [x] **C06-06** **R · M06** Crear la colección auxiliar `resumen_carrera` con información resumida por carrera a partir de los datos procesados, y visualizar sus resultados desde la aplicación.
- [x] **C06-07** **R · M07** Edad promedio de aspirantes aprobados, agrupada por carrera objetivo y tipo de institución educativa.
- [x] **C06-08** **R · M08** Cantidad de aprobados según municipio de la institución educativa y carrera objetivo.
- [x] **C06-09** **R · M09** Cantidad de evaluaciones por mes y materia, solo para instituciones públicas.
- [x] **C06-10** **R · M10** Top 5 de carreras objetivo más demandadas por aspirantes de 16 a 18 años.
- [x] **C06-11** **R · M11** Historial de desempeño de un aspirante: evaluaciones, materias, intentos y resultados. Evidencia: `GET /api/busqueda/aspirantes/{correlativo}` y tabla de historial.
- [x] **C06-12** **R · M12** Cantidad de aspirantes agrupados por sexo y tipo de institución educativa.
- [x] **C06-13** **R · M13** Porcentaje de aprobación por edad.
- [x] **C06-14** **R · M14** Número promedio de oportunidades de evaluación usadas por aspirantes para cada materia.
- [x] **C06-15** **R · M15** Toda la información disponible de un aspirante identificado por `correlativo_aspirante`. Evidencia: detalle de búsqueda y `.docs/tecnica/BUSCADOR_ASPIRANTES.md`.
- [x] **C06-16** **R · M16** Carreras con mayor cantidad de aspirantes que no aprobaron en su primer intento.
- [x] **C06-17** **R · M17** Top 5 de municipios de procedencia de instituciones educativas con más aspirantes registrados.
- [x] **C06-18** **R · M18** Porcentaje de aprobación, comparado por tipo de institución educativa.
- [x] **C06-19** **R · M19** Carreras objetivo con mayor demanda dentro de cada departamento de procedencia de la institución educativa.
- [x] **C06-20** **R · M20** Cantidad y porcentaje de evaluaciones aprobadas y no aprobadas, agrupadas por año de ingreso.
- [x] **C06-21** **R** Mostrar los resultados MongoDB en un dashboard que facilite su interpretación; no usar las tarjetas vacías actuales como evidencia de cumplimiento.
- [x] **C06-22** **D** Elegir para cada consulta una representación adecuada: gráfica, tabla, indicador, tarjeta o listado. No se exige una misma visualización para todas. Mejora UI/UX de escritorio: barras, porcentajes y cifras exactas; cinco subsecciones temáticas sustituyen el desplegable inicial. Investigación y distribución en `manuales/MANUAL_USUARIO.md`.
- [x] **C06-23** **V** Contrastar resultados con conteos conocidos de una muestra; comprobar denominadores, agrupaciones, edades desconocidas, ausencia de resultados y porcentajes con denominador cero.

## 7. Explorador de grafos y consultas Cypher

Referencia: brief, secciones 8 y 10. Los temas de las cinco consultas los define el estudiante; el brief no impone cinco temas específicos.

- [x] **C07-01** **R · G01** Definir e implementar la primera consulta Cypher útil para el problema, documentar qué obtiene e integrarla con la API y la aplicación.
- [x] **C07-02** **R · G02** Definir e implementar una segunda consulta Cypher distinta, documentar qué obtiene e integrarla con la API y la aplicación.
- [x] **C07-03** **R · G03** Definir e implementar una tercera consulta Cypher distinta, documentar qué obtiene e integrarla con la API y la aplicación.
- [x] **C07-04** **R · G04** Definir e implementar una cuarta consulta Cypher distinta, documentar qué obtiene e integrarla con la API y la aplicación.
- [x] **C07-05** **R · G05** Definir e implementar una quinta consulta Cypher distinta, documentar qué obtiene e integrarla con la API y la aplicación.
- [x] **C07-06** **R** Demostrar en el conjunto de consultas el uso de nodos, relaciones, propiedades, filtros y recorridos del grafo.
- [x] **C07-07** **R** Visualizar gráficamente al menos un grafo obtenido de Neo4j desde la interfaz del proyecto.
- [x] **C07-08** **R** Implementar al menos un filtro que modifique la visualización del grafo.
- [x] **C07-09** **D** Documentar los criterios de exploración, los filtros y la biblioteca de visualización elegida. Organización actual: cuatro subsecciones, consultas por demanda y errores independientes; evidencia en `manuales/MANUAL_USUARIO.md`.
- [x] **C07-10** **V** Comprobar que los filtros modifican la consulta y el grafo mostrado; manejar resultados sin nodos o relaciones.

## 8. Buscador

Referencia: brief, sección 8.

- [x] **C08-01** **R** Implementar búsqueda real en la información almacenada mediante el backend y mostrar sus resultados en Angular. Evidencia: `/api/busqueda` y `frontend/src/app/features/search/`.
- [x] **C08-02** **D** Definir y documentar los criterios disponibles. El brief recomienda aspirante, carrera objetivo, institución educativa, materia, municipio y departamento; estos seis criterios no son una lista obligatoria cerrada. Evidencia: `.docs/tecnica/BUSCADOR_ASPIRANTES.md`.
- [x] **C08-03** **D** Elegir cómo representar la búsqueda por institución educativa según los campos reales disponibles. Evidencia: se usa tipo de institución porque el CSV no contiene nombre de centro.
- [x] **C08-04** **V** Verificar búsquedas con coincidencias, sin coincidencias y con entradas inválidas. Evidencia: pruebas `search.spec.ts` y `ApplicantSearchIntegrationTest`.
- [x] **C08-05** **V** Comprobar que los resultados diferencian al aspirante de sus distintas evaluaciones. Evidencia: tabla de historial y prueba de integración con dos evaluaciones.

## 9. Errores y continuidad de la aplicación

Referencia: brief, secciones 4 y 8.

- [x] **C09-01** **V** Disponer de un cliente HTTP e interceptor inicial con mensajes de error en español. Evidencia: `frontend/src/app/core/`.
- [x] **C09-02** **V** Informar el estado independiente de ambos motores en `/api/health`, con HTTP 503 si alguna comprobación falla. Esto no sustituye el manejo de errores de las demás funciones.
- [x] **C09-03** **R** Mostrar claramente errores de carga de archivos. Evidencia: códigos por archivo y `ApiExceptionHandler`.
- [x] **C09-04** **R** Mostrar claramente errores de procesamiento de registros y sus causas. Evidencia: conteos, estados y rechazos por línea de ETL.
- [x] **C09-05** **R** Manejar errores de conexión con MongoDB en los flujos funcionales. Evidencia: 503 `MONGODB_NO_DISPONIBLE`.
- [x] **C09-06** **R** Manejar errores de conexión con Neo4j en los flujos funcionales. Evidencia: 503 `NEO4J_NO_DISPONIBLE` y estado `PENDIENTE_NEO4J`.
- [x] **C09-07** **R** Manejar errores de ejecución de consultas. Evidencia: formato común, errores de solicitud y botones de reintento.
- [x] **C09-08** **R** Manejar errores de visualización de resultados. Evidencia: validaciones de respuesta en dashboard, buscador, carga y grafos.
- [x] **C09-09** **R** Mantener la aplicación utilizable sin cierres inesperados ante esos errores. Evidencia: estados recuperables de Angular y continuidad de navegación.
- [ ] **C09-10** **V** Comprobar los mensajes visibles de Angular ante fallos reales del backend y de cada motor, además de errores simulados en pruebas. Evidencia automatizada lista: `ApiExceptionHandlerTests`, `upload.spec.ts`, `graph.spec.ts`; falta la comprobación operativa indicada en `.docs/tecnica/MANEJO_ERRORES.md` para la demostración final.

## 10. Documentación y decisiones justificadas

Referencia: brief, secciones 2, 7, 10, 11 y 13. Los documentos pueden organizarse en archivos separados o en manuales; los nombres de archivo quedan a criterio del proyecto.

- [x] **C10-01** **V** Documentar los comandos iniciales de ejecución, contenedores y conexiones en el README principal y el del frontend.
- [x] **C10-02** **R** Completar el manual técnico con prerrequisitos, instalación, configuración y ejecución reproducible de todos los componentes. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-03** **R** Documentar la arquitectura final y la comunicación entre componentes. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-04** **R** Documentar y justificar el stack y las bibliotecas de gráficas y grafos elegidos. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-05** **R** Documentar el modelo MongoDB, colecciones, campos, tipos e índices, con ejemplos. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-06** **R** Documentar el modelo Neo4j, etiquetas, propiedades, relaciones, restricciones e índices, con ejemplos. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-07** **R** Documentar el ETL y las reglas de limpieza, normalización, conversión y conservación o exclusión de registros. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-08** **R** Documentar la estrategia de duplicados y repetición de cargas. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-09** **R** Documentar las 20 consultas MongoDB, los parámetros que aceptan y cómo interpretar sus resultados. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-10** **R** Documentar las cinco o más consultas Cypher y explicar qué información obtiene cada una. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-11** **R** Documentar los filtros de grafos, los criterios de búsqueda y las visualizaciones elegidas. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-12** **R** Elaborar el análisis de resultados con información obtenida de los datos procesados. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-13** **R** Analizar ventajas y limitaciones de integrar una base documental y una base de grafos en esta solución. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-14** **R** Redactar conclusiones sustentadas en la implementación y los resultados. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-15** **R** Completar el manual de usuario con carga de CSV, lectura de resúmenes y rechazos, estadísticas, búsqueda, grafos, filtros y mensajes de error. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C10-16** **V** Revisar los README para que describan el estado final y no conserven afirmaciones desactualizadas sobre funciones ya implementadas. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.

## 11. Git, repositorio y entregables

Referencia: brief, secciones 12 y 13.

- [x] **C11-01** **R** Usar Git para el desarrollo. El historial local revisado contiene cinco commits en total, incluido `3d094c4`.
- [x] **C11-02** **R** Confirmar al menos cinco commits significativos. El número total observado no garantiza por sí solo que todos sean significativos; un arreglo de nombre de configuración debe valorarse según su alcance. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [ ] **C11-03** **R** Confirmar el repositorio de entrega en GitHub y que contiene la versión final del proyecto.
- [x] **C11-04** **R** Incluir el código fuente del frontend. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-05** **R** Incluir el código fuente del backend. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-06** **R** Incluir los scripts o código ejecutable de carga y transformación de datos. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-07** **R** Incluir las consultas MongoDB. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-08** **R** Incluir las consultas Cypher de Neo4j. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-09** **R** Incluir el análisis de resultados. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-10** **R** Incluir las configuraciones necesarias para ejecutar el proyecto. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-11** **R** Incluir las conclusiones, el manual técnico y el manual de usuario. Evidencia: implementación integrada hasta `981ec31` y mejoras posteriores; documentación desarrollada localmente, versionada por primera vez en `14808c1` y revisada el 30 de octubre de 2026.
- [x] **C11-12** **V** Comprobar que la documentación necesaria esté versionada. Primer versionado de `.docs/` en `14808c1`, rama temporal `tmp/docs`, confirmado por el usuario. Verificado con `git ls-files` y `git show --stat 14808c1`. No cierra C11-03 ni acredita publicación remota.
- [ ] **C11-13** **V** Verificar el arranque desde una copia limpia siguiendo únicamente la documentación y sin depender de archivos locales no suministrados, salvo las credenciales configurables.
- [ ] **C11-14** **V** Confirmar que no se incluyen contraseñas reales, `.env`, dependencias descargadas ni artefactos de compilación.

## 12. Demostración, comprensión y entrega

Referencia: brief, secciones 12 y 13.

- [ ] **C12-01** **V** Demostrar el recorrido completo: seleccionar varios CSV → validar y transformar → almacenar en MongoDB y Neo4j → mostrar resúmenes y rechazos → ejecutar consultas → visualizar resultados.
- [ ] **C12-02** **V** Demostrar desde la aplicación las 20 consultas MongoDB, la búsqueda, las cinco consultas Cypher y un grafo con filtro.
- [ ] **C12-03** **V** Demostrar que los datos persisten al reiniciar los servicios y que la carga repetida cumple la estrategia de duplicados documentada.
- [ ] **C12-04** **R** Poder explicar el código, las conexiones, el ETL, los modelos y las decisiones técnicas de una solución propia desarrollada individualmente.
- [ ] **C12-05** **R** Comprender, revisar, validar y adaptar el contenido generado con ayuda de IA; no entregar trabajo ajeno o generado sin comprensión.
- [ ] **C12-06** **R** Prepararse para explicar, modificar o reproducir código, configuración y consultas durante la evaluación, incluyendo una consulta MongoDB o Neo4j nueva o modificada.
- [ ] **C12-07** **R** Entregar el proyecto dentro del plazo indicado: 30 de octubre de 2026; revisar las políticas del curso para entregas tardías.

Condición académica informativa del brief: se exige obtener como mínimo el 5 % de la calificación total del proyecto para tener derecho a las siguientes actividades o proyectos. Esto depende de la evaluación, no de marcar una tarea de implementación. El brief también establece penalizaciones por uso inadecuado de IA y copia; consultar su sección 12.

## Organización en ramas y commits

Las tareas se identifican como `C01-01` (sección 1, tarea 1). Su rama y commit de cierre propuestos están en [PLAN_RAMAS_COMMITS.md](PLAN_RAMAS_COMMITS.md). Mantén estos identificadores al actualizar el checklist; para tareas nuevas utiliza el siguiente número libre de la sección sin renumerar las existentes.

## Próximo bloque de trabajo

1. Completar C09-10: comprobar en navegador caída real y recuperación de backend, MongoDB y Neo4j.
2. Registrar la aceptación conjunta C01-10/C12-01–03: carga múltiple, consultas, visualización, persistencia y repetición; conservar los resultados parciales ya registrados.
3. Ampliar la comprobación de idempotencia entre archivos distintos con filas compartidas, fuentes y relaciones; protocolo en `tecnica/ETL_CARGA.md`.
4. Completar C11-13–14: arranque desde copia limpia y revisión final de archivos de entrega; las verificaciones parciales están en `evidencias/VERIFICACION_ENTREGA.md`.
5. Confirmar C11-03 y C12-04–07: versión final en GitHub, comprensión, práctica y entrega del estudiante.

La actualización documental del 30 de octubre corrige arquitectura, API, manuales, filtros y biblioteca, y registra el primer versionado. No repite pruebas de aplicación ni cierra verificaciones operativas por inferencia.

## Historial de avances

Los apartados siguientes conservan los estados, conteos y restricciones de cada etapa. Sus pendientes pueden haber sido resueltos por entradas posteriores; el estado vigente es el de las tareas anteriores.

## Actualización de la base — 20 de octubre de 2026

Se verificaron 6 pruebas del backend, 12 del frontend, compilación de producción y HTTP 200 del diagnóstico directo y por proxy con ambos motores UP. El panel permite reintentar y los fallos están cubiertos con respuestas simuladas. Evidencia: `.docs/evidencias/VERIFICACION_BASE.md`. C01-09 se completa; C01-08 y C01-10 siguen abiertas. Estado del checklist tras esta actualización: 13 completadas y 127 pendientes. Por instrucción explícita del usuario, este checklist y el plan de ramas se conservan como seguimiento local sin versionar.

## Cierre de la sección 2 — Datos de origen y comprensión

C02-01 a C02-08 completadas con revisión completa de los tres CSV confirmados por el usuario, inventario y diccionario de campos, alias de encabezados, conteos de correlativos/evaluaciones y reglas de métricas y edad. Evidencia: `.docs/datos/DATOS_ORIGEN.md`, `.docs/datos/PERFIL_CSV.json` y `.docs/datos/PROCEDENCIA_CSV.json`. Total: 58 377 filas, 16 032 correlativos distintos y 15 duplicados exactos excedentes. Seis pruebas de la herramienta de perfilado correctas. Solo se versionan la herramienta, sus pruebas, la exclusión de cachés Python y las instrucciones README; `.docs/` y `.data/` permanecen locales. Estado tras el cierre: 21 completadas y 119 pendientes. Las decisiones C02-07 y C02-08 se adelantan a B01.01 sin cerrar los modelos ni implementar ETL o consultas.

## Avance de la sección 3 — Modelos

C03-01 a C03-07 y C03-10 completadas. Se aplicaron y verificaron los índices MongoDB y las restricciones/índices Neo4j; la muestra real temporal 2024 se limpió de ambos motores. Evidencia: `.docs/tecnica/MODELOS_DATOS.md`, `backend/src/main/resources/db/`, `scripts/validar_esquema_modelos.py` y `scripts/validar_modelos_reales.py`; commits `7cabe78` y `1e343da`. C03-08 y C03-09 siguen abiertas hasta implementar el cargador con `upsert`/`MERGE` y comprobar cargas repetidas. Estado: 29 completadas y 111 pendientes.


## Avance parcial — 2026-10-29

En la rama `feat/mejoras-panel-analitico` se implementaron IDs explícitos Mxx en selector, encabezado de tabla y gráfica, y presentación de municipios en M08/M17, sus gráficas, G08 y detalle de aspirante. Se preservan los datos y claves originales. Verificación: 28 pruebas Angular correctas, compilación de producción correcta y `git diff --check` sin errores. Pendiente revisión visual en navegador.

Siguen pendientes el historial persistente, paginación de rechazos, muestra de válidos, M21–M25 y las mejoras de interacción y distribución del grafo. Esta entrega parcial no cierra el alcance del archivo temporal.


### Avance 4b4f90d — Tablas de trazabilidad

Se reemplazó el detalle JSON por tablas de cargas, rechazos y evaluaciones válidas. Se mantiene visible el archivo y estado seleccionado, se muestran valores ausentes y fuentes por línea. Los motivos de rechazo estructural se exponen; errores internos de motores se sustituyen por mensajes controlados. Se añadieron pruebas Angular de paginación, reintento y respuestas tardías. Suite Angular y compilación correctas; backend compila con Java 25. Falta prueba de integración de paginación con MongoDB y revisión visual. M21–M25 y mejoras del grafo permanecen pendientes.


Avance adicional: `a5a5313` implementa M21–M25 con API y visualización; `e71aab8` integra vis-network y navegación del grafo. La biblioteca está documentada en `.docs/tecnica/BIBLIOTECA_GRAFOS.md`, excluida de Git. Angular: 30 pruebas y build correctos antes del último ajuste de respuestas tardías. Backend compila; los resultados conocidos de M21–M25 y la inspección visual permanecen pendientes. La ejecución inicial de pruebas backend falló por adjunción de Mockito en el aislamiento; se reintentó fuera de él.


## Cierre de mejoras del panel — 2026-10-29

Completados los comportamientos pendientes de interacción de grafos y pruebas de M21–M25 y paginación ETL. Evidencia: prueba `AdditionalAnalysisIntegrationTest` contra base MongoDB temporal aislada, eliminada al finalizar; 121 rechazos paginados sin pérdida ni duplicación, fuentes filtradas y resultados conocidos para cinco agregaciones. Angular: selección y visualización de las cinco consultas; Chromium: grafo 5/10/40 evaluaciones, detalle y cinco consultas sin errores JavaScript. Capturas locales `.docs/evidencias/grafos/`. Biblioteca e interacción documentadas en `.docs/tecnica/BIBLIOTECA_GRAFOS.md`. El archivo temporal conserva el historial y contiene una sección final de cierre. Ninguna evidencia local se incorpora a Git.


### Cierre técnico del archivo temporal

Completados DTO explícitos para historial y cinco análisis, modelos y validación de respuestas Angular y pruebas de casos límite de recuperación/cohortes/promedios ausentes. Verificación final: 35 pruebas Angular correctas y build de producción correcto; backend: 20 pruebas ejecutadas sin fallos, incluidas dos de integración de análisis/trazabilidad, y ocho pruebas opcionales omitidas por sus propiedades de activación. `git diff --check` correcto. El temporal se actualizó sin estados históricos contradictorios. No quedan tareas de implementación de su alcance.


Corrección de colores del explorador: ocho tipos con colores exclusivos y catálogo único compartido por nodos y filtros. Las pruebas comprueban que ocultar Materia solo oculte sus nodos y enlaces incidentes y conserve colores de los otros siete tipos. Verificación: 37 pruebas Angular y compilación de producción correctas; `git diff --check` correcto. Documentación: BIBLIOTECA_GRAFOS.md.


Mejora de navegación Cypher: selector Gxx por sección y una tabla visible; consulta seleccionada en URL y carga individual con caché. Verificación: 37 pruebas Angular y compilación correctas. Detalle local en EXPLORADOR_GRAFOS.md.


## Filtros parametrizados del backend

Avance anterior de filtros locales guardado en `5a0cd95`, antes de cambiar el enfoque por instrucción del usuario. Los selectores ahora envían parámetros HTTP opcionales y las consultas existentes los aplican sin crear variantes por combinación. MongoDB: `$match` antes de agrupar, con recálculo de totales/porcentajes/rankings; M06 filtrada no sobrescribe resumen_carrera. Neo4j: filtros parametrizados sobre grupos de cada Gxx, conservando sus conteos. Se mantienen opciones base para limpiar y combinar filtros. Documentación y ejemplos en `.docs/tecnica/FILTROS_ANALITICOS.md`.

Verificación: 38 pruebas Angular y compilación de producción correctas. Backend: 21 pruebas ejecutadas sin errores ni fallos y ocho opcionales omitidas; incluye pruebas de filtros MongoDB en base temporal aislada y filtros de categorías sobre G01–G10 cuando hay resultados, más valores literales semejantes a inyección.


Corrección ETL: sexo SIN REGISTRO se conserva como desconocido. Cargas antiguas se reprocesan al volver a subir el CSV mediante versionReglas=2. Verificadas normalización, rechazos reales, persistencia en ambos motores, repetición y reprocesamiento sin duplicados. Detalle en ETL_CARGA.md.


Mejora de visualización G01–G10: barras por consulta, sincronizadas con la respuesta filtrada de la tabla, unidades separadas en G07/G10 y ocho grupos mayores por gráfica. Verificación: 42 pruebas Angular y build de producción correctos; detalles en EXPLORADOR_GRAFOS.md.
