package com.nosql.apiadmisionesusac.upload;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nosql.apiadmisionesusac.etl.CsvImportService;
import com.nosql.apiadmisionesusac.etl.ImportResult;
import com.nosql.apiadmisionesusac.etl.RejectedRow;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CsvUploadControllerTests {
    @Test
    void procesaVariosArchivosYDevuelveConteosYRechazos() throws Exception {
        CsvImportService importer = mock(CsvImportService.class);
        when(importer.importFile(eq("evaluaciones.csv"), any(byte[].class), any()))
                .thenReturn(new ImportResult("hash", "COMPLETA", 3, 2, 1, false, "Carga sincronizada."));
        when(importer.rejectedRows("hash", 100))
                .thenReturn(List.of(new RejectedRow(4, "FECHA_INVALIDA", "Fecha inválida", java.util.Map.of())));
        when(importer.importFile(eq("sin-filas.csv"), any(byte[].class), any()))
                .thenReturn(new ImportResult("hash-2", "SIN_FILAS_VALIDAS", 2, 0, 2, false, "Sin filas utilizables."));
        when(importer.rejectedRows("hash-2", 100)).thenReturn(List.of());
        UploadProgress progress = new UploadProgress();
        var mvc = MockMvcBuilders.standaloneSetup(
                new CsvUploadController(new CsvUploadService(importer, progress), progress)).build();

        mvc.perform(multipart("/api/cargas")
                        .file(new MockMultipartFile("files", "evaluaciones.csv", "text/csv", "datos".getBytes()))
                        .file(new MockMultipartFile("files", "sin-filas.csv", "text/csv", "datos".getBytes())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.archivos[0].nombreArchivo").value("evaluaciones.csv"))
                .andExpect(jsonPath("$.archivos[0].registrosLeidos").value(3))
                .andExpect(jsonPath("$.archivos[0].rechazos[0].code").value("FECHA_INVALIDA"))
                .andExpect(jsonPath("$.archivos[1].nombreArchivo").value("sin-filas.csv"))
                .andExpect(jsonPath("$.archivos[1].estado").value("SIN_FILAS_VALIDAS"));
    }
}
