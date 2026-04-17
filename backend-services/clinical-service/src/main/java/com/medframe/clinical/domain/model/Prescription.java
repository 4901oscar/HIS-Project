package com.medframe.clinical.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class Prescription {

    public enum PrescriptionStatus {
        PENDING, DISPENSED, CANCELLED
    }

    public static class Medication {
        private String name;
        private String dosage;
        private String frequency;
        private Integer durationDays;
        private String route;               // Oral, IV, IM, etc.
        private String specialInstructions;

        public Medication() {}

        public Medication(String name, String dosage, String frequency,
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
        public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    }

    private String id;
    private String consultationId;
    private String patientId;
    private String doctorId;
    private String prescriptionCode;    // 8-char alphanumeric, unique
    private List<Medication> medications;
    private PrescriptionStatus status;
    private LocalDateTime issuedAt;
    private String issuedBy;

    public Prescription() {
        this.status = PrescriptionStatus.PENDING;
        this.issuedAt = LocalDateTime.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getPrescriptionCode() { return prescriptionCode; }
    public void setPrescriptionCode(String prescriptionCode) { this.prescriptionCode = prescriptionCode; }
    public List<Medication> getMedications() { return medications; }
    public void setMedications(List<Medication> medications) { this.medications = medications; }
    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public String getIssuedBy() { return issuedBy; }
    public void setIssuedBy(String issuedBy) { this.issuedBy = issuedBy; }
}
