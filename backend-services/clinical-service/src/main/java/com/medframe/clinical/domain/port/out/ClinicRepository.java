package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.Clinic;
import com.medframe.clinical.domain.model.ClinicStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port (repository interface) for Clinic entity persistence.
 * 
 * <p>This interface defines the contract for clinic data access operations.
 * It follows hexagonal architecture principles by defining the port in the domain layer,
 * while the actual implementation (adapter) resides in the infrastructure layer.
 * 
 * <p>The infrastructure layer will provide a JPA-based implementation of this interface.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public interface ClinicRepository {
    
    /**
     * Saves a clinic entity (create or update).
     * 
     * @param clinic the clinic to save
     * @return the saved clinic with generated ID if it was a new entity
     */
    Clinic save(Clinic clinic);
    
    /**
     * Finds a clinic by its unique identifier.
     * 
     * @param id the clinic UUID
     * @return an Optional containing the clinic if found, empty otherwise
     */
    Optional<Clinic> findById(UUID id);
    
    /**
     * Finds all clinics in the system.
     * 
     * <p>This method returns all clinics regardless of their status.
     * Results are sorted by createdAt timestamp in descending order.
     * 
     * @return list of all clinics
     */
    List<Clinic> findAll();
    
    /**
     * Finds all clinics with a specific operational status.
     * 
     * <p>This method is used to filter clinics by their status (ACTIVE, INACTIVE, or DELETED).
     * Results are sorted by createdAt timestamp in descending order.
     * 
     * @param estado the clinic status to filter by
     * @return list of clinics matching the specified status
     */
    List<Clinic> findByEstado(ClinicStatus estado);
    
    /**
     * Checks if a clinic with the specified code already exists.
     * 
     * <p>This method is used to enforce the unique constraint on clinic codes
     * before creating or updating a clinic.
     * 
     * @param codigo the clinic code to check
     * @return true if a clinic with this code exists, false otherwise
     */
    boolean existsByCodigo(String codigo);
    
    /**
     * Finds a clinic by its unique code.
     * 
     * <p>This method is used to retrieve a clinic by its business identifier (codigo)
     * rather than its technical identifier (UUID).
     * 
     * @param codigo the clinic code
     * @return an Optional containing the clinic if found, empty otherwise
     */
    Optional<Clinic> findByCodigo(String codigo);
}
