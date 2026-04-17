package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.Triage;

import java.util.List;

public interface PerformTriageUseCase {
    Triage performTriage(String patientId, String doctorId,
                         String motifId, List<String> discriminatorIds);
}
