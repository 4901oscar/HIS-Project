package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.DuplicateClinicCodeException;
import com.medframe.clinical.domain.exception.InvalidClinicDataException;
import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.port.out.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for creating a new clinic.
 * 
 * <p>This use case orchestrates clinic creation by:
 * <ol>
 *   <li>Validating that the user has ADMIN role</li>
 *   <li>Validating input data (required fields, format validation)</li>
 *   <li>Checking for duplicate clinic code</li>
 *   <li>Creating the clinic using domain model factory method</li>
 *   <li>Persisting the clinic to the database</li>
 * </ol>
 * 
 * <p><b>Requirements:</b> 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.9, 1.10, 5.1, 5.2, 5.3, 5.4, 5.6, 8.1, 8.2
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreateClinicUseCase {
    
    private final ClinicRepository clinicRepository;
    private final PermissionValidator permissionValidator;
    
    /**
     * Creates a new clinic with the specified data.
     * 
     * <p>This method validates that:
     * <ul>
     *   <li>User has ADMIN role</li>
     *   <li>All required fields are present</li>
     *   <li>Codigo is numeric only</li>
     *   <li>Nombre is alphanumeric only</li>
     *   <li>Descripcion is alphanumeric only</li>
     *   <li>Codigo is unique (not already in use)</li>
     * </ul>
     * 
     * @param codigo unique numeric code for the clinic
     * @param nombre name of the clinic
     * @param descripcion description of the clinic
     * @return the created clinic with generated UUID and audit fields
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMIN role
     * @throws InvalidClinicDataException if validation fails
     * @throws DuplicateClinicCodeException if codigo already exists
     */
    public Clinic execute(String codigo, String nombre, String descripcion) {
        // Validate permissions - only ADMIN role can create clinics
        permissionValidator.requireRole("ADMIN");
        
        // Get current user for audit fields
        String currentUser = permissionValidator.getUserId();
        if (currentUser == null || currentUser.trim().isEmpty()) {
            throw new InvalidClinicDataException("No se pudo obtener el usuario actual");
        }
        
        // Validate required fields
        validateRequiredFields(codigo, nombre, descripcion);
        
        // Check for duplicate codigo
        if (clinicRepository.existsByCodigo(codigo)) {
            throw new DuplicateClinicCodeException(
                "Ya existe una clínica con el código: " + codigo
            );
        }
        
        // Create clinic using domain model factory method (validates format)
        Clinic clinic;
        try {
            clinic = Clinic.create(codigo, nombre, descripcion, currentUser);
        } catch (IllegalArgumentException e) {
            throw new InvalidClinicDataException(e.getMessage(), e);
        }
        
        // Persist the clinic
        return clinicRepository.save(clinic);
    }
    
    /**
     * Validates that all required fields are present and not empty.
     */
    private void validateRequiredFields(String codigo, String nombre, String descripcion) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new InvalidClinicDataException("El código de la clínica es obligatorio");
        }
        
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new InvalidClinicDataException("El nombre de la clínica es obligatorio");
        }
        
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new InvalidClinicDataException("La descripción de la clínica es obligatoria");
        }
    }
}
