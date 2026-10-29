package com.nosql.apiadmisionesusac.graph;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnabledIfSystemProperty(named = "graph.integration", matches = "true")
class GraphExplorerIntegrationTest {
    @Autowired private GraphExplorerService graphs;

    @Test
    void exploraUnSubgrafoYEjecutaLasDiezConsultas() {
        GraphResponse graph = graphs.explore(null, null, null, null, 10);
        assertTrue(graph.evaluacionesIncluidas() > 0);
        assertFalse(graph.nodos().isEmpty());
        assertFalse(graph.relaciones().isEmpty());
        for (String id : new String[] {"G01", "G02", "G03", "G04", "G05", "G06", "G07", "G08", "G09", "G10"}) {
            assertFalse(graphs.query(id).columnas().isEmpty());
        }
    }
}
