package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.VitalSignsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for VitalSigns entities.
 * Provides database access for vital signs records.
 */
@Repository
public interface JpaVitalSignsRepository extends JpaRepository<VitalSignsEntity, String> {
    
    /**
     * Find the most recent vital signs for a specific patient.
     * 
     * @param patientId the patient ID
     * @return optional containing the most recent vital signs entity
     */
    Optional<VitalSignsEntity> findFirstByPatientIdOrderByRecordedAtDesc(String patientId);
    
    /**
     * Find the most recent vital signs for a specific appointment.
     * Uses findFirst to handle the case where multiple records exist (upsert guard).
     *
     * @param appointmentId the appointment ID
     * @return optional containing the most recent vital signs entity for the appointment
     */
    Optional<VitalSignsEntity> findFirstByAppointmentIdOrderByRecordedAtDesc(String appointmentId);
    
    /**
     * Find all vital signs for a specific patient, ordered by recorded date descending.
     * 
     * @param patientId the patient ID
     * @return list of vital signs entities ordered by date (most recent first)
     */
    List<VitalSignsEntity> findByPatientIdOrderByRecordedAtDesc(String patientId);
}
