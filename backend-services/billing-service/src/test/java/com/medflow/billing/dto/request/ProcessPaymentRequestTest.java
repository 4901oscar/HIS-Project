package com.medflow.billing.dto.request;

import com.medflow.billing.model.PaymentMethod;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProcessPaymentRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidProcessPaymentRequest() {
        // Arrange
        ProcessPaymentRequest request = new ProcessPaymentRequest(
            new BigDecimal("100.00"),
            PaymentMethod.CASH
        );
        
        // Act
        Set<ConstraintViolation<ProcessPaymentRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid payment request should have no violations");
    }
    
    @Test
    void testAmountIsRequired() {
        // Arrange
        ProcessPaymentRequest request = new ProcessPaymentRequest(null, PaymentMethod.CASH);
        
        // Act
        Set<ConstraintViolation<ProcessPaymentRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("monto del pago")));
    }
    
    @Test
    void testAmountMustBePositive() {
        // Arrange
        ProcessPaymentRequest request = new ProcessPaymentRequest(
            BigDecimal.ZERO,
            PaymentMethod.CASH
        );
        
        // Act
        Set<ConstraintViolation<ProcessPaymentRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("monto del pago debe ser mayor a cero")));
    }
    
    @Test
    void testPaymentMethodIsRequired() {
        // Arrange
        ProcessPaymentRequest request = new ProcessPaymentRequest(
            new BigDecimal("100.00"),
            null
        );
        
        // Act
        Set<ConstraintViolation<ProcessPaymentRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("método de pago")));
    }
}
