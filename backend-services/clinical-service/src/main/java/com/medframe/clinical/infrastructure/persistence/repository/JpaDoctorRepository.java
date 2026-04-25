package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

/**
 * Spring Data JPA repository for DoctorEntity.
 * 
 * <p>This interface provides database access methods for doctor records.
 * Spring Data JPA automatically implements this interface at runtime.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Repository
public interface JpaDoctorRepository extends JpaRepository<DoctorEntity, String> {
    
    /**
     * Finds all doctors working a specific shift with a given status.
     * 
     * @param shiftStart the start time of the shift
     * @param shiftEnd the end time of the shift
     * @param status the doctor status
     * @return list of doctors matching the criteria
     */
    @Query("SELECT d FROM DoctorEntity d WHERE d.shiftStart = :shiftStart AND d.shiftEnd = :shiftEnd AND d.status = :status")
    List<DoctorEntity> findByShiftAndStatus(
        @Param("shiftStart") LocalTime shiftStart,
        @Param("shiftEnd") LocalTime shiftEnd,
        @Param("status") DoctorEntity.DoctorStatus status
    );
    
    /**
     * Finds all active doctors in the system.
     * 
     * @return list of all active doctors
     */
    @Query("SELECT d FROM DoctorEntity d WHERE d.status = 'ACTIVE'")
    List<DoctorEntity> findAllActive();
}
