package com.nosql.apiadmisionesusac.statistics;

import static java.util.List.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class StatisticsControllerTests {
    @Test
    void exponeElDashboardMedianteLaApi() throws Exception {
        MongoStatisticsService service = mock(MongoStatisticsService.class);
        when(service.dashboard()).thenReturn(new DashboardResponse(
                new DashboardResponse.Summary(10, 7, 4, 40),
                of(new DashboardResponse.CountByLabel("PRIVADO", 5)),
                of(new DashboardResponse.CountByLabel("MATEMATICA", 4)),
                of(new DashboardResponse.RateByLabel("MATEMATICA", 4, 10, 40)),
                of(new DashboardResponse.CountByPair("F", "PRIVADO", 3)),
                of(new DashboardResponse.RateByLabel("PRIVADO", 4, 10, 40)),
                of(new DashboardResponse.ResultsByYear(2022, 4, 6, 10, 40)),
                of(), of(), of(), of(), of(), of(), of(), of(), of(), of(), of(), of()));
        var mvc = MockMvcBuilders.standaloneSetup(new StatisticsController(service)).build();

        mvc.perform(get("/api/estadisticas/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.aspirantesUnicos").value(7))
                .andExpect(jsonPath("$.aprobadasPorMateria[0].etiqueta").value("MATEMATICA"))
                .andExpect(jsonPath("$.resultadosPorAnio[0].anioIngreso").value(2022));
    }
}
