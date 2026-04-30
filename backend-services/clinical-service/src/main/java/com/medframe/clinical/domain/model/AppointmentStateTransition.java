package com.medframe.clinical.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad de auditoría para transiciones de estado de citas.
 * Registra cada cambio de estado con timestamp y usuario responsable.
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-14.2: Auditoría de transiciones de estado</li>
 * </ul>
 */
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
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (transitionedAt == null) {
            transitionedAt = LocalDateTime.now();
        }
    }
}
