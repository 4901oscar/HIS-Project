package com.medflow.billing.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Charge entity
 */
class ChargeTest {
    
    @Test
    void testChargeCreation() {
        Charge charge = new Charge();
        charge.setType(ChargeType.CONSULTATION);
        charge.setDescription("Consulta general");
        charge.setQuantity(1);
        charge.setUnitPrice(new BigDecimal("50.00"));
        charge.setSubtotal(new BigDecimal("50.00"));
        
        assertEquals(ChargeType.CONSULTATION, charge.getType());
        assertEquals("Consulta general", charge.getDescription());
        assertEquals(1, charge.getQuantity());
        assertEquals(new BigDecimal("50.00"), charge.getUnitPrice());
        assertEquals(new BigDecimal("50.00"), charge.getSubtotal());
    }
    
    @Test
    void testChargeWithInvoice() {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-20260416-0001");
        
        Charge charge = new Charge();
        charge.setType(ChargeType.LABORATORY);
        charge.setDescription("Análisis de sangre");
        charge.setQuantity(2);
        charge.setUnitPrice(new BigDecimal("25.00"));
        charge.setSubtotal(new BigDecimal("50.00"));
        charge.setInvoice(invoice);
        
        assertNotNull(charge.getInvoice());
        assertEquals("INV-20260416-0001", charge.getInvoice().getInvoiceNumber());
    }
    
    @Test
    void testChargeTypes() {
        Charge consultation = new Charge();
        consultation.setType(ChargeType.CONSULTATION);
        assertEquals(ChargeType.CONSULTATION, consultation.getType());
        
        Charge laboratory = new Charge();
        laboratory.setType(ChargeType.LABORATORY);
        assertEquals(ChargeType.LABORATORY, laboratory.getType());
        
        Charge medication = new Charge();
        medication.setType(ChargeType.MEDICATION);
        assertEquals(ChargeType.MEDICATION, medication.getType());
        
        Charge other = new Charge();
        other.setType(ChargeType.OTHER);
        assertEquals(ChargeType.OTHER, other.getType());
    }
    
    @Test
    void testChargeWithMultipleQuantity() {
        Charge charge = new Charge();
        charge.setType(ChargeType.MEDICATION);
        charge.setDescription("Paracetamol 500mg");
        charge.setQuantity(10);
        charge.setUnitPrice(new BigDecimal("2.50"));
        charge.setSubtotal(new BigDecimal("25.00"));
        
        assertEquals(10, charge.getQuantity());
        assertEquals(new BigDecimal("2.50"), charge.getUnitPrice());
        assertEquals(new BigDecimal("25.00"), charge.getSubtotal());
    }
}
