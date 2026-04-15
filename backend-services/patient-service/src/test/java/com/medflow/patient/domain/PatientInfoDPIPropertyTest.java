package com.medflow.patient.domain;

import net.jqwik.api.*;
import net.jqwik.api.constraints.StringLength;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for DPI validation in PatientInfo.
 * 
 * Feature: patient-appointment-scheduling
 * Property 3: DPI Validation
 * Validates: Requirements 3.3, 11.4
 * 
 * Tests that DPI validation correctly accepts only 13-digit numeric strings
 * and rejects all other formats across a wide range of generated inputs.
 */
class PatientInfoDPIPropertyTest {
    
    /**
     * Property 3: DPI Validation
     * 
     * For any string input as a DPI, the validation SHALL accept if and only if
     * the string contains exactly 13 numeric digits.
     * 
     * Validates: Requirements 3.3, 11.4
     */
    @Property(tries = 100)
    @Label("Property 3: Only 13-digit numeric strings pass DPI validation")
    void onlyThirteenDigitNumericStringsPassDPIValidation(
            @ForAll("validDPIs") String validDPI) {
        
        // Given a valid 13-digit DPI
        PatientInfo patientInfo = createPatientInfoWithDPI(validDPI);
        
        // When validating the DPI
        // Then no exception should be thrown
        assertDoesNotThrow(() -> patientInfo.validateDPI(),
            "Valid 13-digit DPI should pass validation: " + validDPI);
    }
    
    @Property(tries = 100)
    @Label("Property 3: DPI with less than 13 digits is rejected")
    void dpiWithLessThan13DigitsIsRejected(
            @ForAll("shortNumericStrings") String shortDPI) {
        
        // Given a DPI with less than 13 digits (all numeric)
        PatientInfo patientInfo = createPatientInfoWithDPI(shortDPI);
        
        // When validating the DPI
        // Then an InvalidDPIException should be thrown
        InvalidDPIException exception = assertThrows(InvalidDPIException.class,
            () -> patientInfo.validateDPI(),
            "DPI with " + shortDPI.length() + " digits should be rejected");
        
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Property(tries = 100)
    @Label("Property 3: DPI with more than 13 digits is rejected")
    void dpiWithMoreThan13DigitsIsRejected(
            @ForAll("longNumericStrings") String longDPI) {
        
        // Given a DPI with more than 13 digits (all numeric)
        PatientInfo patientInfo = createPatientInfoWithDPI(longDPI);
        
        // When validating the DPI
        // Then an InvalidDPIException should be thrown
        InvalidDPIException exception = assertThrows(InvalidDPIException.class,
            () -> patientInfo.validateDPI(),
            "DPI with " + longDPI.length() + " digits should be rejected");
        
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Property(tries = 100)
    @Label("Property 3: DPI with alphanumeric characters is rejected")
    void dpiWithAlphanumericCharactersIsRejected(
            @ForAll("alphanumericStrings") String alphanumericDPI) {
        
        // Given a DPI with alphanumeric characters
        PatientInfo patientInfo = createPatientInfoWithDPI(alphanumericDPI);
        
        // When validating the DPI
        // Then an InvalidDPIException should be thrown
        InvalidDPIException exception = assertThrows(InvalidDPIException.class,
            () -> patientInfo.validateDPI(),
            "DPI with alphanumeric characters should be rejected: " + alphanumericDPI);
        
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Property(tries = 100)
    @Label("Property 3: DPI with special characters is rejected")
    void dpiWithSpecialCharactersIsRejected(
            @ForAll("specialCharacterStrings") String specialCharDPI) {
        
        // Given a DPI with special characters
        PatientInfo patientInfo = createPatientInfoWithDPI(specialCharDPI);
        
        // When validating the DPI
        // Then an InvalidDPIException should be thrown
        InvalidDPIException exception = assertThrows(InvalidDPIException.class,
            () -> patientInfo.validateDPI(),
            "DPI with special characters should be rejected: " + specialCharDPI);
        
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Property(tries = 50)
    @Label("Property 3: DPI with exactly 13 characters but non-numeric is rejected")
    void dpiWith13CharactersButNonNumericIsRejected(
            @ForAll("thirteenCharNonNumeric") String nonNumericDPI) {
        
        // Given a DPI with exactly 13 characters but containing non-numeric characters
        PatientInfo patientInfo = createPatientInfoWithDPI(nonNumericDPI);
        
        // When validating the DPI
        // Then an InvalidDPIException should be thrown
        InvalidDPIException exception = assertThrows(InvalidDPIException.class,
            () -> patientInfo.validateDPI(),
            "DPI with 13 characters but non-numeric should be rejected: " + nonNumericDPI);
        
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Property(tries = 10)
    @Label("Property 3: Null DPI is rejected")
    void nullDPIIsRejected() {
        // Given a null DPI
        PatientInfo patientInfo = createPatientInfoWithDPI(null);
        
        // When validating the DPI
        // Then an InvalidDPIException should be thrown
        InvalidDPIException exception = assertThrows(InvalidDPIException.class,
            () -> patientInfo.validateDPI(),
            "Null DPI should be rejected");
        
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    @Property(tries = 10)
    @Label("Property 3: Empty string DPI is rejected")
    void emptyStringDPIIsRejected() {
        // Given an empty string DPI
        PatientInfo patientInfo = createPatientInfoWithDPI("");
        
        // When validating the DPI
        // Then an InvalidDPIException should be thrown
        InvalidDPIException exception = assertThrows(InvalidDPIException.class,
            () -> patientInfo.validateDPI(),
            "Empty string DPI should be rejected");
        
        assertEquals("DPI must be exactly 13 numeric digits", exception.getMessage());
    }
    
    // ========== Arbitraries (Generators) ==========
    
    /**
     * Generates valid 13-digit DPI strings.
     */
    @Provide
    Arbitrary<String> validDPIs() {
        return Arbitraries.strings()
            .numeric()
            .ofLength(13);
    }
    
    /**
     * Generates numeric strings with less than 13 digits (0-12 digits).
     */
    @Provide
    Arbitrary<String> shortNumericStrings() {
        return Arbitraries.integers()
            .between(0, 12)
            .flatMap(length -> Arbitraries.strings()
                .numeric()
                .ofLength(length));
    }
    
    /**
     * Generates numeric strings with more than 13 digits (14-20 digits).
     */
    @Provide
    Arbitrary<String> longNumericStrings() {
        return Arbitraries.integers()
            .between(14, 20)
            .flatMap(length -> Arbitraries.strings()
                .numeric()
                .ofLength(length));
    }
    
    /**
     * Generates alphanumeric strings of length 13 that contain at least one letter.
     */
    @Provide
    Arbitrary<String> alphanumericStrings() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .ofLength(13)
            .filter(s -> s.matches(".*[a-zA-Z].*")); // Must contain at least one letter
    }
    
    /**
     * Generates strings with special characters.
     */
    @Provide
    Arbitrary<String> specialCharacterStrings() {
        return Arbitraries.strings()
            .withCharRange('!', '/')  // Special characters
            .numeric()
            .ofMinLength(10)
            .ofMaxLength(15)
            .filter(s -> s.matches(".*[^0-9].*")); // Must contain at least one non-digit
    }
    
    /**
     * Generates strings with exactly 13 characters but containing non-numeric characters.
     */
    @Provide
    Arbitrary<String> thirteenCharNonNumeric() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .withChars('-', '_', '.', ' ')
            .ofLength(13)
            .filter(s -> !s.matches("\\d{13}")); // Must NOT be all digits
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Creates a PatientInfo instance with the given DPI.
     * Uses valid default values for all other fields.
     */
    private PatientInfo createPatientInfoWithDPI(String dpi) {
        return new PatientInfo(
            dpi,
            "C/F",
            "Juan",
            null,
            "García",
            null,
            LocalDate.of(1990, 5, 15),
            "12345678",
            "juan@example.com"
        );
    }
}
