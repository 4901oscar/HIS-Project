package com.medflow.billing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a payment in the billing system.
 * A payment is associated with an invoice and records the payment details.
 * 
 * Mapped to: billing_schema.payments
 */
@Entity
@Table(name = "payments", schema = "billing_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * ID of the invoice this payment is for
     */
    @Column(name = "invoice_id", nullable = false, length = 36)
    private String invoiceId;
    
    /**
     * Amount paid by the customer
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    /**
     * Change to return to customer (amount - invoice.total)
     */
    @Column(name = "change_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal change;
    
    /**
     * Payment method used (CASH, CARD, TRANSFER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;
    
    /**
     * Timestamp when the payment was made
     */
    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;
    
    /**
     * ID of the user who received the payment
     */
    @Column(name = "received_by", nullable = false, length = 36)
    private String receivedBy;
}
