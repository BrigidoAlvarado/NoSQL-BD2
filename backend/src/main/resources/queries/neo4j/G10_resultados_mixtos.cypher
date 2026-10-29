// G10: Aspirantes que registran al menos una evaluación aprobada y otra no aprobada para la misma carrera.
MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true})
MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado)
WITH a,
    c,
    collect(DISTINCT r.codigo) AS resultados,
    count(DISTINCT e.id) AS evaluaciones
WHERE 'APROBADO' IN resultados AND 'NO APROBADO' IN resultados
RETURN c.clave AS carrera,
    count(DISTINCT a.id) AS aspirantes,
    sum(evaluaciones) AS evaluaciones
ORDER BY aspirantes DESC,
    carrera
