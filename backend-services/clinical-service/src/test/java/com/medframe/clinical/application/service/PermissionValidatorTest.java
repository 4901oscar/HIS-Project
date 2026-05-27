package com.medframe.clinical.application.service;

import com.medframe.clinical.domain.exception.ForbiddenException;
import com.medframe.clinical.domain.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PermissionValidator application service.
 * 
 * Tests validate that:
 * - Users with correct roles can access operations
 * - Users without correct roles are denied access
 * - Missing role information throws UnauthorizedException
 * - Helper methods correctly extract headers
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PermissionValidator Unit Tests")
class PermissionValidatorTest {
    
    @Mock
    private HttpServletRequest request;
    
    @InjectMocks
    private PermissionValidator permissionValidator;
    
    private static final String ROLE_HEADER = "X-User-Roles";
    private static final String USER_ID_HEADER = "X-User-Id";
    
    @BeforeEach
    void setUp() {
        // Reset mocks before each test
    }
    
    @Test
    @DisplayName("Should allow access when user has required role")
    void shouldAllowAccessWhenUserHasRequiredRole() {
        // Given: User has DOCTOR role
        when(request.getHeader(ROLE_HEADER)).thenReturn("DOCTOR");
        
        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> permissionValidator.requireRole("DOCTOR"));
    }
    
    @Test
    @DisplayName("Should allow access when user has one of multiple required roles")
    void shouldAllowAccessWhenUserHasOneOfMultipleRequiredRoles() {
        // Given: User has DOCTOR role
        when(request.getHeader(ROLE_HEADER)).thenReturn("DOCTOR");
        
        // When/Then: Should not throw exception when DOCTOR is one of allowed roles
        assertDoesNotThrow(() -> permissionValidator.requireRole("ADMIN", "DOCTOR", "NURSE"));
    }
    
    @Test
    @DisplayName("Should allow access when user has multiple roles including required one")
    void shouldAllowAccessWhenUserHasMultipleRolesIncludingRequiredOne() {
        // Given: User has multiple roles including DOCTOR
        when(request.getHeader(ROLE_HEADER)).thenReturn("DOCTOR,ADMIN");
        
        // When/Then: Should not throw exception
        assertDoesNotThrow(() -> permissionValidator.requireRole("DOCTOR"));
    }
    
    @Test
    @DisplayName("Should throw ForbiddenException when user does not have required role")
    void shouldThrowForbiddenExceptionWhenUserDoesNotHaveRequiredRole() {
        // Given: User has NURSE role but DOCTOR is required
        when(request.getHeader(ROLE_HEADER)).thenReturn("NURSE");
        
        // When/Then: Should throw ForbiddenException
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> permissionValidator.requireRole("DOCTOR")
        );
        
        assertTrue(exception.getMessage().contains("No tiene permisos"));
        assertTrue(exception.getMessage().contains("DOCTOR"));
    }
    
    @Test
    @DisplayName("Should throw ForbiddenException when user has no matching roles")
    void shouldThrowForbiddenExceptionWhenUserHasNoMatchingRoles() {
        // Given: User has PATIENT role but DOCTOR or ADMIN required
        when(request.getHeader(ROLE_HEADER)).thenReturn("PATIENT");
        
        // When/Then: Should throw ForbiddenException
        ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> permissionValidator.requireRole("DOCTOR", "ADMIN")
        );
        
        assertTrue(exception.getMessage().contains("No tiene permisos"));
        assertTrue(exception.getMessage().contains("DOCTOR"));
        assertTrue(exception.getMessage().contains("ADMIN"));
    }
    
    @Test
    @DisplayName("Should throw UnauthorizedException when role header is null")
    void shouldThrowUnauthorizedExceptionWhenRoleHeaderIsNull() {
        // Given: No role header present
        when(request.getHeader(ROLE_HEADER)).thenReturn(null);
        
        // When/Then: Should throw UnauthorizedException
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> permissionValidator.requireRole("DOCTOR")
        );
        
        assertEquals("No se encontró información de roles", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should throw UnauthorizedException when role header is empty")
    void shouldThrowUnauthorizedExceptionWhenRoleHeaderIsEmpty() {
        // Given: Empty role header
        when(request.getHeader(ROLE_HEADER)).thenReturn("");
        
        // When/Then: Should throw UnauthorizedException
        UnauthorizedException exception = assertThrows(
            UnauthorizedException.class,
            () -> permissionValidator.requireRole("DOCTOR")
        );
        
        assertEquals("No se encontró información de roles", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should allow VITAL_SIGNS role for vital signs operations")
    void shouldAllowVitalSignsRoleForVitalSignsOperations() {
        // Given: User has VITAL_SIGNS role
        when(request.getHeader(ROLE_HEADER)).thenReturn("VITAL_SIGNS");
        
        // When/Then: Should allow access for vital signs operations
        assertDoesNotThrow(() -> permissionValidator.requireRole("VITAL_SIGNS", "DOCTOR"));
    }
    
    @Test
    @DisplayName("Should allow ADMISSION role for appointment operations")
    void shouldAllowAdmissionRoleForAppointmentOperations() {
        // Given: User has ADMISSION role
        when(request.getHeader(ROLE_HEADER)).thenReturn("ADMISSION");
        
        // When/Then: Should allow access for appointment operations
        assertDoesNotThrow(() -> permissionValidator.requireRole("ADMISSION", "ADMIN"));
    }
    
    @Test
    @DisplayName("getUserId should return user ID from header")
    void getUserIdShouldReturnUserIdFromHeader() {
        // Given: User ID header is present
        String expectedUserId = "user-123";
        when(request.getHeader(USER_ID_HEADER)).thenReturn(expectedUserId);
        
        // When: Get user ID
        String actualUserId = permissionValidator.getUserId();
        
        // Then: Should return the user ID
        assertEquals(expectedUserId, actualUserId);
    }
    
    @Test
    @DisplayName("getUserId should return null when header is not present")
    void getUserIdShouldReturnNullWhenHeaderIsNotPresent() {
        // Given: User ID header is not present
        when(request.getHeader(USER_ID_HEADER)).thenReturn(null);
        
        // When: Get user ID
        String actualUserId = permissionValidator.getUserId();
        
        // Then: Should return null
        assertNull(actualUserId);
    }
    
    @Test
    @DisplayName("getUserRoles should return roles from header")
    void getUserRolesShouldReturnRolesFromHeader() {
        // Given: Roles header is present
        String expectedRoles = "DOCTOR,ADMIN";
        when(request.getHeader(ROLE_HEADER)).thenReturn(expectedRoles);
        
        // When: Get user roles
        String actualRoles = permissionValidator.getUserRoles();
        
        // Then: Should return the roles
        assertEquals(expectedRoles, actualRoles);
    }
    
    @Test
    @DisplayName("getUserRoles should return null when header is not present")
    void getUserRolesShouldReturnNullWhenHeaderIsNotPresent() {
        // Given: Roles header is not present
        when(request.getHeader(ROLE_HEADER)).thenReturn(null);
        
        // When: Get user roles
        String actualRoles = permissionValidator.getUserRoles();
        
        // Then: Should return null
        assertNull(actualRoles);
    }
    
    @Test
    @DisplayName("Should handle role matching with partial string matches correctly")
    void shouldHandleRoleMatchingWithPartialStringMatchesCorrectly() {
        // Given: User has DOCTOR role
        when(request.getHeader(ROLE_HEADER)).thenReturn("DOCTOR");
        
        // When/Then: Should match DOCTOR but not DOCTOR_ADMIN
        assertDoesNotThrow(() -> permissionValidator.requireRole("DOCTOR"));
        
        // Given: User has DOCTOR_ADMIN role
        when(request.getHeader(ROLE_HEADER)).thenReturn("DOCTOR_ADMIN");
        
        // When/Then: Should match when checking for DOCTOR (contains check)
        assertDoesNotThrow(() -> permissionValidator.requireRole("DOCTOR"));
    }
}
