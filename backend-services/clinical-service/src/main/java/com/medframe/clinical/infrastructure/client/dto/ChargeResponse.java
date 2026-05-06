package com.medframe.clinical.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO representing a charge within an invoice.
 * Returned by Billing Service as part of InvoiceResponse.
 * 
 * Feature: appointment-billing-integration
 * Requirements: 8.4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeResponse {
    
    /**
     * Unique ID of the charge (UUID)
     */
    private String id;
    
    /**
     * Type of charge: CONSULTATION, LABORATORY, MEDICATION, or OTHER
     */
    private String type;
    
    /**
     * Description of the charge
     */
    private String description;
    
    /**
     * Quantity of the service/item
     */
    private Integer quantity;
    
    /**
     * Unit price of the service/item in GTQ
     */
    private BigDecimal unitPrice;
    
    /**
     * Subtotal for this charge (quantity * unitPrice)
     */
    private BigDecimal subtotal;
}
