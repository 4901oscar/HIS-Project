package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.port.out.LabOrderRepository;
import com.medframe.clinical.domain.port.out.LabServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

public class LabOrderGenerator {

    private static final Logger log = LoggerFactory.getLogger(LabOrderGenerator.class);
    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;

    private final LabOrderRepository labOrderRepository;
    private final LabServiceClient labServiceClient;
    private final SecureRandom secureRandom;

    public LabOrderGenerator(LabOrderRepository labOrderRepository,
                               LabServiceClient labServiceClient,
                               SecureRandom secureRandom) {
        this.labOrderRepository = labOrderRepository;
        this.labServiceClient = labServiceClient;
        this.secureRandom = secureRandom;
    }

    public LabOrder generateLabOrder(String consultationId, String patientId,
                                      String doctorId, String appointmentId, List<String> testNames) {

        String code = generateUniqueCode();

        LabOrder labOrder = new LabOrder();
        labOrder.setConsultationId(consultationId);
        labOrder.setPatientId(patientId);
        labOrder.setDoctorId(doctorId);
        labOrder.setOrderCode(code);
        labOrder.setTestNames(testNames);
        labOrder.setStatus(LabOrder.LabOrderStatus.PENDING);
        labOrder.setOrderedAt(LocalDateTime.now());
        labOrder.setOrderedBy(doctorId);

        LabOrder saved = labOrderRepository.save(labOrder);

        // Notify Lab Service — fire and forget (eventual consistency)
        try {
            labServiceClient.notifyNewLabOrder(saved, appointmentId);
        } catch (Exception e) {
            log.error("No se pudo notificar a Lab Service sobre la orden {}: {}",
                    code, e.getMessage());
        }

        return saved;
    }

    public String generateUniqueCode() {
        String code;
        do {
            code = generateCode();
        } while (labOrderRepository.existsByCode(code));
        return code;
    }

    public String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(secureRandom.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}
