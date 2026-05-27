package com.medframe.clinical.infrastructure.persistence.repository;

import com.medframe.clinical.infrastructure.persistence.entity.DoctorAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for DoctorAvailabilityEntity.
 * 
 * <p>This interface provides database access methods for doctor availability records.
 * Spring Data JPA automatically implements this interface at runtime.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Repository
public interface JpaDoctorAvailabilityRepository extends JpaRepository<DoctorAvailabilityEntity, String> {
    
    /**
     * Finds all availability records for a specific doctor on a specific date.
     * 
     * @param doctorId the doctor ID
     * @param date the date to check
     * @return list of availability records (typically 0 or 1 element)
     */
    List<DoctorAvailabilityEntity> findByDoctorIdAndDate(String doctorId, LocalDate date);
    
    /**
     * Finds all availability records for a specific doctor within a date range.
     * 
     * @param doctorId the doctor ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of availability records within the date range
     */
    @Query("SELECT da FROM DoctorAvailabilityEntity da WHERE da.doctorId = :doctorId AND da.date BETWEEN :startDate AND :endDate")
    List<DoctorAvailabilityEntity> findByDoctorIdAndDateBetween(
        @Param("doctorId") String doctorId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Deletes a doctor availability record by doctor ID and date.
     * 
     * @param doctorId the doctor ID
     * @param date the date
     */
    @Modifying
    @Query("DELETE FROM DoctorAvailabilityEntity da WHERE da.doctorId = :doctorId AND da.date = :date")
    void deleteByDoctorIdAndDate(@Param("doctorId") String doctorId, @Param("date") LocalDate date);
    
    /**
     * Deletes all availability records for a specific doctor.
     * 
     * @param doctorId the doctor ID
     */
    @Modifying
    @Query("DELETE FROM DoctorAvailabilityEntity da WHERE da.doctorId = :doctorId")
    void deleteByDoctorId(@Param("doctorId") String doctorId);
    
    /**
     * Finds all availability records for a specific doctor.
     * 
     * @param doctorId the doctor ID
     * @return list of all availability records for the doctor
     */
    List<DoctorAvailabilityEntity> findByDoctorId(String doctorId);
}
