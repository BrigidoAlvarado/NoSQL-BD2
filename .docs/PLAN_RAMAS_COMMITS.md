# Plan de ramas y commits

Fuente: [BRIEF_PROYECTO.md](BRIEF_PROYECTO.md). Tareas: [CHECKLIST_MAESTRO.md](CHECKLIST_MAESTRO.md). Propuesta preparada el 20 de octubre de 2026 para la entrega indicada el 30 de octubre de 2026.

## Ajuste autorizado — 20 de octubre de 2026

El usuario solicita completar la base del proyecto y conservar el checklist maestro y este plan únicamente como seguimiento local, fuera de los commits. No ejecutar B00.01 para versionarlos. La base se completa en la rama `feat/base-proyecto`, creada desde `main`, con un cambio coherente identificado como B00.02: `feat(base): integra diagnostico de conexiones y documenta arquitectura`. Incluye código y pruebas; la documentación técnica se conserva localmente en `.docs/` por instrucción del usuario y no se versiona. No incluye archivos de `.docs` ni credenciales. C01-09 se adelanta de B01.04 a B00.02; C01-08 y C01-10 permanecen abiertas por depender de las funcionalidades con datos reales. Estado tras el ajuste: 13 tareas completadas y 127 pendientes. No se exige integrar ni publicar automáticamente la rama.

## Alcance y reglas de trabajo

Validación adicional de B02.05/B09.01: se documentó en `tecnica/ETL_CARGA.md` el protocolo de idempotencia entre archivos distintos, conservación de evaluaciones legítimas, comparación de IDs y búsqueda de relaciones duplicadas. Su ampliación automatizada permanece pendiente; la repetición del mismo archivo completo ya está cubierta. Distribución MongoDB: 18 consultas en Estadísticas y M11/M15 en Búsqueda.

Refactorización autorizada el 29 de octubre de 2026: rama `refactor/consultas-por-responsabilidad`, creada desde main. R01.01 separa consultas M01–M20 y catálogo/recursos G01–G10, con pruebas de resultados conocidos. R01.02 separa recepción HTTP, servicio y DTO de carga. R01.03 divide páginas Angular en modelos, catálogos, tablas y componentes visuales; mueve el diagnóstico a layout y retira el placeholder sin uso. Es una mejora de diseño para explicar y modificar consultas durante la evaluación, no un requisito adicional del brief. Guía local: `tecnica/GUIA_CONSULTAS.md`. No añadir `.docs` a Git.

Evidencia R01: commits `8a2cdb5` (consultas y regresiones), `3aa0dff` (carga), `9b16ad9` (frontend). Verificación final: backend 25 pruebas con integraciones sin fallos ni omisiones y empaquetado correcto; frontend 24 pruebas y build de producción correctos. Los recursos Cypher están incluidos en el JAR. Pendiente inspección visual en navegador. Las responsabilidades conservan sus rutas API y la semántica anterior; no se modificaron políticas funcionales como la actualización de M06 al abrir el dashboard.

Organización local solicitada: `.docs/` queda completamente fuera del índice de Git. Se conservan brief, enunciado, checklist y plan en la raíz; manuales en `manuales/`, datos en `datos/`, notas técnicas en `tecnica/` y comprobaciones en `evidencias/`. El índice y las dos notas de evolución UI se retiraron por redundancia con los manuales actuales. Las menciones anteriores a documentación versionada describen el estado histórico; no autorizan volver a añadir `.docs` al índice. La publicación de documentación de entrega queda pendiente de decisión del usuario (C11-12).

Actualización 29 de octubre de 2026: por solicitud de completar pendientes y guardar commits, se integró B07 en main (`f38750b`, `0b5562a`, `c506f01`, `bde8396`) y se creó/integró `docs/manuales-y-analisis` con `981ec31`. La documentación vive en `.docs/` y permanece local, excluida del seguimiento de Git por instrucción del usuario. Se abrió `test/validacion-entrega`. Backend: 21 pruebas incluidas integraciones correctas; frontend: 24 pruebas y build correctos; Python: ocho pruebas. G06–G10 ejecutadas contra Neo4j. Pendientes operativos detallados en `.docs/evidencias/VERIFICACION_ENTREGA.md`; no se da por realizada la entrega ni la demostración personal.

Ajuste UI/UX autorizado el 24 de octubre de 2026: mejora de escritorio del frontend sobre las funcionalidades existentes, sin trabajo responsive adicional. Las decisiones y evidencia se registran localmente en `manuales/MANUAL_USUARIO.md`; no corresponde al cierre de B08 ni cambia las dependencias funcionales.

Refinamiento solicitado el mismo día: investigación documental y segmentación por temas de estadísticas y grafos. Cinco subsecciones para las 18 consultas del dashboard y cuatro para el grafo y las cinco consultas Cypher, con enlaces directos y estados independientes. Distribución y fuentes en `manuales/MANUAL_USUARIO.md`. No se cierran tareas de documentación final ni pruebas de caídas reales con este cambio.

Ajuste posterior: se integran las subsecciones en el sidebar principal bajo Estadísticas y Explorador de grafos. Se elimina el sidebar interno de las vistas; se mantienen los enlaces directos y los temas. Verificación: 24 pruebas del frontend correctas y compilación de producción exitosa.

Extensión de grafos autorizada el 28 de octubre de 2026: además de G01–G05, se implementan G06–G10 para resultados por tipo de institución, reintentos, demanda municipal, resultados por sexo y trayectorias con resultados mixtos. El mínimo del enunciado continúa siendo cinco; esta ampliación es una mejora funcional documentada en `tecnica/EXPLORADOR_GRAFOS.md`.

Este plan organiza las 140 tareas del checklist: 12 ya completadas y 128 pendientes. Los nombres de ramas y mensajes de commit son propuestas de organización, no requisitos del enunciado. Este documento no crea ramas, commits ni funcionalidades por sí mismo.

La base actual es `main`, con cinco commits existentes; el último revisado es `3d094c4`. No se reescribe ese historial ni se vuelve a implementar lo completado. Las tareas pendientes requieren evidencia nueva.

- Crear una rama por bloque desde `main` actualizado con el bloque anterior. Para este proyecto individual se propone trabajar e integrar una rama a la vez.
- Hacer cada commit al terminar su cambio coherente y las comprobaciones correspondientes. Los títulos no justifican crear commits vacíos ni dividir artificialmente un mismo cambio.
- Incluir en cada bloque su documentación y manejo de errores. B07 revisa y unifica los errores restantes; no es una razón para posponerlos en los bloques previos.
- Una tarea puede recibir cambios en varios commits; la matriz indica el commit propuesto para cerrarla. Solo marcarla cuando cumpla todo su criterio, aunque ese commit ya exista.
- Una consulta se completa con implementación, API, visualización, documentación y resultado comprobado; no se cierra únicamente por agregar el pipeline o Cypher.
- Mantener los prefijos `Cxx-yy` del checklist como referencias estables. La documentación y las verificaciones son parte de cada entrega.

## B00 — Guardar la planificación

Rama propuesta: `docs/plan-proyecto`.

| Commit | Mensaje propuesto | Alcance |
| --- | --- | --- |
| B00.01 | `docs(plan): agrega checklist maestro y plan de ramas` | Guardar el checklist y este plan como punto de seguimiento; comprobar que la fuente de requisitos necesaria también esté disponible en el repositorio de entrega. |

La carpeta `.docs` está excluida localmente por `.git/info/exclude`. Para guardar estos dos documentos cuando se ejecute B00, se pueden agregar por sus rutas explícitas, sin cambiar esa exclusión ni incluir `.env`:

```bash
git switch -c docs/plan-proyecto
git add -f -- .docs/CHECKLIST_MAESTRO.md .docs/PLAN_RAMAS_COMMITS.md
git diff --cached --check
git diff --cached --stat
git commit -m "docs(plan): agrega checklist maestro y plan de ramas"
```

Estos comandos son una guía; no se ejecutaron al elaborar el plan. Revisa el contenido preparado antes del commit. B00 no cierra tareas funcionales del checklist.

## Orden recomendado

| Orden | Rama | Depende de | Resultado al integrar |
| --- | --- | --- | --- |
| B01 | `feat/modelos-datos` | B00 | Datos reales, modelos y decisiones documentadas |
| B02 | `feat/carga-csv` | B01 | Carga de varios CSV desde Angular hasta ambos motores |
| B03 | `feat/estadisticas-basicas` | B02 | Dashboard inicial con seis consultas completas |
| B04 | `feat/estadisticas-avanzadas` | B03 | Doce consultas adicionales y colección auxiliar |
| B05 | `feat/buscador-aspirantes` | B02; orden sugerido después de B04 | Búsqueda y consultas de historial e información completa |
| B06 | `feat/explorador-grafos` | B02; orden sugerido después de B05 | Cinco consultas Cypher y visualización filtrable |
| B07 | `fix/manejo-errores` | B02 a B06 | Tratamiento consistente de fallos en todos los flujos |
| B08 | `docs/manuales-y-analisis` | B01 a B07 | Manuales, análisis de resultados y conclusiones |
| B09 | `test/validacion-entrega` | B01 a B08 | Versión final reproducible y lista para demostrar |

Las dependencias indican qué necesita cada bloque. B05 y B06 dependen funcionalmente de B02; aun así, la secuencia de la tabla facilita integrar todo en `main` sin mantener varias ramas abiertas.

## Commits de cada rama

Cada fila describe un commit futuro. Las pruebas indicadas se eligen según el riesgo y el comportamiento que debe verificarse; no se exige una biblioteca de pruebas adicional.

### B01 — `feat/modelos-datos`

Objetivo: Datos reales, modelos y decisiones documentadas.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B01.01 | `chore(datos): agrega perfil reproducible de CSV` | Revisar C02-01 a C02-08: archivos, procedencia, doce campos y alias, calidad, cardinalidad y criterios de métricas y edad. Solo se versiona herramienta, pruebas e instrucciones; evidencia y decisiones en `.docs/`, excluida por instrucción del usuario. |
| B01.02 | `feat(mongodb): define el modelo documental y sus indices` | Definir documentos, colecciones, índices y estructura de resumen_carrera, con script idempotente y ejemplos documentados localmente. |
| B01.03 | `feat(neo4j): define nodos relaciones y restricciones` | Definir el modelo del grafo y sus restricciones e índices sin inventar identificadores ausentes en los CSV; validar ambos modelos con una fila real temporal. |
| B01.04 | `docs(arquitectura): justifica modelos metricas y reglas de calidad` | Cerrar decisiones sobre arquitectura, edades, conteos, duplicados y casos especiales; justificar que ambos modelos permiten resolver las consultas. |

### B02 — `feat/carga-csv`

Objetivo: Carga de varios CSV desde Angular hasta ambos motores.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B02.01 ✓ | `feat(etl): valida y transforma registros CSV` | Implementar lectura, tipos, normalización, valores especiales y trazabilidad de válidos y rechazados; probar las reglas. |
| B02.02 ✓ | `feat(etl): almacena evaluaciones procesadas en MongoDB` | Guardar los registros válidos según el modelo y su estrategia de identificación. |
| B02.03 ✓ | `feat(etl): sincroniza el grafo y controla cargas parciales` | Cargar Neo4j sin nodos duplicados, validar los modelos con una muestra y gestionar fallos o reintentos entre motores. |
| B02.04 ✓ | `feat(carga): conecta Angular con la carga y sus resultados` | Implementar API y envío de múltiples archivos; mostrar nombre, conteos leídos/procesados/rechazados, duración, estado y causas de rechazo por archivo. |
| B02.05 ✓ | `test(etl): verifica cargas multiples rechazos y repeticion` | Comprobar casos válidos e inválidos, repetición sin duplicados indebidos y resultados por archivo; documentar ETL y recuperación. |

### B03 — `feat/estadisticas-basicas`

Objetivo: Dashboard inicial con seis consultas completas.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B03.01 ✓ | `feat(estadisticas): muestra aspirantes por institucion y aprobados por materia` | Entregar M01 y M02 con consulta, API, representación en el dashboard y documentación. |
| B03.02 ✓ | `feat(estadisticas): compara aprobacion por materia e institucion` | Entregar M04 y M18 con denominadores documentados, estados vacíos y representación apropiada. |
| B03.03 ✓ | `feat(estadisticas): muestra distribucion por sexo y resultados anuales` | Entregar M12 y M20, respetando aspirantes únicos frente a evaluaciones. |
| B03.04 ✓ | `docs(estadisticas): justifica visualizaciones y verifica indicadores basicos` | Documentar las representaciones elegidas y contrastar las seis consultas con una muestra conocida. |

### B04 — `feat/estadisticas-avanzadas`

Objetivo: Doce consultas adicionales y colección auxiliar.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B04.01 ✓ | `feat(estadisticas): analiza carreras por anio municipio y departamento` | Entregar M03, M08 y M19 con API, visualización, documentación y comprobación de resultados. |
| B04.02 ✓ | `feat(estadisticas): analiza edades y porcentajes de aprobacion` | Entregar M05, M07 y M13 considerando años desconocidos y la definición de edad acordada. |
| B04.03 ✓ | `feat(estadisticas): analiza meses demanda juvenil e intentos` | Entregar M09, M10 y M14, respetando sus filtros específicos y unidad de conteo. |
| B04.04 ✓ | `feat(estadisticas): genera resumen de carreras y rankings` | Entregar M06, M16 y M17; crear y actualizar resumen_carrera y visualizar sus resultados. |

### B05 — `feat/buscador-aspirantes`

Objetivo: Búsqueda y consultas de historial e información completa.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B05.01 ✓ | `feat(busqueda): implementa filtros e historial de aspirantes` | Criterios y representación de institución documentados; API de búsqueda, M11 y M15 implementados. |
| B05.02 ✓ | `feat(busqueda): conecta filtros y resultados en Angular` | Coincidencias, resultados vacíos, información del aspirante y sus distintas evaluaciones visibles. |
| B05.03 ✓ | `test(busqueda): verifica historiales filtros y datos completos` | Pruebas de historial, entradas inválidas y ausencia de coincidencias; M11 y M15 cerradas. |

### B06 — `feat/explorador-grafos`

Objetivo: Cinco consultas Cypher y visualización filtrable.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B06.01 ✓ | `feat(grafos): implementa cinco consultas Cypher y su API` | Definir y ejecutar G01 a G05. Deben ser distintas, útiles y demostrar nodos, relaciones, propiedades, filtros y recorridos. |
| B06.02 ✓ | `feat(grafos): visualiza relaciones con filtros en Angular` | Elegir y justificar la biblioteca, representar un grafo real e implementar al menos un filtro que cambie los resultados. |
| B06.03 ✓ | `feat(grafos): permite ejecutar las cinco consultas desde la interfaz` | Hacer accesibles las cinco consultas, sus parámetros y resultados. Aquí se pueden cerrar G01 a G05 si también están documentadas y verificadas. |
| B06.04 ✓ | `test(grafos): verifica filtros recorridos y resultados vacios` | Comprobar filtros y casos sin resultados; completar documentación de consultas y criterios de exploración. |

### B07 — `fix/manejo-errores`

Objetivo: Tratamiento consistente de fallos en todos los flujos.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B07.01 ✓ | `fix(api): unifica errores de carga consultas y conexiones` | Respuestas controladas y consistentes para endpoints funcionales. |
| B07.02 ✓ | `fix(frontend): muestra errores recuperables en cada seccion` | Fallos de archivos, procesamiento, motores, consultas y visualizaciones recuperables desde Angular. |
| B07.03 | `test(errores): verifica continuidad ante fallos de los motores` | Pruebas de mensajes y recuperación simulada listas; falta la prueba operativa de motores para cerrar C09-10. |

### B08 — `docs/manuales-y-analisis`

Objetivo: Manuales, análisis de resultados y conclusiones.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B08.01 | `docs(tecnico): completa arquitectura configuracion y consultas` | Consolidar manual técnico, stack, modelos, ETL, índices, restricciones y documentación de las 20 consultas MongoDB y cinco Cypher. |
| B08.02 | `docs(usuario): documenta carga estadisticas busqueda y grafos` | Completar instrucciones de uso, errores, filtros y visualizaciones; actualizar los README al comportamiento final. |
| B08.03 | `docs(resultados): analiza hallazgos y redacta conclusiones` | Analizar datos reales y comparar ventajas y limitaciones de los dos motores; redactar conclusiones sustentadas. |

### B09 — `test/validacion-entrega`

Objetivo: Versión final reproducible y lista para demostrar.

| Commit | Mensaje propuesto | Contenido y criterio de cierre |
| --- | --- | --- |
| B09.01 | `test(integracion): verifica el flujo completo y las consultas` | Verificar carga, persistencia tras reinicio, duplicados, resúmenes, rechazos, búsqueda, 20 consultas MongoDB y cinco Cypher desde la aplicación. |
| B09.02 | `test(instalacion): valida ejecucion desde una copia limpia` | Validar instalación documentada, configuración disponible y ausencia de secretos o artefactos descargados en la entrega. |
| B09.03 | `docs(entrega): registra evidencias y cierra el checklist` | Completar las comprobaciones de entregables y GitHub, revisar commits significativos, registrar evidencia y preparar demostración y explicación del proyecto. |

## Correspondencia con todas las tareas del checklist

Cada identificador aparece una sola vez en esta matriz. `BASE` conserva las 12 tareas inicialmente completadas; B00.02 completa C01-09 y los otros grupos asignan las 127 pendientes a su commit de cierre. Los intervalos son inclusivos: `C04-01 a C04-11` comprende las once tareas.

| Commit de cierre | Tareas del checklist |
| --- | --- |
| BASE | `C01-01` a `C01-07`; `C05-01`; `C09-01` a `C09-02`; `C10-01`; `C11-01` |
| B01.01 | `C02-01` a `C02-08` |
| B01.02 | `C03-01` a `C03-04`; `C10-05` |
| B01.03 | `C03-05` a `C03-07`; `C10-06` |
| B00.02 | `C01-09` |
| B01.04 | `C03-09`; `C10-03` |
| B02.01 | `C04-01` a `C04-11` |
| B02.02 | `C04-12` |
| B02.03 | `C03-08`; `C03-10`; `C04-13` a `C04-14` |
| B02.04 | `C05-02` a `C05-10` |
| B02.05 | `C04-15` a `C04-16`; `C05-11`; `C10-07` a `C10-08` |
| B03.01 ✓ | `C06-01` a `C06-02`; `C06-21` |
| B03.02 ✓ | `C06-04`; `C06-18` |
| B03.03 ✓ | `C06-12`; `C06-20` |
| B03.04 ✓ | `C06-22` |
| B04.01 ✓ | `C06-03`; `C06-08`; `C06-19` |
| B04.02 ✓ | `C06-05`; `C06-07`; `C06-13` |
| B04.03 ✓ | `C06-09` a `C06-10`; `C06-14` |
| B04.04 ✓ | `C06-06`; `C06-16` a `C06-17` |
| B05.01 | `C08-02` a `C08-03` |
| B05.02 | `C08-01` |
| B05.03 | `C06-11`; `C06-15`; `C08-04` a `C08-05` |
| B06.01 ✓ | `C07-06` |
| B06.02 ✓ | `C07-07` a `C07-08` |
| B06.03 ✓ | `C07-01` a `C07-05` |
| B06.04 ✓ | `C07-09` a `C07-10`; `C10-10` |
| B07.01 | Apoya tareas que se cierran en otro commit; ver alcance de la rama. |
| B07.02 | `C09-03` a `C09-08` |
| B07.03 | `C09-09` a `C09-10` |
| B08.01 | `C10-02`; `C10-04`; `C10-09` |
| B08.02 | `C10-11`; `C10-15` a `C10-16` |
| B08.03 | `C10-12` a `C10-14` |
| B09.01 | `C01-08`; `C01-10`; `C06-23`; `C12-01` a `C12-03` |
| B09.02 | `C11-13` a `C11-14` |
| B09.03 | `C11-02` a `C11-12`; `C12-04` a `C12-07` |

B07.01 aporta las respuestas del backend que permiten cerrar las tareas de errores en B07.02 y B07.03. Las consultas G01 a G05 reciben implementación en B06.01 y acceso completo desde la interfaz en B06.03. M11 y M15 reciben backend en B05.01 e interfaz en B05.02, y se cierran después de su validación en B05.03.

## Cobertura de las consultas MongoDB

Esta tabla evita perder una consulta al repartir estadísticas e historial entre ramas. Los commits incluyen API y visualización, salvo los casos de implementación escalonada indicados arriba.

| Commit de cierre | Consultas |
| --- | --- |
| B03.01 ✓ | `M01`, `M02` |
| B03.02 ✓ | `M04`, `M18` |
| B03.03 ✓ | `M12`, `M20` |
| B04.01 ✓ | `M03`, `M08`, `M19` |
| B04.02 ✓ | `M05`, `M07`, `M13` |
| B04.03 ✓ | `M09`, `M10`, `M14` |
| B04.04 ✓ | `M06`, `M16`, `M17` |
| B05.03 | `M11`, `M15` |

## Integración y cierre de una rama

1. Terminar los commits del bloque y revisar los cambios preparados, especialmente los archivos de configuración.
2. Ejecutar las comprobaciones apropiadas: `./mvnw verify` desde `backend/`; `npm test` y `npm run build` desde `frontend/` cuando corresponda. Las pruebas funcionales reales requieren ambos motores activos y credenciales correctas.
3. Actualizar las tareas afectadas del checklist y registrar evidencia. Agregar explícitamente los documentos de `.docs` que deban formar parte de los commits.
4. Integrar en `main` con avance rápido cuando sea posible. Conservar los commits significativos del trabajo; evitar comprimir todo el proyecto en un único commit.
5. Crear la siguiente rama desde ese `main`. Repetir hasta B09.

La publicación en GitHub se verifica en B09; proponer este plan no realiza un push ni modifica el remoto. Los cinco commits significativos exigidos por el brief son un mínimo académico; esta propuesta contiene 34 commits futuros, incluido B00, que pueden ajustarse al alcance real del trabajo sin perder cobertura.

Las tareas de comprensión, demostración y entrega necesitan revisión del estudiante y evidencia de cumplimiento. Un commit con documentación no demuestra automáticamente que se pueden explicar o modificar las consultas durante la evaluación.

## Primer bloque a implementar

Después de guardar la planificación, comenzar por `feat/modelos-datos`, commit B01.01: identificar los CSV requeridos, revisar sus campos y anomalías y documentar los hallazgos. El modelo definitivo y el ETL dependen de esa revisión.

## Evidencia B00.02

Cambio guardado como `ae17ed8` en `feat/base-proyecto`. Solo código, pruebas y README. Toda la documentación técnica local está en `.docs/`, excluida de Git por instrucción del usuario. Se encontraron CSV locales en `.data/` para 2015, 2022 y 2024; B01.01 debe confirmar procedencia y selección y revisar las diferencias de encabezados antes del ETL.

## Ajuste B01.01 — Sección 2 completa

Se integró `ae17ed8` en `main` mediante avance rápido y se creó `feat/modelos-datos`. C02-01 a C02-08 se cierran en B01.01; se adelantan las decisiones de métricas y edad antes asignadas a B01.04. La documentación de fuente, calidad y decisiones permanece local en `.docs/datos/DATOS_ORIGEN.md` y los informes JSON. La comparación SHA-256 local/remota no pudo completarse por HTTP 500 del portal; se conservan URLs y huellas locales, sin afirmar igualdad no comprobada. El usuario confirmó el conjunto inicial 2015/2022/2024 y permitió ampliar si es necesario; la cobertura no exige descargar todos los años. Estado actual: 21 completadas y 119 pendientes. B01.02 y B01.03 no se implementaron como parte de esta sección.

Evidencia de cierre B01.01: commit `d7c29b8` en `feat/modelos-datos`; seis pruebas correctas, conteos contrastados mediante lectura independiente, perfil determinista y SHA-256 de los CSV sin cambios. La rama permanece abierta para los modelos B01.02 y B01.03.

Evidencia B01.02 y B01.03: `7cabe78` y `1e343da` en `feat/modelos-datos`. Los scripts de MongoDB y Neo4j se aplicaron de forma repetible a las instancias locales y se verificaron sus índices/restricciones; ocho pruebas de scripts y seis de backend correctas. La validación de muestra real se completó sin dejar datos temporales. B01.04 queda parcialmente cubierto por las decisiones de C02-07/C02-08 y C03-09 permanece pendiente del ETL.


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


## Estilo compartido de selectores

Los selectores simples usan estilo global en `frontend/src/styles.css`: altura mínima de 48 px, flecha decorativa, borde y fondo consistentes, foco visible, hover y estado deshabilitado. Se conserva el control nativo y su navegación de teclado. Incluye adaptación para colores forzados y movimiento reducido. Se retiró el estilo duplicado del selector en dashboard.css.

La tarjeta de selección se organiza en fila en escritorio y columna en móvil. El sidebar deja de ser sticky en pantallas pequeñas para evitar superponerse a los controles. Verificado en Chromium a 1440 y 390 px: sin desbordamiento horizontal y controles de al menos 48 px. Compilación de producción correcta.


## Filtros parametrizados del backend

Avance anterior de filtros locales guardado en `5a0cd95`, antes de cambiar el enfoque por instrucción del usuario. Los selectores ahora envían parámetros HTTP opcionales y las consultas existentes los aplican sin crear variantes por combinación. MongoDB: `$match` antes de agrupar, con recálculo de totales/porcentajes/rankings; M06 filtrada no sobrescribe resumen_carrera. Neo4j: filtros parametrizados sobre grupos de cada Gxx, conservando sus conteos. Se mantienen opciones base para limpiar y combinar filtros. Documentación y ejemplos en `.docs/tecnica/FILTROS_ANALITICOS.md`.

Verificación: 38 pruebas Angular y compilación de producción correctas. Backend: 21 pruebas ejecutadas sin errores ni fallos y ocho opcionales omitidas; incluye pruebas de filtros MongoDB en base temporal aislada y filtros de categorías sobre G01–G10 cuando hay resultados, más valores literales semejantes a inyección.


Corrección ETL: sexo SIN REGISTRO se conserva como desconocido. Cargas antiguas se reprocesan al volver a subir el CSV mediante versionReglas=2. Verificadas normalización, rechazos reales, persistencia en ambos motores, repetición y reprocesamiento sin duplicados. Detalle en ETL_CARGA.md.


Mejora de visualización G01–G10: barras por consulta, sincronizadas con la respuesta filtrada de la tabla, unidades separadas en G07/G10 y ocho grupos mayores por gráfica. Verificación: 42 pruebas Angular y build de producción correctos; detalles en EXPLORADOR_GRAFOS.md.
