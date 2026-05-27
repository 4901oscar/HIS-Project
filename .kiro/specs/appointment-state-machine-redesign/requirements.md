# Requirements Document

## Introduction

This document specifies the requirements for redesigning the appointment state machine in the hospital workflow system. The current system supports only 5 appointment states (SCHEDULED, ACTIVE, COMPLETED, CANCELLED, MISSED), which is insufficient for managing the complete hospital workflow including payment validations, triage, consultation, laboratory, pharmacy, and completion.

The redesigned state machine will expand to 13 states to support the complete patient journey from appointment creation through discharge, with proper payment validations at each stage requiring payment.

## Glossary

- **Appointment_State_Machine**: The domain model component that manages appointment lifecycle transitions
- **Appointment**: A scheduled medical consultation with a doctor at a specific date and time
- **Patient**: A person receiving medical care in the hospital
- **Doctor**: A medical professional providing consultations
- **Triage_System**: The system component that captures vital signs
- **Consultation_System**: The system component where doctors register consultations
- **Laboratory_System**: The system component that processes lab orders
- **Pharmacy_System**: The system component that dispenses medications
- **Billing_Service**: The external service that manages invoices and payments
- **Invoice**: A billing document that must be paid before certain state transitions
- **Vital_Signs**: Medical measurements captured during triage (blood pressure, heart rate, temperature, etc.)
- **Lab_Order**: A doctor's request for laboratory tests
- **Prescription**: A doctor's order for medications
- **Admission_Staff**: Hospital personnel who create appointments and collect payments
- **Cashier**: Hospital personnel who process payments
- **QR_Code**: A scannable code that activates appointments

## Requirements

### Requirement 1: Expand Appointment Status Enum

**User Story:** As a developer, I want the appointment status enum to support 13 states, so that the system can track the complete hospital workflow.

#### Acceptance Criteria

1. THE Appointment_State_Machine SHALL support the PENDING_PAYMENT state
2. THE Appointment_State_Machine SHALL support the SCHEDULED state
3. THE Appointment_State_Machine SHALL support the ACTIVE state
4. THE Appointment_State_Machine SHALL support the VITAL_SIGNS state
5. THE Appointment_State_Machine SHALL support the CONSULTATION state
6. THE Appointment_State_Machine SHALL support the PENDING_LAB_PAYMENT state
7. THE Appointment_State_Machine SHALL support the LABORATORY state
8. THE Appointment_State_Machine SHALL support the RE_EVALUATION state
9. THE Appointment_State_Machine SHALL support the PENDING_PHARMACY_PAYMENT state
10. THE Appointment_State_Machine SHALL support the PHARMACY state
11. THE Appointment_State_Machine SHALL support the COMPLETED state
12. THE Appointment_State_Machine SHALL support the CANCELLED state
13. THE Appointment_State_Machine SHALL support the MISSED state

### Requirement 2: Appointment Creation with Payment Validation

**User Story:** As admission staff, I want appointments to be created in the correct initial state based on payment status, so that the workflow starts correctly.

#### Acceptance Criteria

1. WHEN an appointment is created without payment, THE Appointment_State_Machine SHALL set the initial state to PENDING_PAYMENT
2. WHEN an appointment is created with payment, THE Appointment_State_Machine SHALL set the initial state to SCHEDULED
3. WHEN a patient self-books an appointment through the web portal, THE Appointment_State_Machine SHALL set the initial state to SCHEDULED

### Requirement 3: Payment to Scheduled Transition

**User Story:** As a cashier, I want to transition appointments from PENDING_PAYMENT to SCHEDULED after payment, so that patients can proceed with their appointments.

#### Acceptance Criteria

1. WHEN payment is confirmed for a PENDING_PAYMENT appointment, THE Appointment_State_Machine SHALL transition the state to SCHEDULED
2. IF the invoice status is not PAID, THEN THE Appointment_State_Machine SHALL reject the transition from PENDING_PAYMENT to SCHEDULED
3. WHEN transitioning from PENDING_PAYMENT to SCHEDULED, THE Appointment_State_Machine SHALL validate the invoice exists in Billing_Service

### Requirement 4: Appointment Activation

**User Story:** As admission staff, I want to activate appointments when patients arrive, so that they can proceed to triage.

#### Acceptance Criteria

1. WHEN a SCHEDULED appointment is activated by QR_Code scan, THE Appointment_State_Machine SHALL transition the state to ACTIVE
2. WHEN a SCHEDULED appointment is activated manually, THE Appointment_State_Machine SHALL transition the state to ACTIVE
3. IF the appointment is not in SCHEDULED state, THEN THE Appointment_State_Machine SHALL reject activation
4. WHEN activating an appointment, THE Appointment_State_Machine SHALL validate payment status before transition

### Requirement 5: Vital Signs Capture Initiation

**User Story:** As triage staff, I want to immediately lock an appointment when I start capturing vital signs, so that no other staff member can access it simultaneously.

#### Acceptance Criteria

1. WHEN triage staff clicks the "Capture Vital Signs" button for an ACTIVE appointment, THE Appointment_State_Machine SHALL transition the state to VITAL_SIGNS immediately
2. THE Triage_System SHALL provide an endpoint PATCH /api/clinical/appointments/{id}/start-vital-signs for this transition
3. WHEN the state transitions to VITAL_SIGNS, THE Triage_System SHALL redirect to the vital signs capture form
4. IF the appointment is not in ACTIVE state, THEN THE Appointment_State_Machine SHALL reject the transition to VITAL_SIGNS

### Requirement 6: Vital Signs Completion

**User Story:** As triage staff, I want appointments to move to consultation queue after saving vital signs, so that doctors can see them.

#### Acceptance Criteria

1. WHEN vital signs are saved for a VITAL_SIGNS appointment, THE Appointment_State_Machine SHALL transition the state to CONSULTATION
2. THE Triage_System SHALL validate all required vital signs are captured before transition
3. IF the appointment is not in VITAL_SIGNS state, THEN THE Appointment_State_Machine SHALL reject vital signs submission

### Requirement 7: Doctor Consultation Registration

**User Story:** As a doctor, I want to register consultations and determine the next workflow step, so that patients receive appropriate care.

#### Acceptance Criteria

1. WHEN a doctor registers a consultation for a CONSULTATION appointment without lab orders and without prescription, THE Appointment_State_Machine SHALL transition the state to COMPLETED
2. WHEN a doctor registers a consultation for a CONSULTATION appointment with prescription but without lab orders, THE Appointment_State_Machine SHALL transition the state to PENDING_PHARMACY_PAYMENT
3. WHEN a doctor registers a consultation for a CONSULTATION appointment with lab orders, THE Appointment_State_Machine SHALL transition the state to PENDING_LAB_PAYMENT
4. IF the appointment is not in CONSULTATION state, THEN THE Appointment_State_Machine SHALL reject consultation registration

### Requirement 8: Laboratory Payment and Processing

**User Story:** As a cashier, I want to process lab payments and move appointments to laboratory queue, so that tests can be performed.

#### Acceptance Criteria

1. WHEN payment is confirmed for a PENDING_LAB_PAYMENT appointment, THE Appointment_State_Machine SHALL transition the state to LABORATORY
2. IF the lab invoice status is not PAID, THEN THE Appointment_State_Machine SHALL reject the transition from PENDING_LAB_PAYMENT to LABORATORY
3. WHEN lab results are completed for a LABORATORY appointment, THE Appointment_State_Machine SHALL transition the state to RE_EVALUATION
4. THE Laboratory_System SHALL validate the lab invoice exists before processing tests

### Requirement 9: Doctor Re-evaluation Priority

**User Story:** As a doctor, I want re-evaluation appointments to appear with priority in my queue, so that I can review lab results promptly.

#### Acceptance Criteria

1. WHEN an appointment transitions to RE_EVALUATION state, THE Consultation_System SHALL display it with PRIORITY indicator in the doctor's queue
2. THE Consultation_System SHALL sort RE_EVALUATION appointments before CONSULTATION appointments in the doctor's view
3. WHEN a doctor completes re-evaluation for a RE_EVALUATION appointment without prescription, THE Appointment_State_Machine SHALL transition the state to COMPLETED
4. WHEN a doctor completes re-evaluation for a RE_EVALUATION appointment with prescription, THE Appointment_State_Machine SHALL transition the state to PENDING_PHARMACY_PAYMENT

### Requirement 10: Pharmacy Payment and Dispensing

**User Story:** As a cashier, I want to process medication payments and move appointments to pharmacy queue, so that medications can be dispensed.

#### Acceptance Criteria

1. WHEN payment is confirmed for a PENDING_PHARMACY_PAYMENT appointment, THE Appointment_State_Machine SHALL transition the state to PHARMACY
2. IF the pharmacy invoice status is not PAID, THEN THE Appointment_State_Machine SHALL reject the transition from PENDING_PHARMACY_PAYMENT to PHARMACY
3. WHEN medications are dispensed for a PHARMACY appointment, THE Appointment_State_Machine SHALL transition the state to COMPLETED
4. THE Pharmacy_System SHALL validate the pharmacy invoice exists before dispensing medications

### Requirement 11: Appointment Cancellation

**User Story:** As admission staff, I want to cancel appointments at any stage before completion, so that resources can be reallocated.

#### Acceptance Criteria

1. WHEN an appointment in any state except COMPLETED is cancelled, THE Appointment_State_Machine SHALL transition the state to CANCELLED
2. IF the appointment is in COMPLETED state, THEN THE Appointment_State_Machine SHALL reject cancellation
3. THE Appointment_State_Machine SHALL allow cancellation from PENDING_PAYMENT state
4. THE Appointment_State_Machine SHALL allow cancellation from SCHEDULED state
5. THE Appointment_State_Machine SHALL allow cancellation from ACTIVE state
6. THE Appointment_State_Machine SHALL allow cancellation from VITAL_SIGNS state
7. THE Appointment_State_Machine SHALL allow cancellation from CONSULTATION state
8. THE Appointment_State_Machine SHALL allow cancellation from PENDING_LAB_PAYMENT state
9. THE Appointment_State_Machine SHALL allow cancellation from LABORATORY state
10. THE Appointment_State_Machine SHALL allow cancellation from RE_EVALUATION state
11. THE Appointment_State_Machine SHALL allow cancellation from PENDING_PHARMACY_PAYMENT state
12. THE Appointment_State_Machine SHALL allow cancellation from PHARMACY state

### Requirement 12: Missed Appointment Detection

**User Story:** As the system, I want to automatically mark appointments as missed when patients don't arrive, so that statistics are accurate.

#### Acceptance Criteria

1. WHEN a SCHEDULED appointment is not activated within 60 minutes after the scheduled time, THE Appointment_State_Machine SHALL transition the state to MISSED
2. IF the appointment is not in SCHEDULED state, THEN THE Appointment_State_Machine SHALL not apply missed appointment logic
3. THE Appointment_State_Machine SHALL check for missed appointments every 5 minutes

### Requirement 13: Role-Based Appointment Views

**User Story:** As hospital staff, I want to see only appointments relevant to my role, so that I can focus on my work.

#### Acceptance Criteria

1. WHEN Admission_Staff views the appointment list, THE Appointment_State_Machine SHALL display appointments in PENDING_PAYMENT state
2. WHEN Admission_Staff views the appointment list, THE Appointment_State_Machine SHALL display appointments in SCHEDULED state
3. WHEN triage staff views the pending list, THE Triage_System SHALL display appointments in ACTIVE state
4. WHEN triage staff views the pending list, THE Triage_System SHALL display appointments in VITAL_SIGNS state
5. WHEN a Doctor views their appointment list, THE Consultation_System SHALL display appointments in CONSULTATION state
6. WHEN a Doctor views their appointment list, THE Consultation_System SHALL display appointments in RE_EVALUATION state with PRIORITY indicator
7. WHEN Cashier views the payment queue, THE Billing_Service SHALL display appointments in PENDING_PAYMENT state
8. WHEN Cashier views the payment queue, THE Billing_Service SHALL display appointments in PENDING_LAB_PAYMENT state
9. WHEN Cashier views the payment queue, THE Billing_Service SHALL display appointments in PENDING_PHARMACY_PAYMENT state
10. WHEN laboratory staff views the lab queue, THE Laboratory_System SHALL display appointments in LABORATORY state
11. WHEN pharmacy staff views the pharmacy queue, THE Pharmacy_System SHALL display appointments in PHARMACY state

### Requirement 14: State Transition Validation

**User Story:** As a developer, I want all state transitions to be validated, so that the system maintains data integrity.

#### Acceptance Criteria

1. WHEN an invalid state transition is attempted, THE Appointment_State_Machine SHALL reject the transition with a descriptive error message
2. THE Appointment_State_Machine SHALL log all state transitions with timestamp and user information
3. THE Appointment_State_Machine SHALL validate that required data exists before each transition
4. WHEN a payment-dependent transition is attempted, THE Appointment_State_Machine SHALL validate invoice status with Billing_Service

### Requirement 15: Backend State Transition Endpoints

**User Story:** As a frontend developer, I want REST endpoints for each state transition, so that I can implement the UI workflows.

#### Acceptance Criteria

1. THE Appointment_State_Machine SHALL provide endpoint POST /api/clinical/appointments/{id}/confirm-payment for PENDING_PAYMENT to SCHEDULED transition
2. THE Appointment_State_Machine SHALL provide endpoint PATCH /api/clinical/appointments/{id}/activate for SCHEDULED to ACTIVE transition
3. THE Appointment_State_Machine SHALL provide endpoint PATCH /api/clinical/appointments/{id}/start-vital-signs for ACTIVE to VITAL_SIGNS transition
4. THE Appointment_State_Machine SHALL provide endpoint POST /api/clinical/vital-signs for VITAL_SIGNS to CONSULTATION transition
5. THE Appointment_State_Machine SHALL provide endpoint POST /api/clinical/consultations for CONSULTATION to next state transition
6. THE Appointment_State_Machine SHALL provide endpoint POST /api/clinical/appointments/{id}/confirm-lab-payment for PENDING_LAB_PAYMENT to LABORATORY transition
7. THE Appointment_State_Machine SHALL provide endpoint PATCH /api/clinical/appointments/{id}/complete-lab for LABORATORY to RE_EVALUATION transition
8. THE Appointment_State_Machine SHALL provide endpoint POST /api/clinical/appointments/{id}/confirm-pharmacy-payment for PENDING_PHARMACY_PAYMENT to PHARMACY transition
9. THE Appointment_State_Machine SHALL provide endpoint PATCH /api/clinical/appointments/{id}/dispense-medication for PHARMACY to COMPLETED transition
10. THE Appointment_State_Machine SHALL provide endpoint DELETE /api/clinical/appointments/{id} for any state to CANCELLED transition

### Requirement 16: Frontend View Updates

**User Story:** As a frontend developer, I want to update all views to show correct appointment states, so that users see accurate information.

#### Acceptance Criteria

1. THE Triage_System SHALL update TriagePendingPage to display appointments in ACTIVE and VITAL_SIGNS states
2. THE Triage_System SHALL update TriageVitalSignsCapture to transition appointments to VITAL_SIGNS on page load
3. THE Consultation_System SHALL update DoctorConsultation to display appointments in CONSULTATION and RE_EVALUATION states
4. THE Consultation_System SHALL display RE_EVALUATION appointments with visual priority indicator
5. THE Admission_Staff interface SHALL update ActivateAppointments to display appointments in PENDING_PAYMENT and SCHEDULED states
6. THE Admission_Staff interface SHALL display payment status indicators for PENDING_PAYMENT appointments

### Requirement 17: Consultation Registration Behavior Change

**User Story:** As a doctor, I want consultation registration to route patients to the next workflow step instead of completing the appointment, so that the workflow continues correctly.

#### Acceptance Criteria

1. WHEN a doctor registers a consultation, THE Consultation_System SHALL not automatically transition to COMPLETED state
2. WHEN a doctor registers a consultation without lab orders and without prescription, THE Consultation_System SHALL transition to COMPLETED state
3. WHEN a doctor registers a consultation with prescription, THE Consultation_System SHALL transition to PENDING_PHARMACY_PAYMENT state
4. WHEN a doctor registers a consultation with lab orders, THE Consultation_System SHALL transition to PENDING_LAB_PAYMENT state
5. THE Consultation_System SHALL determine the next state based on the consultation content

### Requirement 18: Payment Validation Integration

**User Story:** As the system, I want to validate payment status before each paid transition, so that revenue is protected.

#### Acceptance Criteria

1. WHEN transitioning from PENDING_PAYMENT to SCHEDULED, THE Appointment_State_Machine SHALL validate the consultation invoice is PAID
2. WHEN transitioning from PENDING_LAB_PAYMENT to LABORATORY, THE Appointment_State_Machine SHALL validate the lab invoice is PAID
3. WHEN transitioning from PENDING_PHARMACY_PAYMENT to PHARMACY, THE Appointment_State_Machine SHALL validate the pharmacy invoice is PAID
4. IF invoice validation fails due to Billing_Service unavailability, THEN THE Appointment_State_Machine SHALL return HTTP 503 Service Unavailable
5. IF invoice validation fails due to unpaid status, THEN THE Appointment_State_Machine SHALL return HTTP 400 Bad Request with descriptive message

### Requirement 19: State Machine Domain Model

**User Story:** As a developer, I want the state machine logic in the domain model, so that business rules are centralized.

#### Acceptance Criteria

1. THE Appointment domain model SHALL contain methods for each valid state transition
2. THE Appointment domain model SHALL validate preconditions before each state transition
3. THE Appointment domain model SHALL throw IllegalStateException with descriptive message when invalid transitions are attempted
4. THE Appointment domain model SHALL maintain the current state as an enum field
5. THE Appointment domain model SHALL not depend on infrastructure concerns like HTTP or database

### Requirement 20: Backward Compatibility

**User Story:** As a system administrator, I want the new state machine to handle existing appointments gracefully, so that the system continues operating during migration.

#### Acceptance Criteria

1. WHEN an existing appointment in SCHEDULED state is loaded, THE Appointment_State_Machine SHALL maintain the SCHEDULED state
2. WHEN an existing appointment in ACTIVE state is loaded, THE Appointment_State_Machine SHALL maintain the ACTIVE state
3. WHEN an existing appointment in COMPLETED state is loaded, THE Appointment_State_Machine SHALL maintain the COMPLETED state
4. WHEN an existing appointment in CANCELLED state is loaded, THE Appointment_State_Machine SHALL maintain the CANCELLED state
5. WHEN an existing appointment in MISSED state is loaded, THE Appointment_State_Machine SHALL maintain the MISSED state
6. THE Appointment_State_Machine SHALL support database migration from 5 states to 13 states without data loss
