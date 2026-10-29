package com.nosql.apiadmisionesusac.graph;

import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Lee consultas del classpath, también cuando la aplicación se ejecuta como JAR. */
final class CypherResources {
    private CypherResources() {}

    static String read(String resource) {
        try {
            return new ClassPathResource(resource).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo leer la consulta " + resource, exception);
        }
    }
}
