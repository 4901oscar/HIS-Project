package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.VitalSigns;

import java.util.List;
import java.util.Optional;

public interface VitalSignsRepository {
    VitalSigns save(VitalSigns vitalSigns);
    Optional<VitalSigns> findLatestByPatientId(String patientId);
    List<VitalSigns> findByPatientIdOrderByRecordedAtDesc(String patientId);
}
