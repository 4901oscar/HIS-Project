package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.PrescriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Prescription entities.
 * Provides database access for prescription records.
 */
@Repository
public interface JpaPrescriptionRepository extends JpaRepository<PrescriptionEntity, String> {
    
    /**
     * Check if a prescription with the given code exists.
     * 
     * @param prescriptionCode the prescription code
     * @return true if a prescription with this code exists, false otherwise
     */
    boolean existsByPrescriptionCode(String prescriptionCode);
    
    /**
     * Find a prescription by its unique code.
     * 
     * @param prescriptionCode the prescription code
     * @return optional containing the prescription entity if found
     */
    Optional<PrescriptionEntity> findByPrescriptionCode(String prescriptionCode);
    
    /**
     * Find all prescriptions for a specific patient, ordered by issued date descending.
     * 
     * @param patientId the patient ID
     * @return list of prescription entities ordered by date (most recent first)
     */
    List<PrescriptionEntity> findByPatientIdOrderByIssuedAtDesc(String patientId);
    
    /**
     * Find all prescriptions for a specific consultation.
     * 
     * @param consultationId the consultation ID
     * @return list of prescription entities for the consultation
     */
    List<PrescriptionEntity> findByConsultationId(String consultationId);
}
