package com.nosql.apiadmisionesusac.statistics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estadisticas")
public class StatisticsController {
    private final MongoStatisticsService statistics;

    public StatisticsController(MongoStatisticsService statistics) {
        this.statistics = statistics;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@org.springframework.web.bind.annotation.RequestParam java.util.Map<String, String> filters) {
        return filters.isEmpty() ? statistics.dashboard() : statistics.dashboard(com.nosql.apiadmisionesusac.shared.AnalysisFilters.mongo(filters));
    }
}
