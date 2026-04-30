package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.ClinicNotFoundException;
import com.medframe.clinical.domain.exception.InvalidClinicDataException;
import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.port.out.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for deleting a clinic (soft delete).
 * 
 * <p>This use case orchestrates clinic deletion by:
 * <ol>
 *   <li>Validating that the user has ADMIN role</li>
 *   <li>Finding the existing clinic by ID</li>
 *   <li>Performing soft delete using domain model method</li>
 *   <li>Persisting the updated clinic with DELETED status</li>
 * </ol>
 * 
 * <p>This is a soft delete operation. The clinic record remains in the database
 * with estado set to DELETED. All other field values are preserved.
 * 
 * <p><b>Requirements:</b> 4.1, 4.2, 4.4, 4.5, 4.6, 8.3, 8.4
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteClinicUseCase {
    
    private final ClinicRepository clinicRepository;
    private final PermissionValidator permissionValidator;
    
    /**
     * Deletes a clinic by setting its status to DELETED.
     * 
     * <p>This method validates that:
     * <ul>
     *   <li>User has ADMIN role</li>
     *   <li>Clinic exists</li>
     * </ul>
     * 
     * <p>The clinic record is retained in the database with estado = DELETED.
     * All original field values are preserved except estado.
     * Audit fields (updatedAt, updatedBy) are updated.
     * 
     * @param id UUID of the clinic to delete
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMIN role
     * @throws ClinicNotFoundException if clinic with specified ID doesn't exist
     * @throws InvalidClinicDataException if current user cannot be determined
     */
    public void execute(UUID id) {
        // Validate permissions - only ADMIN role can delete clinics
        permissionValidator.requireRole("ADMIN");
        
        // Get current user for audit fields
        String currentUser = permissionValidator.getUserId();
        if (currentUser == null || currentUser.trim().isEmpty()) {
            throw new InvalidClinicDataException("No se pudo obtener el usuario actual");
        }
        
        // Find existing clinic
        Clinic clinic = clinicRepository.findById(id)
            .orElseThrow(() -> new ClinicNotFoundException(
                "Clínica no encontrada con ID: " + id
            ));
        
        // Perform soft delete using domain model method
        clinic.delete(currentUser);
        
        // Persist the updated clinic with DELETED status
        clinicRepository.save(clinic);
    }
}
