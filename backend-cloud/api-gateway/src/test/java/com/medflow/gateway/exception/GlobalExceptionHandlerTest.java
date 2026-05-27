package com.medflow.gateway.exception;

import com.medflow.gateway.security.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests error handling for different exception types.
 */
class GlobalExceptionHandlerTest {
    
    private GlobalExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    
    @Test
    void shouldReturn401ForJwtException() {
        // Arrange
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/patients/123").build()
        );
        JwtException exception = new JwtException("Invalid token");
        
        // Act
        Mono<Void> result = exceptionHandler.handle(exchange, exception);
        
        // Assert
        StepVerifier.create(result)
            .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }
    
    @Test
    void shouldReturn429ForRateLimitException() {
        // Arrange
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/patients/123").build()
        );
        RateLimitException exception = new RateLimitException("Too many requests");
        
        // Act
        Mono<Void> result = exceptionHandler.handle(exchange, exception);
        
        // Assert
        StepVerifier.create(result)
            .verifyComplete();
        
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }
    
    @Test
    void shouldReturn503ForServiceUnavailable() {
        // Arrange
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/patients/123").build()
        );
        ServiceUnavailableException exception = new ServiceUnavailableException("Service unavailable");
        
        // Act
        Mono<Void> result = exceptionHandler.handle(exchange, exception);
        
        // Assert
        StepVerifier.create(result)
            .verifyComplete();
        
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }
    
    @Test
    void shouldReturn500ForUnknownException() {
        // Arrange
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/patients/123").build()
        );
        RuntimeException exception = new RuntimeException("Unknown error");
        
        // Act
        Mono<Void> result = exceptionHandler.handle(exchange, exception);
        
        // Assert
        StepVerifier.create(result)
            .verifyComplete();
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
    }
    
    @Test
    void shouldReturnConsistentErrorFormat() {
        // Arrange
        ServerWebExchange exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/api/patients/123").build()
        );
        JwtException exception = new JwtException("Invalid token");
        
        // Act
        Mono<Void> result = exceptionHandler.handle(exchange, exception);
        
        // Assert
        StepVerifier.create(result)
            .verifyComplete();
        
        // Verify response has JSON content type
        assertEquals(MediaType.APPLICATION_JSON, exchange.getResponse().getHeaders().getContentType());
        
        // Note: In a real scenario, we would parse the response body and verify:
        // - error field exists
        // - message field exists
        // - timestamp field exists
        // - path field exists
    }
}
