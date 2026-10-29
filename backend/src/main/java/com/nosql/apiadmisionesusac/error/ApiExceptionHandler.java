package com.nosql.apiadmisionesusac.error;

import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiErrorResponse> status(ResponseStatusException exception) {
        HttpStatus code = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? "No se pudo completar la solicitud." : exception.getReason();
        return response(code, "SOLICITUD_INVALIDA", message);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class})
    ResponseEntity<ApiErrorResponse> invalidRequest(Exception ignored) {
        return response(HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA", "Revisa los datos enviados e intenta nuevamente.");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    ResponseEntity<ApiErrorResponse> missingUpload(MissingServletRequestPartException ignored) {
        return response(HttpStatus.BAD_REQUEST, "ARCHIVO_REQUERIDO", "Selecciona al menos un archivo CSV para cargar.");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiErrorResponse> maximumUpload(MaxUploadSizeExceededException ignored) {
        return response(HttpStatus.BAD_REQUEST, "ARCHIVO_DEMASIADO_GRANDE", "Los archivos superan el tamaño máximo permitido de 30 MB por archivo.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> unexpected(Exception exception) {
        if (hasClassPrefix(exception, "com.mongodb.")) {
            return response(HttpStatus.SERVICE_UNAVAILABLE, "MONGODB_NO_DISPONIBLE",
                    "MongoDB no está disponible. Conserva tu trabajo y vuelve a intentarlo cuando el servicio se recupere.");
        }
        if (hasClassPrefix(exception, "org.neo4j.driver.") || hasClassPrefix(exception, "org.springframework.data.neo4j.")) {
            return response(HttpStatus.SERVICE_UNAVAILABLE, "NEO4J_NO_DISPONIBLE",
                    "Neo4j no está disponible. Puedes seguir usando las funciones que no requieren grafos y reintentar después.");
        }
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "OPERACION_NO_COMPLETADA",
                "El servicio no pudo completar la operación. Intenta nuevamente más tarde.");
    }

    private boolean hasClassPrefix(Throwable exception, String prefix) {
        Set<Throwable> seen = new HashSet<>();
        Throwable current = exception;
        while (current != null && seen.add(current)) {
            if (current.getClass().getName().startsWith(prefix)) return true;
            current = current.getCause();
        }
        return false;
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(code, message, java.time.Instant.now()));
    }
}
