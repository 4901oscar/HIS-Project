package com.medflow.billing.dto.response;

import com.medflow.billing.model.ChargeType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ChargeResponseTest {
    
    @Test
    void testChargeResponseCreation() {
        // Arrange & Act
        ChargeResponse response = new ChargeResponse(
            "charge-123",
            ChargeType.CONSULTATION,
            "Consulta general",
            1,
            new BigDecimal("50.00"),
            new BigDecimal("50.00")
        );
        
        // Assert
        assertEquals("charge-123", response.getId());
        assertEquals(ChargeType.CONSULTATION, response.getType());
        assertEquals("Consulta general", response.getDescription());
        assertEquals(1, response.getQuantity());
        assertEquals(new BigDecimal("50.00"), response.getUnitPrice());
        assertEquals(new BigDecimal("50.00"), response.getSubtotal());
    }
    
    @Test
    void testChargeResponseNoArgsConstructor() {
        // Act
        ChargeResponse response = new ChargeResponse();
        
        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getType());
        assertNull(response.getDescription());
        assertNull(response.getQuantity());
        assertNull(response.getUnitPrice());
        assertNull(response.getSubtotal());
    }
    
    @Test
    void testChargeResponseSetters() {
        // Arrange
        ChargeResponse response = new ChargeResponse();
        
        // Act
        response.setId("charge-456");
        response.setType(ChargeType.LABORATORY);
        response.setDescription("Análisis de sangre");
        response.setQuantity(2);
        response.setUnitPrice(new BigDecimal("30.00"));
        response.setSubtotal(new BigDecimal("60.00"));
        
        // Assert
        assertEquals("charge-456", response.getId());
        assertEquals(ChargeType.LABORATORY, response.getType());
        assertEquals("Análisis de sangre", response.getDescription());
        assertEquals(2, response.getQuantity());
        assertEquals(new BigDecimal("30.00"), response.getUnitPrice());
        assertEquals(new BigDecimal("60.00"), response.getSubtotal());
    }
}
