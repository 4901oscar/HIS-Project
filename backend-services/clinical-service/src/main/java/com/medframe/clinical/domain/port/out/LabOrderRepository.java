package com.medframe.clinical.domain.port.out;

import com.medframe.clinical.domain.model.LabOrder;

import java.util.List;
import java.util.Optional;

public interface LabOrderRepository {
    LabOrder save(LabOrder labOrder);
    Optional<LabOrder> findById(String id);
    boolean existsByCode(String code);
    List<LabOrder> findByPatientIdOrderByOrderedAtDesc(String patientId);
}
