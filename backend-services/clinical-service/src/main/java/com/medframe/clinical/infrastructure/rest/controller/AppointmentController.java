package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.in.ManageAppointmentUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateAppointmentRequest;
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
        
        Appointment appointment = manageAppointmentUseCase.createAppointment(
            request.getPatientId(),
            request.getDoctorId(),
            request.getAppointmentDate(),
            request.getAppointmentTime(),
            request.getNotes(),
            userId
        );
        
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
            appointment.getCreatedAt()
        );
    }
}
