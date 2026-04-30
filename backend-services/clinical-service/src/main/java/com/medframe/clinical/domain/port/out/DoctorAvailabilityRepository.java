package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.DoctorAvailability;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Output port (repository interface) for DoctorAvailability entity persistence.
 * 
 * <p>This interface defines the contract for doctor availability data access operations.
 * It follows hexagonal architecture principles by defining the port in the domain layer,
 * while the actual implementation (adapter) resides in the infrastructure layer.
 * 
 * <p>The infrastructure layer will provide a JPA-based implementation of this interface.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public interface DoctorAvailabilityRepository {
    
    /**
     * Saves a doctor availability record (create or update).
     * 
     * @param availability the availability record to save
     * @return the saved availability record with generated ID if it was new
     */
    DoctorAvailability save(DoctorAvailability availability);
    
    /**
     * Finds a doctor availability record by ID.
     * 
     * @param id the availability record ID
     * @return an Optional containing the record if found, empty otherwise
     */
    Optional<DoctorAvailability> findById(String id);
    
    /**
     * Finds all availability records for a specific doctor on a specific date.
     * 
     * <p>Typically, there should be at most one record per doctor per date due to
     * the unique constraint on (doctor_id, date). However, this method returns a list
     * to handle edge cases and provide flexibility.
     * 
     * <p>An empty list means the doctor is available (no day-off record exists).
     * 
     * @param doctorId the doctor ID
     * @param date the date to check
     * @return list of availability records (typically 0 or 1 element)
     */
    List<DoctorAvailability> findByDoctorIdAndDate(String doctorId, LocalDate date);
    
    /**
     * Finds all availability records for a specific doctor within a date range.
     * 
     * <p>This method is useful for querying vacation periods or displaying a doctor's
     * availability calendar.
     * 
     * @param doctorId the doctor ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of availability records within the date range
     */
    List<DoctorAvailability> findByDoctorIdAndDateBetween(String doctorId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Deletes a doctor availability record by doctor ID and date.
     * 
     * <p>This method is used to remove day-off records, effectively restoring the doctor's
     * availability for that date.
     * 
     * @param doctorId the doctor ID
     * @param date the date
     */
    void deleteByDoctorIdAndDate(String doctorId, LocalDate date);
    
    /**
     * Deletes all availability records for a specific doctor.
     * 
     * <p>This method is typically used when a doctor is permanently removed from the system.
     * 
     * @param doctorId the doctor ID
     */
    void deleteByDoctorId(String doctorId);
    
    /**
     * Finds all availability records for a specific doctor.
     * 
     * <p>This method returns all day-off records for a doctor, useful for displaying
     * the doctor's complete availability calendar.
     * 
     * @param doctorId the doctor ID
     * @return list of all availability records for the doctor
     */
    List<DoctorAvailability> findByDoctorId(String doctorId);
}
