package com.medflow.pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Aplicación principal del Pharmacy Service.
 * 
 * Microservicio responsable de:
 * - Gestión de inventario de medicamentos
 * - Recepción y despacho de prescripciones médicas
 * - Control de stock y alertas de stock bajo
 * 
 * Puerto: 8085
 * Registro en Eureka: PHARMACY-SERVICE
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PharmacyServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PharmacyServiceApplication.class, args);
    }
}
