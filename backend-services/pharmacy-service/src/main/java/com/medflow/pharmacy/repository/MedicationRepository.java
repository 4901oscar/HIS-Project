package com.medflow.pharmacy.repository;

import com.medflow.pharmacy.model.Medication;
import com.medflow.pharmacy.model.MedicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, String> {
    List<Medication> findByStatusNot(MedicationStatus status);
    List<Medication> findByStatus(MedicationStatus status);
    Optional<Medication> findByNameIgnoreCase(String name);
}
