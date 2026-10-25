package com.ciphercore.verify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "CipherCore Verification Engine",
            "team", "CipherCore - SIH 2026",
            "problemStatement", "SIH26261",
            "port", 8080,
            "cors", "ALLOWED_ALL",
            "timestamp", Instant.now().toString()
        ));
    }
}
