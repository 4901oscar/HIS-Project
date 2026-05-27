package com.medflow.pharmacy.repository;

import com.medflow.pharmacy.model.Prescription;
import com.medflow.pharmacy.model.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar prescripciones médicas.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, String> {
    
    /**
     * Busca prescripciones por estado.
     */
    List<Prescription> findByStatus(PrescriptionStatus status);
    
    /**
     * Busca prescripciones de un paciente específico.
     */
    List<Prescription> findByPatientIdOrderByIssuedAtDesc(String patientId);
    
    /**
     * Busca una prescripción por su código único.
     */
    Optional<Prescription> findByPrescriptionCode(String prescriptionCode);
    
    /**
     * Verifica si existe una prescripción con el código dado.
     */
    boolean existsByPrescriptionCode(String prescriptionCode);
}
