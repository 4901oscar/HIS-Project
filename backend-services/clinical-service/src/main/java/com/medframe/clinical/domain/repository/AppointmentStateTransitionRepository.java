package com.medframe.clinical.domain.repository;

import com.medframe.clinical.domain.model.AppointmentStateTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para auditoría de transiciones de estado de citas.
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-14.2: Auditoría de transiciones de estado</li>
 * </ul>
 */
@Repository
public interface AppointmentStateTransitionRepository extends JpaRepository<AppointmentStateTransition, String> {
    
    /**
     * Encuentra todas las transiciones de estado para una cita específica.
     * Ordenadas por fecha de transición descendente (más reciente primero).
     * 
     * @param appointmentId ID de la cita
     * @return Lista de transiciones de estado
     */
    List<AppointmentStateTransition> findByAppointmentIdOrderByTransitionedAtDesc(String appointmentId);
}
