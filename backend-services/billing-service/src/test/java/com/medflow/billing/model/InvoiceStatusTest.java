package com.medflow.billing.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceStatusTest {

    @Test
    void testEnumValues() {
        // Verify all three states exist
        InvoiceStatus[] statuses = InvoiceStatus.values();
        assertEquals(3, statuses.length);
        
        // Verify each state can be accessed
        assertNotNull(InvoiceStatus.PENDING);
        assertNotNull(InvoiceStatus.PAID);
        assertNotNull(InvoiceStatus.CANCELLED);
    }

    @Test
    void testEnumValueOf() {
        // Verify valueOf works correctly
        assertEquals(InvoiceStatus.PENDING, InvoiceStatus.valueOf("PENDING"));
        assertEquals(InvoiceStatus.PAID, InvoiceStatus.valueOf("PAID"));
        assertEquals(InvoiceStatus.CANCELLED, InvoiceStatus.valueOf("CANCELLED"));
    }

    @Test
    void testEnumToString() {
        // Verify toString returns the correct name
        assertEquals("PENDING", InvoiceStatus.PENDING.toString());
        assertEquals("PAID", InvoiceStatus.PAID.toString());
        assertEquals("CANCELLED", InvoiceStatus.CANCELLED.toString());
    }
}
