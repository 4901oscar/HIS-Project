package com.medflow.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Gateway routing configuration.
 * Tests verify that routes are correctly configured for all microservices.
 * 
 * Note: These tests verify route configuration, not actual routing behavior.
 * Actual routing requires the services to be running and registered with Eureka.
 */
@SpringBootTest
@ActiveProfiles("test")
class GatewayRoutingTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void shouldRouteToAuthService() {
        assertThat(routeLocator.getRoutes()
                .filter(route -> route.getId().equals("auth-service"))
                .blockFirst())
                .isNotNull()
                .satisfies(route -> {
                    assertThat(route.getUri().toString()).isEqualTo("lb://AUTH-SERVICE");
                    assertThat(route.getPredicate().toString()).contains("/api/auth/**");
                });
    }

    @Test
    void shouldRouteToPatientService() {
        assertThat(routeLocator.getRoutes()
                .filter(route -> route.getId().equals("patient-service"))
                .blockFirst())
                .isNotNull()
                .satisfies(route -> {
                    assertThat(route.getUri().toString()).isEqualTo("lb://PATIENT-SERVICE");
                    assertThat(route.getPredicate().toString()).contains("/api/patients/**");
                });
    }

    @Test
    void shouldRouteToClinicalService() {
        assertThat(routeLocator.getRoutes()
                .filter(route -> route.getId().equals("clinical-service"))
                .blockFirst())
                .isNotNull()
                .satisfies(route -> {
                    assertThat(route.getUri().toString()).isEqualTo("lb://CLINICAL-SERVICE");
                    assertThat(route.getPredicate().toString()).contains("/api/clinical/**");
                });
    }

    @Test
    void shouldRouteToLabService() {
        assertThat(routeLocator.getRoutes()
                .filter(route -> route.getId().equals("lab-service"))
                .blockFirst())
                .isNotNull()
                .satisfies(route -> {
                    assertThat(route.getUri().toString()).isEqualTo("lb://LAB-SERVICE");
                    assertThat(route.getPredicate().toString()).contains("/api/lab/**");
                });
    }

    @Test
    void shouldRouteToPharmacyService() {
        assertThat(routeLocator.getRoutes()
                .filter(route -> route.getId().equals("pharmacy-service"))
                .blockFirst())
                .isNotNull()
                .satisfies(route -> {
                    assertThat(route.getUri().toString()).isEqualTo("lb://PHARMACY-SERVICE");
                    assertThat(route.getPredicate().toString()).contains("/api/pharmacy/**");
                });
    }

    @Test
    void shouldRouteToBillingService() {
        assertThat(routeLocator.getRoutes()
                .filter(route -> route.getId().equals("billing-service"))
                .blockFirst())
                .isNotNull()
                .satisfies(route -> {
                    assertThat(route.getUri().toString()).isEqualTo("lb://BILLING-SERVICE");
                    assertThat(route.getPredicate().toString()).contains("/api/billing/**");
                });
    }

    @Test
    void shouldHaveAllSixRoutes() {
        long routeCount = routeLocator.getRoutes()
                .filter(route -> route.getId().matches("^(auth|patient|clinical|lab|pharmacy|billing)-service$"))
                .count()
                .block();
        
        assertThat(routeCount).as("Should have exactly 6 configured routes").isEqualTo(6);
    }
}
