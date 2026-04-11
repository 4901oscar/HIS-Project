package com.medflow.eureka;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test básico para verificar que el contexto de Eureka Server se carga correctamente
 */
@SpringBootTest
@ActiveProfiles("test")
class EurekaServerApplicationTests {

    @Test
    void contextLoads() {
        // Este test verifica que el contexto de Spring Boot se carga sin errores
    }
}
