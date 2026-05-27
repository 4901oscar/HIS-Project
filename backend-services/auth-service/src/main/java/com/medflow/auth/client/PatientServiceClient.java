package com.medflow.auth.client;

import com.medflow.auth.dto.CreatePatientInternalRequest;
import com.medflow.auth.dto.PatientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign Client para comunicación con patient-service.
 * Permite crear registros de pacientes en patient_schema.patients desde auth-service.
 */
@FeignClient(name = "patient-service", path = "/api/patients")
public interface PatientServiceClient {

    /**
     * Crea un nuevo registro de paciente en patient-service.
     * Este endpoint es interno y solo debe ser llamado por auth-service.
     *
     * @param request Datos completos del paciente a crear
     * @return Respuesta con los datos del paciente creado
     */
    @PostMapping("/internal")
    PatientResponse createPatient(@RequestBody CreatePatientInternalRequest request);
}
