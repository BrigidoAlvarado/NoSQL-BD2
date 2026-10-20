package com.nosql.apiadmisionesusac.health;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

@Service
public class DatabaseHealthService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseHealthService.class);
    private final MongoTemplate mongo;
    private final Neo4jClient neo4j;
    private final String neo4jDatabase;

    public DatabaseHealthService(MongoTemplate mongo, Neo4jClient neo4j,
            @Value("${spring.data.neo4j.database}") String neo4jDatabase) {
        this.mongo = mongo;
        this.neo4j = neo4j;
        this.neo4jDatabase = neo4jDatabase;
    }

    public ConnectionStatus check() {
        boolean mongoConnected = checkMongo();
        boolean neo4jConnected = checkNeo4j();
        return new ConnectionStatus(status(mongoConnected && neo4jConnected),
                status(mongoConnected), status(neo4jConnected));
    }

    private boolean checkMongo() {
        try {
            return isOne(mongo.executeCommand(new Document("ping", 1)).get("ok"));
        } catch (RuntimeException exception) {
            log.warn("Comprobación de MongoDB fallida: {}", exception.getClass().getSimpleName());
            return false;
        }
    }

    private boolean checkNeo4j() {
        try {
            return neo4j.query("RETURN 1 AS connected").in(neo4jDatabase).fetch().one()
                    .map(result -> isOne(result.get("connected"))).orElse(false);
        } catch (RuntimeException exception) {
            log.warn("Comprobación de Neo4j fallida: {}", exception.getClass().getSimpleName());
            return false;
        }
    }

    private static boolean isOne(Object value) {
        return value instanceof Number number && number.doubleValue() == 1;
    }

    private static String status(boolean connected) {
        return connected ? "UP" : "DOWN";
    }

    public record ConnectionStatus(String status, String mongodb, String neo4j) {}
}
