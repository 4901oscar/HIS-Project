package com.medframe.clinical.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DoctorAvailability domain entity representing a doctor's availability status for a specific date.
 * 
 * <p>This entity is used to track day-off records, vacations, and other periods when a doctor
 * is not available for appointments. It follows hexagonal architecture principles with no
 * infrastructure dependencies.
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Each doctor can have at most one availability record per date</li>
 *   <li>isAvailable = false indicates a day-off (vacation, personal day, etc.)</li>
 *   <li>isAvailable = true or no record means the doctor is available</li>
 *   <li>Day-off records can be created, restored, or removed</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailability {
    
    /**
     * Unique identifier for the availability record.
     */
    private String id;
    
    /**
     * ID of the doctor this availability record belongs to.
     */
    private String doctorId;
    
    /**
     * The date for which this availability status applies.
     */
    private LocalDate date;
    
    /**
     * Availability status for the date.
     * <ul>
     *   <li>true: Doctor is available (normal working day)</li>
     *   <li>false: Doctor is not available (day-off, vacation, etc.)</li>
     * </ul>
     */
    private boolean isAvailable;
    
    /**
     * Reason for unavailability (e.g., "Vacation", "Personal Day", "Sick Leave").
     * This field is only relevant when isAvailable = false.
     */
    private String reason;
    
    /**
     * Timestamp when this availability record was created.
     */
    private LocalDateTime createdAt;
    
    /**
     * ID of the user who created this availability record (typically an administrator).
     */
    private String createdBy;
    
    /**
     * Marks the doctor as unavailable for the date with a specified reason.
     * 
     * <p>This method is used to create day-off records for vacations, personal days,
     * sick leave, or other absences.
     * 
     * <p><b>Example:</b>
     * <pre>
     * DoctorAvailability availability = new DoctorAvailability();
     * availability.setDoctorId("doctor-123");
     * availability.setDate(LocalDate.of(2024, 5, 20));
     * availability.markUnavailable("Vacation");
     * // Result: isAvailable = false, reason = "Vacation"
     * </pre>
     * 
     * @param reason the reason for unavailability (e.g., "Vacation", "Sick Leave")
     * @throws IllegalArgumentException if reason is null or empty
     */
    public void markUnavailable(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El motivo de no disponibilidad no puede estar vacío. " +
                "Doctor ID: " + this.doctorId + ", Fecha: " + this.date
            );
        }
        
        this.isAvailable = false;
        this.reason = reason.trim();
    }
    
    /**
     * Restores the doctor's availability for the date.
     * 
     * <p>This method is used to cancel a previously marked day-off, making the doctor
     * available again for appointments on that date.
     * 
     * <p><b>Example:</b>
     * <pre>
     * // Doctor was marked unavailable
     * availability.markUnavailable("Vacation");
     * 
     * // Plans changed, restore availability
     * availability.restore();
     * // Result: isAvailable = true, reason = null
     * </pre>
     */
    public void restore() {
        this.isAvailable = true;
        this.reason = null;
    }
    
    /**
     * Checks if the doctor is unavailable (has a day-off) for the date.
     * 
     * @return true if isAvailable = false, false otherwise
     */
    public boolean isUnavailable() {
        return !this.isAvailable;
    }
    
    /**
     * Checks if this availability record represents a day-off.
     * 
     * <p>A day-off is defined as isAvailable = false with a non-null reason.
     * 
     * @return true if this is a day-off record, false otherwise
     */
    public boolean isDayOff() {
        return !this.isAvailable && this.reason != null && !this.reason.trim().isEmpty();
    }
}
