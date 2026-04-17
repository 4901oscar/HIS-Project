package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.exception.PatientNotFoundException;
import com.medframe.clinical.domain.exception.ServiceUnavailableException;
import com.medframe.clinical.domain.port.out.PatientServiceClient;
import com.medframe.clinical.infrastructure.client.dto.PatientDTO;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PatientServiceClientAdapter implements PatientServiceClient {
    
    private final PatientServiceFeignClient patientServiceFeignClient;
    
    @Override
    @CircuitBreaker(name = "patientService", fallbackMethod = "getPatientFallback")
    @Retry(name = "patientService")
    public Object getPatient(String patientId) {
        try {
            log.debug("Calling Patient Service to get patient with ID: {}", patientId);
            PatientDTO patient = patientServiceFeignClient.getPatient(patientId);
            log.debug("Successfully retrieved patient with ID: {}", patientId);
            return patient;
        } catch (FeignException.NotFound e) {
            log.warn("Patient not found with ID: {}", patientId);
            throw new PatientNotFoundException("Paciente no encontrado con ID: " + patientId);
        } catch (FeignException e) {
            log.error("Error calling Patient Service for patient ID {}: {}", patientId, e.getMessage());
            throw new ServiceUnavailableException("Patient Service no está disponible");
        }
    }
    
    @Override
    @CircuitBreaker(name = "patientService", fallbackMethod = "validatePatientExistsFallback")
    @Retry(name = "patientService")
    public void validatePatientExists(String patientId) {
        try {
            log.debug("Validating patient exists with ID: {}", patientId);
            patientServiceFeignClient.getPatient(patientId);
            log.debug("Patient validation successful for ID: {}", patientId);
        } catch (FeignException.NotFound e) {
            log.warn("Patient validation failed - not found with ID: {}", patientId);
            throw new PatientNotFoundException("Paciente no encontrado con ID: " + patientId);
        } catch (FeignException e) {
            log.error("Error validating patient with ID {}: {}", patientId, e.getMessage());
            throw new ServiceUnavailableException("Patient Service no está disponible");
        }
    }
    
    // Fallback method for getPatient
    private Object getPatientFallback(String patientId, Exception e) {
        log.error("Circuit breaker activated for getPatient with patient ID: {}. Error: {}", 
                  patientId, e.getMessage());
        throw new ServiceUnavailableException(
            "Patient Service no está disponible en este momento. Por favor, intente más tarde.");
    }
    
    // Fallback method for validatePatientExists
    private void validatePatientExistsFallback(String patientId, Exception e) {
        log.error("Circuit breaker activated for validatePatientExists with patient ID: {}. Error: {}", 
                  patientId, e.getMessage());
        throw new ServiceUnavailableException(
            "Patient Service no está disponible en este momento. Por favor, intente más tarde.");
    }
}
