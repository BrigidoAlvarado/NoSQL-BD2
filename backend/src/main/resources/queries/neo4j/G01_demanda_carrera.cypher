// G01: Evaluaciones vinculadas a cada carrera conocida.
MATCH (c:Carrera {conocida:true})<-[:PARA_CARRERA]-(e:Evaluacion)
RETURN c.clave AS carrera,
    count(DISTINCT e.id) AS evaluaciones
ORDER BY evaluaciones DESC,
    carrera
