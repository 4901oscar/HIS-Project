package com.medframe.clinical.infrastructure.client.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating an invoice in Billing Service.
 * Used by Clinical Service to automatically create invoices when appointments are created.
 * 
 * Feature: appointment-billing-integration
 * Requirements: 8.1, 8.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {
    
    /**
     * ID of the patient for whom the invoice is being created
     */
    @NotBlank(message = "El ID del paciente es obligatorio")
    private String patientId;
    
    /**
     * ID of the appointment that generated this invoice
     * Used to establish bidirectional reference between appointment and invoice
     */
    @NotBlank(message = "El ID de la cita es obligatorio")
    private String appointmentId;
    
    /**
     * List of charges to be included in the invoice
     * Must contain at least one charge
     */
    @NotEmpty(message = "La factura debe contener al menos un cargo")
    @Valid
    private List<ChargeRequest> charges;
}
