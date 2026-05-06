package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.Appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Output port (repository interface) for Appointment entity persistence.
 * 
 * <p>This interface defines the contract for appointment data access operations.
 * It follows hexagonal architecture principles by defining the port in the domain layer,
 * while the actual implementation (adapter) resides in the infrastructure layer.
 * 
 * <p>The infrastructure layer will provide a JPA-based implementation of this interface.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
public interface AppointmentRepository {
    
    /**
     * Saves an appointment entity (create or update).
     * 
     * @param appointment the appointment to save
     * @return the saved appointment with generated ID if it was a new entity
     */
    Appointment save(Appointment appointment);
    
    /**
     * Finds an appointment by its unique identifier.
     * 
     * @param id the appointment ID
     * @return an Optional containing the appointment if found, empty otherwise
     */
    Optional<Appointment> findById(String id);
    
    /**
     * Finds all appointments for a specific doctor on a specific date.
     * 
     * <p>This method is used by the workload balancing algorithm to count
     * how many appointments each doctor has on a given date.
     * 
     * @param doctorId the doctor ID
     * @param date the appointment date
     * @return list of appointments for the doctor on that date
     */
    List<Appointment> findByDoctorIdAndDate(String doctorId, LocalDate date);
    
    /**
     * Finds all appointments for a specific patient.
     * 
     * @param patientId the patient ID
     * @return list of appointments for the patient
     */
    List<Appointment> findByPatientId(String patientId);
    
    /**
     * Finds all appointments for a specific date.
     * 
     * @param date the appointment date
     * @return list of appointments on that date
     */
    List<Appointment> findByDate(LocalDate date);
    
    /**
     * Finds all appointments in the system.
     *
     * @return list of all appointments
     */
    List<Appointment> findAll();

    /**
     * Finds all appointments for a specific doctor (all dates).
     *
     * @param doctorId the doctor ID
     * @return list of appointments for the doctor
     */
    List<Appointment> findByDoctorId(String doctorId);

    /**
     * Deletes an appointment by its unique identifier.
     *
     * @param id the appointment ID
     */
    void deleteById(String id);

    /**
     * Finds all active appointments that do not have an associated triage record.
     * 
     * <p>This method returns appointments that are in ACTIVE status and have no
     * corresponding triage record. It is used by triage staff to identify which
     * patients are waiting for triage assessment.
     * 
     * <p>The implementation should use a NOT EXISTS subquery for optimal performance:
     * <pre>
     * SELECT * FROM appointments a
     * WHERE a.status = 'ACTIVE'
     * AND NOT EXISTS (
     *     SELECT 1 FROM triages t
     *     WHERE t.appointment_id = a.id
     * )
     * </pre>
     * 
     * @return list of active appointments without triage records
     */
    List<Appointment> findPendingTriage();
    
    /**
     * Finds all appointments that do not have an associated invoice.
     * 
     * <p>This method is used for manual reconciliation when the Billing Service
     * was unavailable during appointment creation. It returns appointments where
     * the invoiceId field is NULL, indicating that no invoice was created.</p>
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-12.1: Consultar citas sin factura para reconciliación manual</li>
     * </ul>
     * 
     * @return list of appointments without invoice (invoiceId is NULL)
     */
    List<Appointment> findAppointmentsWithoutInvoice();
    
    /**
     * Updates an existing appointment.
     * 
     * <p>This method is used for manual reconciliation to update the invoiceId
     * of an appointment after the invoice has been created manually.</p>
     * 
     * @param appointment the appointment to update
     * @return the updated appointment
     */
    Appointment update(Appointment appointment);
}
