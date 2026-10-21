package com.nosql.apiadmisionesusac.upload;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class UploadExceptionHandler {
    @ExceptionHandler({MaxUploadSizeExceededException.class, MissingServletRequestPartException.class})
    ResponseEntity<Map<String, String>> invalidUpload(Exception exception) {
        String message = exception instanceof MaxUploadSizeExceededException
                ? "Los archivos superan el tamaño máximo permitido de 30 MB por archivo."
                : "Seleccione al menos un archivo CSV para cargar.";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }
}
