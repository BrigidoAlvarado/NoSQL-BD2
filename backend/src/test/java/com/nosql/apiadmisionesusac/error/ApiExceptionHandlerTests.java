package com.nosql.apiadmisionesusac.error;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mongodb.MongoException;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ApiExceptionHandlerTests {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void clasificaFallosDeMongoSinExponerElDetalleTecnico() {
        var response = handler.unexpected(new RuntimeException(new MongoException("credencial interna")));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("MONGODB_NO_DISPONIBLE", response.getBody().codigo());
        assertFalse(response.getBody().mensaje().contains("credencial interna"));
    }

    @Test
    void clasificaFallosDeNeo4jSinExponerElDetalleTecnico() {
        var response = handler.unexpected(new ServiceUnavailableException("bolt interno"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("NEO4J_NO_DISPONIBLE", response.getBody().codigo());
        assertFalse(response.getBody().mensaje().contains("bolt interno"));
    }

    @Test
    void conservaUnMensajeControladoParaSolicitudInvalida() {
        var response = handler.status(new ResponseStatusException(HttpStatus.NOT_FOUND, "La consulta no existe."));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("SOLICITUD_INVALIDA", response.getBody().codigo());
        assertEquals("La consulta no existe.", response.getBody().mensaje());
    }
}
