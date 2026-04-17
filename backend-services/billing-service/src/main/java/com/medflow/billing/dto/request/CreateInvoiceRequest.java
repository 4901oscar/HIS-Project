package com.medflow.billing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for creating a new invoice with charges.
 * Contains patient information and a list of charges to be billed.
 */
public class CreateInvoiceRequest {
    
    @NotBlank(message = "El ID del paciente es obligatorio")
    private String patientId;
    
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
    
    // Getters and Setters
    public String getPatientId() {
        return patientId;
    }
    
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    
    public List<ChargeRequest> getCharges() {
        return charges;
    }
    
    public void setCharges(List<ChargeRequest> charges) {
        this.charges = charges;
    }
}
