# Verificación de entrega — 29 de octubre de 2026

## Evidencia ejecutada

Corrección M05: `AgeQuerySupport` extrae `_id.carrera` como etiqueta en lugar de convertir el documento completo a texto. Regresión `AgeQuerySupportTest` e integración `IndividualQueriesIntegrationTest`: cuatro pruebas correctas con MongoDB local. La prueba de integración también comprueba el nombre exacto de la carrera.

Refactor R01 (29 de octubre de 2026): backend `verify` con las cuatro banderas de integración activadas: 25 pruebas, cero fallos/errores/omitidas y JAR generado con los once recursos Cypher. Incluye muestra MongoDB de cuatro evaluaciones para contrastar M01–M20 y muestra Neo4j aislada por marca para G01–G10. Angular: 24 pruebas y compilación de producción correctas tras separar modelos, catálogos y componentes; la prueba del dashboard selecciona las 18 consultas. Se conservaron las rutas y los contratos. No se repitió una inspección visual de escritorio con navegador.

- Java 25: `./mvnw -o verify` correcto; 17 pruebas ejecutadas y cuatro integraciones inicialmente desactivadas.
- Bases locales iniciadas con `docker compose up -d --wait`: ambas saludables.
- `./mvnw -o -Detl.integration=true -Dsearch.integration=true -Dstatistics.integration=true -Dgraph.integration=true test`: 21 pruebas, cero fallos, cero errores y cero omitidas. Incluye carga/repetición de muestra, búsqueda, consistencia estadística y ejecución de G01–G10.
- Angular: 24 pruebas correctas y compilación de producción correcta.
- Python: ocho pruebas correctas.
- Respuesta real de estadísticas documentada en ../manuales/ANALISIS_Y_CONCLUSIONES.md.
- Reinicio real de MongoDB y Neo4j conservando volúmenes: ambos saludables y mismos 44 505 registros, 12 528 aspirantes y 6 407 aprobaciones después del reinicio.
- Copia limpia mediante `git archive HEAD` en un directorio temporal: backend compilado/empaquetado con Java 25; frontend instalado con npm 10 y compilado. Se usaron cachés Maven existentes y descargas npm; no se certifica una máquina nueva sin cachés ni arranque completo desde esa copia.
- Revisión de rutas versionadas: sin `.env`, claves `.pem`/`.key`, `node_modules`, `target` ni `dist`. Esto no constituye una auditoría de secretos del historial.
- La instalación npm informó 12 alertas de dependencias (seis moderadas y seis altas); queda pendiente evaluarlas antes de un despliegue público.

## Pendientes de aceptación

- Validación ampliada de almacenamiento sin duplicados: protocolo y consultas de solo lectura en [ETL_CARGA.md](../tecnica/ETL_CARGA.md). La prueba existente repite el mismo archivo completo; falta contrastar archivos distintos con filas compartidas y verificar fuentes y relaciones repetidas. La documentación de este protocolo no implica que esas comprobaciones se hayan ejecutado.

- C09-10: comprobar en navegador los mensajes durante una caída real de cada servicio y su recuperación.
- C01-10, C12-01–03: completar recorrido visual, carga múltiple y comparación antes/después de reiniciar los servicios.
- C11-13: completar el arranque de todos los componentes desde la copia limpia; compilación e instalación de dependencias ya verificadas.
- C11-03: confirmar publicación de la versión final en GitHub. Los commits locales no equivalen a publicación.
- C12-04–07: comprensión, práctica de modificaciones y entrega del estudiante.

Las pruebas de integración existentes comprueban ejecución y algunos invariantes; no constituyen una comparación exhaustiva de todas las consultas con resultados calculados independientemente.


## Cierre de mejoras del panel — 2026-10-29

Completados los comportamientos pendientes de interacción de grafos y pruebas de M21–M25 y paginación ETL. Evidencia: prueba `AdditionalAnalysisIntegrationTest` contra base MongoDB temporal aislada, eliminada al finalizar; 121 rechazos paginados sin pérdida ni duplicación, fuentes filtradas y resultados conocidos para cinco agregaciones. Angular: selección y visualización de las cinco consultas; Chromium: grafo 5/10/40 evaluaciones, detalle y cinco consultas sin errores JavaScript. Capturas locales `.docs/evidencias/grafos/`. Biblioteca e interacción documentadas en `.docs/tecnica/BIBLIOTECA_GRAFOS.md`. El archivo temporal conserva el historial y contiene una sección final de cierre. Ninguna evidencia local se incorpora a Git.


### Cierre técnico del archivo temporal

Completados DTO explícitos para historial y cinco análisis, modelos y validación de respuestas Angular y pruebas de casos límite de recuperación/cohortes/promedios ausentes. Verificación final: 35 pruebas Angular correctas y build de producción correcto; backend: 20 pruebas ejecutadas sin fallos, incluidas dos de integración de análisis/trazabilidad, y ocho pruebas opcionales omitidas por sus propiedades de activación. `git diff --check` correcto. El temporal se actualizó sin estados históricos contradictorios. No quedan tareas de implementación de su alcance.


## Revisión documental — 30 de octubre de 2026

Se contrastaron controladores, filtros, ETL, historial y componentes de visualización con los manuales y notas técnicas. Se consolidaron los apartados desactualizados y se conservaron las verificaciones anteriores con su alcance y fecha. Las cifras del análisis siguen siendo la instantánea del 29 de octubre; no se consultaron nuevamente las bases.

Primer versionado de `.docs/`: commit `14808c1` (`documentacion publica temporal`), rama `tmp/docs`, confirmado por el usuario y comprobado con `git ls-files` y `git show --stat`. Esto cierra la evidencia local de C11-12; no acredita publicación remota ni entrega final. Las menciones anteriores a evidencias exclusivamente locales describen su situación previa a ese commit.

Esta revisión no ejecuta suites backend/frontend, cargas, reinicios, pruebas de caídas ni navegador. Sus comprobaciones son consistencia con el código, enlaces relativos de los documentos modificados y `git diff --check`. Esta actualización no modifica, traslada ni incorpora el archivo Excalidraw sin seguimiento, conforme a la instrucción final del usuario de no trasladarlo.
