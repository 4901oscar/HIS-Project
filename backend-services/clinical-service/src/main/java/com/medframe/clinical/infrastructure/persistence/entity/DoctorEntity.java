package com.medframe.clinical.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * JPA entity for doctors table.
 * 
 * <p>This entity represents the persistence layer mapping for the Doctor domain model.
 * It follows hexagonal architecture by residing in the infrastructure layer.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Doctor status enum matching the domain model.
     */
    public enum DoctorStatus {
        ACTIVE,
        INACTIVE
    }
}
