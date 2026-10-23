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
    public DashboardResponse dashboard() {
        return statistics.dashboard();
    }
}
