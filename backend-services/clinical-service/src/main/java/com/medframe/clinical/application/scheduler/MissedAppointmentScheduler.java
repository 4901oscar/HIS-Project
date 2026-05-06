package com.medframe.clinical.application.scheduler;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job para detectar y marcar citas perdidas.
 * 
 * <p>Ejecuta cada 5 minutos y marca como MISSED las citas en estado SCHEDULED
 * donde han pasado más de 60 minutos desde la hora de la cita.
 * 
 * <p><strong>Requisitos relacionados:</strong></p>
 * <ul>
 *   <li>REQ-12.1: Detección automática de citas perdidas</li>
 *   <li>REQ-12.2: Ventana de 60 minutos después de la hora de cita</li>
 *   <li>REQ-12.3: Ejecución cada 5 minutos</li>
 * </ul>
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class MissedAppointmentScheduler {
    
    private final AppointmentRepository appointmentRepository;
    
    public MissedAppointmentScheduler(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }
    
    /**
     * Detecta y marca citas perdidas cada 5 minutos.
     * 
     * <p>Busca citas en estado SCHEDULED donde:
     * <ul>
     *   <li>appointmentDateTime + 60 minutos < hora actual</li>
     * </ul>
     * 
     * <p>Para cada cita encontrada:
     * <ul>
     *   <li>Llama a appointment.markAsMissed()</li>
     *   <li>Persiste el cambio</li>
     *   <li>Registra en log para reportes</li>
     * </ul>
     */
    @Scheduled(fixedRate = 300000) // 5 minutos = 300,000 ms
    @Transactional
    public void detectMissedAppointments() {
        log.debug("Iniciando detección de citas perdidas...");
        
        try {
            // Obtener todas las citas en estado SCHEDULED
            List<Appointment> scheduledAppointments = appointmentRepository.findAll().stream()
                    .filter(a -> a.getStatus() == Appointment.AppointmentStatus.SCHEDULED)
                    .toList();
            
            LocalDateTime now = LocalDateTime.now();
            int missedCount = 0;
            
            for (Appointment appointment : scheduledAppointments) {
                // Calcular el datetime de la cita + 60 minutos
                LocalDateTime appointmentDateTime = LocalDateTime.of(
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime()
                );
                LocalDateTime expirationTime = appointmentDateTime.plusMinutes(60);
                
                // Si ya pasó la ventana de 60 minutos, marcar como perdida
                if (now.isAfter(expirationTime)) {
                    try {
                        appointment.markAsMissed();
                        appointmentRepository.update(appointment);
                        missedCount++;
                        
                        log.info("Cita marcada como perdida - ID: {}, Paciente: {}, Fecha/Hora: {} {}", 
                                appointment.getId(),
                                appointment.getPatientId(),
                                appointment.getAppointmentDate(),
                                appointment.getAppointmentTime());
                    } catch (Exception e) {
                        log.error("Error al marcar cita como perdida - ID: {}", 
                                appointment.getId(), e);
                    }
                }
            }
            
            if (missedCount > 0) {
                log.info("Detección de citas perdidas completada - {} citas marcadas como MISSED", 
                        missedCount);
            } else {
                log.debug("Detección de citas perdidas completada - No se encontraron citas perdidas");
            }
            
        } catch (Exception e) {
            log.error("Error en el proceso de detección de citas perdidas", e);
        }
    }
}
