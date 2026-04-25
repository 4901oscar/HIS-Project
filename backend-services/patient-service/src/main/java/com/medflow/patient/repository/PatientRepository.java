package com.medflow.patient.repository;

import com.medflow.patient.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByDpi(String dpi);
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByAuthUserId(String authUserId);
    boolean existsByDpi(String dpi);
    boolean existsByEmail(String email);

    @Query("""
        SELECT p FROM Patient p
        WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%',:q,'%'))
           OR LOWER(p.firstLastName) LIKE LOWER(CONCAT('%',:q,'%'))
           OR p.dpi = :q
           OR LOWER(p.email) = LOWER(:q)
        ORDER BY p.firstLastName, p.firstName
        LIMIT 50
        """)
    List<Patient> search(@Param("q") String query);
}
