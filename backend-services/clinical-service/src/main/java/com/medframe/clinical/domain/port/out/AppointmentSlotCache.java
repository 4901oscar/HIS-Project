package com.medframe.clinical.domain.port.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

/**
 * Output port (cache interface) for appointment slot reservation management.
 * 
 * <p>This interface defines the contract for Redis-based slot caching operations.
 * It follows hexagonal architecture principles by defining the port in the domain layer,
 * while the actual implementation (adapter) resides in the infrastructure layer.
 * 
 * <p>The infrastructure layer will provide a Redis-based implementation of this interface
 * using atomic operations to prevent double-booking.
 * 
 * <p><b>Redis Key Structure:</b>
 * <pre>
 * appointment:slots:{doctorId}:{date} → Set of occupied time slots
 * Example: appointment:slots:doctor-123:2024-05-20 → {10:00, 10:30, 14:00}
 * </pre>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public interface AppointmentSlotCache {
    
    /**
     * Reserves a time slot for a doctor on a specific date.
     * 
     * <p>This operation must be atomic to prevent race conditions and double-booking.
     * It adds the time slot to the set of occupied slots for the doctor on that date.
     * 
     * <p><b>Example:</b>
     * <pre>
     * boolean reserved = slotCache.reserveSlot(
     *     "doctor-123",
     *     LocalDate.of(2024, 5, 20),
     *     LocalTime.of(10, 0)
     * );
     * // Returns: true if slot was available and reserved, false if already occupied
     * </pre>
     * 
     * @param doctorId the doctor ID
     * @param date the appointment date
     * @param time the appointment time
     * @return true if the slot was successfully reserved, false if already occupied
     */
    boolean reserveSlot(String doctorId, LocalDate date, LocalTime time);
    
    /**
     * Releases a previously reserved time slot.
     * 
     * <p>This operation is used when an appointment is cancelled or rescheduled.
     * It removes the time slot from the set of occupied slots.
     * 
     * <p><b>Example:</b>
     * <pre>
     * slotCache.releaseSlot(
     *     "doctor-123",
     *     LocalDate.of(2024, 5, 20),
     *     LocalTime.of(10, 0)
     * );
     * // Result: Slot is now available for booking again
     * </pre>
     * 
     * @param doctorId the doctor ID
     * @param date the appointment date
     * @param time the appointment time
     */
    void releaseSlot(String doctorId, LocalDate date, LocalTime time);
    
    /**
     * Gets all occupied time slots for a doctor on a specific date.
     * 
     * <p>This method is used by the capacity controller to determine which slots
     * are already booked.
     * 
     * <p><b>Example:</b>
     * <pre>
     * Set<LocalTime> occupied = slotCache.getOccupiedSlots(
     *     "doctor-123",
     *     LocalDate.of(2024, 5, 20)
     * );
     * // Returns: {10:00, 10:30, 14:00} (slots with appointments)
     * </pre>
     * 
     * @param doctorId the doctor ID
     * @param date the appointment date
     * @return set of occupied time slots (empty set if no appointments)
     */
    Set<LocalTime> getOccupiedSlots(String doctorId, LocalDate date);
    
    /**
     * Checks if a specific time slot is occupied.
     * 
     * <p>This is a convenience method that checks if a slot exists in the occupied set.
     * 
     * @param doctorId the doctor ID
     * @param date the appointment date
     * @param time the appointment time
     * @return true if the slot is occupied, false if available
     */
    boolean isSlotOccupied(String doctorId, LocalDate date, LocalTime time);
    
    /**
     * Clears all occupied slots for a doctor on a specific date.
     */
    void clearSlots(String doctorId, LocalDate date);

    /**
     * Temporarily holds a (date, time) pair for a session (10-minute TTL).
     * Atomically replaces any previous hold by the same session.
     *
     * @return true if the slot was held successfully, false if already held by another session
     */
    boolean holdTimeSlot(String sessionId, LocalDate date, LocalTime time);

    /**
     * Releases whatever (date, time) the session was holding, if any.
     */
    void releaseTimeSlotHold(String sessionId);

    /**
     * Returns all (date, time) slots that are held by sessions OTHER than the given one.
     * Used to filter out held slots from the available-slots response.
     */
    Set<LocalTime> getHeldByOthers(String sessionId, LocalDate date);
}
