package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.infrastructure.client.dto.AppointmentEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for communicating with auth-service email endpoints.
 * Configured with timeouts: connect 2s, read 5s.
 */
@FeignClient(
    name = "auth-service",
    url = "${services.auth-service.url}",
    configuration = com.medframe.clinical.config.FeignConfiguration.class
)
public interface EmailServiceFeignClient {
    
    /**
     * Sends an appointment confirmation email via auth-service.
     * 
     * @param request The email request with appointment details and QR code
     */
    @PostMapping("/api/emails/appointment-confirmation")
    void sendAppointmentConfirmation(@RequestBody AppointmentEmailRequest request);
}
