package com.medflow.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) configuration for API Gateway.
 * 
 * <p>This configuration allows the frontend application to make cross-origin requests
 * to the API Gateway. CORS is a security feature implemented by browsers to prevent
 * malicious websites from accessing resources from different origins.</p>
 * 
 * <p>Configuration includes:</p>
 * <ul>
 *   <li>Allowed origins: Configurable list of frontend URLs (default: http://localhost:3000)</li>
 *   <li>Allowed methods: GET, POST, PUT, DELETE, OPTIONS</li>
 *   <li>Allowed headers: All headers (*)</li>
 *   <li>Credentials: Enabled (allows cookies and authorization headers)</li>
 *   <li>Max age: 3600 seconds (preflight requests cached for 1 hour)</li>
 * </ul>
 */
@Configuration
public class CorsConfiguration {

    /**
     * Allowed origins for CORS requests.
     * Can be configured via application.yml using: cors.allowed-origins
     * Default: http://localhost:3000 (development frontend)
     */
    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /**
     * Creates and configures the CORS web filter.
     * 
     * <p>The filter intercepts all incoming requests and adds appropriate CORS headers
     * to the response. This includes handling preflight OPTIONS requests that browsers
     * send before actual requests.</p>
     * 
     * @return configured CorsWebFilter bean
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        
        // Parse comma-separated origins from configuration
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);
        
        // Allow common HTTP methods used by REST APIs
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // Allow all headers (including custom headers like Authorization)
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials (cookies, authorization headers, TLS client certificates)
        config.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour to reduce OPTIONS requests
        config.setMaxAge(3600L);
        
        // Register CORS configuration for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
