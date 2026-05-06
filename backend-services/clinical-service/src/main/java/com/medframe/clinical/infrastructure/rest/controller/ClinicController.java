package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.application.usecase.CreateClinicUseCase;
import com.medframe.clinical.application.usecase.DeleteClinicUseCase;
import com.medframe.clinical.application.usecase.ListClinicsUseCase;
import com.medframe.clinical.application.usecase.UpdateClinicUseCase;
import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.model.ClinicStatus;
import com.medframe.clinical.infrastructure.rest.dto.request.CreateClinicRequest;
import com.medframe.clinical.infrastructure.rest.dto.request.UpdateClinicRequest;
import com.medframe.clinical.infrastructure.rest.dto.response.ClinicResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for clinic management operations.
 * 
 * <p>This controller provides endpoints for:
 * <ul>
 *   <li>Creating clinics (ADMIN only)</li>
 *   <li>Listing clinics with optional status filter (authenticated users)</li>
 *   <li>Updating clinics (ADMIN only)</li>
 *   <li>Deleting clinics - soft delete (ADMIN only)</li>
 * </ul>
 * 
 * <p>Security is handled at the API Gateway level through JWT validation
 * and role-based access control. Use cases validate permissions internally.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/clinical/clinics")
@Validated
@RequiredArgsConstructor
public class ClinicController {
    
    private final CreateClinicUseCase createClinicUseCase;
    private final ListClinicsUseCase listClinicsUseCase;
    private final UpdateClinicUseCase updateClinicUseCase;
    private final DeleteClinicUseCase deleteClinicUseCase;
    
    /**
     * Creates a new clinic.
     * 
     * <p><b>Endpoint:</b> POST /api/clinics
     * 
     * <p><b>Security:</b> Requires ADMIN role (validated in use case)
     * 
     * <p><b>Validation:</b>
     * <ul>
     *   <li>Codigo must be numeric only</li>
     *   <li>Nombre must be alphanumeric only</li>
     *   <li>Descripcion must be alphanumeric only</li>
     *   <li>Codigo must be unique</li>
     * </ul>
     * 
     * @param request the clinic creation request
     * @return the created clinic with HTTP 201 Created
     */
    @PostMapping
    public ResponseEntity<ClinicResponse> createClinic(@Valid @RequestBody CreateClinicRequest request) {
        Clinic clinic = createClinicUseCase.execute(
            request.getCodigo(),
            request.getNombre(),
            request.getDescripcion()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(clinic));
    }
    
    /**
     * Lists all clinics with optional status filter.
     * 
     * <p><b>Endpoint:</b> GET /api/clinics?estado={ACTIVE|INACTIVE|DELETED}
     * 
     * <p><b>Security:</b> Requires authentication (any authenticated user can access)
     * 
     * <p>Results are sorted by createdAt timestamp in descending order.
     * 
     * @param estado optional status filter (ACTIVE, INACTIVE, or DELETED)
     * @return list of clinics with HTTP 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ClinicResponse>> listClinics(
            @RequestParam(required = false) String estado) {
        
        List<Clinic> clinics;
        
        if (estado != null && !estado.trim().isEmpty()) {
            ClinicStatus status = ClinicStatus.valueOf(estado.toUpperCase());
            clinics = listClinicsUseCase.executeWithFilter(status);
        } else {
            clinics = listClinicsUseCase.execute();
        }
        
        List<ClinicResponse> response = clinics.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Updates an existing clinic.
     * 
     * <p><b>Endpoint:</b> PUT /api/clinics/{id}
     * 
     * <p><b>Security:</b> Requires ADMIN role (validated in use case)
     * 
     * <p><b>Validation:</b>
     * <ul>
     *   <li>Codigo must be numeric only (if provided)</li>
     *   <li>Nombre must be alphanumeric only (if provided)</li>
     *   <li>Descripcion must be alphanumeric only (if provided)</li>
     *   <li>Codigo must be unique if being changed</li>
     * </ul>
     * 
     * <p>Only provided fields will be updated. Null fields are ignored.
     * 
     * @param id the clinic UUID
     * @param request the clinic update request
     * @return the updated clinic with HTTP 200 OK
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClinicResponse> updateClinic(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClinicRequest request) {
        
        ClinicStatus status = null;
        if (request.getEstado() != null && !request.getEstado().trim().isEmpty()) {
            status = ClinicStatus.valueOf(request.getEstado().toUpperCase());
        }
        
        Clinic clinic = updateClinicUseCase.execute(
            id,
            request.getCodigo(),
            request.getNombre(),
            request.getDescripcion(),
            status
        );
        
        return ResponseEntity.ok(toResponse(clinic));
    }
    
    /**
     * Deletes a clinic (soft delete).
     * 
     * <p><b>Endpoint:</b> DELETE /api/clinics/{id}
     * 
     * <p><b>Security:</b> Requires ADMIN role (validated in use case)
     * 
     * <p>This is a soft delete operation. The clinic record remains in the database
     * with estado set to DELETED. All other field values are preserved.
     * 
     * @param id the clinic UUID
     * @return HTTP 200 OK
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClinic(@PathVariable UUID id) {
        deleteClinicUseCase.execute(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Converts a Clinic domain model to a ClinicResponse DTO.
     */
    private ClinicResponse toResponse(Clinic clinic) {
        return new ClinicResponse(
            clinic.getId(),
            clinic.getCodigo(),
            clinic.getNombre(),
            clinic.getDescripcion(),
            clinic.getEstado().name(),
            clinic.getCreatedAt(),
            clinic.getCreatedBy(),
            clinic.getUpdatedAt(),
            clinic.getUpdatedBy()
        );
    }
}
