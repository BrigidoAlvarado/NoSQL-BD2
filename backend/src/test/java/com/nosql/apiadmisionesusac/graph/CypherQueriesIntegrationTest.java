package com.nosql.apiadmisionesusac.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.UUID;

@SpringBootTest
@EnabledIfSystemProperty(named = "graph.integration", matches = "true")
class CypherQueriesIntegrationTest {
    @Autowired Driver driver;
    @Autowired GraphQueryService queries;

    @Test
    void diezConsultasDevuelvenResultadosConocidos() {
        String marker = "REFACTOR_" + UUID.randomUUID();
        try {
            CypherFixture.create(driver, marker);
            assertEquals(3, number(row("G01", "carrera", marker + " A"), "evaluaciones"));
            assertEquals(2, number(row("G02", "materia", marker), "evaluaciones"));
            assertEquals(1, number(row("G03", "carrera", marker + " A"), "aspirantes"));
            assertEquals(1, number(row("G04", "carreraOrigen", marker + " A"), "aspirantes"));
            assertEquals(2, number(row("G05", "materia", marker), "evaluaciones"));
            assertEquals(2, number(row("G06", "tipoInstitucion", marker), "evaluaciones"));
            var retry = row("G07", "materia", marker);
            assertEquals(1, number(retry, "aspirantesReintentaron"));
            assertEquals(2.0, ((Number) retry.get("promedioOportunidades")).doubleValue());
            assertEquals(2, number(row("G08", "carrera", marker + " A"), "aspirantes"));
            assertEquals(2, number(row("G09", "carrera", marker + " A"), "evaluaciones"));
            var mixed = row("G10", "carrera", marker + " A");
            assertEquals(1, number(mixed, "aspirantes"));
            assertEquals(2, number(mixed, "evaluaciones"));
        } finally {
            CypherFixture.delete(driver, marker);
        }
    }

    private Map<String, Object> row(String query, String column, String value) {
        return queries.execute(query).filas().stream()
                .filter(row -> value.equals(row.get(column)))
                .findFirst()
                .orElseThrow();
    }

    private long number(Map<String, Object> row, String column) {
        return ((Number) row.get(column)).longValue();
    }
}
