package com.medflow.billing.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Payment entity
 */
class PaymentTest {
    
    @Test
    void testPaymentCreation() {
        Payment payment = new Payment();
        payment.setInvoiceId("invoice-123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setChange(new BigDecimal("0.00"));
        payment.setMethod(PaymentMethod.CASH);
        payment.setPaidAt(LocalDateTime.now());
        payment.setReceivedBy("user-123");
        
        assertEquals("invoice-123", payment.getInvoiceId());
        assertEquals(new BigDecimal("100.00"), payment.getAmount());
        assertEquals(new BigDecimal("0.00"), payment.getChange());
        assertEquals(PaymentMethod.CASH, payment.getMethod());
        assertNotNull(payment.getPaidAt());
        assertEquals("user-123", payment.getReceivedBy());
    }
    
    @Test
    void testPaymentWithChange() {
        Payment payment = new Payment();
        payment.setInvoiceId("invoice-123");
        payment.setAmount(new BigDecimal("150.00"));
        payment.setChange(new BigDecimal("50.00"));
        payment.setMethod(PaymentMethod.CASH);
        payment.setPaidAt(LocalDateTime.now());
        payment.setReceivedBy("user-123");
        
        assertEquals(new BigDecimal("150.00"), payment.getAmount());
        assertEquals(new BigDecimal("50.00"), payment.getChange());
    }
    
    @Test
    void testPaymentWithCardMethod() {
        Payment payment = new Payment();
        payment.setInvoiceId("invoice-123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setChange(new BigDecimal("0.00"));
        payment.setMethod(PaymentMethod.CARD);
        payment.setPaidAt(LocalDateTime.now());
        payment.setReceivedBy("user-123");
        
        assertEquals(PaymentMethod.CARD, payment.getMethod());
        assertEquals(new BigDecimal("0.00"), payment.getChange());
    }
    
    @Test
    void testPaymentWithTransferMethod() {
        Payment payment = new Payment();
        payment.setInvoiceId("invoice-123");
        payment.setAmount(new BigDecimal("100.00"));
        payment.setChange(new BigDecimal("0.00"));
        payment.setMethod(PaymentMethod.TRANSFER);
        payment.setPaidAt(LocalDateTime.now());
        payment.setReceivedBy("user-123");
        
        assertEquals(PaymentMethod.TRANSFER, payment.getMethod());
    }
    
    @Test
    void testPaymentAllArgsConstructor() {
        LocalDateTime paidAt = LocalDateTime.now();
        Payment payment = new Payment(
            "payment-123",
            "invoice-123",
            new BigDecimal("100.00"),
            new BigDecimal("0.00"),
            PaymentMethod.CASH,
            paidAt,
            "user-123"
        );
        
        assertEquals("payment-123", payment.getId());
        assertEquals("invoice-123", payment.getInvoiceId());
        assertEquals(new BigDecimal("100.00"), payment.getAmount());
        assertEquals(new BigDecimal("0.00"), payment.getChange());
        assertEquals(PaymentMethod.CASH, payment.getMethod());
        assertEquals(paidAt, payment.getPaidAt());
        assertEquals("user-123", payment.getReceivedBy());
    }
    
    @Test
    void testPaymentNoArgsConstructor() {
        Payment payment = new Payment();
        assertNotNull(payment);
        assertNull(payment.getId());
        assertNull(payment.getInvoiceId());
        assertNull(payment.getAmount());
        assertNull(payment.getChange());
        assertNull(payment.getMethod());
        assertNull(payment.getPaidAt());
        assertNull(payment.getReceivedBy());
    }
}
