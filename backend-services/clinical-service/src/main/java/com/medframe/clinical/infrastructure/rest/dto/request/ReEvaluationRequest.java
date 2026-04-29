package com.medframe.clinical.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for doctor re-evaluation after lab results.
 * Used when doctor reviews lab results and updates diagnosis/treatment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReEvaluationRequest {
    
    /**
     * Consultation ID being re-evaluated.
     */
    @NotBlank(message = "Consultation ID is required")
    private String consultationId;
    
    /**
     * Doctor's review of lab results.
     */
    private String labResultsReview;
    
    /**
     * Updated diagnosis based on lab results.
     */
    private String updatedDiagnosis;
    
    /**
     * Updated treatment plan based on lab results.
     */
    private String updatedTreatmentPlan;
    
    /**
     * Whether a prescription is issued after re-evaluation.
     * Determines next state: true → PENDING_PHARMACY_PAYMENT, false → COMPLETED
     */
    private Boolean hasPrescription;
}
