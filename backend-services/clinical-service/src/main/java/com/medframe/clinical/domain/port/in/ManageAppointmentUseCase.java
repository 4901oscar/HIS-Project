package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.ScanResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public interface ManageAppointmentUseCase {
    List<LocalTime> findAvailableSlots(String doctorId, LocalDate date);

    /** Returns available time slots for a date across all active doctors (no doctorId needed). */
    List<LocalTime> findAvailableSlotsForDate(LocalDate date);
    
    /**
     * Creates an appointment with manual doctor selection (backward compatibility).
     * 
     * @param patientId the patient ID
     * @param doctorId the doctor ID (required)
     * @param date the appointment date
     * @param time the appointment time
     * @param notes optional notes
     * @param createdBy user ID creating the appointment
     * @return the created appointment
     */
    Appointment createAppointment(String patientId, String doctorId,
                                  LocalDate date, LocalTime time,
                                  String notes, String createdBy);
    
    /**
     * Creates an appointment with automatic doctor assignment.
     * 
     * @param patientId the patient ID
     * @param date the appointment date
     * @param time the appointment time
     * @param notes optional notes
     * @param createdBy user ID creating the appointment
     * @return the created appointment with automatically assigned doctor
     */
    Appointment createAppointmentWithAutoAssignment(String patientId,
                                                     LocalDate date, LocalTime time,
                                                     String notes, String createdBy);
    
    void activateAppointment(String appointmentId);
    void cancelAppointment(String appointmentId);

    /**
     * Scans a QR code and validates/activates the appointment based on time window.
     * 
     * @param appointmentId the appointment ID from QR code
     * @param scanTime the time when QR was scanned
     * @return ScanResult with status (EARLY, ACTIVE, MISSED) and message
     */
    ScanResult scanAndActivateAppointment(String appointmentId, LocalDateTime scanTime);

    /**
     * Temporarily holds a (date, time) slot for 10 minutes.
     * Returns true if the slot was held, false if already held by another session.
     */
    boolean holdSlot(String sessionId, LocalDate date, LocalTime time);

    /** Releases any hold owned by this session. */
    void releaseHold(String sessionId);

    /** All appointments — for ADMISSION / ADMIN. */
    List<Appointment> listAll();
    
    /**
     * Lists all appointments that do not have an associated invoice.
     * Used for manual reconciliation when Billing Service was unavailable.
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-12.1: Consultar citas sin factura para reconciliación manual</li>
     * </ul>
     * 
     * @return list of appointments without invoice (invoiceId is NULL)
     */
    List<Appointment> listAppointmentsWithoutInvoice();
    
    /**
     * Updates the invoiceId of an appointment during manual reconciliation.
     * 
     * <p><strong>Requisitos relacionados:</strong></p>
     * <ul>
     *   <li>REQ-12.2: Actualizar invoiceId manualmente</li>
     * </ul>
     * 
     * @param appointmentId ID of the appointment to update
     * @param invoiceId ID of the invoice to link
     * @return the updated appointment
     */
    Appointment updateInvoiceId(String appointmentId, String invoiceId);

    /** Appointments belonging to the authenticated patient (patientId from JWT). */
    List<Appointment> listMyAppointments();

    /** Appointments assigned to the authenticated doctor (doctorId == JWT userId). */
    List<Appointment> listDoctorAppointments();
}
