package com.medflow.billing.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for applying a discount to an invoice.
 * Supports either a fixed discount amount or a percentage discount.
 * Only one of the two fields should be provided.
 */
public class ApplyDiscountRequest {
    
    @Positive(message = "El monto del descuento debe ser mayor a cero")
    private BigDecimal discountAmount;
    
    @DecimalMin(value = "0.01", message = "El porcentaje de descuento debe ser mayor a 0")
    @DecimalMax(value = "100.0", message = "El porcentaje de descuento no puede exceder 100")
    private BigDecimal discountPercentage;
    
    // Constructors
    public ApplyDiscountRequest() {
    }
    
    public ApplyDiscountRequest(BigDecimal discountAmount, BigDecimal discountPercentage) {
        this.discountAmount = discountAmount;
        this.discountPercentage = discountPercentage;
    }
    
    // Getters and Setters
    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }
    
    public BigDecimal getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(BigDecimal discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
}
