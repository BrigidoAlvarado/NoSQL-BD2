package com.nosql.apiadmisionesusac.upload;

import com.nosql.apiadmisionesusac.etl.ImportProgress;
import com.nosql.apiadmisionesusac.upload.dto.FileUploadResponse;
import com.nosql.apiadmisionesusac.upload.dto.UploadProgressResponse;
import com.nosql.apiadmisionesusac.upload.dto.UploadProgressResponse.Event;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Reserva un único lote y publica su avance sin bloquear las consultas de seguimiento. */
@Component
public class UploadProgress {
    private String id;
    private boolean active;
    private boolean errors;
    private String state = "INACTIVA";
    private int totalFiles;
    private int currentFile;
    private String filename;
    private String stage;
    private int processed;
    private int total;
    private long sequence;
    private final List<Event> events = new ArrayList<>();
    private final List<FileUploadResponse> results = new ArrayList<>();

    public synchronized void start(int files) {
        // La comprobación y la reserva son atómicas, incluso entre navegadores distintos.
        if (active) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya hay una carga en curso. Consulta su avance antes de enviar otra.");
        }
        id = UUID.randomUUID().toString();
        active = true;
        errors = false;
        state = "EN_CURSO";
        totalFiles = files;
        currentFile = 0;
        filename = null;
        stage = "RECIBIDA";
        processed = 0;
        total = 0;
        sequence = 0;
        events.clear();
        results.clear();
        log("Carga iniciada: " + files + " archivo(s).");
    }

    public synchronized void nextFile(String name) {
        currentFile++;
        filename = name;
        stage = "VALIDANDO";
        processed = 0;
        total = 0;
        log("Archivo " + currentFile + " de " + totalFiles + ": " + name);
    }

    public synchronized void update(ImportProgress progress) {
        stage = progress.etapa();
        processed = progress.procesadas();
        total = progress.total();
        log(progress.mensaje());
    }

    public synchronized void fileFinished(FileUploadResponse result) {
        results.add(result);
        stage = result.estado();
        errors |= !"COMPLETA".equals(result.estado());
        log(filename + ": " + result.detalle());
    }

    public synchronized void failed() {
        errors = true;
        stage = "ERROR";
        // Los eventos son públicos: nunca publicar excepciones internas ni credenciales.
        log("La carga se interrumpió por un error. Revisa el historial antes de reintentar.");
    }

    public synchronized void finish() {
        active = false;
        state = errors ? "CON_ERRORES" : "COMPLETA";
        log(errors ? "Lote finalizado con incidencias. Revisa los resultados."
                : "Todos los archivos del lote finalizaron correctamente.");
    }

    public synchronized UploadProgressResponse snapshot() {
        return new UploadProgressResponse(id, active, state, totalFiles, currentFile,
                filename, stage, processed, total, List.copyOf(events), List.copyOf(results));
    }

    private void log(String message) {
        events.add(new Event(++sequence, Instant.now(), message));
        // Se conserva una ventana acotada; el historial permanente sigue en MongoDB.
        if (events.size() > 200) {
            events.removeFirst();
        }
    }
}
