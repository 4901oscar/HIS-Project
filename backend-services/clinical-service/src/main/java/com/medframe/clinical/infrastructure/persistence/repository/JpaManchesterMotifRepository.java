package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.ManchesterMotifEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for ManchesterMotif entities.
 * Provides database access for Manchester triage motif catalog.
 */
@Repository
public interface JpaManchesterMotifRepository extends JpaRepository<ManchesterMotifEntity, String> {
    
    /**
     * Find all active motifs.
     * 
     * @return list of active motif entities
     */
    List<ManchesterMotifEntity> findByActiveTrue();
    
    /**
     * Find a motif by its unique code.
     * 
     * @param code the motif code
     * @return optional containing the motif entity if found
     */
    Optional<ManchesterMotifEntity> findByCode(String code);
    
    /**
     * Check if a motif with the given code exists.
     * 
     * @param code the motif code
     * @return true if a motif with this code exists, false otherwise
     */
    boolean existsByCode(String code);
    
    /**
     * Find all motifs by category.
     * 
     * @param category the motif category
     * @return list of motif entities in the category
     */
    List<ManchesterMotifEntity> findByCategory(String category);
}
