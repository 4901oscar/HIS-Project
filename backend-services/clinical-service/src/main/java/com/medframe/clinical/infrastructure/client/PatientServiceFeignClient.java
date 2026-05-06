package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.infrastructure.client.dto.PatientDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "patient-service",
    url = "${services.patient-service.url}",
    configuration = com.medframe.clinical.config.FeignConfiguration.class
)
public interface PatientServiceFeignClient {
    
    @GetMapping("/api/patients/{id}")
    PatientDTO getPatient(@PathVariable("id") String id);
    
    @GetMapping("/api/patients/by-auth-user/{authUserId}")
    PatientDTO getPatientByAuthUserId(@PathVariable("authUserId") String authUserId);
}
