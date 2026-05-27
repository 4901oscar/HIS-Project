package com.medframe.clinical.infrastructure.rest.controller;

import com.medframe.clinical.domain.model.Appointment;
import com.medframe.clinical.domain.model.Appointment.AppointmentStatus;
import net.jqwik.api.*;
import org.assertj.core.api.Assertions;

import java.lang.reflect.Method;

/**
 * Preservation Property Tests for Cashier Duplicate Appointment Fix
 * 
 * **IMPORTANT**: These tests follow the observation-first methodology.
 * They capture the CURRENT behavior of non-buggy inputs on UNFIXED code.
 * 
 * **EXPECTED OUTCOME ON UNFIXED CODE**: All tests PASS (confirms baseline behavior)
 * **EXPECTED OUTCOME ON FIXED CODE**: All tests PASS (confirms no regressions)
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8**
 * 
 * Property 2: Preservation - Non-Buggy Filter Behavior Unchanged
 * 
 * For any appointment and queue filter combination where the bug condition does NOT hold
 * (i.e., queue filter is not "payment", or status is not PENDING_LAB_PAYMENT/PENDING_PHARMACY_PAYMENT),
 * the fixed matchesQueueFilter function SHALL produce exactly the same result as the
 * original function, preserving all existing filter behaviors.
 */
class AppointmentQueueFilterPreservationTest {

    /**
     * Preservation Test 1: PENDING_PAYMENT still included in payment queue
     * 
     * **Validates: Requirement 3.1**
     * 
     * Observation: matchesQueueFilter(appointment with PENDING_PAYMENT, "payment") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 1: PENDING_PAYMENT included in payment queue")
    void paymentQueue_shouldIncludePendingPayment(
            @ForAll("paymentFilterVariations") String queueFilter) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(AppointmentStatus.PENDING_PAYMENT);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, queueFilter);
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("PENDING_PAYMENT appointments should be included in payment queue")
            .isTrue();
    }
    
    /**
     * Preservation Test 2: SCHEDULED still included in payment queue
     * 
     * **Validates: Requirement 3.2**
     * 
     * Observation: matchesQueueFilter(appointment with SCHEDULED, "payment") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 2: SCHEDULED included in payment queue")
    void paymentQueue_shouldIncludeScheduled(
            @ForAll("paymentFilterVariations") String queueFilter) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(AppointmentStatus.SCHEDULED);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, queueFilter);
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("SCHEDULED appointments should be included in payment queue")
            .isTrue();
    }
    
    /**
     * Preservation Test 3: Lab queue filter unchanged
     * 
     * **Validates: Requirement 3.3**
     * 
     * Observation: matchesQueueFilter(appointment with LABORATORY, "lab") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 3: LABORATORY included in lab queue")
    void labQueue_shouldIncludeLaboratory(
            @ForAll("labFilterVariations") String queueFilter) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(AppointmentStatus.LABORATORY);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, queueFilter);
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("LABORATORY appointments should be included in lab queue")
            .isTrue();
    }
    
    /**
     * Preservation Test 4: Pharmacy queue filter unchanged
     * 
     * **Validates: Requirement 3.4**
     * 
     * Observation: matchesQueueFilter(appointment with PHARMACY, "pharmacy") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 4: PHARMACY included in pharmacy queue")
    void pharmacyQueue_shouldIncludePharmacy() throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(AppointmentStatus.PHARMACY);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, "pharmacy");
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("PHARMACY appointments should be included in pharmacy queue")
            .isTrue();
    }
    
    /**
     * Preservation Test 5: Triage queue filter unchanged
     * 
     * **Validates: Requirement 3.5**
     * 
     * Observation: matchesQueueFilter(appointment with VITAL_SIGNS, "triage") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 5: VITAL_SIGNS included in triage queue")
    void triageQueue_shouldIncludeVitalSigns() throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(AppointmentStatus.VITAL_SIGNS);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, "triage");
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("VITAL_SIGNS appointments should be included in triage queue")
            .isTrue();
    }
    
    /**
     * Preservation Test 6: Admission queue filter unchanged
     * 
     * **Validates: Requirement 3.6**
     * 
     * Observation: matchesQueueFilter(appointment with PENDING_PAYMENT/SCHEDULED, "admission") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 6: PENDING_PAYMENT and SCHEDULED included in admission queue")
    void admissionQueue_shouldIncludePendingPaymentAndScheduled(
            @ForAll("admissionQueueStatuses") AppointmentStatus status) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(status);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, "admission");
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("PENDING_PAYMENT and SCHEDULED appointments should be included in admission queue")
            .isTrue();
    }
    
    /**
     * Preservation Test 7: PENDING_LAB_PAYMENT not in lab queue
     * 
     * **Validates: Requirement 3.7**
     * 
     * Observation: matchesQueueFilter(appointment with PENDING_LAB_PAYMENT, "lab") returns FALSE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 7: PENDING_LAB_PAYMENT excluded from lab queue")
    void labQueue_shouldExcludePendingLabPayment(
            @ForAll("labFilterVariations") String queueFilter) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(AppointmentStatus.PENDING_LAB_PAYMENT);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, queueFilter);
        
        // Assert: Should return FALSE (preserved behavior)
        Assertions.assertThat(result)
            .as("PENDING_LAB_PAYMENT appointments should NOT be included in lab queue")
            .isFalse();
    }
    
    /**
     * Preservation Test 8: PENDING_PHARMACY_PAYMENT not in pharmacy queue
     * 
     * **Validates: Requirement 3.8**
     * 
     * Observation: matchesQueueFilter(appointment with PENDING_PHARMACY_PAYMENT, "pharmacy") returns FALSE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 8: PENDING_PHARMACY_PAYMENT excluded from pharmacy queue")
    void pharmacyQueue_shouldExcludePendingPharmacyPayment() throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(AppointmentStatus.PENDING_PHARMACY_PAYMENT);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, "pharmacy");
        
        // Assert: Should return FALSE (preserved behavior)
        Assertions.assertThat(result)
            .as("PENDING_PHARMACY_PAYMENT appointments should NOT be included in pharmacy queue")
            .isFalse();
    }
    
    /**
     * Preservation Test 9: Null filter returns true for all statuses
     * 
     * **Validates: Edge case handling**
     * 
     * Observation: matchesQueueFilter(appointment with any status, null) returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 9: Null filter returns true for all statuses")
    void nullFilter_shouldReturnTrueForAllStatuses(
            @ForAll("allAppointmentStatuses") AppointmentStatus status) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(status);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, null);
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("Null filter should return true for all appointment statuses")
            .isTrue();
    }
    
    /**
     * Preservation Test 10: Empty filter returns true for all statuses
     * 
     * **Validates: Edge case handling**
     * 
     * Observation: matchesQueueFilter(appointment with any status, "") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 10: Empty filter returns true for all statuses")
    void emptyFilter_shouldReturnTrueForAllStatuses(
            @ForAll("allAppointmentStatuses") AppointmentStatus status) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(status);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, "");
        
        // Assert: Should return TRUE (preserved behavior)
        Assertions.assertThat(result)
            .as("Empty filter should return true for all appointment statuses")
            .isTrue();
    }
    
    /**
     * Preservation Test 11: Unknown filter returns true (default behavior)
     * 
     * **Validates: Default case handling**
     * 
     * Observation: matchesQueueFilter(appointment with any status, "unknown") returns TRUE
     * Expected: This behavior MUST be preserved after the fix
     */
    @Property(tries = 50)
    @Label("Preservation 11: Unknown filter returns true (default behavior)")
    void unknownFilter_shouldReturnTrue(
            @ForAll("allAppointmentStatuses") AppointmentStatus status,
            @ForAll("unknownFilters") String queueFilter) throws Exception {
        
        // Arrange
        Appointment appointment = createAppointment(status);
        
        // Act
        boolean result = invokeMatchesQueueFilter(appointment, queueFilter);
        
        // Assert: Should return TRUE (preserved behavior - default case)
        Assertions.assertThat(result)
            .as("Unknown filter '%s' should return true (default behavior)", queueFilter)
            .isTrue();
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Creates a test appointment with the specified status
     */
    private Appointment createAppointment(AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setId("test-appt-" + status.name());
        appointment.setPatientId("test-patient");
        appointment.setDoctorId("test-doctor");
        appointment.setStatus(status);
        return appointment;
    }
    
    /**
     * Invokes the private matchesQueueFilter method via reflection
     */
    private boolean invokeMatchesQueueFilter(Appointment appointment, String queueFilter) throws Exception {
        AppointmentController controller = new AppointmentController(
            null, null, null, null, null, null, null, null, null, null, null
        );
        
        Method method = AppointmentController.class.getDeclaredMethod(
            "matchesQueueFilter", Appointment.class, String.class
        );
        method.setAccessible(true);
        
        return (boolean) method.invoke(controller, appointment, queueFilter);
    }
    
    // ==================== Providers ====================
    
    /**
     * Provides case-insensitive variations of the "payment" filter
     */
    @Provide
    Arbitrary<String> paymentFilterVariations() {
        return Arbitraries.of("payment", "PAYMENT", "Payment");
    }
    
    /**
     * Provides case-insensitive variations of the "lab" filter
     */
    @Provide
    Arbitrary<String> labFilterVariations() {
        return Arbitraries.of("lab", "LAB", "Lab", "laboratory", "LABORATORY", "Laboratory");
    }
    
    /**
     * Provides appointment statuses that should be in admission queue
     */
    @Provide
    Arbitrary<AppointmentStatus> admissionQueueStatuses() {
        return Arbitraries.of(
            AppointmentStatus.PENDING_PAYMENT,
            AppointmentStatus.SCHEDULED
        );
    }
    
    /**
     * Provides all possible appointment statuses for comprehensive testing
     */
    @Provide
    Arbitrary<AppointmentStatus> allAppointmentStatuses() {
        return Arbitraries.of(AppointmentStatus.values());
    }
    
    /**
     * Provides unknown/invalid filter values to test default behavior
     */
    @Provide
    Arbitrary<String> unknownFilters() {
        return Arbitraries.of(
            "unknown",
            "invalid",
            "xyz",
            "test",
            "random"
        );
    }
}
