package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.exception.VitalSignsNotFoundException;
import com.medframe.clinical.domain.model.ManchesterDiscriminator;
import com.medframe.clinical.domain.model.PriorityLevel;
import com.medframe.clinical.domain.model.Triage;
import com.medframe.clinical.domain.model.VitalSigns;
import com.medframe.clinical.domain.port.out.ManchesterCatalogRepository;
import com.medframe.clinical.domain.port.out.VitalSignsRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * TriageEngine - Core business logic for Manchester Triage System.
 * Pure domain service with no Spring annotations.
 * 
 * Algorithm: Select the HIGHEST priority (minimum wait time) among all discriminators.
 * This is the correct Manchester implementation — escalate to the most urgent level.
 */
public class TriageEngine {

    private final VitalSignsRepository vitalSignsRepository;
    private final ManchesterCatalogRepository manchesterCatalogRepository;

    public TriageEngine(VitalSignsRepository vitalSignsRepository,
                        ManchesterCatalogRepository manchesterCatalogRepository) {
        this.vitalSignsRepository = vitalSignsRepository;
        this.manchesterCatalogRepository = manchesterCatalogRepository;
    }

    public Triage performTriage(String patientId, String doctorId,
                                String motifId, List<String> discriminatorIds) {

        // 1. Verify patient has vital signs (required for triage)
        VitalSigns vitalSigns = vitalSignsRepository.findLatestByPatientId(patientId)
                .orElseThrow(() -> new VitalSignsNotFoundException(
                        "El paciente no tiene signos vitales registrados. " +
                        "Por favor, capture los signos vitales antes de realizar el triaje."));

        // 2. Load discriminators from catalog
        List<ManchesterDiscriminator> discriminators =
                manchesterCatalogRepository.findDiscriminatorsByIds(discriminatorIds);

        if (discriminators.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se encontraron discriminadores válidos. " +
                    "Seleccione al menos un discriminador del catálogo Manchester.");
        }

        // 3. Calculate priority level using Manchester algorithm
        PriorityLevel priorityLevel = calculatePriorityLevel(discriminators);

        // 4. Build and return Triage domain object
        Triage triage = new Triage();
        triage.setPatientId(patientId);
        triage.setDoctorId(doctorId);
        triage.setMotifId(motifId);
        triage.setDiscriminatorIds(discriminatorIds);
        triage.setPriorityLevel(priorityLevel);
        triage.setMaxWaitTimeMinutes(priorityLevel.getMaxWaitMinutes());
        triage.setPerformedAt(LocalDateTime.now());
        triage.setPerformedBy(doctorId);

        return triage;
    }

    /**
     * Core Manchester Algorithm:
     * Select the discriminator with the MINIMUM wait time (MAXIMUM urgency).
     * RED(0) > ORANGE(10) > YELLOW(60) > GREEN(120) > BLUE(240)
     * 
     * This method is a pure function — deterministic for the same input.
     * Property: for any set of discriminators, result = min(waitTime) among all discriminators.
     */
    public PriorityLevel calculatePriorityLevel(List<ManchesterDiscriminator> discriminators) {
        return discriminators.stream()
                .map(ManchesterDiscriminator::getPriorityLevel)
                .min(Comparator.comparingInt(PriorityLevel::getMaxWaitMinutes))
                .orElse(PriorityLevel.BLUE);
    }
}
