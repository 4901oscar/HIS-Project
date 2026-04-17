package com.medframe.clinical.infrastructure.persistence.entity;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.infrastructure.persistence.converter.StringListConverter;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lab_orders", schema = "clinical_schema",
       indexes = {
           @Index(name = "idx_lab_orders_code", columnList = "order_code", unique = true),
           @Index(name = "idx_lab_orders_patient", columnList = "patient_id")
       })
public class LabOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "consultation_id", nullable = false, length = 36)
    private String consultationId;

    @Column(name = "patient_id", nullable = false, length = 36)
    private String patientId;

    @Column(name = "doctor_id", nullable = false, length = 36)
    private String doctorId;

    @Column(name = "order_code", nullable = false, unique = true, length = 8)
    private String orderCode;

    @Column(name = "test_names", nullable = false, columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> testNames;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LabOrder.LabOrderStatus status;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "ordered_by", nullable = false, length = 36)
    private String orderedBy;

    // Constructors
    public LabOrderEntity() {}

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

    public LabOrder.LabOrderStatus getStatus() { return status; }
    public void setStatus(LabOrder.LabOrderStatus status) { this.status = status; }

    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }

    public String getOrderedBy() { return orderedBy; }
    public void setOrderedBy(String orderedBy) { this.orderedBy = orderedBy; }
}
