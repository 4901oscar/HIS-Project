package com.medframe.clinical.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuración de RestTemplate para comunicación HTTP con otros microservicios.
 * 
 * <p>Esta clase configura un bean RestTemplate con timeouts apropiados para garantizar
 * que las llamadas HTTP no bloqueen indefinidamente. Los timeouts son configurables
 * externamente a través de application.yml.</p>
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-3.7: Timeout de conexión y lectura configurables</li>
 *   <li>NFR-2: Tiempo de respuesta menor a 3 segundos para operaciones síncronas</li>
 * </ul>
 * 
 * <p><strong>Configuración en application.yml:</strong></p>
 * <pre>
 * rest:
 *   template:
 *     connection:
 *       timeout: 5000
 *     read:
 *       timeout: 5000
 * </pre>
 * 
 * <p><strong>Timeouts configurados:</strong></p>
 * <ul>
 *   <li><strong>Connection Timeout:</strong> Tiempo máximo para establecer conexión TCP (default: 5s)</li>
 *   <li><strong>Read Timeout:</strong> Tiempo máximo para recibir respuesta completa (default: 5s)</li>
 * </ul>
 * 
 * @author MedFlow Development Team
 * @version 1.0
 * @since 2024-04-24
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Timeout de conexión en milisegundos.
     * Tiempo máximo para establecer la conexión TCP con el servidor remoto.
     */
    @Value("${rest.template.connection.timeout:5000}")
    private int connectionTimeout;

    /**
     * Timeout de lectura en milisegundos.
     * Tiempo máximo para recibir la respuesta completa del servidor remoto.
     */
    @Value("${rest.template.read.timeout:5000}")
    private int readTimeout;

    /**
     * Crea y configura un bean RestTemplate con timeouts apropiados.
     * 
     * <p>Este RestTemplate será utilizado por {@code BillingServiceClient} para
     * realizar llamadas HTTP síncronas al Billing Service. Los timeouts garantizan
     * que las llamadas fallen rápidamente en caso de problemas de red, permitiendo
     * que el circuit breaker y retry mechanism funcionen correctamente.</p>
     * 
     * @param builder RestTemplateBuilder proporcionado por Spring Boot
     * @return RestTemplate configurado con timeouts
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(connectionTimeout))
                .setReadTimeout(Duration.ofMillis(readTimeout))
                .build();
    }
}
