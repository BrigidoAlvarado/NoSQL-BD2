package com.nosql.apiadmisionesusac.etl;

/** Avance confirmado de una etapa del archivo; no equivale a documentos nuevos. */
public record ImportProgress(String etapa, int procesadas, int total, String mensaje) {}
