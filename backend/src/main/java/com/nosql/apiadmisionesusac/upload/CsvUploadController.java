package com.nosql.apiadmisionesusac.upload;

import com.nosql.apiadmisionesusac.etl.RejectedRow;
import com.nosql.apiadmisionesusac.upload.dto.BatchUploadResponse;
import com.nosql.apiadmisionesusac.upload.dto.FileUploadResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/** Entrada HTTP para el ETL; el navegador nunca recibe credenciales de las bases. */
@RestController
@RequestMapping("/api/cargas")
public class CsvUploadController {
    private final CsvUploadService uploads;

    public CsvUploadController(CsvUploadService uploads) {
        this.uploads = uploads;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<BatchUploadResponse> upload(
            @RequestParam("files") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(
                            new BatchUploadResponse(
                                    List.of(), "Seleccione al menos un archivo CSV."));
        }
        List<FileUploadResponse> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploads.process(file));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new BatchUploadResponse(results, null));
    }

    @GetMapping("/{sha256Archivo}/rechazos")
    public List<RejectedRow> rejectedRows(
            @PathVariable String sha256Archivo, @RequestParam(defaultValue = "100") int limit) {
        return uploads.rejectedRows(sha256Archivo, limit);
    }
}
