package com.medframe.clinical.infrastructure.rest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for detailed prescription information.
 * Used by pharmacy module to display prescription details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDetailResponse {
    
    private String id;
    private String prescriptionCode;
    private String status;
    private LocalDateTime issuedAt;
    private PatientInfo patient;
    private DoctorInfo doctor;
    private List<MedicationItemResponse> medications;
    
    /**
     * Patient information nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientInfo {
        private String id;
        private String fullName;
        private String dpi;
        private String phone;
        private String email;
    }
    
    /**
     * Doctor information nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorInfo {
        private String id;
        private String name;
        private String specialty;
    }
    
    /**
     * Medication item nested DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationItemResponse {
        private String name;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String route;
        private String specialInstructions;
        
        // Optional calculated fields for internal pharmacy
        private Double dosageAmount;
        private String dosageUnit;
        private Integer frequencyHours;
        private Double totalQuantity;
    }
}
