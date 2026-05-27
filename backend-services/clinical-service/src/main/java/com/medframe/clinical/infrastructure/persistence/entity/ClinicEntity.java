package com.medframe.clinical.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JPA entity for clinics table.
 * 
 * <p>This entity represents the persistence layer mapping for the Clinic domain model.
 * It follows hexagonal architecture by residing in the infrastructure layer.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Entity
@Table(name = "clinics", schema = "clinical_schema",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_clinics_codigo", columnNames = "codigo")
       },
       indexes = {
           @Index(name = "idx_clinics_codigo", columnList = "codigo"),
           @Index(name = "idx_clinics_estado", columnList = "estado")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicEntity {
    
    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;
    
    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;
    
    @Column(name = "nombre", nullable = false)
    private String nombre;
    
    @Column(name = "descripcion", nullable = false)
    private String descripcion;
    
    @Column(name = "estado", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ClinicStatus estado;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", updatable = false, nullable = false)
    private String createdBy;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", nullable = false)
    private String updatedBy;
    
    /**
     * Clinic status enum matching the domain model.
     */
    public enum ClinicStatus {
        ACTIVE,
        INACTIVE,
        DELETED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (createdBy == null) createdBy = "internal";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}