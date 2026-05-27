package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.infrastructure.client.dto.AppointmentEmailRequest;
import com.medframe.clinical.infrastructure.client.dto.TriageAlertRequest;

/**
 * Port for sending appointment confirmation emails.
 * Implementations should handle asynchronous email delivery without throwing exceptions.
 */
public interface AppointmentEmailSender {
    
    /**
     * Sends an appointment confirmation email asynchronously.
     * This method should not block and should not throw exceptions.
     * Failures should be logged but not propagated.
     * 
     * @param request The email request containing appointment details and QR code
     */
    void sendAppointmentConfirmationEmail(AppointmentEmailRequest request);

    /**
     * Envía alerta de triaje urgente (RED/ORANGE) al médico asignado.
     * No lanza excepciones; los fallos se loguean y se ignoran.
     */
    void sendTriageAlert(TriageAlertRequest request);
}
