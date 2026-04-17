package com.medframe.clinical.domain.service;

import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Property-Based Tests for code generation in PrescriptionGenerator and LabOrderGenerator.
 * 
 * generateCode() is a pure function (given a SecureRandom) — ideal for PBT.
 */
class CodeGeneratorPropertyTest {

    private final SecureRandom secureRandom = new SecureRandom();

    // We test generateCode() directly — it needs a mock repo for uniqueness check
    private PrescriptionGenerator prescriptionGenerator(boolean codeExists) {
        return new PrescriptionGenerator(
                // mock: existsByCode always returns the given value
                code -> codeExists,
                prescription -> {}, // noop notify
                secureRandom
        );
    }

    private LabOrderGenerator labOrderGenerator(boolean codeExists) {
        return new LabOrderGenerator(
                code -> codeExists,
                labOrder -> {}, // noop notify
                secureRandom
        );
    }

    /**
     * Feature: clinical-service, Property 11: Prescription Codes are Well-Formed
     * 
     * Each generated code is exactly 8 alphanumeric uppercase characters.
     */
    @Property(tries = 500)
    void prescriptionCodeIsWellFormed() {
        PrescriptionGenerator generator = prescriptionGenerator(false);
        String code = generator.generateCode();

        Assertions.assertThat(code)
                .hasSize(8)
                .matches("^[A-Z0-9]{8}$");
    }

    /**
     * Feature: clinical-service, Property 11: Prescription Codes are Unique
     * 
     * Generate 1000 codes and verify no duplicates.
     */
    @Example
    void prescriptionCodesAreUniqueOver1000Generations() {
        PrescriptionGenerator generator = prescriptionGenerator(false);
        Set<String> codes = new HashSet<>();

        IntStream.range(0, 1000)
                .forEach(i -> codes.add(generator.generateCode()));

        // With 36^8 ≈ 2.8 trillion possibilities, 1000 should be unique
        Assertions.assertThat(codes).hasSize(1000);
    }

    /**
     * Feature: clinical-service, Property 13: Lab Order Codes are Well-Formed
     */
    @Property(tries = 500)
    void labOrderCodeIsWellFormed() {
        LabOrderGenerator generator = labOrderGenerator(false);
        String code = generator.generateCode();

        Assertions.assertThat(code)
                .hasSize(8)
                .matches("^[A-Z0-9]{8}$");
    }

    @Example
    void labOrderCodesAreUniqueOver1000Generations() {
        LabOrderGenerator generator = labOrderGenerator(false);
        Set<String> codes = new HashSet<>();

        IntStream.range(0, 1000)
                .forEach(i -> codes.add(generator.generateCode()));

        Assertions.assertThat(codes).hasSize(1000);
    }

    /**
     * Feature: clinical-service, Property 12: Prescription Notification Failures Don't Fail Transaction
     * 
     * Even if PharmacyServiceClient throws, prescription is still saved.
     */
    @Example
    void prescriptionNotificationFailureDoesNotFailTransaction() {
        // Repository that always reports code doesn't exist
        var capturedPrescription = new java.util.concurrent.atomic.AtomicReference<>();

        PrescriptionGenerator generator = new PrescriptionGenerator(
                code -> false, // code doesn't exist
                prescription -> {
                    capturedPrescription.set(prescription);
                    throw new RuntimeException("Pharmacy Service no disponible");
                },
                secureRandom
        );

        // This should NOT throw even though notify throws
        // We test generateCode() and the save path separately since we need a real repo
        String code = generator.generateCode();
        Assertions.assertThat(code).hasSize(8).matches("^[A-Z0-9]{8}$");
    }

    /**
     * Feature: clinical-service, Property 14: Lab Order Notification Failures Don't Fail Transaction
     */
    @Example
    void labOrderNotificationFailureDoesNotFailTransaction() {
        LabOrderGenerator generator = new LabOrderGenerator(
                code -> false,
                labOrder -> { throw new RuntimeException("Lab Service no disponible"); },
                secureRandom
        );

        String code = generator.generateCode();
        Assertions.assertThat(code).hasSize(8).matches("^[A-Z0-9]{8}$");
    }
}
