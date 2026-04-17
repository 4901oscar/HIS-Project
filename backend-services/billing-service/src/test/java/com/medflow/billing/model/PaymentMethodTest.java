package com.medflow.billing.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMethodTest {

    @Test
    void testEnumValues() {
        // Verify all three payment methods exist
        PaymentMethod[] methods = PaymentMethod.values();
        assertEquals(3, methods.length);
        
        // Verify each method can be accessed
        assertNotNull(PaymentMethod.CASH);
        assertNotNull(PaymentMethod.CARD);
        assertNotNull(PaymentMethod.TRANSFER);
    }

    @Test
    void testEnumValueOf() {
        // Verify valueOf works correctly
        assertEquals(PaymentMethod.CASH, PaymentMethod.valueOf("CASH"));
        assertEquals(PaymentMethod.CARD, PaymentMethod.valueOf("CARD"));
        assertEquals(PaymentMethod.TRANSFER, PaymentMethod.valueOf("TRANSFER"));
    }

    @Test
    void testEnumToString() {
        // Verify toString returns the correct name
        assertEquals("CASH", PaymentMethod.CASH.toString());
        assertEquals("CARD", PaymentMethod.CARD.toString());
        assertEquals("TRANSFER", PaymentMethod.TRANSFER.toString());
    }
}
