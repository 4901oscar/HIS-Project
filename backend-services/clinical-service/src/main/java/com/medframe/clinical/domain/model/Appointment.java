package com.medframe.clinical.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Appointment domain entity - pure Java, no Spring/JPA annotations.
 * Contains state transition business logic.
 */
public class Appointment {

    /**
     * Appointment lifecycle states supporting complete hospital workflow.
     */
    public enum AppointmentStatus {
        // Initial states
        PENDING_PAYMENT,    // Created without payment
        SCHEDULED,          // Payment confirmed, waiting for arrival
        
        // Arrival and triage
        ACTIVE,             // Patient arrived, waiting for triage
        VITAL_SIGNS,        // Triage in progress (locked state)
        
        // Consultation
        CONSULTATION,       // Waiting for doctor consultation
        
        // Laboratory workflow
        PENDING_LAB_PAYMENT,  // Lab ordered, waiting for payment
        LABORATORY,           // Lab payment confirmed, tests in progress
        RE_EVALUATION,        // Lab results ready, waiting for doctor review
        
        // Laboratory sample workflow (4-step wizard)
        LAB_SAMPLE_COLLECTION,  // Step 1: Waiting for sample collection
        LAB_SAMPLE_PENDING,     // Step 2: Samples collected, awaiting validation
        LAB_PROCESSING,         // Step 3: Samples accepted, tests in progress
        LAB_RESULTS_READY,      // Step 4: Results uploaded, ready for doctor review
        
        // Pharmacy workflow
        PENDING_PHARMACY_PAYMENT,  // Prescription issued, waiting for payment
        PHARMACY,                  // Pharmacy payment confirmed, dispensing
        
        // Terminal states
        COMPLETED,          // All services completed
        CANCELLED,          // Cancelled at any stage
        MISSED              // Patient did not arrive
    }

    private String id;
    private String patientId;
    private String doctorId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private String createdBy;
    
    // Invoice tracking fields
    private String invoiceId;              // Consultation invoice
    private String labInvoiceId;           // Lab tests invoice
    private String pharmacyInvoiceId;      // Medications invoice
    
    // Transient field - not persisted in database
    // Populated after QR generation for API response
    private String qrCodeBase64;

    public Appointment() {
        this.createdAt = LocalDateTime.now();
        // Initial state determined by payment status (set by factory method or explicitly)
    }

    // ═══════════════════════════════════════════════════════════════
    // State Transition Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Transition: PENDING_PAYMENT → SCHEDULED
     * Triggered when consultation payment is confirmed.
     * 
     * @throws IllegalStateException if not in PENDING_PAYMENT state
     */
    public void confirmPayment() {
        if (this.status != AppointmentStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                "Solo se puede confirmar pago para citas en estado PENDING_PAYMENT. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.SCHEDULED;
    }

    /**
     * Transition: SCHEDULED → VITAL_SIGNS
     * Triggered when patient arrives (QR scan or manual activation).
     * Goes directly to vital signs capture (no intermediate ACTIVE state).
     * 
     * @throws IllegalStateException if not in SCHEDULED state
     */
    public void activate() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Solo se pueden activar citas en estado SCHEDULED. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.VITAL_SIGNS;
    }

    /**
     * Transition: VITAL_SIGNS → CONSULTATION
     * Triggered when vital signs are saved successfully.
     * 
     * @throws IllegalStateException if not in VITAL_SIGNS state
     */
    public void completeVitalSigns() {
        if (this.status != AppointmentStatus.VITAL_SIGNS) {
            throw new IllegalStateException(
                "Solo se pueden completar signos vitales para citas en estado VITAL_SIGNS. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.CONSULTATION;
    }

    /**
     * Transition: CONSULTATION → (COMPLETED | PENDING_PHARMACY_PAYMENT | PENDING_LAB_PAYMENT)
     * Triggered when doctor registers consultation.
     * Next state depends on consultation content:
     * - No orders → COMPLETED
     * - Prescription only → PENDING_PHARMACY_PAYMENT
     * - Lab orders → PENDING_LAB_PAYMENT (takes priority)
     * 
     * @param hasLabOrders true if consultation includes lab orders
     * @param hasPrescription true if consultation includes prescription
     * @throws IllegalStateException if not in CONSULTATION state
     */
    public void registerConsultation(boolean hasLabOrders, boolean hasPrescription) {
        if (this.status != AppointmentStatus.CONSULTATION) {
            throw new IllegalStateException(
                "Solo se puede registrar consulta para citas en estado CONSULTATION. " +
                "Estado actual: " + this.status);
        }
        
        // Determine next state based on consultation content
        if (hasLabOrders) {
            // Lab orders take priority over prescription
            this.status = AppointmentStatus.PENDING_LAB_PAYMENT;
        } else if (hasPrescription) {
            this.status = AppointmentStatus.PENDING_PHARMACY_PAYMENT;
        } else {
            // No orders, complete the appointment
            this.status = AppointmentStatus.COMPLETED;
        }
    }

    /**
     * Transition: PENDING_LAB_PAYMENT → LAB_SAMPLE_COLLECTION
     * Triggered when lab payment is confirmed.
     * 
     * @throws IllegalStateException if not in PENDING_LAB_PAYMENT state
     */
    public void confirmLabPayment() {
        if (this.status != AppointmentStatus.PENDING_LAB_PAYMENT) {
            throw new IllegalStateException(
                "Solo se puede confirmar pago de laboratorio para citas en estado PENDING_LAB_PAYMENT. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.LAB_SAMPLE_COLLECTION;
    }

    /**
     * Transition: LABORATORY → RE_EVALUATION
     * Triggered when lab tests are completed and results are ready.
     * 
     * @throws IllegalStateException if not in LABORATORY state
     */
    public void completeLab() {
        if (this.status != AppointmentStatus.LABORATORY) {
            throw new IllegalStateException(
                "Solo se pueden completar laboratorios para citas en estado LABORATORY. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.RE_EVALUATION;
    }

    /**
     * Transition: RE_EVALUATION → (COMPLETED | PENDING_PHARMACY_PAYMENT)
     * Triggered when doctor completes re-evaluation after lab results.
     * Next state depends on whether prescription is issued.
     * 
     * @param hasPrescription true if re-evaluation includes prescription
     * @throws IllegalStateException if not in RE_EVALUATION state
     */
    public void completeReEvaluation(boolean hasPrescription) {
        if (this.status != AppointmentStatus.RE_EVALUATION) {
            throw new IllegalStateException(
                "Solo se puede completar re-evaluación para citas en estado RE_EVALUATION. " +
                "Estado actual: " + this.status);
        }
        
        if (hasPrescription) {
            this.status = AppointmentStatus.PENDING_PHARMACY_PAYMENT;
        } else {
            this.status = AppointmentStatus.COMPLETED;
        }
    }

    /**
     * Transition: PENDING_PHARMACY_PAYMENT → PHARMACY
     * Triggered when pharmacy payment is confirmed.
     * 
     * @throws IllegalStateException if not in PENDING_PHARMACY_PAYMENT state
     */
    public void confirmPharmacyPayment() {
        if (this.status != AppointmentStatus.PENDING_PHARMACY_PAYMENT) {
            throw new IllegalStateException(
                "Solo se puede confirmar pago de farmacia para citas en estado PENDING_PHARMACY_PAYMENT. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.PHARMACY;
    }

    /**
     * Transition: PHARMACY → COMPLETED
     * Triggered when medications are dispensed.
     * 
     * @throws IllegalStateException if not in PHARMACY state
     */
    public void dispenseMedication() {
        if (this.status != AppointmentStatus.PHARMACY) {
            throw new IllegalStateException(
                "Solo se pueden dispensar medicamentos para citas en estado PHARMACY. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    /**
     * Transition: ACTIVE → COMPLETED
     * Legacy method for backward compatibility.
     * New workflow should use registerConsultation() instead.
     * 
     * @deprecated Use registerConsultation() for new workflow
     * @throws IllegalStateException if not in ACTIVE state
     */
    @Deprecated
    public void complete() {
        if (this.status != AppointmentStatus.ACTIVE) {
            throw new IllegalStateException(
                "Solo se pueden completar citas en estado ACTIVE. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.COMPLETED;
    }

    /**
     * Transition: (any except COMPLETED) → CANCELLED
     * Can be triggered from any state except COMPLETED.
     * 
     * @throws IllegalStateException if already COMPLETED
     */
    public void cancel() {
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException(
                "No se pueden cancelar citas ya completadas.");
        }
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException(
                "La cita ya está cancelada.");
        }
        this.status = AppointmentStatus.CANCELLED;
    }

    /**
     * Transition: SCHEDULED → MISSED
     * Triggered automatically when patient doesn't arrive within time window.
     * 
     * @throws IllegalStateException if not in SCHEDULED state
     */
    public void markAsMissed() {
        if (this.status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException(
                "Solo se pueden marcar como perdidas las citas en estado SCHEDULED. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.MISSED;
    }

    /**
     * Transition: LAB_SAMPLE_COLLECTION → LAB_SAMPLE_PENDING
     * Triggered when lab technician collects samples from patient.
     * 
     * @throws IllegalStateException if not in LAB_SAMPLE_COLLECTION state
     */
    public void collectLabSamples() {
        if (this.status != AppointmentStatus.LAB_SAMPLE_COLLECTION) {
            throw new IllegalStateException(
                "Solo se pueden recolectar muestras para citas en estado LAB_SAMPLE_COLLECTION. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.LAB_SAMPLE_PENDING;
    }

    /**
     * Transition: LAB_SAMPLE_PENDING → LAB_PROCESSING
     * Triggered when lab technician accepts collected samples as valid.
     * 
     * @throws IllegalStateException if not in LAB_SAMPLE_PENDING state
     */
    public void acceptLabSamples() {
        if (this.status != AppointmentStatus.LAB_SAMPLE_PENDING) {
            throw new IllegalStateException(
                "Solo se pueden aceptar muestras para citas en estado LAB_SAMPLE_PENDING. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.LAB_PROCESSING;
    }

    /**
     * Transition: LAB_SAMPLE_PENDING → LAB_SAMPLE_COLLECTION
     * Triggered when lab technician rejects samples and requests new collection.
     * 
     * @throws IllegalStateException if not in LAB_SAMPLE_PENDING state
     */
    public void rejectLabSamples() {
        if (this.status != AppointmentStatus.LAB_SAMPLE_PENDING) {
            throw new IllegalStateException(
                "Solo se pueden rechazar muestras para citas en estado LAB_SAMPLE_PENDING. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.LAB_SAMPLE_COLLECTION;
    }

    /**
     * Transition: LAB_PROCESSING → LAB_RESULTS_READY
     * Triggered when all lab tests are completed and results are uploaded.
     * 
     * @throws IllegalStateException if not in LAB_PROCESSING state
     */
    public void completeLabProcessing() {
        if (this.status != AppointmentStatus.LAB_PROCESSING) {
            throw new IllegalStateException(
                "Solo se puede completar procesamiento para citas en estado LAB_PROCESSING. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.LAB_RESULTS_READY;
    }

    /**
     * Transition: LAB_RESULTS_READY → CONSULTATION
     * Triggered when lab technician sends results to doctor for review.
     * 
     * @throws IllegalStateException if not in LAB_RESULTS_READY state
     */
    public void sendLabResultsToDoctor() {
        if (this.status != AppointmentStatus.LAB_RESULTS_READY) {
            throw new IllegalStateException(
                "Solo se pueden enviar resultados para citas en estado LAB_RESULTS_READY. " +
                "Estado actual: " + this.status);
        }
        this.status = AppointmentStatus.CONSULTATION;
    }

    // ═══════════════════════════════════════════════════════════════
    // Query Methods
    // ═══════════════════════════════════════════════════════════════

    /**
     * Checks if appointment can be cancelled.
     * 
     * @return true if appointment is not COMPLETED
     */
    public boolean canBeCancelled() {
        return this.status != AppointmentStatus.COMPLETED;
    }

    /**
     * Checks if appointment is in a payment-pending state.
     * 
     * @return true if waiting for any payment
     */
    public boolean isPendingPayment() {
        return this.status == AppointmentStatus.PENDING_PAYMENT ||
               this.status == AppointmentStatus.PENDING_LAB_PAYMENT ||
               this.status == AppointmentStatus.PENDING_PHARMACY_PAYMENT;
    }

    /**
     * Checks if appointment is in a terminal state.
     * 
     * @return true if COMPLETED, CANCELLED, or MISSED
     */
    public boolean isTerminal() {
        return this.status == AppointmentStatus.COMPLETED ||
               this.status == AppointmentStatus.CANCELLED ||
               this.status == AppointmentStatus.MISSED;
    }

    /**
     * Checks if appointment requires priority handling (re-evaluation).
     * 
     * @return true if in RE_EVALUATION state
     */
    public boolean isPriority() {
        return this.status == AppointmentStatus.RE_EVALUATION;
    }

    // ═══════════════════════════════════════════════════════════════
    // Getters and Setters
    // ═══════════════════════════════════════════════════════════════
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getLabInvoiceId() { return labInvoiceId; }
    public void setLabInvoiceId(String labInvoiceId) { 
        this.labInvoiceId = labInvoiceId; 
    }

    public String getPharmacyInvoiceId() { return pharmacyInvoiceId; }
    public void setPharmacyInvoiceId(String pharmacyInvoiceId) { 
        this.pharmacyInvoiceId = pharmacyInvoiceId; 
    }

    public String getQrCodeBase64() { return qrCodeBase64; }
    public void setQrCodeBase64(String qrCodeBase64) { this.qrCodeBase64 = qrCodeBase64; }
}
