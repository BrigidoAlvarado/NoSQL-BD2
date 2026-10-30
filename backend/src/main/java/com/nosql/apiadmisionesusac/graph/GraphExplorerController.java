package com.nosql.apiadmisionesusac.graph;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/grafos")
public class GraphExplorerController {
    private final GraphExplorerService graphs;

    public GraphExplorerController(GraphExplorerService graphs) {
        this.graphs = graphs;
    }

    @GetMapping
    public GraphResponse explore(
            @RequestParam(required = false) Integer anio,
            @RequestParam(required = false) String carrera,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) String departamento,
            @RequestParam(defaultValue = "10") int limite) {
        if (limite < 5 || limite > 40) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El límite debe estar entre 5 y 40.");
        return graphs.explore(anio, carrera, resultado, departamento, limite);
    }

    @GetMapping("/opciones")
    public java.util.Map<String, java.util.List<String>> options() { return graphs.options(); }

    @GetMapping("/consultas/{id}")
    public GraphQueryResponse query(@PathVariable String id, @RequestParam java.util.Map<String, String> filters) {
        try {
            return graphs.query(id, filters);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "La consulta de grafo solicitada no existe.");
        }
    }
}
