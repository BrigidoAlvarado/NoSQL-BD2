# Verificación de entrega — 29 de octubre de 2026

## Evidencia ejecutada

- Java 25: `./mvnw -o verify` correcto; 17 pruebas ejecutadas y cuatro integraciones inicialmente desactivadas.
- Bases locales iniciadas con `docker compose up -d --wait`: ambas saludables.
- `./mvnw -o -Detl.integration=true -Dsearch.integration=true -Dstatistics.integration=true -Dgraph.integration=true test`: 21 pruebas, cero fallos, cero errores y cero omitidas. Incluye carga/repetición de muestra, búsqueda, consistencia estadística y ejecución de G01–G10.
- Angular: 24 pruebas correctas y compilación de producción correcta.
- Python: ocho pruebas correctas.
- Respuesta real de estadísticas documentada en ANALISIS_Y_CONCLUSIONES.md.

## Pendientes de aceptación

- C09-10: comprobar en navegador los mensajes durante una caída real de cada servicio y su recuperación.
- C01-10, C12-01–03: completar recorrido visual, carga múltiple y comparación antes/después de reiniciar los servicios.
- C11-13: instalar desde una copia limpia siguiendo el manual.
- C11-03: confirmar publicación de la versión final en GitHub. Los commits locales no equivalen a publicación.
- C12-04–07: comprensión, práctica de modificaciones y entrega del estudiante.

Las pruebas de integración existentes comprueban ejecución y algunos invariantes; no constituyen una comparación exhaustiva de todas las consultas con resultados calculados independientemente.
