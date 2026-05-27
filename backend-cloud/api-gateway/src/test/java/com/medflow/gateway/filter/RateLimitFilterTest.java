package com.medflow.gateway.filter;

import com.medflow.gateway.ratelimit.RateLimitBucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowRequestUnderLimit() {
        // Create request with unique IP
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients/123")
                .remoteAddress(new java.net.InetSocketAddress("192.168.1.100", 8080))
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // Execute filter
        Mono<Void> result = rateLimitFilter.filter(exchange, chain);
        
        // Verify request is allowed
        StepVerifier.create(result)
                .verifyComplete();
        
        // Verify chain was called (request passed through)
        verify(chain, times(1)).filter(any());
        
        // Verify response status is not set (no error)
        assertNull(exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldReturn429WhenLimitExceeded() {
        // Create request with same IP
        String clientIp = "192.168.1.101";
        
        // Consume all 100 requests
        for (int i = 0; i < 100; i++) {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/patients/" + i)
                    .remoteAddress(new java.net.InetSocketAddress(clientIp, 8080))
                    .build();
            
            ServerWebExchange exchange = MockServerWebExchange.from(request);
            rateLimitFilter.filter(exchange, chain).block();
        }
        
        // 101st request should be rejected
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients/101")
                .remoteAddress(new java.net.InetSocketAddress(clientIp, 8080))
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        Mono<Void> result = rateLimitFilter.filter(exchange, chain);
        
        // Verify request is rejected with 429
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
    }

    @Test
    void shouldAddRateLimitHeaders() {
        // Create request
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients/123")
                .remoteAddress(new java.net.InetSocketAddress("192.168.1.102", 8080))
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        // Execute filter
        rateLimitFilter.filter(exchange, chain).block();
        
        // Verify rate limit headers are present
        assertNotNull(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit"));
        assertNotNull(exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining"));
        
        // Verify header values
        assertEquals("100", exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit"));
        assertEquals("99", exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining"));
    }

    @Test
    void shouldTrackPerIpAddress() {
        String ip1 = "192.168.1.103";
        String ip2 = "192.168.1.104";
        
        // IP1: Consume 50 requests
        for (int i = 0; i < 50; i++) {
            MockServerHttpRequest request = MockServerHttpRequest
                    .get("/api/patients/" + i)
                    .remoteAddress(new java.net.InetSocketAddress(ip1, 8080))
                    .build();
            
            ServerWebExchange exchange = MockServerWebExchange.from(request);
            rateLimitFilter.filter(exchange, chain).block();
        }
        
        // IP2: Should still have 100 requests available
        MockServerHttpRequest request2 = MockServerHttpRequest
                .get("/api/patients/123")
                .remoteAddress(new java.net.InetSocketAddress(ip2, 8080))
                .build();
        
        ServerWebExchange exchange2 = MockServerWebExchange.from(request2);
        rateLimitFilter.filter(exchange2, chain).block();
        
        // Verify IP2 has 99 remaining (not affected by IP1)
        assertEquals("99", exchange2.getResponse().getHeaders().getFirst("X-RateLimit-Remaining"));
        
        // Verify IP1 has 50 remaining
        MockServerHttpRequest request1 = MockServerHttpRequest
                .get("/api/patients/999")
                .remoteAddress(new java.net.InetSocketAddress(ip1, 8080))
                .build();
        
        ServerWebExchange exchange1 = MockServerWebExchange.from(request1);
        rateLimitFilter.filter(exchange1, chain).block();
        
        assertEquals("49", exchange1.getResponse().getHeaders().getFirst("X-RateLimit-Remaining"));
    }
}
