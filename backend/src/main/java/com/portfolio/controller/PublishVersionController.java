package com.portfolio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.CacheEvict;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight version endpoint for instant publish detection.
 * Public pages poll GET /api/public/version every 15s.
 * When admin publishes, POST /api/admin/publish bumps the version.
 * Public pages compare their local version and reload data if changed.
 */
@RestController
public class PublishVersionController {

    // Shared in-memory version counter (resets on restart — that's fine)
    public static final AtomicLong VERSION = new AtomicLong(System.currentTimeMillis());

    /** Public endpoint — polled by frontend every 15 seconds */
    @GetMapping("/api/public/version")
    public ResponseEntity<Map<String, Object>> getVersion() {
        return ResponseEntity.ok(Map.of(
            "version", VERSION.get(),
            "ts", System.currentTimeMillis()
        ));
    }

    /**
     * Admin publish trigger — called after any save/publish action.
     * Bumps version so public pages detect the change and reload.
     * Also evicts all public caches.
     */
    @PostMapping("/api/admin/publish")
    @CacheEvict(value = {
        "publicPortfolio", "publicSettings", "publicTestimonials",
        "publicServices", "publicProjects", "publicSkills"
    }, allEntries = true)
    public ResponseEntity<Map<String, Object>> publishNow() {
        long newVersion = System.currentTimeMillis();
        VERSION.set(newVersion);
        return ResponseEntity.ok(Map.of(
            "published", true,
            "version", newVersion
        ));
    }
}
