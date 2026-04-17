package com.medframe.clinical.infrastructure.client.dto;

import java.time.LocalDateTime;
import java.util.List;

public class LabOrderNotificationDTO {
    
    private String labOrderId;
    private String orderCode;
    private String patientId;
    private String doctorId;
    private List<String> testNames;
    private LocalDateTime orderedAt;
    
    public LabOrderNotificationDTO() {}
    
    public LabOrderNotificationDTO(String labOrderId, String orderCode, 
                                   String patientId, String doctorId, 
                                   List<String> testNames, LocalDateTime orderedAt) {
        this.labOrderId = labOrderId;
        this.orderCode = orderCode;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.testNames = testNames;
        this.orderedAt = orderedAt;
    }
    
    // Getters and setters
    public String getLabOrderId() { return labOrderId; }
    public void setLabOrderId(String labOrderId) { this.labOrderId = labOrderId; }
    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public List<String> getTestNames() { return testNames; }
    public void setTestNames(List<String> testNames) { this.testNames = testNames; }
    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
}
