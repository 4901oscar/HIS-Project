# Implementation Plan: Appointment State Machine Redesign

## Overview

This implementation plan expands the appointment state machine from 5 states to 13 states to support the complete hospital workflow including payment validations, triage, consultation, laboratory, pharmacy, and completion. The implementation follows Domain-Driven Design principles with business logic in the domain model and infrastructure concerns separated into appropriate layers.

## Tasks

- [x] 1. Update domain model with expanded state machine
  - [x] 1.1 Expand AppointmentStatus enum to 13 states
    - Add PENDING_PAYMENT, VITAL_SIGNS, PENDING_LAB_PAYMENT, LABORATORY, RE_EVALUATION, PENDING_PHARMACY_PAYMENT, PHARMACY states to existing enum
    - Update Appointment.java in domain model package
    - _Requirements: 1.1-1.13_
  
  - [x] 1.2 Add invoice tracking fields to Appointment entity
    - Add labInvoiceId and pharmacyInvoiceId fields
    - Update JPA entity annotations
    - _Requirements: 2.1, 3.1_
  
  - [x] 1.3 Implement state transition methods in Appointment domain model
    - Implement confirmPayment() for PENDING_PAYMENT → SCHEDULED
    - Implement activate() for SCHEDULED → ACTIVE
    - Implement startVitalSigns() for ACTIVE → VITAL_SIGNS
    - Implement completeVitalSigns() for VITAL_SIGNS → CONSULTATION
    - Implement registerConsultation() for CONSULTATION → (COMPLETED | PENDING_PHARMACY_PAYMENT | PENDING_LAB_PAYMENT)
    - Implement confirmLabPayment() for PENDING_LAB_PAYMENT → LABORATORY
    - Implement completeLab() for LABORATORY → RE_EVALUATION
    - Implement completeReEvaluation() for RE_EVALUATION → (COMPLETED | PENDING_PHARMACY_PAYMENT)
    - Implement confirmPharmacyPayment() for PENDING_PHARMACY_PAYMENT → PHARMACY
    - Implement dispenseMedication() for PHARMACY → COMPLETED
    - Update cancel() to work from any non-terminal state
    - Update markAsMissed() for SCHEDULED → MISSED
    - _Requirements: 3.1, 4.1-4.4, 5.1-5.4, 6.1-6.3, 7.1-7.4, 8.1-8.4, 9.1-9.4, 10.1-10.4, 11.1-11.12, 12.1-12.3, 19.1-19.5_
  
  - [x] 1.4 Implement query methods in Appointment domain model
    - Implement canBeCancelled() method
    - Implement isPendingPayment() method
    - Implement isTerminal() method
    - Implement isPriority() method for RE_EVALUATION state
    - _Requirements: 9.1, 11.1-11.2, 14.1-14.4_
  
  - [ ]* 1.5 Write unit tests for Appointment state transitions
    - Test each valid state transition (13 test cases)
    - Test each invalid state transition throws IllegalStateException
    - Test registerConsultation() routing logic (lab orders, prescription, no orders)
    - Test completeReEvaluation() routing logic
    - Test query methods (canBeCancelled, isPendingPayment, isTerminal, isPriority)
    - Test terminal state protection
    - _Requirements: 14.1-14.4, 19.1-19.5_

- [x] 2. Create database migration for schema changes
  - [x] 2.1 Create Flyway migration script for appointments table
    - Add lab_invoice_id VARCHAR(255) column
    - Add pharmacy_invoice_id VARCHAR(255) column
    - Expand status enum to include 8 new states
    - Ensure backward compatibility (existing appointments remain in current states)
    - _Requirements: 1.1-1.13, 20.1-20.6_
  
  - [x] 2.2 Create appointment_state_transitions audit table
    - Create table with id, appointment_id, from_state, to_state, transitioned_by, transitioned_at, notes columns
    - Add foreign key constraint to appointments table
    - Add indexes on appointment_id and transitioned_at
    - _Requirements: 14.2_
  
  - [ ]* 2.3 Write integration tests for database migrations
    - Test migration runs successfully
    - Test existing appointments remain in current states
    - Test new columns are nullable
    - Test enum values are correctly added
    - _Requirements: 20.1-20.6_

- [x] 3. Implement payment validation service
  - [x] 3.1 Create PaymentValidator interface and implementation
    - Define PaymentValidator interface with validatePayment() method
    - Create PaymentValidationResult class with factory methods
    - Create PaymentValidationError enum
    - Implement BillingServicePaymentValidator with REST client integration
    - _Requirements: 3.3, 4.4, 8.2, 8.4, 10.2, 10.4, 14.4, 18.1-18.5_
  
  - [x] 3.2 Implement circuit breaker and resilience patterns
    - Add @CircuitBreaker annotation to validatePayment() method
    - Implement fallback method for service unavailability
    - Configure retry policy in application.yml
    - Handle compensation case (null invoiceId) with warning
    - _Requirements: 18.4-18.5_
  
  - [ ]* 3.3 Write unit tests for PaymentValidator
    - Test validation with PAID invoice returns allowed
    - Test validation with PENDING invoice returns denied
    - Test validation with CANCELLED invoice returns denied
    - Test validation with null invoiceId returns allowed with warning
    - Test validation with service timeout returns denied
    - Test validation with service error triggers circuit breaker
    - _Requirements: 18.1-18.5_

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Create application layer use cases
  - [x] 5.1 Implement ManageAppointmentUseCase for state transitions
    - Implement activateAppointment() with payment validation
    - Implement startVitalSigns() with locking logic
    - Implement confirmPayment() for consultation fee
    - Implement confirmLabPayment() for lab tests
    - Implement confirmPharmacyPayment() for medications
    - Implement completeLab() for lab results
    - Implement dispenseMedication() for pharmacy
    - Implement cancelAppointment() with state validation
    - _Requirements: 3.1-3.3, 4.1-4.4, 5.1-5.4, 8.1-8.4, 10.1-10.4, 11.1-11.12_
  
  - [x] 5.2 Update CreateAppointmentUseCase to set initial state based on payment
    - Add hasPaid parameter to createAppointment() method
    - Set initial state to PENDING_PAYMENT if hasPaid=false
    - Set initial state to SCHEDULED if hasPaid=true
    - _Requirements: 2.1-2.3_
  
  - [ ]* 5.3 Write unit tests for ManageAppointmentUseCase
    - Test activateAppointment() with valid payment
    - Test activateAppointment() with invalid payment throws exception
    - Test startVitalSigns() transitions correctly
    - Test confirmPayment() validates invoice
    - Test confirmLabPayment() validates lab invoice
    - Test confirmPharmacyPayment() validates pharmacy invoice
    - Test cancelAppointment() from various states
    - _Requirements: 3.1-3.3, 4.1-4.4, 14.1-14.4_

- [x] 6. Create REST API endpoints for state transitions
  - [x] 6.1 Implement payment confirmation endpoints
    - POST /api/clinical/appointments/{id}/confirm-payment for consultation fee
    - POST /api/clinical/appointments/{id}/confirm-lab-payment for lab tests
    - POST /api/clinical/appointments/{id}/confirm-pharmacy-payment for medications
    - Add request validation and error handling
    - _Requirements: 3.1-3.3, 8.1-8.4, 10.1-10.4, 15.1, 15.6, 15.8_
  
  - [x] 6.2 Implement triage workflow endpoints
    - PATCH /api/clinical/appointments/{id}/activate for patient arrival
    - PATCH /api/clinical/appointments/{id}/start-vital-signs for locking transition
    - Update POST /api/clinical/vital-signs to transition to CONSULTATION
    - _Requirements: 4.1-4.4, 5.1-5.4, 6.1-6.3, 15.2, 15.3, 15.4_
  
  - [x] 6.3 Implement consultation and lab workflow endpoints
    - Update POST /api/clinical/consultations to determine next state based on orders
    - PATCH /api/clinical/appointments/{id}/complete-lab for lab completion
    - POST /api/clinical/consultations/{consultationId}/re-evaluate for re-evaluation
    - _Requirements: 7.1-7.4, 8.3, 9.1-9.4, 15.5, 15.7_
  
  - [x] 6.4 Implement pharmacy and cancellation endpoints
    - PATCH /api/clinical/appointments/{id}/dispense-medication for pharmacy completion
    - Update DELETE /api/clinical/appointments/{id} to work from any non-terminal state
    - _Requirements: 10.3, 11.1-11.12, 15.9, 15.10_
  
  - [ ]* 6.5 Write integration tests for REST API endpoints
    - Test each endpoint with valid state transitions
    - Test each endpoint with invalid state returns 400 Bad Request
    - Test payment validation integration returns 400 for unpaid invoices
    - Test service unavailability returns 503 Service Unavailable
    - Test concurrent access to VITAL_SIGNS returns 409 Conflict
    - Test error response format matches specification
    - _Requirements: 14.1-14.4, 15.1-15.10, 18.1-18.5_

- [x] 7. Create query endpoints for role-based views
  - [x] 7.1 Implement triage queue endpoints
    - GET /api/clinical/appointments/pending-triage returns ACTIVE and VITAL_SIGNS appointments
    - Add lock indicator for VITAL_SIGNS appointments
    - Include patient and doctor information in response
    - _Requirements: 13.3-13.4_
  
  - [x] 7.2 Implement doctor queue endpoints
    - GET /api/clinical/appointments/doctor returns CONSULTATION and RE_EVALUATION appointments
    - Sort RE_EVALUATION appointments first (priority)
    - Add isPriority flag to response for RE_EVALUATION appointments
    - _Requirements: 9.1-9.2, 13.5-13.6_
  
  - [x] 7.3 Implement payment queue endpoints
    - GET /api/clinical/appointments/payment-queue returns PENDING_PAYMENT, PENDING_LAB_PAYMENT, PENDING_PHARMACY_PAYMENT appointments
    - Add optional filter by payment type
    - Include invoice information in response
    - _Requirements: 13.1-13.2, 13.7-13.9_
  
  - [x] 7.4 Implement lab and pharmacy queue endpoints
    - GET /api/clinical/appointments/lab-queue returns LABORATORY appointments
    - GET /api/clinical/appointments/pharmacy-queue returns PHARMACY appointments
    - _Requirements: 13.10-13.11_
  
  - [ ]* 7.5 Write integration tests for query endpoints
    - Test each endpoint returns correct appointments by state
    - Test doctor queue sorts RE_EVALUATION first
    - Test payment queue filtering by payment type
    - Test response includes all required fields
    - _Requirements: 13.1-13.11_

- [ ] 8. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Update DTOs and request/response models
  - [x] 9.1 Update CreateAppointmentRequest DTO
    - Add hasPaid Boolean field
    - Update validation annotations
    - _Requirements: 2.1-2.3_
  
  - [x] 9.2 Update AppointmentResponse DTO
    - Add labInvoiceId field
    - Add pharmacyInvoiceId field
    - Add isPriority Boolean field
    - Update status field to include all 13 states
    - _Requirements: 1.1-1.13, 9.1_
  
  - [x] 9.3 Create payment confirmation request DTOs
    - Create ConfirmPaymentRequest with invoiceId field
    - Reuse for lab and pharmacy payment confirmations
    - _Requirements: 3.1-3.3, 8.1-8.4, 10.1-10.4_
  
  - [x] 9.4 Update ConsultationRequest DTO
    - Add hasLabOrders Boolean field
    - Add hasPrescription Boolean field
    - Update validation to ensure at least one field is set
    - _Requirements: 7.1-7.4, 17.1-17.5_
  
  - [x] 9.5 Create ReEvaluationRequest DTO
    - Create DTO with consultationId, labResultsReview, updatedDiagnosis, updatedTreatmentPlan, hasPrescription fields
    - Add validation annotations
    - _Requirements: 9.3-9.4_

- [x] 10. Implement exception handling and error responses
  - [x] 10.1 Create exception hierarchy
    - Create AppointmentStateMachineException base class
    - Create InvalidAppointmentStatusException for invalid state transitions
    - Create PaymentValidationException for payment failures
    - Create ServiceUnavailableException for billing service errors
    - Create AppointmentLockedException for concurrent access
    - _Requirements: 14.1_
  
  - [x] 10.2 Implement global exception handler
    - Map InvalidAppointmentStatusException to 400 Bad Request
    - Map PaymentValidationException to 400 Bad Request
    - Map ServiceUnavailableException to 503 Service Unavailable
    - Map AppointmentLockedException to 409 Conflict
    - Include error details in response (current state, valid states, error code)
    - _Requirements: 14.1-14.4, 18.4-18.5_
  
  - [ ]* 10.3 Write integration tests for error handling
    - Test invalid state transition returns 400 with descriptive message
    - Test unpaid invoice returns 400 with payment required message
    - Test billing service unavailable returns 503 with retry-after header
    - Test concurrent access returns 409 with lock holder information
    - Test error response format matches specification
    - _Requirements: 14.1-14.4, 18.4-18.5_

- [ ] 11. Implement state transition audit logging
  - [x] 11.1 Create AppointmentStateTransition entity
    - Create JPA entity for appointment_state_transitions table
    - Add fields: id, appointmentId, fromState, toState, transitionedBy, transitionedAt, notes
    - Add repository interface
    - _Requirements: 14.2_
  
  - [x] 11.2 Implement audit logging in use cases
    - Log all state transitions with timestamp and user information
    - Save to appointment_state_transitions table
    - Include transition notes when available
    - _Requirements: 14.2_
  
  - [ ]* 11.3 Write integration tests for audit logging
    - Test each state transition creates audit record
    - Test audit record includes correct from/to states
    - Test audit record includes user information
    - Test audit records can be queried by appointment ID
    - _Requirements: 14.2_

- [ ] 12. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 13. Update frontend components for triage workflow
  - [x] 13.1 Update TriagePendingPage component
    - Update API call to fetch appointments in ACTIVE and VITAL_SIGNS states
    - Add visual indicator for appointments in VITAL_SIGNS state (locked)
    - Disable "Capture Vital Signs" button for appointments already in VITAL_SIGNS state
    - Show which staff member is currently capturing vital signs
    - _Requirements: 5.1-5.4, 13.3-13.4, 16.1_
  
  - [x] 13.2 Update TriageVitalSignsCapture component
    - Add useEffect hook to call /appointments/{id}/start-vital-signs on component mount
    - Handle 409 Conflict error if appointment already locked
    - Show loading state while transitioning to VITAL_SIGNS
    - Redirect back to pending page if transition fails
    - Update save handler to transition to CONSULTATION state
    - _Requirements: 5.1-5.4, 6.1-6.3, 16.2_
  
  - [ ]* 13.3 Write component tests for triage workflow
    - Test TriagePendingPage displays ACTIVE and VITAL_SIGNS appointments
    - Test lock indicator appears for VITAL_SIGNS appointments
    - Test "Capture Vital Signs" button disabled for locked appointments
    - Test TriageVitalSignsCapture transitions to VITAL_SIGNS on mount
    - Test TriageVitalSignsCapture handles 409 Conflict error
    - _Requirements: 5.1-5.4, 13.3-13.4, 16.1-16.2_

- [x] 14. Update frontend components for doctor workflow
  - [x] 14.1 Update DoctorConsultation component
    - Update API call to fetch appointments in CONSULTATION and RE_EVALUATION states
    - Sort RE_EVALUATION appointments first
    - Add visual priority badge for RE_EVALUATION appointments
    - Update consultation form to include hasLabOrders and hasPrescription checkboxes
    - Remove automatic completion logic - let backend determine next state
    - _Requirements: 7.1-7.4, 9.1-9.4, 13.5-13.6, 16.3-16.5, 17.1-17.5_
  
  - [x] 14.2 Create ReEvaluation component for lab results review
    - Create new component for re-evaluation workflow
    - Display lab results and consultation history
    - Add form for updated diagnosis and treatment plan
    - Add hasPrescription checkbox
    - Call /consultations/{consultationId}/re-evaluate endpoint
    - _Requirements: 9.3-9.4_
  
  - [ ]* 14.3 Write component tests for doctor workflow
    - Test DoctorConsultation displays CONSULTATION and RE_EVALUATION appointments
    - Test RE_EVALUATION appointments sorted first with priority badge
    - Test consultation form includes hasLabOrders and hasPrescription fields
    - Test ReEvaluation component displays lab results
    - Test ReEvaluation form submits correctly
    - _Requirements: 7.1-7.4, 9.1-9.4, 13.5-13.6, 16.3-16.5_

- [x] 15. Update frontend components for admission workflow
  - [x] 15.1 Update ActivateAppointments component
    - Update API call to fetch appointments in PENDING_PAYMENT and SCHEDULED states
    - Add payment status indicator (PENDING_PAYMENT vs SCHEDULED)
    - Show "Confirm Payment" button for PENDING_PAYMENT appointments
    - Show "Activate" button for SCHEDULED appointments
    - Integrate payment validation before activation
    - Handle 400 Bad Request for unpaid invoices
    - Handle 503 Service Unavailable for billing service errors
    - _Requirements: 2.1-2.3, 3.1-3.3, 4.1-4.4, 13.1-13.2, 16.5-16.6, 18.1-18.5_
  
  - [x] 15.2 Update CreateAppointment component
    - Add hasPaid checkbox to appointment creation form
    - Update API call to include hasPaid parameter
    - Show confirmation message with correct initial state
    - _Requirements: 2.1-2.3_
  
  - [ ]* 15.3 Write component tests for admission workflow
    - Test ActivateAppointments displays PENDING_PAYMENT and SCHEDULED appointments
    - Test payment status indicator appears correctly
    - Test "Confirm Payment" button for PENDING_PAYMENT appointments
    - Test "Activate" button for SCHEDULED appointments
    - Test error handling for unpaid invoices
    - Test CreateAppointment includes hasPaid checkbox
    - _Requirements: 2.1-2.3, 3.1-3.3, 4.1-4.4, 13.1-13.2, 16.5-16.6_

- [x] 16. Create frontend components for payment, lab, and pharmacy queues
  - [x] 16.1 Create PaymentQueue component
    - Create component to display appointments in PENDING_PAYMENT, PENDING_LAB_PAYMENT, PENDING_PHARMACY_PAYMENT states
    - Add filter dropdown for payment type
    - Show "Confirm Payment" button for each appointment
    - Call appropriate payment confirmation endpoint based on state
    - Handle payment validation errors
    - _Requirements: 3.1-3.3, 8.1-8.4, 10.1-10.4, 13.7-13.9_
  
  - [x] 16.2 Create LabQueue component
    - Create component to display appointments in LABORATORY state
    - Show "Complete Lab" button for each appointment
    - Call /appointments/{id}/complete-lab endpoint
    - _Requirements: 8.3, 13.10_
  
  - [x] 16.3 Create PharmacyQueue component
    - Create component to display appointments in PHARMACY state
    - Show "Dispense Medication" button for each appointment
    - Call /appointments/{id}/dispense-medication endpoint
    - _Requirements: 10.3, 13.11_
  
  - [ ]* 16.4 Write component tests for queue components
    - Test PaymentQueue displays appointments in payment-pending states
    - Test PaymentQueue filter by payment type
    - Test LabQueue displays LABORATORY appointments
    - Test PharmacyQueue displays PHARMACY appointments
    - Test action buttons call correct endpoints
    - _Requirements: 3.1-3.3, 8.1-8.4, 10.1-10.4, 13.7-13.11_

- [ ] 17. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 18. Implement missed appointment detection
  - [x] 18.1 Create scheduled job for missed appointment detection
    - Create @Scheduled method to run every 5 minutes
    - Query appointments in SCHEDULED state where appointmentTime + 60 minutes < current time
    - Call markAsMissed() for each expired appointment
    - Log missed appointments for reporting
    - _Requirements: 12.1-12.3_
  
  - [ ]* 18.2 Write integration tests for missed appointment detection
    - Test scheduled job marks appointments as MISSED after 60 minutes
    - Test scheduled job only affects SCHEDULED appointments
    - Test scheduled job runs every 5 minutes
    - _Requirements: 12.1-12.3_

- [ ] 19. End-to-end testing for complete workflows
  - [ ]* 19.1 Write E2E test for happy path without lab or prescription
    - Create appointment with payment → SCHEDULED
    - Activate appointment → ACTIVE
    - Start vital signs → VITAL_SIGNS
    - Save vital signs → CONSULTATION
    - Register consultation (no orders) → COMPLETED
    - _Requirements: 2.1-2.3, 4.1-4.4, 5.1-5.4, 6.1-6.3, 7.1-7.4_
  
  - [ ]* 19.2 Write E2E test for happy path with lab and prescription
    - Create appointment with payment → SCHEDULED
    - Activate appointment → ACTIVE
    - Start vital signs → VITAL_SIGNS
    - Save vital signs → CONSULTATION
    - Register consultation (lab orders) → PENDING_LAB_PAYMENT
    - Confirm lab payment → LABORATORY
    - Complete lab tests → RE_EVALUATION
    - Re-evaluate (prescription) → PENDING_PHARMACY_PAYMENT
    - Confirm pharmacy payment → PHARMACY
    - Dispense medication → COMPLETED
    - _Requirements: 2.1-2.3, 4.1-4.4, 5.1-5.4, 6.1-6.3, 7.1-7.4, 8.1-8.4, 9.1-9.4, 10.1-10.4_
  
  - [ ]* 19.3 Write E2E test for payment required workflow
    - Create appointment without payment → PENDING_PAYMENT
    - Attempt to activate → 400 Bad Request
    - Confirm payment → SCHEDULED
    - Activate appointment → ACTIVE
    - Continue workflow normally
    - _Requirements: 2.1-2.3, 3.1-3.3, 4.1-4.4_
  
  - [ ]* 19.4 Write E2E test for billing service unavailable
    - Create appointment (billing service down) → SCHEDULED (invoiceId=null)
    - Activate appointment → ACTIVE (allowed with warning)
    - Continue workflow normally
    - _Requirements: 18.4-18.5_

- [ ] 20. Final checkpoint and documentation
  - [ ] 20.1 Run full test suite
    - Run all unit tests
    - Run all integration tests
    - Run all E2E tests
    - Verify test coverage meets goals (90% domain, 80% API)
    - _Requirements: All_
  
  - [ ] 20.2 Update API documentation
    - Document all new endpoints in OpenAPI/Swagger
    - Include request/response examples
    - Document error responses
    - _Requirements: 15.1-15.10_
  
  - [ ] 20.3 Create deployment checklist
    - Database migration steps
    - Configuration changes
    - Rollback procedure
    - Monitoring and alerting setup
    - _Requirements: 20.1-20.6_
  
  - [ ] 20.4 Final checkpoint - Ensure all tests pass
    - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at reasonable breaks
- Implementation uses Java with Spring Boot framework
- Frontend uses React with TypeScript
- Database migrations use Flyway
- Testing uses JUnit 5 for backend, Jest/React Testing Library for frontend
- Property-based testing is NOT used for this feature (state machine logic is deterministic)
