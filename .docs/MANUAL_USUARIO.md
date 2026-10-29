# Manual de usuario

1. Iniciar los componentes siguiendo el manual técnico. Abrir `http://localhost:4200`; comprobar API, MongoDB y Neo4j en Estado de los servicios.
2. En Carga de datos seleccionar uno o varios CSV UTF-8 y pulsar Cargar y procesar. Esperar los resultados por archivo: filas leídas, procesadas, rechazadas, duración y estado. Consultar las causas de rechazo antes de corregir el archivo. Se muestran hasta los primeros 100 rechazos.
3. Si aparece PENDIENTE_NEO4J, recuperar Neo4j y reenviar exactamente el archivo. Una carga completa repetida reutiliza su resultado; no duplica evaluaciones.
4. En Estadísticas elegir Panorama, Aprobación, Carreras, Perfil y edades o Procedencia. Abrir el selector de consulta: se muestra una tabla y su gráfica. La tabla contiene las cifras completas. La gráfica actual muestra hasta ocho categorías, por lo que no debe interpretarse como ranking completo.
5. En Búsqueda elegir criterio y escribir el término. Correlativo exige coincidencia exacta; los demás criterios admiten coincidencias parciales sin distinguir mayúsculas. Abrir el aspirante para consultar todas sus evaluaciones. Tipo de institución significa público/privado u otra categoría declarada, no nombre de colegio.
6. En Explorar grafo introducir filtros opcionales y aplicarlos. La vista usa una muestra de hasta 40 evaluaciones. Limpiar elimina los filtros. Los análisis de Carreras, Trayectorias, Resultados y Procedencia consultan el conjunto completo y tienen actualización independiente.

## Interpretación

Una aprobación corresponde a una evaluación y no demuestra admisión universitaria. Una persona puede pertenecer a varios grupos: sumar aspirantes de carreras distintas puede contar a alguien varias veces. Los porcentajes de aprobación usan evaluaciones como denominador. La edad es aproximada al año de ingreso y excluye edades ausentes. Mes corresponde a asignación, no necesariamente a realización del examen.

## Recuperación

Ante un error de conexión comprobar los servicios y pulsar Reintentar. Si el archivo está vacío, excede los límites o tiene formato inválido, corregirlo y volver a seleccionarlo. No borrar volúmenes de las bases para resolver una conexión. La navegación permanece disponible aunque una consulta falle.

## Preparación de la demostración

Mostrar una carga múltiple y sus rechazos, repetir una carga, consultar las veinte consultas MongoDB y diez Cypher, buscar un correlativo con varias evaluaciones y cambiar un filtro del grafo. Comparar los totales antes y después de reiniciar los servicios. Practicar explicar el hash, `upsert`, `MERGE`, los denominadores y el tratamiento de desconocidos. La comprensión y la entrega deben ser confirmadas por el estudiante.
