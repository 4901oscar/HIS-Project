package com.medflow.billing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an invoice in the billing system.
 * An invoice contains multiple charges and tracks the billing status for a patient.
 * 
 * Invoice number format: INV-YYYYMMDD-XXXX (auto-generated)
 * Mapped to: billing_schema.invoices
 */
@Entity
@Table(name = "invoices", schema = "billing_schema")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    /**
     * Unique invoice number in format INV-YYYYMMDD-XXXX
     */
    @Column(name = "invoice_number", unique = true, nullable = false, length = 20)
    private String invoiceNumber;
    
    /**
     * ID of the patient this invoice belongs to
     */
    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;
    
    /**
     * ID of the appointment that generated this invoice (nullable)
     * Logical foreign key to clinical_schema.appointments.id (not enforced)
     * NULL when invoice was created manually without an appointment
     */
    @Column(name = "appointment_id", length = 36)
    private String appointmentId;
    
    /**
     * List of charges associated with this invoice
     * Cascade ALL: when invoice is saved/deleted, charges are also saved/deleted
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Charge> charges = new ArrayList<>();
    
    /**
     * Sum of all charge subtotals before discount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;
    
    /**
     * Discount amount applied to the invoice
     */
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    /**
     * Final total (subtotal - discountAmount)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    
    /**
     * Current status of the invoice (PENDING, PAID, CANCELLED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status = InvoiceStatus.PENDING;
    
    /**
     * Timestamp when the invoice was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * ID of the user who created the invoice
     */
    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;
    
    /**
     * Customer NIT for electronic invoice
     */
    @Column(name = "customer_nit", length = 20)
    private String customerNit;
    
    /**
     * Customer name for electronic invoice
     */
    @Column(name = "customer_name", length = 200)
    private String customerName;
    
    /**
     * Timestamp when the invoice was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Helper method to add a charge to the invoice and set the bidirectional relationship
     */
    public void addCharge(Charge charge) {
        charges.add(charge);
        charge.setInvoice(this);
    }
    
    /**
     * Helper method to remove a charge from the invoice
     */
    public void removeCharge(Charge charge) {
        charges.remove(charge);
        charge.setInvoice(null);
    }
}
