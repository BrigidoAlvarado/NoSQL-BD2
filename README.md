# Admisiones USAC

Aplicación para analizar evaluaciones de aspirantes a la Facultad de Ingeniería de la USAC.

## Estructura

```text
backend/     API Spring Boot, código Java y Maven Wrapper
frontend/    Aplicación Angular y recursos visuales
scripts/     Herramientas de revisión y validación de datos
compose.yaml Contenedores de MongoDB y Neo4j
.env.example Plantilla de configuración local sin credenciales reales
```

## Bases de datos en contenedores

Requiere Docker Engine en ejecución y Docker Compose v2. Ejecuta los comandos desde la raíz del repositorio. Si no tienes `.env`, copia `.env.example` a `.env` y cambia ambas contraseñas. El archivo `.env` está excluido de Git.

```bash
docker compose config --quiet
docker compose up -d --wait --wait-timeout 180
docker compose ps
```

La configuración utiliza la [imagen oficial de MongoDB](https://hub.docker.com/_/mongo) y [Neo4j 5.26 Community LTS](https://neo4j.com/blog/developer/neo4j-v5-lts-evolution/). Las etiquetas permiten recibir parches dentro de esas líneas; para reproducir exactamente una instalación puedes fijar las imágenes por digest en `.env`. Los contenedores tienen comprobaciones de salud autenticadas y volúmenes persistentes. Como decisión de desarrollo local, MongoDB utiliza el usuario administrador inicial; para una instalación con permisos restringidos, configura un usuario con acceso a la base de la aplicación.

| Servicio | Dirección predeterminada | Credenciales |
| --- | --- | --- |
| MongoDB | `localhost:27017` | `MONGODB_USERNAME` y `MONGODB_PASSWORD`, autenticación en `admin` |
| Neo4j Browser | `http://localhost:7474` | Usuario `neo4j` y `NEO4J_PASSWORD` |
| Neo4j Bolt | `bolt://localhost:7687` | Usuario `neo4j` y `NEO4J_PASSWORD` |

Los puertos están publicados solo en `127.0.0.1`. Si alguno está ocupado, cambia el puerto correspondiente en `.env`. Spring Boot importa el `.env` de la raíz al iniciarse desde `backend/` o desde la raíz; las variables del sistema pueden sobrescribir esos valores.

Para consultar los registros o detener los servicios conservando los datos:

```bash
docker compose logs --tail 100 mongodb neo4j
docker compose stop
```

`docker compose down` elimina los contenedores y la red, pero conserva los volúmenes. No añadas `--volumes` salvo que quieras borrar los datos. Las contraseñas iniciales se aplican al crear los datos por primera vez; modificar `.env` no cambia las contraseñas de una base ya inicializada.

## Backend

Requiere Java 25. Desde la raíz del repositorio:

```bash
cd backend
./mvnw spring-boot:run
```

En Windows utiliza `mvnw.cmd spring-boot:run`. Maven y las dependencias se descargan durante la primera ejecución. La API usa el puerto predeterminado `8080`.

Las conexiones usan `spring.mongodb` (Spring Boot 4) y `spring.neo4j`. MongoDB apunta por defecto a `admisiones_usac`, con autenticación en `admin`; Neo4j utiliza la base `neo4j`. Las credenciales se toman de `MONGODB_USERNAME`, `MONGODB_PASSWORD` y `NEO4J_PASSWORD`, sin contraseñas incluidas en el código. Los valores adicionales de `.env.example` permiten cambiar hosts, puertos y bases. MongoDB crea la base de la aplicación al insertar el primer documento.

El `.env` se importa como archivo de propiedades Java: usa `CLAVE=valor` sin comillas, sin prefijo `export` y sin comentarios en la misma línea. Si una contraseña contiene barras invertidas, usa variables de entorno del proceso para evitar las reglas de escape del formato de propiedades. No cambies las credenciales locales sin actualizar también las bases de datos existentes.

En IntelliJ configura el JDK 25 y establece el directorio de trabajo de la ejecución en `backend/` o en la raíz del repositorio. Ejecuta `ApiAdmisionesUsacApplication` con las bases de datos encendidas.

Para comprobar las conexiones desde el backend:

```bash
curl -i http://localhost:8080/api/health
```

La ruta ejecuta `ping` en MongoDB y `RETURN 1 AS connected` en Neo4j, sin modificar datos. Devuelve HTTP 200 y `{"status":"UP","mongodb":"UP","neo4j":"UP"}` si ambas responden; si alguna falla, devuelve HTTP 503 con su estado `DOWN`. No expone contraseñas ni detalles internos de los errores. Al editar `.env`, reinicia el backend para cargar los nuevos valores.

Las pruebas automatizadas usan clientes simulados para los estados de conexión y comprueban el arranque del contexto con credenciales de prueba; no requieren bases disponibles. La conexión real se comprueba con `/api/health`. El ETL, la carga, las estadísticas, la búsqueda y los grafos están implementados.

Para compilar, ejecutar las pruebas y empaquetar el backend, usa `./mvnw verify` desde `backend/`. Si tu Java predeterminado es otra versión, configura `JAVA_HOME` para apuntar al JDK 25 antes de ejecutar Maven.

Spring Boot permanece en `4.1.1`. El POM fija MongoDB Driver `5.11.1`, Logback `1.6.3`, Tomcat `11.0.26` y el BOM de Jackson `3.1.7` para incorporar correcciones de seguridad. Las propiedades actualizan conjuntamente los módulos de cada familia. Revisa estos ajustes al actualizar Spring Boot para volver a sus versiones administradas cuando incluyan las correcciones.

## Frontend

Consulta los requisitos de Node.js y npm en el [README del frontend](frontend/README.md).

En otra terminal, desde la raíz del repositorio:

```bash
cd frontend
npm ci
npm start
```

Abre `http://localhost:4200`. El proxy de desarrollo reenvía `/api/**` a `http://localhost:8080`.

El panel «Estado de los servicios» comprueba la API y ambas bases al iniciar la aplicación y permite reintentar sin bloquear la navegación. El diagnóstico comprueba conectividad; las pantallas funcionales permiten cargar y consultar los datos.


## Revisión de los CSV

La herramienta `scripts/perfilar_csv.py` permite reproducir la revisión de estructura, calidad, duplicados y cardinalidad con Python 3, sin dependencias adicionales. Es una herramienta de análisis de solo lectura; no sustituye la carga desde Angular ni el ETL de Spring Boot.

Coloca los CSV del [portal oficial](https://datosabiertos.ingenieria.usac.edu.gt/en/dataset/pruebas-especificas) en `.data/`. Para ejecutar las pruebas del perfilador, desde la raíz:

```bash
python3 -m unittest discover -s scripts -p 'test_*.py'
```

Los CSV y los informes generados localmente permanecen excluidos de Git.

## Esquemas de datos

Los índices de MongoDB y las restricciones e índices de Neo4j están declarados en `backend/src/main/resources/db/`. **Arrancar los contenedores no ejecuta automáticamente estos scripts.** Antes de la primera carga, ejecútalos desde la raíz del repositorio, sin cargas en curso:

```bash
docker compose up -d --wait --wait-timeout 180
docker compose exec -T mongodb sh -c 'mongosh --quiet --username "$MONGO_INITDB_ROOT_USERNAME" --password "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin admisiones_usac' < backend/src/main/resources/db/mongodb/01_indices.js
docker compose exec -T neo4j sh -c 'cypher-shell --non-interactive --username neo4j --password "${NEO4J_AUTH#*/}" --database neo4j' < backend/src/main/resources/db/neo4j/01_esquema.cypher
docker compose exec -T neo4j sh -c 'cypher-shell --non-interactive --username neo4j --password "${NEO4J_AUTH#*/}" --database neo4j "CALL db.awaitIndexes(300);"'
```

El último comando espera hasta 300 segundos a que los índices de Neo4j estén disponibles. Si algún paso devuelve un error, resuélvelo antes de iniciar una carga. El estado `healthy` de Docker o `UP` de `/api/health` confirma disponibilidad, no la existencia de índices.

Comprueba MongoDB desde su consola de consultas, por ejemplo en IntelliJ:

```javascript
use admisiones_usac;
db.evaluaciones.getIndexes();
```

La colección `evaluaciones` debe tener **ocho índices**: `_id_` y siete adicionales. El mismo script también prepara índices de `cargas`, `rechazos_carga` y `resumen_carrera`.

En Neo4j Desktop, Query o Neo4j Browser, conectado a la base `neo4j`, ejecuta estas consultas por separado:

```cypher
SHOW INDEXES
YIELD name, type, state, labelsOrTypes, properties
RETURN name, type, state, labelsOrTypes, properties;
```

```cypher
SHOW CONSTRAINTS;
```

Con el esquema del proyecto deben aparecer **catorce índices en estado `ONLINE`** y **nueve restricciones de unicidad**. Los catorce índices corresponden a los nueve que respaldan esas restricciones, tres adicionales de evaluaciones y dos generales `LOOKUP`. Los índices sobre `Aspirante.id` y `Evaluacion.id` permiten localizar los nodos durante los `MERGE` del ETL; sin ellos, la carga puede volverse progresivamente más lenta conforme crece el grafo.

Los comandos son repetibles y pueden aplicarse sobre los datos existentes. **Después de eliminar los volúmenes con `docker compose down --volumes` o `docker compose down -v`, hay que levantar los motores y repetir esta preparación antes de cargar CSV.** Detener, reiniciar o recrear contenedores conservando los volúmenes mantiene tanto los datos como los índices y restricciones. No es necesario volver a cargar archivos que ya terminaron en `COMPLETA` para añadir los índices faltantes.

Para comprobar el diseño con una fila real y limpiar automáticamente los datos temporales creados, ejecuta `python3 scripts/validar_modelos_reales.py .data/pruebas_especificas_2024.csv`.

## ETL de CSV

El backend incluye el servicio interno `CsvImportService`, que valida y transforma CSV UTF-8 con los encabezados publicados, registra rechazos por línea, guarda evaluaciones idempotentemente en MongoDB y sincroniza el grafo en Neo4j. La API HTTP y Angular ejecutan este flujo desde Carga de datos.

Para ejecutar la prueba de integración contra los contenedores locales, desde `backend/`:

```bash
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./mvnw -Detl.integration=true -Dtest=CsvImportIntegrationTest test
```

La prueba inserta una fila temporal y la borra; comprueba la persistencia en ambos motores, la repetición sin duplicados y el reprocesamiento al cambiar la versión de las reglas del ETL.

## Carga desde la interfaz

En **Carga de datos**, selecciona uno o más CSV y pulsa «Cargar y procesar». Angular los envía a `POST /api/cargas`; el backend aplica el ETL y la pantalla presenta por archivo los conteos, duración, estado y causas de las primeras 100 filas rechazadas. El máximo es 30 MB por archivo y 100 MB por solicitud. El historial conserva una entrada por contenido de archivo y permite consultar evaluaciones válidas y rechazos, incluso después de recargar la página. Las vistas se paginan en grupos de 20 registros:

- `GET /api/cargas?page=0`: historial de cargas.
- `GET /api/cargas/progreso`: estado del lote actual o del último lote atendido por el backend.
- `GET /api/cargas/{sha256Archivo}/detalle/validos?page=0`: evaluaciones y sus fuentes del archivo seleccionado.
- `GET /api/cargas/{sha256Archivo}/detalle/rechazos?page=0`: línea, causa y valores originales disponibles.

Se conserva `GET /api/cargas/{sha256Archivo}/rechazos?limit=100` para consultar la lista inicial de rechazos. La duración se muestra al terminar la carga; en el historial aparece como no disponible cuando no se ha almacenado.

### Seguimiento al navegar y mensajes de avance

La solicitud y la selección de archivos se conservan en un servicio global de Angular. Cambiar de sección no cancela la carga: un aviso global permite volver al detalle, donde se muestran los resultados y mensajes con hora. El seguimiento consulta `/api/cargas/progreso` cada tres segundos y muestra el archivo actual, su posición en el lote, la validación y las evaluaciones procesadas en MongoDB o Neo4j. Cada contador avanza después de confirmar un lote de hasta 500 evaluaciones; no representa necesariamente documentos nuevos, porque el ETL evita duplicados. Los contadores corresponden a una etapa, no al porcentaje total del proceso.

Mientras hay una carga activa, Angular impide enviar otro lote o modificar la selección. El backend también reserva un único lote por instancia y devuelve HTTP `409` ante envíos simultáneos, incluso desde otros navegadores. La reserva se libera al terminar o fallar el procesamiento. Un fallo de seguimiento muestra un aviso y se reintenta; no se considera una cancelación del ETL.

Como decisión de diseño, el backend mantiene en memoria el último lote, sus resultados y los últimos 200 mensajes de progreso. No se transmiten logs internos ni credenciales. Recargar el navegador permite recuperar el seguimiento mientras siga activo ese backend, pero no recupera los objetos `File` seleccionados ni reenvía los archivos automáticamente. Reiniciar Spring Boot interrumpe la petición y borra este seguimiento temporal; la trazabilidad por archivo y los datos confirmados permanecen en las bases. La solución está pensada para una sola instancia local, sin cola de trabajos, reanudación automática ni botón de cancelación.

La persistencia del estado, los mensajes y el bloqueo no aceleran el ETL. Para comprobarlos sin modificar las bases del usuario, las pruebas simulan una carga detenida en Neo4j, consultan su avance, rechazan un segundo envío y verifican que navegar fuera de Carga y regresar conserva la solicitud y los mensajes. Ejecutar `./mvnw test` desde `backend/` y `npm test` desde `frontend/`.

## Dashboard de estadísticas

La aplicación implementa **25 consultas MongoDB**: 23 en Estadísticas y M11/M15 en Búsqueda. En cada tema se selecciona una sola consulta, identificada por su código Mxx, con tabla y gráfica asociada.

`GET /api/estadisticas/dashboard` devuelve M01–M10, M12–M14 y M16–M20. La subsección **Análisis adicionales** consulta `GET /api/estadisticas/adicionales/{id}` para M21–M25: aprobación por oportunidad, recuperación tras un fallo, materias distintas por aspirante, intervalo entre asignaciones y calidad de información por cohorte.

Los selectores de categorías envían parámetros opcionales al backend. En MongoDB se aplican antes de agrupar, por lo que se recalculan los totales, porcentajes y rankings del subconjunto. Por ejemplo:

```bash
curl 'http://localhost:8080/api/estadisticas/dashboard?materia=MATEMATICA&tipoInstitucion=PRIVADO'
curl 'http://localhost:8080/api/estadisticas/adicionales/M21?anio=2024&materia=MATEMATICA'
```

Las tablas y gráficas usan la misma respuesta filtrada. Las gráficas muestran hasta ocho grupos; las tablas conservan todos los resultados recibidos. Si no hay datos cargados, la pantalla ofrece acceso a Carga de datos.

M16 aplica los filtros antes de contar aspirantes distintos por carrera con resultado `NO APROBADO` en la oportunidad 1. M20 presenta por año las cantidades y los dos porcentajes: `porcentajeAprobacion` y `porcentajeNoAprobacion`. Ambos usan como denominador el total de evaluaciones de ese año dentro del subconjunto filtrado y se redondean a dos decimales.

La colección auxiliar `resumen_carrera` se recalcula al consultar el dashboard sin filtros. M06 con filtros devuelve el resumen del subconjunto sin sobrescribir la colección general.

## Buscador de aspirantes

La ruta **Búsqueda** consulta `GET /api/busqueda` con los criterios de correlativo anónimo, carrera, tipo de institución educativa, materia, municipio y departamento. Desde cada coincidencia se puede abrir `GET /api/busqueda/aspirantes/{correlativo}` para ver el historial de evaluaciones (M11) y toda la información disponible por evaluación (M15). Los CSV no traen el nombre de un establecimiento, por lo que “institución” representa el tipo de institución de procedencia.

## Errores y recuperación

La API devuelve mensajes controlados para archivos inválidos, procesamiento, consultas y motores no disponibles; no expone contraseñas ni detalles de conexión. La interfaz muestra el mensaje sin cerrar la aplicación y mantiene disponible el reintento. MongoDB y Neo4j se diferencian en `GET /api/health` y en los errores de operación.

## Explorador de grafos

La ruta **Explorador de grafos** consulta Neo4j mediante la API. La muestra visual admite año, carrera, resultado y departamento, con selectores de categorías y un control de 5 a 40 evaluaciones; comienza con 10. El número de nodos depende de las entidades compartidas y se muestra por separado.

El lienzo utiliza `vis-network`: distribución por fuerzas, zoom, desplazamiento, ajuste de vista, selección con detalle, resaltado de vecinos y controles por tipo de entidad. Cada uno de los ocho tipos tiene un color propio. Ocultar un tipo también oculta sus relaciones incidentes.

Las subsecciones Carreras, Trayectorias, Resultados y Procedencia contienen **10 consultas Cypher, G01–G10**. Cada sección permite seleccionar una consulta por código y nombre, con tabla y gráfica de barras asociada. La selección puede abrirse mediante una URL como `/grafos?seccion=resultados&consulta=G02`.

Los filtros analíticos son parámetros de `GET /api/grafos/consultas/{id}` y restringen los grupos calculados, conservando sus conteos. Son independientes de los filtros de la muestra visual. Por ejemplo:

```bash
curl 'http://localhost:8080/api/grafos/consultas/G02?materia=MATEMATICA&resultado=APROBADO'
```

## Organización de las consultas

- MongoDB: un archivo por consulta en `backend/src/main/java/com/nosql/apiadmisionesusac/statistics/queries/`; M11/M15 están en `search/queries/`.
- Neo4j: un recurso por consulta en `backend/src/main/resources/queries/neo4j/Gxx_*.cypher`. `explorar_grafo.cypher` corresponde a la muestra visual y no forma parte de las diez consultas analíticas.
- Angular: funcionalidades separadas en `frontend/src/app/features/`; los componentes visuales reciben los resultados del backend y las reglas de presentación compartidas están en `shared/`.

## Pruebas de integración

Las pruebas de integración se activan explícitamente y requieren ambos motores disponibles, credenciales configuradas y los esquemas aplicados. Desde `backend/`, con Java 25:

```bash
./mvnw -Detl.integration=true -Dsearch.integration=true -Dstatistics.integration=true -Dgraph.integration=true -Dadditional.integration=true -Dfilters.integration=true verify
```

Estas pruebas incluyen carga repetida, búsqueda, consultas MongoDB/Cypher, análisis adicionales, paginación de rechazos y filtros parametrizados. Las muestras temporales se limpian al finalizar; las comprobaciones sobre datos cargados requieren un conjunto de evaluaciones disponible. Este comando describe cómo ejecutarlas y no sustituye la revisión del recorrido completo desde la interfaz.
