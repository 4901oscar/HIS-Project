package com.medflow.gateway.filter;

import com.medflow.gateway.security.JwtException;
import com.medflow.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter following TDD methodology.
 * These tests are written FIRST (RED phase) before implementation.
 * 
 * Test Coverage:
 * - Public paths (no JWT required)
 * - Missing token (401)
 * - Invalid token (401)
 * - Expired token (401)
 * - Valid token (add headers)
 * - Request ID header
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtValidator jwtValidator;

    @Mock
    private GatewayFilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtValidator);
        String secretKey = "test-secret-key-for-jwt-validation-must-be-at-least-256-bits-long";
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    @Test
    void shouldAllowPublicPaths() {
        // Arrange - Test all public paths
        String[] publicPaths = {
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/health"
        };

        // Mock filter chain to return empty Mono
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        for (String path : publicPaths) {
            MockServerHttpRequest request = MockServerHttpRequest.get(path).build();
            ServerWebExchange exchange = MockServerWebExchange.from(request);

            // Act
            Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

            // Assert
            StepVerifier.create(result)
                    .verifyComplete();

            // Verify filter chain was called (request passed through)
            verify(filterChain, atLeastOnce()).filter(any(ServerWebExchange.class));
            
            // Verify JWT validator was NOT called for public paths
            verify(jwtValidator, never()).validateAndGetClaims(anyString());
        }
    }

    @Test
    void shouldReject401WhenTokenMissing() {
        // Arrange - Protected path without Authorization header
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients/123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify response status is 401
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        
        // Verify filter chain was NOT called
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldReject401WhenTokenInvalid() {
        // Arrange - Invalid token format
        String invalidToken = "invalid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock validator to throw exception for invalid token
        when(jwtValidator.validateAndGetClaims(invalidToken))
                .thenThrow(new JwtException("Invalid token"));

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify response status is 401
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        
        // Verify filter chain was NOT called
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldReject401WhenTokenExpired() {
        // Arrange - Expired token
        String expiredToken = generateExpiredToken();
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock validator to throw exception for expired token
        when(jwtValidator.validateAndGetClaims(expiredToken))
                .thenThrow(new JwtException("Token expired"));

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify response status is 401
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        
        // Verify filter chain was NOT called
        verify(filterChain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldAddUserIdHeaderWhenTokenValid() {
        // Arrange - Valid token
        String validToken = generateValidToken("123", "DOCTOR");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/patients/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock validator to return valid claims
        Claims mockClaims = createMockClaims("123", "DOCTOR");
        when(jwtValidator.validateAndGetClaims(validToken)).thenReturn(mockClaims);
        
        // Mock filter chain to return empty Mono
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify X-User-Id header was added
        verify(filterChain).filter(argThat(modifiedExchange -> {
            String userId = modifiedExchange.getRequest().getHeaders().getFirst("X-User-Id");
            return "123".equals(userId);
        }));
    }

    @Test
    void shouldAddUserRolesHeaderWhenTokenValid() {
        // Arrange - Valid token with roles
        String validToken = generateValidToken("456", "DOCTOR,ADMIN");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/clinical/appointments")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock validator to return valid claims
        Claims mockClaims = createMockClaims("456", "DOCTOR,ADMIN");
        when(jwtValidator.validateAndGetClaims(validToken)).thenReturn(mockClaims);
        
        // Mock filter chain to return empty Mono
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify X-User-Roles header was added
        verify(filterChain).filter(argThat(modifiedExchange -> {
            String roles = modifiedExchange.getRequest().getHeaders().getFirst("X-User-Roles");
            return "DOCTOR,ADMIN".equals(roles);
        }));
    }

    @Test
    void shouldAddRequestIdHeader() {
        // Arrange - Valid token
        String validToken = generateValidToken("789", "NURSE");
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/lab/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Mock validator to return valid claims
        Claims mockClaims = createMockClaims("789", "NURSE");
        when(jwtValidator.validateAndGetClaims(validToken)).thenReturn(mockClaims);
        
        // Mock filter chain to return empty Mono
        when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        // Verify X-Request-Id header was added and is not null
        verify(filterChain).filter(argThat(modifiedExchange -> {
            String requestId = modifiedExchange.getRequest().getHeaders().getFirst("X-Request-Id");
            return requestId != null && !requestId.isEmpty();
        }));
    }

    // Helper methods

    private String generateValidToken(String userId, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateExpiredToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("roles", "DOCTOR");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("123")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago (expired)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims createMockClaims(String userId, String roles) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("userId", userId);
        claimsMap.put("roles", roles);

        return Jwts.claims(claimsMap);
    }
}
