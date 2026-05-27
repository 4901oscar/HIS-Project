# Requirements Document

## Introduction

This document specifies the requirements for integrating the triage-appointment backend endpoints into the frontend React application. The feature enables triage staff (VITAL_SIGNS and DOCTOR roles) to view a list of active appointments awaiting triage, select an appointment, record vital signs, and perform triage with automatic appointment linking.

The backend has been successfully implemented with three endpoints:
1. GET /api/clinical/appointments/pending-triage - Returns list of ACTIVE appointments without triage
2. GET /api/clinical/appointments/{id}/triage - Returns triage for a specific appointment
3. POST /api/clinical/triage (modified) - Now requires appointmentId field

This frontend integration completes the patient flow: appointment activation → vital signs capture → triage → consultation.

## Glossary

- **Triage_Staff**: Healthcare personnel authorized to perform triage (VITAL_SIGNS or DOCTOR role)
- **Pending_Triage_List**: A list of ACTIVE appointments that do not have an associated triage record
- **Clinical_Service**: The TypeScript service module that communicates with the clinical-service backend API
- **Triage_Page**: The new React page component that displays pending appointments and manages the triage workflow
- **Auto_Refresh**: Automatic periodic re-fetching of the pending triage list every 30 seconds
- **Manchester_Triage**: The triage system using Manchester discriminators to assess patient priority
- **Appointment_Selection**: The action of clicking an appointment from the pending list to begin triage workflow
- **Vital_Signs_Capture**: The process of recording patient vital signs before triage
- **Triage_Workflow**: The complete sequence: select appointment → record vital signs → perform triage → return to list
- **Frontend_Application**: The React + TypeScript application (frontend-medflow)
- **Route_Protection**: Authorization mechanism that restricts page access to specific user roles

## Requirements

### Requirement 1: Service Layer Integration

**User Story:** As a frontend developer, I want service functions to communicate with the new backend endpoints, so that the UI can fetch and submit triage-appointment data.

#### Acceptance Criteria

1. THE Clinical_Service SHALL provide function getPendingTriageAppointments() that calls GET /api/clinical/appointments/pending-triage
2. THE Clinical_Service SHALL provide function getAppointmentTriage(appointmentId: string) that calls GET /api/clinical/appointments/{id}/triage
3. THE Clinical_Service SHALL modify function performTriage() to accept appointmentId parameter and include it in the POST /api/clinical/triage request body
4. WHEN getPendingTriageAppointments() succeeds, THE Clinical_Service SHALL return an array of AppointmentResponse objects
5. WHEN getAppointmentTriage() succeeds, THE Clinical_Service SHALL return a TriageResponse object
6. WHEN getAppointmentTriage() receives 404 response, THE Clinical_Service SHALL throw an error indicating no triage found
7. THE Clinical_Service SHALL define TypeScript interface PendingTriageAppointment with fields: id, patientId, doctorId, appointmentDate, appointmentTime, status, notes, createdAt
8. FOR ALL service functions, network errors SHALL be propagated to the calling component for handling

### Requirement 2: Pending Triage List Display

**User Story:** As a triage staff member, I want to see a list of all active appointments awaiting triage, so that I can identify which patients need assessment.

#### Acceptance Criteria

1. THE Triage_Page SHALL display a table showing all appointments returned by getPendingTriageAppointments()
2. THE table SHALL include columns: Fecha (date), Hora (time), Paciente (patient ID), Motivo (notes), ID Cita (appointment ID)
3. WHEN the pending list is empty, THE Triage_Page SHALL display message "No hay citas pendientes de triaje"
4. WHEN loading pending appointments, THE Triage_Page SHALL display a loading spinner
5. WHEN an API error occurs, THE Triage_Page SHALL display error message "Error al cargar citas pendientes"
6. THE table rows SHALL be clickable to select an appointment
7. WHEN a row is clicked, THE Triage_Page SHALL highlight the selected appointment and display patient details
8. THE appointments SHALL be sorted by date ascending, then by time ascending

### Requirement 3: Auto-Refresh Functionality

**User Story:** As a triage staff member, I want the pending list to refresh automatically, so that I see new appointments without manually reloading the page.

#### Acceptance Criteria

1. THE Triage_Page SHALL automatically call getPendingTriageAppointments() every 30 seconds
2. WHEN the component mounts, THE Triage_Page SHALL immediately fetch the pending list
3. WHEN the component unmounts, THE Triage_Page SHALL cancel the auto-refresh timer
4. WHEN auto-refresh is active, THE Triage_Page SHALL NOT display a loading spinner (to avoid UI flicker)
5. THE Triage_Page SHALL provide a manual "Actualizar" button to refresh the list immediately
6. WHEN the manual refresh button is clicked, THE Triage_Page SHALL fetch the pending list and reset the 30-second timer
7. WHILE a triage workflow is in progress (appointment selected), THE auto-refresh SHALL continue in the background
8. WHEN auto-refresh detects the selected appointment is no longer in the pending list, THE Triage_Page SHALL display message "Esta cita ya tiene triaje registrado"

### Requirement 4: Appointment Selection and Patient Details

**User Story:** As a triage staff member, I want to select an appointment and see patient details, so that I can verify I'm working with the correct patient before starting triage.

#### Acceptance Criteria

1. WHEN an appointment is selected, THE Triage_Page SHALL display a patient details card with: patient name, DPI, appointment date, appointment time, appointment ID
2. THE patient details card SHALL have a "Cancelar" button to deselect the appointment and return to the list view
3. WHEN an appointment is selected, THE Triage_Page SHALL display a "Registrar Signos Vitales" button
4. WHEN an appointment is selected, THE Triage_Page SHALL display a "Realizar Triaje" button
5. IF the patient does not have vital signs recorded, THE "Realizar Triaje" button SHALL be disabled with tooltip "Primero registre los signos vitales"
6. THE selected appointment SHALL remain highlighted in the table
7. WHEN "Cancelar" is clicked, THE Triage_Page SHALL clear the selection and return to list-only view

### Requirement 5: Vital Signs Integration

**User Story:** As a triage staff member, I want to record vital signs for the selected patient, so that I can proceed with triage assessment.

#### Acceptance Criteria

1. WHEN "Registrar Signos Vitales" button is clicked, THE Triage_Page SHALL display a vital signs form
2. THE vital signs form SHALL include fields: systolic pressure, diastolic pressure, heart rate, respiratory rate, temperature, oxygen saturation, weight (optional), height (optional)
3. WHEN the vital signs form is submitted, THE Triage_Page SHALL call recordVitalSigns() with the patient ID from the selected appointment
4. WHEN vital signs are successfully recorded, THE Triage_Page SHALL display success message "Signos vitales registrados exitosamente"
5. WHEN vital signs are successfully recorded, THE "Realizar Triaje" button SHALL become enabled
6. IF vital signs recording fails, THE Triage_Page SHALL display error message from the API response
7. THE vital signs form SHALL validate numeric ranges: systolic (50-250), diastolic (30-150), heart rate (20-300), respiratory rate (5-60), temperature (30-45), oxygen saturation (50-100)

### Requirement 6: Triage Workflow Execution

**User Story:** As a triage staff member, I want to perform triage for the selected appointment, so that the patient receives a priority assessment.

#### Acceptance Criteria

1. WHEN "Realizar Triaje" button is clicked, THE Triage_Page SHALL display the Manchester triage form
2. THE triage form SHALL include: motif selection dropdown, discriminator checkboxes (from Manchester catalog)
3. WHEN the triage form is submitted, THE Triage_Page SHALL call performTriage() with appointmentId, patientId, motifId, and discriminatorIds
4. WHEN triage is successfully created, THE Triage_Page SHALL display success message "Triaje registrado exitosamente — Prioridad: [priority level]"
5. WHEN triage is successfully created, THE Triage_Page SHALL clear the appointment selection and return to the pending list view
6. WHEN triage is successfully created, THE Triage_Page SHALL immediately refresh the pending list (removing the triaged appointment)
7. IF triage creation fails with 409 error, THE Triage_Page SHALL display message "Esta cita ya tiene triaje registrado"
8. IF triage creation fails with 400 error (no vital signs), THE Triage_Page SHALL display message "El paciente no tiene signos vitales registrados"
9. IF triage creation fails with 404 error (appointment not found), THE Triage_Page SHALL display message "Cita no encontrada"

### Requirement 7: Routing and Navigation

**User Story:** As a triage staff member, I want to access the triage page from the application menu, so that I can start processing pending appointments.

#### Acceptance Criteria

1. THE Frontend_Application SHALL add route /vitals/triage that renders the Triage_Page component
2. THE route /vitals/triage SHALL be protected and require VITAL_SIGNS or DOCTOR role
3. WHEN a user without VITAL_SIGNS or DOCTOR role attempts to access /vitals/triage, THE application SHALL redirect to the login page or display "Acceso denegado"
4. THE application navigation menu SHALL include a link "Triaje Pendiente" for users with VITAL_SIGNS or DOCTOR role
5. WHEN the "Triaje Pendiente" link is clicked, THE application SHALL navigate to /vitals/triage

### Requirement 8: UI/UX Consistency

**User Story:** As a user, I want the triage page to match the existing application design, so that I have a consistent experience across all pages.

#### Acceptance Criteria

1. THE Triage_Page SHALL use the MainLayout component wrapper
2. THE Triage_Page SHALL use Tailwind CSS classes consistent with existing pages (medin-cyan, medin-navy, medin-blue colors)
3. THE Triage_Page SHALL use the same input styling as VitalSignsCapture and DoctorConsultation pages
4. THE Triage_Page SHALL use Heroicons for all icons
5. THE Triage_Page SHALL be responsive and display correctly on mobile devices (min-width: 320px)
6. THE Triage_Page SHALL use Spanish language for all text labels, buttons, and messages
7. THE loading spinner SHALL use the same animation as existing pages (animate-spin with medin-cyan border)
8. THE error messages SHALL use red-50 background with red-200 border and red-800 text

### Requirement 9: Error Handling and User Feedback

**User Story:** As a triage staff member, I want clear error messages and feedback, so that I understand what went wrong and how to proceed.

#### Acceptance Criteria

1. WHEN a network error occurs, THE Triage_Page SHALL display message "Error al conectar con el servidor"
2. WHEN the API returns a 404 error, THE Triage_Page SHALL display the specific error message from the API response
3. WHEN the API returns a 400 validation error, THE Triage_Page SHALL display the validation message from the API response
4. WHEN the API returns a 409 conflict error, THE Triage_Page SHALL display message "Esta cita ya tiene triaje registrado"
5. WHEN a successful operation completes, THE Triage_Page SHALL display a success message in green-50 background with green-200 border
6. THE success and error messages SHALL auto-dismiss after 5 seconds
7. WHEN an error occurs during auto-refresh, THE Triage_Page SHALL log the error to console but NOT display an error message (to avoid interrupting the user)
8. THE Triage_Page SHALL provide a retry button for failed operations

### Requirement 10: Performance and Optimization

**User Story:** As a user, I want the triage page to load quickly and respond smoothly, so that I can work efficiently.

#### Acceptance Criteria

1. THE Triage_Page SHALL use React.memo for the appointment table rows to prevent unnecessary re-renders
2. THE Triage_Page SHALL use useMemo to memoize the sorted appointment list
3. THE Triage_Page SHALL debounce the manual refresh button to prevent multiple simultaneous API calls (500ms debounce)
4. WHEN the pending list contains more than 50 appointments, THE Triage_Page SHALL implement pagination (20 appointments per page)
5. THE Triage_Page SHALL cancel in-flight API requests when the component unmounts
6. THE auto-refresh timer SHALL be cleared when the component unmounts to prevent memory leaks
7. THE Triage_Page SHALL use lazy loading for the Manchester catalog data (load on first triage form open)

### Requirement 11: Accessibility Compliance

**User Story:** As a user with accessibility needs, I want the triage page to be accessible, so that I can use assistive technologies effectively.

#### Acceptance Criteria

1. THE Triage_Page SHALL include ARIA labels for all interactive elements (buttons, inputs, table)
2. THE table SHALL use semantic HTML table elements (thead, tbody, th, td) with proper scope attributes
3. THE loading spinner SHALL include aria-live="polite" and aria-busy="true" attributes
4. THE error messages SHALL include role="alert" attribute for screen reader announcement
5. THE Triage_Page SHALL support keyboard navigation (Tab, Enter, Escape keys)
6. WHEN an appointment is selected via keyboard, THE Triage_Page SHALL move focus to the patient details card
7. THE color contrast ratio SHALL meet WCAG 2.1 AA standards (4.5:1 for normal text, 3:1 for large text)
8. THE form inputs SHALL include associated label elements with htmlFor attributes

### Requirement 12: Data Validation and Integrity

**User Story:** As a system administrator, I want the frontend to validate data before submission, so that we prevent invalid API requests and maintain data integrity.

#### Acceptance Criteria

1. WHEN submitting vital signs, THE Triage_Page SHALL validate that all required fields are filled
2. WHEN submitting vital signs, THE Triage_Page SHALL validate numeric ranges match the backend constraints
3. WHEN submitting triage, THE Triage_Page SHALL validate that at least one discriminator is selected
4. WHEN submitting triage, THE Triage_Page SHALL validate that a motif is selected
5. IF validation fails, THE Triage_Page SHALL display field-specific error messages below the invalid inputs
6. THE Triage_Page SHALL prevent form submission while validation errors exist
7. THE Triage_Page SHALL sanitize user input to prevent XSS attacks (use React's built-in escaping)
8. THE appointmentId SHALL be validated as a non-empty string before API calls

## Correctness Properties

### Property 1: Pending List Accuracy (Metamorphic)

**Property:** The set of appointments displayed in the pending list SHALL equal the set returned by the backend API, with no duplicates and correct sorting.

**Test Strategy:** Property-based test
- Generate random sets of appointments with various dates and times
- Mock the API to return these appointments
- Verify the component displays exactly the same appointments
- Verify no duplicates exist in the rendered list
- Verify sorting is correct (date ascending, then time ascending)

**Rationale:** This ensures the UI accurately reflects the backend state. Property-based testing verifies correctness across diverse data sets.

### Property 2: Auto-Refresh Idempotence (Idempotence)

**Property:** Calling getPendingTriageAppointments() multiple times SHALL return consistent results if no triages are created between calls.

**Test Strategy:** Property-based test
- Mock the API to return a fixed set of appointments
- Trigger auto-refresh multiple times
- Verify the displayed list remains identical across refreshes
- Verify no UI flicker or state corruption occurs

**Rationale:** Auto-refresh should be idempotent and not cause UI instability. Property-based testing ensures this holds across various refresh scenarios.

### Property 3: Appointment Selection Round-Trip (Round-Trip)

**Property:** Selecting an appointment then canceling SHALL return the UI to the initial state with no side effects.

**Test Strategy:** Property-based test
- Generate random appointments
- Select an appointment and capture UI state
- Click cancel and verify UI returns to initial state
- Verify no API calls are made during cancel
- Verify the appointment remains in the pending list

**Rationale:** This verifies the selection/cancel workflow is reversible. Property-based testing ensures correctness across various appointment data.

### Property 4: Triage Workflow Completion (Invariant)

**Property:** After successfully completing triage for an appointment, that appointment SHALL no longer appear in the pending list.

**Test Strategy:** Integration test (not property-based)
- Create a pending appointment via API
- Complete the triage workflow in the UI
- Verify the appointment is removed from the pending list
- Verify the appointment has an associated triage record in the backend

**Rationale:** This is a critical invariant that ensures data consistency between frontend and backend. Integration testing is appropriate since it involves real API interactions.

### Property 5: Vital Signs Validation Completeness (Error Conditions)

**Property:** The vital signs form SHALL reject all invalid inputs: out-of-range values, non-numeric values, missing required fields.

**Test Strategy:** Property-based test
- Generate invalid vital signs inputs (negative values, out-of-range, non-numeric, missing fields)
- Attempt to submit the form with each invalid input
- Verify the form displays appropriate error messages
- Verify no API call is made for invalid inputs

**Rationale:** Comprehensive validation prevents invalid data submission. Property-based testing generates diverse invalid inputs to verify all validation rules.

### Property 6: Error Message Clarity (Metamorphic)

**Property:** For each API error code (400, 404, 409, 500), the UI SHALL display a user-friendly Spanish error message that corresponds to the error type.

**Test Strategy:** Property-based test
- Generate API error responses with various status codes and messages
- Trigger API calls that return these errors
- Verify the UI displays the correct Spanish error message for each error code
- Verify error messages are user-friendly (no technical jargon)

**Rationale:** Error messages must be clear and localized. Property-based testing ensures all error codes are handled correctly.

### Property 7: Auto-Refresh Timer Cleanup (Invariant)

**Property:** When the Triage_Page component unmounts, all timers and pending API requests SHALL be canceled to prevent memory leaks.

**Test Strategy:** Integration test (not property-based)
- Mount the Triage_Page component
- Start auto-refresh
- Unmount the component
- Verify no timers remain active (using jest.getTimerCount())
- Verify no API requests are in flight

**Rationale:** Proper cleanup prevents memory leaks. This is tested with a fixed sequence rather than property-based testing since cleanup is deterministic.

### Property 8: Keyboard Navigation Completeness (Accessibility Property)

**Property:** All interactive elements SHALL be reachable and operable via keyboard (Tab, Enter, Escape).

**Test Strategy:** Integration test (not property-based)
- Render the Triage_Page
- Simulate Tab key presses to navigate through all interactive elements
- Verify focus moves to each button, input, and table row
- Simulate Enter key on buttons and verify actions execute
- Simulate Escape key and verify modals/forms close

**Rationale:** Keyboard navigation is essential for accessibility. This is tested with a fixed sequence of key presses rather than property-based testing.

### Property 9: Concurrent Triage Prevention (Invariant)

**Property:** If two triage staff members attempt to triage the same appointment simultaneously, only the first SHALL succeed and the second SHALL receive a 409 error.

**Test Strategy:** Integration test (not property-based)
- Create a pending appointment
- Simulate two concurrent triage submissions for the same appointment
- Verify the first submission succeeds (200 response)
- Verify the second submission fails (409 response)
- Verify the UI displays "Esta cita ya tiene triaje registrado" for the second user

**Rationale:** This tests the backend's duplicate prevention and the frontend's error handling. Integration testing is appropriate for concurrent scenarios.

### Property 10: UI Responsiveness (Performance Property)

**Property:** The Triage_Page SHALL render the pending list in less than 500ms for lists containing up to 100 appointments.

**Test Strategy:** Performance test (not property-based)
- Mock the API to return 100 appointments
- Measure the time from component mount to list render completion
- Verify 95th percentile render time is below 500ms
- Verify no UI blocking occurs during render

**Rationale:** Performance is critical for user experience. This is measured with statistical sampling rather than property-based testing.

## Notes

- **Parser/Serializer Requirements:** This feature does not introduce new parsers or serializers. All data formats (JSON request/response) use existing Axios serialization and TypeScript interfaces.

- **Property-Based Testing Scope:** Properties 1, 2, 3, 5, and 6 are suitable for property-based testing as they verify logic that varies meaningfully with input. Properties 4, 7, 8, 9, and 10 are better suited for integration/performance tests as they test infrastructure behavior or require real API interactions.

- **Manchester Catalog Integration:** The Manchester motifs and discriminators catalog should be loaded from the backend API (existing endpoint) rather than hardcoded in the frontend. Consider caching the catalog in localStorage to reduce API calls.

- **Backward Compatibility:** The modified performTriage() function must maintain backward compatibility if other parts of the application call it without appointmentId. Consider making appointmentId optional with a deprecation warning.

- **Mobile Optimization:** The table layout should switch to a card-based layout on mobile devices (screen width < 768px) for better usability.

- **Future Enhancements:** Consider adding filters (by date, by patient name) and search functionality if the pending list grows large. Consider adding a "priority view" that shows appointments sorted by wait time.

- **Testing Strategy:** Use React Testing Library for component tests, Mock Service Worker (MSW) for API mocking, and Jest for test execution. Aim for 80%+ code coverage.

- **Deployment Considerations:** Ensure the backend endpoints are deployed and accessible before deploying the frontend changes. Consider feature flags to enable/disable the new triage page during rollout.
