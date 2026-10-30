# Explorador de grafos y consultas Cypher

Alcance: C07-01 a C07-10. Angular consume exclusivamente `/api/grafos`; Neo4j solo recibe consultas desde Spring Boot.

## Visualización y filtros

La ruta `/grafos` dibuja un subgrafo en canvas mediante `vis-network`, con zoom, desplazamiento, selección, detalle y controles por tipo; véase [BIBLIOTECA_GRAFOS.md](BIBLIOTECA_GRAFOS.md). Cada petición está limitada a 40 evaluaciones para mantener legibilidad. Los filtros opcionales son año de ingreso, carrera, resultado y departamento; modifican directamente la consulta Cypher y por tanto los nodos y aristas devueltos. Se muestran aspirantes, evaluaciones, carreras, materias, resultados, procedencia, municipios y departamentos, junto con las relaciones del modelo.

`GET /api/grafos?anio=&carrera=&resultado=&departamento=&limite=` es la API del subgrafo. Si no hay coincidencias, devuelve arreglos vacíos y la interfaz muestra un estado claro.

Los filtros analíticos de G01–G10 son parámetros HTTP sobre categorías permitidas por ID. Seleccionan grupos calculados sin recalcular conteos y son independientes de los filtros del subgrafo. Véase [FILTROS_ANALITICOS.md](FILTROS_ANALITICOS.md).

## Consultas

| ID | Consulta y recorrido | Utilidad |
| --- | --- | --- |
| G01 | `Carrera ← PARA_CARRERA ← Evaluacion` | Demanda de evaluaciones por carrera conocida. |
| G02 | `Materia ← EVALUO_MATERIA ← Evaluacion → OBTUVO_RESULTADO → Resultado` | Distribución de aprobación por materia. |
| G03 | `Aspirante → Evaluacion → Procedencia → Municipio → Departamento` y carrera | Aspirantes aprobados por departamento de procedencia y carrera. |
| G04 | Aspirante con carreras distintas, reducidas a pares únicos | Cambios o diversidad de carrera objetivo observada. |
| G05 | Evaluacion conectada con carrera, materia y resultado, agrupada por cohorte | Comparación de resultados dentro de cada año de ingreso. |
| G06 | Evaluacion → ProcedenciaEducativa → TipoInstitucion, materia y resultado | Compara resultados por tipo de institución educativa y materia. |
| G07 | Aspirante → Evaluacion → Materia, agrupado por cohorte | Identifica aspirantes que reintentaron una materia y el promedio de oportunidades usadas. |
| G08 | Aspirante → Evaluacion → Procedencia → Municipio → Departamento y carrera | Ubica la demanda de carreras por municipio de procedencia. |
| G09 | Aspirante → Evaluacion → Carrera, Materia y Resultado | Compara evaluaciones por sexo, carrera, materia y resultado. |
| G10 | Aspirante con evaluaciones aprobadas y no aprobadas para una carrera | Identifica trayectorias con resultados mixtos dentro de la misma carrera. |

Las consultas se exponen en `GET /api/grafos/consultas/G01` a `G10` y la misma pantalla permite seleccionarlas por tema. G03 y G08 excluyen categorías geográficas desconocidas; G01, G04, G09 y G10 excluyen carreras no conocidas. G04 primero agrupa carreras distintas por aspirante y después forma pares, para evitar una combinación de todas las evaluaciones que degradaría el rendimiento. G07 cuenta oportunidades distintas, por aspirante, materia y cohorte, para no confundir varias filas del mismo intento con reintentos.

## Verificación

`GraphExplorerIntegrationTest` ejecuta una exploración y G01–G10 contra Neo4j local. `graph.spec.ts` comprueba que Angular solicita el grafo, transmite el filtro de año, carga las diez consultas por tema y muestra un resultado vacío. La exploración real realizada el 23 de octubre de 2026 devolvió nodos, relaciones y las cinco consultas originales sin error; G06–G10 se ejecutaron posteriormente contra Neo4j según la evidencia del 29 de octubre en [VERIFICACION_ENTREGA.md](../evidencias/VERIFICACION_ENTREGA.md). La revisión documental actual no repite esas pruebas.


## Selección individual de consultas por sección

Las secciones Carreras, Trayectorias, Resultados y Procedencia ofrecen un selector accesible con código Gxx y nombre completo. Se muestra una sola tabla; Angular solicita únicamente la consulta seleccionada y conserva sus resultados al regresar. El código también aparece en el encabezado de la tabla. La selección se guarda en `consulta` de la URL, por ejemplo `/grafos?seccion=procedencia&consulta=G08`. Un código ajeno a la sección utiliza la primera consulta válida. Cada consulta conserva sus propios estados de carga, error y reintento; una respuesta tardía se guarda sin sustituir la tabla seleccionada. Explorar grafo mantiene su funcionamiento independiente.

Verificación: `graph.spec.ts` cubre las diez opciones, una tabla visible, selección mediante el control, URL, caché, enlace directo G08, código de otra sección, reintento y respuestas tardías. Suite completa: 37 pruebas Angular correctas y compilación de producción correcta.


## Estilo compartido de selectores

Los selectores simples usan estilo global en `frontend/src/styles.css`: altura mínima de 48 px, flecha decorativa, borde y fondo consistentes, foco visible, hover y estado deshabilitado. Se conserva el control nativo y su navegación de teclado. Incluye adaptación para colores forzados y movimiento reducido. Se retiró el estilo duplicado del selector en dashboard.css.

La tarjeta de selección se organiza en fila en escritorio y columna en móvil. El sidebar deja de ser sticky en pantallas pequeñas para evitar superponerse a los controles. Verificado en Chromium a 1440 y 390 px: sin desbordamiento horizontal y controles de al menos 48 px. Compilación de producción correcta.


## Gráficas asociadas a consultas Cypher

G01–G10 muestran una lectura visual de barras junto a su resultado tabular. El componente `graph-query-chart` reutiliza el dibujo de barras existente; `graph.charts.ts` declara categorías y métricas por consulta. No añade dependencias ni solicitudes HTTP. Tabla y gráfica reciben la misma respuesta del servidor, de modo que los filtros y las actualizaciones se reflejan en ambas.

- G01/G02/G05/G06/G09: evaluaciones por las categorías completas de la fila.
- G03/G04/G08: aspirantes por departamento/carrera, par de carreras o municipio/carrera.
- G07: gráficas separadas para aspirantes con reintento y promedio de oportunidades.
- G10: gráficas separadas para aspirantes y evaluaciones.

Las barras se ordenan de mayor a menor y muestran hasta ocho grupos por métrica; la tabla conserva todas las filas. Las categorías no se fusionan ni se suman entre sí. No se usan pasteles ni porcentajes porque algunas agrupaciones de personas se superponen. Los valores cero se conservan y la ausencia de datos se indica explícitamente.

Verificación: 42 pruebas Angular correctas y compilación de producción correcta. Incluye cobertura del catálogo G01–G10, unidades separadas, decimales, ceros y actualización del componente al cambiar a una respuesta filtrada o vacía.
