package com.medflow.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtValidator following TDD methodology.
 * These tests are written FIRST (RED phase) before implementation.
 */
class JwtValidatorTest {

    private JwtValidator jwtValidator;
    private String secretKey;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtValidator = new JwtValidator();
        secretKey = "test-secret-key-for-jwt-validation-must-be-at-least-256-bits-long";
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
        ReflectionTestUtils.setField(jwtValidator, "secretKey", secretKey);
    }

    @Test
    void shouldValidateValidJwt() {
        // Arrange
        String token = generateValidToken("123", "DOCTOR,ADMIN");

        // Act
        Claims claims = jwtValidator.validateAndGetClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals("123", claims.get("userId", String.class));
        assertEquals("DOCTOR,ADMIN", claims.get("roles", String.class));
    }

    @Test
    void shouldRejectExpiredJwt() {
        // Arrange
        String expiredToken = generateExpiredToken();

        // Act & Assert
        Exception exception = assertThrows(JwtException.class, () -> {
            jwtValidator.validateAndGetClaims(expiredToken);
        });
        assertTrue(exception.getMessage().contains("expired") || 
                   exception.getMessage().contains("Expired"));
    }

    @Test
    void shouldRejectInvalidSignature() {
        // Arrange
        String tokenWithInvalidSignature = generateTokenWithInvalidSignature();

        // Act & Assert
        Exception exception = assertThrows(JwtException.class, () -> {
            jwtValidator.validateAndGetClaims(tokenWithInvalidSignature);
        });
        assertTrue(exception.getMessage().contains("signature") || 
                   exception.getMessage().contains("Invalid"));
    }

    @Test
    void shouldRejectMalformedJwt() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.token";

        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtValidator.validateAndGetClaims(malformedToken);
        });
    }

    @Test
    void shouldExtractUserIdFromClaims() {
        // Arrange
        String token = generateValidToken("456", "NURSE");

        // Act
        Claims claims = jwtValidator.validateAndGetClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals("456", claims.get("userId", String.class));
    }

    @Test
    void shouldExtractRolesFromClaims() {
        // Arrange
        String token = generateValidToken("789", "ADMIN,DOCTOR,NURSE");

        // Act
        Claims claims = jwtValidator.validateAndGetClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals("ADMIN,DOCTOR,NURSE", claims.get("roles", String.class));
    }

    // Helper methods to generate test tokens

    private String generateValidToken(String userId, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateExpiredToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("roles", "DOCTOR");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("123")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago (expired)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateTokenWithInvalidSignature() {
        // Generate token with a different secret key
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrong-secret-key-that-is-different-from-the-correct-one-256-bits".getBytes());
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("roles", "DOCTOR");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("123")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(wrongKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
