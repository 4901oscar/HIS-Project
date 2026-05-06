package com.medframe.clinical.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for laboratory workflow state transitions in Appointment entity.
 * Tests the five new state transition methods added for lab sample workflow.
 * 
 * Requirements: 1.4, 1.5, 9.1, 9.2
 */
class LabWorkflowStateTransitionTest {

    @Test
    void collectLabSamples_shouldTransitionFromLabSampleCollectionToPending() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_SAMPLE_COLLECTION);
        
        // When
        appointment.collectLabSamples();
        
        // Then
        assertThat(appointment.getStatus())
            .isEqualTo(Appointment.AppointmentStatus.LAB_SAMPLE_PENDING);
    }

    @Test
    void collectLabSamples_shouldThrowExceptionWhenNotInLabSampleCollection() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_PROCESSING);
        
        // When/Then
        assertThatThrownBy(() -> appointment.collectLabSamples())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("LAB_SAMPLE_COLLECTION")
            .hasMessageContaining("LAB_PROCESSING");
    }

    @Test
    void acceptLabSamples_shouldTransitionFromPendingToProcessing() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_SAMPLE_PENDING);
        
        // When
        appointment.acceptLabSamples();
        
        // Then
        assertThat(appointment.getStatus())
            .isEqualTo(Appointment.AppointmentStatus.LAB_PROCESSING);
    }

    @Test
    void acceptLabSamples_shouldThrowExceptionWhenNotInPending() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_SAMPLE_COLLECTION);
        
        // When/Then
        assertThatThrownBy(() -> appointment.acceptLabSamples())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("LAB_SAMPLE_PENDING")
            .hasMessageContaining("LAB_SAMPLE_COLLECTION");
    }

    @Test
    void rejectLabSamples_shouldTransitionFromPendingBackToCollection() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_SAMPLE_PENDING);
        
        // When
        appointment.rejectLabSamples();
        
        // Then
        assertThat(appointment.getStatus())
            .isEqualTo(Appointment.AppointmentStatus.LAB_SAMPLE_COLLECTION);
    }

    @Test
    void rejectLabSamples_shouldThrowExceptionWhenNotInPending() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_PROCESSING);
        
        // When/Then
        assertThatThrownBy(() -> appointment.rejectLabSamples())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("LAB_SAMPLE_PENDING")
            .hasMessageContaining("LAB_PROCESSING");
    }

    @Test
    void completeLabProcessing_shouldTransitionFromProcessingToResultsReady() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_PROCESSING);
        
        // When
        appointment.completeLabProcessing();
        
        // Then
        assertThat(appointment.getStatus())
            .isEqualTo(Appointment.AppointmentStatus.LAB_RESULTS_READY);
    }

    @Test
    void completeLabProcessing_shouldThrowExceptionWhenNotInProcessing() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_SAMPLE_PENDING);
        
        // When/Then
        assertThatThrownBy(() -> appointment.completeLabProcessing())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("LAB_PROCESSING")
            .hasMessageContaining("LAB_SAMPLE_PENDING");
    }

    @Test
    void sendLabResultsToDoctor_shouldTransitionFromResultsReadyToConsultation() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_RESULTS_READY);
        
        // When
        appointment.sendLabResultsToDoctor();
        
        // Then
        assertThat(appointment.getStatus())
            .isEqualTo(Appointment.AppointmentStatus.CONSULTATION);
    }

    @Test
    void sendLabResultsToDoctor_shouldThrowExceptionWhenNotInResultsReady() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_PROCESSING);
        
        // When/Then
        assertThatThrownBy(() -> appointment.sendLabResultsToDoctor())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("LAB_RESULTS_READY")
            .hasMessageContaining("LAB_PROCESSING");
    }

    @Test
    void labWorkflow_shouldFollowCompleteHappyPath() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_SAMPLE_COLLECTION);
        
        // When - Execute complete workflow
        appointment.collectLabSamples();
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.LAB_SAMPLE_PENDING);
        
        appointment.acceptLabSamples();
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.LAB_PROCESSING);
        
        appointment.completeLabProcessing();
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.LAB_RESULTS_READY);
        
        appointment.sendLabResultsToDoctor();
        
        // Then
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.CONSULTATION);
    }

    @Test
    void labWorkflow_shouldHandleSampleRejectionPath() {
        // Given
        Appointment appointment = new Appointment();
        appointment.setStatus(Appointment.AppointmentStatus.LAB_SAMPLE_COLLECTION);
        
        // When - Collect samples, then reject them
        appointment.collectLabSamples();
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.LAB_SAMPLE_PENDING);
        
        appointment.rejectLabSamples();
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.LAB_SAMPLE_COLLECTION);
        
        // Then - Should be able to collect again
        appointment.collectLabSamples();
        assertThat(appointment.getStatus()).isEqualTo(Appointment.AppointmentStatus.LAB_SAMPLE_PENDING);
    }
}
