package com.nosql.apiadmisionesusac.health;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class DatabaseHealthController {

    private final DatabaseHealthService health;

    public DatabaseHealthController(DatabaseHealthService health) {
        this.health = health;
    }

    @GetMapping
    public ResponseEntity<DatabaseHealthService.ConnectionStatus> check() {
        var result = health.check();
        return ResponseEntity.status("UP".equals(result.status())
                ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(result);
    }
}
