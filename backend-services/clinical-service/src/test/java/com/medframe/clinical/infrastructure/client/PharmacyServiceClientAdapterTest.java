package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.infrastructure.client.dto.PrescriptionNotificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PharmacyServiceClientAdapter Tests")
class PharmacyServiceClientAdapterTest {

    @Mock
    private PharmacyServiceFeignClient feignClient;

    @InjectMocks
    private PharmacyServiceClientAdapter adapter;

    @Captor
    private ArgumentCaptor<PrescriptionNotificationDTO> dtoCaptor;

    private Prescription prescription;

    @BeforeEach
    void setUp() {
        prescription = new Prescription();
        prescription.setId("prescription-123");
        prescription.setPrescriptionCode("ABC12345");
        prescription.setPatientId("patient-456");
        prescription.setDoctorId("doctor-789");
        prescription.setIssuedAt(LocalDateTime.of(2026, 4, 16, 10, 30));
        
        Prescription.Medication medication = new Prescription.Medication(
            "Paracetamol",
            "500mg",
            "Cada 8 horas",
            7,
            "Oral",
            "Tomar con alimentos"
        );
        prescription.setMedications(List.of(medication));
    }

    @Test
    @DisplayName("Should successfully notify Pharmacy Service with correct DTO mapping")
    void shouldNotifyPharmacyServiceSuccessfully() {
        // When
        adapter.notifyNewPrescription(prescription);

        // Give async time to complete (in real scenario, this would be handled differently)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(feignClient, timeout(1000)).notifyNewPrescription(dtoCaptor.capture());
        
        PrescriptionNotificationDTO capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getPrescriptionId()).isEqualTo("prescription-123");
        assertThat(capturedDto.getPrescriptionCode()).isEqualTo("ABC12345");
        assertThat(capturedDto.getPatientId()).isEqualTo("patient-456");
        assertThat(capturedDto.getDoctorId()).isEqualTo("doctor-789");
        assertThat(capturedDto.getMedications()).hasSize(1);
        
        PrescriptionNotificationDTO.MedicationDTO medicationDto = capturedDto.getMedications().get(0);
        assertThat(medicationDto.getName()).isEqualTo("Paracetamol");
        assertThat(medicationDto.getDosage()).isEqualTo("500mg");
        assertThat(medicationDto.getFrequency()).isEqualTo("Cada 8 horas");
        assertThat(medicationDto.getDurationDays()).isEqualTo(7);
        assertThat(medicationDto.getRoute()).isEqualTo("Oral");
        assertThat(medicationDto.getSpecialInstructions()).isEqualTo("Tomar con alimentos");
    }

    @Test
    @DisplayName("Should swallow exception when Pharmacy Service is unavailable (eventual consistency)")
    void shouldSwallowExceptionWhenPharmacyServiceUnavailable() {
        // Given
        doThrow(new RuntimeException("Pharmacy Service unavailable"))
            .when(feignClient).notifyNewPrescription(any());

        // When - should not throw exception
        adapter.notifyNewPrescription(prescription);

        // Give async time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - verify the call was attempted
        verify(feignClient, timeout(1000)).notifyNewPrescription(any());
    }

    @Test
    @DisplayName("Should swallow exception when Feign client throws exception")
    void shouldSwallowFeignException() {
        // Given
        doThrow(new IllegalStateException("Connection timeout"))
            .when(feignClient).notifyNewPrescription(any());

        // When - should not throw exception
        adapter.notifyNewPrescription(prescription);

        // Give async time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - verify the call was attempted
        verify(feignClient, timeout(1000)).notifyNewPrescription(any());
    }

    @Test
    @DisplayName("Should handle prescription with multiple medications")
    void shouldHandleMultipleMedications() {
        // Given
        Prescription.Medication med1 = new Prescription.Medication(
            "Paracetamol", "500mg", "Cada 8 horas", 7, "Oral", "Con alimentos"
        );
        Prescription.Medication med2 = new Prescription.Medication(
            "Ibuprofeno", "400mg", "Cada 12 horas", 5, "Oral", "Después de comer"
        );
        prescription.setMedications(List.of(med1, med2));

        // When
        adapter.notifyNewPrescription(prescription);

        // Give async time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(feignClient, timeout(1000)).notifyNewPrescription(dtoCaptor.capture());
        
        PrescriptionNotificationDTO capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getMedications()).hasSize(2);
        assertThat(capturedDto.getMedications().get(0).getName()).isEqualTo("Paracetamol");
        assertThat(capturedDto.getMedications().get(1).getName()).isEqualTo("Ibuprofeno");
    }
}
