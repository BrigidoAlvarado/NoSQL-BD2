// G04: Aspirantes que aparecen vinculados con dos carreras objetivo distintas.
MATCH (a:Aspirante)-[:REALIZO]->(:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true})
WITH a,
    collect(DISTINCT c.clave) AS carreras
UNWIND carreras AS c1
UNWIND carreras AS c2
WITH a,c1,c2
WHERE c1 < c2
RETURN c1 AS carreraOrigen,
    c2 AS carreraDestino,
    count(DISTINCT a.id) AS aspirantes
ORDER BY aspirantes DESC,
    carreraOrigen,
    carreraDestino
