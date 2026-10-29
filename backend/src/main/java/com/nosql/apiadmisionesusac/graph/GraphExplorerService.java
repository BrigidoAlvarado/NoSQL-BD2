package com.nosql.apiadmisionesusac.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.types.Node;
import org.springframework.stereotype.Service;

@Service
public class GraphExplorerService {
    private static final String EXPLORER = """
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
            """;
    private final Driver driver;

    public GraphExplorerService(Driver driver) { this.driver = driver; }

    public GraphResponse explore(Integer anio, String carrera, String resultado, String departamento, int requestedLimit) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("anio", anio); parameters.put("carrera", blankToNull(carrera)); parameters.put("resultado", blankToNull(resultado));
        parameters.put("departamento", blankToNull(departamento)); parameters.put("limite", Math.clamp(requestedLimit, 1, 150));
        Map<String, GraphResponse.GraphNode> nodes = new LinkedHashMap<>();
        Set<GraphResponse.GraphEdge> edges = new LinkedHashSet<>();
        int[] count = {0};
        try (Session session = driver.session()) {
            session.executeRead(tx -> {
                for (Record record : tx.run(EXPLORER, parameters).list()) {
                    Node a = record.get("a").asNode(), e = record.get("e").asNode(), c = record.get("c").asNode();
                    Node m = record.get("m").asNode(), r = record.get("r").asNode(), p = record.get("p").asNode();
                    Node u = record.get("u").asNode(), d = record.get("d").asNode();
                    add(nodes, a); add(nodes, e); add(nodes, c); add(nodes, m); add(nodes, r); add(nodes, p); add(nodes, u); add(nodes, d);
                    edge(edges, a, e, "REALIZO"); edge(edges, e, c, "PARA_CARRERA"); edge(edges, e, m, "EVALUO_MATERIA");
                    edge(edges, e, r, "OBTUVO_RESULTADO"); edge(edges, e, p, "TIENE_PROCEDENCIA"); edge(edges, p, u, "UBICADA_EN"); edge(edges, u, d, "PERTENECE_A");
                    count[0]++;
                }
                return null;
            });
        }
        return new GraphResponse(List.copyOf(nodes.values()), List.copyOf(edges), count[0]);
    }

    public GraphQueryResponse query(String id) {
        return switch (id) {
            case "G01" -> tabular("G01", "Demanda por carrera", "Evaluaciones vinculadas a cada carrera conocida.",
                    List.of("carrera", "evaluaciones"), "MATCH (c:Carrera {conocida:true})<-[:PARA_CARRERA]-(e:Evaluacion) RETURN c.clave AS carrera, count(DISTINCT e.id) AS evaluaciones ORDER BY evaluaciones DESC, carrera", List.of());
            case "G02" -> tabular("G02", "Resultados por materia", "Recorrido evaluación–materia–resultado.",
                    List.of("materia", "resultado", "evaluaciones"), "MATCH (m:Materia)<-[:EVALUO_MATERIA]-(e:Evaluacion)-[:OBTUVO_RESULTADO]->(r:Resultado) RETURN m.clave AS materia, r.codigo AS resultado, count(e) AS evaluaciones ORDER BY materia, resultado", List.of());
            case "G03" -> tabular("G03", "Aprobados por departamento y carrera", "Aspirantes aprobados conectados con la procedencia educativa.",
                    List.of("departamento", "carrera", "aspirantes"), "MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion {aprobacion:'APROBADO'})-[:PARA_CARRERA]->(c:Carrera {conocida:true}) MATCH (e)-[:TIENE_PROCEDENCIA]->(:ProcedenciaEducativa)-[:UBICADA_EN]->(:Municipio)-[:PERTENECE_A]->(d:Departamento {conocido:true}) RETURN d.clave AS departamento, c.clave AS carrera, count(DISTINCT a.id) AS aspirantes ORDER BY aspirantes DESC, departamento, carrera", List.of());
            case "G04" -> tabular("G04", "Cambios de carrera", "Aspirantes que aparecen vinculados con dos carreras objetivo distintas.",
                    List.of("carreraOrigen", "carreraDestino", "aspirantes"), "MATCH (a:Aspirante)-[:REALIZO]->(:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true}) WITH a, collect(DISTINCT c.clave) AS carreras UNWIND carreras AS c1 UNWIND carreras AS c2 WITH a,c1,c2 WHERE c1 < c2 RETURN c1 AS carreraOrigen, c2 AS carreraDestino, count(DISTINCT a.id) AS aspirantes ORDER BY aspirantes DESC, carreraOrigen, carreraDestino", List.of());
            case "G05" -> tabular("G05", "Cohortes, materias y resultado", "Distribución de evaluaciones por cohorte, carrera, materia y resultado.",
                    List.of("anioIngreso", "carrera", "materia", "resultado", "evaluaciones"), "MATCH (e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera) MATCH (e)-[:EVALUO_MATERIA]->(m:Materia) MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado) RETURN e.anioIngreso AS anioIngreso, c.clave AS carrera, m.clave AS materia, r.codigo AS resultado, count(e) AS evaluaciones ORDER BY anioIngreso DESC, evaluaciones DESC", List.of());
            case "G06" -> tabular("G06", "Resultados por tipo de institución", "Recorre procedencia, tipo de institución, materia y resultado para comparar evaluaciones.",
                    List.of("tipoInstitucion", "materia", "resultado", "evaluaciones"), "MATCH (e:Evaluacion)-[:TIENE_PROCEDENCIA]->(p:ProcedenciaEducativa)-[:ES_DE_TIPO]->(t:TipoInstitucion) MATCH (e)-[:EVALUO_MATERIA]->(m:Materia) MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado) RETURN t.codigo AS tipoInstitucion, m.clave AS materia, r.codigo AS resultado, count(e) AS evaluaciones ORDER BY tipoInstitucion, materia, resultado", List.of());
            case "G07" -> tabular("G07", "Reintentos por materia y cohorte", "Aspirantes que usaron más de una oportunidad en la misma materia y año de ingreso.",
                    List.of("anioIngreso", "materia", "aspirantesReintentaron", "promedioOportunidades"), "MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:EVALUO_MATERIA]->(m:Materia) WITH a, m, e.anioIngreso AS anioIngreso, count(DISTINCT e.oportunidad) AS oportunidades WHERE oportunidades > 1 RETURN anioIngreso, m.clave AS materia, count(a) AS aspirantesReintentaron, round(avg(oportunidades) * 100) / 100.0 AS promedioOportunidades ORDER BY anioIngreso DESC, aspirantesReintentaron DESC, materia", List.of());
            case "G08" -> tabular("G08", "Demanda por municipio y carrera", "Conecta la procedencia educativa con carreras conocidas para identificar concentraciones territoriales.",
                    List.of("departamento", "municipio", "carrera", "aspirantes"), "MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true}) MATCH (e)-[:TIENE_PROCEDENCIA]->(:ProcedenciaEducativa)-[:UBICADA_EN]->(u:Municipio)-[:PERTENECE_A]->(d:Departamento {conocido:true}) RETURN d.clave AS departamento, u.nombre AS municipio, c.clave AS carrera, count(DISTINCT a.id) AS aspirantes ORDER BY aspirantes DESC, departamento, municipio, carrera", List.of());
            case "G09" -> tabular("G09", "Resultados por sexo, carrera y materia", "Compara los resultados de evaluación a través de las relaciones con aspirante, carrera y materia.",
                    List.of("sexo", "carrera", "materia", "resultado", "evaluaciones"), "MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true}) MATCH (e)-[:EVALUO_MATERIA]->(m:Materia) MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado) RETURN e.sexo AS sexo, c.clave AS carrera, m.clave AS materia, r.codigo AS resultado, count(e) AS evaluaciones ORDER BY evaluaciones DESC, sexo, carrera, materia", List.of());
            case "G10" -> tabular("G10", "Aspirantes con resultados mixtos", "Aspirantes que registran al menos una evaluación aprobada y otra no aprobada para la misma carrera.",
                    List.of("carrera", "aspirantes", "evaluaciones"), "MATCH (a:Aspirante)-[:REALIZO]->(e:Evaluacion)-[:PARA_CARRERA]->(c:Carrera {conocida:true}) MATCH (e)-[:OBTUVO_RESULTADO]->(r:Resultado) WITH a, c, collect(DISTINCT r.codigo) AS resultados, count(DISTINCT e.id) AS evaluaciones WHERE 'APROBADO' IN resultados AND 'NO APROBADO' IN resultados RETURN c.clave AS carrera, count(DISTINCT a.id) AS aspirantes, sum(evaluaciones) AS evaluaciones ORDER BY aspirantes DESC, carrera", List.of());
            default -> throw new IllegalArgumentException("Consulta de grafo no reconocida.");
        };
    }

    private GraphQueryResponse tabular(String id, String title, String description, List<String> columns, String cypher, List<Object> ignored) {
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Session session = driver.session()) {
            session.executeRead(tx -> {
                for (Record record : tx.run(cypher).list()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (String column : columns) row.put(column, record.get(column).asObject());
                    rows.add(row);
                }
                return null;
            });
        }
        return new GraphQueryResponse(id, title, description, columns, rows);
    }

    private void add(Map<String, GraphResponse.GraphNode> nodes, Node node) {
        String label = node.labels().iterator().next(); String id = nodeId(label, node);
        Map<String, Object> properties = new LinkedHashMap<>();
        for (String key : List.of("anioIngreso", "aprobacion", "sexo", "oportunidad", "conocida", "conocido", "nombre", "departamento", "municipio", "tipo")) if (node.containsKey(key)) properties.put(key, node.get(key).asObject());
        nodes.putIfAbsent(id, new GraphResponse.GraphNode(id, label, title(label, node), properties));
    }
    private void edge(Set<GraphResponse.GraphEdge> edges, Node from, Node to, String type) { edges.add(new GraphResponse.GraphEdge(nodeId(from.labels().iterator().next(), from), nodeId(to.labels().iterator().next(), to), type)); }
    private String nodeId(String label, Node node) { String key = node.containsKey("id") ? node.get("id").asString() : node.containsKey("clave") ? node.get("clave").asString() : node.get("codigo").asString(); return label + ":" + key; }
    private String title(String label, Node node) { return switch (label) { case "Aspirante", "Evaluacion" -> label + " " + node.get("id").asString().substring(0, Math.min(10, node.get("id").asString().length())); case "Municipio" -> node.get("nombre").asString(); case "ProcedenciaEducativa" -> node.get("tipo").asString() + " · " + node.get("municipio").asString(); default -> node.containsKey("clave") ? node.get("clave").asString() : node.get("codigo").asString(); }; }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value; }
}
