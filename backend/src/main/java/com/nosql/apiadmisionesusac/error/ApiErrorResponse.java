package com.nosql.apiadmisionesusac.error;

import java.time.Instant;

/** Respuesta segura y estable para errores consumidos por la interfaz. */
public record ApiErrorResponse(String codigo, String mensaje, Instant ocurridoEn) { }
