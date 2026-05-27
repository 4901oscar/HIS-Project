package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for confirming payment and transitioning appointment state.
 * Used for consultation, lab, and pharmacy payment confirmations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPaymentRequest {
    
    /**
     * Invoice ID to validate and link to the appointment.
     */
    @NotBlank(message = "Invoice ID is required")
    private String invoiceId;
}
