package com.medflow.patient.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PatientInfo value object.
 * Tests all validation methods and business logic.
 */
class PatientInfoTest {
    
    @Test
    @DisplayName("Should create valid PatientInfo with all fields")
    void shouldCreateValidPatientInfo() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "12345678",
            "Juan",
            "Carlos",
            "García",
            "López",
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan.garcia@example.com"
        );
        
        assertNotNull(patientInfo);
        assertEquals("1234567890123", patientInfo.getDpi());
        assertEquals("12345678", patientInfo.getNit());
        assertEquals("Juan", patientInfo.getPrimerNombre());
        assertEquals("Carlos", patientInfo.getSegundoNombre());
        assertEquals("García", patientInfo.getPrimerApellido());
        assertEquals("López", patientInfo.getSegundoApellido());
        assertEquals(LocalDate.of(1990, 5, 15), patientInfo.getFechaNacimiento());
        assertEquals("12345678", patientInfo.getTelefono());
        assertEquals("juan.garcia@example.com", patientInfo.getCorreo());
    }
    
    @Test
    @DisplayName("Should validate all fields successfully")
    void shouldValidateAllFieldsSuccessfully() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validate());
    }
    
    // DPI Validation Tests
    
    @Test
    @DisplayName("Should accept valid 13-digit DPI")
    void shouldAcceptValid13DigitDPI() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validateDPI());
    }
    
    @Test
    @DisplayName("Should reject DPI with less than 13 digits")
    void shouldRejectDPIWithLessThan13Digits() {
        PatientInfo patientInfo = new PatientInfo(
            "123456789012",  // 12 digits
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        InvalidDPIException exception = assertThrows(InvalidDPIException.class, 
            () -> patientInfo.validateDPI());
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject DPI with more than 13 digits")
    void shouldRejectDPIWithMoreThan13Digits() {
        PatientInfo patientInfo = new PatientInfo(
            "12345678901234",  // 14 digits
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        InvalidDPIException exception = assertThrows(InvalidDPIException.class, 
            () -> patientInfo.validateDPI());
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject DPI with alphanumeric characters")
    void shouldRejectDPIWithAlphanumericCharacters() {
        PatientInfo patientInfo = new PatientInfo(
            "123456789012A",  // Contains letter
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        InvalidDPIException exception = assertThrows(InvalidDPIException.class, 
            () -> patientInfo.validateDPI());
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject null DPI")
    void shouldRejectNullDPI() {
        PatientInfo patientInfo = new PatientInfo(
            null,
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        InvalidDPIException exception = assertThrows(InvalidDPIException.class, 
            () -> patientInfo.validateDPI());
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    // NIT Validation Tests
    
    @Test
    @DisplayName("Should accept NIT as C/F")
    void shouldAcceptNITAsCF() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validateNIT());
    }
    
    @Test
    @DisplayName("Should accept NIT with 1 digit")
    void shouldAcceptNITWith1Digit() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "1",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validateNIT());
    }
    
    @Test
    @DisplayName("Should accept NIT with 8 digits")
    void shouldAcceptNITWith8Digits() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "12345678",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validateNIT());
    }
    
    @Test
    @DisplayName("Should reject NIT with 9 digits")
    void shouldRejectNITWith9Digits() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "123456789",  // 9 digits
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        InvalidNITException exception = assertThrows(InvalidNITException.class, 
            () -> patientInfo.validateNIT());
        assertEquals("NIT must be 'C/F' or a valid numeric format (1-8 digits)", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject NIT with alphanumeric characters")
    void shouldRejectNITWithAlphanumericCharacters() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "1234567A",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        InvalidNITException exception = assertThrows(InvalidNITException.class, 
            () -> patientInfo.validateNIT());
        assertEquals("NIT must be 'C/F' or a valid numeric format (1-8 digits)", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject null NIT")
    void shouldRejectNullNIT() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            null,
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        InvalidNITException exception = assertThrows(InvalidNITException.class, 
            () -> patientInfo.validateNIT());
        assertEquals("NIT must be 'C/F' or a valid numeric format (1-8 digits)", exception.getMessage());
    }
    
    // Email Validation Tests
    
    @Test
    @DisplayName("Should accept valid email format")
    void shouldAcceptValidEmailFormat() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan.garcia@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validateEmail());
    }
    
    @Test
    @DisplayName("Should accept email with plus sign")
    void shouldAcceptEmailWithPlusSign() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan+test@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validateEmail());
    }
    
    @Test
    @DisplayName("Should reject email without @ symbol")
    void shouldRejectEmailWithoutAtSymbol() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juanexample.com"
        );
        
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, 
            () -> patientInfo.validateEmail());
        assertEquals("Email must be in a valid format", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject email without domain")
    void shouldRejectEmailWithoutDomain() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@"
        );
        
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, 
            () -> patientInfo.validateEmail());
        assertEquals("Email must be in a valid format", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject null email")
    void shouldRejectNullEmail() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            null
        );
        
        InvalidEmailException exception = assertThrows(InvalidEmailException.class, 
            () -> patientInfo.validateEmail());
        assertEquals("Email must be in a valid format", exception.getMessage());
    }
    
    // Phone Validation Tests
    
    @Test
    @DisplayName("Should accept valid 8-digit phone")
    void shouldAcceptValid8DigitPhone() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertDoesNotThrow(() -> patientInfo.validatePhone());
    }
    
    @Test
    @DisplayName("Should reject phone with less than 8 digits")
    void shouldRejectPhoneWithLessThan8Digits() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "1234567",  // 7 digits
            "juan@example.com"
        );
        
        InvalidPhoneException exception = assertThrows(InvalidPhoneException.class, 
            () -> patientInfo.validatePhone());
        assertEquals("Phone must be exactly 8 numeric digits", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject phone with more than 8 digits")
    void shouldRejectPhoneWithMoreThan8Digits() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "123456789",  // 9 digits
            "juan@example.com"
        );
        
        InvalidPhoneException exception = assertThrows(InvalidPhoneException.class, 
            () -> patientInfo.validatePhone());
        assertEquals("Phone must be exactly 8 numeric digits", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject phone with alphanumeric characters")
    void shouldRejectPhoneWithAlphanumericCharacters() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "1234567A",
            "juan@example.com"
        );
        
        InvalidPhoneException exception = assertThrows(InvalidPhoneException.class, 
            () -> patientInfo.validatePhone());
        assertEquals("Phone must be exactly 8 numeric digits", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should reject null phone")
    void shouldRejectNullPhone() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            null,
            "juan@example.com"
        );
        
        InvalidPhoneException exception = assertThrows(InvalidPhoneException.class, 
            () -> patientInfo.validatePhone());
        assertEquals("Phone must be exactly 8 numeric digits", exception.getMessage());
    }
    
    // getFullName() Tests
    
    @Test
    @DisplayName("Should return full name with all four name parts")
    void shouldReturnFullNameWithAllFourParts() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            "Carlos",
            "García",
            "López",
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertEquals("Juan Carlos García López", patientInfo.getFullName());
    }
    
    @Test
    @DisplayName("Should return full name without second name")
    void shouldReturnFullNameWithoutSecondName() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            "López",
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertEquals("Juan García López", patientInfo.getFullName());
    }
    
    @Test
    @DisplayName("Should return full name without second last name")
    void shouldReturnFullNameWithoutSecondLastName() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            "Carlos",
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertEquals("Juan Carlos García", patientInfo.getFullName());
    }
    
    @Test
    @DisplayName("Should return full name with only required names")
    void shouldReturnFullNameWithOnlyRequiredNames() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertEquals("Juan García", patientInfo.getFullName());
    }
    
    @Test
    @DisplayName("Should return full name ignoring empty strings")
    void shouldReturnFullNameIgnoringEmptyStrings() {
        PatientInfo patientInfo = new PatientInfo(
            "1234567890123",
            "C/F",
            "Juan",
            "",
            "García",
            "",
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
        
        assertEquals("Juan García", patientInfo.getFullName());
    }
}
