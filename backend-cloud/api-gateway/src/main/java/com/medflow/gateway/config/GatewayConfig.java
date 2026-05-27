package com.medflow.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Gateway routing configuration.
 * 
 * Defines routes to all microservices using Eureka service discovery.
 * The 'lb://' prefix enables client-side load balancing through Eureka.
 * 
 * Route mappings:
 * - /api/auth/**      → AUTH-SERVICE (port 8081)
 * - /api/patients/**  → PATIENT-SERVICE (port 8082)
 * - /api/clinical/**  → CLINICAL-SERVICE (port 8083)
 * - /api/lab/**       → LAB-SERVICE (port 8084)
 * - /api/pharmacy/**  → PHARMACY-SERVICE (port 8085)
 * - /api/billing/**   → BILLING-SERVICE (port 8086)
 * 
 * Note: This configuration is disabled in test profile.
 * Test routes are configured in application-test.yml to point to WireMock.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Configuration
@Profile("!test")
public class GatewayConfig {

    // Route IDs
    private static final String AUTH_ROUTE_ID = "auth-service";
    private static final String PATIENT_ROUTE_ID = "patient-service";
    private static final String CLINICAL_ROUTE_ID = "clinical-service";
    private static final String LAB_ROUTE_ID = "lab-service";
    private static final String PHARMACY_ROUTE_ID = "pharmacy-service";
    private static final String BILLING_ROUTE_ID = "billing-service";

    // Path patterns
    private static final String AUTH_PATH = "/api/auth/**";
    private static final String USERS_PATH = "/api/users/**";
    private static final String PATIENT_PATH = "/api/patients/**";
    private static final String CLINICAL_PATH = "/api/clinical/**";
    private static final String LAB_PATH = "/api/lab/**";
    private static final String PHARMACY_PATH = "/api/pharmacy/**";
    private static final String BILLING_PATH = "/api/billing/**";

    // Service URIs (lb:// prefix for load balancing via Eureka)
    private static final String AUTH_SERVICE_URI = "lb://AUTH-SERVICE";
    private static final String PATIENT_SERVICE_URI = "lb://PATIENT-SERVICE";
    private static final String CLINICAL_SERVICE_URI = "lb://CLINICAL-SERVICE";
    private static final String LAB_SERVICE_URI = "lb://LAB-SERVICE";
    private static final String PHARMACY_SERVICE_URI = "lb://PHARMACY-SERVICE";
    private static final String BILLING_SERVICE_URI = "lb://BILLING-SERVICE";

    /**
     * Configures routes for all microservices.
     * 
     * Each route:
     * 1. Matches requests by path pattern
     * 2. Resolves service location via Eureka
     * 3. Load balances across multiple instances (if available)
     * 
     * @param builder RouteLocatorBuilder provided by Spring Cloud Gateway
     * @return Configured RouteLocator with all service routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Authentication Service - Handles user login, registration, and JWT generation
                .route(AUTH_ROUTE_ID, r -> r
                        .path(AUTH_PATH)
                        .uri(AUTH_SERVICE_URI))

                // User Management - Employee CRUD (also handled by auth-service)
                .route("user-management", r -> r
                        .path(USERS_PATH)
                        .uri(AUTH_SERVICE_URI))
                
                // Patient Service - Manages patient records and demographics
                .route(PATIENT_ROUTE_ID, r -> r
                        .path(PATIENT_PATH)
                        .uri(PATIENT_SERVICE_URI))
                
                // Clinical Service - Handles appointments, consultations, and medical records
                .route(CLINICAL_ROUTE_ID, r -> r
                        .path(CLINICAL_PATH)
                        .uri(CLINICAL_SERVICE_URI))
                
                // Lab Service - Manages laboratory orders and results
                .route(LAB_ROUTE_ID, r -> r
                        .path(LAB_PATH)
                        .uri(LAB_SERVICE_URI))
                
                // Pharmacy Service - Handles medication prescriptions and inventory
                .route(PHARMACY_ROUTE_ID, r -> r
                        .path(PHARMACY_PATH)
                        .uri(PHARMACY_SERVICE_URI))
                
                // Billing Service - Manages invoices, payments, and insurance claims
                .route(BILLING_ROUTE_ID, r -> r
                        .path(BILLING_PATH)
                        .uri(BILLING_SERVICE_URI))
                
                .build();
    }
}
