package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.Prescription;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository {
    Prescription save(Prescription prescription);
    Optional<Prescription> findById(String id);
    boolean existsByCode(String code);
    List<Prescription> findByPatientIdOrderByIssuedAtDesc(String patientId);
    List<Prescription> findByConsultationId(String consultationId);
}
