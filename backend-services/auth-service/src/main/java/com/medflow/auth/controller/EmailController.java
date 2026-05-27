package com.medflow.auth.controller;

import com.medflow.auth.dto.AppointmentEmailRequest;
import com.medflow.auth.dto.LabResultsReadyEmailRequest;
import com.medflow.auth.dto.TriageAlertEmailRequest;
import com.medflow.auth.service.EmailService;
import com.medflow.auth.service.UserManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private static final Logger log = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final UserManagementService userManagementService;

    public EmailController(EmailService emailService, UserManagementService userManagementService) {
        this.emailService = emailService;
        this.userManagementService = userManagementService;
    }

    @PostMapping("/appointment-confirmation")
    public ResponseEntity<Void> sendAppointmentConfirmation(
            @RequestBody AppointmentEmailRequest request) {
        log.info("Received appointment confirmation email request for {}", request.getToEmail());
        emailService.sendAppointmentConfirmationEmail(
            request.getToEmail(), request.getFirstName(),
            request.getAppointmentDate(), request.getAppointmentTime(),
            request.getDoctorName(), request.getInvoiceNumber(),
            request.getQrCodeBase64(), request.getNotes(),
            request.getValidFromTime(), request.getValidUntilTime()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/triage-alert")
    public ResponseEntity<Void> sendTriageAlert(@RequestBody TriageAlertEmailRequest request) {
        log.info("Triage alert email request for doctorId={}, priority={}",
                request.getDoctorId(), request.getPriorityLevel());
        UserManagementService.UserEmailInfo info = userManagementService.getUserEmailById(request.getDoctorId());
        if (info == null) {
            log.warn("Doctor no encontrado para envio de alerta de triaje: {}", request.getDoctorId());
            return ResponseEntity.ok().build();
        }
        emailService.sendTriageAlertEmail(
            info.email(), info.fullName(),
            request.getPatientId(), request.getAppointmentId(),
            request.getPriorityLevel(), request.getPriorityDescription(),
            request.getMaxWaitMinutes()
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/lab-results-ready")
    public ResponseEntity<Void> sendLabResultsReady(@RequestBody LabResultsReadyEmailRequest request) {
        log.info("Lab results ready email request for doctorId={}", request.getDoctorId());
        UserManagementService.UserEmailInfo info = userManagementService.getUserEmailById(request.getDoctorId());
        if (info == null) {
            log.warn("Doctor no encontrado para envio de resultados de lab: {}", request.getDoctorId());
            return ResponseEntity.ok().build();
        }
        emailService.sendLabResultsReadyEmail(
            info.email(), info.fullName(),
            request.getPatientId(), request.getAppointmentId(),
            request.getOrderId()
        );
        return ResponseEntity.ok().build();
    }
}
