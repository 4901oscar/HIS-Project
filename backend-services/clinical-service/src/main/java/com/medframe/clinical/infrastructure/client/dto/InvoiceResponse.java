package com.medframe.clinical.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing an invoice returned by Billing Service.
 * Used by Clinical Service to receive invoice details after creation.
 * 
 * Feature: appointment-billing-integration
 * Requirements: 8.3, 8.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    
    /**
     * Unique ID of the invoice (UUID)
     */
    private String id;
    
    /**
     * Human-readable invoice number (format: INV-YYYYMMDD-XXXX)
     */
    private String invoiceNumber;
    
    /**
     * ID of the patient this invoice belongs to
     */
    private String patientId;
    
    /**
     * ID of the appointment that generated this invoice
     */
    private String appointmentId;
    
    /**
     * List of charges included in this invoice
     */
    private List<ChargeResponse> charges;
    
    /**
     * Sum of all charge subtotals before discount
     */
    private BigDecimal subtotal;
    
    /**
     * Discount amount applied to the invoice
     */
    private BigDecimal discountAmount;
    
    /**
     * Final total amount (subtotal - discountAmount)
     */
    private BigDecimal total;
    
    /**
     * Current status of the invoice: PENDING, PAID, or CANCELLED
     */
    private String status;
    
    /**
     * Timestamp when the invoice was created
     */
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the invoice was last updated
     */
    private LocalDateTime updatedAt;
    
    /**
     * ID of the user who created the invoice
     */
    private String createdBy;
    
    /**
     * Customer NIT (optional)
     */
    private String customerNit;
    
    /**
     * Customer name (optional)
     */
    private String customerName;
}
