package com.medframe.clinical.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "doctors", schema = "clinical_schema",
       indexes = {
           @Index(name = "idx_doctors_shift_status", columnList = "shift_start, shift_end, status"),
           @Index(name = "idx_doctors_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEntity {
    
    @Id
    @Column(name = "id", nullable = false)
    private String id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "shift_start", nullable = false)
    private LocalTime shiftStart;
    
    @Column(name = "shift_end", nullable = false)
    private LocalTime shiftEnd;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DoctorStatus status;
    
    @Column(name = "clinic_id", length = 36)
    private String clinicId;

    @Column(name = "created_at", nullable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false, length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;
    
    public enum DoctorStatus {
        ACTIVE,
        INACTIVE
    }

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
