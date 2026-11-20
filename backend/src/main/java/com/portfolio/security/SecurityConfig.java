package com.portfolio.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                    .frameOptions(frameOptions -> frameOptions.deny())
                    .httpStrictTransportSecurity(hsts -> hsts
                            .maxAgeInSeconds(31536000)
                            .includeSubDomains(true)
                            .preload(true))
                    .xssProtection(xss -> xss.disable()) // Modern browsers use CSP
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' https://fonts.googleapis.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com; font-src 'self' https://fonts.gstatic.com; img-src 'self' data: https:; connect-src 'self' http://localhost:* https:;"))
                )
                .authorizeHttpRequests(auth -> auth
                        // Public APIs
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/status").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/setup-first-admin").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/request-password-reset").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/reset-password").permitAll()

                        // WebAuthn Passkey public authentication endpoints (Step 1 and Step 2 assertion)
                        .requestMatchers(HttpMethod.POST, "/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/passkey/authentication/**").permitAll()
                        .requestMatchers("/api/admin/passkey/recovery-verify").permitAll()

                        // Static resources and uploads
                        .requestMatchers("/", "/index.html", "/*.html", "/*.css", "/*.js", "/*.ico", "/*.png", "/*.jpg", "/*.svg").permitAll()
                        .requestMatchers("/frontend/**", "/frontendport/**", "/assets/**", "/admin/**", "/uploads/**", "/static/**").permitAll()

                        // Swagger / OpenAPI documentation (restricted to authenticated administrators)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").hasRole("ADMIN")

                        // Forward and error dispatchers
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()

                        // Actuator health and info endpoints open to all; metrics/sensitive restricted to ADMIN
                        .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/info/**").permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // All Admin Management endpoints require ADMIN role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Fallback
                        .anyRequest().authenticated()
                );

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
