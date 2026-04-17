package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.domain.model.PriorityLevel;
import com.medframe.clinical.infrastructure.persistence.entity.ManchesterDiscriminatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ManchesterDiscriminator entities.
 * Provides database access for Manchester triage discriminator catalog.
 */
@Repository
public interface JpaManchesterDiscriminatorRepository extends JpaRepository<ManchesterDiscriminatorEntity, String> {
    
    /**
     * Find active discriminators by their IDs.
     * 
     * @param ids list of discriminator IDs
     * @return list of active discriminator entities matching the IDs
     */
    List<ManchesterDiscriminatorEntity> findByIdInAndActiveTrue(List<String> ids);
    
    /**
     * Find all active discriminators.
     * 
     * @return list of all active discriminator entities
     */
    List<ManchesterDiscriminatorEntity> findByActiveTrue();
    
    /**
     * Find a discriminator by its unique code.
     * 
     * @param code the discriminator code
     * @return optional containing the discriminator entity if found
     */
    Optional<ManchesterDiscriminatorEntity> findByCode(String code);
    
    /**
     * Check if a discriminator with the given code exists.
     * 
     * @param code the discriminator code
     * @return true if a discriminator with this code exists, false otherwise
     */
    boolean existsByCode(String code);
    
    /**
     * Find all discriminators by priority level.
     * 
     * @param priorityLevel the priority level
     * @return list of discriminator entities with the specified priority level
     */
    List<ManchesterDiscriminatorEntity> findByPriorityLevel(PriorityLevel priorityLevel);
    
    /**
     * Find all discriminators for a specific motif.
     * 
     * @param motifId the motif ID
     * @return list of discriminator entities associated with the motif
     */
    List<ManchesterDiscriminatorEntity> findByMotifId(String motifId);
}
