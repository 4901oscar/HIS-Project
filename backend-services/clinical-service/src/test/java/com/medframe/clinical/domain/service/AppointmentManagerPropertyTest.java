package com.medframe.clinical.domain.service;

import com.medframe.clinical.domain.model.Appointment;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

/**
 * Property-Based Tests for AppointmentManager.
 * generateDailySlots() is a pure function — ideal for PBT.
 * State transition tests use the Appointment domain entity directly.
 */
class AppointmentManagerPropertyTest {

    private final AppointmentManager manager = new AppointmentManager(null, null, null, null, null);

    /**
     * Feature: clinical-service, Property 6: Daily Slots Generation Produces Exactly 18 Slots
     * 
     * generateDailySlots() must always return exactly 18 slots,
     * starting at 08:00, ending at 16:30, each 30 minutes apart.
     */
    @Example
    void dailySlotsProducesExactly18Slots() {
        List<LocalTime> slots = manager.generateDailySlots();

        Assertions.assertThat(slots).hasSize(18);
        Assertions.assertThat(slots.get(0)).isEqualTo(LocalTime.of(8, 0));
        Assertions.assertThat(slots.get(17)).isEqualTo(LocalTime.of(16, 30));
    }

    @Example
    void eachSlotIs30MinutesApart() {
        List<LocalTime> slots = manager.generateDailySlots();

        for (int i = 0; i < slots.size() - 1; i++) {
            Assertions.assertThat(slots.get(i + 1))
                    .isEqualTo(slots.get(i).plusMinutes(30));
        }
    }

    @Example
    void allSlotsAreBeforeEndTime() {
        List<LocalTime> slots = manager.generateDailySlots();

        slots.forEach(slot ->
                Assertions.assertThat(slot).isBefore(LocalTime.of(17, 0)));
    }

    @Example
    void firstSlotIsStartTime() {
        List<LocalTime> slots = manager.generateDailySlots();
        Assertions.assertThat(slots.get(0)).isEqualTo(AppointmentManager.START_TIME);
    }

    /**
     * Feature: clinical-service, Property 8: Appointment State Transitions are Valid
     * 
     * Valid: SCHEDULED→ACTIVE, ACTIVE→COMPLETED, SCHEDULED→CANCELLED, ACTIVE→CANCELLED
     * Invalid: COMPLETED→anything, CANCELLED→anything
     */
    @Example
    void scheduledCanBeActivated() {
        Appointment a = new Appointment();
        a.activate();
        Assertions.assertThat(a.getStatus()).isEqualTo(Appointment.AppointmentStatus.ACTIVE);
    }

    @Example
    void activeCanBeCompleted() {
        Appointment a = new Appointment();
        a.activate();
        a.complete();
        Assertions.assertThat(a.getStatus()).isEqualTo(Appointment.AppointmentStatus.COMPLETED);
    }

    @Example
    void scheduledCanBeCancelled() {
        Appointment a = new Appointment();
        a.cancel();
        Assertions.assertThat(a.getStatus()).isEqualTo(Appointment.AppointmentStatus.CANCELLED);
    }

    @Example
    void activeCanBeCancelled() {
        Appointment a = new Appointment();
        a.activate();
        a.cancel();
        Assertions.assertThat(a.getStatus()).isEqualTo(Appointment.AppointmentStatus.CANCELLED);
    }

    @Example
    void completedCannotBeCancelled() {
        Appointment a = new Appointment();
        a.activate();
        a.complete();

        Assertions.assertThatThrownBy(a::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Example
    void completedCannotBeActivated() {
        Appointment a = new Appointment();
        a.activate();
        a.complete();

        Assertions.assertThatThrownBy(a::activate)
                .isInstanceOf(IllegalStateException.class);
    }

    @Example
    void cancelledCannotBeActivated() {
        Appointment a = new Appointment();
        a.cancel();

        Assertions.assertThatThrownBy(a::activate)
                .isInstanceOf(IllegalStateException.class);
    }

    @Example
    void activeCannotBeActivatedAgain() {
        Appointment a = new Appointment();
        a.activate();

        Assertions.assertThatThrownBy(a::activate)
                .isInstanceOf(IllegalStateException.class);
    }
}
