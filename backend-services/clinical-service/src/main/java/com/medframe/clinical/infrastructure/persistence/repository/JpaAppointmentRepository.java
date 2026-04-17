package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA Repository for Appointment entities.
 * Provides database access for appointment records.
 */
@Repository
public interface JpaAppointmentRepository extends JpaRepository<AppointmentEntity, String> {
    
    /**
     * Find all appointments for a specific doctor on a specific date.
     * 
     * @param doctorId the doctor ID
     * @param appointmentDate the appointment date
     * @return list of appointment entities for the doctor on that date
     */
    List<AppointmentEntity> findByDoctorIdAndAppointmentDate(String doctorId, LocalDate appointmentDate);
    
    /**
     * Find all appointments for a specific patient.
     * 
     * @param patientId the patient ID
     * @return list of appointment entities for the patient
     */
    List<AppointmentEntity> findByPatientId(String patientId);
}
