package com.portfolio.config;

import com.portfolio.controller.PublishVersionController;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.CacheManager;

/**
 * AOP Aspect: Automatically bumps the publish version and evicts all
 * public caches whenever ANY admin controller write method succeeds.
 *
 * This means: profile save, project update, skill delete, testimonial approve,
 * settings change — ALL instantly propagate to the public portal.
 */
@Aspect
@Configuration
public class PublishBumpAspect {

    private final CacheManager cacheManager;

    public PublishBumpAspect(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Intercepts all public methods in admin controllers that are POST, PUT, PATCH, DELETE.
     * Runs AFTER a successful return (no exception).
     */
    @AfterReturning(
        pointcut = "execution(* com.portfolio.controller.Admin*Controller.*(..)) " +
                   "&& (@annotation(org.springframework.web.bind.annotation.PostMapping) " +
                   "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
                   "|| @annotation(org.springframework.web.bind.annotation.PatchMapping) " +
                   "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping))"
    )
    public void bumpPublishVersion() {
        // Bump version — public portal polling will detect this within 15 seconds
        PublishVersionController.VERSION.set(System.currentTimeMillis());

        // Evict all public caches immediately
        String[] caches = {
            "publicPortfolio", "publicSettings", "publicTestimonials",
            "publicServices", "publicProjects", "publicSkills"
        };
        for (String cacheName : caches) {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) cache.clear();
        }
    }
}
