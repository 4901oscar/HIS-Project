package com.medflow.gateway.ratelimit;

import java.time.Duration;
import java.time.Instant;

/**
 * Rate limit bucket using sliding window algorithm.
 * Tracks request count per time window and resets when window expires.
 */
public class RateLimitBucket {

    private final int maxRequests;
    private final Duration window;
    private int remaining;
    private Instant windowStart;

    public RateLimitBucket(int maxRequests, Duration window) {
        this.maxRequests = maxRequests;
        this.window = window;
        this.remaining = maxRequests;
        this.windowStart = Instant.now();
    }

    /**
     * Attempts to consume one request from the bucket.
     * 
     * @return true if request is allowed, false if rate limit exceeded
     */
    public synchronized boolean tryConsume() {
        resetIfNeeded();
        
        if (remaining > 0) {
            remaining--;
            return true;
        }
        
        return false;
    }

    /**
     * Gets the number of remaining requests in the current window.
     * 
     * @return number of remaining requests
     */
    public synchronized int getRemaining() {
        resetIfNeeded();
        return remaining;
    }

    /**
     * Resets the bucket if the time window has expired.
     */
    private void resetIfNeeded() {
        if (Instant.now().isAfter(windowStart.plus(window))) {
            remaining = maxRequests;
            windowStart = Instant.now();
        }
    }
}
