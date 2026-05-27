package com.medflow.billing.dto.request;

import com.medflow.billing.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for processing a payment on an invoice.
 * Contains the payment amount, method, and customer billing information.
 */
public class ProcessPaymentRequest {
    
    @NotNull(message = "El monto del pago es obligatorio")
    @Positive(message = "El monto del pago debe ser mayor a cero")
    private BigDecimal amount;
    
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod method;
    
    /**
     * Customer NIT for electronic invoice (required by SAT Guatemala)
     * Can be "CF" for Consumidor Final
     */
    @NotBlank(message = "El NIT es obligatorio")
    private String nit;
    
    /**
     * Customer name for electronic invoice
     */
    @NotBlank(message = "El nombre del cliente es obligatorio")
    private String customerName;
    
    // Constructors
    public ProcessPaymentRequest() {
    }
    
    public ProcessPaymentRequest(BigDecimal amount, PaymentMethod method, String nit, String customerName) {
        this.amount = amount;
        this.method = method;
        this.nit = nit;
        this.customerName = customerName;
    }
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getMethod() {
        return method;
    }
    
    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
    
    public String getNit() {
        return nit;
    }
    
    public void setNit(String nit) {
        this.nit = nit;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
