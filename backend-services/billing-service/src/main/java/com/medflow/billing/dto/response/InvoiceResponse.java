package com.medflow.billing.dto.response;

import com.medflow.billing.model.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for invoice information.
 * Contains complete invoice details including all charges.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    
    private String id;
    private String invoiceNumber;
    private String patientId;
    private List<ChargeResponse> charges;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private InvoiceStatus status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
}
