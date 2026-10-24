package com.nosql.apiadmisionesusac.search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/busqueda")
public class ApplicantSearchController {
    private final ApplicantSearchService search;

    public ApplicantSearchController(ApplicantSearchService search) { this.search = search; }

    @GetMapping
    public SearchResponse search(@RequestParam String criterio, @RequestParam String termino,
                                 @RequestParam(required = false) Integer limite) {
        return search.search(criterio, termino, limite);
    }

    @GetMapping("/aspirantes/{aspiranteId}")
    public ApplicantDetail applicant(@PathVariable String aspiranteId) { return search.applicant(aspiranteId); }
}
