package com.medframe.clinical.domain.port.in;

import com.medframe.clinical.domain.model.VitalSigns;

public interface RecordVitalSignsUseCase {
    VitalSigns recordVitalSigns(String appointmentId, String patientId, Integer systolic, Integer diastolic,
                                Integer heartRate, Integer respiratoryRate,
                                Double temperature, Integer oxygenSaturation,
                                Double weight, Double height, String recordedBy);
    VitalSigns getLatestVitalSigns(String patientId);
}
