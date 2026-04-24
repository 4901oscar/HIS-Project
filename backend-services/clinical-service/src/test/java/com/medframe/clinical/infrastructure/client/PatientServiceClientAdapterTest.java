package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.exception.PatientNotFoundException;
import com.medframe.clinical.domain.exception.ServiceUnavailableException;
import com.medframe.clinical.infrastructure.client.dto.PatientDTO;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceClientAdapterTest {

    @Mock
    private PatientServiceFeignClient patientServiceFeignClient;

    @InjectMocks
    private PatientServiceClientAdapter patientServiceClientAdapter;

    private PatientDTO samplePatient;

    @BeforeEach
    void setUp() {
        samplePatient = new PatientDTO(
            "patient-123",                      // id
            "1234567890123",                    // dpi
            "12345678",                         // nit
            "Juan",                             // firstName
            "Carlos",                           // secondName
            "Pérez",                            // firstLastName
            "García",                           // secondLastName
            LocalDate.of(1990, 1, 15),         // birthDate
            "M",                                // gender
            "juan.perez@example.com",          // email
            "555-1234",                         // phone
            "Guatemala",                        // department
            "Guatemala",                        // municipality
            "1",                                // zone
            "Calle Principal 123",              // address
            "auth-user-123",                    // authUserId
            true                                // active
        );
    }

    @Test
    void getPatient_WhenPatientExists_ShouldReturnPatientDTO() {
        // Given
        when(patientServiceFeignClient.getPatient("patient-123")).thenReturn(samplePatient);

        // When
        Object result = patientServiceClientAdapter.getPatient("patient-123");

        // Then
        assertNotNull(result);
        assertTrue(result instanceof PatientDTO);
        PatientDTO patient = (PatientDTO) result;
        assertEquals("patient-123", patient.getId());
        assertEquals("Juan", patient.getFirstName());
        assertEquals("Pérez", patient.getFirstLastName());
        verify(patientServiceFeignClient, times(1)).getPatient("patient-123");
    }

    @Test
    void getPatient_WhenPatientNotFound_ShouldThrowPatientNotFoundException() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "/api/patients/unknown", 
                                        new HashMap<>(), null, new RequestTemplate());
        when(patientServiceFeignClient.getPatient("unknown"))
            .thenThrow(new FeignException.NotFound("Not found", request, null, null));

        // When & Then
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> patientServiceClientAdapter.getPatient("unknown")
        );
        
        assertTrue(exception.getMessage().contains("Paciente no encontrado"));
        verify(patientServiceFeignClient, times(1)).getPatient("unknown");
    }

    @Test
    void getPatient_WhenServiceUnavailable_ShouldThrowServiceUnavailableException() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "/api/patients/patient-123", 
                                        new HashMap<>(), null, new RequestTemplate());
        when(patientServiceFeignClient.getPatient("patient-123"))
            .thenThrow(new FeignException.ServiceUnavailable("Service unavailable", request, null, null));

        // When & Then
        ServiceUnavailableException exception = assertThrows(
            ServiceUnavailableException.class,
            () -> patientServiceClientAdapter.getPatient("patient-123")
        );
        
        assertTrue(exception.getMessage().contains("Patient Service no está disponible"));
        verify(patientServiceFeignClient, times(1)).getPatient("patient-123");
    }

    @Test
    void validatePatientExists_WhenPatientExists_ShouldNotThrowException() {
        // Given
        when(patientServiceFeignClient.getPatient("patient-123")).thenReturn(samplePatient);

        // When & Then
        assertDoesNotThrow(() -> patientServiceClientAdapter.validatePatientExists("patient-123"));
        verify(patientServiceFeignClient, times(1)).getPatient("patient-123");
    }

    @Test
    void validatePatientExists_WhenPatientNotFound_ShouldThrowPatientNotFoundException() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "/api/patients/unknown", 
                                        new HashMap<>(), null, new RequestTemplate());
        when(patientServiceFeignClient.getPatient("unknown"))
            .thenThrow(new FeignException.NotFound("Not found", request, null, null));

        // When & Then
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> patientServiceClientAdapter.validatePatientExists("unknown")
        );
        
        assertTrue(exception.getMessage().contains("Paciente no encontrado"));
        verify(patientServiceFeignClient, times(1)).getPatient("unknown");
    }

    @Test
    void validatePatientExists_WhenServiceUnavailable_ShouldThrowServiceUnavailableException() {
        // Given
        Request request = Request.create(Request.HttpMethod.GET, "/api/patients/patient-123", 
                                        new HashMap<>(), null, new RequestTemplate());
        when(patientServiceFeignClient.getPatient("patient-123"))
            .thenThrow(new FeignException.InternalServerError("Internal error", request, null, null));

        // When & Then
        ServiceUnavailableException exception = assertThrows(
            ServiceUnavailableException.class,
            () -> patientServiceClientAdapter.validatePatientExists("patient-123")
        );
        
        assertTrue(exception.getMessage().contains("Patient Service no está disponible"));
        verify(patientServiceFeignClient, times(1)).getPatient("patient-123");
    }
}
