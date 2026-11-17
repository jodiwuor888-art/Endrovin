package com.portfolio.security;

import com.portfolio.config.PortfolioProperties;
import com.portfolio.entity.Admin;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final PortfolioProperties properties;

    public JwtTokenProvider(PortfolioProperties properties) {
        this.properties = properties;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = properties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        AdminUserDetails userPrincipal = (AdminUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + properties.getSecurity().getJwt().getExpirationMs());

        String roles = userPrincipal.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("email", userPrincipal.getEmail())
                .claim("roles", roles)
                .claim("status", "AUTHENTICATED")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateTokenForAdmin(Admin admin) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + properties.getSecurity().getJwt().getExpirationMs());

        String roles = admin.getRoles().stream()
                .map(r -> "ROLE_" + r.getRoleName())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(admin.getUsername())
                .claim("userId", admin.getAdminId())
                .claim("email", admin.getEmail())
                .claim("roles", roles)
                .claim("status", "AUTHENTICATED")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String generatePendingMfaToken(Admin admin) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 300000); // 5 minutes

        return Jwts.builder()
                .subject(admin.getUsername())
                .claim("userId", admin.getAdminId())
                .claim("email", admin.getEmail())
                .claim("roles", "")
                .claim("status", "PENDING_MFA")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims getClaimsFromJwt(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromJwt(String token) {
        return getClaimsFromJwt(token).getSubject();
    }

    public boolean isPendingMfa(String token) {
        try {
            Claims claims = getClaimsFromJwt(token);
            return "PENDING_MFA".equals(claims.get("status"));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}
