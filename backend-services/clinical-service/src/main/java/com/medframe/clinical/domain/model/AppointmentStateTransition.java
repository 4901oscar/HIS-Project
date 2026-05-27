package com.medframe.clinical.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_state_transitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentStateTransition {
    
    @Id
    @Column(name = "id", length = 36)
    private String id;
    
    @Column(name = "appointment_id", nullable = false, length = 36)
    private String appointmentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "from_state", nullable = false, length = 50)
    private Appointment.AppointmentStatus fromState;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "to_state", nullable = false, length = 50)
    private Appointment.AppointmentStatus toState;
    
    @Column(name = "transitioned_by", nullable = false)
    private String transitionedBy;
    
    @Column(name = "transitioned_at", nullable = false)
    private LocalDateTime transitionedAt;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", updatable = false, length = 36)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) id = java.util.UUID.randomUUID().toString();
        if (transitionedAt == null) transitionedAt = LocalDateTime.now();
        if (createdAt == null) createdAt = transitionedAt;
        if (createdBy == null) createdBy = "internal";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
