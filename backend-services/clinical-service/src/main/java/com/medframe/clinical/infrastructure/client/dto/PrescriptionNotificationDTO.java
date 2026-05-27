package com.medframe.clinical.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionNotificationDTO {
    
    private String prescriptionId;
    private String prescriptionCode;
    private String patientId;
    private String doctorId;
    private List<MedicationDTO> medications;
    private LocalDateTime issuedAt;
    
    public PrescriptionNotificationDTO() {}
    
    public PrescriptionNotificationDTO(String prescriptionId, String prescriptionCode, 
                                       String patientId, String doctorId, 
                                       List<MedicationDTO> medications, LocalDateTime issuedAt) {
        this.prescriptionId = prescriptionId;
        this.prescriptionCode = prescriptionCode;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.medications = medications;
        this.issuedAt = issuedAt;
    }
    
    public static class MedicationDTO {
        private String name;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String route;
        private String specialInstructions;
        
        public MedicationDTO() {}
        
        public MedicationDTO(String name, String dosage, String frequency,
                            Integer durationDays, String route, String specialInstructions) {
            this.name = name;
            this.dosage = dosage;
            this.frequency = frequency;
            this.durationDays = durationDays;
            this.route = route;
            this.specialInstructions = specialInstructions;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public Integer getDurationDays() { return durationDays; }
        public void setDurationDays(Integer durationDays) { this.durationDays = durationDays; }
        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }
        public String getSpecialInstructions() { return specialInstructions; }
        public void setSpecialInstructions(String specialInstructions) { 
            this.specialInstructions = specialInstructions; 
        }
    }
    
    // Getters and setters
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }
    public String getPrescriptionCode() { return prescriptionCode; }
    public void setPrescriptionCode(String prescriptionCode) { 
        this.prescriptionCode = prescriptionCode; 
    }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public List<MedicationDTO> getMedications() { return medications; }
    public void setMedications(List<MedicationDTO> medications) { this.medications = medications; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
