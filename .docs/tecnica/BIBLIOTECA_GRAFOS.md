# Biblioteca de visualización del grafo

Se integra `vis-network` 10.1.4 en Angular. El paquete declara licencia dual `Apache-2.0 OR MIT`; conservar sus avisos al distribuir la aplicación. La versión reproducible y sus dependencias están en `frontend/package-lock.json`; usar `npm ci` desde `frontend/` para instalar el proyecto.

Es una decisión de diseño para mejorar la legibilidad: el brief exige visualizar relaciones y ofrecer filtros, pero no impone esta biblioteca. La biblioteca dibuja los datos del API; no se conecta a Neo4j ni ejecuta consultas.

## Responsabilidades

- `features/graph/graph.ts` solicita opciones y muestras al backend y coordina filtros.
- `features/graph/components/graph-canvas.ts` importa `Network` desde `vis-network/standalone`, convierte nodos y relaciones de la respuesta en elementos visuales y gestiona la interacción.
- Neo4j, sus identidades y consultas permanecen en el backend. Las relaciones dibujadas provienen de la respuesta; no se infieren conexiones nuevas.

## Distribución e interacción

Se usa distribución por fuerzas Barnes–Hut, con estabilización inicial de 150 iteraciones, repulsión y separación de enlaces. Estos parámetros controlan posiciones visuales y no alteran los resultados analíticos. Permite zoom, desplazamiento y movimiento de nodos. «Ajustar vista» encuadra la muestra.

Las etiquetas iniciales corresponden a carreras, municipios y departamentos. El selector de entidades permite seleccionar por teclado cualquier nodo y abrir sus datos completos, incluidos los identificadores y propiedades recibidas. La selección resalta las relaciones incidentes y centra la entidad. «Quitar selección» limpia el detalle.

Al recibir una nueva muestra se destruye la instancia anterior y se crea otra. `ngOnDestroy()` libera la red al salir de la página. El tamaño solicitado se refiere a evaluaciones: la cantidad de nodos depende de las relaciones compartidas. La muestra no representa los totales del conjunto.

## Verificación y límites

Comprobar compilación Angular, navegación, cambio de muestra y liberación de recursos; revisar en navegador muestras pequeñas y de 40 evaluaciones. Las pruebas unitarias HTTP no sustituyen la inspección del canvas. Sigue pendiente documentar la evidencia visual final y comprobar ausencia de superposiciones en pantallas habituales. No se han implementado todavía filtros opcionales por tipo ni atenuación del conjunto de nodos no vecinos.

Referencia de la biblioteca: https://visjs.github.io/vis-network/docs/network/ . La instalación incluye declaraciones TypeScript que permiten comprobar la compatibilidad de las opciones mediante compilación.

## Cierre de interacción y verificación

La selección ahora conserva colores de la entidad y vecinos directos y atenúa el resto; sus enlaces incidentes aumentan de grosor. Al superar escala 1.5 se muestran las etiquetas completas. Los controles por tipo ocultan entidades y sus relaciones incidentes sin crear conexiones, y muestran nodos visibles frente a recibidos. La plantilla y estilos residen en archivos separados del componente.

Verificación real en Chromium a 1440 px: muestras de 5/10/40 evaluaciones devolvieron 21/33/98 nodos. Se comprobó selección y cierre del detalle y se capturaron imágenes de las tres muestras. El navegador no registró errores JavaScript. Las cinco consultas adicionales devolvieron filas y pudieron seleccionarse. Esta evidencia corresponde al conjunto local actual; la distribución puede variar con otras cargas.


## Corrección de la leyenda de tipos

`features/graph/graph-node-types.ts` define un nombre visible y un color exclusivo para cada una de las ocho entidades: Aspirante, Evaluación, Carrera, Materia/curso, Resultado, Procedencia educativa, Municipio y Departamento. Tanto los filtros como el motor gráfico consumen este catálogo; se eliminó el color verde compartido por defecto y la leyenda de solo cuatro colores.

Cada filtro muestra un indicador de su color. Desmarcar un tipo oculta únicamente sus nodos y todas las relaciones incidentes; los nodos de otros tipos conservan sus colores. Seleccionar un nodo atenúa en gris los ajenos a su vecindad y la interfaz explica ese estado. Los colores de selección y hover preservan el color de la entidad.
