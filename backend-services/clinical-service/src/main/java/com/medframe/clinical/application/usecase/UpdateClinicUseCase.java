package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.ClinicNotFoundException;
import com.medframe.clinical.domain.exception.DuplicateClinicCodeException;
import com.medframe.clinical.domain.exception.InvalidClinicDataException;
import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.model.ClinicStatus;
import com.medframe.clinical.domain.port.out.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for updating an existing clinic.
 * 
 * <p>This use case orchestrates clinic updates by:
 * <ol>
 *   <li>Validating that the user has ADMIN role</li>
 *   <li>Finding the existing clinic by ID</li>
 *   <li>Validating input data (format validation)</li>
 *   <li>Checking for duplicate codigo if codigo is being changed</li>
 *   <li>Updating the clinic using domain model method</li>
 *   <li>Persisting the updated clinic</li>
 * </ol>
 * 
 * <p><b>Requirements:</b> 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.9, 3.10, 5.1, 5.2, 5.3, 5.4, 5.6, 8.3, 8.4
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateClinicUseCase {
    
    private final ClinicRepository clinicRepository;
    private final PermissionValidator permissionValidator;
    
    /**
     * Updates an existing clinic with the specified data.
     * 
     * <p>This method validates that:
     * <ul>
     *   <li>User has ADMIN role</li>
     *   <li>Clinic exists</li>
     *   <li>Codigo is numeric only (if provided)</li>
     *   <li>Nombre is alphanumeric only (if provided)</li>
     *   <li>Descripcion is alphanumeric only (if provided)</li>
     *   <li>Codigo is unique if being changed (not already in use by another clinic)</li>
     * </ul>
     * 
     * <p>The method preserves original createdAt and createdBy values.
     * 
     * @param id UUID of the clinic to update
     * @param codigo new clinic code (null to keep existing)
     * @param nombre new clinic name (null to keep existing)
     * @param descripcion new clinic description (null to keep existing)
     * @param estado new clinic status (null to keep existing)
     * @return the updated clinic with updated audit fields
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMIN role
     * @throws ClinicNotFoundException if clinic with specified ID doesn't exist
     * @throws InvalidClinicDataException if validation fails
     * @throws DuplicateClinicCodeException if new codigo already exists for another clinic
     */
    public Clinic execute(UUID id, String codigo, String nombre, String descripcion, ClinicStatus estado) {
        // Validate permissions - only ADMIN role can update clinics
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
        
        // Check for duplicate codigo if codigo is being changed
        if (codigo != null && !codigo.equals(clinic.getCodigo())) {
            clinicRepository.findByCodigo(codigo).ifPresent(existingClinic -> {
                if (!existingClinic.getId().equals(id)) {
                    throw new DuplicateClinicCodeException(
                        "Ya existe otra clínica con el código: " + codigo
                    );
                }
            });
        }
        
        // Update clinic using domain model method (validates format)
        try {
            clinic.update(codigo, nombre, descripcion, estado, currentUser);
        } catch (IllegalArgumentException e) {
            throw new InvalidClinicDataException(e.getMessage(), e);
        }
        
        // Persist the updated clinic
        return clinicRepository.save(clinic);
    }
}
