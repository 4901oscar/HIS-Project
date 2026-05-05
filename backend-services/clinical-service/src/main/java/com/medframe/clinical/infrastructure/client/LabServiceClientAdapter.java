package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.port.out.LabServiceClient;
import com.medframe.clinical.infrastructure.client.dto.LabOrderNotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LabServiceClientAdapter implements LabServiceClient {
    
    private final LabServiceFeignClient feignClient;
    
    @Override
    @Async
    public void notifyNewLabOrder(LabOrder labOrder, String appointmentId) {
        try {
            log.debug("Notifying Lab Service about new lab order: {}", 
                     labOrder.getOrderCode());
            
            LabOrderNotificationDTO dto = mapToNotificationDTO(labOrder, appointmentId);
            feignClient.notifyNewLabOrder(dto);
            
            log.info("Successfully notified Lab Service about lab order: {}", 
                    labOrder.getOrderCode());
        } catch (Exception e) {
            // Log error but don't throw - eventual consistency pattern
            log.error("Failed to notify Lab Service about lab order {}: {}. " +
                     "This is expected behavior (eventual consistency). Error: {}", 
                     labOrder.getOrderCode(), 
                     e.getClass().getSimpleName(),
                     e.getMessage());
        }
    }
    
    private LabOrderNotificationDTO mapToNotificationDTO(LabOrder labOrder, String appointmentId) {
        return new LabOrderNotificationDTO(
            labOrder.getId(),
            labOrder.getOrderCode(),
            labOrder.getPatientId(),
            labOrder.getDoctorId(),
            appointmentId,
            labOrder.getTestNames(),
            labOrder.getOrderedAt()
        );
    }
}
