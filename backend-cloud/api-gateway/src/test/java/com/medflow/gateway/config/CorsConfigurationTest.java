package com.medflow.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Tests for CORS configuration.
 * Following TDD methodology (RED phase).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class CorsConfigurationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void shouldAllowLocalhostOrigin() {
        webTestClient.get()
                .uri("http://localhost:8080/actuator/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .exchange()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000")
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
    }

    @Test
    void shouldAllowAllMethods() {
        webTestClient.options()
                .uri("http://localhost:8080/actuator/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .exchange()
                .expectHeader().valueMatches(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        ".*GET.*POST.*PUT.*DELETE.*OPTIONS.*"
                );
    }

    @Test
    void shouldAllowAllHeaders() {
        webTestClient.options()
                .uri("http://localhost:8080/actuator/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type,Authorization")
                .exchange()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
    }

    @Test
    void shouldHandlePreflightRequest() {
        webTestClient.options()
                .uri("http://localhost:8080/actuator/health")
                .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000")
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
    }
}
