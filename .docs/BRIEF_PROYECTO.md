# Brief fuente primaria — Proyecto Final Bases de Datos 2

> Fuente: `enunciado_proyecto.pdf` (15 páginas). Este documento organiza fielmente los requisitos del enunciado para orientar la elaboración del proyecto. Cuando el enunciado deja una decisión al estudiante, se identifica como **decisión de diseño**, no como requisito impuesto.

## 1. Propósito del proyecto

Desarrollar una aplicación para almacenar, procesar, analizar y visualizar información de las evaluaciones de aspirantes a ingresar a la Universidad de San Carlos de Guatemala, usando conjuntamente:

- **MongoDB** como base documental para registros, consultas estadísticas y analíticas.
- **Neo4j** como base orientada a grafos para modelar y analizar relaciones entre los elementos del conjunto de datos.

La información de origen se encuentra en archivos CSV. La aplicación debe permitir cargarlos, transformarlos, almacenarlos en ambos motores y presentar los resultados desde una interfaz gráfica.

## 2. Objetivos del enunciado

### Objetivo general

Aplicar los conocimientos del curso para diseñar, implementar y analizar una solución NoSQL con MongoDB y Neo4j para almacenamiento, procesamiento, consulta y visualización de información.

### Objetivos específicos

- Comprender características, ventajas y casos de uso de MongoDB (documental) y Neo4j (grafos).
- Diseñar una arquitectura que integre la aplicación con ambos motores.
- Cargar, transformar y almacenar datos estructurados o semiestructurados.
- Construir consultas analíticas en MongoDB para identificar tendencias, distribuciones y patrones.
- Modelar nodos, propiedades y relaciones en Neo4j para analizar conexiones entre elementos del problema.
- Crear una interfaz gráfica para consultar, filtrar y visualizar resultados.
- Analizar ventajas y limitaciones de usar ambas clases de bases NoSQL en una misma solución.
- Documentar arquitectura, configuración, consultas, decisiones técnicas y resultados.

## 3. Alcance funcional mínimo

La solución debe incluir, como mínimo:

- Interfaz gráfica o frontend.
- Backend o API.
- Base de datos MongoDB.
- Base de datos Neo4j.
- Proceso de carga y transformación de datos (ETL).
- Consultas analíticas.
- Visualización de estadísticas.
- Visualización de relaciones mediante grafos.
- Documentación técnica.

## 4. Tecnologías y arquitectura

### Tecnologías obligatorias

- MongoDB.
- Neo4j.

### Tecnologías de libre elección

Frontend y backend son libres. El enunciado menciona como ejemplos Angular, React, Vue, HTML/CSS/JavaScript, Node.js/Express/NestJS, Java/Spring Boot, Python/Flask/FastAPI/Django y .NET, pero no exige ninguno en particular.

La elección debe ser coherente con la arquitectura e integrar correctamente MongoDB y Neo4j. Se evalúa el funcionamiento, la estructura y el cumplimiento de requisitos, no un framework específico.

### Reglas arquitectónicas

- Debe existir una separación clara entre interfaz gráfica, lógica de aplicación y bases de datos.
- El frontend debe comunicarse preferentemente con las bases de datos mediante el backend/API.
- **El frontend no debe conectarse directamente a MongoDB ni a Neo4j.**
- Se admite otra arquitectura si se justifica técnicamente y cumple todos los requisitos.

### Responsabilidades mínimas del backend

- Recibir solicitudes del frontend.
- Validar datos recibidos.
- Ejecutar consultas en MongoDB y Neo4j.
- Procesar resultados y devolver la información requerida al frontend.
- Manejar errores de forma controlada.

## 5. Contexto y fuente de datos

El problema corresponde a pruebas específicas de aspirantes de primer ingreso a la Facultad de Ingeniería de la Universidad de San Carlos de Guatemala. Los CSV dificultan consultas complejas, descubrimiento de patrones, análisis de relaciones y presentación para toma de decisiones; la aplicación debe resolver esas necesidades con MongoDB y Neo4j.

Fuente oficial indicada por el enunciado: [Datos Abiertos – Facultad de Ingeniería – Pruebas específicas](https://datosabiertos.ingenieria.usac.edu.gt/en/dataset/pruebas-especificas).

El portal ofrece archivos de distintos años; se deben usar los archivos de datos indicados para el proyecto.

## 6. Campos del dataset

Cada fila representa una evaluación realizada por un aspirante; no equivale necesariamente a un aspirante único.

| Campo | Descripción |
| --- | --- |
| `fecha_asignacion` | Fecha en la que el aspirante se asignó a la prueba. |
| `sexo` | Sexo registrado para el aspirante. |
| `anio_nacimiento` | Año de nacimiento del aspirante. |
| `materia` | Materia evaluada en la prueba específica. |
| `numero_de_fecha_de_evaluacion` | Número de oportunidad o fecha de evaluación del año de ingreso. |
| `anio_de_ingreso` | Año para el cual el aspirante desea ingresar. |
| `aprobacion` | Resultado de la evaluación. |
| `carrera_objetivo` | Carrera a la que el aspirante desea ingresar. |
| `departamento_institucion_educativa` | Departamento de la institución educativa de procedencia. |
| `municipio_institucion_educativa` | Municipio de la institución educativa de procedencia. |
| `tipo_institucion_educativa` | Tipo de institución educativa de procedencia. |
| `correlativo_aspirante` | Identificador anónimo para reconocer al aspirante. |

Ejemplo de registro original:

```json
{
  "fecha_asignacion": "2014-08-23",
  "sexo": "M",
  "anio_nacimiento": "SIN REGISTRO",
  "materia": "MATEMATICA",
  "numero_de_fecha_de_evaluacion": 1,
  "anio_de_ingreso": 2015,
  "aprobacion": "NO APROBADO",
  "carrera_objetivo": "NINGUNO",
  "departamento_institucion_educativa": "GUATEMALA",
  "municipio_institucion_educativa": "GUATEMALA - CIUDAD CAPITAL ZONA 1",
  "tipo_institucion_educativa": "PRIVADO",
  "correlativo_aspirante": "949d241ac10256edf11339a303c72a50"
}
```

### Consideraciones de interpretación

- `correlativo_aspirante` es el identificador anónimo del aspirante.
- Una misma persona puede figurar en múltiples filas por distintas evaluaciones o intentos.
- Esta cardinalidad debe considerarse en el diseño documental de MongoDB y en el grafo Neo4j.

## 7. ETL: carga, transformación y calidad de datos

Debe implementarse un proceso de extracción, transformación y carga que prepare los datos antes de almacenarlos o usarlos. Debe incluir como mínimo:

1. Lectura de CSV.
2. Validación de la estructura de los registros.
3. Conversión de tipos cuando corresponda.
4. Identificación y tratamiento de valores faltantes o especiales.
5. Estandarización de valores cuando sea necesaria.
6. Registro de datos válidos e inválidos.
7. Carga de información procesada a MongoDB.
8. Generación de nodos y relaciones en Neo4j.

Casos explícitamente señalados:

- `anio_nacimiento` puede contener `SIN REGISTRO`.
- `aprobacion` usa valores textuales como `APROBADO` y `NO APROBADO`.
- `carrera_objetivo` puede contener `NINGUNO`.
- Otros campos pueden tener diferencias de formato o valores que requieran estandarización.

Se deben **definir y documentar** las reglas aplicadas a estos casos. No es obligatorio eliminar todos los registros incompletos: pueden conservarse, transformarse o excluirse según el análisis, con justificación técnica.

### Resultado visible de cada carga

La interfaz debe permitir cargar uno o varios CSV y mostrar en cada proceso:

- Nombre del archivo.
- Total de registros leídos.
- Registros procesados correctamente.
- Registros rechazados o con error.
- Tiempo de procesamiento.
- Estado final de la operación.

Los registros rechazados deben poder identificarse y registrar la causa del rechazo.

### Flujo requerido

1. Selección y carga de uno o varios CSV.
2. Validación y transformación de registros.
3. Almacenamiento en MongoDB.
4. Generación de nodos y relaciones en Neo4j.
5. Consulta de la información almacenada.
6. Visualización de resultados en la interfaz.

Durante la carga, el backend debe recibir el archivo, procesar y validar registros, transformarlos según sea necesario, insertarlos en MongoDB, generar la información de Neo4j y reportar el resultado final. La implementación interna queda a criterio del estudiante.

## 8. Interfaz gráfica

### Dashboard de estadísticas

Debe presentar información relevante proveniente de consultas MongoDB. Puede usar gráficas de barras, pastel, líneas, indicadores, tablas u otros elementos visuales; biblioteca y representación son libres, siempre que permitan interpretar los datos.

### Explorador de grafos

Debe existir una sección que visualice gráficamente información obtenida de Neo4j. El estudiante define filtros y criterios de visualización para facilitar la exploración.

### Buscador

Debe permitir búsquedas en la información almacenada. Puede considerar, como mínimo recomendado por el enunciado:

- Aspirante.
- Carrera objetivo.
- Institución educativa.
- Materia.
- Municipio.
- Departamento.

Se pueden agregar criterios adicionales.

### Manejo de errores

Los errores deben mostrarse claramente sin cerrar inesperadamente la aplicación. Deben contemplarse, como mínimo, errores de carga de archivos, procesamiento de registros, conexión con MongoDB, conexión con Neo4j, ejecución de consultas y visualización de resultados.

## 9. Consultas MongoDB obligatorias

Las 20 consultas siguientes deben implementarse y visualizarse desde la aplicación. La forma de resolverlas depende del modelo documental definido por el estudiante.

| # | Consulta requerida |
| ---: | --- |
| 1 | Cantidad de aspirantes agrupados por tipo de institución educativa. |
| 2 | Cantidad de evaluaciones aprobadas agrupadas por materia. |
| 3 | Cantidad de aspirantes aprobados agrupados por carrera objetivo y año de ingreso. |
| 4 | Porcentaje de aprobación por materia. |
| 5 | Edad promedio de aspirantes agrupada por carrera objetivo. |
| 6 | Crear colección auxiliar `resumen_carrera` con información resumida por carrera a partir de los datos procesados. |
| 7 | Edad promedio de aspirantes aprobados, agrupada por carrera objetivo y tipo de institución educativa. |
| 8 | Cantidad de aprobados según municipio de la institución educativa y carrera objetivo. |
| 9 | Cantidad de evaluaciones por mes y materia, solo para instituciones públicas. |
| 10 | Top 5 de carreras objetivo más demandadas por aspirantes de 16 a 18 años. |
| 11 | Historial de desempeño de un aspirante: evaluaciones, materias, intentos y resultados. |
| 12 | Cantidad de aspirantes agrupados por sexo y tipo de institución educativa. |
| 13 | Porcentaje de aprobación por edad. |
| 14 | Número promedio de oportunidades de evaluación usadas por aspirantes para cada materia. |
| 15 | Toda la información disponible de un aspirante identificado por `correlativo_aspirante`. |
| 16 | Carreras con mayor cantidad de aspirantes que no aprobaron en su primer intento. |
| 17 | Top 5 de municipios de procedencia de instituciones educativas con más aspirantes registrados. |
| 18 | Porcentaje de aprobación, comparado por tipo de institución educativa. |
| 19 | Carreras objetivo con mayor demanda dentro de cada departamento de procedencia de la institución educativa. |
| 20 | Cantidad y porcentaje de evaluaciones aprobadas y no aprobadas, agrupadas por año de ingreso. |

Los resultados pueden presentarse con gráficas, tablas, indicadores, tarjetas, listados u otros componentes. No se exige la misma visualización para todas: debe escogerse una representación adecuada para cada resultado.

## 10. Neo4j: requisitos obligatorios

Se debe cargar el dataset también en Neo4j y diseñar un modelo de grafo con los nodos, propiedades y relaciones que el estudiante considere adecuados. El modelo debe representar elementos relevantes, tales como aspirantes, carreras, instituciones educativas, municipios, departamentos, materias y resultados de evaluación.

Debe cumplirse lo siguiente:

1. Cargar la información necesaria en Neo4j.
2. Evitar la creación innecesaria de nodos duplicados.
3. Implementar relaciones que representen conexiones relevantes.
4. Integrar las consultas Neo4j con el backend.
5. Visualizar al menos un grafo desde la interfaz.
6. Permitir al menos un filtro sobre la visualización del grafo.

### Consultas Cypher

- Implementar como mínimo **cinco consultas Neo4j**, definidas por el estudiante.
- Deben ser distintas entre sí, útiles para el problema y demostrar uso de nodos, relaciones, propiedades, filtros y recorridos del grafo.
- Documentar cada consulta Cypher y explicar brevemente qué información obtiene.
- Durante la calificación se puede solicitar modificar o crear una consulta adicional sobre el modelo implementado.

## 11. Decisiones de diseño que deben justificarse

El enunciado delega al estudiante, entre otras, las siguientes decisiones:

- Stack de frontend, backend, bibliotecas de gráficas y visualización de grafos.
- Arquitectura alternativa, si no se usa la referencia de frontend → backend/API → bases de datos.
- Esquema documental, colecciones e índices de MongoDB.
- Modelo de Neo4j: etiquetas, propiedades, relaciones, restricciones e índices.
- Reglas de limpieza, normalización, conversión y exclusión/conservación de datos.
- Estrategia para evitar duplicados e idempotencia de cargas.
- Filtros del explorador de grafos y criterios de búsqueda.
- Visualización apropiada para cada consulta.

Estas decisiones deben quedar documentadas y ser explicables, modificables y reproducibles durante la evaluación.

## 12. Reglas académicas y de evaluación

- El proyecto es individual.
- Debe desarrollarse y configurarse una solución propia: frontend, backend, conexiones y proceso de carga/transformación/validación.
- Los modelos documental y de grafo los define el estudiante.
- Todas las consultas solicitadas deben ejecutarse y visualizarse desde la aplicación.
- Se debe usar Git y GitHub; el repositorio necesita al menos **5 commits significativos** durante el desarrollo.
- Se permite software libre, cualquier IDE/editor, cliente de base de datos o herramienta de desarrollo necesaria.
- Se permite IA como apoyo solo si el estudiante comprende, revisa y valida el contenido generado.
- Copiar trabajo ajeno o soluciones completas sin comprender, revisar, adaptar y justificar está prohibido. El uso inadecuado de IA tiene una penalización del 50 % de la nota; copias totales o parciales obtienen cero y se notifican a coordinación.
- En la calificación se puede pedir explicar, modificar o reproducir código, consultas, configuraciones y procedimientos, así como crear/modificar consultas MongoDB o Neo4j.
- Se requiere obtener como mínimo el 5 % de la calificación total del proyecto para tener derecho a las siguientes actividades o proyectos del curso.
- Entregas tardías están sujetas a las políticas de penalización del curso.

## 13. Entregables del repositorio

El proyecto debe entregarse en un repositorio de GitHub que incluya como mínimo:

- Código fuente del frontend.
- Código fuente del backend.
- Scripts de carga y transformación.
- Consultas MongoDB.
- Consultas Cypher de Neo4j.
- Análisis de resultados.
- Archivos de configuración necesarios para ejecutar el proyecto.
- Conclusiones.
- Manual técnico.
- Manual de usuario.

**Fecha de entrega:** viernes 30 de octubre de 2026.

**Calificación:** pendiente de definir en el enunciado.

## 14. Lista de verificación para elaborar el proyecto

- [ ] MongoDB y Neo4j configurados e integrados mediante backend/API.
- [ ] Frontend sin conexiones directas a las bases de datos.
- [ ] Carga masiva de uno o varios CSV desde la interfaz.
- [ ] ETL con validación, transformación, trazabilidad de válidos/rechazados y causas.
- [ ] Resumen visible por archivo: nombre, conteos, duración y estado.
- [ ] Datos procesados almacenados en MongoDB y representados en Neo4j sin duplicados innecesarios.
- [ ] Dashboard con las 20 consultas MongoDB implementadas y visualizadas.
- [ ] Colección auxiliar `resumen_carrera` creada.
- [ ] Buscador implementado.
- [ ] Explorador Neo4j con al menos un grafo y un filtro.
- [ ] Cinco o más consultas Cypher diferenciadas, documentadas y accesibles desde la aplicación.
- [ ] Manejo claro de errores en carga, procesamiento, conexiones, consultas y visualización.
- [ ] Documentación: arquitectura, configuración, modelos, ETL, reglas de calidad, consultas, decisiones y resultados.
- [ ] Análisis de resultados, conclusiones, manual técnico y manual de usuario.
- [ ] Repositorio GitHub con cinco o más commits significativos.
