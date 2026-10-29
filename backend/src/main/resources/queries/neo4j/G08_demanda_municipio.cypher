// G08: Conecta la procedencia educativa con carreras conocidas para identificar concentraciones territoriales.
MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true})
MATCH (e)-[:TIENE_PROCEDENCIA]->(:ProcedenciaEducativa)-[:UBICADA_EN]->(u:Municipio)-[:PERTENECE_A]->(d:Departamento {conocido:true})
RETURN d.clave AS departamento,
    u.nombre AS municipio,
    c.clave AS carrera,
    count(DISTINCT a.id) AS aspirantes
ORDER BY aspirantes DESC,
    departamento,
    municipio,
    carrera
