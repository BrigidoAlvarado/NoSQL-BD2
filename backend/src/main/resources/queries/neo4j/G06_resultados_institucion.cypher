// G06: Recorre procedencia, tipo de institución, materia y resultado para comparar evaluaciones.
MATCH (e:Evaluacion)-[:TIENE_PROCEDENCIA]->(p:ProcedenciaEducativa)-[:ES_DE_TIPO]->(t:TipoInstitucion)
MATCH (e)-[:EVALUO_MATERIA]->(m:Materia)
MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado)
RETURN t.codigo AS tipoInstitucion,
    m.clave AS materia,
    r.codigo AS resultado,
    count(e) AS evaluaciones
ORDER BY tipoInstitucion,
    materia,
    resultado
