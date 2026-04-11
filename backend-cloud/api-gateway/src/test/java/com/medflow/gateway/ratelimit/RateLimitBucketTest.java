package com.medflow.gateway.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitBucketTest {

    private RateLimitBucket bucket;
    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    @BeforeEach
    void setUp() {
        bucket = new RateLimitBucket(MAX_REQUESTS, WINDOW);
    }

    @Test
    void shouldAllowRequestsUnderLimit() {
        // Consume 50 requests (under limit of 100)
        for (int i = 0; i < 50; i++) {
            assertTrue(bucket.tryConsume(), "Request " + (i + 1) + " should be allowed");
        }
        
        // Verify remaining requests
        assertEquals(50, bucket.getRemaining());
    }

    @Test
    void shouldRejectRequestsOverLimit() {
        // Consume all 100 requests
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(bucket.tryConsume(), "Request " + (i + 1) + " should be allowed");
        }
        
        // 101st request should be rejected
        assertFalse(bucket.tryConsume(), "Request 101 should be rejected");
        assertEquals(0, bucket.getRemaining());
    }

    @Test
    void shouldResetAfterTimeWindow() throws InterruptedException {
        // Create bucket with short window for testing
        RateLimitBucket shortWindowBucket = new RateLimitBucket(5, Duration.ofSeconds(1));
        
        // Consume all 5 requests
        for (int i = 0; i < 5; i++) {
            assertTrue(shortWindowBucket.tryConsume());
        }
        
        // 6th request should be rejected
        assertFalse(shortWindowBucket.tryConsume());
        assertEquals(0, shortWindowBucket.getRemaining());
        
        // Wait for window to expire
        Thread.sleep(1100);
        
        // After reset, requests should be allowed again
        assertTrue(shortWindowBucket.tryConsume(), "Request should be allowed after window reset");
        assertEquals(4, shortWindowBucket.getRemaining());
    }

    @Test
    void shouldTrackRemainingRequests() {
        // Initially should have max requests available
        assertEquals(MAX_REQUESTS, bucket.getRemaining());
        
        // Consume 10 requests
        for (int i = 0; i < 10; i++) {
            bucket.tryConsume();
        }
        
        // Should have 90 remaining
        assertEquals(90, bucket.getRemaining());
        
        // Consume 30 more
        for (int i = 0; i < 30; i++) {
            bucket.tryConsume();
        }
        
        // Should have 60 remaining
        assertEquals(60, bucket.getRemaining());
    }
}
