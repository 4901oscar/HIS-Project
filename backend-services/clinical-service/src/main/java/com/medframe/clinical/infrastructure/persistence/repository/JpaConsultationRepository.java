package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.ConsultationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Consultation entities.
 * Provides database access for consultation records.
 */
@Repository
public interface JpaConsultationRepository extends JpaRepository<ConsultationEntity, String> {
    
    /**
     * Find all consultations for a specific patient, ordered by consultation date descending.
     * 
     * @param patientId the patient ID
     * @return list of consultation entities ordered by date (most recent first)
     */
    List<ConsultationEntity> findByPatientIdOrderByConsultationDateDesc(String patientId);
    
    /**
     * Find all consultations for a specific doctor.
     * 
     * @param doctorId the doctor ID
     * @return list of consultation entities for the doctor
     */
    List<ConsultationEntity> findByDoctorId(String doctorId);
}
