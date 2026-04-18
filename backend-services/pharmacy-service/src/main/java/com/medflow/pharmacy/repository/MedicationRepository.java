package com.medflow.pharmacy.repository;

import com.medflow.pharmacy.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar el catálogo de medicamentos.
 */
@Repository
public interface MedicationRepository extends JpaRepository<Medication, String> {
    
    /**
     * Busca un medicamento por nombre (case-insensitive).
     */
    Optional<Medication> findByNameIgnoreCase(String name);
    
    /**
     * Busca medicamentos con stock bajo (menor al mínimo).
     */
    List<Medication> findByCurrentStockLessThan(Integer minStock);
    
    /**
     * Busca medicamentos activos.
     */
    List<Medication> findByActiveTrue();
    
    /**
     * Busca medicamentos activos con stock bajo.
     */
    List<Medication> findByActiveTrueAndCurrentStockLessThan(Integer minStock);
}
