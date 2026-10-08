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

Los puertos están publicados solo en `127.0.0.1`. Si alguno está ocupado, cambia el puerto correspondiente en `.env`. La configuración de Compose no transmite automáticamente sus variables al backend ejecutado fuera de Docker; las conexiones de Spring Boot se configurarán por separado.

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

En Windows utiliza `mvnw.cmd spring-boot:run`. Maven y las dependencias se descargan durante la primera ejecución. La API usa el puerto predeterminado `8080`. Las conexiones a MongoDB y Neo4j y los endpoints todavía requieren implementación y configuración.

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
