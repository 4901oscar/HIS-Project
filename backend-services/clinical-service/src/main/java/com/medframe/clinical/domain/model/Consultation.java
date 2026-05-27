package com.medframe.clinical.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Consultation {

    private String id;
    private String patientId;
    private String doctorId;
    private String appointmentId;
    private String chiefComplaint;
    private String symptoms;
    private String primaryDiagnosis;        // CIE-10 code
    private List<String> secondaryDiagnoses;
    private String medicalNotes;
    private String treatmentPlan;
    private LocalDateTime consultationDate;
    private String performedBy;

    public Consultation() {
        this.secondaryDiagnoses = new ArrayList<>();
        this.consultationDate = LocalDateTime.now();
    }

    public void addSecondaryDiagnosis(String cie10Code) {
        if (this.secondaryDiagnoses == null) {
            this.secondaryDiagnoses = new ArrayList<>();
        }
        this.secondaryDiagnoses.add(cie10Code);
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getPrimaryDiagnosis() { return primaryDiagnosis; }
    public void setPrimaryDiagnosis(String primaryDiagnosis) { this.primaryDiagnosis = primaryDiagnosis; }

    public List<String> getSecondaryDiagnoses() { return secondaryDiagnoses; }
    public void setSecondaryDiagnoses(List<String> secondaryDiagnoses) { this.secondaryDiagnoses = secondaryDiagnoses; }

    public String getMedicalNotes() { return medicalNotes; }
    public void setMedicalNotes(String medicalNotes) { this.medicalNotes = medicalNotes; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public LocalDateTime getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDateTime consultationDate) { this.consultationDate = consultationDate; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
}
