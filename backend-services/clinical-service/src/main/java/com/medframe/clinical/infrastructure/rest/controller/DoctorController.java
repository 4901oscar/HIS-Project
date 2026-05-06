package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.port.in.ManageDoctorUseCase;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateDoctorRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.MarkDaysOffRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.UpdateDoctorRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.DoctorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for doctor management operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Creating and updating doctors</li>
 *   <li>Deactivating doctors (soft delete)</li>
 *   <li>Listing active doctors</li>
 *   <li>Managing day-off records</li>
 * </ul>
 * 
 * <p>All endpoints require ADMINISTRATOR role.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/clinical/doctors")
@Validated
@RequiredArgsConstructor
public class DoctorController {
    
    private final ManageDoctorUseCase manageDoctorUseCase;
    
    /**
     * Creates a new doctor.
     * 
     * <p><b>Endpoint:</b> POST /api/clinical/doctors
     * 
     * <p><b>Security:</b> Handled at API Gateway level
     * 
     * @param request the doctor creation request
     * @return the created doctor with HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        LocalTime shiftStart = LocalTime.parse(request.getShiftStart());
        LocalTime shiftEnd = LocalTime.parse(request.getShiftEnd());
        
        Doctor doctor = manageDoctorUseCase.createDoctor(
            request.getUserId(),
            request.getName(),
            shiftStart,
            shiftEnd,
            request.getClinicId()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(doctor));
    }
    
    /**
     * Updates an existing doctor.
     * 
     * <p><b>Endpoint:</b> PUT /api/clinical/doctors/{id}
     * 
     * <p><b>Security:</b> Handled at API Gateway level
     * 
     * @param id the doctor ID
     * @param request the doctor update request
     * @return the updated doctor with HTTP 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable String id,
            @Valid @RequestBody UpdateDoctorRequest request) {
        
        LocalTime shiftStart = LocalTime.parse(request.getShiftStart());
        LocalTime shiftEnd = LocalTime.parse(request.getShiftEnd());
        
        Doctor doctor = manageDoctorUseCase.updateDoctor(
            id,
            request.getName(),
            shiftStart,
            shiftEnd,
            request.getClinicId()
        );
        
        return ResponseEntity.ok(toResponse(doctor));
    }
    
    /**
     * Deactivates a doctor (soft delete).
     * 
     * <p><b>Endpoint:</b> DELETE /api/clinical/doctors/{id}
     * 
     * <p><b>Security:</b> Handled at API Gateway level
     * 
     * @param id the doctor ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateDoctor(@PathVariable String id) {
        manageDoctorUseCase.deactivateDoctor(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Lists all active doctors.
     * 
     * <p><b>Endpoint:</b> GET /api/clinical/doctors
     * 
     * <p><b>Security:</b> Handled at API Gateway level
     * 
     * @return list of active doctors with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> listActiveDoctors() {
        List<Doctor> doctors = manageDoctorUseCase.listActiveDoctors();
        List<DoctorResponse> response = doctors.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Marks days off for a doctor (vacation period).
     * 
     * <p><b>Endpoint:</b> POST /api/clinical/doctors/{id}/days-off
     * 
     * <p><b>Security:</b> Handled at API Gateway level
     * 
     * @param id the doctor ID
     * @param request the days-off request
     * @return HTTP 204 No Content
     */
    @PostMapping("/{id}/days-off")
    public ResponseEntity<Void> markDaysOff(
            @PathVariable String id,
            @Valid @RequestBody MarkDaysOffRequest request) {
        
        manageDoctorUseCase.markDaysOff(
            id,
            request.getStartDate(),
            request.getEndDate(),
            request.getReason()
        );
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Removes a day-off record for a doctor.
     * 
     * <p><b>Endpoint:</b> DELETE /api/clinical/doctors/{id}/days-off/{date}
     * 
     * <p><b>Security:</b> Handled at API Gateway level
     * 
     * @param id the doctor ID
     * @param date the date to restore availability for
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}/days-off/{date}")
    public ResponseEntity<Void> removeDayOff(
            @PathVariable String id,
            @PathVariable LocalDate date) {
        
        manageDoctorUseCase.removeDayOff(id, date);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Retrieves all day-off records for a doctor.
     * 
     * <p><b>Endpoint:</b> GET /api/clinical/doctors/{id}/days-off
     * 
     * <p><b>Security:</b> Handled at API Gateway level
     * 
     * @param id the doctor ID
     * @return list of day-off records with HTTP 200 OK
     */
    @GetMapping("/{id}/days-off")
    public ResponseEntity<List<com.medframe.clinical.infrastructure.rest.dto.response.DayOffResponse>> getDaysOff(
            @PathVariable String id) {
        
        List<com.medframe.clinical.domain.model.DoctorAvailability> daysOff = manageDoctorUseCase.getDaysOff(id);
        
        List<com.medframe.clinical.infrastructure.rest.dto.response.DayOffResponse> response = daysOff.stream()
            .map(availability -> new com.medframe.clinical.infrastructure.rest.dto.response.DayOffResponse(
                availability.getId(),
                availability.getDoctorId(),
                availability.getDate(),
                availability.isAvailable(),
                availability.getReason(),
                availability.getCreatedAt(),
                availability.getCreatedBy()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Converts a Doctor domain model to a DoctorResponse DTO.
     */
    private DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(
            doctor.getId(),
            doctor.getName(),
            doctor.getShiftStart().toString(),
            doctor.getShiftEnd().toString(),
            doctor.getStatus().name(),
            doctor.getClinicId(),
            doctor.getCreatedAt()
        );
    }
}
