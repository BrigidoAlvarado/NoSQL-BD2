package com.nosql.apiadmisionesusac.upload;

import com.nosql.apiadmisionesusac.etl.CsvImportService;
import com.nosql.apiadmisionesusac.etl.ImportResult;
import com.nosql.apiadmisionesusac.etl.RejectedRow;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Entrada HTTP para el ETL; el navegador nunca recibe credenciales de las bases. */
@RestController
@RequestMapping("/api/cargas")
public class CsvUploadController {
    private final CsvImportService importer;

    public CsvUploadController(CsvImportService importer) {
        this.importer = importer;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<BatchUploadResponse> upload(@RequestParam("files") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().body(new BatchUploadResponse(List.of(), "Seleccione al menos un archivo CSV."));
        }
        List<FileUploadResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(process(file));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new BatchUploadResponse(results, null));
    }

    @GetMapping("/{sha256Archivo}/rechazos")
    public List<RejectedRow> rejectedRows(@PathVariable String sha256Archivo,
                                          @RequestParam(defaultValue = "100") int limit) {
        return importer.rejectedRows(sha256Archivo, limit);
    }

    private FileUploadResponse process(MultipartFile file) {
        String name = safeName(file);
        if (file.isEmpty()) {
            return FileUploadResponse.error(name, "ARCHIVO_VACIO", "El archivo está vacío.");
        }
        if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            return FileUploadResponse.error(name, "ARCHIVO_INVALIDO", "El archivo debe tener extensión .csv.");
        }
        Instant started = Instant.now();
        try {
            ImportResult result = importer.importFile(name, file.getBytes());
            return FileUploadResponse.from(name, result, Duration.between(started, Instant.now()).toMillis(),
                    importer.rejectedRows(result.sha256Archivo(), 100));
        } catch (Exception exception) {
            return FileUploadResponse.error(name, "ERROR_ARCHIVO", "No fue posible leer el archivo recibido.",
                    Duration.between(started, Instant.now()).toMillis());
        }
    }

    private String safeName(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name == null || name.isBlank() ? "archivo.csv" : name.replaceAll("[\\r\\n]", "");
    }

    public record BatchUploadResponse(List<FileUploadResponse> archivos, String mensaje) {
    }

    public record FileUploadResponse(String nombreArchivo, String sha256Archivo, String estado,
                                     int registrosLeidos, int registrosProcesados, int registrosRechazados,
                                     long duracionMilisegundos, boolean reutilizada, String detalle,
                                     List<RejectedRow> rechazos) {
        static FileUploadResponse from(String name, ImportResult result, long elapsed, List<RejectedRow> rejections) {
            return new FileUploadResponse(name, result.sha256Archivo(), result.estado(), result.filasLeidas(),
                    result.filasValidas(), result.filasRechazadas(), elapsed, result.reutilizada(), result.detalle(), rejections);
        }

        static FileUploadResponse error(String name, String state, String detail) {
            return error(name, state, detail, 0);
        }

        static FileUploadResponse error(String name, String state, String detail, long elapsed) {
            return new FileUploadResponse(name, null, state, 0, 0, 0, elapsed, false, detail, List.of());
        }
    }
}
