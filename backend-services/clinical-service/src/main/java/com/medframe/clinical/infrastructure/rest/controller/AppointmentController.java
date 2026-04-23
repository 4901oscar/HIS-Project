package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.application.usecase.ManageAppointmentUseCaseImpl;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.port.out.PatientServiceClient;
import com.medframe.clinical.domain.service.AppointmentManager;
import com.medframe.clinical.infrastructure.client.dto.PatientDTO;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateAppointmentRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.HoldSlotRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.AvailableSlotsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clinical/appointments")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AppointmentController {
    
    private final ManageAppointmentUseCase manageAppointmentUseCase;
    private final AppointmentManager appointmentManager;
    private final PatientServiceClient patientServiceClient;
    private final DoctorRepository doctorRepository;
    
    private static final DateTimeFormatter INVOICE_TIMESTAMP_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /** All appointments — ADMISSION / ADMIN. */
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> listAll() {
        List<AppointmentResponse> list = manageAppointmentUseCase.listAll()
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Appointments for the authenticated patient. */
    @GetMapping("/my")
    public ResponseEntity<List<AppointmentResponse>> listMine() {
        List<AppointmentResponse> list = manageAppointmentUseCase.listMyAppointments()
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Appointments assigned to the authenticated doctor. */
    @GetMapping("/doctor")
    public ResponseEntity<List<AppointmentResponse>> listDoctor() {
        List<AppointmentResponse> list = manageAppointmentUseCase.listDoctorAppointments()
                .stream().map(this::mapToResponse).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /** Available slots for a date — excludes slots held by other sessions. */
    @GetMapping("/available")
    public ResponseEntity<List<String>> getAvailableSlotsForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sessionId) {
        List<LocalTime> slots = ((ManageAppointmentUseCaseImpl) manageAppointmentUseCase)
                .findAvailableSlotsForDate(date, sessionId);
        List<String> formatted = slots.stream()
            .map(t -> String.format("%02d:%02d", t.getHour(), t.getMinute()))
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(formatted);
    }

    /** Hold a (date, time) slot for 10 minutes. */
    @PostMapping("/hold")
    public ResponseEntity<Void> holdSlot(@Valid @RequestBody HoldSlotRequest request) {
        boolean held = manageAppointmentUseCase.holdSlot(
                request.getSessionId(), request.getDate(), request.getTime());
        return held
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    /** Release a hold by sessionId. */
    @DeleteMapping("/hold")
    public ResponseEntity<Void> releaseHold(@RequestParam String sessionId) {
        manageAppointmentUseCase.releaseHold(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/slots")
    public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
            @RequestParam String doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<LocalTime> slots = manageAppointmentUseCase.findAvailableSlots(doctorId, date);
        
        AvailableSlotsResponse response = new AvailableSlotsResponse(
            doctorId,
            date,
            slots
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @RequestHeader("X-User-Id") String userId) {

        // 1. Fetch patient info for email (graceful degradation on failure)
        String patientEmail = "no-email@medflow.com";
        String patientFirstName = "Paciente";
        try {
            PatientDTO patient = (PatientDTO) patientServiceClient.getPatient(userId);
            patientEmail = patient.getEmail();
            patientFirstName = patient.getFirstName();
        } catch (Exception e) {
            log.warn("No se pudo obtener info del paciente para el correo: {}", e.getMessage());
        }

        // 2. Create appointment (manual or auto-assignment) — exactly once
        Appointment appointment;
        if (request.getDoctorId() != null && !request.getDoctorId().trim().isEmpty()) {
            appointment = appointmentManager.createAppointment(
                userId, request.getDoctorId(),
                request.getAppointmentDate(), request.getAppointmentTime(),
                request.getNotes(), userId, true);
        } else {
            appointment = manageAppointmentUseCase.createAppointmentWithAutoAssignment(
                userId,
                request.getAppointmentDate(), request.getAppointmentTime(),
                request.getNotes(), userId);
        }

        // 3. Release slot hold if provided
        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            manageAppointmentUseCase.releaseHold(request.getSessionId());
        }

        // 4. Resolve doctor name from repository (fallback to placeholder)
        String doctorName = doctorRepository.findById(appointment.getDoctorId())
                .map(d -> "Dr. " + d.getName())
                .orElse("Dr. Asignado");

        // 5. Attach QR code and send confirmation email (graceful — never blocks the response)
        String invoiceNumber = generateInvoiceNumber();
        appointmentManager.attachQRAndNotify(appointment, patientEmail, patientFirstName, doctorName, invoiceNumber);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(appointment));
    }
    
    /**
     * Generates a unique invoice number using timestamp format.
     * Format: INV-yyyyMMddHHmmss (e.g., INV-20260422143025)
     */
    private String generateInvoiceNumber() {
        return "INV-" + java.time.LocalDateTime.now().format(INVOICE_TIMESTAMP_FORMATTER);
    }
    
    /** QR scan — validates time window and activates the appointment if within window. */
    @PostMapping("/{id}/scan")
    public ResponseEntity<Map<String, Object>> scanAppointment(@PathVariable String id) {
        com.medframe.clinical.domain.model.ScanResult result =
                appointmentManager.validateAndActivateAppointment(id, LocalDateTime.now());
        Appointment appt = result.getAppointment();
        Map<String, Object> body = new java.util.LinkedHashMap<>();
        body.put("status", result.getStatus().name());
        body.put("message", result.getMessage());
        body.put("appointmentId", appt.getId());
        body.put("patientId", appt.getPatientId());
        body.put("doctorId", appt.getDoctorId());
        body.put("date", appt.getAppointmentDate().toString());
        body.put("time", appt.getAppointmentTime().toString());
        body.put("appointmentStatus", appt.getStatus().name());
        return ResponseEntity.ok(body);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateAppointment(@PathVariable String id) {
        manageAppointmentUseCase.activateAppointment(id);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable String id) {
        manageAppointmentUseCase.cancelAppointment(id);
        return ResponseEntity.noContent().build();
    }
    
    private AppointmentResponse mapToResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getPatientId(),
            appointment.getDoctorId(),
            appointment.getAppointmentDate(),
            appointment.getAppointmentTime(),
            appointment.getStatus().name(),
            appointment.getNotes(),
            appointment.getCreatedAt(),
            appointment.getQrCodeBase64()  // Include QR code if generated
        );
    }
}
