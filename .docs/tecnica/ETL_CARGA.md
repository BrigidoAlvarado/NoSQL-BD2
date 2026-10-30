# ETL de cargas CSV

Alcance: C04-01 a C04-16. La pantalla Carga de datos envía archivos a `POST /api/cargas`; `CsvUploadService` los adapta y `CsvImportService` coordina validación, transformación y almacenamiento en ambos motores.

## Lectura y reglas

`CsvEvaluationProcessor` decodifica únicamente UTF-8, acepta CSV entrecomillado y valida que existan las doce columnas esperadas, sin exigir un orden. Reconoce los encabezados publicados: `correlativo_aspirante`, `numero_de_fecha_de_evaluacion`/`numero_de_fecha_de_evaluaci`, `anio_de_ingreso`, `carrera_objetivo`, `departamento_institucion_ed` y `municipio_institucion_`.

La fecha se transforma a `LocalDate`; oportunidad, años y edad a enteros. `sexo` se normaliza a `M`, `F` o `SIN REGISTRO` y `aprobacion` a `APROBADO`/`NO APROBADO`, tras quitar espacios externos y convertir mayúsculas. No se recortan ni corrigen las categorías de materia, carrera o procedencia: hacerlo podría fusionar valores realmente distintos o truncados de los CSV.

| Caso | Tratamiento |
| --- | --- |
| `SIN REGISTRO` en nacimiento | Conserva la evaluación con `anioNacimiento`, `edadIngreso` en `null` y marca de desconocido. |
| Edad fuera de 0 a 100 | Conserva la fila; edad queda `null`. |
| `NINGUNO` en carrera | Conserva texto y marca la carrera como no conocida. |
| `SIN DATOS` de procedencia | Conserva texto y marca cada dimensión afectada como no conocida. |
| Fecha, año, oportunidad, sexo, aprobación, campo requerido o número de columnas inválidos | Rechaza solo esa fila e indica línea, código, detalle y valores. |
| Archivo sin encabezado válido, UTF-8 inválido o comillas sin cerrar | Rechaza el archivo completo sin intentar una carga parcial. |

El identificador de una evaluación es SHA-256 de los doce valores canónicos. La fecha distinta forma parte del identificador; dos evaluaciones diferentes de un mismo correlativo se preservan.

## Persistencia, estados y recuperación

`cargas` se identifica por SHA-256 del archivo. Las filas válidas se escriben en MongoDB con `upsert` por `_id`; `fuentes` usa `addToSet` e incluye archivo y fila. Los rechazos se guardan en `rechazos_carga`, indexados por archivo y fila. Neo4j hace `MERGE` por las claves del modelo para nodos y relaciones.

Estados: `RECIBIDA`, `MONGO_LISTO`, `COMPLETA`, `RECHAZADA`, `SIN_FILAS_VALIDAS`, `FALLO_MONGO` y `PENDIENTE_NEO4J`. Si MongoDB falla no se intenta Neo4j. Si Neo4j falla después de MongoDB, el resultado queda `PENDIENTE_NEO4J`; al volver a presentar exactamente el archivo se repite el `upsert` idempotente y se reintenta el grafo. Un archivo cuya carga ya está `COMPLETA` devuelve su resultado sin volver a insertar.

## Verificación

- `CsvEvaluationProcessorTest` cubre valores especiales, encabezados truncados, tipos inválidos, CSV malformado y hashes repetidos/diferentes.
- `CsvImportIntegrationTest` se activa solo contra los contenedores locales: `JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64 ./mvnw -o -Detl.integration=true -Dtest=CsvImportIntegrationTest test` desde `backend/`. Inserta una fila temporal `C04_VALIDACION_20261021`, verifica ambos motores y la repetición, y la elimina al terminar.

## Datos procesados en ambos motores sin duplicados innecesarios

Estado de implementación: disponible. `EtlMongoStore.saveEvaluations` guarda documentos en MongoDB con `upsert` por `_id`, `$setOnInsert` y `$addToSet` para las fuentes. `EtlNeo4jStore.saveEvaluations` usa `MERGE` por las identidades de cada entidad y por origen/tipo/destino para sus relaciones. Ambos motores comparten el hash canónico de evaluación.

La identidad de evaluación depende de los doce campos canónicos; no solo del correlativo. Una persona con varias materias, oportunidades o fechas puede tener varias evaluaciones legítimas. Materias, carreras y procedencias compartidas se reutilizan como nodos. Las diferencias de texto preservadas del CSV pueden formar categorías distintas; no deben fusionarse automáticamente al comprobar duplicados.

El esquema `backend/src/main/resources/db/neo4j/01_esquema.cypher` declara nueve restricciones de unicidad. MongoDB garantiza `_id` único y el script de índices añade unicidad de hash de carga y archivo/fila de rechazo. La presencia de los scripts no sustituye comprobar que el esquema esté aplicado en la instancia usada.

### Protocolo de validación pendiente de demostración completa

1. Con ambos motores disponibles, registrar conteos iniciales de evaluaciones, aspirantes, nodos y relaciones. Usar una muestra identificable y conservar resultados antes/después; evitar cambios simultáneos de otras cargas.
2. Cargar el archivo A y comprobar estado `COMPLETA`. Confirmar que cada evaluación válida distinta tenga un documento MongoDB y un nodo Evaluacion con el mismo ID y las relaciones esperadas.
3. Reenviar exactamente A. Debe reutilizar la carga completa sin aumentar documentos, nodos o relaciones.
4. Cargar un archivo B con contenido diferente que incluya una evaluación idéntica de A. La evaluación compartida debe existir una sola vez por motor; MongoDB debe registrar las fuentes de ambos archivos. Este caso fuerza la ruta de escritura, a diferencia del reenvío de una carga ya completa.
5. Incluir en B una oportunidad o fecha diferente del mismo aspirante. Debe aparecer una evaluación nueva y conservarse un solo nodo Aspirante para su correlativo.
6. Comprobar ausencia de nodos y relaciones duplicados y verificar las restricciones. Comparar conjuntos de IDs MongoDB/Neo4j: igualdad de totales no demuestra por sí sola igualdad de registros.
7. Documentar los resultados y limpiar exclusivamente los datos temporales propios de la prueba; no borrar colecciones ni nodos de cargas del usuario.

Si hay cargas `PENDIENTE_NEO4J` u otras escrituras parciales, los totales globales pueden diferir. Primero revisar los estados y reconciliar con el mismo archivo. No declarar pérdida de datos únicamente por una diferencia global de conteos.

### Consultas de comprobación de solo lectura

En mongosh, seleccionar la base configurada para la aplicación (predeterminada `admisiones_usac`):

```javascript
db.evaluaciones.countDocuments({})
db.cargas.find({}, {nombreArchivo: 1, sha256Archivo: 1, estado: 1})
db.evaluaciones.getIndexes()
```

Para seguir un archivo concreto:

```javascript
const hashArchivo = "REEMPLAZAR_POR_SHA256_DEL_ARCHIVO";
db.evaluaciones.find(
  {"fuentes.sha256Archivo": hashArchivo},
  {_id: 1, aspiranteId: 1, materia: 1, oportunidad: 1, fuentes: 1}
)
```

En Neo4j Browser, ejecutar cada sentencia por separado:

```cypher
MATCH (e:Evaluacion) RETURN count(e) AS evaluaciones;
```

```cypher
MATCH (n) RETURN labels(n) AS etiquetas, count(*) AS nodos;
```

```cypher
MATCH ()-[r]->() RETURN type(r) AS tipo, count(*) AS relaciones;
```

```cypher
SHOW CONSTRAINTS;
```

Buscar identidades repetidas en las etiquetas del modelo (IDs, claves o códigos según corresponda):

```cypher
MATCH (n)
WHERE any(etiqueta IN labels(n) WHERE etiqueta IN [
  'Aspirante', 'Evaluacion', 'Materia', 'Carrera', 'Resultado',
  'TipoInstitucion', 'Departamento', 'Municipio', 'ProcedenciaEducativa'
])
WITH labels(n) AS etiquetas, coalesce(n.id, n.clave, n.codigo) AS clave,
     count(*) AS cantidad
WHERE clave IS NOT NULL AND cantidad > 1
RETURN etiquetas, clave, cantidad;
```

La comprobación anterior asume las etiquetas del modelo actual; no detecta categorías semánticamente equivalentes con textos distintos. Para comprobar relaciones repetidas:

```cypher
MATCH (a)-[r]->(b)
WITH elementId(a) AS origen, type(r) AS tipo,
     elementId(b) AS destino, count(*) AS cantidad
WHERE cantidad > 1
RETURN origen, tipo, destino, cantidad;
```

Ambas búsquedas deben devolver cero filas. Las restricciones únicas protegen identidades de nodos; no equivalen a una restricción única para las relaciones. Por eso se comprueban también estas últimas.

### Alcance real de la evidencia automatizada

`CsvImportIntegrationTest` verifica una evaluación en ambos motores y la reutilización del mismo archivo. Como el importador retorna anticipadamente para una carga `COMPLETA`, esa repetición no vuelve a ejecutar las escrituras ni demuestra por sí sola la deduplicación entre archivos distintos o la ausencia de relaciones duplicadas.

`CsvEvaluationProcessorTest` verifica que una fila idéntica produce el mismo hash y una fecha diferente produce otro. Queda por ampliar la integración con archivos diferentes que compartan filas, una nueva oportunidad del mismo aspirante y aserciones sobre nodos dimensionales, fuentes y relaciones. Estas comprobaciones adicionales se describen aquí como pendientes, no como pruebas ya ejecutadas en esta revisión.


## Sexo sin registro y versión de reglas

`SIN REGISTRO` en sexo representa información desconocida y conserva la evaluación válida. Se normalizan mayúsculas y espacios externos; no se infiere M ni F. Los campos vacíos siguen produciendo CAMPO_OBLIGATORIO y los valores no reconocidos SEXO_INVALIDO. M12 y G09 presentan SIN REGISTRO como categoría separada.

Las cargas registran `versionReglas` (actual: 2). Al volver a subir un archivo completo procesado con reglas anteriores o sin versión, el ETL lo reprocesa una vez; así puede recuperar filas rechazadas bajo la validación anterior. Los upserts de evaluaciones y MERGE de Neo4j conservan la idempotencia. Una carga completa con versión actual vuelve a reutilizarse. El cambio no reprocesa archivos automáticamente: requiere subir de nuevo el CSV.

Verificación: pruebas unitarias de aceptación, normalización y hash estable de SIN REGISTRO; rechazo de valores arbitrarios y campos vacíos. Prueba de integración: conservación de SIN REGISTRO en MongoDB y Neo4j, repetición sin duplicados y reprocesamiento de una carga sin versión. Datos temporales limpiados al terminar.
