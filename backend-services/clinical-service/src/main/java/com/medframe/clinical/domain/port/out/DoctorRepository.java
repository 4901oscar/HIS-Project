package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.Doctor;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Output port (repository interface) for Doctor entity persistence.
 * 
 * <p>This interface defines the contract for doctor data access operations.
 * It follows hexagonal architecture principles by defining the port in the domain layer,
 * while the actual implementation (adapter) resides in the infrastructure layer.
 * 
 * <p>The infrastructure layer will provide a JPA-based implementation of this interface.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public interface DoctorRepository {
    
    /**
     * Saves a doctor entity (create or update).
     * 
     * @param doctor the doctor to save
     * @return the saved doctor with generated ID if it was a new entity
     */
    Doctor save(Doctor doctor);
    
    /**
     * Finds a doctor by their unique identifier.
     * 
     * @param id the doctor ID
     * @return an Optional containing the doctor if found, empty otherwise
     */
    Optional<Doctor> findById(String id);
    
    /**
     * Finds all doctors working a specific shift with a given status.
     * 
     * <p>This method is used to filter doctors by their work schedule and status.
     * For example, to find all active doctors working the morning shift (08:00-16:00).
     * 
     * @param shiftStart the start time of the shift
     * @param shiftEnd the end time of the shift
     * @param status the doctor status (ACTIVE or INACTIVE)
     * @return list of doctors matching the criteria
     */
    List<Doctor> findByShiftAndStatus(LocalTime shiftStart, LocalTime shiftEnd, Doctor.DoctorStatus status);
    
    /**
     * Finds all active doctors in the system.
     * 
     * <p>This method returns all doctors with status = ACTIVE, regardless of their shift.
     * It's used by the assignment algorithm to get the pool of available doctors.
     * 
     * @return list of all active doctors
     */
    List<Doctor> findAllActive();
    
    /**
     * Deletes a doctor by their unique identifier.
     * 
     * <p>Note: In practice, doctors should be soft-deleted (status = INACTIVE)
     * rather than hard-deleted to preserve historical appointment data.
     * 
     * @param id the doctor ID
     */
    void deleteById(String id);
}
