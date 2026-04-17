package com.medflow.patient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente HTTP para llamar al auth-service (CU-01).
 * Crea la cuenta del paciente con contraseña temporal.
 */
@Component
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    public record PatientAccountResult(String authUserId, String temporaryPassword) {}

    private final RestTemplate restTemplate;

    @Value("${auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Crea cuenta de paciente en auth-service.
     * @return resultado con authUserId y contraseña temporal, o null si falla.
     */
    public PatientAccountResult createPatientAccount(String dpi, String email, String fullName) {
        try {
            Map<String, String> body = Map.of(
                    "dpi", dpi,
                    "email", email,
                    "fullName", fullName
            );
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    authServiceUrl + "/api/auth/internal/create-patient",
                    body,
                    Map.class
            );
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<?, ?> resBody = response.getBody();
                String userId = (String) resBody.get("userId");
                String tempPassword = (String) resBody.get("temporaryPassword");
                return new PatientAccountResult(userId, tempPassword);
            }
        } catch (Exception ex) {
            log.error("[AuthServiceClient] Error creando cuenta de paciente para DPI {}: {}", dpi, ex.getMessage());
        }
        return null;
    }
}
