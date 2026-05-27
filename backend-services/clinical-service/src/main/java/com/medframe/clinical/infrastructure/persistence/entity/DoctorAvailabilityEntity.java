package com.medframe.clinical.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_availability", schema = "clinical_schema",
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_doctor_availability_doctor_date", columnNames = {"doctor_id", "date"})
       },
       indexes = {
           @Index(name = "idx_doctor_availability_doctor_date", columnList = "doctor_id, date"),
           @Index(name = "idx_doctor_availability_date", columnList = "date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailabilityEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    
    @Column(name = "doctor_id", nullable = false)
    private String doctorId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable;
    
    @Column(name = "reason", length = 500)
    private String reason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
        if (createdBy == null) {
            createdBy = "internal";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }}
