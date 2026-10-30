package com.nosql.apiadmisionesusac.upload.dto;

import java.time.Instant;
import java.util.List;

/** Fotografía del último lote atendido por esta instancia del backend. */
public record UploadProgressResponse(
        String id, boolean activa, String estado, int totalArchivos, int archivoActual,
        String nombreArchivo, String etapa, int procesadas, int totalEvaluaciones,
        List<Event> eventos, List<FileUploadResponse> archivos) {
    public record Event(long secuencia, Instant fecha, String mensaje) {}
}
