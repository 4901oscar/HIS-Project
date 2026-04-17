package com.medflow.billing.dto.response;

import com.medflow.billing.model.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for charge information.
 * Represents a single charge item within an invoice.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargeResponse {
    
    private String id;
    private ChargeType type;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
