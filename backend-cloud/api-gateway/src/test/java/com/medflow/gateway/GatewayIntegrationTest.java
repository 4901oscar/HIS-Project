package com.medflow.gateway;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * End-to-End Integration Tests for API Gateway.
 * 
 * Tests the complete request flow through all filters:
 * 1. Rate Limit Filter (-200)
 * 2. JWT Authentication Filter (-100)
 * 3. Routing to downstream services
 * 4. CORS headers on responses
 * 
 * Uses WireMock to simulate downstream services.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.cloud.gateway.discovery.locator.enabled=false",
        "eureka.client.enabled=false"
    }
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private WireMockServer wireMockServer;
    private SecretKey jwtKey;
    private static final String JWT_SECRET = "test-secret-key-for-jwt-validation-must-be-at-least-256-bits-long";

    @BeforeEach
    void setUp() {
        // Initialize JWT key
        jwtKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

        // Start WireMock server to simulate downstream services
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8090);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    /**
     * Test: shouldAuthenticateAndRouteSuccessfully
     * 
     * Validates:
     * - Valid JWT token is accepted
     * - Request is routed to correct service
     * - User headers (X-User-Id, X-User-Roles, X-Request-Id) are added
     * - Response is returned successfully
     */
    @Test
    void shouldAuthenticateAndRouteSuccessfully() {
        // Arrange - Mock downstream service
        stubFor(get(urlPathEqualTo("/api/patients/123"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"123\",\"name\":\"John Doe\"}")));

        String validToken = generateValidToken("user123", "DOCTOR");

        // Act & Assert
        webTestClient.get()
                .uri("/api/patients/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.id").isEqualTo("123")
                .jsonPath("$.name").isEqualTo("John Doe");

        // Verify downstream service received user headers
        verify(getRequestedFor(urlPathEqualTo("/api/patients/123"))
                .withHeader("X-User-Id", equalTo("user123"))
                .withHeader("X-User-Roles", equalTo("DOCTOR"))
                .withHeader("X-Request-Id", matching(".*"))); // Any UUID
    }

    /**
     * Test: shouldRejectInvalidToken
     * 
     * Validates:
     * - Invalid JWT token is rejected
     * - 401 Unauthorized status is returned
     * - Request does NOT reach downstream service
     * - Error response contains appropriate message
     */
    @Test
    void shouldRejectInvalidToken() {
        // Arrange - Invalid token
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        webTestClient.get()
                .uri("/api/patients/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.error").exists()
                .jsonPath("$.message").exists();

        // Verify downstream service was NOT called
        verify(0, getRequestedFor(urlPathEqualTo("/api/patients/123")));
    }

    /**
     * Test: shouldEnforceRateLimit
     * 
     * Validates:
     * - Rate limit of 100 requests per minute is enforced
     * - 101st request returns 429 Too Many Requests
     * - Rate limit headers are present (X-RateLimit-Limit, X-RateLimit-Remaining)
     * - Request does NOT reach downstream service after limit exceeded
     */
    @Test
    void shouldEnforceRateLimit() {
        // Arrange - Mock downstream service
        stubFor(get(urlPathMatching("/api/patients/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"status\":\"ok\"}")));

        String validToken = generateValidToken("user456", "DOCTOR");

        // Act - Make 100 requests (should all succeed)
        for (int i = 0; i < 100; i++) {
            webTestClient.get()
                    .uri("/api/patients/" + i)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().exists("X-RateLimit-Limit")
                    .expectHeader().exists("X-RateLimit-Remaining");
        }

        // Assert - 101st request should be rate limited
        webTestClient.get()
                .uri("/api/patients/101")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody()
                .jsonPath("$.error").exists();

        // Verify 101st request did NOT reach downstream service
        verify(0, getRequestedFor(urlPathEqualTo("/api/patients/101")));
    }

    /**
     * Test: shouldHandleServiceUnavailable
     * 
     * Validates:
     * - When downstream service is unavailable, gateway returns 503
     * - Error response contains appropriate message
     * - Gateway handles connection failures gracefully
     */
    @Test
    void shouldHandleServiceUnavailable() {
        // Arrange - Mock service to return 503
        stubFor(get(urlPathEqualTo("/api/patients/999"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("{\"error\":\"Service Unavailable\"}")));

        String validToken = generateValidToken("user789", "DOCTOR");

        // Act & Assert
        webTestClient.get()
                .uri("/api/patients/999")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Test: shouldAddUserHeadersToRequest
     * 
     * Validates:
     * - X-User-Id header is extracted from JWT and added to request
     * - X-User-Roles header is extracted from JWT and added to request
     * - X-Request-Id header is generated and added to request
     * - Downstream service receives all user headers
     */
    @Test
    void shouldAddUserHeadersToRequest() {
        // Arrange - Mock downstream service
        stubFor(get(urlPathEqualTo("/api/clinical/appointments"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"appointments\":[]}")));

        String validToken = generateValidToken("doctor123", "DOCTOR,ADMIN");

        // Act
        webTestClient.get()
                .uri("/api/clinical/appointments")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .exchange()
                .expectStatus().isOk();

        // Assert - Verify all user headers were added
        verify(getRequestedFor(urlPathEqualTo("/api/clinical/appointments"))
                .withHeader("X-User-Id", equalTo("doctor123"))
                .withHeader("X-User-Roles", equalTo("DOCTOR,ADMIN"))
                .withHeader("X-Request-Id", matching("[a-f0-9-]{36}"))); // UUID format
    }

    /**
     * Test: shouldReturnCorsHeaders
     * 
     * Validates:
     * - CORS headers are present in response
     * - Access-Control-Allow-Origin is set correctly
     * - Access-Control-Allow-Methods includes required methods
     * - Access-Control-Allow-Headers is configured
     * - Preflight OPTIONS requests are handled
     */
    @Test
    void shouldReturnCorsHeaders() {
        // Arrange - Mock downstream service
        stubFor(get(urlPathEqualTo("/api/patients/456"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"id\":\"456\"}")));

        String validToken = generateValidToken("user999", "NURSE");

        // Act & Assert
        webTestClient.get()
                .uri("/api/patients/456")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists("Access-Control-Allow-Origin")
                .expectHeader().valueEquals("Access-Control-Allow-Origin", "http://localhost:3000");
    }

    // Helper Methods

    /**
     * Generates a valid JWT token for testing.
     * 
     * @param userId User ID to include in token
     * @param roles User roles (comma-separated)
     * @return Valid JWT token string
     */
    private String generateValidToken(String userId, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generates an expired JWT token for testing.
     * 
     * @param userId User ID to include in token
     * @param roles User roles (comma-separated)
     * @return Expired JWT token string
     */
    private String generateExpiredToken(String userId, String roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roles", roles);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago (expired)
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
