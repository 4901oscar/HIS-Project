package com.medflow.billing.dto.request;

import com.medflow.billing.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for processing a payment on an invoice.
 * Contains the payment amount and method.
 */
public class ProcessPaymentRequest {
    
    @NotNull(message = "El monto del pago es obligatorio")
    @Positive(message = "El monto del pago debe ser mayor a cero")
    private BigDecimal amount;
    
    @NotNull(message = "El método de pago es obligatorio")
    private PaymentMethod method;
    
    // Constructors
    public ProcessPaymentRequest() {
    }
    
    public ProcessPaymentRequest(BigDecimal amount, PaymentMethod method) {
        this.amount = amount;
        this.method = method;
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
}
