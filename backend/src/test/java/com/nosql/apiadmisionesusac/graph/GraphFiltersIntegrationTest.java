package com.nosql.apiadmisionesusac.graph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/** Solo lectura: verifica parámetros contra resultados reales, sin crear datos. */
@SpringBootTest
@EnabledIfSystemProperty(named = "filters.integration", matches = "true")
class GraphFiltersIntegrationTest {
    @Autowired GraphQueryService queries;
    @Test void filtraCategoriasSinCambiarLosConteosDelGrupo() {
        for (int index = 1; index <= 10; index++) {
            String id = "G%02d".formatted(index);
            var original = queries.execute(id);
            if (original.filas().isEmpty()) continue;
            String column = original.columnas().getFirst();
            Object value = original.filas().getFirst().get(column);
            var filtered = queries.execute(id, Map.of(column, String.valueOf(value)));
            assertFalse(filtered.filas().isEmpty());
            assertTrue(filtered.filas().stream().allMatch(row -> row.get(column).equals(value)));
            assertTrue(filtered.filas().stream().allMatch(original.filas()::contains));
        }
        assertTrue(queries.execute("G02", Map.of("materia", "' OR true //")).filas().isEmpty());
    }
}
