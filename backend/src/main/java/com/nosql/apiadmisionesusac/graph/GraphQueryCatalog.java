package com.nosql.apiadmisionesusac.graph;

import java.util.List;

/** Registro de títulos y recursos. La consulta se modifica en el archivo .cypher. */
public final class GraphQueryCatalog {
    private GraphQueryCatalog() {}

    public record Query(String id, String title, String description, String resource) {}

    public static final List<Query> QUERIES =
            List.of(
                    new Query(
                            "G01",
                            "Demanda por carrera",
                            "Evaluaciones vinculadas a cada carrera conocida.",
                            "queries/neo4j/G01_demanda_carrera.cypher"),
                    new Query(
                            "G02",
                            "Resultados por materia",
                            "Recorrido evaluación–materia–resultado.",
                            "queries/neo4j/G02_resultados_materia.cypher"),
                    new Query(
                            "G03",
                            "Aprobados por departamento y carrera",
                            "Aspirantes aprobados conectados con la procedencia educativa.",
                            "queries/neo4j/G03_aprobados_departamento.cypher"),
                    new Query(
                            "G04",
                            "Cambios de carrera",
                            "Aspirantes que aparecen vinculados con dos carreras objetivo"
                                    + " distintas.",
                            "queries/neo4j/G04_carreras_compartidas.cypher"),
                    new Query(
                            "G05",
                            "Cohortes, materias y resultado",
                            "Distribución de evaluaciones por cohorte, carrera, materia y"
                                    + " resultado.",
                            "queries/neo4j/G05_resultados_cohorte.cypher"),
                    new Query(
                            "G06",
                            "Resultados por tipo de institución",
                            "Recorre procedencia, tipo de institución, materia y resultado para"
                                    + " comparar evaluaciones.",
                            "queries/neo4j/G06_resultados_institucion.cypher"),
                    new Query(
                            "G07",
                            "Reintentos por materia y cohorte",
                            "Aspirantes que usaron más de una oportunidad en la misma materia y año"
                                    + " de ingreso.",
                            "queries/neo4j/G07_reintentos_materia.cypher"),
                    new Query(
                            "G08",
                            "Demanda por municipio y carrera",
                            "Conecta la procedencia educativa con carreras conocidas para"
                                    + " identificar concentraciones territoriales.",
                            "queries/neo4j/G08_demanda_municipio.cypher"),
                    new Query(
                            "G09",
                            "Resultados por sexo, carrera y materia",
                            "Compara los resultados de evaluación a través de las relaciones con"
                                    + " aspirante, carrera y materia.",
                            "queries/neo4j/G09_resultados_sexo.cypher"),
                    new Query(
                            "G10",
                            "Aspirantes con resultados mixtos",
                            "Aspirantes que registran al menos una evaluación aprobada y otra no"
                                    + " aprobada para la misma carrera.",
                            "queries/neo4j/G10_resultados_mixtos.cypher"));

    public static Query find(String id) {
        return QUERIES.stream()
                .filter(query -> query.id().equals(id))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException("Consulta de grafo no reconocida."));
    }
}
