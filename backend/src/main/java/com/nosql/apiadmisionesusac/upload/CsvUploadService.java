package com.nosql.apiadmisionesusac.upload;

import com.nosql.apiadmisionesusac.etl.CsvImportService;
import com.nosql.apiadmisionesusac.etl.ImportResult;
import com.nosql.apiadmisionesusac.etl.RejectedRow;
import com.nosql.apiadmisionesusac.upload.dto.FileUploadResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/** Valida el archivo recibido, mide duración y adapta el resultado del ETL. */
@Service
public class CsvUploadService {
    private final CsvImportService importer;

    public CsvUploadService(CsvImportService importer) {
        this.importer = importer;
    }

    public List<RejectedRow> rejectedRows(String hash, int limit) {
        return importer.rejectedRows(hash, limit);
    }

    public FileUploadResponse process(MultipartFile file) {
        String name = safeName(file);
        if (file.isEmpty()) {
            return FileUploadResponse.error(name, "ARCHIVO_VACIO", "El archivo está vacío.");
        }
        if (!name.toLowerCase(java.util.Locale.ROOT).endsWith(".csv")) {
            return FileUploadResponse.error(
                    name, "ARCHIVO_INVALIDO", "El archivo debe tener extensión .csv.");
        }
        Instant started = Instant.now();
        final byte[] contents;
        try {
            contents = file.getBytes();
        } catch (IOException exception) {
            return FileUploadResponse.error(
                    name,
                    "ARCHIVO_ILEGIBLE",
                    "No fue posible leer el archivo recibido.",
                    Duration.between(started, Instant.now()).toMillis());
        }
        ImportResult result = importer.importFile(name, contents);
        return FileUploadResponse.from(
                name,
                result,
                Duration.between(started, Instant.now()).toMillis(),
                importer.rejectedRows(result.sha256Archivo(), 100));
    }

    private String safeName(MultipartFile file) {
        String name = file.getOriginalFilename();
        return name == null || name.isBlank() ? "archivo.csv" : name.replaceAll("[\\r\\n]", "");
    }
}
