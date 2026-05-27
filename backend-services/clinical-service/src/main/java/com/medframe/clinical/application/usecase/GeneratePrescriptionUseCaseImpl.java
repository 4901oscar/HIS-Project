package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.port.in.GeneratePrescriptionUseCase;
import com.medframe.clinical.domain.service.PrescriptionGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GeneratePrescriptionUseCase.
 * 
 * This use case orchestrates the prescription generation process by:
 * 1. Validating that the user has DOCTOR role
 * 2. Delegating business logic to PrescriptionGenerator domain service
 * 3. Generating unique prescription code and notifying Pharmacy Service
 * 
 * Requirements: Requirement 5 (Prescription Generation), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
public class GeneratePrescriptionUseCaseImpl implements GeneratePrescriptionUseCase {
    
    private final PrescriptionGenerator prescriptionGenerator;
    private final PermissionValidator permissionValidator;
    
    public GeneratePrescriptionUseCaseImpl(PrescriptionGenerator prescriptionGenerator,
                                            PermissionValidator permissionValidator) {
        this.prescriptionGenerator = prescriptionGenerator;
        this.permissionValidator = permissionValidator;
    }
    
    /**
     * Generates a medical prescription with medications.
     * 
     * @param consultationId The consultation ID this prescription is associated with
     * @param patientId The patient's unique identifier
     * @param doctorId The doctor issuing the prescription
     * @param medications List of medications to prescribe
     * @return The generated Prescription with unique code and PENDING status
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have DOCTOR role
     */
    @Override
    public Prescription generatePrescription(String consultationId, String patientId,
                                              String doctorId,
                                              List<Prescription.Medication> medications) {
        // 1. Validate permissions - only DOCTOR role can generate prescriptions
        permissionValidator.requireRole("DOCTOR");
        
        // 2. Delegate to domain service for business logic
        return prescriptionGenerator.generatePrescription(consultationId, patientId,
                                                           doctorId, medications);
    }
}
