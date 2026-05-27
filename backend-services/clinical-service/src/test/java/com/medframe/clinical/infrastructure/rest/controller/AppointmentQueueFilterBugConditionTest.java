package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Appointment.AppointmentStatus;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;

import java.lang.reflect.Method;

/**
 * Bug Condition Exploration Property Test for Cashier Duplicate Appointment Fix
 * 
 * **CRITICAL**: This test is EXPECTED TO FAIL on unfixed code.
 * Failure confirms the bug exists: matchesQueueFilter incorrectly returns true
 * when it should return false for PENDING_LAB_PAYMENT and PENDING_PHARMACY_PAYMENT
 * with "payment" filter.
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
 * 
 * Bug Condition: isBugCondition(input) where:
 *   - input.queueFilter = "payment" (case-insensitive)
 *   - AND (input.appointment.status = PENDING_LAB_PAYMENT 
 *          OR input.appointment.status = PENDING_PHARMACY_PAYMENT)
 * 
 * Expected Behavior: matchesQueueFilter SHALL return false for bug condition inputs
 * 
 * Current Behavior (BUG): matchesQueueFilter returns true (causing duplicate display)
 */
class AppointmentQueueFilterBugConditionTest {

    /**
     * Property 1: Bug Condition - Payment Queue Excludes Additional Service Payments
     * 
     * **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
     * 
     * For any appointment where the status is PENDING_LAB_PAYMENT or PENDING_PHARMACY_PAYMENT
     * and the queue filter is "payment" (case-insensitive), the matchesQueueFilter function
     * SHALL return false, excluding the appointment from the consultation payment queue.
     * 
     * **EXPECTED OUTCOME ON UNFIXED CODE**: This test will FAIL because the current
     * implementation incorrectly returns true, causing appointments to appear in both
     * the consultation payment queue and their respective service queues.
     * 
     * **EXPECTED OUTCOME ON FIXED CODE**: This test will PASS, confirming the bug is fixed.
     */
    @Property(tries = 100)
    @Label("Property 1: Bug Condition - Payment queue excludes PENDING_LAB_PAYMENT and PENDING_PHARMACY_PAYMENT")
    void paymentQueueFilter_shouldExcludeAdditionalServicePayments(
            @ForAll("bugConditionStatuses") AppointmentStatus status,
            @ForAll("paymentFilterVariations") String queueFilter) throws Exception {
        
        // Arrange: Create appointment with bug condition status
        Appointment appointment = new Appointment();
        appointment.setId("test-appt-" + status.name());
        appointment.setPatientId("test-patient");
        appointment.setDoctorId("test-doctor");
        appointment.setStatus(status);
        
        // Create controller instance to access private method via reflection
        AppointmentController controller = new AppointmentController(
            null, null, null, null, null, null, null, null, null, null, null
        );
        
        // Access private matchesQueueFilter method via reflection
        Method method = AppointmentController.class.getDeclaredMethod(
            "matchesQueueFilter", Appointment.class, String.class
        );
        method.setAccessible(true);
        
        // Act: Invoke matchesQueueFilter
        boolean result = (boolean) method.invoke(controller, appointment, queueFilter);
        
        // Assert: Expected behavior - should return FALSE
        // Bug manifestation: currently returns TRUE (causing duplicate display)
        Assertions.assertThat(result)
            .as("Bug Condition: matchesQueueFilter(appointment with %s, '%s') should return FALSE " +
                "to exclude from payment queue. Currently returns TRUE (BUG).",
                status.name(), queueFilter)
            .describedAs("Counterexample found: status=%s, queueFilter='%s' -> result=%s (expected: false)",
                status.name(), queueFilter, result)
            .isFalse();
    }
    
    /**
     * Provides the two appointment statuses that trigger the bug condition:
     * - PENDING_LAB_PAYMENT: Appointment waiting for lab payment
     * - PENDING_PHARMACY_PAYMENT: Appointment waiting for pharmacy payment
     */
    @Provide
    Arbitrary<AppointmentStatus> bugConditionStatuses() {
        return Arbitraries.of(
            AppointmentStatus.PENDING_LAB_PAYMENT,
            AppointmentStatus.PENDING_PHARMACY_PAYMENT
        );
    }
    
    /**
     * Provides case-insensitive variations of the "payment" filter:
     * - "payment" (lowercase)
     * - "PAYMENT" (uppercase)
     * - "Payment" (mixed case)
     * 
     * All variations should behave identically due to toLowerCase() in implementation.
     */
    @Provide
    Arbitrary<String> paymentFilterVariations() {
        return Arbitraries.of("payment", "PAYMENT", "Payment");
    }
}
