package com.medflow.billing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for creating a new invoice with charges.
 * Contains patient information and a list of charges to be billed.
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-7.1: Recibir appointmentId en request de creación de factura</li>
 *   <li>REQ-8.1: appointmentId es opcional (nullable) para mantener compatibilidad</li>
 * </ul>
 */
public class CreateInvoiceRequest {
    
    @NotBlank(message = "El ID del paciente es obligatorio")
    private String patientId;
    
    /**
     * ID de la cita médica asociada a esta factura (opcional).
     * 
     * <p>Este campo establece la referencia lógica entre Invoice y Appointment.
     * Es nullable para mantener compatibilidad con facturas creadas manualmente
     * desde el módulo de caja que no están asociadas a una cita.</p>
     * 
     * <p><strong>Nota:</strong> No es una foreign key, solo una referencia lógica
     * siguiendo el principio de CERO JOINs entre esquemas de microservicios.</p>
     */
    private String appointmentId;
    
    @NotEmpty(message = "La factura debe contener al menos un cargo")
    @Valid
    private List<ChargeRequest> charges;
    
    // Constructors
    public CreateInvoiceRequest() {
    }
    
    public CreateInvoiceRequest(String patientId, List<ChargeRequest> charges) {
        this.patientId = patientId;
        this.charges = charges;
    }
    
    public CreateInvoiceRequest(String patientId, String appointmentId, List<ChargeRequest> charges) {
        this.patientId = patientId;
        this.appointmentId = appointmentId;
        this.charges = charges;
    }
    
    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public String getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public List<ChargeRequest> getCharges() {
        return charges;
    }
    
    public void setCharges(List<ChargeRequest> charges) {
        this.charges = charges;
    }
}
