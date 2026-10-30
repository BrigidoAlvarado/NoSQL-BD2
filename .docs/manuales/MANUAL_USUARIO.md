# Manual de usuario

## Carga y trazabilidad

1. Iniciar los componentes siguiendo el [manual técnico](MANUAL_TECNICO.md). Abrir `http://localhost:4200` y comprobar API, MongoDB y Neo4j en Estado de los servicios.
2. En Carga de datos seleccionar uno o varios CSV UTF-8 y pulsar Cargar y procesar. Esperar el resumen por archivo: filas leídas, procesadas, rechazadas, duración y estado. El resumen inmediato incluye hasta los primeros 100 rechazos.
3. Consultar Historial de cargas para revisar resultados guardados, incluso después de recargar la página. Los botones Rechazos y Válidos abren tablas con páginas de 20 registros; Anterior y Siguiente recorren los resultados. Actualizar historial regresa a las cargas.
4. En Rechazos revisar línea, código, causa y valores disponibles. En Válidos consultar evaluaciones guardadas en MongoDB y sus fuentes del archivo seleccionado. Varias filas válidas pueden corresponder a una sola evaluación distinta. La duración histórica no está disponible.
5. Si aparece `PENDIENTE_NEO4J`, recuperar Neo4j y reenviar exactamente el archivo. Una carga completa repetida con reglas vigentes reutiliza su resultado. Si fue procesada con reglas antiguas, volver a subirla permite aplicar las nuevas reglas sin duplicar evaluaciones. El historial tiene una entrada por contenido; los reintentos actualizan esa entrada.

## Estadísticas y búsqueda

En Estadísticas elegir Panorama, Aprobación, Carreras, Perfil y edades o Procedencia, y seleccionar el código Mxx. Análisis adicionales contiene M21–M25, una ampliación de las veinte consultas obligatorias. Cada selección muestra tabla y gráfica; las barras representan hasta ocho grupos por métrica y la tabla conserva las filas devueltas.

Los filtros de categorías solicitan al servidor un subconjunto de los registros y recalculan sus métricas. Por ejemplo, filtrar por resultado APROBADO produce 100 % en los grupos con evaluaciones. Limpiar elimina los filtros; los controles permanecen disponibles si no hay resultados o falla la solicitud.

En Búsqueda elegir criterio y escribir el término. Correlativo exige coincidencia exacta; los demás criterios admiten coincidencias parciales sin distinguir mayúsculas. Abrir el aspirante para ver M11/M15: historial y datos de sus evaluaciones. Tipo de institución significa público/privado u otra categoría declarada; el origen no identifica colegios por nombre.

## Grafos y análisis Cypher

En Explorar grafo aplicar los filtros opcionales de año, carrera, resultado y departamento. Elegir una muestra entre 5 y 40 evaluaciones; el tamaño de la muestra no equivale al número de nodos. Usar zoom, desplazamiento y Ajustar vista; seleccionar una entidad en el grafo o en el selector para consultar su detalle. La selección resalta vecinos directos y atenúa el resto. Los controles por tipo ocultan entidades y enlaces incidentes de la muestra recibida.

Carreras, Trayectorias, Resultados y Procedencia ofrecen un selector Gxx con una sola consulta visible. Cada consulta tiene tabla, barras, carga, error y reintento independientes. La selección queda en la URL, por ejemplo `/grafos?seccion=procedencia&consulta=G08`. Los filtros de estos análisis seleccionan grupos calculados del conjunto completo y conservan sus conteos; son independientes de la muestra visual. G07 y G10 separan gráficas con unidades diferentes.

## Interpretación y recuperación

Una aprobación corresponde a una evaluación y no demuestra admisión universitaria. Una persona puede pertenecer a varios grupos: sumar aspirantes entre carreras puede contar a alguien varias veces. Los porcentajes de aprobación usan evaluaciones como denominador; otras métricas, como la recuperación de M22, usan personas según la definición del [catálogo](CONSULTAS.md). La edad es aproximada al año de ingreso y excluye edades ausentes. Mes corresponde a asignación, no necesariamente a realización del examen. Sexo `SIN REGISTRO` se conserva como categoría desconocida.

Ante un error de conexión comprobar los servicios y pulsar Reintentar. Si un archivo está vacío, excede los límites o tiene formato inválido, corregirlo y volver a seleccionarlo. No borrar volúmenes para resolver una conexión. La navegación permanece disponible aunque una consulta falle.

## Preparación de la demostración

Mostrar carga múltiple, resúmenes y rechazos, repetir una carga, consultar las veinte consultas obligatorias MongoDB y diez Cypher, buscar un correlativo con varias evaluaciones y cambiar filtros del grafo. M21–M25 complementan la demostración. Comparar totales antes y después de reiniciar los servicios. Practicar explicar hashes, `upsert`, `MERGE`, denominadores y desconocidos.

Las verificaciones anteriores y sus límites están en [VERIFICACION_ENTREGA.md](../evidencias/VERIFICACION_ENTREGA.md). La revisión documental del 30 de octubre no constituye una nueva demostración ni prueba de caídas reales. La comprensión y la entrega deben ser confirmadas por el estudiante.
