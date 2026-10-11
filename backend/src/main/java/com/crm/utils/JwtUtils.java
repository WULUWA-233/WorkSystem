package com.crm.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Base64;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * JWT Token Utility
 *
 * @author CRM Team
 */
@Component
public class JwtUtils {

    private static final Logger log = LoggerFactory.getLogger(JwtUtils.class);

    /** Default secret key (override in production via configuration) */
    private static final String DEFAULT_SECRET = "crm-system-jwt-secret-key-2024-min-length-256-bits!!";

    private static String secret = DEFAULT_SECRET;

    /** Token expiration in seconds (for response) */
    public static long EXPIRATION_SECONDS = 7200;

    @Value("${app.jwt.secret:" + DEFAULT_SECRET + "}")
    public void setSecret(String configuredSecret) {
        if (configuredSecret != null && !configuredSecret.trim().isEmpty()) {
            secret = configuredSecret.trim();
        }
    }

    @Value("${app.jwt.expiration-seconds:7200}")
    public void setExpirationSeconds(long expirationSeconds) {
        if (expirationSeconds > 0) {
            EXPIRATION_SECONDS = expirationSeconds;
        }
    }

    /**
     * Generate a SecretKey from the configured secret string
     */
    private static SecretKey getSecretKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token
     *
     * @param userId   user ID
     * @param username username
     * @param claims   additional claims
     * @return JWT token string
     */
    public static String generateToken(Long userId, String username, Map<String, Object> claims) {
        // Copy caller-owned maps, which may be immutable or shared.
        claims = claims == null ? new HashMap<>() : new HashMap<>(claims);
        claims.put("userId", userId);
        claims.put("username", username);
        // Issued-at timestamps have second precision; distinguish even rapid re-logins.
        claims.put("sessionId", UUID.randomUUID().toString());

        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_SECONDS * 1000L);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setId(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parse a JWT token and extract claims
     *
     * @param token JWT token string
     * @return Claims object
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract username from token
     *
     * @param token JWT token string
     * @return username
     */
    public static String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Extract user ID from token
     *
     * @param token JWT token string
     * @return user ID
     */
    public static Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getId());
    }

    /**
     * Generate a JWT token (convenience method without extra claims)
     *
     * @param userId   user ID
     * @param username username
     * @return JWT token string
     */
    public static String generateToken(Long userId, String username) {
        return generateToken(userId, username, null);
    }

    /** Bind sessions to the current stored password without exposing its BCrypt hash. */
    public static String credentialVersion(String passwordHash) {
        if (passwordHash == null || passwordHash.isEmpty()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(passwordHash.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public static boolean matchesCredentialVersion(String version, String passwordHash) {
        if (version == null || passwordHash == null || passwordHash.isEmpty()) return false;
        return MessageDigest.isEqual(version.getBytes(StandardCharsets.UTF_8),
                credentialVersion(passwordHash).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validate token and return true if valid (not expired, parsable)
     *
     * @param token JWT token string
     * @return true if valid
     */
    public static boolean validateToken(String token) {
        try {
            parseToken(token);
            return !isExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate if the token is expired
     *
     * @param token JWT token string
     * @return true if expired
     */
    public static boolean isExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return true;
        }
    }
}
