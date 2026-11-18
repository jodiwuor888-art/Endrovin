package com.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

/**
 * Lightweight warm-up endpoint.
 * The frontend pings /api/ping on page load to wake the Render free-tier
 * container before the user triggers a real API call.
 */
@RestController
@RequestMapping("/api")
public class WarmUpController {

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "alive",
            "ts", Instant.now().toEpochMilli()
        ));
    }
}
