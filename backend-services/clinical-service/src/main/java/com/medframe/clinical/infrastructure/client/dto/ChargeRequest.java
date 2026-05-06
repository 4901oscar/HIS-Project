package com.medframe.clinical.infrastructure.client.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO representing a single charge within an invoice.
 * Used when creating invoices in Billing Service from Clinical Service.
 * 
 * Feature: appointment-billing-integration
 * Requirements: 8.2, 8.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeRequest {
    
    /**
     * Type of charge: CONSULTATION, LABORATORY, MEDICATION, or OTHER
     */
    @NotBlank(message = "El tipo de cargo es obligatorio")
    @Pattern(regexp = "CONSULTATION|LABORATORY|MEDICATION|OTHER", 
             message = "El tipo debe ser uno de: CONSULTATION, LABORATORY, MEDICATION, OTHER")
    private String type;
    
    /**
     * Description of the charge (e.g., "Consulta médica - Dr. García - 2026-04-25")
     */
    @NotBlank(message = "La descripción es obligatoria")
    private String description;
    
    /**
     * Quantity of the service/item (typically 1 for consultations)
     */
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer quantity;
    
    /**
     * Unit price of the service/item in GTQ
     */
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    private BigDecimal unitPrice;
}
