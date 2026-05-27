package com.medflow.auth.service;

import com.medflow.auth.exception.TooManyAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter for login attempts.
 * 
 * <p>This component tracks login attempts per IP address and enforces
 * a rate limit of 5 attempts per minute. After 5 failed attempts,
 * subsequent attempts are blocked until the counter is reset.
 * 
 * <p>Features:
 * <ul>
 *   <li>Track attempts per IP address</li>
 *   <li>Limit to 5 attempts per minute</li>
 *   <li>Reset on successful login</li>
 *   <li>Thread-safe using ConcurrentHashMap</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@Component
public class LoginRateLimiter {
    
    private static final Logger log = LoggerFactory.getLogger(LoginRateLimiter.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_DURATION_MILLIS = 60_000; // 1 minute
    
    private final Map<String, AttemptTracker> attempts = new ConcurrentHashMap<>();
    
    /**
     * Checks if the rate limit has been exceeded for the given IP address.
     * 
     * @param ipAddress the IP address to check
     * @throws TooManyAttemptsException if rate limit exceeded
     */
    public void checkRateLimit(String ipAddress) {
        AttemptTracker tracker = attempts.computeIfAbsent(ipAddress, k -> new AttemptTracker());
        
        // Clean up expired attempts
        tracker.cleanupExpired();
        
        if (tracker.getAttempts() >= MAX_ATTEMPTS) {
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
            throw new TooManyAttemptsException("Too many login attempts. Please try again later.");
        }
        
        tracker.increment();
        log.debug("Login attempt recorded for IP: {}. Count: {}", ipAddress, tracker.getAttempts());
    }
    
    /**
     * Resets the attempt counter for the given IP address.
     * Called after successful login.
     * 
     * @param ipAddress the IP address to reset
     */
    public void resetAttempts(String ipAddress) {
        attempts.remove(ipAddress);
        log.debug("Login attempts reset for IP: {}", ipAddress);
    }
    
    /**
     * Clears all attempt trackers.
     * Used for testing purposes to reset state between tests.
     */
    public void clearAll() {
        attempts.clear();
        log.debug("All login attempts cleared");
    }
    
    /**
     * Internal class to track login attempts with timestamps.
     */
    private static class AttemptTracker {
        private int count = 0;
        private long windowStart = System.currentTimeMillis();
        
        public synchronized void increment() {
            count++;
        }
        
        public synchronized int getAttempts() {
            return count;
        }
        
        public synchronized void cleanupExpired() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_DURATION_MILLIS) {
                // Reset counter if window expired
                count = 0;
                windowStart = now;
            }
        }
    }
}
