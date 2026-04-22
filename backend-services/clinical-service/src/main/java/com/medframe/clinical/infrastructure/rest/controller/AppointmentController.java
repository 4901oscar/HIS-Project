package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.application.usecase.ManageAppointmentUseCaseImpl;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateAppointmentRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.HoldSlotRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.AppointmentResponse;
import com.medframe.clinical.infrastructure.rest.dto.response.AvailableSlotsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/clinical/appointments")
@Validated
@RequiredArgsConstructor
public class AppointmentController {
    
    private final ManageAppointmentUseCase manageAppointmentUseCase;
    
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
        
        Appointment appointment;
        
        // Check if doctorId is provided (manual selection) or null (automatic assignment)
        if (request.getDoctorId() != null && !request.getDoctorId().trim().isEmpty()) {
            // Manual doctor selection (backward compatibility)
            appointment = manageAppointmentUseCase.createAppointment(
                request.getPatientId(),
                request.getDoctorId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                request.getNotes(),
                userId
            );
        } else {
            // Automatic doctor assignment
            appointment = manageAppointmentUseCase.createAppointmentWithAutoAssignment(
                request.getPatientId(),
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                request.getNotes(),
                userId
            );
        }
        
        // Release the temporary hold if the client provided a session ID
        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            manageAppointmentUseCase.releaseHold(request.getSessionId());
        }

        AppointmentResponse response = mapToResponse(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
            appointment.getCreatedAt()
        );
    }
}
