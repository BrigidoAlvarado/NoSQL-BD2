package com.nosql.apiadmisionesusac.health;

import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DatabaseHealthServiceTests {
    private final MongoTemplate mongo = mock(MongoTemplate.class);
    private final Neo4jClient neo4j = mock(Neo4jClient.class, RETURNS_DEEP_STUBS);
    private final DatabaseHealthService service = new DatabaseHealthService(mongo, neo4j, "neo4j");

    @Test
    void reportsBothAuthenticatedConnections() {
        when(mongo.executeCommand(new Document("ping", 1))).thenReturn(new Document("ok", 1.0));
        when(neo4j.query("RETURN 1 AS connected").in("neo4j").fetch().one())
                .thenReturn(Optional.of(Map.of("connected", 1L)));
        assertEquals(new DatabaseHealthService.ConnectionStatus("UP", "UP", "UP"), service.check());
    }

    @Test
    void stillChecksNeo4jWhenMongoFails() {
        when(mongo.executeCommand(new Document("ping", 1))).thenThrow(new IllegalStateException("sensitive detail"));
        when(neo4j.query("RETURN 1 AS connected").in("neo4j").fetch().one())
                .thenReturn(Optional.of(Map.of("connected", 1L)));
        assertEquals(new DatabaseHealthService.ConnectionStatus("DOWN", "DOWN", "UP"), service.check());
    }

    @Test
    void reportsNeo4jFailureIndependently() {
        when(mongo.executeCommand(new Document("ping", 1))).thenReturn(new Document("ok", 1));
        when(neo4j.query("RETURN 1 AS connected").in("neo4j").fetch().one())
                .thenThrow(new IllegalStateException("sensitive detail"));
        assertEquals(new DatabaseHealthService.ConnectionStatus("DOWN", "UP", "DOWN"), service.check());
    }

    @Test
    void rejectsMissingOrUnsuccessfulResults() {
        when(mongo.executeCommand(new Document("ping", 1))).thenReturn(new Document("ok", 0));
        when(neo4j.query("RETURN 1 AS connected").in("neo4j").fetch().one()).thenReturn(Optional.empty());
        assertEquals(new DatabaseHealthService.ConnectionStatus("DOWN", "DOWN", "DOWN"), service.check());
    }
}
