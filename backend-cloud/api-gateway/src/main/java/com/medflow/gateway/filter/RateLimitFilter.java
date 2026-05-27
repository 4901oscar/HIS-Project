package com.medflow.gateway.filter;

import com.medflow.gateway.ratelimit.RateLimitBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter that limits requests per IP address.
 * Uses sliding window algorithm with automatic cleanup of old buckets.
 * Executes before JWT validation (order -200).
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Instant> lastAccessTime = new ConcurrentHashMap<>();
    
    private static final int MAX_REQUESTS = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Duration CLEANUP_THRESHOLD = Duration.ofMinutes(5);
    
    private Instant lastCleanup = Instant.now();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = extractClientIp(exchange);
        
        // Periodic cleanup of old buckets to prevent memory leaks
        cleanupOldBucketsIfNeeded();
        
        // Get or create bucket for this IP
        RateLimitBucket bucket = buckets.computeIfAbsent(clientIp, 
            k -> {
                logger.debug("Creating new rate limit bucket for IP: {}", k);
                return new RateLimitBucket(MAX_REQUESTS, WINDOW);
            });
        
        // Track last access time for cleanup
        lastAccessTime.put(clientIp, Instant.now());
        
        // Try to consume a request
        if (!bucket.tryConsume()) {
            logger.warn("Rate limit exceeded for IP: {} (limit: {} req/min)", clientIp, MAX_REQUESTS);
            return onRateLimitExceeded(exchange);
        }
        
        // Add rate limit headers
        addRateLimitHeaders(exchange.getResponse(), bucket);
        
        logger.debug("Request allowed for IP: {} (remaining: {})", clientIp, bucket.getRemaining());
        
        return chain.filter(exchange);
    }

    /**
     * Extracts client IP address from the request.
     * Checks X-Forwarded-For header first, then falls back to remote address.
     * 
     * @param exchange the server web exchange
     * @return the client IP address
     */
    private String extractClientIp(ServerWebExchange exchange) {
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Fall back to remote address
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        
        return "unknown";
    }
    
    /**
     * Adds rate limit headers to the response.
     * 
     * @param response the server HTTP response
     * @param bucket the rate limit bucket
     */
    private void addRateLimitHeaders(ServerHttpResponse response, RateLimitBucket bucket) {
        response.getHeaders().add("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS));
        response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(bucket.getRemaining()));
    }
    
    /**
     * Cleans up old buckets that haven't been accessed recently.
     * This prevents memory leaks from accumulating buckets for IPs that no longer make requests.
     */
    private void cleanupOldBucketsIfNeeded() {
        Instant now = Instant.now();
        
        // Only cleanup every 5 minutes
        if (now.isAfter(lastCleanup.plus(CLEANUP_THRESHOLD))) {
            logger.info("Starting cleanup of old rate limit buckets");
            
            int removedCount = 0;
            Instant cutoffTime = now.minus(CLEANUP_THRESHOLD);
            
            // Remove buckets not accessed in the last 5 minutes
            for (Map.Entry<String, Instant> entry : lastAccessTime.entrySet()) {
                if (entry.getValue().isBefore(cutoffTime)) {
                    String ip = entry.getKey();
                    buckets.remove(ip);
                    lastAccessTime.remove(ip);
                    removedCount++;
                }
            }
            
            logger.info("Cleanup completed: removed {} old buckets, {} active buckets remaining", 
                       removedCount, buckets.size());
            
            lastCleanup = now;
        }
    }

    /**
     * Returns 429 Too Many Requests error response.
     * 
     * @param exchange the server web exchange
     * @return a Mono that completes when the error response is written
     */
    private Mono<Void> onRateLimitExceeded(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        String errorJson = "{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded\"}";
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -200; // Execute before JWT validation (-100)
    }
}
