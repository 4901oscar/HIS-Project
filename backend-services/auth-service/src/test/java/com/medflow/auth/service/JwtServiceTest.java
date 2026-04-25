package com.medflow.auth.service;

import com.medflow.auth.domain.Role;
import com.medflow.auth.domain.RoleName;
import com.medflow.auth.domain.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for JwtService (RED phase - TDD)
 * These tests will fail until JwtService is implemented
 */
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private String secretKey = "test-secret-key-for-jwt-signing-must-be-256-bits-minimum-length-required";
    private long expirationTime = 86400000; // 24 hours in milliseconds

    @BeforeEach
    void setUp() {
        // Initialize JwtService (will fail until class exists)
        jwtService = new JwtService();
        
        // Set test configuration using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "expirationTime", expirationTime);
        
        // Create test user with roles
        Role doctorRole = Role.builder()
                .id(1L)
                .name(RoleName.DOCTOR)
                .description("Médico")
                .build();
        
        Role adminRole = Role.builder()
                .id(2L)
                .name(RoleName.ADMIN)
                .description("Súper Usuario")
                .build();
        
        testUser = User.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .username("doctor1")
                .password("$2a$10$hashedPassword")
                .email("doctor1@medflow.com")
                .firstName("Juan")
                .firstLastName("Pérez")
                .active(true)
                .roles(Set.of(doctorRole, adminRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldGenerateValidJwt() {
        // When: Generate JWT for user
        String token = jwtService.generateToken(testUser);
        
        // Then: Token should not be null or empty
        assertNotNull(token, "Generated token should not be null");
        assertFalse(token.isEmpty(), "Generated token should not be empty");
        
        // JWT should have 3 parts separated by dots (header.payload.signature)
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts (header.payload.signature)");
    }

    @Test
    void shouldExtractUserIdFromJwt() {
        // Given: A valid JWT
        String token = jwtService.generateToken(testUser);
        
        // When: Extract userId from token
        String userId = jwtService.extractUserId(token);
        
        // Then: Should match the test user's ID
        assertEquals("550e8400-e29b-41d4-a716-446655440000", userId, "Extracted userId should match test user ID");
    }

    @Test
    void shouldExtractRolesFromJwt() {
        // Given: A valid JWT
        String token = jwtService.generateToken(testUser);
        
        // When: Extract claims from token
        Claims claims = jwtService.extractClaims(token);
        String roles = claims.get("roles", String.class);
        
        // Then: Should contain both roles as comma-separated string
        assertNotNull(roles, "Roles claim should not be null");
        assertTrue(roles.contains("DOCTOR"), "Roles should contain DOCTOR");
        assertTrue(roles.contains("ADMIN"), "Roles should contain ADMIN");
    }

    @Test
    void shouldValidateValidJwt() {
        // Given: A valid JWT
        String token = jwtService.generateToken(testUser);
        
        // When: Validate the token
        boolean isValid = jwtService.isValid(token);
        
        // Then: Should return true
        assertTrue(isValid, "Valid token should be validated as true");
    }

    @Test
    void shouldRejectExpiredJwt() {
        // Given: A JwtService with very short expiration (1 millisecond)
        JwtService shortExpirationService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationService, "secretKey", secretKey);
        ReflectionTestUtils.setField(shortExpirationService, "expirationTime", 1L);
        
        String token = shortExpirationService.generateToken(testUser);
        
        // When: Wait for token to expire
        try {
            Thread.sleep(10); // Wait 10ms to ensure expiration
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Then: Token should be invalid
        boolean isValid = shortExpirationService.isValid(token);
        assertFalse(isValid, "Expired token should be invalid");
    }

    @Test
    void shouldRejectInvalidSignature() {
        // Given: A valid JWT
        String token = jwtService.generateToken(testUser);
        
        // When: Create a JwtService with different secret key
        JwtService differentKeyService = new JwtService();
        ReflectionTestUtils.setField(differentKeyService, "secretKey", "different-secret-key-that-will-not-match-original-signature-key");
        ReflectionTestUtils.setField(differentKeyService, "expirationTime", expirationTime);
        
        // Then: Token should be invalid with different key
        boolean isValid = differentKeyService.isValid(token);
        assertFalse(isValid, "Token with invalid signature should be rejected");
    }

    @Test
    void shouldRejectMalformedJwt() {
        // Given: Malformed JWT tokens
        String[] malformedTokens = {
            "not.a.valid.jwt.token",
            "invalid-token",
            "",
            "eyJhbGciOiJIUzI1NiJ9.invalid",
            "header.payload" // Missing signature
        };
        
        // When/Then: All malformed tokens should be invalid
        for (String malformedToken : malformedTokens) {
            boolean isValid = jwtService.isValid(malformedToken);
            assertFalse(isValid, "Malformed token should be invalid: " + malformedToken);
        }
    }
}
