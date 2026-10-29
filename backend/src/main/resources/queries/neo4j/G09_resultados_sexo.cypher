// G09: Compara los resultados de evaluación a través de las relaciones con aspirante, carrera y materia.
MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true})
MATCH (e)-[:EVALUO_MATERIA]->(m:Materia)
MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado)
RETURN e.sexo AS sexo,
    c.clave AS carrera,
    m.clave AS materia,
    r.codigo AS resultado,
    count(e) AS evaluaciones
ORDER BY evaluaciones DESC,
    sexo,
    carrera,
    materia
