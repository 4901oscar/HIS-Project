package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.port.out.PharmacyServiceClient;
import com.medframe.clinical.domain.port.out.PrescriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionGenerator {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionGenerator.class);
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    private final PrescriptionRepository prescriptionRepository;
    private final PharmacyServiceClient pharmacyServiceClient;
    private final SecureRandom secureRandom;

    public PrescriptionGenerator(PrescriptionRepository prescriptionRepository,
                                   PharmacyServiceClient pharmacyServiceClient,
                                   SecureRandom secureRandom) {
        this.prescriptionRepository = prescriptionRepository;
        this.pharmacyServiceClient = pharmacyServiceClient;
        this.secureRandom = secureRandom;
    }

    public Prescription generatePrescription(String consultationId, String patientId,
                                              String doctorId,
                                              List<Prescription.Medication> medications) {

        String code = generateUniqueCode();

        Prescription prescription = new Prescription();
        prescription.setConsultationId(consultationId);
        prescription.setPatientId(patientId);
        prescription.setDoctorId(doctorId);
        prescription.setPrescriptionCode(code);
        prescription.setMedications(medications);
        prescription.setStatus(Prescription.PrescriptionStatus.PENDING);
        prescription.setIssuedAt(LocalDateTime.now());
        prescription.setIssuedBy(doctorId);

        Prescription saved = prescriptionRepository.save(prescription);

        // Notify Pharmacy Service — fire and forget (eventual consistency)
        // Failure here must NOT fail the transaction
        try {
            pharmacyServiceClient.notifyNewPrescription(saved);
        } catch (Exception e) {
            log.error("No se pudo notificar a Pharmacy Service sobre la receta {}: {}",
                    code, e.getMessage());
        }

        return saved;
    }

    /**
     * Generates a unique 8-character alphanumeric code.
     * Pure generation logic — checks uniqueness against repository.
     */
    public String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (prescriptionRepository.existsByCode(code));
        return code;
    }

    /**
     * Generate a random 8-char alphanumeric code using SecureRandom.
     * This is a pure-enough function for property testing.
     */
    public String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(secureRandom.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
