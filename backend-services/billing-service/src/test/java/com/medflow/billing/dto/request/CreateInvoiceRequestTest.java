package com.medflow.billing.dto.request;

import com.medflow.billing.model.ChargeType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateInvoiceRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidCreateInvoiceRequest() {
        // Arrange
        ChargeRequest charge = new ChargeRequest(
            ChargeType.CONSULTATION,
            "Consulta general",
            1,
            new BigDecimal("50.00")
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "patient-123",
            List.of(charge)
        );
        
        // Act
        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid request should have no violations");
    }
    
    @Test
    void testPatientIdIsRequired() {
        // Arrange
        ChargeRequest charge = new ChargeRequest(
            ChargeType.CONSULTATION,
            "Consulta general",
            1,
            new BigDecimal("50.00")
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(null, List.of(charge));
        
        // Act
        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("ID del paciente")));
    }
    
    @Test
    void testChargesListCannotBeEmpty() {
        // Arrange
        CreateInvoiceRequest request = new CreateInvoiceRequest("patient-123", new ArrayList<>());
        
        // Act
        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("al menos un cargo")));
    }
    
    @Test
    void testNestedChargeValidation() {
        // Arrange - Invalid charge with null type
        ChargeRequest invalidCharge = new ChargeRequest(
            null,
            "Consulta general",
            1,
            new BigDecimal("50.00")
        );
        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "patient-123",
            List.of(invalidCharge)
        );
        
        // Act
        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("tipo de cargo")));
    }
}
