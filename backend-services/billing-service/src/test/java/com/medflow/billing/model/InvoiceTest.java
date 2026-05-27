package com.medflow.billing.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Invoice entity
 */
class InvoiceTest {
    
    @Test
    void testInvoiceCreation() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-20260416-0001");
        invoice.setPatientId("patient-123");
        invoice.setSubtotal(new BigDecimal("100.00"));
        invoice.setTotal(new BigDecimal("100.00"));
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setCreatedBy("user-123");
        
        assertEquals("INV-20260416-0001", invoice.getInvoiceNumber());
        assertEquals("patient-123", invoice.getPatientId());
        assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("100.00"), invoice.getTotal());
        assertEquals(InvoiceStatus.PENDING, invoice.getStatus());
        assertEquals(BigDecimal.ZERO, invoice.getDiscountAmount());
        assertNotNull(invoice.getCharges());
        assertTrue(invoice.getCharges().isEmpty());
    }
    
    @Test
    void testAddCharge() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-20260416-0001");
        
        Charge charge = new Charge();
        charge.setType(ChargeType.CONSULTATION);
        charge.setDescription("Consulta general");
        charge.setQuantity(1);
        charge.setUnitPrice(new BigDecimal("50.00"));
        charge.setSubtotal(new BigDecimal("50.00"));
        
        invoice.addCharge(charge);
        
        assertEquals(1, invoice.getCharges().size());
        assertEquals(invoice, charge.getInvoice());
        assertEquals(ChargeType.CONSULTATION, invoice.getCharges().get(0).getType());
    }
    
    @Test
    void testAddMultipleCharges() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-20260416-0001");
        
        Charge charge1 = new Charge();
        charge1.setType(ChargeType.CONSULTATION);
        charge1.setDescription("Consulta general");
        charge1.setQuantity(1);
        charge1.setUnitPrice(new BigDecimal("50.00"));
        charge1.setSubtotal(new BigDecimal("50.00"));
        
        Charge charge2 = new Charge();
        charge2.setType(ChargeType.LABORATORY);
        charge2.setDescription("Análisis de sangre");
        charge2.setQuantity(2);
        charge2.setUnitPrice(new BigDecimal("25.00"));
        charge2.setSubtotal(new BigDecimal("50.00"));
        
        invoice.addCharge(charge1);
        invoice.addCharge(charge2);
        
        assertEquals(2, invoice.getCharges().size());
        assertEquals(invoice, charge1.getInvoice());
        assertEquals(invoice, charge2.getInvoice());
    }
    
    @Test
    void testRemoveCharge() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-20260416-0001");
        
        Charge charge = new Charge();
        charge.setType(ChargeType.CONSULTATION);
        charge.setDescription("Consulta general");
        charge.setQuantity(1);
        charge.setUnitPrice(new BigDecimal("50.00"));
        charge.setSubtotal(new BigDecimal("50.00"));
        
        invoice.addCharge(charge);
        assertEquals(1, invoice.getCharges().size());
        
        invoice.removeCharge(charge);
        assertEquals(0, invoice.getCharges().size());
        assertNull(charge.getInvoice());
    }
    
    @Test
    void testDefaultValues() {
        Invoice invoice = new Invoice();
        
        assertEquals(BigDecimal.ZERO, invoice.getSubtotal());
        assertEquals(BigDecimal.ZERO, invoice.getDiscountAmount());
        assertEquals(BigDecimal.ZERO, invoice.getTotal());
        assertEquals(InvoiceStatus.PENDING, invoice.getStatus());
        assertNotNull(invoice.getCharges());
        assertTrue(invoice.getCharges().isEmpty());
    }
    
    @Test
    void testInvoiceWithDiscount() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-20260416-0001");
        invoice.setSubtotal(new BigDecimal("100.00"));
        invoice.setDiscountAmount(new BigDecimal("10.00"));
        invoice.setTotal(new BigDecimal("90.00"));
        
        assertEquals(new BigDecimal("100.00"), invoice.getSubtotal());
        assertEquals(new BigDecimal("10.00"), invoice.getDiscountAmount());
        assertEquals(new BigDecimal("90.00"), invoice.getTotal());
    }
}
