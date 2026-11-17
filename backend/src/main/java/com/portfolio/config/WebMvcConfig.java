package com.portfolio.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PortfolioProperties properties;

    /**
     * Comma-separated list of allowed CORS origins.
     * Set via ALLOWED_ORIGINS env var or WEBAUTHN_ALLOWED_ORIGINS in production.
     * Falls back to allowing all origins if not configured.
     */
    @Value("${portfolio.cors.allowed-origins:${WEBAUTHN_ALLOWED_ORIGINS:*}}")
    private String allowedOriginsRaw;

    public WebMvcConfig(PortfolioProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        String[] origins;
        if (allowedOriginsRaw == null || allowedOriginsRaw.isBlank() || "*".equals(allowedOriginsRaw.trim())) {
            origins = new String[]{"*"};
        } else {
            origins = allowedOriginsRaw.split(",");
            // Trim whitespace
            for (int i = 0; i < origins.length; i++) {
                origins[i] = origins[i].trim();
            }
        }

        boolean usePatterns = origins.length == 1 && "*".equals(origins[0]);

        if (usePatterns) {
            registry.addMapping("/**")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        } else {
            registry.addMapping("/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Map uploads folder to /uploads/**
        Path uploadDir = Paths.get(properties.getUpload().getDir()).toAbsolutePath().normalize();
        String uploadPath = uploadDir.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.endsWith("/") ? uploadPath : uploadPath + "/")
                .setCachePeriod(3600);

        // Swagger UI static assets
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .setCachePeriod(3600);

        // Explicitly serve classpath static resources (css, js, favicon)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);

        // Map frontendport directory for browser access
        // NOTE: Only map specific non-API prefixes to avoid intercepting /api/** routes
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("file:frontendport/assets/", "file:../frontendport/assets/")
                .setCachePeriod(0);

        registry.addResourceHandler("/admin/**")
                .addResourceLocations("file:frontendport/admin/", "file:../frontendport/admin/")
                .setCachePeriod(0);

        // Root HTML files and media assets only — explicitly named, not a wildcard that catches /api/**
        registry.addResourceHandler("/*.html", "/*.ico", "/*.png", "/*.jpg", "/*.jpeg", "/*.svg", "/*.webmanifest")
                .addResourceLocations("file:frontendport/", "file:../frontendport/", "classpath:/static/")
                .setCachePeriod(0);

        // Root index fallback (SPA / direct navigation to /)
        registry.addResourceHandler("/")
                .addResourceLocations("file:frontendport/", "file:../frontendport/")
                .setCachePeriod(0);
    }
}
