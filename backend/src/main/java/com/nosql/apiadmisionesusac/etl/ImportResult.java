package com.nosql.apiadmisionesusac.etl;

/** Resultado apto para la futura API de carga, sin exponer errores internos. */
public record ImportResult(
        String sha256Archivo,
        String estado,
        int filasLeidas,
        int filasValidas,
        int filasRechazadas,
        boolean reutilizada,
        String detalle) {
}
