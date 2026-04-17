package com.medflow.billing.dto.response;

import com.medflow.billing.model.ChargeType;
import com.medflow.billing.model.InvoiceStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceResponseTest {
    
    @Test
    void testInvoiceResponseCreation() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ChargeResponse charge1 = new ChargeResponse(
            "charge-1",
            ChargeType.CONSULTATION,
            "Consulta",
            1,
            new BigDecimal("50.00"),
            new BigDecimal("50.00")
        );
        ChargeResponse charge2 = new ChargeResponse(
            "charge-2",
            ChargeType.LABORATORY,
            "Laboratorio",
            1,
            new BigDecimal("30.00"),
            new BigDecimal("30.00")
        );
        List<ChargeResponse> charges = Arrays.asList(charge1, charge2);
        
        // Act
        InvoiceResponse response = new InvoiceResponse(
            "invoice-123",
            "INV-20260416-0001",
            "patient-456",
            charges,
            new BigDecimal("80.00"),
            new BigDecimal("10.00"),
            new BigDecimal("70.00"),
            InvoiceStatus.PENDING,
            now,
            "user-789",
            null
        );
        
        // Assert
        assertEquals("invoice-123", response.getId());
        assertEquals("INV-20260416-0001", response.getInvoiceNumber());
        assertEquals("patient-456", response.getPatientId());
        assertEquals(2, response.getCharges().size());
        assertEquals(new BigDecimal("80.00"), response.getSubtotal());
        assertEquals(new BigDecimal("10.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("70.00"), response.getTotal());
        assertEquals(InvoiceStatus.PENDING, response.getStatus());
        assertEquals(now, response.getCreatedAt());
        assertEquals("user-789", response.getCreatedBy());
        assertNull(response.getUpdatedAt());
    }
    
    @Test
    void testInvoiceResponseNoArgsConstructor() {
        // Act
        InvoiceResponse response = new InvoiceResponse();
        
        // Assert
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getInvoiceNumber());
        assertNull(response.getPatientId());
        assertNull(response.getCharges());
        assertNull(response.getSubtotal());
        assertNull(response.getDiscountAmount());
        assertNull(response.getTotal());
        assertNull(response.getStatus());
        assertNull(response.getCreatedAt());
        assertNull(response.getCreatedBy());
        assertNull(response.getUpdatedAt());
    }
    
    @Test
    void testInvoiceResponseSetters() {
        // Arrange
        InvoiceResponse response = new InvoiceResponse();
        LocalDateTime now = LocalDateTime.now();
        ChargeResponse charge = new ChargeResponse(
            "charge-1",
            ChargeType.MEDICATION,
            "Medicamento",
            1,
            new BigDecimal("25.00"),
            new BigDecimal("25.00")
        );
        List<ChargeResponse> charges = Arrays.asList(charge);
        
        // Act
        response.setId("invoice-999");
        response.setInvoiceNumber("INV-20260416-0002");
        response.setPatientId("patient-111");
        response.setCharges(charges);
        response.setSubtotal(new BigDecimal("25.00"));
        response.setDiscountAmount(new BigDecimal("5.00"));
        response.setTotal(new BigDecimal("20.00"));
        response.setStatus(InvoiceStatus.PAID);
        response.setCreatedAt(now);
        response.setCreatedBy("user-222");
        response.setUpdatedAt(now);
        
        // Assert
        assertEquals("invoice-999", response.getId());
        assertEquals("INV-20260416-0002", response.getInvoiceNumber());
        assertEquals("patient-111", response.getPatientId());
        assertEquals(1, response.getCharges().size());
        assertEquals(new BigDecimal("25.00"), response.getSubtotal());
        assertEquals(new BigDecimal("5.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("20.00"), response.getTotal());
        assertEquals(InvoiceStatus.PAID, response.getStatus());
        assertEquals(now, response.getCreatedAt());
        assertEquals("user-222", response.getCreatedBy());
        assertEquals(now, response.getUpdatedAt());
    }
}
