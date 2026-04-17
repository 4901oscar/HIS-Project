package com.medflow.billing.dto.request;

import com.medflow.billing.model.ChargeType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ChargeRequestTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testValidChargeRequest() {
        // Arrange
        ChargeRequest request = new ChargeRequest(
            ChargeType.CONSULTATION,
            "Consulta general",
            1,
            new BigDecimal("50.00")
        );
        
        // Act
        Set<ConstraintViolation<ChargeRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid charge should have no violations");
    }
    
    @Test
    void testTypeIsRequired() {
        // Arrange
        ChargeRequest request = new ChargeRequest(
            null,
            "Consulta general",
            1,
            new BigDecimal("50.00")
        );
        
        // Act
        Set<ConstraintViolation<ChargeRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("tipo de cargo")));
    }
    
    @Test
    void testDescriptionIsRequired() {
        // Arrange
        ChargeRequest request = new ChargeRequest(
            ChargeType.CONSULTATION,
            "",
            1,
            new BigDecimal("50.00")
        );
        
        // Act
        Set<ConstraintViolation<ChargeRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("descripción")));
    }
    
    @Test
    void testQuantityMustBePositive() {
        // Arrange
        ChargeRequest request = new ChargeRequest(
            ChargeType.CONSULTATION,
            "Consulta general",
            0,
            new BigDecimal("50.00")
        );
        
        // Act
        Set<ConstraintViolation<ChargeRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("cantidad debe ser mayor a cero")));
    }
    
    @Test
    void testUnitPriceMustBePositive() {
        // Arrange
        ChargeRequest request = new ChargeRequest(
            ChargeType.CONSULTATION,
            "Consulta general",
            1,
            BigDecimal.ZERO
        );
        
        // Act
        Set<ConstraintViolation<ChargeRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("precio unitario debe ser mayor a cero")));
    }
}
