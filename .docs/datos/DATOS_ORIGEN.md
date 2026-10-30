# Datos de origen y comprensión del problema

Alcance: C02-01 a C02-08, B01.01 y decisiones de métricas adelantadas de B01.04. El usuario confirmó 2015, 2022 y 2024 como conjunto inicial; indicó que se pueden obtener más archivos si hacen falta. Se conserva la documentación localmente en `.docs/`, fuera de Git.

## Procedencia y cobertura

Equivalencia de presentación confirmada por el usuario el 29 de octubre de 2026: `INGENIERIA EN CIENCIAS Y ` (CSV 2022/2024) se muestra como `INGENIERIA EN CIENCIAS Y SISTEMAS`. También se admite la variante sin el espacio final en la presentación. La regla está centralizada en `frontend/src/app/shared/career-name.pipe.ts`; no modifica CSV, documentos, nodos, claves, filtros ni agrupaciones. Las categorías originales permanecen separadas y trazables; no es una normalización general de otras etiquetas truncadas. Verificación: 26 pruebas Angular correctas y build de producción correcto.

Fuente: [Datos Abiertos de Ingeniería USAC — Pruebas específicas](https://datosabiertos.ingenieria.usac.edu.gt/en/dataset/pruebas-especificas). El catálogo contiene recursos de 2015 a 2024. No se requiere añadir todos para revisar los datos; las conclusiones se limitarán a los años cargados. El PDF del proyecto no enumera los años asignados; no se encontró contradicción con el brief. La selección de estos tres archivos queda respaldada por la confirmación del usuario.

Los archivos están obtenidos en `.data/`. Las URL oficiales de cada recurso y las huellas locales están en `PROCEDENCIA_CSV.json`. Se identificaron los recursos en el catálogo oficial, pero la descarga directa durante esta verificación respondió HTTP 500. Por ello no se afirma igualdad byte a byte con el archivo actualmente alojado en el portal ni se certifica una fecha histórica de descarga que no consta.

## Inventario comprobado

| Archivo | Filas sin encabezado | Correlativos distintos | Fechas de asignación | Bytes |
| --- | ---: | ---: | --- | ---: |
| `pruebas_especificas_2015.csv` | 20277 | 6148 | 2014-08-23 a 2015-01-14 | 2777758 |
| `pruebas_especificas_2022.csv` | 24237 | 6380 | 2021-07-26 a 2021-11-22 | 3043257 |
| `pruebas_especificas_2024.csv` | 13863 | 3861 | 2023-08-01 a 2024-01-15 | 1739504 |

Los tres se decodifican estrictamente como UTF-8 sin BOM, usan coma como delimitador y LF como salto de línea. Todas las filas tienen 12 columnas. Los encabezados no se cuentan como evaluaciones. El año del archivo corresponde a `anio_de_ingreso`, no necesariamente al año de asignación: hay asignaciones del año anterior.

| Archivo | SHA-256 local |
| --- | --- |
| `pruebas_especificas_2015.csv` | `b35a6e65432dc910e1e5712285959603d27fec939944d2b2939f54166821e671` |
| `pruebas_especificas_2022.csv` | `49b111c3733bbc00b0d86a99ea4ffd44e20d72260d4dbba48114c3085bef379f` |
| `pruebas_especificas_2024.csv` | `c33873a374b7c28cc479359bb6f58199a6419fdddf636fc160ff216af9335dad` |

## Doce campos y nombres canónicos

La correspondencia se hace por encabezado, nunca por posición. En 2015 el correlativo es la última columna; en 2022 y 2024 es la segunda. Los tres alias abreviados son explícitos; no se aceptan nombres arbitrarios por semejanza.

| Campo canónico | Interpretación y tratamiento previsto |
| --- | --- |
| `fecha_asignacion` | Fecha de asignación, formato ISO `AAAA-MM-DD`; no es una fecha comprobada de realización de examen. |
| `sexo` | Categorías observadas inicialmente `F` o `M`; conservar lo declarado. El ETL también admite `SIN REGISTRO` como desconocido, sin inferir sexo. |
| `anio_nacimiento` | Año o `SIN REGISTRO`; mantener dato original y edad derivada nullable. |
| `materia` | Materia evaluada; conservar `MATEMATICA ITUGS` como categoría distinta. |
| `numero_de_fecha_de_evaluacion` | Oportunidad del calendario del año de ingreso, entero positivo; alias `numero_de_fecha_de_evaluaci`. |
| `anio_de_ingreso` | Cohorte de ingreso solicitada, entero de cuatro cifras. |
| `aprobacion` | Resultado de esta evaluación: `APROBADO` o `NO APROBADO`; no implica admisión ni aprobación de todas las materias. |
| `carrera_objetivo` | Carrera declarada en cada registro; puede cambiar para un mismo aspirante y contener nombres truncados. |
| `departamento_institucion_educativa` | Procedencia de la institución; alias `departamento_institucion_ed`. |
| `municipio_institucion_educativa` | Procedencia de la institución; alias `municipio_institucion_`. Conservar junto con departamento. |
| `tipo_institucion_educativa` | Tipo declarado, no nombre ni identificador de una institución concreta. |
| `correlativo_aspirante` | Identificador anónimo opaco y textual. No convertir a número, completar, recortar ni exigir longitud 32. |

El modelo no debe inventar un código o nombre de colegio: los CSV solo suministran tipo y ubicación. Tampoco debe interpretar la ubicación del colegio como domicilio del aspirante.

## Cardinalidad e identidad

Una fila representa una evaluación. Los 58 377 registros contienen 16 032 correlativos distintos y 58 362 registros distintos al comparar los doce campos. Hay 357 correlativos compartidos entre 2022 y 2024; 2015 no comparte correlativos exactos con esos dos años. La suma de aspirantes por año no es el total global. No se harán coincidencias aproximadas para unir identificadores diferentes.

En 2015 los identificadores tienen 32 caracteres hexadecimales; en 2022 y 2024 tienen entre 20 y 23. Se usa el texto completo suministrado como identidad, según el enunciado. La longitud no permite afirmar que sean un hash completo ni reconstruir su contenido original. No hay identificadores vacíos. Los cambios de carrera pertenecen a cada evaluación; no se reduce el historial a una única carrera del aspirante.

## Calidad observada

| Archivo | Duplicados exactos excedentes | Aspirantes con varias filas | SIN REGISTRO en nacimiento | SIN DATOS en institución |
| --- | ---: | ---: | ---: | ---: |
| `pruebas_especificas_2015.csv` | 3 | 5754 | 14192 | 24 |
| `pruebas_especificas_2022.csv` | 6 | 6047 | 73 | 786 |
| `pruebas_especificas_2024.csv` | 6 | 3692 | 15 | 485 |

- No se encontraron campos vacíos, fechas inválidas ni oportunidades no positivas en estos archivos. Esto describe la muestra analizada y no elimina las validaciones futuras.
- Hay 15 duplicados exactos excedentes (3, 6 y 6). Como decisión para el ETL, solo se deduplicarán coincidencias en los doce campos canónicos, conservando la trazabilidad de todas las filas. Una carga repetida no deberá aumentar el número de evaluaciones. Esta política aún no está implementada.
- En 2015 hay ocho claves `(correlativo, año, materia, oportunidad)` con fechas de asignación distintas. Por tanto, esa clave corta no sirve para eliminar registros: se conservarán las variantes, sin afirmar que representen dos intentos personales diferentes.
- `NINGUNO` aparece en 20 275 de las 20 277 filas de 2015. No representa una carrera real; es un valor desconocido que debe reportarse separadamente. Las otras dos filas declaran Ingeniería en Ciencias y Sistemas.
- En 2022 y 2024 hay nombres como `INGENIERIA EN CIENCIAS Y ` y `GUATEMALA - CIUDAD `. No se reconstruirán por suposición ni se fusionarán con un nombre completo de otro año sin un catálogo verificado.
- Coexisten `INGENIERIA MECANICA` y `INGENIERIA MECANICA `: 1 249 frente a 1 707 filas en 2022; 671 frente a 826 en 2024. Recortar espacios fusionaría categorías del origen que podrían corresponder a carreras distintas truncadas. Se preserva la distinción y se señala la calidad de la etiqueta en la visualización.
- También hay colisiones al recortar municipios (`SAN ANDRES` en 2022 y `SAN SEBASTIAN` en 2024). No aplicar trim indiscriminado a categorías. La identidad municipal debe incluir departamento y valor de origen.
- Se observaron 427 aspirantes con varias carreras en 2022 y 235 en 2024 (2 en 2015). El dato es contextual por evaluación, no una inconsistencia que justifique descartar su historial.
- Entre los correlativos comunes de 2022 y 2024, uno pasa de año de nacimiento desconocido a conocido; no hay años numéricos contradictorios. También cambian algunos datos de institución. Se conservarán por observación; no sobrescribir el historial con la última fila.

Los conteos completos de valores, espacios externos, longitudes, duplicados y variantes están en `PERFIL_CSV.json`. El perfil conserva las categorías originales y no expone listas de identificadores.

## Criterios de métricas — C02-07

Las siguientes son decisiones de diseño para implementar las consultas, no requisitos adicionales. Primero se filtra el período solicitado y se excluyen duplicados exactos. Cada consulta debe publicar su unidad, filtros y denominador. Los desconocidos se muestran como tales y nunca como cero.

- **Aspirante único:** un correlativo completo distinto dentro de cada grupo. Una persona puede pertenecer a varios grupos de carrera, institución o municipio por sus observaciones; sumar grupos no equivale al total global.
- **Evaluación:** un registro distinto por los doce campos. Se conservan fechas distintas de una misma oportunidad. El historial muestra ambas sin inventar un examen adicional.
- **Oportunidades usadas:** cantidad de números de oportunidad distintos por `(correlativo, año de ingreso, materia)`, no el máximo número observado ni el número de filas. Para M14 se promedia esa cantidad entre esos grupos que tienen evaluaciones. Una primera observación con número 3 no demuestra uso de las oportunidades 1 y 2.
- **Primer intento (M16):** se utiliza oportunidad calendario 1 y resultado `NO APROBADO`, con correlativos distintos por carrera. No se sustituye por la primera fila observada cuando falta la oportunidad 1; se indicará esta interpretación de primer intento.
- **Aspirante aprobado:** correlativo con al menos una evaluación aprobada dentro del grupo consultado. No equivale a aspirante admitido. No se descuentan fallos anteriores del historial.

| Consultas | Unidad y denominador acordados |
| --- | --- |
| M01, M12 | Correlativos distintos por tipo de institución o por sexo/tipo, respectivamente. `SIN DATOS` se mantiene como grupo explícito. |
| M02 | Evaluaciones con resultado `APROBADO` por materia. |
| M03, M08 | Correlativos distintos con alguna evaluación aprobada por carrera/año o municipio/carrera. En M08 municipio incluye departamento para no mezclar homónimos. |
| M04, M13, M18 | `100 × evaluaciones aprobadas / evaluaciones con resultado conocido` por materia, edad o tipo de institución. M13 solo usa edades válidas y publica cuántas evaluaciones quedaron sin edad. |
| M05, M07 | Promedio de una edad por correlativo dentro del grupo, sin ponderar por cantidad de exámenes. M07 restringe a observaciones aprobadas por carrera/tipo. La referencia de edad se define abajo. |
| M06 | `resumen_carrera` separará evaluaciones, correlativos distintos, aprobaciones y su porcentaje; no mezclará estas unidades. La colección se implementará en B04. |
| M09 | Conteo de evaluaciones de tipo `PUBLICO`, agrupadas por año-mes de asignación y materia. Es un indicador basado en fecha de asignación porque el origen no contiene fecha efectiva de examen. |
| M10 | Correlativos distintos por carrera cuya edad de referencia por aspirante/carrera esté entre 16 y 18 inclusive; excluir carreras desconocidas del top y reportarlas aparte. |
| M11, M15 | Historial y datos disponibles del correlativo, conservando todas las evaluaciones distintas y el contexto de cada una. |
| M16 | Correlativos distintos por carrera con evaluación no aprobada en oportunidad 1; no contar varias materias fallidas como varias personas. |
| M17 | Correlativos distintos por departamento/municipio; excluir municipios desconocidos del top y reportarlos aparte. |
| M19 | Correlativos distintos por departamento/carrera; todas las carreras empatadas en el máximo se muestran. Los desconocidos no compiten como carrera o departamento real. |
| M20 | Evaluaciones aprobadas y no aprobadas por año de ingreso; cada porcentaje usa todas las evaluaciones de ese año con resultado conocido. |

En porcentajes con denominador cero se devuelve valor ausente y se muestra «Sin datos», nunca 0 % inventado. Una categoría desconocida puede mantenerse en las distribuciones generales para conservar cobertura. Si una consulta filtra desconocidos, mostrará el conteo excluido. Los empates de top 5 se resuelven por etiqueta de origen y clave de grupo para producir cinco filas reproducibles; M19 conserva todos los empates por no pedir un top 5.

## Edad — C02-08

Se define **edad aproximada al año de ingreso = anio_de_ingreso − anio_nacimiento**. Se elige el año de ingreso por ser la cohorte del análisis. No se usa el año actual ni el de asignación. Al no tener fecha completa de nacimiento, no es una edad cumplida exacta.

Un nacimiento `SIN REGISTRO`, vacío, no convertible, futuro respecto de la cohorte o que produzca edad fuera del intervalo 0–100 genera edad ausente y una causa de calidad. El límite 100 es una decisión analítica conservadora, no un requisito del enunciado ni motivo para eliminar la evaluación. No se reemplaza por cero y se conserva el año original. Las edades entre 0 y 13 se señalarán para revisión; no hay casos en estos CSV. Si aparecen años numéricos contradictorios para un mismo correlativo en el conjunto, no se selecciona uno arbitrariamente: su edad será ausente hasta resolverlo.

En 2015 hay dos filas con nacimiento 1900, que darían 115 años: se conservan como evaluaciones, pero se excluyen del análisis de edad por esta regla. Tras ella hay 6 083 filas con edad válida en 2015 (15–43 años), 24 164 en 2022 (17–61) y 13 848 en 2024 (16–49). Estos son conteos de filas para diagnóstico, no denominadores de promedio por aspirante.

Para M05 se toma la primera cohorte observada de cada `(correlativo, carrera)` dentro del período filtrado y una sola edad de esa cohorte; para M07, la primera cohorte con evaluación aprobada de cada `(correlativo, carrera, tipo de institución)`. M10 usa la referencia de M05. Esto evita que varios intentos o la participación en varios años den más peso a la misma persona. Si en esa referencia la edad es desconocida, se excluye de la media y se informa, sin sustituirla por otra observación más conveniente. M13, en cambio, agrupa evaluaciones por la edad que corresponde a la cohorte de cada evaluación.

Los campos ausentes en la fila no se rellenarán silenciosamente con una fila de otra cohorte. La futura API deberá devolver tanto el número de personas con edad válida usado en cada media como el número sin edad; una media con cero personas válidas es ausente.

## Reproducción y alcance

```bash
python3 scripts/perfilar_csv.py .data/*.csv --salida .docs/datos/PERFIL_CSV.json
python3 -m unittest discover -s scripts -p 'test_*.py'
```

La herramienta no modifica los CSV ni carga bases. Sus seis pruebas cubren alias y orden, identidad textual, duplicados frente a oportunidades y fechas distintas, colisiones de espacios, edad, filas incompletas y errores de encabezado. La evidencia JSON se reproduce a partir de los mismos SHA-256. El ETL, los modelos y las consultas siguen pendientes: este trabajo completa la comprensión y las decisiones de la sección 2, no su implementación posterior.

Evidencia de cierre B01.01: commit `d7c29b8` en `feat/modelos-datos`; seis pruebas correctas, conteos contrastados mediante lectura independiente, perfil determinista y SHA-256 de los CSV sin cambios. La rama permanece abierta para los modelos B01.02 y B01.03.
