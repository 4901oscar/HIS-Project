package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Doctor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Input port (use case interface) for doctor management operations.
 * 
 * <p>This interface defines the contract for doctor management use cases.
 * It follows hexagonal architecture principles by defining the port in the domain layer,
 * while the actual implementation resides in the application layer.
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Create and update doctor records</li>
 *   <li>Deactivate doctors (soft delete)</li>
 *   <li>List active doctors</li>
 *   <li>Manage day-off records</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public interface ManageDoctorUseCase {
    
    /**
     * Creates a new doctor with the specified shift configuration.
     * 
     * @param name the doctor's full name
     * @param specialty the doctor's medical specialty
     * @param shiftStart the start time of the doctor's shift
     * @param shiftEnd the end time of the doctor's shift
     * @return the created doctor with generated ID
     * @throws IllegalArgumentException if shift duration is not exactly 8 hours
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    Doctor createDoctor(String userId, String name, String specialty, LocalTime shiftStart, LocalTime shiftEnd);
    
    /**
     * Updates an existing doctor's information.
     * 
     * @param doctorId the ID of the doctor to update
     * @param name the new name
     * @param specialty the new specialty
     * @param shiftStart the new shift start time
     * @param shiftEnd the new shift end time
     * @return the updated doctor
     * @throws com.medframe.clinical.domain.exception.DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalArgumentException if the new shift duration is not exactly 8 hours
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    Doctor updateDoctor(String doctorId, String name, String specialty, LocalTime shiftStart, LocalTime shiftEnd);
    
    /**
     * Deactivates a doctor, preventing them from being assigned to new appointments.
     * 
     * @param doctorId the ID of the doctor to deactivate
     * @throws com.medframe.clinical.domain.exception.DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalStateException if the doctor is already inactive
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    void deactivateDoctor(String doctorId);
    
    /**
     * Lists all active doctors in the system.
     * 
     * @return list of all active doctors
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    List<Doctor> listActiveDoctors();
    
    /**
     * Marks a date range as unavailable for a doctor (vacation period).
     * 
     * @param doctorId the ID of the doctor
     * @param startDate the first date of the vacation period (inclusive)
     * @param endDate the last date of the vacation period (inclusive)
     * @param reason the reason for unavailability
     * @throws com.medframe.clinical.domain.exception.DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalArgumentException if startDate is after endDate
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    void markDaysOff(String doctorId, LocalDate startDate, LocalDate endDate, String reason);
    
    /**
     * Removes a day-off record, restoring the doctor's availability for that date.
     * 
     * @param doctorId the ID of the doctor
     * @param date the date to restore availability for
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    void removeDayOff(String doctorId, LocalDate date);
    
    /**
     * Gets all day-off records for a doctor.
     * 
     * @param doctorId the ID of the doctor
     * @return list of day-off records for the doctor
     * @throws com.medframe.clinical.domain.exception.DoctorNotFoundException if the doctor doesn't exist
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    List<com.medframe.clinical.domain.model.DoctorAvailability> getDaysOff(String doctorId);
}
