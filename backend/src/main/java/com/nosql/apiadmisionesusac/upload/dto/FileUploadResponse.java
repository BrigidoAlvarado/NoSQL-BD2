package com.nosql.apiadmisionesusac.upload.dto;

import com.nosql.apiadmisionesusac.etl.ImportResult;
import com.nosql.apiadmisionesusac.etl.RejectedRow;

import java.util.List;

public record FileUploadResponse(
        String nombreArchivo,
        String sha256Archivo,
        String estado,
        int registrosLeidos,
        int registrosProcesados,
        int registrosRechazados,
        long duracionMilisegundos,
        boolean reutilizada,
        String detalle,
        List<RejectedRow> rechazos) {
    public static FileUploadResponse from(
            String name, ImportResult result, long elapsed, List<RejectedRow> rejections) {
        return new FileUploadResponse(
                name,
                result.sha256Archivo(),
                result.estado(),
                result.filasLeidas(),
                result.filasValidas(),
                result.filasRechazadas(),
                elapsed,
                result.reutilizada(),
                result.detalle(),
                rejections);
    }

    public static FileUploadResponse error(String name, String state, String detail) {
        return error(name, state, detail, 0);
    }

    public static FileUploadResponse error(String name, String state, String detail, long elapsed) {
        return new FileUploadResponse(
                name, null, state, 0, 0, 0, elapsed, false, detail, List.of());
    }
}
