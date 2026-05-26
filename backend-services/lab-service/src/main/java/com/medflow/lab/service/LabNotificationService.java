package com.medflow.lab.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@Slf4j
public class LabNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Async
    public void notifyLabResultsReady(String doctorId, String patientId,
                                      String appointmentId, String orderId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = Map.of(
                "doctorId", doctorId,
                "patientId", patientId,
                "appointmentId", appointmentId != null ? appointmentId : "",
                "orderId", orderId
            );

            restTemplate.postForEntity(
                authServiceUrl + "/api/emails/lab-results-ready",
                new HttpEntity<>(body, headers),
                Void.class
            );
            log.info("[Lab] Notificación de resultados enviada para orderId={}", orderId);
        } catch (Exception e) {
            log.error("[Lab] Error al notificar resultados de lab orderId={}: {}", orderId, e.getMessage());
        }
    }
}