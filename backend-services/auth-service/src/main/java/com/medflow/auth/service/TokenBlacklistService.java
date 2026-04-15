package com.medflow.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing blacklisted JWT tokens.
 * 
 * <p>This service provides an in-memory blacklist for invalidated tokens (logout).
 * Tokens are automatically cleaned up after expiration to prevent memory leaks.
 * 
 * <p>Implementation uses ConcurrentHashMap for thread-safe operations.
 * 
 * <p><strong>Note:</strong> This is a Phase 1 implementation using in-memory storage.
 * For production distributed systems, consider using Redis for shared blacklist state.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @since 2026-04-13
 */
@Service
public class TokenBlacklistService {
    
    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    
    /**
     * Thread-safe set for storing blacklisted tokens.
     * Uses ConcurrentHashMap.newKeySet() for concurrent access.
     */
    private final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    
    /**
     * Executor for scheduling token cleanup tasks.
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * Adds a token to the blacklist with default expiration (24 hours).
     * 
     * @param token the JWT token to blacklist
     */
    public void addToBlacklist(String token) {
        addToBlacklist(token, 24 * 60 * 60 * 1000L); // 24 hours in milliseconds
    }
    
    /**
     * Adds a token to the blacklist with custom expiration delay.
     * 
     * @param token the JWT token to blacklist
     * @param delayMillis delay in milliseconds before token is removed from blacklist
     */
    public void addToBlacklist(String token, long delayMillis) {
        blacklist.add(token);
        log.debug("Token added to blacklist. Total blacklisted tokens: {}", blacklist.size());
        
        // Schedule cleanup after token expiration
        scheduleCleanup(token, delayMillis);
    }
    
    /**
     * Checks if a token is blacklisted.
     * 
     * @param token the JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
    
    /**
     * Schedules automatic removal of a token from the blacklist.
     * 
     * <p>This prevents memory leaks by cleaning up expired tokens.
     * 
     * @param token the token to remove
     * @param delayMillis delay in milliseconds before removal
     */
    private void scheduleCleanup(String token, long delayMillis) {
        scheduler.schedule(() -> {
            blacklist.remove(token);
            log.debug("Token removed from blacklist after expiration. Remaining tokens: {}", 
                blacklist.size());
        }, delayMillis, TimeUnit.MILLISECONDS);
    }
}
