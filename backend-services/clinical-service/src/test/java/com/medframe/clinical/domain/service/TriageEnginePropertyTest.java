package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.ManchesterDiscriminator;
import com.medframe.clinical.domain.model.PriorityLevel;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotEmpty;
import org.assertj.core.api.Assertions;

import java.util.Comparator;
import java.util.List;

/**
 * Property-Based Tests for TriageEngine.calculatePriorityLevel()
 * Pure function — no repositories needed.
 */
class TriageEnginePropertyTest {

    private final TriageEngine engine = new TriageEngine(null, null);

    /**
     * Property 1: Manchester Algorithm Selects Maximum Priority
     * Result must be the level with the minimum wait time (max urgency).
     */
    @Property(tries = 200)
    void manchesterAlgorithmSelectsMaximumPriority(
            @ForAll("nonEmptyDiscriminatorList") List<ManchesterDiscriminator> discriminators) {

        PriorityLevel result = engine.calculatePriorityLevel(discriminators);

        PriorityLevel expected = discriminators.stream()
                .map(ManchesterDiscriminator::getPriorityLevel)
                .min(Comparator.comparingInt(PriorityLevel::getMaxWaitMinutes))
                .orElseThrow();

        Assertions.assertThat(result).isEqualTo(expected);
        Assertions.assertThat(result.getMaxWaitMinutes()).isEqualTo(expected.getMaxWaitMinutes());
    }

    /**
     * Property 2: Manchester Algorithm is Deterministic
     */
    @Property(tries = 200)
    void manchesterAlgorithmIsDeterministic(
            @ForAll("nonEmptyDiscriminatorList") List<ManchesterDiscriminator> discriminators) {

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
            @ForAll("nonEmptyDiscriminatorList") List<ManchesterDiscriminator> discriminators) {

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
            @ForAll("nonEmptyDiscriminatorList") List<ManchesterDiscriminator> others) {

        ManchesterDiscriminator red =
                new ManchesterDiscriminator("red-1", "R01", "Paro cardíaco", PriorityLevel.RED);

        List<ManchesterDiscriminator> withRed = new java.util.ArrayList<>(others);
        withRed.add(red);

        PriorityLevel result = engine.calculatePriorityLevel(withRed);

        Assertions.assertThat(result).isEqualTo(PriorityLevel.RED);
        Assertions.assertThat(result.getMaxWaitMinutes()).isEqualTo(0);
    }

    // ---- Arbitrary providers ----

    @Provide
    Arbitrary<List<ManchesterDiscriminator>> nonEmptyDiscriminatorList() {
        return discriminatorArbitrary().list().ofMinSize(1).ofMaxSize(10);
    }

    @Provide
    Arbitrary<ManchesterDiscriminator> discriminatorArbitrary() {
        return Arbitraries.of(PriorityLevel.values())
                .map(level -> new ManchesterDiscriminator(
                        "id-" + level.name(),
                        level.name().substring(0, 1) + "01",
                        "Discriminator for " + level.getDescription(),
                        level
                ));
    }
}
