package com.nosql.apiadmisionesusac.statistics.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.bson.Document;
import org.junit.jupiter.api.Test;

class AgeQuerySupportTest {
    @Test
    void devuelveElNombreDeCarreraYConservaLasMetricas() {
        Document result = new Document("_id", new Document("carrera", "INGENIERIA CIVIL"))
                .append("promedio", 21.44).append("conDato", 843).append("sinDato", 2);

        var row = AgeQuerySupport.averageLabel(result);

        assertEquals("INGENIERIA CIVIL", row.etiqueta());
        assertEquals(21.44, row.promedio());
        assertEquals(843, row.conDato());
        assertEquals(2, row.sinDato());
    }
}
