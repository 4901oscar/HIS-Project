package com.medframe.clinical.domain.model;

import java.time.LocalDateTime;

/**
 * VitalSigns domain entity - pure Java, no Spring/JPA annotations.
 * Contains business logic for BMI calculation and physiological range validation.
 */
public class VitalSigns {

    private String id;
    private String appointmentId;
    private String patientId;
    private Integer systolicPressure;   // 50-250 mmHg
    private Integer diastolicPressure;  // 30-150 mmHg
    private Integer heartRate;          // 20-250 bpm
    private Integer respiratoryRate;
    private Double temperature;         // 30-45 °C
    private Integer oxygenSaturation;   // 0-100 %
    private Double weight;              // kg
    private Double height;              // cm
    private Double bmi;                 // calculated
    private LocalDateTime recordedAt;
    private String recordedBy;

    public VitalSigns() {}

    /** Business method: calculates BMI from weight (kg) and height (cm). */
    public void calculateBMI() {
        if (weight != null && height != null && height > 0) {
            double heightInMeters = height / 100.0;
            this.bmi = weight / (heightInMeters * heightInMeters);
            // Round to 2 decimal places
            this.bmi = Math.round(this.bmi * 100.0) / 100.0;
        }
    }

    /**
     * Business method: validates that all vital sign values are within physiological ranges.
     * Returns true only if ALL values are within their valid ranges.
     */
    public boolean isValid() {
        if (systolicPressure == null || diastolicPressure == null ||
            heartRate == null || temperature == null || oxygenSaturation == null) {
            return false;
        }
        return systolicPressure >= 50 && systolicPressure <= 250
            && diastolicPressure >= 30 && diastolicPressure <= 150
            && heartRate >= 20 && heartRate <= 250
            && temperature >= 30.0 && temperature <= 45.0
            && oxygenSaturation >= 0 && oxygenSaturation <= 100;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public Integer getSystolicPressure() { return systolicPressure; }
    public void setSystolicPressure(Integer systolicPressure) { this.systolicPressure = systolicPressure; }

    public Integer getDiastolicPressure() { return diastolicPressure; }
    public void setDiastolicPressure(Integer diastolicPressure) { this.diastolicPressure = diastolicPressure; }

    public Integer getHeartRate() { return heartRate; }
    public void setHeartRate(Integer heartRate) { this.heartRate = heartRate; }

    public Integer getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(Integer respiratoryRate) { this.respiratoryRate = respiratoryRate; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Integer getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(Integer oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }
}
