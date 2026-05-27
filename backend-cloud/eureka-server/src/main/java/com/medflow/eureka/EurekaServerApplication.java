package com.medflow.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application
 * 
 * Service Discovery Server para MedFlow HIS.
 * Todos los microservicios se registran aquí para descubrimiento dinámico.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer  // Esta anotación habilita el servidor Eureka
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
