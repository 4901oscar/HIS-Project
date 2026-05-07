# Requirements Document

## Introduction

The Pharmacy Dispensing Module completes the medication dispensing workflow for internal pharmacy operations. Currently, when a doctor prescribes internal pharmacy medications, the system creates prescriptions and invoices, processes payments, and transitions appointments to PHARMACY status—but the workflow ends there with no interface for pharmacists to view pending prescriptions or mark medications as dispensed.

This module provides pharmacists with a queue-based interface to view paid prescriptions awaiting dispensing, access prescription details (patient information, medications, quantities, and instructions), and mark medications as dispensed when handed to patients. Upon dispensing, the appointment transitions to COMPLETED status, closing the patient flow.

The module follows established patterns from the Lab Sample Workflow and Cashier modules, providing a consistent user experience across clinical workflows.

## Glossary

- **Pharmacy_Module**: The frontend React component and associated backend endpoints that enable pharmacists to manage medication dispensing
- **Pharmacy_Queue**: The list of appointments in PHARMACY status with paid prescriptions awaiting dispensing
- **Prescription**: A clinical document containing one or more medications with dosage, frequency, duration, route, and special instructions
- **Dispensing**: The act of physically handing medications to a patient and recording this action in the system
- **Appointment**: A scheduled patient visit that progresses through various status states
- **Clinical_Service**: The Spring Boot microservice that manages appointments, prescriptions, and clinical workflows
- **Frontend**: The React + TypeScript web application used by hospital staff
- **Pharmacist**: A user with PHARMACY role who dispenses medications to patients

## Requirements

### Requirement 1: Display Pharmacy Queue

**User Story:** As a pharmacist, I want to see all paid prescriptions waiting to be dispensed, so that I know which patients are waiting for their medications.

#### Acceptance Criteria

1. WHEN the Pharmacy_Module loads, THE Frontend SHALL fetch all appointments with status PHARMACY from Clinical_Service
2. THE Pharmacy_Queue SHALL display patient full name, DPI, appointment date, appointment time, and prescription code for each appointment
3. THE Pharmacy_Queue SHALL sort appointments by appointment date ascending, then by appointment time ascending
4. WHEN the Pharmacy_Queue is empty, THE Frontend SHALL display a message "No hay recetas pendientes de despacho"
5. THE Pharmacy_Queue SHALL provide a refresh button that reloads the queue data when clicked
6. WHEN the pharmacist clicks on an appointment in the Pharmacy_Queue, THE Frontend SHALL display the prescription details view

### Requirement 2: Display Prescription Details

**User Story:** As a pharmacist, I want to view detailed prescription information including all medications, quantities, and instructions, so that I can prepare and verify the correct medications for the patient.

#### Acceptance Criteria

1. WHEN a pharmacist selects an appointment from the Pharmacy_Queue, THE Frontend SHALL fetch the prescription associated with that appointment from Clinical_Service
2. THE Frontend SHALL display patient information including full name, DPI, phone number, and email address
3. THE Frontend SHALL display prescription metadata including prescription code, issuing doctor name, and issue date
4. FOR EACH medication in the prescription, THE Frontend SHALL display medication name, dosage, frequency, duration in days, route, and special instructions
5. WHERE a medication includes calculated quantity fields (dosageAmount, dosageUnit, frequencyHours, totalQuantity), THE Frontend SHALL display the total quantity to dispense
6. THE Frontend SHALL provide a "Dispensar Medicamentos" button that is enabled when the prescription status is PENDING
7. THE Frontend SHALL provide a "Volver a la cola" button that returns to the Pharmacy_Queue view

### Requirement 3: Mark Prescription as Dispensed

**User Story:** As a pharmacist, I want to mark medications as dispensed when I hand them to the patient, so that the system records the completion of the dispensing process.

#### Acceptance Criteria

1. WHEN a pharmacist clicks "Dispensar Medicamentos", THE Frontend SHALL send a PATCH request to Clinical_Service endpoint `/api/clinical/appointments/{id}/dispense-medication`
2. WHEN the dispense request succeeds, THE Clinical_Service SHALL transition the appointment status from PHARMACY to COMPLETED
3. WHEN the dispense request succeeds, THE Frontend SHALL display a success message "Medicamentos dispensados correctamente"
4. WHEN the dispense request succeeds, THE Frontend SHALL return to the Pharmacy_Queue view and refresh the queue
5. IF the dispense request fails with status 400, THE Frontend SHALL display the error message from Clinical_Service
6. IF the dispense request fails with status 500, THE Frontend SHALL display "Error al dispensar medicamentos. Por favor, intente nuevamente."
7. WHEN the appointment status is not PHARMACY, THE Clinical_Service SHALL return status 400 with message "Solo se pueden dispensar medicamentos para citas en estado PHARMACY"

### Requirement 4: Backend Endpoint for Dispensing

**User Story:** As a system, I want to provide a secure endpoint for marking medications as dispensed, so that only authorized pharmacists can complete the dispensing workflow.

#### Acceptance Criteria

1. THE Clinical_Service SHALL provide endpoint `PATCH /api/clinical/appointments/{id}/dispense-medication`
2. WHEN the endpoint receives a valid appointment ID, THE Clinical_Service SHALL verify the appointment exists
3. WHEN the appointment exists and status is PHARMACY, THE Clinical_Service SHALL call the `dispenseMedication()` method on the Appointment domain entity
4. WHEN the `dispenseMedication()` method succeeds, THE Clinical_Service SHALL persist the updated appointment status
5. WHEN the `dispenseMedication()` method succeeds, THE Clinical_Service SHALL return HTTP status 200
6. IF the appointment does not exist, THE Clinical_Service SHALL return HTTP status 404 with message "Cita no encontrada"
7. IF the appointment status is not PHARMACY, THE Clinical_Service SHALL return HTTP status 400 with message "Solo se pueden dispensar medicamentos para citas en estado PHARMACY"
8. THE Clinical_Service SHALL require the user to have PHARMACY role to access this endpoint

### Requirement 5: Frontend Routing and Navigation

**User Story:** As a pharmacist, I want to access the pharmacy module from the main navigation menu, so that I can easily navigate to my work area.

#### Acceptance Criteria

1. THE Frontend SHALL provide a route `/pharmacy` that renders the Pharmacy_Module
2. THE Frontend SHALL add a navigation menu item labeled "Farmacia" that links to `/pharmacy`
3. WHEN a user without PHARMACY role attempts to access `/pharmacy`, THE Frontend SHALL redirect to the home page or display an unauthorized message
4. THE Pharmacy_Module SHALL use the MainLayout component for consistent page structure
5. THE Pharmacy_Module SHALL display a page title "Módulo de Farmacia" and subtitle "Gestiona el despacho de medicamentos a pacientes"

### Requirement 6: Prescription Retrieval Endpoint

**User Story:** As a system, I want to provide an endpoint to retrieve prescription details by appointment ID, so that the frontend can display complete prescription information to pharmacists.

#### Acceptance Criteria

1. THE Clinical_Service SHALL provide endpoint `GET /api/clinical/appointments/{appointmentId}/prescription`
2. WHEN the endpoint receives a valid appointment ID, THE Clinical_Service SHALL retrieve the prescription associated with that appointment
3. WHEN a prescription exists for the appointment, THE Clinical_Service SHALL return HTTP status 200 with prescription data including id, prescriptionCode, patientId, doctorId, medications array, status, and issuedAt
4. IF no prescription exists for the appointment, THE Clinical_Service SHALL return HTTP status 404 with message "No se encontró receta para esta cita"
5. THE medications array SHALL include all medication fields: name, dosage, frequency, durationDays, route, specialInstructions, dosageAmount, dosageUnit, frequencyHours, and totalQuantity
6. THE Clinical_Service SHALL require the user to have PHARMACY role to access this endpoint

### Requirement 7: Error Handling and Loading States

**User Story:** As a pharmacist, I want clear feedback when the system is loading data or encounters errors, so that I understand the system state and can take appropriate action.

#### Acceptance Criteria

1. WHILE the Pharmacy_Queue is loading, THE Frontend SHALL display a loading spinner with text "Cargando cola de farmacia..."
2. WHILE prescription details are loading, THE Frontend SHALL display a loading spinner with text "Cargando detalles de la receta..."
3. IF the Pharmacy_Queue fails to load, THE Frontend SHALL display an error message "Error al cargar la cola de farmacia" with a "Reintentar" button
4. IF prescription details fail to load, THE Frontend SHALL display an error message "Error al cargar los detalles de la receta" with a "Reintentar" button
5. WHEN the pharmacist clicks "Reintentar", THE Frontend SHALL attempt to reload the failed data
6. WHILE a dispense operation is in progress, THE Frontend SHALL disable the "Dispensar Medicamentos" button and display loading state

### Requirement 8: Data Consistency and Refresh

**User Story:** As a pharmacist, I want the queue to automatically refresh after I dispense medications, so that I always see the current list of pending prescriptions.

#### Acceptance Criteria

1. WHEN a dispense operation completes successfully, THE Frontend SHALL automatically refresh the Pharmacy_Queue
2. WHEN the pharmacist manually clicks the refresh button, THE Frontend SHALL reload the Pharmacy_Queue from Clinical_Service
3. THE Frontend SHALL preserve the current scroll position in the Pharmacy_Queue after manual refresh
4. WHEN returning to the Pharmacy_Queue from prescription details view, THE Frontend SHALL reload the queue to reflect any status changes

### Requirement 9: Responsive Design and Accessibility

**User Story:** As a pharmacist, I want the pharmacy module to work well on different screen sizes and be accessible, so that I can use it comfortably in various work environments.

#### Acceptance Criteria

1. THE Pharmacy_Module SHALL be responsive and usable on screen widths from 768px to 1920px
2. THE Pharmacy_Queue table SHALL be horizontally scrollable on smaller screens to prevent data truncation
3. THE Frontend SHALL use semantic HTML elements for the Pharmacy_Queue table (table, thead, tbody, tr, th, td)
4. THE Frontend SHALL provide descriptive button labels and aria-labels for screen readers
5. THE Frontend SHALL use sufficient color contrast ratios (WCAG AA standard) for all text and interactive elements
6. THE Frontend SHALL provide keyboard navigation support for all interactive elements in the Pharmacy_Module

### Requirement 10: Integration with Existing Appointment Flow

**User Story:** As a system, I want the pharmacy dispensing workflow to integrate seamlessly with the existing appointment status flow, so that appointments progress correctly through all stages.

#### Acceptance Criteria

1. WHEN an appointment transitions to PHARMACY status via `confirmPharmacyPayment()`, THE appointment SHALL be eligible to appear in the Pharmacy_Queue
2. WHEN the `dispenseMedication()` method is called on an appointment in PHARMACY status, THE appointment status SHALL transition to COMPLETED
3. WHEN an appointment transitions to COMPLETED status, THE appointment SHALL no longer appear in the Pharmacy_Queue
4. THE Clinical_Service SHALL maintain referential integrity between appointments and prescriptions throughout the dispensing workflow
5. WHEN a prescription is marked as dispensed, THE prescription status SHALL remain PENDING (prescription status is independent of appointment status for this requirement)
