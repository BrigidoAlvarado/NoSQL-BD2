package com.nosql.apiadmisionesusac.etl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntConsumer;
import org.junit.jupiter.api.Test;

class ImportProgressTests {
    @Test
    void informaEtapasYConservaAvanceParcialSiFallaNeo4j() throws Exception {
        var processor = mock(CsvEvaluationProcessor.class);
        var mongo = mock(EtlMongoStore.class);
        var neo4j = mock(EtlNeo4jStore.class);
        var importer = new CsvImportService(processor, mongo, neo4j);
        when(mongo.completed(anyString())).thenReturn(Optional.empty());
        when(processor.process(any())).thenReturn(new CsvProcessingResult(
                List.of(mock(Evaluation.class), mock(Evaluation.class)), List.of()));
        doAnswer(invocation -> {
            IntConsumer callback = invocation.getArgument(3);
            callback.accept(2);
            return null;
        }).when(mongo).saveEvaluations(anyString(), anyString(), anyList(), any());
        doAnswer(invocation -> {
            IntConsumer callback = invocation.getArgument(1);
            callback.accept(1);
            throw new IllegalStateException("Fallo simulado");
        }).when(neo4j).saveEvaluations(anyList(), any());
        var events = new ArrayList<ImportProgress>();
        var result = importer.importFile("muestra.csv", new byte[] {1}, events::add);
        assertEquals("PENDIENTE_NEO4J", result.estado());
        assertEquals(List.of("VALIDADA", "MONGODB", "NEO4J", "NEO4J"),
                events.stream().map(ImportProgress::etapa).toList());
        assertEquals(1, events.getLast().procesadas());
        assertEquals(2, events.getLast().total());
    }
}
