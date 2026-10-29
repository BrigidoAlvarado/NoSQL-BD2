package com.nosql.apiadmisionesusac.upload;

import java.util.List;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

/** Consulta de trazabilidad; no vuelve a ejecutar el ETL. */
@RestController
@RequestMapping("/api/cargas")
public class LoadHistoryController {
    private final LoadHistoryService history;

    public LoadHistoryController(LoadHistoryService history) {
        this.history = history;
    }

    @GetMapping
    public LoadHistoryService.Page loads(@RequestParam(defaultValue = "0") int page) {
        return history.loads(page);
    }

    @GetMapping("/{hash}/detalle/{kind}")
    public LoadHistoryService.Page details(@PathVariable String hash,
            @PathVariable String kind, @RequestParam(defaultValue = "0") int page) {
        return history.details(hash, kind, page);
    }
}
