package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.LabOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for LabOrder entities.
 * Provides database access for lab order records.
 */
@Repository
public interface JpaLabOrderRepository extends JpaRepository<LabOrderEntity, String> {
    
    /**
     * Check if a lab order with the given code exists.
     * 
     * @param orderCode the lab order code
     * @return true if a lab order with this code exists, false otherwise
     */
    boolean existsByOrderCode(String orderCode);
    
    /**
     * Find a lab order by its unique code.
     * 
     * @param orderCode the lab order code
     * @return optional containing the lab order entity if found
     */
    Optional<LabOrderEntity> findByOrderCode(String orderCode);
    
    /**
     * Find all lab orders for a specific patient, ordered by ordered date descending.
     * 
     * @param patientId the patient ID
     * @return list of lab order entities ordered by date (most recent first)
     */
    List<LabOrderEntity> findByPatientIdOrderByOrderedAtDesc(String patientId);
    
    /**
     * Find all lab orders for a specific consultation.
     * 
     * @param consultationId the consultation ID
     * @return list of lab order entities for the consultation
     */
    List<LabOrderEntity> findByConsultationId(String consultationId);
}
