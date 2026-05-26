package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.port.out.AppointmentEmailSender;
import com.medframe.clinical.infrastructure.client.dto.AppointmentEmailRequest;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Adapter for sending appointment confirmation emails via auth-service.
 * Implements asynchronous email sending with graceful error handling.
 */
@Component
public class EmailClientAdapter implements AppointmentEmailSender {
    
    private static final Logger log = LoggerFactory.getLogger(EmailClientAdapter.class);
    
    private final EmailServiceFeignClient emailClient;
    
    public EmailClientAdapter(EmailServiceFeignClient emailClient) {
        this.emailClient = emailClient;
    }
    
    @Async
    @Override
    public void sendAppointmentConfirmationEmail(AppointmentEmailRequest request) {
        try {
            log.info("Sending appointment confirmation email to {}", request.getToEmail());
            emailClient.sendAppointmentConfirmation(request);
            log.info("Appointment confirmation email sent successfully to {}", 
                     request.getToEmail());
        } catch (FeignException e) {
            log.error("Failed to send appointment confirmation email to {}: HTTP {} - {}", 
                      request.getToEmail(), e.status(), e.getMessage());
            // Do not throw - email failure should not affect appointment creation
        } catch (Exception e) {
            log.error("Unexpected error sending appointment confirmation email to {}: {}", 
                      request.getToEmail(), e.getMessage(), e);
            // Do not throw - email failure should not affect appointment creation
        }
    }

    @Async
    @Override
    public void sendTriageAlert(com.medframe.clinical.infrastructure.client.dto.TriageAlertRequest request) {
        try {
            log.info("Sending triage alert email for doctorId={}, priority={}",
                    request.getDoctorId(), request.getPriorityLevel());
            emailClient.sendTriageAlert(request);
            log.info("Triage alert email sent for doctorId={}", request.getDoctorId());
        } catch (FeignException e) {
            log.error("Failed to send triage alert email for doctorId={}: HTTP {} - {}",
                    request.getDoctorId(), e.status(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error sending triage alert email for doctorId={}: {}",
                    request.getDoctorId(), e.getMessage(), e);
        }
    }
}
