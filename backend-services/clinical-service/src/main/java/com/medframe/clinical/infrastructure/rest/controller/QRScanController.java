package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.exception.AppointmentNotFoundException;
import com.medframe.clinical.domain.model.ScanResult;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.infrastructure.rest.dto.QRScanRequest;
import com.medframe.clinical.infrastructure.rest.dto.ScanResultDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for QR code scanning operations.
 * Handles appointment activation via QR code scanning at reception.
 */
@RestController
@RequestMapping("/api/clinical/appointments")
public class QRScanController {
    
    private static final Logger log = LoggerFactory.getLogger(QRScanController.class);
    
    private final ManageAppointmentUseCase manageAppointmentUseCase;
    
    public QRScanController(ManageAppointmentUseCase manageAppointmentUseCase) {
        this.manageAppointmentUseCase = manageAppointmentUseCase;
    }
    
    /**
     * Scans a QR code and validates/activates the appointment.
     * 
     * @param request QR scan request with decoded QR data
     * @return Scan result with status (EARLY, ACTIVE, MISSED) and message
     */
    @PostMapping("/scan-qr")
    public ResponseEntity<ScanResultDTO> scanQRCode(@RequestBody QRScanRequest request) {
        log.info("QR scan request received for appointment {}", request.getAppointmentId());
        
        // Validate request
        if (request.getAppointmentId() == null || request.getAppointmentId().isBlank()) {
            log.warn("QR scan request rejected: appointmentId is null or blank");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Scan and validate with current time
            LocalDateTime scanTime = LocalDateTime.now();
            ScanResult result = manageAppointmentUseCase.scanAndActivateAppointment(
                request.getAppointmentId(), scanTime);
            
            // Convert to DTO
            ScanResultDTO dto = ScanResultDTO.fromDomain(result);
            
            log.info("QR scan completed for appointment {} with status {}", 
                     request.getAppointmentId(), result.getStatus());
            
            return ResponseEntity.ok(dto);
            
        } catch (AppointmentNotFoundException e) {
            log.warn("Appointment not found for QR scan: {}", request.getAppointmentId());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error processing QR scan for appointment {}: {}", 
                     request.getAppointmentId(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
