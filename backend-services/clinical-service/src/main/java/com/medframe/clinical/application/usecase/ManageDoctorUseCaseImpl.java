package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.port.in.ManageDoctorUseCase;
import com.medframe.clinical.domain.port.out.DoctorRepository;
import com.medframe.clinical.domain.service.ShiftManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementation of ManageDoctorUseCase.
 * 
 * <p>This use case orchestrates doctor management operations by:
 * <ol>
 *   <li>Validating that the user has ADMINISTRATOR role</li>
 *   <li>Delegating business logic to ShiftManager domain service</li>
 *   <li>The domain service handles shift validation, day-off management, and persistence</li>
 * </ol>
 * 
 * <p><b>Requirements:</b> Requirements 1.1, 1.3, 1.4, 2.1, 2.4, 2.5
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ManageDoctorUseCaseImpl implements ManageDoctorUseCase {
    
    private final ShiftManager shiftManager;
    private final DoctorRepository doctorRepository;
    private final PermissionValidator permissionValidator;
    
    /**
     * Creates a new doctor with the specified shift configuration.
     * 
     * <p>This method validates that:
     * <ul>
     *   <li>User has ADMINISTRATOR role</li>
     *   <li>Shift duration is exactly 8 hours (validated by domain service)</li>
     * </ul>
     * 
     * @param name the doctor's full name
     * @param specialty the doctor's medical specialty
     * @param shiftStart the start time of the doctor's shift
     * @param shiftEnd the end time of the doctor's shift
     * @return the created doctor with generated ID
     * @throws IllegalArgumentException if shift duration is not exactly 8 hours
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    @Override
    public Doctor createDoctor(String userId, String name, String specialty, LocalTime shiftStart, LocalTime shiftEnd) {
        permissionValidator.requireRole("ADMIN");
        return shiftManager.createDoctor(userId, name, specialty, shiftStart, shiftEnd);
    }
    
    /**
     * Updates an existing doctor's information.
     * 
     * <p>This method validates that:
     * <ul>
     *   <li>User has ADMINISTRATOR role</li>
     *   <li>Doctor exists</li>
     *   <li>New shift duration is exactly 8 hours (validated by domain service)</li>
     * </ul>
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
    @Override
    public Doctor updateDoctor(String doctorId, String name, String specialty, 
                               LocalTime shiftStart, LocalTime shiftEnd) {
        // Validate permissions - only ADMINISTRATOR role can update doctors
        permissionValidator.requireRole("ADMIN");
        
        // Delegate to domain service
        return shiftManager.updateDoctor(doctorId, name, specialty, shiftStart, shiftEnd);
    }
    
    /**
     * Deactivates a doctor, preventing them from being assigned to new appointments.
     * 
     * <p>This is a soft delete operation. The doctor record remains in the system
     * but their status is changed to INACTIVE. Existing appointments are not affected.
     * 
     * @param doctorId the ID of the doctor to deactivate
     * @throws com.medframe.clinical.domain.exception.DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalStateException if the doctor is already inactive
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    @Override
    public void deactivateDoctor(String doctorId) {
        // Validate permissions - only ADMINISTRATOR role can deactivate doctors
        permissionValidator.requireRole("ADMIN");
        
        // Delegate to domain service
        shiftManager.deactivateDoctor(doctorId);
    }
    
    /**
     * Lists all active doctors in the system.
     * 
     * @return list of all active doctors
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    @Override
    @Transactional(readOnly = true)
    public List<Doctor> listActiveDoctors() {
        return doctorRepository.findAllActive();
    }
    
    /**
     * Marks a date range as unavailable for a doctor (vacation period).
     * 
     * <p>This method creates one day-off record for each date in the range [startDate, endDate] (inclusive).
     * 
     * @param doctorId the ID of the doctor
     * @param startDate the first date of the vacation period (inclusive)
     * @param endDate the last date of the vacation period (inclusive)
     * @param reason the reason for unavailability
     * @throws com.medframe.clinical.domain.exception.DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalArgumentException if startDate is after endDate
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    @Override
    public void markDaysOff(String doctorId, LocalDate startDate, LocalDate endDate, String reason) {
        // Validate permissions - only ADMINISTRATOR role can mark days off
        permissionValidator.requireRole("ADMIN");
        
        // Delegate to domain service
        shiftManager.markVacationPeriod(doctorId, startDate, endDate, reason);
    }
    
    /**
     * Removes a day-off record, restoring the doctor's availability for that date.
     * 
     * <p>This method is used when a previously marked day-off is cancelled
     * (e.g., vacation plans changed).
     * 
     * @param doctorId the ID of the doctor
     * @param date the date to restore availability for
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    @Override
    public void removeDayOff(String doctorId, LocalDate date) {
        // Validate permissions - only ADMINISTRATOR role can remove days off
        permissionValidator.requireRole("ADMIN");
        
        // Delegate to domain service
        shiftManager.removeDayOff(doctorId, date);
    }
    
    /**
     * Retrieves all day-off records for a specific doctor.
     * 
     * <p>This method returns all availability records where isAvailable = false,
     * which represent days when the doctor is not available for appointments.
     * 
     * @param doctorId the ID of the doctor
     * @return list of day-off records for the doctor
     * @throws com.medframe.clinical.domain.exception.DoctorNotFoundException if the doctor doesn't exist
     * @throws com.medframe.clinical.domain.exception.ForbiddenException if user doesn't have ADMINISTRATOR role
     */
    @Override
    @Transactional(readOnly = true)
    public List<com.medframe.clinical.domain.model.DoctorAvailability> getDaysOff(String doctorId) {
        
        // Verify doctor exists
        doctorRepository.findById(doctorId)
            .orElseThrow(() -> new com.medframe.clinical.domain.exception.DoctorNotFoundException(
                "Doctor no encontrado con ID: " + doctorId
            ));
        
        // Delegate to domain service to get all day-off records
        return shiftManager.getDaysOff(doctorId);
    }
}
