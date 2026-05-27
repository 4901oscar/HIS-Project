package com.medflow.pharmacy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "medications", schema = "pharmacy_schema", indexes = {
    @Index(name = "idx_medications_stock", columnList = "current_stock")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @Column(name = "min_stock", nullable = false)
    private Integer minStock = 10;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MedicationStatus status = MedicationStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

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
