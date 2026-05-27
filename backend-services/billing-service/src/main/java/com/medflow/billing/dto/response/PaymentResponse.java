package com.medflow.billing.dto.response;

import com.medflow.billing.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for payment information.
 * Contains payment details including change amount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    
    private String id;
    private String invoiceId;
    private BigDecimal amount;
    private BigDecimal change;
    private PaymentMethod method;
    private LocalDateTime paidAt;
    private String receivedBy;
}
