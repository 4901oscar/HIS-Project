package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.port.out.PharmacyServiceClient;
import com.medframe.clinical.infrastructure.client.dto.PrescriptionNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class PharmacyServiceClientAdapter implements PharmacyServiceClient {
    
    private final PharmacyServiceFeignClient feignClient;
    
    @Override
    @Async
    public void notifyNewPrescription(Prescription prescription) {
        try {
            log.debug("Notifying Pharmacy Service about new prescription: {}", 
                     prescription.getPrescriptionCode());
            
            PrescriptionNotificationDTO dto = mapToNotificationDTO(prescription);
            feignClient.notifyNewPrescription(dto);
            
            log.info("Successfully notified Pharmacy Service about prescription: {}", 
                    prescription.getPrescriptionCode());
        } catch (Exception e) {
            // Log error but don't throw - eventual consistency pattern
            log.error("Failed to notify Pharmacy Service about prescription {}: {}. " +
                     "This is expected behavior (eventual consistency). Error: {}", 
                     prescription.getPrescriptionCode(), 
                     e.getClass().getSimpleName(),
                     e.getMessage());
        }
    }
    
    private PrescriptionNotificationDTO mapToNotificationDTO(Prescription prescription) {
        var medications = prescription.getMedications().stream()
            .map(med -> new PrescriptionNotificationDTO.MedicationDTO(
                med.getName(),
                med.getDosage(),
                med.getFrequency(),
                med.getDurationDays(),
                med.getRoute(),
                med.getSpecialInstructions()
            ))
            .collect(Collectors.toList());
        
        return new PrescriptionNotificationDTO(
            prescription.getId(),
            prescription.getPrescriptionCode(),
            prescription.getPatientId(),
            prescription.getDoctorId(),
            medications,
            prescription.getIssuedAt()
        );
    }
}
