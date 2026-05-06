package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.ClinicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for ClinicEntity.
 * 
 * <p>This interface provides database access methods for clinic records.
 * Spring Data JPA automatically implements this interface at runtime.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Repository
public interface JpaClinicRepository extends JpaRepository<ClinicEntity, String> {
    
    /**
     * Finds all clinics sorted by creation date descending.
     * 
     * @return list of all clinics ordered by createdAt desc
     */
    @Query("SELECT c FROM ClinicEntity c ORDER BY c.createdAt DESC")
    List<ClinicEntity> findAllOrderByCreatedAtDesc();
    
    /**
     * Finds all clinics with a specific status, sorted by creation date descending.
     * 
     * @param estado the clinic status
     * @return list of clinics matching the status, ordered by createdAt desc
     */
    @Query("SELECT c FROM ClinicEntity c WHERE c.estado = :estado ORDER BY c.createdAt DESC")
    List<ClinicEntity> findByEstadoOrderByCreatedAtDesc(@Param("estado") ClinicEntity.ClinicStatus estado);
    
    /**
     * Checks if a clinic with the specified code exists.
     * 
     * @param codigo the clinic code
     * @return true if exists, false otherwise
     */
    boolean existsByCodigo(String codigo);
    
    /**
     * Finds a clinic by its unique code.
     * 
     * @param codigo the clinic code
     * @return an Optional containing the clinic if found
     */
    Optional<ClinicEntity> findByCodigo(String codigo);
}
