// G05: Distribución de evaluaciones por cohorte, carrera, materia y resultado.
MATCH (e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera)
MATCH (e)-[:EVALUO_MATERIA]->(m:Materia)
MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado)
RETURN e.anioIngreso AS anioIngreso,
    c.clave AS carrera,
    m.clave AS materia,
    r.codigo AS resultado,
    count(e) AS evaluaciones
ORDER BY anioIngreso DESC,
    evaluaciones DESC
