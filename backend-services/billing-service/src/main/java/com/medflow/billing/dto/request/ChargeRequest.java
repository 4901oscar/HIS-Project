package com.medflow.billing.dto.request;

import com.medflow.billing.model.ChargeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request DTO for creating a charge within an invoice.
 * Represents a single billable item or service.
 */
public class ChargeRequest {
    
    @NotNull(message = "El tipo de cargo es obligatorio")
    private ChargeType type;
    
    @NotBlank(message = "La descripción es obligatoria")
    private String description;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer quantity;
    
    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    private BigDecimal unitPrice;
    
    // Constructors
    public ChargeRequest() {
    }
    
    public ChargeRequest(ChargeType type, String description, Integer quantity, BigDecimal unitPrice) {
        this.type = type;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }
    
    // Getters and Setters
    public ChargeType getType() {
        return type;
    }
    
    public void setType(ChargeType type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }
    
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}
