# Buscador de aspirantes

La ruta Angular **Búsqueda** consulta siempre el backend; no accede directamente a MongoDB. La API usa la colección `evaluaciones`, cuyos documentos representan una evaluación y no una persona única.

## Endpoints

- `GET /api/busqueda?criterio=<criterio>&termino=<texto>&limite=50`: devuelve hasta 100 evaluaciones coincidentes.
- `GET /api/busqueda/aspirantes/{correlativo}`: devuelve toda la información disponible y las evaluaciones del correlativo solicitado, ordenadas por año, fecha, materia e intento.

Los criterios disponibles son `aspirante`, `carrera`, `institucion`, `materia`, `municipio` y `departamento`. Salvo `aspirante`, que es coincidencia exacta por el correlativo anónimo, las búsquedas de texto son parciales y no distinguen mayúsculas/minúsculas. Un término vacío, un criterio desconocido o un límite menor que uno devuelve un error HTTP 400; un correlativo que no existe devuelve 404.

## Decisión sobre institución educativa

Los CSV asignados no incluyen nombre o código de un centro educativo. Solo contienen el tipo de institución, municipio y departamento de procedencia. Por ello, el criterio `institucion` consulta `procedencia.tipoInstitucion` y la interfaz lo llama explícitamente **Tipo de institución educativa**; no se presenta como una búsqueda por nombre de colegio. Municipio y departamento permanecen como criterios separados.

## M11 y M15

El detalle por correlativo materializa ambas consultas obligatorias:

- **M11:** la tabla conserva cada evaluación con materia, número de oportunidad y resultado, para representar el historial de desempeño.
- **M15:** la misma tabla muestra todos los campos disponibles por evaluación: fecha, sexo, nacimiento/edad si fueron registrados, carrera y procedencia educativa.

No se colapsan filas de un aspirante: un mismo correlativo puede tener varias materias, intentos y años de ingreso. El ETL ya elimina duplicados exactos; el buscador muestra las evaluaciones distintas que quedaron almacenadas.

## Comprobaciones

- La prueba Angular `search.spec.ts` cubre coincidencia, visualización de dos evaluaciones de un aspirante y término vacío.
- La prueba de integración `ApplicantSearchIntegrationTest`, activada con `-Dsearch.integration=true`, inserta dos evaluaciones temporales, verifica coincidencia, cero resultados, entrada inválida e historial, y las elimina al finalizar.

Ejecutar desde `backend/` con MongoDB disponible:

```bash
JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./mvnw -o -Dsearch.integration=true -Dtest=ApplicantSearchIntegrationTest test
```
