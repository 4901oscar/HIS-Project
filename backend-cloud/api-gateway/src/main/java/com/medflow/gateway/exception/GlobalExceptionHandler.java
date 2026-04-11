package com.medflow.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medflow.gateway.security.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

/**
 * Global exception handler for the API Gateway.
 * Handles all exceptions and returns consistent error responses.
 */
@Component
public class GlobalExceptionHandler implements WebExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private final ObjectMapper objectMapper;
    
    public GlobalExceptionHandler() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        String path = exchange.getRequest().getPath().value();
        
        // Determine status code and error type
        HttpStatus status = determineHttpStatus(ex);
        String errorType = determineErrorType(ex);
        String message = determineErrorMessage(ex);
        
        // Log the exception
        logException(ex, path, status);
        
        // Create error response using builder pattern
        ErrorResponse errorResponse = ErrorResponse.builder()
            .error(errorType)
            .message(message)
            .path(path)
            .build();
        
        // Set response status and content type
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        // Write error response as JSON
        return writeErrorResponse(exchange, errorResponse);
    }
    
    /**
     * Determine HTTP status code based on exception type.
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof JwtException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof RateLimitException) {
            return HttpStatus.TOO_MANY_REQUESTS;
        } else if (ex instanceof ServiceUnavailableException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * Determine error type string based on exception.
     */
    private String determineErrorType(Throwable ex) {
        if (ex instanceof JwtException) {
            return "Unauthorized";
        } else if (ex instanceof RateLimitException) {
            return "Too Many Requests";
        } else if (ex instanceof ServiceUnavailableException) {
            return "Service Unavailable";
        } else {
            return "Internal Server Error";
        }
    }
    
    /**
     * Determine error message based on exception.
     */
    private String determineErrorMessage(Throwable ex) {
        if (ex instanceof JwtException || 
            ex instanceof RateLimitException || 
            ex instanceof ServiceUnavailableException) {
            return ex.getMessage();
        } else {
            return "An unexpected error occurred";
        }
    }
    
    /**
     * Log exception with appropriate level based on status code.
     */
    private void logException(Throwable ex, String path, HttpStatus status) {
        if (status.is5xxServerError()) {
            logger.error("Server error on path {}: {}", path, ex.getMessage(), ex);
        } else if (status.is4xxClientError()) {
            logger.warn("Client error on path {}: {}", path, ex.getMessage());
        } else {
            logger.info("Exception on path {}: {}", path, ex.getMessage());
        }
    }
    
    /**
     * Write error response to the exchange.
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, ErrorResponse errorResponse) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize error response", e);
            return Mono.error(e);
        }
    }
}
