package com.medflow.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main application class for MedFlow API Gateway.
 * 
 * This gateway serves as the single entry point for all client requests,
 * providing:
 * - JWT authentication and authorization
 * - Dynamic service discovery via Eureka
 * - Rate limiting
 * - CORS handling
 * - Request routing to microservices
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
