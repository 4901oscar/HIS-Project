package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.LabOrder;

import java.util.List;

public interface GenerateLabOrderUseCase {
    LabOrder generateLabOrder(String consultationId, String patientId,
                              String doctorId, List<String> testNames);
}
