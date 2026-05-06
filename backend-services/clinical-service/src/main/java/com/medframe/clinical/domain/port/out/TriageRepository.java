package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.Triage;

import java.util.List;
import java.util.Optional;

public interface TriageRepository {
    Triage save(Triage triage);
    Optional<Triage> findById(String id);
    Optional<Triage> findByAppointmentId(String appointmentId);
    List<Triage> findByPatientId(String patientId);
}
