package com.medflow.pharmacy.repository;

import com.medflow.pharmacy.model.Dispensation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar registros de despacho de medicamentos.
 */
@Repository
public interface DispensationRepository extends JpaRepository<Dispensation, String> {
    
    /**
     * Busca dispensaciones de un paciente específico.
     */
    List<Dispensation> findByPatientIdOrderByDispensedAtDesc(String patientId);
    
    /**
     * Busca dispensaciones de una prescripción específica.
     */
    List<Dispensation> findByPrescriptionId(String prescriptionId);
}
