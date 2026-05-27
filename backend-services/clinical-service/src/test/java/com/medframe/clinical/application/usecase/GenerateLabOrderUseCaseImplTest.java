package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.service.LabOrderGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GenerateLabOrderUseCaseImpl.
 * 
 * Tests verify:
 * - DOCTOR role validation
 * - Delegation to LabOrderGenerator domain service
 * - Proper exception handling for unauthorized access
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GenerateLabOrderUseCaseImpl Tests")
class GenerateLabOrderUseCaseImplTest {

    @Mock
    private LabOrderGenerator labOrderGenerator;

    @Mock
    private PermissionValidator permissionValidator;

    private GenerateLabOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateLabOrderUseCaseImpl(labOrderGenerator, permissionValidator);
    }

    @Test
    @DisplayName("Should generate lab order when user has DOCTOR role")
    void shouldGenerateLabOrderWhenUserHasDoctorRole() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<String> testNames = List.of("Complete Blood Count", "Lipid Panel");

        LabOrder expectedLabOrder = new LabOrder();
        expectedLabOrder.setId("laborder-001");
        expectedLabOrder.setConsultationId(consultationId);
        expectedLabOrder.setPatientId(patientId);
        expectedLabOrder.setDoctorId(doctorId);
        expectedLabOrder.setOrderCode("LAB12345");
        expectedLabOrder.setTestNames(testNames);
        expectedLabOrder.setStatus(LabOrder.LabOrderStatus.PENDING);
        expectedLabOrder.setOrderedAt(LocalDateTime.now());
        expectedLabOrder.setOrderedBy(doctorId);

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(labOrderGenerator.generateLabOrder(consultationId, patientId, doctorId, testNames))
            .thenReturn(expectedLabOrder);

        // When
        LabOrder result = useCase.generateLabOrder(consultationId, patientId, doctorId, testNames);

        // Then
        assertNotNull(result);
        assertEquals("laborder-001", result.getId());
        assertEquals("LAB12345", result.getOrderCode());
        assertEquals(consultationId, result.getConsultationId());
        assertEquals(patientId, result.getPatientId());
        assertEquals(doctorId, result.getDoctorId());
        assertEquals(LabOrder.LabOrderStatus.PENDING, result.getStatus());
        assertEquals(2, result.getTestNames().size());
        assertEquals("Complete Blood Count", result.getTestNames().get(0));
        assertEquals("Lipid Panel", result.getTestNames().get(1));

        verify(permissionValidator).requireRole("DOCTOR");
        verify(labOrderGenerator).generateLabOrder(consultationId, patientId, doctorId, testNames);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when user does not have DOCTOR role")
    void shouldThrowForbiddenExceptionWhenUserDoesNotHaveDoctorRole() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<String> testNames = List.of("Complete Blood Count");

        doThrow(new ForbiddenException("No tiene permisos para realizar esta operación. Roles requeridos: DOCTOR"))
            .when(permissionValidator).requireRole("DOCTOR");

        // When & Then
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
            useCase.generateLabOrder(consultationId, patientId, doctorId, testNames)
        );

        assertEquals("No tiene permisos para realizar esta operación. Roles requeridos: DOCTOR", 
                     exception.getMessage());

        verify(permissionValidator).requireRole("DOCTOR");
        verify(labOrderGenerator, never()).generateLabOrder(anyString(), anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("Should generate lab order with multiple tests")
    void shouldGenerateLabOrderWithMultipleTests() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<String> testNames = List.of(
            "Complete Blood Count",
            "Lipid Panel",
            "Liver Function Test",
            "Kidney Function Test",
            "Thyroid Panel"
        );

        LabOrder expectedLabOrder = new LabOrder();
        expectedLabOrder.setId("laborder-002");
        expectedLabOrder.setOrderCode("XYZ98765");
        expectedLabOrder.setTestNames(testNames);
        expectedLabOrder.setStatus(LabOrder.LabOrderStatus.PENDING);

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(labOrderGenerator.generateLabOrder(consultationId, patientId, doctorId, testNames))
            .thenReturn(expectedLabOrder);

        // When
        LabOrder result = useCase.generateLabOrder(consultationId, patientId, doctorId, testNames);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getTestNames().size());
        assertEquals("Complete Blood Count", result.getTestNames().get(0));
        assertEquals("Lipid Panel", result.getTestNames().get(1));
        assertEquals("Liver Function Test", result.getTestNames().get(2));
        assertEquals("Kidney Function Test", result.getTestNames().get(3));
        assertEquals("Thyroid Panel", result.getTestNames().get(4));

        verify(permissionValidator).requireRole("DOCTOR");
        verify(labOrderGenerator).generateLabOrder(consultationId, patientId, doctorId, testNames);
    }

    @Test
    @DisplayName("Should validate DOCTOR role before delegating to domain service")
    void shouldValidateDoctorRoleBeforeDelegatingToDomainService() {
        // Given
        String consultationId = "consultation-123";
        String patientId = "patient-456";
        String doctorId = "doctor-789";
        List<String> testNames = List.of("Glucose Test");

        LabOrder expectedLabOrder = new LabOrder();
        expectedLabOrder.setId("laborder-003");

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(labOrderGenerator.generateLabOrder(consultationId, patientId, doctorId, testNames))
            .thenReturn(expectedLabOrder);

        // When
        useCase.generateLabOrder(consultationId, patientId, doctorId, testNames);

        // Then - verify order of operations
        var inOrder = inOrder(permissionValidator, labOrderGenerator);
        inOrder.verify(permissionValidator).requireRole("DOCTOR");
        inOrder.verify(labOrderGenerator).generateLabOrder(consultationId, patientId, doctorId, testNames);
    }

    @Test
    @DisplayName("Should generate lab order with single test")
    void shouldGenerateLabOrderWithSingleTest() {
        // Given
        String consultationId = "consultation-456";
        String patientId = "patient-789";
        String doctorId = "doctor-012";
        List<String> testNames = List.of("Hemoglobin A1C");

        LabOrder expectedLabOrder = new LabOrder();
        expectedLabOrder.setId("laborder-004");
        expectedLabOrder.setOrderCode("HBA1C001");
        expectedLabOrder.setTestNames(testNames);
        expectedLabOrder.setStatus(LabOrder.LabOrderStatus.PENDING);

        doNothing().when(permissionValidator).requireRole("DOCTOR");
        when(labOrderGenerator.generateLabOrder(consultationId, patientId, doctorId, testNames))
            .thenReturn(expectedLabOrder);

        // When
        LabOrder result = useCase.generateLabOrder(consultationId, patientId, doctorId, testNames);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTestNames().size());
        assertEquals("Hemoglobin A1C", result.getTestNames().get(0));
        assertEquals(LabOrder.LabOrderStatus.PENDING, result.getStatus());

        verify(permissionValidator).requireRole("DOCTOR");
        verify(labOrderGenerator).generateLabOrder(consultationId, patientId, doctorId, testNames);
    }
}
