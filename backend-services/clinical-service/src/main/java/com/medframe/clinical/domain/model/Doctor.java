package com.medframe.clinical.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Doctor domain entity representing a medical professional with assigned work shifts.
 * 
 * <p>This is a pure domain model with business logic for shift validation and availability checking.
 * It follows hexagonal architecture principles with no infrastructure dependencies.
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Shift duration must be exactly 8 hours</li>
 *   <li>Doctor can be ACTIVE or INACTIVE</li>
 *   <li>Only ACTIVE doctors can be assigned to appointments</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    
    /**
     * Unique identifier for the doctor.
     */
    private String id;
    
    /**
     * Full name of the doctor.
     */
    private String name;
    
    /**
     * Start time of the doctor's work shift (e.g., 08:00 for morning shift).
     */
    private LocalTime shiftStart;
    
    /**
     * End time of the doctor's work shift (e.g., 16:00 for morning shift).
     */
    private LocalTime shiftEnd;
    
    /**
     * Current status of the doctor (ACTIVE or INACTIVE).
     */
    private DoctorStatus status;
    
    /**
     * ID of the clinic this doctor is assigned to.
     */
    private String clinicId;

    /**
     * Timestamp when the doctor record was created.
     */
    private LocalDateTime createdAt;
    
    /**
     * Doctor status enum.
     */
    public enum DoctorStatus {
        /**
         * Doctor is active and can be assigned to appointments.
         */
        ACTIVE,
        
        /**
         * Doctor is inactive and cannot be assigned to appointments.
         */
        INACTIVE
    }
    
    /**
     * Validates that the shift duration is exactly 8 hours.
     * 
     * <p>This method enforces the business rule that all doctor shifts must be 8 hours long.
     * This ensures consistent scheduling and capacity planning across the system.
     * 
     * @throws IllegalArgumentException if shift duration is not exactly 8 hours
     */
    public void validateShift() {
        if (shiftStart == null || shiftEnd == null) {
            throw new IllegalArgumentException(
                "Los horarios de turno no pueden ser nulos. " +
                "shiftStart: " + shiftStart + ", shiftEnd: " + shiftEnd
            );
        }
        
        int startMin = shiftStart.getHour() * 60 + shiftStart.getMinute();
        int endMin   = shiftEnd.getHour()   * 60 + shiftEnd.getMinute();
        if (endMin == 0) endMin = 24 * 60;          // 00:00 = fin de día
        if (endMin <= startMin) endMin += 24 * 60;  // turno nocturno
        long hours = (endMin - startMin) / 60;

        if (hours != 8) {
            throw new IllegalArgumentException(
                "El turno debe ser de exactamente 8 horas. " +
                "Duración actual: " + hours + " horas " +
                "(desde " + shiftStart + " hasta " + shiftEnd + ")"
            );
        }
    }
    
    /**
     * Checks if the doctor works at a specific time.
     * 
     * <p>A doctor works at a given time if the time falls within their shift window:
     * <ul>
     *   <li>Time must be greater than or equal to shift start</li>
     *   <li>Time must be strictly less than shift end</li>
     * </ul>
     * 
     * <p><b>Examples:</b>
     * <ul>
     *   <li>Shift 08:00-16:00, time 10:00 → true</li>
     *   <li>Shift 08:00-16:00, time 08:00 → true (inclusive start)</li>
     *   <li>Shift 08:00-16:00, time 16:00 → false (exclusive end)</li>
     *   <li>Shift 08:00-16:00, time 07:00 → false</li>
     * </ul>
     * 
     * @param time the time to check
     * @return true if the doctor works at the specified time, false otherwise
     */
    public boolean worksAt(LocalTime time) {
        if (time == null) {
            return false;
        }
        
        int startMin = shiftStart.getHour() * 60 + shiftStart.getMinute();
        int endMin   = shiftEnd.getHour()   * 60 + shiftEnd.getMinute();
        int timeMin  = time.getHour()        * 60 + time.getMinute();
        if (endMin == 0) endMin = 24 * 60;
        if (endMin <= startMin) {
            // Turno nocturno: cubre startMin..medianoche y medianoche..endMin
            return timeMin >= startMin || timeMin < endMin;
        }
        return timeMin >= startMin && timeMin < endMin;
    }
    
    /**
     * Deactivates the doctor, preventing them from being assigned to new appointments.
     * 
     * <p>This is a soft delete operation. The doctor record remains in the system
     * but cannot be assigned to new appointments. Existing appointments are not affected.
     * 
     * @throws IllegalStateException if the doctor is already inactive
     */
    public void deactivate() {
        if (this.status == DoctorStatus.INACTIVE) {
            throw new IllegalStateException(
                "El doctor ya está inactivo. ID: " + this.id + ", Nombre: " + this.name
            );
        }
        
        this.status = DoctorStatus.INACTIVE;
    }
    
    /**
     * Activates the doctor, allowing them to be assigned to appointments.
     * 
     * @throws IllegalStateException if the doctor is already active
     */
    public void activate() {
        if (this.status == DoctorStatus.ACTIVE) {
            throw new IllegalStateException(
                "El doctor ya está activo. ID: " + this.id + ", Nombre: " + this.name
            );
        }
        
        this.status = DoctorStatus.ACTIVE;
    }
    
    /**
     * Checks if the doctor is currently active.
     * 
     * @return true if status is ACTIVE, false otherwise
     */
    public boolean isActive() {
        return this.status == DoctorStatus.ACTIVE;
    }
}
