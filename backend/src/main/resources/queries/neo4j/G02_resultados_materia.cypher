// G02: Recorrido evaluación–materia–resultado.
MATCH (m:Materia)<-[:EVALUO_MATERIA]-(e:Evaluacion)-[:OBTUVO_RESULTADO]->(r:Resultado)
RETURN m.clave AS materia,
    r.codigo AS resultado,
    count(e) AS evaluaciones
ORDER BY materia,
    resultado
