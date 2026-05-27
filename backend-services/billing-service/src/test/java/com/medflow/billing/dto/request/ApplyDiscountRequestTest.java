package com.medflow.billing.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApplyDiscountRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidDiscountAmount() {
        // Arrange
        ApplyDiscountRequest request = new ApplyDiscountRequest(
            new BigDecimal("10.00"),
            null
        );
        
        // Act
        Set<ConstraintViolation<ApplyDiscountRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid discount amount should have no violations");
    }
    
    @Test
    void testValidDiscountPercentage() {
        // Arrange
        ApplyDiscountRequest request = new ApplyDiscountRequest(
            null,
            new BigDecimal("15.0")
        );
        
        // Act
        Set<ConstraintViolation<ApplyDiscountRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid discount percentage should have no violations");
    }
    
    @Test
    void testDiscountAmountMustBePositive() {
        // Arrange
        ApplyDiscountRequest request = new ApplyDiscountRequest(
            BigDecimal.ZERO,
            null
        );
        
        // Act
        Set<ConstraintViolation<ApplyDiscountRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("monto del descuento debe ser mayor a cero")));
    }
    
    @Test
    void testDiscountPercentageMinimum() {
        // Arrange
        ApplyDiscountRequest request = new ApplyDiscountRequest(
            null,
            BigDecimal.ZERO
        );
        
        // Act
        Set<ConstraintViolation<ApplyDiscountRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("porcentaje de descuento debe ser mayor a 0")));
    }
    
    @Test
    void testDiscountPercentageMaximum() {
        // Arrange
        ApplyDiscountRequest request = new ApplyDiscountRequest(
            null,
            new BigDecimal("101.0")
        );
        
        // Act
        Set<ConstraintViolation<ApplyDiscountRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("porcentaje de descuento no puede exceder 100")));
    }
    
    @Test
    void testBothFieldsCanBeNull() {
        // Arrange - Both null is allowed, business logic will handle validation
        ApplyDiscountRequest request = new ApplyDiscountRequest(null, null);
        
        // Act
        Set<ConstraintViolation<ApplyDiscountRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Both fields null should pass validation");
    }
}
