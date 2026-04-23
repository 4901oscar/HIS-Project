package com.medflow.auth.controller;

import com.medflow.auth.dto.AppointmentEmailRequest;
import com.medflow.auth.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for email operations.
 * Handles appointment confirmation email requests from clinical-service.
 */
@RestController
@RequestMapping("/api/emails")
public class EmailController {
    
    private static final Logger log = LoggerFactory.getLogger(EmailController.class);
    
    private final EmailService emailService;
    
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }
    
    /**
     * Sends an appointment confirmation email.
     * Returns immediately while email is sent asynchronously.
     * 
     * @param request The email request with appointment details and QR code
     * @return 200 OK response (email is sent asynchronously)
     */
    @PostMapping("/appointment-confirmation")
    public ResponseEntity<Void> sendAppointmentConfirmation(
            @RequestBody AppointmentEmailRequest request) {
        
        log.info("Received appointment confirmation email request for {}", 
                 request.getToEmail());
        
        emailService.sendAppointmentConfirmationEmail(
            request.getToEmail(),
            request.getFirstName(),
            request.getAppointmentDate(),
            request.getAppointmentTime(),
            request.getDoctorName(),
            request.getInvoiceNumber(),
            request.getQrCodeBase64(),
            request.getNotes(),
            request.getValidFromTime(),
            request.getValidUntilTime()
        );
        
        return ResponseEntity.ok().build();
    }
}
