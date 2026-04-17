package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.port.in.GenerateLabOrderUseCase;
import com.medframe.clinical.domain.service.LabOrderGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of GenerateLabOrderUseCase.
 * 
 * This use case orchestrates the lab order generation process by:
 * 1. Validating that the user has DOCTOR role
 * 2. Delegating business logic to LabOrderGenerator domain service
 * 3. Generating unique order code and notifying Lab Service
 * 
 * Requirements: Requirement 6 (Lab Order Generation), Requirement 9 (Permission Validation)
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
public class GenerateLabOrderUseCaseImpl implements GenerateLabOrderUseCase {
    
    private final LabOrderGenerator labOrderGenerator;
    private final PermissionValidator permissionValidator;
    
    public GenerateLabOrderUseCaseImpl(LabOrderGenerator labOrderGenerator,
                                        PermissionValidator permissionValidator) {
        this.labOrderGenerator = labOrderGenerator;
        this.permissionValidator = permissionValidator;
    }
    
    /**
     * Generates a laboratory order with requested tests.
     * 
     * @param consultationId The consultation ID this lab order is associated with
     * @param patientId The patient's unique identifier
     * @param doctorId The doctor ordering the tests
     * @param testNames List of laboratory test names to order
     * @return The generated LabOrder with unique code and PENDING status
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have DOCTOR role
     */
    @Override
    public LabOrder generateLabOrder(String consultationId, String patientId,
                                      String doctorId, List<String> testNames) {
        // 1. Validate permissions - only DOCTOR role can generate lab orders
        permissionValidator.requireRole("DOCTOR");
        
        // 2. Delegate to domain service for business logic
        return labOrderGenerator.generateLabOrder(consultationId, patientId,
                                                    doctorId, testNames);
    }
}
