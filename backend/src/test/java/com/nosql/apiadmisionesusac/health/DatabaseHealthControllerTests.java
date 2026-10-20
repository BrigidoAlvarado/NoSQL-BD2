package com.nosql.apiadmisionesusac.health;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DatabaseHealthControllerTests {
    @Test
    void respondsWithJsonAndTheAppropriateHttpStatus() throws Exception {
        var health = mock(DatabaseHealthService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new DatabaseHealthController(health)).build();
        when(health.check()).thenReturn(new DatabaseHealthService.ConnectionStatus("UP", "UP", "UP"));
        mvc.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.mongodb").value("UP")).andExpect(jsonPath("$.neo4j").value("UP"));
        when(health.check()).thenReturn(new DatabaseHealthService.ConnectionStatus("DOWN", "UP", "DOWN"));
        mvc.perform(get("/api/health")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN")).andExpect(jsonPath("$.neo4j").value("DOWN"));
    }
}
