package com.nosql.apiadmisionesusac.upload;

import com.nosql.apiadmisionesusac.upload.dto.LoadHistoryResponse;
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
    public LoadHistoryResponse.Page loads(@RequestParam(defaultValue = "0") int page) {
        return LoadHistoryResponse.from(history.loads(page), "cargas");
    }

    @GetMapping("/{hash}/detalle/{kind}")
    public LoadHistoryResponse.Page details(@PathVariable String hash,
            @PathVariable String kind, @RequestParam(defaultValue = "0") int page) {
        return LoadHistoryResponse.from(history.details(hash, kind, page), kind);
    }
}
