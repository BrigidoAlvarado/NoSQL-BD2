// G07: Aspirantes que usaron más de una oportunidad en la misma materia y año de ingreso.
MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:EVALUO_MATERIA]->(m:Materia)
WITH a,
    m,
    e.anioIngreso AS anioIngreso,
    count(DISTINCT e.oportunidad) AS oportunidades
WHERE oportunidades > 1
RETURN anioIngreso,
    m.clave AS materia,
    count(a) AS aspirantesReintentaron,
    round(avg(oportunidades) * 100) / 100.0 AS promedioOportunidades
ORDER BY anioIngreso DESC,
    aspirantesReintentaron DESC,
    materia
