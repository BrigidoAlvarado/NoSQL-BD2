// G03: Aspirantes aprobados conectados con la procedencia educativa.
MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion {aprobacion:'APROBADO'})-[:PARA_CARRERA]->(c:Carrera {conocida:true})
MATCH (e)-[:TIENE_PROCEDENCIA]->(:ProcedenciaEducativa)-[:UBICADA_EN]->(:Municipio)-[:PERTENECE_A]->(d:Departamento {conocido:true})
RETURN d.clave AS departamento,
    c.clave AS carrera,
    count(DISTINCT a.id) AS aspirantes
ORDER BY aspirantes DESC,
    departamento,
    carrera
