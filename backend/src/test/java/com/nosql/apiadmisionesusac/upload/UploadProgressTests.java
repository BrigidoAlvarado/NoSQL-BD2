package com.nosql.apiadmisionesusac.upload;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nosql.apiadmisionesusac.error.ApiExceptionHandler;
import com.nosql.apiadmisionesusac.etl.CsvImportService;
import com.nosql.apiadmisionesusac.etl.ImportProgress;
import com.nosql.apiadmisionesusac.etl.ImportResult;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UploadProgressTests {
    private final UploadProgress progress = new UploadProgress();
    private final CsvImportService importer = mock(CsvImportService.class);
    private final CsvUploadService uploads = new CsvUploadService(importer, progress);
    private final MockMultipartFile file = new MockMultipartFile("files", "muestra.csv", "text/csv", new byte[] {1});

    @Test
    void consultaAvanceDuranteUnaCargaYRechazaUnSegundoEnvio() throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        when(importer.importFile(anyString(), any(byte[].class), any())).thenAnswer(invocation -> {
            Consumer<ImportProgress> callback = invocation.getArgument(2);
            callback.accept(new ImportProgress("NEO4J", 500, 1000, "Neo4j: 500 de 1000."));
            entered.countDown();
            if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Tiempo de prueba agotado");
            return new ImportResult("hash", "COMPLETA", 1000, 1000, 0, false, "Sincronizada.");
        });
        var mvc = MockMvcBuilders.standaloneSetup(new CsvUploadController(uploads, progress))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        try (var executor = Executors.newSingleThreadExecutor()) {
            var first = executor.submit(() -> mvc.perform(multipart("/api/cargas").file(file))
                    .andExpect(status().isOk()));
            try {
                assertTrue(entered.await(5, TimeUnit.SECONDS));
                mvc.perform(get("/api/cargas/progreso"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.activa").value(true))
                        .andExpect(jsonPath("$.archivoActual").value(1))
                        .andExpect(jsonPath("$.etapa").value("NEO4J"))
                        .andExpect(jsonPath("$.procesadas").value(500));
                mvc.perform(multipart("/api/cargas").file(file))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.mensaje").value(
                                "Ya hay una carga en curso. Consulta su avance antes de enviar otra."));
                assertTrue(progress.snapshot().activa());
                verify(importer, times(1)).importFile(anyString(), any(byte[].class), any());
            } finally {
                release.countDown();
            }
            first.get(5, TimeUnit.SECONDS);
        }
        assertFalse(progress.snapshot().activa());
        assertEquals("COMPLETA", progress.snapshot().estado());
        assertEquals(1, progress.snapshot().archivos().size());
    }

    @Test
    void liberaElBloqueoTrasUnErrorSinPublicarLaExcepcion() {
        when(importer.importFile(anyString(), any(byte[].class), any()))
                .thenThrow(new IllegalStateException("credencial-privada"));
        assertThrows(IllegalStateException.class, () -> uploads.processBatch(java.util.List.of(file)));
        assertFalse(progress.snapshot().activa());
        assertEquals("CON_ERRORES", progress.snapshot().estado());
        assertFalse(progress.snapshot().toString().contains("credencial-privada"));
        assertDoesNotThrow(() -> progress.start(1));
    }

    @Test
    void limitaMensajesYEntregaFotografiasInmutables() {
        progress.start(1);
        var initial = progress.snapshot();
        for (int index = 0; index < 250; index++) {
            progress.update(new ImportProgress("MONGODB", index, 250, "Avance " + index));
        }
        assertEquals(1, initial.eventos().size());
        assertEquals(200, progress.snapshot().eventos().size());
        assertEquals("Avance 249", progress.snapshot().eventos().getLast().mensaje());
    }
}
