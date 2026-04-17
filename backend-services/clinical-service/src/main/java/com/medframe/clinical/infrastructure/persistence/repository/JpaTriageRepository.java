package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.TriageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Triage entities.
 * Provides database access for triage records.
 */
@Repository
public interface JpaTriageRepository extends JpaRepository<TriageEntity, String> {
    
    /**
     * Find all triages for a specific patient.
     * 
     * @param patientId the patient ID
     * @return list of triage entities for the patient
     */
    List<TriageEntity> findByPatientId(String patientId);
}
