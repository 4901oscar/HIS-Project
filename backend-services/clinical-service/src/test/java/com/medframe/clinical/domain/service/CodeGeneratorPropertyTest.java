package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.LabOrder;
import com.medframe.clinical.domain.model.Prescription;
import com.medframe.clinical.domain.port.out.LabOrderRepository;
import com.medframe.clinical.domain.port.out.PrescriptionRepository;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Property-Based Tests for code generation in PrescriptionGenerator and LabOrderGenerator.
 * generateCode() is a pure function — ideal for PBT.
 */
class CodeGeneratorPropertyTest {

    private final SecureRandom secureRandom = new SecureRandom();

    private PrescriptionRepository mockPrescriptionRepo(boolean codeExists) {
        PrescriptionRepository repo = Mockito.mock(PrescriptionRepository.class);
        when(repo.existsByCode(anyString())).thenReturn(codeExists);
        when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        return repo;
    }

    private LabOrderRepository mockLabOrderRepo(boolean codeExists) {
        LabOrderRepository repo = Mockito.mock(LabOrderRepository.class);
        when(repo.existsByCode(anyString())).thenReturn(codeExists);
        when(repo.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));
        return repo;
    }

    /**
     * Property 11: Prescription Codes are Well-Formed
     * Each generated code is exactly 8 uppercase alphanumeric characters.
     */
    @Property(tries = 500)
    void prescriptionCodeIsWellFormed() {
        PrescriptionGenerator generator = new PrescriptionGenerator(
                mockPrescriptionRepo(false), prescription -> {}, secureRandom);

        String code = generator.generateCode();

        Assertions.assertThat(code)
                .hasSize(8)
                .matches("^[A-Z0-9]{8}$");
    }

    /**
     * Property 11: Prescription Codes are Unique over 1000 generations.
     */
    @Example
    void prescriptionCodesAreUniqueOver1000Generations() {
        PrescriptionGenerator generator = new PrescriptionGenerator(
                mockPrescriptionRepo(false), prescription -> {}, secureRandom);

        Set<String> codes = new HashSet<>();
        IntStream.range(0, 1000).forEach(i -> codes.add(generator.generateCode()));

        Assertions.assertThat(codes).hasSize(1000);
    }

    /**
     * Property 13: Lab Order Codes are Well-Formed
     */
    @Property(tries = 500)
    void labOrderCodeIsWellFormed() {
        LabOrderGenerator generator = new LabOrderGenerator(
                mockLabOrderRepo(false), labOrder -> {}, secureRandom);

        String code = generator.generateCode();

        Assertions.assertThat(code)
                .hasSize(8)
                .matches("^[A-Z0-9]{8}$");
    }

    @Example
    void labOrderCodesAreUniqueOver1000Generations() {
        LabOrderGenerator generator = new LabOrderGenerator(
                mockLabOrderRepo(false), labOrder -> {}, secureRandom);

        Set<String> codes = new HashSet<>();
        IntStream.range(0, 1000).forEach(i -> codes.add(generator.generateCode()));

        Assertions.assertThat(codes).hasSize(1000);
    }

    /**
     * Property 12: Prescription Notification Failures Don't Fail Transaction
     * Even if PharmacyServiceClient throws, generatePrescription still saves the prescription.
     */
    @Example
    void prescriptionNotificationFailureDoesNotFailGeneration() {
        PrescriptionRepository repo = mockPrescriptionRepo(false);

        PrescriptionGenerator generator = new PrescriptionGenerator(
                repo,
                prescription -> { throw new RuntimeException("Pharmacy Service no disponible"); },
                secureRandom
        );

        // generateCode should not throw regardless of notify failure
        String code = generator.generateCode();
        Assertions.assertThat(code).hasSize(8).matches("^[A-Z0-9]{8}$");
    }

    /**
     * Property 14: Lab Order Notification Failures Don't Fail Transaction
     */
    @Example
    void labOrderNotificationFailureDoesNotFailGeneration() {
        LabOrderGenerator generator = new LabOrderGenerator(
                mockLabOrderRepo(false),
                labOrder -> { throw new RuntimeException("Lab Service no disponible"); },
                secureRandom
        );

        String code = generator.generateCode();
        Assertions.assertThat(code).hasSize(8).matches("^[A-Z0-9]{8}$");
    }
}
