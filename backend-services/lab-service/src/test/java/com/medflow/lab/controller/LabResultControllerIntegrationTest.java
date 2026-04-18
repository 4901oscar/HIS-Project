package com.medflow.lab.controller;

import com.medflow.lab.model.LabOrder;
import com.medflow.lab.model.LabResult;
import com.medflow.lab.model.OrderStatus;
import com.medflow.lab.repository.LabOrderRepository;
import com.medflow.lab.repository.LabResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LabResultControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabOrderRepository labOrderRepository;

    @Autowired
    private LabResultRepository labResultRepository;

    @BeforeEach
    void setUp() {
        labResultRepository.deleteAll();
        labOrderRepository.deleteAll();
    }

    @Test
    void getPatientResults_AsDoctor_ShouldReturnResults() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("patient-1");
        createTestResult(order.getId(), "patient-1", LocalDateTime.now().minusDays(1));
        createTestResult(order.getId(), "patient-1", LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(get("/api/lab/results/patient-1")
                        .header("X-User-Id", "doctor-1")
                        .header("X-User-Role", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].patientId").value("patient-1"));
    }

    @Test
    void getPatientResults_AsPatientOwnResults_ShouldReturnResults() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("patient-1");
        createTestResult(order.getId(), "patient-1", LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(get("/api/lab/results/patient-1")
                        .header("X-User-Id", "patient-1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value("patient-1"));
    }

    @Test
    void getPatientResults_AsPatientOtherResults_ShouldReturn403() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("patient-2");
        createTestResult(order.getId(), "patient-2", LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(get("/api/lab/results/patient-2")
                        .header("X-User-Id", "patient-1")
                        .header("X-User-Role", "PATIENT"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acceso Denegado"))
                .andExpect(jsonPath("$.message").value("Los pacientes solo pueden ver sus propios resultados"));
    }

    @Test
    void getPatientResults_ShouldReturnInDescendingOrder() throws Exception {
        // Arrange
        LabOrder order = createTestOrder("patient-1");
        LabResult result1 = createTestResult(order.getId(), "patient-1", LocalDateTime.now().minusDays(2));
        LabResult result2 = createTestResult(order.getId(), "patient-1", LocalDateTime.now().minusDays(1));
        LabResult result3 = createTestResult(order.getId(), "patient-1", LocalDateTime.now());

        // Act & Assert
        mockMvc.perform(get("/api/lab/results/patient-1")
                        .header("X-User-Id", "doctor-1")
                        .header("X-User-Role", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(result3.getId()))
                .andExpect(jsonPath("$[1].id").value(result2.getId()))
                .andExpect(jsonPath("$[2].id").value(result1.getId()));
    }

    @Test
    void getPatientResults_WithNoResults_ShouldReturnEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/lab/results/patient-1")
                        .header("X-User-Id", "doctor-1")
                        .header("X-User-Role", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private LabOrder createTestOrder(String patientId) {
        LabOrder order = LabOrder.builder()
                .orderCode("AB12CD34")
                .patientId(patientId)
                .doctorId("doctor-1")
                .testNames(List.of("Hemograma"))
                .status(OrderStatus.COMPLETED)
                .orderedAt(LocalDateTime.now())
                .build();
        return labOrderRepository.save(order);
    }

    private LabResult createTestResult(String orderId, String patientId, LocalDateTime uploadedAt) {
        LabResult result = LabResult.builder()
                .orderId(orderId)
                .patientId(patientId)
                .resultFilePath("/path/to/result.pdf")
                .uploadedAt(uploadedAt)
                .uploadedBy("tech-1")
                .build();
        return labResultRepository.save(result);
    }
}
