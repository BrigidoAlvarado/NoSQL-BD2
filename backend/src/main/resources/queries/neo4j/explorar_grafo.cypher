MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera)
MATCH (e)-[:EVALUO_MATERIA]->(m:Materia)
MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado)
MATCH (e)-[:TIENE_PROCEDENCIA]->(p:ProcedenciaEducativa)-[:UBICADA_EN]->(u:Municipio)-[:PERTENECE_A]->(d:Departamento)
WHERE ($anio IS NULL OR e.anioIngreso = $anio)
  AND ($carrera IS NULL OR c.clave = $carrera)
  AND ($resultado IS NULL OR r.codigo = $resultado)
  AND ($departamento IS NULL OR d.clave = $departamento)
WITH a,e,c,m,r,p,u,d ORDER BY e.fechaAsignacion DESC LIMIT $limite
RETURN a,e,c,m,r,p,u,d
