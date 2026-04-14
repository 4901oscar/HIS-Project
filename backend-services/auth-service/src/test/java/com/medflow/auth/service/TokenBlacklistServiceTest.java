package com.medflow.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TokenBlacklistService.
 * 
 * Tests the token blacklist functionality including:
 * - Adding tokens to blacklist
 * - Detecting blacklisted tokens
 * - Cleanup of expired tokens
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
class TokenBlacklistServiceTest {
    
    private TokenBlacklistService blacklistService;
    
    @BeforeEach
    void setUp() {
        blacklistService = new TokenBlacklistService();
    }
    
    @Test
    void shouldAddTokenToBlacklist() {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        
        // When
        blacklistService.addToBlacklist(token);
        
        // Then
        assertTrue(blacklistService.isBlacklisted(token), 
            "Token should be in blacklist after adding");
    }
    
    @Test
    void shouldDetectBlacklistedToken() {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.blacklisted.token";
        blacklistService.addToBlacklist(token);
        
        // When
        boolean isBlacklisted = blacklistService.isBlacklisted(token);
        
        // Then
        assertTrue(isBlacklisted, "Should detect blacklisted token");
    }
    
    @Test
    void shouldNotDetectNonBlacklistedToken() {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.token";
        
        // When
        boolean isBlacklisted = blacklistService.isBlacklisted(token);
        
        // Then
        assertFalse(isBlacklisted, "Should not detect non-blacklisted token");
    }
    
    @Test
    void shouldCleanupExpiredTokens() throws InterruptedException {
        // Given
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.token";
        long shortDelay = 100; // 100ms for testing
        
        // When
        blacklistService.addToBlacklist(token, shortDelay);
        assertTrue(blacklistService.isBlacklisted(token), 
            "Token should be blacklisted initially");
        
        // Wait for cleanup
        Thread.sleep(200); // Wait longer than delay
        
        // Then
        assertFalse(blacklistService.isBlacklisted(token), 
            "Token should be removed from blacklist after expiration");
    }
}
