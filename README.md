# Admisiones USAC

Aplicación para analizar evaluaciones de aspirantes a la Facultad de Ingeniería de la USAC. Los requisitos se encuentran en [`.docs/BRIEF_PROYECTO.md`](.docs/BRIEF_PROYECTO.md).

## Estructura

```text
backend/     API Spring Boot, código Java y Maven Wrapper
frontend/    Aplicación Angular
.docs/       Brief y enunciado del proyecto
AGENTS.md    Indicaciones para agentes
compose.yaml Contenedores de MongoDB y Neo4j
.env.example Plantilla de configuración local sin credenciales reales
```

## Bases de datos en contenedores

Requiere Docker Engine en ejecución y Docker Compose v2. Ejecuta los comandos desde la raíz del repositorio. Si no tienes `.env`, copia `.env.example` a `.env` y cambia ambas contraseñas. En el entorno donde se creó esta configuración se generó un `.env` local con contraseñas aleatorias; ese archivo está excluido de Git.

```bash
docker compose config --quiet
docker compose up -d --wait --wait-timeout 180
docker compose ps
```

La configuración utiliza la [imagen oficial de MongoDB](https://hub.docker.com/_/mongo) y [Neo4j 5.26 Community LTS](https://neo4j.com/blog/developer/neo4j-v5-lts-evolution/). Las etiquetas permiten recibir parches dentro de esas líneas; para reproducir exactamente una instalación puedes fijar las imágenes por digest en `.env`. Los contenedores tienen comprobaciones de salud autenticadas y volúmenes persistentes. Como decisión de desarrollo local, MongoDB utiliza el usuario administrador inicial; al integrar la API se debe crear un usuario con permisos sobre la base de la aplicación.

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

Las pruebas automatizadas usan clientes simulados para los estados de conexión y comprueban el arranque del contexto con credenciales de prueba; no requieren bases disponibles. La conexión real se comprueba con `/api/health`. Quedan pendientes el modelo de datos, ETL y endpoints de carga, consultas y búsqueda.

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

El panel «Estado de los servicios» comprueba la API y ambas bases al iniciar la aplicación y permite reintentar sin bloquear la navegación. El diagnóstico no carga datos ni sustituye la implementación del ETL y las consultas pendientes.

Consulta las [decisiones de arquitectura](.docs/ARQUITECTURA.md) y la [evidencia y pruebas manuales de la base](.docs/VERIFICACION_BASE.md).

## Revisión de los CSV

La herramienta `scripts/perfilar_csv.py` permite reproducir la revisión de estructura, calidad, duplicados y cardinalidad con Python 3, sin dependencias adicionales. Es una herramienta de análisis de solo lectura; no sustituye la carga desde Angular ni el ETL de Spring Boot.

Coloca los CSV del [portal oficial](https://datosabiertos.ingenieria.usac.edu.gt/en/dataset/pruebas-especificas) en `.data/`. Desde la raíz:

```bash
mkdir -p .docs
python3 scripts/perfilar_csv.py .data/*.csv --salida .docs/PERFIL_CSV.json
python3 -m unittest discover -s scripts -p 'test_*.py'
```

Los CSV y los informes locales permanecen excluidos de Git. Los criterios de conteo, edad y las diferencias entre archivos se documentan localmente en `.docs/DATOS_ORIGEN.md`.

## Esquemas de datos

Los índices de MongoDB y las restricciones e índices de Neo4j están declarados en `backend/src/main/resources/db/`. Antes de la primera carga, con los contenedores activos, aplícalos desde la raíz:

```bash
docker compose exec -T mongodb sh -c 'mongosh --quiet --username "$MONGO_INITDB_ROOT_USERNAME" --password "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin admisiones_usac' < backend/src/main/resources/db/mongodb/01_indices.js
cat backend/src/main/resources/db/neo4j/01_esquema.cypher | docker compose exec -T neo4j sh -c 'cypher-shell --non-interactive --username neo4j --password "${NEO4J_AUTH#*/}" --database neo4j'
```

Los comandos son repetibles. Para comprobar el diseño con una fila real y limpiar automáticamente los datos temporales creados, ejecuta `python3 scripts/validar_modelos_reales.py .data/pruebas_especificas_2024.csv`.

## ETL de CSV

El backend incluye el servicio interno `CsvImportService`, que valida y transforma CSV UTF-8 con los encabezados publicados, registra rechazos por línea, guarda evaluaciones idempotentemente en MongoDB y sincroniza el grafo en Neo4j. La API HTTP y el envío desde Angular se implementarán con la sección de carga de interfaz.

Las reglas de datos y los estados de recuperación se describen en `.docs/ETL_CARGA.md`. Para ejecutar la prueba de integración contra los contenedores locales, desde `backend/`:

```bash
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./mvnw -o -Detl.integration=true -Dtest=CsvImportIntegrationTest test
```

La prueba inserta una fila temporal y la borra; comprueba que una segunda carga del mismo archivo no duplica la evaluación.

## Carga desde la interfaz

En **Carga de datos**, selecciona uno o más CSV y pulsa «Cargar y procesar». Angular los envía a `POST /api/cargas`; el backend aplica el ETL y la pantalla presenta por archivo los conteos, duración, estado y causas de las primeras 100 filas rechazadas. El máximo es 30 MB por archivo y 100 MB por solicitud. Los rechazos de una carga se pueden consultar mediante `GET /api/cargas/{sha256Archivo}/rechazos?limit=100`.

## Dashboard de estadísticas

La ruta **Estadísticas** consulta `GET /api/estadisticas/dashboard` y presenta las consultas MongoDB M01, M02, M04, M12, M18 y M20 con sus unidades de conteo. Requiere MongoDB encendido y al menos una carga completa; si no hay evaluaciones, muestra el acceso a la pantalla de carga. Las definiciones, denominadores y comprobaciones están en `.docs/ESTADISTICAS_BASICAS.md`.

El dashboard también presenta M03, M05–M10, M13–M14 y M16–M17, M19. La colección MongoDB `resumen_carrera` se recalcula de forma idempotente al consultar el dashboard para M06; no necesita una carga separada.

## Buscador de aspirantes

La ruta **Búsqueda** consulta `GET /api/busqueda` con los criterios de correlativo anónimo, carrera, tipo de institución educativa, materia, municipio y departamento. Desde cada coincidencia se puede abrir `GET /api/busqueda/aspirantes/{correlativo}` para ver el historial de evaluaciones (M11) y toda la información disponible por evaluación (M15). Los CSV no traen el nombre de un establecimiento, por lo que “institución” representa el tipo de institución de procedencia. Las reglas, endpoints y prueba de integración se documentan en `.docs/BUSCADOR_ASPIRANTES.md`.

## Explorador de grafos

La ruta **Explorador de grafos** consulta Neo4j mediante `/api/grafos`, dibuja el subgrafo y permite filtros por año, carrera, resultado y departamento. En la misma vista se ejecutan G01 a G05. Los recorridos, criterios y límites se documentan en `.docs/EXPLORADOR_GRAFOS.md`.
