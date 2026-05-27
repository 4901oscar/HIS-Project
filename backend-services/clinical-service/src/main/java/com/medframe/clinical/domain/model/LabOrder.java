package com.medframe.clinical.domain.model;

import java.time.LocalDateTime;
import java.util.List;

public class LabOrder {

    public enum LabOrderStatus {
        PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    private String id;
    private String consultationId;
    private String patientId;
    private String doctorId;
    private String orderCode;           // 8-char alphanumeric, unique
    private List<String> testNames;
    private LabOrderStatus status;
    private LocalDateTime orderedAt;
    private String orderedBy;

    public LabOrder() {
        this.status = LabOrderStatus.PENDING;
        this.orderedAt = LocalDateTime.now();
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
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public List<String> getTestNames() { return testNames; }
    public void setTestNames(List<String> testNames) { this.testNames = testNames; }
    public LabOrderStatus getStatus() { return status; }
    public void setStatus(LabOrderStatus status) { this.status = status; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
    public String getOrderedBy() { return orderedBy; }
    public void setOrderedBy(String orderedBy) { this.orderedBy = orderedBy; }
}
