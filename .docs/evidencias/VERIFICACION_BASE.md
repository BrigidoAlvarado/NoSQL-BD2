# Verificación de la base del proyecto

Verificación del 20 de octubre de 2026. Alcance: infraestructura, navegación e integración del diagnóstico de conexiones. No certifica ETL ni consultas funcionales pendientes.

## Evidencia

| Comprobación | Resultado |
| --- | --- |
| `./mvnw -o verify` desde `backend/`, con JDK 25 | 6 pruebas correctas; empaquetado correcto |
| `npm test` desde `frontend/` | 12 pruebas correctas en 4 archivos |
| `npm run build` desde `frontend/` | Compilación de producción correcta |
| `docker compose up -d --wait --wait-timeout 180` | Ambos motores saludables |
| `GET http://127.0.0.1:8080/api/health` | HTTP 200; los tres estados en `UP` |
| `GET http://127.0.0.1:4200/api/health` | HTTP 200; mismo resultado mediante el proxy |
| `GET http://127.0.0.1:4200/estadisticas` | HTTP 200; documento Angular servido |

El Java predeterminado de este equipo es 21; se usó `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./mvnw -o verify` desde `backend/`. `-o` permite usar dependencias previamente descargadas; en una instalación nueva ejecuta `./mvnw verify` con JDK 25 y acceso a red. Mockito no pudo adjuntar su agente dentro del entorno aislado; las seis pruebas pasaron al ejecutarse fuera del aislamiento sin cambios en el backend.

Las pruebas del panel cubren éxito, falla individual y conjunta de los motores, recuperación con el botón, errores de red, respuesta inconsistente, límite de espera y cancelación al destruir la vista. Los fallos y la recuperación usan respuestas simuladas; no se apagaron bases para provocar fallas reales. Las pruebas existentes también cubren navegación, selección local de CSV, cliente HTTP y diagnóstico del backend.

No se realizó una verificación visual automatizada en un navegador real.

## Prueba manual del usuario

Con las bases y las aplicaciones iniciadas según el README:

1. Abre `http://localhost:4200`. Debe aparecer «Servicios disponibles» y los tres servicios disponibles.
2. Pulsa «Comprobar conexión». El botón debe deshabilitarse mientras espera y habilitarse al terminar.
3. Navega por Estadísticas, Carga de datos, Búsqueda y Explorador de grafos. El panel permanece visible; las secciones pendientes deben indicar su estado sin resultados ficticios.
4. En Carga de datos, selecciona varios CSV no vacíos. Deben listarse localmente; todavía no existe envío a las bases. Prueba «Limpiar selección».
5. Comprueba el uso con teclado y una ventana estrecha.

Estas pruebas visuales quedan para el usuario. La API, ambos motores y el proxy ya se verificaron por HTTP.

## Dependencias pendientes

Para cerrar C01-08 y C01-10 falta revisar la calidad de los CSV, confirmar que corresponden a los archivos asignados, definir modelos, implementar ETL y endpoints funcionales y comprobar resultados en la interfaz. Seleccionar un CSV en la pantalla actual no completa una carga.

Por instrucción del usuario, toda esta documentación se conserva localmente en `.docs/`, excluida de Git: arquitectura, verificaciones, checklist maestro y plan de ramas. Los commits de esta etapa incluyen únicamente código, pruebas y los README actualizados.

## Cierre del cambio de infraestructura

Commit `ae17ed8` en `feat/base-proyecto`: únicamente código, pruebas y README. Arquitectura y verificaciones se movieron a `.docs/` y se comprobó que están excluidas de Git, junto con el checklist, el plan y `.data/`. Árbol de trabajo limpio tras el commit.

Se encontraron tres CSV locales: 2015 (20 277 filas), 2022 (24 237 filas) y 2024 (13 863 filas), todos con 12 columnas. Los de 2022 y 2024 tienen encabezados abreviados y un orden distinto al de 2015; por tanto, el ETL deberá reconocer sus encabezados en vez de asumir posiciones iguales. Esta inspección inicial no cierra B01.01: falta analizar calidad y documentar procedencia y selección de archivos.
