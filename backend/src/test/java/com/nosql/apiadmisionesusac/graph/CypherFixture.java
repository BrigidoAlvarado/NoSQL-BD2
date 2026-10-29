package com.nosql.apiadmisionesusac.graph;

import org.neo4j.driver.Driver;

import java.util.Map;

/** Datos temporales identificados por una marca única; no modifica nodos del usuario. */
final class CypherFixture {
    private CypherFixture() {}

    static void create(Driver driver, String marker) {
        String cypher =
                """
CREATE (m:Materia {clave:$marker, refactorFixture:$marker})
CREATE (c1:Carrera {clave:$marker + ' A', conocida:true, refactorFixture:$marker})
CREATE (c2:Carrera {clave:$marker + ' B', conocida:true, refactorFixture:$marker})
CREATE (d:Departamento {clave:$marker, conocido:true, refactorFixture:$marker})
CREATE (u:Municipio {clave:$marker, nombre:$marker, refactorFixture:$marker})
CREATE (t:TipoInstitucion {codigo:$marker, refactorFixture:$marker})
CREATE (p:ProcedenciaEducativa {clave:$marker, tipo:$marker, municipio:$marker, refactorFixture:$marker})
CREATE (p)-[:ES_DE_TIPO]->(t)
CREATE (p)-[:UBICADA_EN]->(u)-[:PERTENECE_A]->(d)
WITH m,c1,c2,p
UNWIND [
  {id:1, persona:'a1', oportunidad:1, resultado:'NO APROBADO', carrera:'A'},
  {id:2, persona:'a1', oportunidad:2, resultado:'APROBADO', carrera:'A'},
  {id:3, persona:'a1', oportunidad:1, resultado:'APROBADO', carrera:'B'},
  {id:4, persona:'a2', oportunidad:1, resultado:'NO APROBADO', carrera:'A'}
] AS row
MERGE (a:Aspirante {id:$marker + row.persona})
ON CREATE SET a.refactorFixture=$marker
MERGE (r:Resultado {codigo:row.resultado})
ON CREATE SET r.refactorFixture=$marker
CREATE (e:Evaluacion {id:$marker + toString(row.id), refactorFixture:$marker,
  anioIngreso:2022, oportunidad:row.oportunidad, sexo:'F', aprobacion:row.resultado,
  fechaAsignacion:date('2021-08-01')})
CREATE (a)-[:REALIZO]->(e)
CREATE (e)-[:EVALUO_MATERIA]->(m)
CREATE (e)-[:OBTUVO_RESULTADO]->(r)
CREATE (e)-[:TIENE_PROCEDENCIA]->(p)
WITH e, row, CASE row.carrera WHEN 'A' THEN c1 ELSE c2 END AS c
CREATE (e)-[:PARA_CARRERA]->(c)
""";
        try (var session = driver.session()) {
            session.executeWrite(
                    tx -> {
                        tx.run(cypher, Map.of("marker", marker)).consume();
                        return null;
                    });
        }
    }

    static void delete(Driver driver, String marker) {
        try (var session = driver.session()) {
            session.executeWrite(
                    tx -> {
                        tx.run(
                                        "MATCH (n {refactorFixture:$marker}) DETACH DELETE n",
                                        Map.of("marker", marker))
                                .consume();
                        return null;
                    });
        }
    }
}
