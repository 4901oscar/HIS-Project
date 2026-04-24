package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.port.in.PerformTriageUseCase;
import com.medframe.clinical.domain.port.out.TriageRepository;
import com.medframe.clinical.domain.service.TriageEngine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of PerformTriageUseCase.
 * 
 * This use case orchestrates the triage process by:
 * 1. Validating that the user has DOCTOR role
 * 2. Delegating business logic to TriageEngine domain service
 * 3. Persisting the triage result via TriageRepository
 * 
 * Requirements: Requirement 1 (Manchester Triage), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
public class PerformTriageUseCaseImpl implements PerformTriageUseCase {
    
    private final TriageEngine triageEngine;
    private final TriageRepository triageRepository;
    private final PermissionValidator permissionValidator;
    
    public PerformTriageUseCaseImpl(TriageEngine triageEngine,
                                    TriageRepository triageRepository,
                                    PermissionValidator permissionValidator) {
        this.triageEngine = triageEngine;
        this.triageRepository = triageRepository;
        this.permissionValidator = permissionValidator;
    }
    
    /**
     * Performs Manchester triage for a patient linked to an appointment.
     * 
     * @param appointmentId The appointment's unique identifier
     * @param patientId The patient's unique identifier
     * @param doctorId The doctor performing the triage
     * @param motifId The Manchester motif (reason for consultation)
     * @param discriminatorIds List of Manchester discriminator IDs selected by the doctor
     * @return The completed Triage with calculated priority level
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have DOCTOR role
     * @throws com.medframe.clinical.domain.exception.VitalSignsNotFoundException if patient has no vital signs
     * @throws com.medframe.clinical.domain.exception.AppointmentNotFoundException if appointment not found
     * @throws com.medframe.clinical.domain.exception.DuplicateTriageException if triage already exists for appointment
     * @throws IllegalStateException if appointment is not in ACTIVE status
     */
    @Override
    public Triage performTriage(String appointmentId, String patientId, String doctorId,
                                String motifId, List<String> discriminatorIds) {
        // 1. Validate permissions - only DOCTOR role can perform triage
        permissionValidator.requireRole("DOCTOR");
        
        // 2. Delegate to domain service for business logic
        // TriageEngine handles appointment validation (exists, ACTIVE status, no duplicate)
        Triage triage = triageEngine.performTriage(appointmentId, patientId, doctorId,
                                                    motifId, discriminatorIds);
        
        // 3. Persist the triage result
        return triageRepository.save(triage);
    }
}
