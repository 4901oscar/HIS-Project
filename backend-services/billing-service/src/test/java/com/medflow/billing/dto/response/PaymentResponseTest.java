package com.medflow.billing.dto.response;

import com.medflow.billing.model.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PaymentResponseTest {
    
    @Test
    void testPaymentResponseCreation() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        PaymentResponse response = new PaymentResponse(
            "payment-123",
            "invoice-456",
            new BigDecimal("100.00"),
            new BigDecimal("10.00"),
            PaymentMethod.CASH,
            now,
            "user-789"
        );
        
        // Assert
        assertEquals("payment-123", response.getId());
        assertEquals("invoice-456", response.getInvoiceId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(new BigDecimal("10.00"), response.getChange());
        assertEquals(PaymentMethod.CASH, response.getMethod());
        assertEquals(now, response.getPaidAt());
        assertEquals("user-789", response.getReceivedBy());
    }
    
    @Test
    void testPaymentResponseNoArgsConstructor() {
        // Act
        PaymentResponse response = new PaymentResponse();
        
        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getInvoiceId());
        assertNull(response.getAmount());
        assertNull(response.getChange());
        assertNull(response.getMethod());
        assertNull(response.getPaidAt());
        assertNull(response.getReceivedBy());
    }
    
    @Test
    void testPaymentResponseSetters() {
        // Arrange
        PaymentResponse response = new PaymentResponse();
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        response.setId("payment-999");
        response.setInvoiceId("invoice-111");
        response.setAmount(new BigDecimal("50.00"));
        response.setChange(new BigDecimal("0.00"));
        response.setMethod(PaymentMethod.CARD);
        response.setPaidAt(now);
        response.setReceivedBy("user-222");
        
        // Assert
        assertEquals("payment-999", response.getId());
        assertEquals("invoice-111", response.getInvoiceId());
        assertEquals(new BigDecimal("50.00"), response.getAmount());
        assertEquals(new BigDecimal("0.00"), response.getChange());
        assertEquals(PaymentMethod.CARD, response.getMethod());
        assertEquals(now, response.getPaidAt());
        assertEquals("user-222", response.getReceivedBy());
    }
    
    @Test
    void testPaymentResponseWithNoChange() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        
        // Act
        PaymentResponse response = new PaymentResponse(
            "payment-555",
            "invoice-666",
            new BigDecimal("75.00"),
            BigDecimal.ZERO,
            PaymentMethod.TRANSFER,
            now,
            "user-333"
        );
        
        // Assert
        assertEquals(BigDecimal.ZERO, response.getChange());
        assertEquals(PaymentMethod.TRANSFER, response.getMethod());
    }
}
