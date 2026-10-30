# Filtros analíticos mediante parámetros HTTP

El avance inicial de filtros de presentación quedó conservado en el commit `5a0cd95`. La implementación posterior envía los valores seleccionados al backend. No hay consultas diferentes para cada combinación.

## MongoDB

`GET /api/estadisticas/dashboard?materia=MATEMATICA&tipoInstitucion=PRIVADO`

`GET /api/estadisticas/adicionales/M21?anio=2024&materia=MATEMATICA`

`AnalysisFilters` valida una lista explícita de parámetros y convierte año, edad y oportunidad a enteros. Construye un documento de igualdad con valores literales. No acepta operadores MongoDB del cliente. Mes se compara con la fecha de asignación en formato AAAA-MM.

Cada Mxx conserva su archivo y su método sin filtros para compatibilidad. La sobrecarga recibe el documento de filtro y `QueryValues` antepone `$match` a la misma agregación. Los filtros se aplican **antes de agrupar**: recalculan métricas, denominadores y rankings del subconjunto. Por ejemplo, M04 filtrada por resultado APROBADO tiene 100 % en los grupos con datos. Esto no equivale a ocultar filas después del cálculo.

M06 solo materializa `resumen_carrera` sin filtros. Sus versiones filtradas son de lectura para no reemplazar el resumen general con cifras parciales. En M21–M25, filtros sobre oportunidades/resultados también restringen la evidencia disponible para las trayectorias: interpretar siempre el resultado como análisis del subconjunto elegido.

## Neo4j

`GET /api/grafos/consultas/G02?materia=MATEMATICA&resultado=APROBADO`

Se ejecuta la consulta Gxx dentro de una subconsulta Cypher y se filtran **sus grupos calculados** mediante `WHERE materia = $materia AND resultado = $resultado`. Las columnas admitidas se declaran por ID; los valores se pasan al driver en un mapa. No se concatena texto del usuario como Cypher. Los conteos originales del grupo se conservan. Esta elección permite filtrar también pares de carreras en G04 sin perder el recorrido que construye el par.

Los filtros analíticos no modifican los filtros independientes de la muestra visual del grafo.

## Angular y navegación

El componente compartido `CategoryFilters` presenta opciones de la respuesta inicial sin filtrar. Las páginas traducen sus selecciones a parámetros HTTP y muestran los resultados del servidor. Los valores originales se conservan como claves. Tabla y gráfica usan la misma respuesta. Limpiar elimina los parámetros, y las respuestas tardías no sustituyen los resultados actuales. Los filtros permanecen disponibles al recibir una respuesta vacía.

En estadísticas se declaran las categorías de cada Mxx en `dashboard.filters.ts`; en Cypher se usan las columnas categóricas devueltas, excluyendo métricas. M21–M25 ofrecen cohorte y, cuando corresponde, materia y oportunidad. Añadir una nueva combinación no requiere otro archivo de consulta.

## Verificación

- Angular: selección y limpieza producen solicitudes con parámetros para MongoDB y Neo4j; resultados vacíos conservan controles.
- Integración MongoDB: filtro por materia/resultado devuelve el total esperado, se calcula porcentaje 100 sobre el subconjunto, entradas semejantes a inyección son literales y operadores no permitidos se rechazan.
- Integración Neo4j: filtros por categoría en G01–G10 con datos, conservación de grupos originales y texto semejante a inyección sin coincidencias. Solo lectura de datos locales.
- Se mantienen las pruebas anteriores sin filtros para comprobar compatibilidad.


Ubicación en Estadísticas: selector de consulta → filtros de categorías → tabla y gráfica. Los controles permanecen disponibles ante un error de la solicitud filtrada para poder limpiar y reintentar. Verificación del ajuste: compilación Angular correcta.


Actualización visual: consulta y categorías se integran en una única tarjeta compacta. En escritorio se alinean en dos columnas; por debajo de 1000 px se apilan. El fieldset conserva su nombre accesible y los controles mantienen etiquetas visibles. La selección inicial del control refleja explícitamente la consulta de la URL. Verificación: 38 pruebas Angular y compilación de producción correctas.
