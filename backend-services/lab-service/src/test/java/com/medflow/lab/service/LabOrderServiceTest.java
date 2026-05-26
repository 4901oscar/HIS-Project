package com.medflow.lab.service;

import com.medflow.lab.dto.request.LabOrderNotificationRequest;
import com.medflow.lab.dto.response.LabOrderResponse;
import com.medflow.lab.exception.InvalidOrderStatusException;
import com.medflow.lab.exception.LabOrderNotFoundException;
import com.medflow.lab.model.LabOrder;
import com.medflow.lab.model.OrderStatus;
import com.medflow.lab.model.Sample;
import com.medflow.lab.repository.LabOrderRepository;
import com.medflow.lab.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LabOrderServiceTest {

    @Mock
    private LabOrderRepository labOrderRepository;

    @Mock
    private SampleRepository sampleRepository;

    @InjectMocks
    private LabOrderService labOrderService;

    private LabOrder testOrder;
    private LabOrderNotificationRequest testRequest;

    @BeforeEach
    void setUp() {
        testOrder = LabOrder.builder()
                .id("order-123")
                .orderCode("AB12CD34")
                .patientId("patient-1")
                .doctorId("doctor-1")
                .testNames(List.of("Hemograma", "Glucosa"))
                .status(OrderStatus.PENDING)
                .orderedAt(LocalDateTime.now())
                .build();

        testRequest = LabOrderNotificationRequest.builder()
                .orderCode("AB12CD34")
                .patientId("patient-1")
                .doctorId("doctor-1")
                .testNames(List.of("Hemograma", "Glucosa"))
                .build();
    }

    @Test
    void receiveOrder_ShouldCreateOrderWithPendingStatus() {
        // Arrange
        when(labOrderRepository.save(any(LabOrder.class))).thenReturn(testOrder);

        // Act
        LabOrderResponse response = labOrderService.receiveOrder(testRequest, null);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getOrderCode()).isEqualTo("AB12CD34");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(labOrderRepository, times(1)).save(any(LabOrder.class));
    }

    @Test
    void getOrders_WithStatusFilter_ShouldReturnFilteredOrders() {
        // Arrange
        when(labOrderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(List.of(testOrder));

        // Act
        List<LabOrderResponse> orders = labOrderService.getOrders(OrderStatus.PENDING);

        // Assert
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(labOrderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }

    @Test
    void getOrders_WithoutStatusFilter_ShouldReturnAllOrders() {
        // Arrange
        when(labOrderRepository.findAll()).thenReturn(List.of(testOrder));

        // Act
        List<LabOrderResponse> orders = labOrderService.getOrders(null);

        // Assert
        assertThat(orders).hasSize(1);
        verify(labOrderRepository, times(1)).findAll();
    }

    @Test
    void getOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act
        LabOrderResponse response = labOrderService.getOrderById("order-123");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("order-123");
    }

    @Test
    void getOrderById_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(labOrderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> labOrderService.getOrderById("invalid-id"))
                .isInstanceOf(LabOrderNotFoundException.class)
                .hasMessageContaining("Orden de laboratorio no encontrada");
    }

    @Test
    void collectSample_WithPendingStatus_ShouldTransitionToInProgress() {
        // Arrange
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));
        
        LabOrder updatedOrder = LabOrder.builder()
                .id("order-123")
                .orderCode("AB12CD34")
                .patientId("patient-1")
                .doctorId("doctor-1")
                .testNames(List.of("Hemograma", "Glucosa"))
                .status(OrderStatus.IN_PROGRESS)
                .orderedAt(testOrder.getOrderedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(labOrderRepository.save(any(LabOrder.class))).thenReturn(updatedOrder);
        
        Sample sample = Sample.builder()
                .id("sample-123")
                .orderId("order-123")
                .collectedBy("tech-1")
                .collectedAt(LocalDateTime.now())
                .build();
        
        when(sampleRepository.save(any(Sample.class))).thenReturn(sample);

        // Act
        LabOrderResponse response = labOrderService.collectSample("order-123", "tech-1");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(response.getSampleId()).isEqualTo("sample-123");
        verify(labOrderRepository, times(1)).save(any(LabOrder.class));
        verify(sampleRepository, times(1)).save(any(Sample.class));
    }

    @Test
    void collectSample_WithInProgressStatus_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.IN_PROGRESS);
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labOrderService.collectSample("order-123", "tech-1"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Solo las órdenes con estado PENDING pueden pasar a IN_PROGRESS");
    }

    @Test
    void collectSample_WithCompletedStatus_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labOrderService.collectSample("order-123", "tech-1"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Solo las órdenes con estado PENDING pueden pasar a IN_PROGRESS");
    }

    @Test
    void collectSample_WithCancelledStatus_ShouldThrowException() {
        // Arrange
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(labOrderRepository.findById("order-123")).thenReturn(Optional.of(testOrder));

        // Act & Assert
        assertThatThrownBy(() -> labOrderService.collectSample("order-123", "tech-1"))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("Solo las órdenes con estado PENDING pueden pasar a IN_PROGRESS");
    }

    @Test
    void collectSample_WhenOrderNotFound_ShouldThrowException() {
        // Arrange
        when(labOrderRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> labOrderService.collectSample("invalid-id", "tech-1"))
                .isInstanceOf(LabOrderNotFoundException.class)
                .hasMessageContaining("Orden de laboratorio no encontrada");
    }
}
