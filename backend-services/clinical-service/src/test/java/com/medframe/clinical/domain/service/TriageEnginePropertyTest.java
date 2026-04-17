package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.ManchesterDiscriminator;
import com.medframe.clinical.domain.model.PriorityLevel;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import org.assertj.core.api.Assertions;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Property-Based Tests for TriageEngine.calculatePriorityLevel()
 * 
 * This is a pure function — ideal for PBT.
 * We test the TriageEngine directly without needing repositories.
 */
class TriageEnginePropertyTest {

    /**
     * Feature: clinical-service, Property 1: Manchester Algorithm Selects Maximum Priority
     * 
     * For any non-empty list of discriminators, the calculated priority level
     * must have the minimum wait time among all discriminators (= maximum urgency).
     */
    @Property(tries = 200)
    void manchesterAlgorithmSelectsMaximumPriority(
            @ForAll @NotEmpty List<@ForAll("discriminators") ManchesterDiscriminator> discriminators) {

        // Given: Create TriageEngine with null repos (calculatePriorityLevel is pure)
        TriageEngine engine = new TriageEngine(null, null);

        // When: Calculate priority
        PriorityLevel result = engine.calculatePriorityLevel(discriminators);

        // Then: Result must be the level with minimum wait time
        PriorityLevel expected = discriminators.stream()
                .map(ManchesterDiscriminator::getPriorityLevel)
                .min(Comparator.comparingInt(PriorityLevel::getMaxWaitMinutes))
                .orElseThrow();

        Assertions.assertThat(result).isEqualTo(expected);
        Assertions.assertThat(result.getMaxWaitMinutes()).isEqualTo(expected.getMaxWaitMinutes());
    }

    /**
     * Feature: clinical-service, Property 2: Manchester Algorithm is Deterministic
     * 
     * For any set of discriminators, running the algorithm multiple times
     * with the same input always produces the same output.
     */
    @Property(tries = 200)
    void manchesterAlgorithmIsDeterministic(
            @ForAll @NotEmpty List<@ForAll("discriminators") ManchesterDiscriminator> discriminators) {

        TriageEngine engine = new TriageEngine(null, null);

        PriorityLevel first  = engine.calculatePriorityLevel(discriminators);
        PriorityLevel second = engine.calculatePriorityLevel(discriminators);
        PriorityLevel third  = engine.calculatePriorityLevel(discriminators);

        Assertions.assertThat(first).isEqualTo(second);
        Assertions.assertThat(second).isEqualTo(third);
    }

    /**
     * Result is always one of the 5 valid Manchester levels.
     */
    @Property(tries = 200)
    void resultIsAlwaysAValidPriorityLevel(
            @ForAll @NotEmpty List<@ForAll("discriminators") ManchesterDiscriminator> discriminators) {

        TriageEngine engine = new TriageEngine(null, null);
        PriorityLevel result = engine.calculatePriorityLevel(discriminators);

        Assertions.assertThat(result).isIn(
                PriorityLevel.RED, PriorityLevel.ORANGE,
                PriorityLevel.YELLOW, PriorityLevel.GREEN, PriorityLevel.BLUE);
    }

    /**
     * If any RED discriminator is present, result must be RED.
     */
    @Property(tries = 100)
    void ifAnyRedDiscriminatorThenResultIsRed(
            @ForAll @NotEmpty List<@ForAll("discriminators") ManchesterDiscriminator> otherDiscriminators) {

        TriageEngine engine = new TriageEngine(null, null);

        ManchesterDiscriminator redDiscriminator =
                new ManchesterDiscriminator("red-1", "R01", "Paro cardíaco", PriorityLevel.RED);

        List<ManchesterDiscriminator> withRed = new java.util.ArrayList<>(otherDiscriminators);
        withRed.add(redDiscriminator);

        PriorityLevel result = engine.calculatePriorityLevel(withRed);

        Assertions.assertThat(result).isEqualTo(PriorityLevel.RED);
        Assertions.assertThat(result.getMaxWaitMinutes()).isEqualTo(0);
    }

    // ---- Arbitrary providers ----

    @Provide
    Arbitrary<ManchesterDiscriminator> discriminators() {
        return Arbitraries.of(PriorityLevel.values())
                .map(level -> new ManchesterDiscriminator(
                        "id-" + level.name(),
                        level.name().substring(0, 1) + "01",
                        "Discriminator for " + level.getDescription(),
                        level
                ));
    }
}
