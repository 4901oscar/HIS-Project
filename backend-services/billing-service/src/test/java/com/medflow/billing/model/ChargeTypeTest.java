package com.medflow.billing.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChargeTypeTest {

    @Test
    void testEnumValues() {
        // Verify all four charge types exist
        ChargeType[] types = ChargeType.values();
        assertEquals(4, types.length);
        
        // Verify each type can be accessed
        assertNotNull(ChargeType.CONSULTATION);
        assertNotNull(ChargeType.LABORATORY);
        assertNotNull(ChargeType.MEDICATION);
        assertNotNull(ChargeType.OTHER);
    }

    @Test
    void testEnumValueOf() {
        // Verify valueOf works correctly
        assertEquals(ChargeType.CONSULTATION, ChargeType.valueOf("CONSULTATION"));
        assertEquals(ChargeType.LABORATORY, ChargeType.valueOf("LABORATORY"));
        assertEquals(ChargeType.MEDICATION, ChargeType.valueOf("MEDICATION"));
        assertEquals(ChargeType.OTHER, ChargeType.valueOf("OTHER"));
    }

    @Test
    void testEnumToString() {
        // Verify toString returns the correct name
        assertEquals("CONSULTATION", ChargeType.CONSULTATION.toString());
        assertEquals("LABORATORY", ChargeType.LABORATORY.toString());
        assertEquals("MEDICATION", ChargeType.MEDICATION.toString());
        assertEquals("OTHER", ChargeType.OTHER.toString());
    }
}
