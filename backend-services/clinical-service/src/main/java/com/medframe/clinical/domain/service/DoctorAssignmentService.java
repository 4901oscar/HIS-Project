package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.NoAvailableDoctorException;
import com.medframe.clinical.domain.model.Doctor;
import com.medframe.clinical.domain.model.DoctorAvailability;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.port.out.DoctorAvailabilityRepository;
import com.medframe.clinical.domain.port.out.DoctorRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Domain service responsible for automatic doctor assignment to appointments.
 * 
 * <p>This service implements the workload balancing algorithm that assigns appointments
 * to the doctor with the lowest daily workload. It follows hexagonal architecture principles
 * with no infrastructure dependencies (no Spring annotations).
 * 
 * <p><b>Assignment Algorithm:</b>
 * <ol>
 *   <li>Find all active doctors working during the requested time slot</li>
 *   <li>Exclude doctors with day-off records for the requested date</li>
 *   <li>Count total appointments for each remaining doctor on that date</li>
 *   <li>Select the doctor with the fewest appointments (least-loaded)</li>
 *   <li>If multiple doctors have equal minimum workload, select alphabetically by name</li>
 * </ol>
 * 
 * <p><b>Business Rules:</b>
 * <ul>
 *   <li>Only ACTIVE doctors can be assigned</li>
 *   <li>Doctors with day-offs are excluded</li>
 *   <li>Assignment is based on daily workload (not hourly)</li>
 *   <li>Alphabetical tiebreaker ensures deterministic results</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public class DoctorAssignmentService {
    
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    
    /**
     * Constructs a new DoctorAssignmentService with required dependencies.
     * 
     * @param doctorRepository repository for doctor data access
     * @param availabilityRepository repository for doctor availability data access
     * @param appointmentRepository repository for appointment data access
     */
    public DoctorAssignmentService(DoctorRepository doctorRepository,
                                    DoctorAvailabilityRepository availabilityRepository,
                                    AppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }
    
    /**
     * Assigns a doctor to an appointment using the least-loaded algorithm.
     * 
     * <p>This method implements the core business logic for automatic doctor assignment.
     * It ensures fair workload distribution across all available doctors.
     * 
     * <p><b>Algorithm Steps:</b>
     * <ol>
     *   <li>Find doctors working during the requested time slot</li>
     *   <li>Filter out doctors with day-offs</li>
     *   <li>Calculate workload for each available doctor</li>
     *   <li>Select doctor with minimum workload</li>
     *   <li>Apply alphabetical tiebreaker if needed</li>
     * </ol>
     * 
     * <p><b>Example:</b>
     * <pre>
     * // Assign doctor for appointment on 2024-05-20 at 10:00
     * String doctorId = assignmentService.assignDoctor(
     *     LocalDate.of(2024, 5, 20),
     *     LocalTime.of(10, 0)
     * );
     * // Returns: "doctor-123" (the doctor with lowest workload)
     * </pre>
     * 
     * @param date the appointment date
     * @param time the appointment time
     * @return the ID of the assigned doctor
     * @throws NoAvailableDoctorException if no doctors are available for the requested time
     */
    public String assignDoctor(LocalDate date, LocalTime time) {
        // Step 1: Find doctors working during this time slot
        List<Doctor> workingDoctors = findDoctorsWorkingAt(time);
        
        if (workingDoctors.isEmpty()) {
            throw new NoAvailableDoctorException(
                "No hay doctores trabajando en el horario " + time + ". " +
                "Por favor seleccione otro horario o contacte al administrador."
            );
        }
        
        // Step 2: Exclude doctors with day-off records
        List<Doctor> availableDoctors = filterAvailableDoctors(workingDoctors, date);
        
        if (availableDoctors.isEmpty()) {
            throw new NoAvailableDoctorException(
                "No hay doctores disponibles para la fecha " + date + " en el horario " + time + ". " +
                "Todos los doctores del turno tienen días libres. " +
                "Por favor seleccione otra fecha."
            );
        }
        
        // Step 3: Count appointments for each doctor on this date
        Map<String, Integer> workloadMap = calculateWorkload(availableDoctors, date);
        
        // Step 4: Find doctor with minimum workload
        Doctor assignedDoctor = selectLeastLoadedDoctor(availableDoctors, workloadMap);
        
        return assignedDoctor.getId();
    }
    
    /**
     * Finds all active doctors working at a specific time.
     * 
     * <p>A doctor works at a given time if:
     * <ul>
     *   <li>Their status is ACTIVE</li>
     *   <li>The time falls within their shift window (shiftStart <= time < shiftEnd)</li>
     * </ul>
     * 
     * @param time the time to check
     * @return list of doctors working at the specified time
     */
    private List<Doctor> findDoctorsWorkingAt(LocalTime time) {
        List<Doctor> allActive = doctorRepository.findAllActive();
        
        return allActive.stream()
            .filter(doctor -> doctor.worksAt(time))
            .collect(Collectors.toList());
    }
    
    /**
     * Filters out doctors who have day-off records for the specified date.
     * 
     * <p>A doctor is considered available if:
     * <ul>
     *   <li>No availability record exists for that date (default: available), OR</li>
     *   <li>An availability record exists with isAvailable = true</li>
     * </ul>
     * 
     * <p>A doctor is considered unavailable if:
     * <ul>
     *   <li>An availability record exists with isAvailable = false (day-off)</li>
     * </ul>
     * 
     * @param doctors the list of doctors to filter
     * @param date the date to check
     * @return list of doctors available on the specified date
     */
    private List<Doctor> filterAvailableDoctors(List<Doctor> doctors, LocalDate date) {
        return doctors.stream()
            .filter(doctor -> {
                List<DoctorAvailability> dayOffs = 
                    availabilityRepository.findByDoctorIdAndDate(doctor.getId(), date);
                
                // No record means available (default)
                if (dayOffs.isEmpty()) {
                    return true;
                }
                
                // Check if all records indicate availability
                // (typically there's only one record per doctor per date)
                return dayOffs.stream().allMatch(DoctorAvailability::isAvailable);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calculates the workload (appointment count) for each doctor on a specific date.
     * 
     * <p>Workload is defined as the total number of appointments assigned to a doctor
     * for the entire day, regardless of time slots.
     * 
     * <p><b>Example:</b>
     * <pre>
     * Doctor A: 5 appointments on 2024-05-20
     * Doctor B: 3 appointments on 2024-05-20
     * Doctor C: 7 appointments on 2024-05-20
     * 
     * Result: {"doctor-A": 5, "doctor-B": 3, "doctor-C": 7}
     * </pre>
     * 
     * @param doctors the list of doctors to calculate workload for
     * @param date the date to count appointments
     * @return map of doctor ID to appointment count
     */
    private Map<String, Integer> calculateWorkload(List<Doctor> doctors, LocalDate date) {
        Map<String, Integer> workload = new HashMap<>();
        
        for (Doctor doctor : doctors) {
            int count = appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date).size();
            workload.put(doctor.getId(), count);
        }
        
        return workload;
    }
    
    /**
     * Selects the doctor with the least workload from the available doctors.
     * 
     * <p>Selection criteria (in order of priority):
     * <ol>
     *   <li>Doctor with minimum appointment count for the date</li>
     *   <li>If multiple doctors have equal minimum count, select alphabetically by name</li>
     * </ol>
     * 
     * <p><b>Example:</b>
     * <pre>
     * Available doctors:
     * - Dr. Alice (3 appointments)
     * - Dr. Bob (3 appointments)
     * - Dr. Charlie (5 appointments)
     * 
     * Result: Dr. Alice (minimum workload, alphabetically first among tied doctors)
     * </pre>
     * 
     * @param doctors the list of available doctors
     * @param workload map of doctor ID to appointment count
     * @return the selected doctor
     * @throws NoAvailableDoctorException if the list of doctors is empty
     */
    private Doctor selectLeastLoadedDoctor(List<Doctor> doctors, Map<String, Integer> workload) {
        return doctors.stream()
            .min(Comparator
                .comparingInt((Doctor d) -> workload.get(d.getId()))  // Primary: minimum workload
                .thenComparing(Doctor::getName))                       // Tiebreaker: alphabetical
            .orElseThrow(() -> new NoAvailableDoctorException(
                "No se pudo asignar doctor. La lista de doctores disponibles está vacía."
            ));
    }
}
