package com.portfolio.security;

import com.portfolio.config.PortfolioProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

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

        attempts.compute(normalizedKey, (k, attempt) -> {
            if (attempt == null) {
                return new Attempt(1);
            }
            attempt.count++;
            if (attempt.count >= maxAttempts) {
                attempt.lockoutUntil = LocalDateTime.now().plusMinutes(lockoutMinutes);
            }
            return attempt;
        });
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
