package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.Consultation;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository {
    Consultation save(Consultation consultation);
    Optional<Consultation> findById(String id);
    List<Consultation> findByPatientIdOrderByConsultationDateDesc(String patientId);
}
