package com.medframe.clinical.infrastructure.client;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.infrastructure.client.dto.LabOrderNotificationDTO;
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
@DisplayName("LabServiceClientAdapter Tests")
class LabServiceClientAdapterTest {

    @Mock
    private LabServiceFeignClient feignClient;

    @InjectMocks
    private LabServiceClientAdapter adapter;

    @Captor
    private ArgumentCaptor<LabOrderNotificationDTO> dtoCaptor;

    private LabOrder labOrder;

    @BeforeEach
    void setUp() {
        labOrder = new LabOrder();
        labOrder.setId("laborder-123");
        labOrder.setOrderCode("XYZ98765");
        labOrder.setPatientId("patient-456");
        labOrder.setDoctorId("doctor-789");
        labOrder.setTestNames(List.of("Hemograma completo", "Glucosa en sangre"));
        labOrder.setOrderedAt(LocalDateTime.of(2026, 4, 16, 11, 45));
    }

    @Test
    @DisplayName("Should successfully notify Lab Service with correct DTO mapping")
    void shouldNotifyLabServiceSuccessfully() {
        // When
        adapter.notifyNewLabOrder(labOrder);

        // Give async time to complete (in real scenario, this would be handled differently)
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(feignClient, timeout(1000)).notifyNewLabOrder(dtoCaptor.capture());
        
        LabOrderNotificationDTO capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getLabOrderId()).isEqualTo("laborder-123");
        assertThat(capturedDto.getOrderCode()).isEqualTo("XYZ98765");
        assertThat(capturedDto.getPatientId()).isEqualTo("patient-456");
        assertThat(capturedDto.getDoctorId()).isEqualTo("doctor-789");
        assertThat(capturedDto.getTestNames()).hasSize(2);
        assertThat(capturedDto.getTestNames()).containsExactly("Hemograma completo", "Glucosa en sangre");
        assertThat(capturedDto.getOrderedAt()).isEqualTo(LocalDateTime.of(2026, 4, 16, 11, 45));
    }

    @Test
    @DisplayName("Should swallow exception when Lab Service is unavailable (eventual consistency)")
    void shouldSwallowExceptionWhenLabServiceUnavailable() {
        // Given
        doThrow(new RuntimeException("Lab Service unavailable"))
            .when(feignClient).notifyNewLabOrder(any());

        // When - should not throw exception
        adapter.notifyNewLabOrder(labOrder);

        // Give async time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - verify the call was attempted
        verify(feignClient, timeout(1000)).notifyNewLabOrder(any());
    }

    @Test
    @DisplayName("Should swallow exception when Feign client throws exception")
    void shouldSwallowFeignException() {
        // Given
        doThrow(new IllegalStateException("Connection timeout"))
            .when(feignClient).notifyNewLabOrder(any());

        // When - should not throw exception
        adapter.notifyNewLabOrder(labOrder);

        // Give async time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then - verify the call was attempted
        verify(feignClient, timeout(1000)).notifyNewLabOrder(any());
    }

    @Test
    @DisplayName("Should handle lab order with single test")
    void shouldHandleSingleTest() {
        // Given
        labOrder.setTestNames(List.of("Radiografía de tórax"));

        // When
        adapter.notifyNewLabOrder(labOrder);

        // Give async time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(feignClient, timeout(1000)).notifyNewLabOrder(dtoCaptor.capture());
        
        LabOrderNotificationDTO capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getTestNames()).hasSize(1);
        assertThat(capturedDto.getTestNames().get(0)).isEqualTo("Radiografía de tórax");
    }

    @Test
    @DisplayName("Should handle lab order with multiple tests")
    void shouldHandleMultipleTests() {
        // Given
        labOrder.setTestNames(List.of(
            "Hemograma completo",
            "Glucosa en sangre",
            "Perfil lipídico",
            "Función renal"
        ));

        // When
        adapter.notifyNewLabOrder(labOrder);

        // Give async time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        verify(feignClient, timeout(1000)).notifyNewLabOrder(dtoCaptor.capture());
        
        LabOrderNotificationDTO capturedDto = dtoCaptor.getValue();
        assertThat(capturedDto.getTestNames()).hasSize(4);
        assertThat(capturedDto.getTestNames()).containsExactly(
            "Hemograma completo",
            "Glucosa en sangre",
            "Perfil lipídico",
            "Función renal"
        );
    }
}
