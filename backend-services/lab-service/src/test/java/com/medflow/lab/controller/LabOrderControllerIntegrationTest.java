package com.medflow.lab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medflow.lab.dto.request.LabOrderNotificationRequest;
import com.medflow.lab.model.LabOrder;
import com.medflow.lab.model.OrderStatus;
import com.medflow.lab.repository.LabOrderRepository;
import com.medflow.lab.repository.LabResultRepository;
import com.medflow.lab.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LabOrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private SampleRepository sampleRepository;

    @Autowired
    private LabResultRepository labResultRepository;

    @BeforeEach
    void setUp() {
        labResultRepository.deleteAll();
        sampleRepository.deleteAll();
        labOrderRepository.deleteAll();
    }

    @Test
    void notifyOrder_WithValidData_ShouldCreateOrder() throws Exception {
        // Arrange
        LabOrderNotificationRequest request = LabOrderNotificationRequest.builder()
                .orderCode("AB12CD34")
                .patientId("patient-1")
                .doctorId("doctor-1")
                .testNames(List.of("Hemograma", "Glucosa"))
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/lab/orders/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderCode").value("AB12CD34"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.patientId").value("patient-1"))
                .andExpect(jsonPath("$.testNames[0]").value("Hemograma"));

        // Verify database
        List<LabOrder> orders = labOrderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getOrderCode()).isEqualTo("AB12CD34");
    }

    @Test
    void notifyOrder_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        LabOrderNotificationRequest request = LabOrderNotificationRequest.builder()
                .orderCode("") // Invalid: empty
                .patientId("patient-1")
                .doctorId("doctor-1")
                .testNames(List.of())
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/lab/orders/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error de Validación"));
    }

    @Test
    void getOrders_WithoutFilter_ShouldReturnAllOrders() throws Exception {
        // Arrange
        createTestOrder("AB12CD34", OrderStatus.PENDING);
        createTestOrder("XY98ZW76", OrderStatus.IN_PROGRESS);

        // Act & Assert
        mockMvc.perform(get("/api/lab/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOrders_WithStatusFilter_ShouldReturnFilteredOrders() throws Exception {
        // Arrange
        createTestOrder("AB12CD34", OrderStatus.PENDING);
        createTestOrder("XY98ZW76", OrderStatus.IN_PROGRESS);

        // Act & Assert
        mockMvc.perform(get("/api/lab/orders")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void getOrderById_WhenExists_ShouldReturnOrder() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("AB12CD34", OrderStatus.PENDING);

        // Act & Assert
        mockMvc.perform(get("/api/lab/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()))
                .andExpect(jsonPath("$.orderCode").value("AB12CD34"));
    }

    @Test
    void getOrderById_WhenNotFound_ShouldReturn404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/lab/orders/invalid-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No Encontrado"))
                .andExpect(jsonPath("$.message").value("Orden de laboratorio no encontrada con ID: invalid-id"));
    }

    @Test
    void collectSample_WithPendingOrder_ShouldTransitionToInProgress() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("AB12CD34", OrderStatus.PENDING);

        // Act & Assert
        mockMvc.perform(put("/api/lab/orders/" + order.getId() + "/collect")
                        .header("X-User-Id", "tech-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.sampleId").exists());

        // Verify database
        LabOrder updatedOrder = labOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.IN_PROGRESS);
        assertThat(sampleRepository.findByOrderId(order.getId())).hasSize(1);
    }

    @Test
    void collectSample_WithInProgressOrder_ShouldReturn409() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("AB12CD34", OrderStatus.IN_PROGRESS);

        // Act & Assert
        mockMvc.perform(put("/api/lab/orders/" + order.getId() + "/collect")
                        .header("X-User-Id", "tech-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflicto de Estado"))
                .andExpect(jsonPath("$.message").value(
                        "Solo las órdenes con estado PENDING pueden pasar a IN_PROGRESS. Estado actual: IN_PROGRESS"));
    }

    @Test
    void collectSample_WithCompletedOrder_ShouldReturn409() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("AB12CD34", OrderStatus.COMPLETED);

        // Act & Assert
        mockMvc.perform(put("/api/lab/orders/" + order.getId() + "/collect")
                        .header("X-User-Id", "tech-1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflicto de Estado"));
    }

    @Test
    void uploadResult_WithInProgressOrder_ShouldCompleteOrder() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("AB12CD34", OrderStatus.IN_PROGRESS);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "result.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/lab/orders/" + order.getId() + "/result")
                        .file(file)
                        .header("X-User-Id", "tech-1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Verify database
        LabOrder updatedOrder = labOrderRepository.findById(order.getId()).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(labResultRepository.findByPatientIdOrderByUploadedAtDesc(order.getPatientId())).hasSize(1);
    }

    @Test
    void uploadResult_WithPendingOrder_ShouldReturn409() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("AB12CD34", OrderStatus.PENDING);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "result.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/lab/orders/" + order.getId() + "/result")
                        .file(file)
                        .header("X-User-Id", "tech-1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflicto de Estado"))
                .andExpect(jsonPath("$.message").value(
                        "Solo las órdenes con estado IN_PROGRESS pueden pasar a COMPLETED. Estado actual: PENDING"));
    }

    private LabOrder createTestOrder(String orderCode, OrderStatus status) {
        LabOrder order = LabOrder.builder()
                .orderCode(orderCode)
                .patientId("patient-1")
                .doctorId("doctor-1")
                .testNames(List.of("Hemograma", "Glucosa"))
                .status(status)
                .orderedAt(LocalDateTime.now())
                .build();
        return labOrderRepository.save(order);
    }
}
