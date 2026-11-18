package com.portfolio.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * In-memory caching for public portfolio data.
 * Reduces DB round-trips for frequently read data (portfolio, settings, testimonials).
 * Use @Cacheable("publicPortfolio") / @CacheEvict in service layer.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "publicPortfolio",
            "publicSettings",
            "publicTestimonials",
            "publicServices",
            "publicProjects",
            "publicSkills"
        );
    }
}
