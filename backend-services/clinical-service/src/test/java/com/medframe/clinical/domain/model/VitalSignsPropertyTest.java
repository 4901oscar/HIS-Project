package com.medframe.clinical.domain.model;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;

/**
 * Property-Based Tests for VitalSigns domain entity.
 * Pure functions: calculateBMI() and isValid() — no external dependencies.
 */
class VitalSignsPropertyTest {

    /**
     * Feature: clinical-service, Property 3: BMI Calculation is Correct
     * 
     * For any valid weight (kg) and height (cm),
     * BMI = weight / (height_in_meters)^2
     */
    @Property(tries = 300)
    void bmiCalculationIsCorrect(
            @ForAll @DoubleRange(min = 1.0, max = 300.0) double weight,
            @ForAll @DoubleRange(min = 50.0, max = 250.0) double height) {

        VitalSigns vs = new VitalSigns();
        vs.setWeight(weight);
        vs.setHeight(height);

        vs.calculateBMI();

        double heightInMeters = height / 100.0;
        double expected = weight / (heightInMeters * heightInMeters);
        // Rounded to 2 decimal places as per implementation
        double expectedRounded = Math.round(expected * 100.0) / 100.0;

        Assertions.assertThat(vs.getBmi())
                .isNotNull()
                .isCloseTo(expectedRounded, Offset.offset(0.01));
    }

    /**
     * BMI is always positive for valid inputs.
     */
    @Property(tries = 200)
    void bmiIsAlwaysPositive(
            @ForAll @DoubleRange(min = 1.0, max = 300.0) double weight,
            @ForAll @DoubleRange(min = 50.0, max = 250.0) double height) {

        VitalSigns vs = new VitalSigns();
        vs.setWeight(weight);
        vs.setHeight(height);
        vs.calculateBMI();

        Assertions.assertThat(vs.getBmi()).isGreaterThan(0.0);
    }

    /**
     * BMI is not calculated when weight or height is null.
     */
    @Example
    void bmiIsNullWhenWeightIsNull() {
        VitalSigns vs = new VitalSigns();
        vs.setHeight(170.0);
        vs.calculateBMI();
        Assertions.assertThat(vs.getBmi()).isNull();
    }

    @Example
    void bmiIsNullWhenHeightIsNull() {
        VitalSigns vs = new VitalSigns();
        vs.setWeight(70.0);
        vs.calculateBMI();
        Assertions.assertThat(vs.getBmi()).isNull();
    }

    /**
     * Feature: clinical-service, Property 4: Vital Signs Validation Respects Physiological Ranges
     * 
     * isValid() returns true if and only if ALL values are within their ranges:
     * - systolic: 50-250 mmHg
     * - diastolic: 30-150 mmHg
     * - heartRate: 20-250 bpm
     * - temperature: 30.0-45.0 °C
     * - oxygenSaturation: 0-100 %
     */
    @Property(tries = 500)
    void validationRespectsPhysiologicalRanges(
            @ForAll @IntRange(min = 0, max = 300) int systolic,
            @ForAll @IntRange(min = 0, max = 200) int diastolic,
            @ForAll @IntRange(min = 0, max = 300) int heartRate,
            @ForAll @DoubleRange(min = 0.0, max = 50.0) double temperature,
            @ForAll @IntRange(min = 0, max = 150) int oxygenSaturation) {

        VitalSigns vs = new VitalSigns();
        vs.setSystolicPressure(systolic);
        vs.setDiastolicPressure(diastolic);
        vs.setHeartRate(heartRate);
        vs.setTemperature(temperature);
        vs.setOxygenSaturation(oxygenSaturation);

        boolean isValid = vs.isValid();

        boolean expectedValid =
                systolic >= 50 && systolic <= 250 &&
                diastolic >= 30 && diastolic <= 150 &&
                heartRate >= 20 && heartRate <= 250 &&
                temperature >= 30.0 && temperature <= 45.0 &&
                oxygenSaturation >= 0 && oxygenSaturation <= 100;

        Assertions.assertThat(isValid).isEqualTo(expectedValid);
    }

    /**
     * Valid vital signs in normal ranges always pass validation.
     */
    @Example
    void normalVitalSignsAreValid() {
        VitalSigns vs = new VitalSigns();
        vs.setSystolicPressure(120);
        vs.setDiastolicPressure(80);
        vs.setHeartRate(72);
        vs.setTemperature(36.6);
        vs.setOxygenSaturation(98);

        Assertions.assertThat(vs.isValid()).isTrue();
    }

    /**
     * Null fields make validation return false.
     */
    @Example
    void nullFieldsMakeValidationFail() {
        VitalSigns vs = new VitalSigns();
        vs.setSystolicPressure(120);
        // diastolicPressure is null
        vs.setHeartRate(72);
        vs.setTemperature(36.6);
        vs.setOxygenSaturation(98);

        Assertions.assertThat(vs.isValid()).isFalse();
    }
}
