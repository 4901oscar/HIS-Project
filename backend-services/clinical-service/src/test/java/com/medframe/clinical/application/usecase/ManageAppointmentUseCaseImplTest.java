package com.medframe.clinical.application.usecase;

import com.medframe.clinical.application.service.PermissionValidator;
import com.medframe.clinical.domain.exception.AppointmentNotFoundException;
import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.exception.SlotNotAvailableException;
import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.port.out.AppointmentRepository;
import com.medframe.clinical.domain.service.AppointmentManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ManageAppointmentUseCaseImpl.
 * 
 * Tests permission validation and delegation to AppointmentManager domain service.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ManageAppointmentUseCase Implementation Tests")
class ManageAppointmentUseCaseImplTest {
    
    @Mock
    private AppointmentManager appointmentManager;
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @Mock
    private PermissionValidator permissionValidator;
    
    @InjectMocks
    private ManageAppointmentUseCaseImpl useCase;
    
    private static final String PATIENT_ID = "patient-123";
    private static final String DOCTOR_ID = "doctor-456";
    private static final String APPOINTMENT_ID = "appointment-789";
    private static final String USER_ID = "user-001";
    private static final LocalDate TEST_DATE = LocalDate.of(2026, 4, 20);
    private static final LocalTime TEST_TIME = LocalTime.of(10, 0);
    
    @BeforeEach
    void setUp() {
        // Default: no exception thrown (user has permission)
        // Use lenient() to avoid strict stubbing issues when specific tests override this
        lenient().doNothing().when(permissionValidator).requireRole(anyString());
    }
    
    // ========== findAvailableSlots Tests ==========
    
    @Test
    @DisplayName("findAvailableSlots - Success with ADMISSION role")
    void findAvailableSlots_WithAdmissionRole_ReturnsSlots() {
        // Arrange
        List<LocalTime> expectedSlots = Arrays.asList(
            LocalTime.of(8, 0),
            LocalTime.of(8, 30),
            LocalTime.of(9, 0)
        );
        when(appointmentManager.findAvailableSlots(DOCTOR_ID, TEST_DATE))
            .thenReturn(expectedSlots);
        
        // Act
        List<LocalTime> result = useCase.findAvailableSlots(DOCTOR_ID, TEST_DATE);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(expectedSlots, result);
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager).findAvailableSlots(DOCTOR_ID, TEST_DATE);
    }
    
    @Test
    @DisplayName("findAvailableSlots - Forbidden without ADMISSION or ADMIN role")
    void findAvailableSlots_WithoutPermission_ThrowsForbiddenException() {
        // Arrange
        doThrow(new ForbiddenException("No tiene permisos"))
            .when(permissionValidator).requireRole("ADMISSION", "ADMIN");
        
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> 
            useCase.findAvailableSlots(DOCTOR_ID, TEST_DATE)
        );
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager, never()).findAvailableSlots(any(), any());
    }
    
    // ========== createAppointment Tests ==========
    
    @Test
    @DisplayName("createAppointment - Success with ADMIN role")
    void createAppointment_WithAdminRole_CreatesAppointment() {
        // Arrange
        Appointment expectedAppointment = new Appointment();
        expectedAppointment.setId(APPOINTMENT_ID);
        expectedAppointment.setPatientId(PATIENT_ID);
        expectedAppointment.setDoctorId(DOCTOR_ID);
        expectedAppointment.setAppointmentDate(TEST_DATE);
        expectedAppointment.setAppointmentTime(TEST_TIME);
        
        when(appointmentManager.createAppointment(
            PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, "Consulta general", USER_ID))
            .thenReturn(expectedAppointment);
        
        // Act
        Appointment result = useCase.createAppointment(
            PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, "Consulta general", USER_ID);
        
        // Assert
        assertNotNull(result);
        assertEquals(APPOINTMENT_ID, result.getId());
        assertEquals(PATIENT_ID, result.getPatientId());
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager).createAppointment(
            PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, "Consulta general", USER_ID);
    }
    
    @Test
    @DisplayName("createAppointment - Forbidden without permission")
    void createAppointment_WithoutPermission_ThrowsForbiddenException() {
        // Arrange
        doThrow(new ForbiddenException("No tiene permisos"))
            .when(permissionValidator).requireRole("ADMISSION", "ADMIN");
        
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> 
            useCase.createAppointment(PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, null, USER_ID)
        );
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager, never()).createAppointment(any(), any(), any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("createAppointment - Slot not available throws exception")
    void createAppointment_SlotNotAvailable_ThrowsSlotNotAvailableException() {
        // Arrange
        when(appointmentManager.createAppointment(
            PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, null, USER_ID))
            .thenThrow(new SlotNotAvailableException("El horario ya no está disponible"));
        
        // Act & Assert
        assertThrows(SlotNotAvailableException.class, () -> 
            useCase.createAppointment(PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, null, USER_ID)
        );
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager).createAppointment(
            PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, null, USER_ID);
    }
    
    // ========== activateAppointment Tests ==========
    
    @Test
    @DisplayName("activateAppointment - Success with ADMISSION role")
    void activateAppointment_WithAdmissionRole_ActivatesAppointment() {
        // Arrange
        doNothing().when(appointmentManager).activateAppointment(APPOINTMENT_ID);
        
        // Act
        useCase.activateAppointment(APPOINTMENT_ID);
        
        // Assert
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager).activateAppointment(APPOINTMENT_ID);
    }
    
    @Test
    @DisplayName("activateAppointment - Forbidden without permission")
    void activateAppointment_WithoutPermission_ThrowsForbiddenException() {
        // Arrange
        doThrow(new ForbiddenException("No tiene permisos"))
            .when(permissionValidator).requireRole("ADMISSION", "ADMIN");
        
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> 
            useCase.activateAppointment(APPOINTMENT_ID)
        );
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager, never()).activateAppointment(any());
    }
    
    @Test
    @DisplayName("activateAppointment - Appointment not found throws exception")
    void activateAppointment_AppointmentNotFound_ThrowsAppointmentNotFoundException() {
        // Arrange
        doThrow(new AppointmentNotFoundException("Cita no encontrada"))
            .when(appointmentManager).activateAppointment(APPOINTMENT_ID);
        
        // Act & Assert
        assertThrows(AppointmentNotFoundException.class, () -> 
            useCase.activateAppointment(APPOINTMENT_ID)
        );
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager).activateAppointment(APPOINTMENT_ID);
    }
    
    // ========== cancelAppointment Tests ==========
    
    @Test
    @DisplayName("cancelAppointment - Success with ADMIN role")
    void cancelAppointment_WithAdminRole_CancelsAppointment() {
        // Arrange
        doNothing().when(appointmentManager).cancelAppointment(APPOINTMENT_ID);
        
        // Act
        useCase.cancelAppointment(APPOINTMENT_ID);
        
        // Assert
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager).cancelAppointment(APPOINTMENT_ID);
    }
    
    @Test
    @DisplayName("cancelAppointment - Forbidden without permission")
    void cancelAppointment_WithoutPermission_ThrowsForbiddenException() {
        // Arrange
        doThrow(new ForbiddenException("No tiene permisos"))
            .when(permissionValidator).requireRole("ADMISSION", "ADMIN");
        
        // Act & Assert
        assertThrows(ForbiddenException.class, () -> 
            useCase.cancelAppointment(APPOINTMENT_ID)
        );
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager, never()).cancelAppointment(any());
    }
    
    @Test
    @DisplayName("cancelAppointment - Appointment not found throws exception")
    void cancelAppointment_AppointmentNotFound_ThrowsAppointmentNotFoundException() {
        // Arrange
        doThrow(new AppointmentNotFoundException("Cita no encontrada"))
            .when(appointmentManager).cancelAppointment(APPOINTMENT_ID);
        
        // Act & Assert
        assertThrows(AppointmentNotFoundException.class, () -> 
            useCase.cancelAppointment(APPOINTMENT_ID)
        );
        verify(permissionValidator).requireRole("ADMISSION", "ADMIN");
        verify(appointmentManager).cancelAppointment(APPOINTMENT_ID);
    }
    
    // ========== Integration Tests ==========
    
    @Test
    @DisplayName("All operations validate ADMISSION or ADMIN role")
    void allOperations_ValidateAdmissionOrAdminRole() {
        // Arrange
        when(appointmentManager.findAvailableSlots(any(), any()))
            .thenReturn(Arrays.asList(TEST_TIME));
        when(appointmentManager.createAppointment(any(), any(), any(), any(), any(), any()))
            .thenReturn(new Appointment());
        doNothing().when(appointmentManager).activateAppointment(any());
        doNothing().when(appointmentManager).cancelAppointment(any());
        
        // Act
        useCase.findAvailableSlots(DOCTOR_ID, TEST_DATE);
        useCase.createAppointment(PATIENT_ID, DOCTOR_ID, TEST_DATE, TEST_TIME, null, USER_ID);
        useCase.activateAppointment(APPOINTMENT_ID);
        useCase.cancelAppointment(APPOINTMENT_ID);
        
        // Assert - all operations should validate the same roles
        verify(permissionValidator, times(4)).requireRole("ADMISSION", "ADMIN");
    }
}
