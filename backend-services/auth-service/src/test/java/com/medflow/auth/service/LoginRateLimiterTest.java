package com.medflow.auth.service;

import com.medflow.auth.exception.TooManyAttemptsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginRateLimiter.
 * 
 * <p>Tests rate limiting functionality:
 * <ul>
 *   <li>Allow requests under 5 attempts</li>
 *   <li>Block requests after 5 attempts</li>
 *   <li>Reset attempts after successful login</li>
 *   <li>Track attempts per IP address</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
class LoginRateLimiterTest {
    
    private LoginRateLimiter rateLimiter;
    
    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }
    
    /**
     * Test that requests under 5 attempts are allowed.
     */
    @Test
    void shouldAllowUnder5Attempts() {
        String ipAddress = "192.168.1.1";
        
        // Should allow first 5 attempts
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
    }
    
    /**
     * Test that requests are blocked after 5 attempts.
     */
    @Test
    void shouldBlockAfter5Attempts() {
        String ipAddress = "192.168.1.2";
        
        // Make 5 attempts
        for (int i = 0; i < 5; i++) {
            rateLimiter.checkRateLimit(ipAddress);
        }
        
        // 6th attempt should throw exception
        assertThrows(TooManyAttemptsException.class, 
            () -> rateLimiter.checkRateLimit(ipAddress));
    }
    
    /**
     * Test that attempts are reset after successful login.
     */
    @Test
    void shouldResetAfterSuccessfulLogin() {
        String ipAddress = "192.168.1.3";
        
        // Make 3 attempts
        rateLimiter.checkRateLimit(ipAddress);
        rateLimiter.checkRateLimit(ipAddress);
        rateLimiter.checkRateLimit(ipAddress);
        
        // Reset after successful login
        rateLimiter.resetAttempts(ipAddress);
        
        // Should allow 5 more attempts
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress));
    }
    
    /**
     * Test that attempts are tracked per IP address.
     */
    @Test
    void shouldTrackPerIpAddress() {
        String ipAddress1 = "192.168.1.4";
        String ipAddress2 = "192.168.1.5";
        
        // Make 5 attempts from IP 1
        for (int i = 0; i < 5; i++) {
            rateLimiter.checkRateLimit(ipAddress1);
        }
        
        // IP 1 should be blocked
        assertThrows(TooManyAttemptsException.class, 
            () -> rateLimiter.checkRateLimit(ipAddress1));
        
        // IP 2 should still be allowed
        assertDoesNotThrow(() -> rateLimiter.checkRateLimit(ipAddress2));
    }
}
