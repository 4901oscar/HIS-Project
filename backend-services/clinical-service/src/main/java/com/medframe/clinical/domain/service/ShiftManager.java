package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.ClinicShiftConflictException;
import com.medframe.clinical.domain.exception.DoctorNotFoundException;
import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.model.DoctorAvailability;
import com.medframe.clinical.domain.port.out.DoctorAvailabilityRepository;
import com.medframe.clinical.domain.port.out.DoctorRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Domain service responsible for managing doctor records and work schedules.
 * 
 * <p>This service handles CRUD operations for doctors and their availability records.
 * It follows hexagonal architecture principles with no infrastructure dependencies
 * (no Spring annotations).
 * 
 * <p><b>Responsibilities:</b>
 * <ul>
 *   <li>Create and update doctor records</li>
 *   <li>Validate shift configurations (8-hour constraint)</li>
 *   <li>Manage day-off records (vacations, personal days)</li>
 *   <li>Deactivate doctors (soft delete)</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class ShiftManager {
    
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    
    /**
     * Constructs a new ShiftManager with required dependencies.
     * 
     * @param doctorRepository repository for doctor data access
     * @param availabilityRepository repository for doctor availability data access
     */
    public ShiftManager(DoctorRepository doctorRepository,
                        DoctorAvailabilityRepository availabilityRepository) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
    }
    
    /**
     * Creates a new doctor with the specified shift configuration, or reactivates an inactive doctor.
     * 
     * <p>This method validates that the shift duration is exactly 8 hours before
     * persisting the doctor record.
     * 
     * <p><b>Behavior:</b>
     * <ul>
     *   <li>If doctor doesn't exist: Creates a new active doctor</li>
     *   <li>If doctor exists and is INACTIVE: Reactivates the doctor and updates their information</li>
     *   <li>If doctor exists and is ACTIVE: Throws IllegalStateException</li>
     * </ul>
     * 
     * <p><b>Example:</b>
     * <pre>
     * // First time - creates new doctor
     * Doctor doctor = shiftManager.createDoctor(
     *     "user-123",
     *     "Dr. Juan Pérez",
     *     "Cardiology",
     *     LocalTime.of(8, 0),   // 08:00
     *     LocalTime.of(16, 0)   // 16:00
     * );
     * 
     * // Later - deactivate
     * shiftManager.deactivateDoctor("user-123");
     * 
     * // Re-register - reactivates existing doctor
     * Doctor reactivated = shiftManager.createDoctor(
     *     "user-123",
     *     "Dr. Juan Pérez",
     *     "Neurology",  // Can update specialty
     *     LocalTime.of(14, 0),  // Can update shift
     *     LocalTime.of(22, 0)
     * );
     * // Result: Same doctor reactivated with updated information
     * </pre>
     * 
     * @param userId the user ID from auth-service
     * @param name the doctor's full name
     * @param specialty the doctor's medical specialty
     * @param shiftStart the start time of the doctor's shift
     * @param shiftEnd the end time of the doctor's shift
     * @return the created or reactivated doctor
     * @throws IllegalArgumentException if shift duration is not exactly 8 hours
     * @throws IllegalStateException if doctor already exists and is active
     */
    public Doctor createDoctor(String userId, String name,
                               LocalTime shiftStart, LocalTime shiftEnd, String clinicId) {
        var existingDoctor = doctorRepository.findById(userId);

        if (existingDoctor.isPresent()) {
            Doctor doctor = existingDoctor.get();

            if (doctor.getStatus() == Doctor.DoctorStatus.INACTIVE) {
                doctor.setName(name);
                doctor.setShiftStart(shiftStart);
                doctor.setShiftEnd(shiftEnd);
                doctor.setClinicId(clinicId);
                doctor.activate();
                doctor.validateShift();
                validateClinicShift(clinicId, shiftStart, shiftEnd, userId);
                return doctorRepository.save(doctor);
            } else {
                throw new IllegalStateException(
                    "El usuario ya está registrado como doctor activo en el sistema. " +
                    "ID: " + userId + ", Nombre: " + doctor.getName()
                );
            }
        }

        Doctor doctor = new Doctor();
        doctor.setId(userId);
        doctor.setName(name);
        doctor.setShiftStart(shiftStart);
        doctor.setShiftEnd(shiftEnd);
        doctor.setClinicId(clinicId);
        doctor.setStatus(Doctor.DoctorStatus.ACTIVE);
        doctor.setCreatedAt(LocalDateTime.now());
        doctor.validateShift();
        validateClinicShift(clinicId, shiftStart, shiftEnd, null);
        return doctorRepository.save(doctor);
    }
    
    /**
     * Updates an existing doctor's information.
     * 
     * <p>This method allows updating the doctor's name, specialty, and shift times.
     * The shift duration is validated to ensure it remains exactly 8 hours.
     * 
     * @param doctorId the ID of the doctor to update
     * @param name the new name (or existing name if unchanged)
     * @param specialty the new specialty (or existing specialty if unchanged)
     * @param shiftStart the new shift start time
     * @param shiftEnd the new shift end time
     * @return the updated doctor
     * @throws DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalArgumentException if the new shift duration is not exactly 8 hours
     */
    public Doctor updateDoctor(String doctorId, String name,
                               LocalTime shiftStart, LocalTime shiftEnd, String clinicId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new DoctorNotFoundException(
                "Doctor no encontrado con ID: " + doctorId
            ));

        doctor.setName(name);
        doctor.setShiftStart(shiftStart);
        doctor.setShiftEnd(shiftEnd);
        doctor.setClinicId(clinicId);
        doctor.validateShift();
        validateClinicShift(clinicId, shiftStart, shiftEnd, doctorId);
        return doctorRepository.save(doctor);
    }

    // Verifica que ningún otro doctor activo en la misma clínica tenga un turno solapado.
    // excludeDoctorId es el propio doctor al actualizar (se excluye de la comparación).
    private void validateClinicShift(String clinicId, LocalTime shiftStart, LocalTime shiftEnd,
                                     String excludeDoctorId) {
        doctorRepository.findActiveByClinicId(clinicId).stream()
            .filter(d -> !d.getId().equals(excludeDoctorId))
            .filter(d -> shiftsOverlap(shiftStart, d.getShiftStart()))
            .findFirst()
            .ifPresent(d -> {
                throw new ClinicShiftConflictException(
                    "La clínica ya tiene un doctor asignado en ese horario: " +
                    d.getName() + " (" + d.getShiftStart() + " - " + d.getShiftEnd() + ")"
                );
            });
    }

    // Dos turnos de exactamente 8 horas se solapan si la diferencia circular entre
    // sus horas de inicio es menor a 480 minutos.
    private boolean shiftsOverlap(LocalTime s1, LocalTime s2) {
        int m1 = s1.getHour() * 60 + s1.getMinute();
        int m2 = s2.getHour() * 60 + s2.getMinute();
        int diff = Math.abs(m1 - m2);
        return Math.min(diff, 1440 - diff) < 480;
    }
    
    /**
     * Deactivates a doctor, preventing them from being assigned to new appointments.
     * 
     * <p>This is a soft delete operation. The doctor record remains in the system
     * but their status is changed to INACTIVE. Existing appointments are not affected.
     * 
     * @param doctorId the ID of the doctor to deactivate
     * @throws DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalStateException if the doctor is already inactive
     */
    public void deactivateDoctor(String doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new DoctorNotFoundException(
                "Doctor no encontrado con ID: " + doctorId
            ));
        
        doctor.deactivate();
        doctorRepository.save(doctor);
    }
    
    /**
     * Marks a specific date as unavailable for a doctor (day-off).
     * 
     * <p>This method creates a day-off record that prevents the doctor from being
     * assigned to appointments on that date. Common reasons include:
     * <ul>
     *   <li>Vacation</li>
     *   <li>Personal Day</li>
     *   <li>Sick Leave</li>
     *   <li>Training/Conference</li>
     * </ul>
     * 
     * <p><b>Example:</b>
     * <pre>
     * shiftManager.markDayOff(
     *     "doctor-123",
     *     LocalDate.of(2024, 5, 20),
     *     "Vacation"
     * );
     * // Result: Doctor unavailable on 2024-05-20
     * </pre>
     * 
     * @param doctorId the ID of the doctor
     * @param date the date to mark as unavailable
     * @param reason the reason for unavailability
     * @throws DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalArgumentException if reason is null or empty
     */
    public void markDayOff(String doctorId, LocalDate date, String reason) {
        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
            .orElseThrow(() -> new DoctorNotFoundException(
                "Doctor no encontrado con ID: " + doctorId
            ));
        
        // Create availability record
        DoctorAvailability availability = new DoctorAvailability();
        availability.setId(UUID.randomUUID().toString());
        availability.setDoctorId(doctorId);
        availability.setDate(date);
        availability.setCreatedAt(LocalDateTime.now());
        
        // Mark as unavailable with reason
        availability.markUnavailable(reason);
        
        availabilityRepository.save(availability);
    }
    
    /**
     * Marks a date range as unavailable for a doctor (vacation period).
     * 
     * <p>This is a convenience method for marking multiple consecutive dates as unavailable.
     * It creates one day-off record for each date in the range [startDate, endDate] (inclusive).
     * 
     * <p><b>Example:</b>
     * <pre>
     * shiftManager.markVacationPeriod(
     *     "doctor-123",
     *     LocalDate.of(2024, 5, 20),
     *     LocalDate.of(2024, 5, 25),
     *     "Summer Vacation"
     * );
     * // Result: Doctor unavailable from May 20 to May 25 (6 days)
     * </pre>
     * 
     * @param doctorId the ID of the doctor
     * @param startDate the first date of the vacation period (inclusive)
     * @param endDate the last date of the vacation period (inclusive)
     * @param reason the reason for unavailability
     * @throws DoctorNotFoundException if the doctor doesn't exist
     * @throws IllegalArgumentException if startDate is after endDate
     */
    public void markVacationPeriod(String doctorId, LocalDate startDate, LocalDate endDate, String reason) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser posterior a la fecha de fin. " +
                "Inicio: " + startDate + ", Fin: " + endDate
            );
        }
        
        // Mark each date in the range as unavailable
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            markDayOff(doctorId, currentDate, reason);
            currentDate = currentDate.plusDays(1);
        }
    }
    
    /**
     * Removes a day-off record, restoring the doctor's availability for that date.
     * 
     * <p>This method is used when a previously marked day-off is cancelled
     * (e.g., vacation plans changed).
     * 
     * <p><b>Example:</b>
     * <pre>
     * // Doctor was marked unavailable
     * shiftManager.markDayOff("doctor-123", LocalDate.of(2024, 5, 20), "Vacation");
     * 
     * // Plans changed, restore availability
     * shiftManager.removeDayOff("doctor-123", LocalDate.of(2024, 5, 20));
     * // Result: Doctor available again on 2024-05-20
     * </pre>
     * 
     * @param doctorId the ID of the doctor
     * @param date the date to restore availability for
     */
    public void removeDayOff(String doctorId, LocalDate date) {
        availabilityRepository.deleteByDoctorIdAndDate(doctorId, date);
    }
    
    /**
     * Retrieves all day-off records for a specific doctor.
     * 
     * <p>This method returns all availability records for the doctor,
     * which can be used to display a calendar view of their days off.
     * 
     * <p><b>Example:</b>
     * <pre>
     * List&lt;DoctorAvailability&gt; daysOff = shiftManager.getDaysOff("doctor-123");
     * // Result: List of all day-off records for the doctor
     * </pre>
     * 
     * @param doctorId the ID of the doctor
     * @return list of all day-off records for the doctor
     * @throws DoctorNotFoundException if the doctor doesn't exist
     */
    public java.util.List<DoctorAvailability> getDaysOff(String doctorId) {
        // Validate doctor exists
        doctorRepository.findById(doctorId)
            .orElseThrow(() -> new DoctorNotFoundException(
                "Doctor no encontrado con ID: " + doctorId
            ));
        
        // Return all availability records for the doctor
        return availabilityRepository.findByDoctorId(doctorId);
    }
}
