package com.medflow.billing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing a charge item in an invoice.
 * Each charge represents a billable service or item (consultation, lab test, medication, etc.)
 * 
 * Mapped to: billing_schema.charges
 */
@Entity
@Table(name = "charges", schema = "billing_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Charge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Reference to the parent invoice
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    
    /**
     * Type of charge (CONSULTATION, LABORATORY, MEDICATION, OTHER)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargeType type;
    
    /**
     * Description of the charge
     */
    @Column(nullable = false, length = 300)
    private String description;
    
    /**
     * Quantity of items/services
     */
    @Column(nullable = false)
    private Integer quantity = 1;
    
    /**
     * Price per unit
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    /**
     * Subtotal (quantity * unitPrice)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}
