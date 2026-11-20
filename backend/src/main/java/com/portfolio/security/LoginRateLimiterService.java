package com.portfolio.security;

import com.portfolio.config.PortfolioProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LoginRateLimiterService {

    private final PortfolioProperties properties;

    private static class Attempt {
        int count;
        LocalDateTime lockoutUntil;

        Attempt(int count) {
            this.count = count;
        }
    }

    // Maximum number of distinct IP/username keys to track in memory.
    // Older entries are evicted (LRU order) when the cap is reached,
    // preventing unbounded memory growth under a distributed brute-force attack.
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Attempt> attempts = Collections.synchronizedMap(
            new LinkedHashMap<>(256, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Attempt> eldest) {
                    return size() > MAX_TRACKED_KEYS;
                }
            }
    );

    public LoginRateLimiterService(PortfolioProperties properties) {
        this.properties = properties;
    }

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key.toLowerCase());
        if (attempt == null) {
            return false;
        }

        if (attempt.lockoutUntil != null) {
            if (LocalDateTime.now().isBefore(attempt.lockoutUntil)) {
                return true;
            } else {
                // Lockout period expired
                attempts.remove(key.toLowerCase());
                return false;
            }
        }

        return false;
    }

    public void recordFailedAttempt(String key) {
        String normalizedKey = key.toLowerCase();
        int maxAttempts = properties.getSecurity().getRateLimit().getMaxAttempts();
        int lockoutMinutes = properties.getSecurity().getRateLimit().getLockoutDurationMinutes();

        synchronized (attempts) {
            Attempt attempt = attempts.get(normalizedKey);
            if (attempt == null) {
                attempt = new Attempt(1);
            } else {
                attempt.count++;
                if (attempt.count >= maxAttempts) {
                    attempt.lockoutUntil = LocalDateTime.now().plusMinutes(lockoutMinutes);
                }
            }
            attempts.put(normalizedKey, attempt);
        }
    }

    public void resetAttempts(String key) {
        attempts.remove(key.toLowerCase());
    }

    public int getRemainingLockoutMinutes(String key) {
        Attempt attempt = attempts.get(key.toLowerCase());
        if (attempt != null && attempt.lockoutUntil != null) {
            long seconds = java.time.Duration.between(LocalDateTime.now(), attempt.lockoutUntil).getSeconds();
            return (int) Math.max(1, (seconds + 59) / 60);
        }
        return 0;
    }
}
