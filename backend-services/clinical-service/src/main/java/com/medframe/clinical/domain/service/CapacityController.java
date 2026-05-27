package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.model.DoctorAvailability;
import com.medframe.clinical.domain.port.out.AppointmentSlotCache;
import com.medframe.clinical.domain.port.out.DoctorAvailabilityRepository;
import com.medframe.clinical.domain.port.out.DoctorRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain service responsible for calculating appointment capacity and available slots.
 * 
 * <p>This service determines which time slots are available for appointments based on:
 * <ul>
 *   <li>Doctor work schedules (shifts)</li>
 *   <li>Doctor availability (day-offs)</li>
 *   <li>Capacity constraints (2 appointments per hour per doctor)</li>
 *   <li>Existing appointments (occupied slots from Redis cache)</li>
 * </ul>
 * 
 * <p>It follows hexagonal architecture principles with no infrastructure dependencies
 * (no Spring annotations).
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Each doctor can handle 2 appointments per hour (slots at :00 and :30)</li>
 *   <li>Total capacity for a time slot = (number of available doctors) × 2</li>
 *   <li>A slot is available if occupied count < total capacity</li>
 *   <li>Doctors with day-offs do not contribute to capacity</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class CapacityController {
    
    /**
     * Number of appointment slots per hour per doctor.
     * Each doctor can handle 2 appointments per hour (one at :00, one at :30).
     */
    private static final int SLOTS_PER_HOUR_PER_DOCTOR = 2;
    
    /**
     * Duration of each appointment slot in minutes.
     * Slots are at 30-minute intervals (00:00, 00:30, 01:00, 01:30, ...).
     */
    private static final int SLOT_DURATION_MINUTES = 30;
    
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentSlotCache slotCache;
    
    /**
     * Constructs a new CapacityController with required dependencies.
     * 
     * @param doctorRepository repository for doctor data access
     * @param availabilityRepository repository for doctor availability data access
     * @param slotCache cache for occupied appointment slots (Redis)
     */
    public CapacityController(DoctorRepository doctorRepository,
                              DoctorAvailabilityRepository availabilityRepository,
                              AppointmentSlotCache slotCache) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.slotCache = slotCache;
    }
    
    /**
     * Calculates available time slots for a given date across all doctors.
     * 
     * <p>This method generates all possible time slots (48 slots from 00:00 to 23:30)
     * and filters them based on capacity availability. A slot is included in the result
     * if at least one appointment space is available.
     * 
     * <p><b>Algorithm:</b>
     * <ol>
     *   <li>Generate all 48 time slots (00:00 to 23:30, every 30 minutes)</li>
     *   <li>For each slot, determine which shift covers it</li>
     *   <li>Count active doctors in that shift without day-offs</li>
     *   <li>Calculate total capacity = doctors × 2 slots/hour</li>
     *   <li>Count occupied slots from Redis cache</li>
     *   <li>If occupied < capacity, include slot in results</li>
     * </ol>
     * 
     * <p><b>Example:</b>
     * <pre>
     * List<LocalTime> slots = capacityController.findAvailableSlots(
     *     LocalDate.of(2024, 5, 20)
     * );
     * // Returns: [08:00, 08:30, 09:00, 09:30, ...] (slots with available capacity)
     * </pre>
     * 
     * @param date the date to check for available slots
     * @return list of time slots with available capacity
     */
    public List<LocalTime> findAvailableSlots(LocalDate date) {
        List<LocalTime> allSlots = generateAllSlots();
        List<LocalTime> availableSlots = new ArrayList<>();
        
        for (LocalTime slot : allSlots) {
            if (hasCapacity(slot, date)) {
                availableSlots.add(slot);
            }
        }
        
        return availableSlots;
    }
    
    /**
     * Generates all possible time slots for a day.
     * 
     * <p>This method creates 48 time slots from 00:00 to 23:30 at 30-minute intervals.
     * These represent all possible appointment times in a 24-hour period.
     * 
     * <p><b>Generated slots:</b>
     * <pre>
     * 00:00, 00:30, 01:00, 01:30, 02:00, 02:30, ..., 23:00, 23:30
     * </pre>
     * 
     * @return list of 48 time slots
     */
    private List<LocalTime> generateAllSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = LocalTime.of(0, 0);
        LocalTime end = LocalTime.of(23, 30);
        
        while (!current.isAfter(end)) {
            slots.add(current);
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }
        
        return slots;
    }
    
    /**
     * Checks if a specific time slot has available capacity on a given date.
     * 
     * <p>A slot has capacity if the number of occupied appointments is less than
     * the total capacity (number of available doctors × 2).
     * 
     * <p><b>Capacity Calculation:</b>
     * <pre>
     * Total Capacity = (Available Doctors) × 2
     * Available Doctors = Active doctors working at slot time without day-offs
     * 
     * Example:
     * - 2 doctors working morning shift (08:00-16:00)
     * - 0 doctors with day-offs
     * - Total capacity at 10:00 = 2 × 2 = 4 appointments
     * - Occupied slots = 3
     * - Has capacity? Yes (3 < 4)
     * </pre>
     * 
     * @param slot the time slot to check
     * @param date the date to check
     * @return true if the slot has available capacity, false otherwise
     */
    private boolean hasCapacity(LocalTime slot, LocalDate date) {
        // Find doctors working during this slot
        List<Doctor> workingDoctors = doctorRepository.findAllActive().stream()
            .filter(doctor -> doctor.worksAt(slot))
            .collect(Collectors.toList());
        
        // Exclude doctors with day-offs
        List<Doctor> availableDoctors = workingDoctors.stream()
            .filter(doctor -> {
                List<DoctorAvailability> dayOffs = 
                    availabilityRepository.findByDoctorIdAndDate(doctor.getId(), date);
                
                // No record means available (default)
                if (dayOffs.isEmpty()) {
                    return true;
                }
                
                // Check if all records indicate availability
                return dayOffs.stream().allMatch(DoctorAvailability::isAvailable);
            })
            .collect(Collectors.toList());
        
        // No doctors available means no capacity
        if (availableDoctors.isEmpty()) {
            return false;
        }
        
        // Calculate total capacity
        int totalCapacity = availableDoctors.size() * SLOTS_PER_HOUR_PER_DOCTOR;
        
        // Count occupied slots from Redis
        int occupiedCount = 0;
        for (Doctor doctor : availableDoctors) {
            Set<LocalTime> occupied = slotCache.getOccupiedSlots(doctor.getId(), date);
            if (occupied.contains(slot)) {
                occupiedCount++;
            }
        }
        
        // Has capacity if occupied < total capacity
        return occupiedCount < totalCapacity;
    }
    
    /**
     * Calculates the total capacity for a specific time slot on a given date.
     * 
     * <p>This method is useful for displaying capacity information to users
     * (e.g., "3 of 4 slots available").
     * 
     * @param slot the time slot
     * @param date the date
     * @return the total number of appointments that can be scheduled at this slot
     */
    public int calculateTotalCapacity(LocalTime slot, LocalDate date) {
        List<Doctor> workingDoctors = doctorRepository.findAllActive().stream()
            .filter(doctor -> doctor.worksAt(slot))
            .collect(Collectors.toList());
        
        List<Doctor> availableDoctors = workingDoctors.stream()
            .filter(doctor -> {
                List<DoctorAvailability> dayOffs = 
                    availabilityRepository.findByDoctorIdAndDate(doctor.getId(), date);
                return dayOffs.isEmpty() || dayOffs.stream().allMatch(DoctorAvailability::isAvailable);
            })
            .collect(Collectors.toList());
        
        return availableDoctors.size() * SLOTS_PER_HOUR_PER_DOCTOR;
    }
    
    /**
     * Calculates the number of occupied slots for a specific time slot on a given date.
     * 
     * <p>This method counts how many appointments are already scheduled at this slot
     * across all available doctors.
     * 
     * @param slot the time slot
     * @param date the date
     * @return the number of occupied appointment slots
     */
    public int calculateOccupiedCount(LocalTime slot, LocalDate date) {
        List<Doctor> workingDoctors = doctorRepository.findAllActive().stream()
            .filter(doctor -> doctor.worksAt(slot))
            .collect(Collectors.toList());
        
        List<Doctor> availableDoctors = workingDoctors.stream()
            .filter(doctor -> {
                List<DoctorAvailability> dayOffs = 
                    availabilityRepository.findByDoctorIdAndDate(doctor.getId(), date);
                return dayOffs.isEmpty() || dayOffs.stream().allMatch(DoctorAvailability::isAvailable);
            })
            .collect(Collectors.toList());
        
        int occupiedCount = 0;
        for (Doctor doctor : availableDoctors) {
            Set<LocalTime> occupied = slotCache.getOccupiedSlots(doctor.getId(), date);
            if (occupied.contains(slot)) {
                occupiedCount++;
            }
        }
        
        return occupiedCount;
    }
    
    /**
     * Calculates the number of available slots for a specific time slot on a given date.
     * 
     * <p>Available slots = Total capacity - Occupied count
     * 
     * @param slot the time slot
     * @param date the date
     * @return the number of available appointment slots
     */
    public int calculateAvailableCount(LocalTime slot, LocalDate date) {
        int totalCapacity = calculateTotalCapacity(slot, date);
        int occupiedCount = calculateOccupiedCount(slot, date);
        return Math.max(0, totalCapacity - occupiedCount);
    }
}
