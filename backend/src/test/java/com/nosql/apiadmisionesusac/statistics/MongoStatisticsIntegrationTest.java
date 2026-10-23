package com.nosql.apiadmisionesusac.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Comprueba las seis agregaciones contra los CSV cargados en contenedores locales. */
@SpringBootTest
@EnabledIfSystemProperty(named = "statistics.integration", matches = "true")
class MongoStatisticsIntegrationTest {
    @Autowired
    private MongoStatisticsService statistics;

    @Test
    void resultadosSonConsistentesConLosTotalesCargados() {
        DashboardResponse response = statistics.dashboard();
        assertTrue(response.resumen().evaluaciones() > 0);
        assertTrue(response.resumen().aspirantesUnicos() > 0);
        assertFalse(response.aspirantesPorInstitucion().isEmpty());
        assertFalse(response.aprobadasPorMateria().isEmpty());
        assertEquals(response.resumen().aprobadas(), response.aprobadasPorMateria().stream()
                .mapToLong(DashboardResponse.CountByLabel::cantidad).sum());
        assertEquals(response.resumen().evaluaciones(), response.resultadosPorAnio().stream()
                .mapToLong(DashboardResponse.ResultsByYear::evaluaciones).sum());
        assertEquals(response.resumen().aprobadas(), response.resultadosPorAnio().stream()
                .mapToLong(DashboardResponse.ResultsByYear::aprobadas).sum());
        assertEquals(response.resumen().evaluaciones(), response.aprobacionPorInstitucion().stream()
                .mapToLong(DashboardResponse.RateByLabel::evaluaciones).sum());
    }
}
