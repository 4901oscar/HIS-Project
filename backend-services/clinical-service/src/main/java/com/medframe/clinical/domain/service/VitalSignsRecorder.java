package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.InvalidVitalSignsException;
import com.medframe.clinical.domain.exception.VitalSignsNotFoundException;
import com.medframe.clinical.domain.model.VitalSigns;
import com.medframe.clinical.domain.port.out.VitalSignsRepository;

import java.time.LocalDateTime;

public class VitalSignsRecorder {

    private final VitalSignsRepository vitalSignsRepository;

    public VitalSignsRecorder(VitalSignsRepository vitalSignsRepository) {
        this.vitalSignsRepository = vitalSignsRepository;
    }

    public VitalSigns recordVitalSigns(String patientId, Integer systolic, Integer diastolic,
                                       Integer heartRate, Integer respiratoryRate,
                                       Double temperature, Integer oxygenSaturation,
                                       Double weight, Double height, String recordedBy) {

        VitalSigns vitalSigns = new VitalSigns();
        vitalSigns.setPatientId(patientId);
        vitalSigns.setSystolicPressure(systolic);
        vitalSigns.setDiastolicPressure(diastolic);
        vitalSigns.setHeartRate(heartRate);
        vitalSigns.setRespiratoryRate(respiratoryRate);
        vitalSigns.setTemperature(temperature);
        vitalSigns.setOxygenSaturation(oxygenSaturation);
        vitalSigns.setWeight(weight);
        vitalSigns.setHeight(height);
        vitalSigns.setRecordedAt(LocalDateTime.now());
        vitalSigns.setRecordedBy(recordedBy);

        // Calculate BMI (business method on entity)
        vitalSigns.calculateBMI();

        // Validate physiological ranges (business method on entity)
        if (!vitalSigns.isValid()) {
            throw new InvalidVitalSignsException(
                    "Los signos vitales están fuera de los rangos fisiológicos válidos. " +
                    "Verifique: PA sistólica (50-250), diastólica (30-150), " +
                    "FC (20-250 bpm), temperatura (30-45°C), SpO2 (0-100%).");
        }

        return vitalSignsRepository.save(vitalSigns);
    }

    public VitalSigns getLatestVitalSigns(String patientId) {
        return vitalSignsRepository.findLatestByPatientId(patientId)
                .orElseThrow(() -> new VitalSignsNotFoundException(
                        "No se encontraron signos vitales para el paciente."));
    }
}
