package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.infrastructure.client.dto.PrescriptionNotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "pharmacy-service", url = "${services.pharmacy-service.url}")
public interface PharmacyServiceFeignClient {
    
    @PostMapping("/api/pharmacy/prescriptions/notify")
    void notifyNewPrescription(@RequestBody PrescriptionNotificationDTO prescription);
}
