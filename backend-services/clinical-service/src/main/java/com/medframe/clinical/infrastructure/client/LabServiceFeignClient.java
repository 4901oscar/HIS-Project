package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.infrastructure.client.dto.LabOrderNotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "lab-service", url = "${services.lab-service.url}")
public interface LabServiceFeignClient {
    
    @PostMapping("/api/lab/orders/notify")
    void notifyNewLabOrder(@RequestBody LabOrderNotificationDTO labOrder);
}
