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
    public Object getPatient(String patientIdOrAuthUserId) {
        try {
            log.debug("Calling Patient Service to get patient with ID or authUserId: {}", patientIdOrAuthUserId);
            PatientDTO patient;
            // Try to get by auth_user_id first (for appointment creation from frontend)
            try {
                patient = patientServiceFeignClient.getPatientByAuthUserId(patientIdOrAuthUserId);
                log.debug("Successfully retrieved patient by authUserId: {}", patientIdOrAuthUserId);
            } catch (FeignException.NotFound e) {
                // If not found by authUserId, try by patient ID
                patient = patientServiceFeignClient.getPatient(patientIdOrAuthUserId);
                log.debug("Successfully retrieved patient by ID: {}", patientIdOrAuthUserId);
            }
            return patient;
        } catch (FeignException.NotFound e) {
            log.warn("Patient not found with ID or authUserId: {}", patientIdOrAuthUserId);
            throw new PatientNotFoundException("Paciente no encontrado con ID: " + patientIdOrAuthUserId);
        } catch (FeignException e) {
            log.error("Error calling Patient Service for patient ID/authUserId {}: {}", patientIdOrAuthUserId, e.getMessage());
            throw new ServiceUnavailableException("Patient Service no está disponible");
        }
    }
    
    @Override
    @CircuitBreaker(name = "patientService", fallbackMethod = "getPatientByAuthUserIdFallback")
    @Retry(name = "patientService")
    public Object getPatientByAuthUserId(String authUserId) {
        try {
            log.debug("Calling Patient Service to get patient by authUserId: {}", authUserId);
            PatientDTO patient = patientServiceFeignClient.getPatientByAuthUserId(authUserId);
            log.debug("Successfully retrieved patient by authUserId: {}", authUserId);
            return patient;
        } catch (FeignException.NotFound e) {
            log.warn("Patient not found with authUserId: {}", authUserId);
            throw new PatientNotFoundException("Paciente no encontrado con authUserId: " + authUserId);
        } catch (FeignException e) {
            log.error("Error calling Patient Service for authUserId {}: {}", authUserId, e.getMessage());
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
    
    // Fallback method for getPatientByAuthUserId
    private Object getPatientByAuthUserIdFallback(String authUserId, Exception e) {
        log.error("Circuit breaker activated for getPatientByAuthUserId with authUserId: {}. Error: {}", 
                  authUserId, e.getMessage());
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
