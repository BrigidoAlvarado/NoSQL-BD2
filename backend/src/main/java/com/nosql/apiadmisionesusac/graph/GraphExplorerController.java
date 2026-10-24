package com.nosql.apiadmisionesusac.graph;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grafos")
public class GraphExplorerController {
    private final GraphExplorerService graphs;
    public GraphExplorerController(GraphExplorerService graphs) { this.graphs = graphs; }
    @GetMapping
    public GraphResponse explore(@RequestParam(required = false) Integer anio, @RequestParam(required = false) String carrera,
                                 @RequestParam(required = false) String resultado, @RequestParam(required = false) String departamento,
                                 @RequestParam(defaultValue = "40") int limite) { return graphs.explore(anio, carrera, resultado, departamento, limite); }
    @GetMapping("/consultas/{id}")
    public ResponseEntity<GraphQueryResponse> query(@PathVariable String id) {
        try { return ResponseEntity.ok(graphs.query(id)); } catch (IllegalArgumentException exception) { return ResponseEntity.notFound().build(); }
    }
}
