package com.medflow.gateway.filter;

import com.medflow.gateway.security.JwtException;
import com.medflow.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * JWT Authentication Filter for API Gateway.
 * 
 * <p>This filter validates JWT tokens for all incoming requests except public paths.
 * It performs the following operations:
 * <ol>
 *   <li>Checks if the request path is public (skip validation)</li>
 *   <li>Extracts JWT token from Authorization header</li>
 *   <li>Validates the token using JwtValidator</li>
 *   <li>Adds user information headers (X-User-Id, X-User-Roles)</li>
 *   <li>Adds request tracking header (X-Request-Id)</li>
 *   <li>Returns 401 Unauthorized if validation fails</li>
 * </ol>
 * 
 * <p>Filter order is set to -100 to execute before routing filters.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 * @see JwtValidator
 * @see GlobalFilter
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtValidator jwtValidator;
    
    /**
     * List of public paths that do not require JWT authentication.
     */
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/activate",
            "/actuator/health"
    );

    /**
     * Constructs a new JwtAuthenticationFilter.
     *
     * @param jwtValidator the JWT validator component
     */
    public JwtAuthenticationFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    /**
     * Filters incoming requests to validate JWT tokens.
     *
     * @param exchange the current server exchange
     * @param chain the gateway filter chain
     * @return a Mono that indicates when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String method = exchange.getRequest().getMethod().toString();

        logger.debug("Processing request: {} {}", method, path);

        // Skip JWT validation for public paths
        if (isPublicPath(path)) {
            logger.debug("Public path accessed, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        // Extract JWT token from Authorization header
        String token = extractToken(exchange.getRequest());

        // Return 401 if token is missing
        if (token == null) {
            logger.warn("Authentication failed: Token not provided for path: {}", path);
            return onError(exchange, "Token not provided", HttpStatus.UNAUTHORIZED);
        }

        // Validate JWT token
        try {
            Claims claims = jwtValidator.validateAndGetClaims(token);
            String userId = claims.get("userId", String.class);
            String roles = claims.get("roles", String.class);

            logger.info("Authentication successful: userId={}, roles={}, path={}", userId, roles, path);

            // Add user info headers to the request
            String requestId = UUID.randomUUID().toString();
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Roles", roles)
                    .header("X-Request-Id", requestId)
                    .build();

            logger.debug("Added headers: X-User-Id={}, X-User-Roles={}, X-Request-Id={}", 
                    userId, roles, requestId);

            // Continue with modified request
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (JwtException e) {
            // Return 401 if token validation fails
            logger.warn("Authentication failed: {} for path: {}", e.getMessage(), path);
            return onError(exchange, "Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Returns the filter order. Lower values have higher priority.
     * This filter runs at -100 to execute before routing filters.
     *
     * @return the filter order (-100)
     */
    @Override
    public int getOrder() {
        return -100;
    }

    /**
     * Checks if the given path is a public path that doesn't require authentication.
     *
     * @param path the request path
     * @return true if the path is public, false otherwise
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Extracts the JWT token from the Authorization header.
     * Expected format: "Bearer <token>"
     *
     * @param request the server HTTP request
     * @return the extracted token, or null if not found or invalid format
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }

    /**
     * Creates an error response with the given message and status code.
     *
     * @param exchange the server web exchange
     * @param message the error message
     * @param status the HTTP status code
     * @return a Mono that writes the error response
     */
    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String errorJson = String.format("{\"error\":\"%s\",\"message\":\"%s\"}", 
                status.getReasonPhrase(), message);
        
        byte[] bytes = errorJson.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);

        return response.writeWith(Mono.just(buffer));
    }
}
