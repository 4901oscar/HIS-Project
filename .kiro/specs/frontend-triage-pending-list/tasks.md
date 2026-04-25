# Implementation Plan: Frontend Triage Pending List

## Overview

This implementation plan breaks down the frontend integration of the triage-appointment backend endpoints into discrete coding tasks. The feature enables triage staff (VITAL_SIGNS and DOCTOR roles) to view a list of active appointments awaiting triage, select an appointment, record vital signs, and perform triage with automatic appointment linking.

The implementation follows a phased approach: service layer → basic page structure → data display → user interactions → forms → error handling → testing → optimization → deployment. Each phase builds on the previous one, ensuring incremental validation and integration.

## Tasks

### Phase 1: Service Layer Updates

- [x] 1. Update clinicalService.ts with new triage-appointment functions
  - Modify `frontend-medflow/src/services/clinicalService.ts`
  - Add `getPendingTriageAppointments()` function that calls GET /api/clinical/appointments/pending-triage
  - Add `getAppointmentTriage(appointmentId: string)` function that calls GET /api/clinical/appointments/{id}/triage
  - Modify `performTriage()` function to accept `appointmentId` parameter and include it in POST /api/clinical/triage request body
  - Add TypeScript interface `PendingTriageAppointment` with fields: id, patientId, doctorId, appointmentDate, appointmentTime, status, notes, createdAt
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8_

- [x] 2. Create manchesterService.ts for Manchester catalog operations
  - Create file `frontend-medflow/src/services/manchesterService.ts`
  - Implement `getManchesterCatalog()` function that fetches motifs and discriminators in parallel
  - Implement `getManchesterMotifs()` function that calls GET /api/clinical/manchester/motifs
  - Implement `getManchesterDiscriminators()` function that calls GET /api/clinical/manchester/discriminators
  - Add TypeScript interfaces: `ManchesterMotif`, `ManchesterDiscriminator`, `ManchesterCatalog`
  - _Requirements: 6.2_

- [x] 3. Update patientService.ts with getPatientById function
  - Modify `frontend-medflow/src/services/patientService.ts`
  - Add `getPatientById(patientId: string)` function that calls GET /api/patients/{id}
  - Handle 404 error when patient not found
  - _Requirements: 4.1_

- [x] 4. Create errorHandler.ts utility for error message extraction
  - Create file `frontend-medflow/src/utils/errorHandler.ts`
  - Implement `extractErrorMessage(error: unknown)` function that maps HTTP status codes to Spanish error messages
  - Implement `isHttpError(error: unknown, status: number)` function to check specific HTTP status codes
  - Map 400 → "Solicitud inválida", 404 → "Recurso no encontrado", 409 → "Esta cita ya tiene triaje registrado", 500 → "Error del servidor"
  - Handle network errors with "Error al conectar con el servidor"
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8_

- [ ]* 5. Write unit tests for service functions
  - Test `getPendingTriageAppointments()` returns array of appointments
  - Test `getAppointmentTriage()` returns triage response
  - Test `getAppointmentTriage()` throws error on 404
  - Test `performTriage()` includes appointmentId in request body
  - Test error handling for all service functions
  - _Requirements: 1.8_

- [x] 6. Checkpoint - Verify service layer works correctly
  - Run all service unit tests
  - Verify TypeScript compilation succeeds
  - Verify API calls use correct endpoints and request formats
  - Ensure all tests pass, ask the user if questions arise

### Phase 2: Basic Page Component and Routing

- [x] 7. Create TriagePendingPage.tsx component with basic structure
  - Create file `frontend-medflow/src/pages/vitals/TriagePendingPage.tsx`
  - Set up component with MainLayout wrapper
  - Define state interface `TriagePendingPageState` with all required state fields
  - Initialize state with useState hooks for appointments, selection, forms, errors
  - Add page title "Triaje Pendiente"
  - _Requirements: 2.1, 8.1_

- [x] 8. Update App.tsx with /vitals/triage route
  - Modify `frontend-medflow/src/App.tsx`
  - Import TriagePendingPage component
  - Add route `/vitals/triage` with ProtectedRoute wrapper requiring VITAL_SIGNS or DOCTOR role
  - _Requirements: 7.1, 7.2, 7.3_

- [x] 9. Update navigation menu with "Triaje Pendiente" link
  - Modify navigation component (MainLayout.tsx or equivalent)
  - Add "Triaje Pendiente" menu item visible to VITAL_SIGNS and DOCTOR roles
  - Use ClipboardDocumentCheckIcon from Heroicons
  - Link to `/vitals/triage`
  - _Requirements: 7.4, 7.5_

- [x] 10. Create basic page layout with placeholder sections
  - Add container div with Tailwind classes for responsive layout
  - Add placeholder sections for: pending list, patient details, forms
  - Use medin-cyan, medin-navy, medin-blue color scheme
  - Ensure responsive design (min-width: 320px)
  - _Requirements: 8.1, 8.2, 8.3, 8.5_

- [x] 11. Checkpoint - Verify page renders and routing works
  - Navigate to /vitals/triage as VITAL_SIGNS user
  - Verify page renders without errors
  - Verify route protection works (redirect for unauthorized users)
  - Verify navigation link appears for authorized roles
  - Ensure all tests pass, ask the user if questions arise

### Phase 3: Pending List Display with Auto-Refresh

- [x] 12. Create PendingAppointmentsList.tsx component
  - Create file `frontend-medflow/src/components/triage/PendingAppointmentsList.tsx`
  - Define props interface: appointments, selectedAppointmentId, onSelectAppointment, loading
  - Implement table with columns: Fecha, Hora, Paciente, Motivo, ID Cita
  - Add loading spinner for loading state
  - Add empty state message "No hay citas pendientes de triaje"
  - Use semantic HTML table elements (thead, tbody, th, td)
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 11.2_

- [x] 13. Create AppointmentRow.tsx component
  - Create file `frontend-medflow/src/components/triage/AppointmentRow.tsx`
  - Define props interface: appointment, isSelected, onSelect
  - Implement clickable table row with hover effect
  - Add highlight styling for selected row (bg-medin-cyan/10, border-l-4 border-medin-cyan)
  - Add keyboard navigation support (Enter, Space keys)
  - Add ARIA label for screen readers
  - Wrap with React.memo for performance
  - _Requirements: 2.6, 2.7, 11.5, 11.6_

- [x] 14. Implement data fetching in TriagePendingPage
  - Add `fetchPendingAppointments()` function that calls `getPendingTriageAppointments()`
  - Call `fetchPendingAppointments()` on component mount using useEffect
  - Handle loading state with spinner
  - Handle error state with error message "Error al cargar citas pendientes"
  - _Requirements: 2.1, 2.4, 2.5_

- [x] 15. Implement appointment sorting logic
  - Add `useMemo` hook to sort appointments by date ascending, then time ascending
  - Use `localeCompare()` for string comparison
  - Pass sorted appointments to PendingAppointmentsList component
  - _Requirements: 2.8_

- [x] 16. Implement auto-refresh functionality
  - Add `setInterval` in useEffect to call `fetchPendingAppointments()` every 30 seconds
  - Clear interval on component unmount
  - Do NOT show loading spinner during auto-refresh (avoid UI flicker)
  - Log errors to console during auto-refresh (do not display to user)
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 9.7_

- [x] 17. Add manual refresh button
  - Add "Actualizar" button above the pending list
  - Call `fetchPendingAppointments()` on button click
  - Reset the 30-second auto-refresh timer after manual refresh
  - Show loading spinner during manual refresh
  - _Requirements: 3.5, 3.6_

- [x] 18. Checkpoint - Verify pending list displays correctly
  - Verify appointments display in correct sorted order
  - Verify auto-refresh works every 30 seconds
  - Verify manual refresh button works
  - Verify loading and empty states display correctly
  - Ensure all tests pass, ask the user if questions arise

### Phase 4: Appointment Selection and Patient Details

- [x] 19. Create PatientDetailsCard.tsx component
  - Create file `frontend-medflow/src/components/triage/PatientDetailsCard.tsx`
  - Define props interface: patient, appointment, vitalSignsRecorded, onRecordVitalSigns, onPerformTriage, onCancel, loading
  - Display patient information: fullName, DPI, appointmentDate, appointmentTime, appointment ID
  - Add "Cancelar" button to clear selection
  - Add "Registrar Signos Vitales" button
  - Add "Realizar Triaje" button (disabled if vital signs not recorded)
  - Add tooltip "Primero registre los signos vitales" for disabled triage button
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7_

- [x] 20. Implement appointment selection logic in TriagePendingPage
  - Add `handleSelectAppointment()` function that accepts appointment parameter
  - Set selectedAppointment state
  - Call `getPatientById()` to fetch patient data
  - Set selectedPatient state
  - Handle loading state while fetching patient
  - Handle error if patient not found
  - _Requirements: 2.6, 2.7, 4.1_

- [x] 21. Implement cancel selection logic
  - Add `handleCancelSelection()` function
  - Clear selectedAppointment and selectedPatient state
  - Clear form states (showVitalSignsForm, showTriageForm)
  - Clear success/error messages
  - Return to list-only view
  - _Requirements: 4.7_

- [x] 22. Implement focus management for accessibility
  - Create ref for PatientDetailsCard
  - Move focus to patient details card when appointment selected
  - Use setTimeout to ensure DOM update before focus
  - Add tabIndex={-1} to patient details card for programmatic focus
  - _Requirements: 11.6_

- [x] 23. Add keyboard navigation for appointment selection
  - Handle Enter and Space keys on table rows
  - Prevent default behavior for Space key
  - Call handleSelectAppointment on key press
  - _Requirements: 11.5_

- [x] 24. Checkpoint - Verify appointment selection works
  - Click appointment row and verify patient details display
  - Verify selected row is highlighted
  - Verify cancel button clears selection
  - Verify keyboard navigation works (Enter, Space)
  - Verify focus moves to patient details card
  - Ensure all tests pass, ask the user if questions arise

### Phase 5: Vital Signs Integration

- [x] 25. Create VitalSignsForm.tsx component
  - Create file `frontend-medflow/src/components/triage/VitalSignsForm.tsx`
  - Define props interface: patientId, onSubmit, onCancel, submitting, error
  - Define form data interface with all vital signs fields
  - Initialize form state with useState
  - Add input fields: systolicPressure, diastolicPressure, heartRate, respiratoryRate, temperature, oxygenSaturation, weight (optional), height (optional)
  - Use consistent Tailwind styling matching existing pages
  - Add Spanish labels with required field indicators (*)
  - _Requirements: 5.1, 5.2, 8.3_

- [x] 26. Implement vital signs validation logic
  - Add `validate()` function that checks numeric ranges
  - Validate systolicPressure: 50-250 mmHg
  - Validate diastolicPressure: 30-150 mmHg
  - Validate heartRate: 20-300 lpm
  - Validate respiratoryRate: 5-60 rpm
  - Validate temperature: 30-45 °C
  - Validate oxygenSaturation: 50-100%
  - Display field-specific error messages below invalid inputs
  - _Requirements: 5.7, 12.1, 12.2, 12.5_

- [x] 27. Implement vital signs form submission
  - Add `handleSubmit()` function that validates form data
  - Call `onSubmit` prop with validated data
  - Convert string inputs to numbers
  - Handle optional fields (weight, height)
  - Prevent submission if validation fails
  - _Requirements: 5.3, 12.6_

- [x] 28. Integrate VitalSignsForm into TriagePendingPage
  - Add state for showVitalSignsForm, vitalSignsSubmitting, vitalSignsSuccess, vitalSignsError
  - Add `handleVitalSignsSubmit()` function that calls `recordVitalSigns()` service
  - Display success message "Signos vitales registrados exitosamente" on success
  - Enable "Realizar Triaje" button after successful vital signs recording
  - Display error message from API response on failure
  - _Requirements: 5.3, 5.4, 5.5, 5.6_

- [x] 29. Add "Registrar Signos Vitales" button handler
  - Add click handler to show VitalSignsForm
  - Set showVitalSignsForm state to true
  - Hide form on cancel or successful submission
  - _Requirements: 4.3_

- [ ]* 30. Write unit tests for VitalSignsForm validation
  - Test validation rejects out-of-range values
  - Test validation rejects non-numeric values
  - Test validation requires all required fields
  - Test form submission with valid data
  - Test error display for invalid inputs
  - _Requirements: 5.7, 12.1, 12.2_

- [x] 31. Checkpoint - Verify vital signs workflow works
  - Click "Registrar Signos Vitales" and verify form displays
  - Submit form with valid data and verify success message
  - Submit form with invalid data and verify validation errors
  - Verify "Realizar Triaje" button becomes enabled after success
  - Ensure all tests pass, ask the user if questions arise

### Phase 6: Triage Workflow Integration

- [x] 32. Create TriageForm.tsx component
  - Create file `frontend-medflow/src/components/triage/TriageForm.tsx`
  - Define props interface: patientId, appointmentId, onSubmit, onCancel, submitting, error
  - Define form data interface: motifId, discriminatorIds
  - Initialize form state with useState
  - Add state for Manchester catalog (motifs, discriminators)
  - Add loading state for catalog
  - _Requirements: 6.1, 6.2_

- [x] 33. Implement Manchester catalog loading
  - Add useEffect to call `getManchesterCatalog()` on component mount
  - Set catalogLoading state during fetch
  - Set motifs and discriminators state on success
  - Handle catalog loading error with message "Error al cargar el catálogo Manchester"
  - Display loading spinner while catalog loads
  - _Requirements: 6.2, 10.7_

- [x] 34. Implement motif selection dropdown
  - Add select element for motif selection
  - Populate options from motifs state
  - Add "Seleccione un motivo..." placeholder option
  - Handle onChange to update selectedMotifId state
  - Add required field indicator (*)
  - _Requirements: 6.2_

- [x] 35. Implement discriminator checkbox list
  - Add checkbox list for discriminator selection
  - Display discriminator name, description, and priority level
  - Use color-coded badges for priority levels (RED, ORANGE, YELLOW, GREEN, BLUE)
  - Handle checkbox toggle to update selectedDiscriminatorIds array
  - Add/remove discriminator ID from array on toggle
  - Display count of selected discriminators
  - Add scrollable container (max-height: 24rem) for long lists
  - _Requirements: 6.2_

- [x] 36. Implement triage form validation
  - Add validation: motif is required
  - Add validation: at least one discriminator is required
  - Display validation error message if validation fails
  - Prevent form submission if validation fails
  - _Requirements: 12.3, 12.4, 12.6_

- [x] 37. Implement triage form submission
  - Add `handleSubmit()` function that validates form data
  - Call `onSubmit` prop with motifId and discriminatorIds
  - _Requirements: 6.3_

- [x] 38. Integrate TriageForm into TriagePendingPage
  - Add state for showTriageForm, triageSubmitting, triageSuccess, triageError
  - Add `handleTriageSubmit()` function that calls `performTriage()` service with appointmentId, patientId, motifId, discriminatorIds
  - Display success message "Triaje registrado exitosamente — Prioridad: [priority level]" on success
  - Clear appointment selection after successful triage
  - Immediately refresh pending list after successful triage
  - Handle 409 error with message "Esta cita ya tiene triaje registrado"
  - Handle 400 error with message "El paciente no tiene signos vitales registrados"
  - Handle 404 error with message "Cita no encontrada"
  - _Requirements: 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 6.9_

- [x] 39. Add "Realizar Triaje" button handler
  - Add click handler to show TriageForm
  - Set showTriageForm state to true
  - Disable button if vital signs not recorded
  - Hide form on cancel or successful submission
  - _Requirements: 4.4, 4.5_

- [x] 40. Implement auto-refresh detection for triaged appointments
  - Check if selected appointment is still in pending list after auto-refresh
  - Display message "Esta cita ya tiene triaje registrado" if appointment removed
  - Clear selection if appointment no longer pending
  - _Requirements: 3.8_

- [x] 41. Checkpoint - Verify triage workflow works end-to-end
  - Select appointment, record vital signs, perform triage
  - Verify success message displays with priority level
  - Verify appointment is removed from pending list after triage
  - Verify 409 error is handled correctly
  - Verify 400 error is handled correctly (no vital signs)
  - Ensure all tests pass, ask the user if questions arise

### Phase 7: Error Handling and Polish

- [x] 42. Create ErrorAlert.tsx component
  - Create file `frontend-medflow/src/components/common/ErrorAlert.tsx`
  - Define props interface: message, onDismiss (optional)
  - Display error message with red-50 background, red-200 border, red-800 text
  - Add XCircleIcon from Heroicons
  - Add role="alert" for screen reader announcement
  - Add dismiss button if onDismiss prop provided
  - _Requirements: 8.8, 9.5, 11.4_

- [x] 43. Create SuccessAlert.tsx component
  - Create file `frontend-medflow/src/components/common/SuccessAlert.tsx`
  - Define props interface: message, onDismiss (optional)
  - Display success message with green-50 background, green-200 border, green-800 text
  - Add CheckCircleIcon from Heroicons
  - Add role="status" for screen reader announcement
  - Add dismiss button if onDismiss prop provided
  - _Requirements: 9.5, 11.4_

- [x] 44. Implement auto-dismiss for alerts
  - Add useEffect in TriagePendingPage to auto-dismiss success/error messages after 5 seconds
  - Clear success message state after timeout
  - Clear error message state after timeout
  - Clear timeout on component unmount
  - _Requirements: 9.6_

- [x] 45. Add retry buttons for failed operations
  - Add retry button to error alerts for failed API calls
  - Retry button calls the same operation again
  - Add retry for: pending list fetch, patient fetch, vital signs submission, triage submission
  - _Requirements: 9.8_

- [x] 46. Improve error messages for all scenarios
  - Use `extractErrorMessage()` utility for all API errors
  - Display field-specific validation errors below inputs
  - Display user-friendly Spanish messages for all error types
  - Test all error scenarios: network errors, 400, 404, 409, 500
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 12.5_

- [x] 47. Add loading states for all async operations
  - Add loading spinner for: pending list fetch, patient fetch, vital signs submission, triage submission, catalog loading
  - Use consistent spinner styling (animate-spin with medin-cyan border)
  - Disable buttons during submission
  - _Requirements: 2.4, 8.7_

- [x] 48. Checkpoint - Verify error handling works correctly
  - Test all error scenarios and verify appropriate messages display
  - Verify success messages display and auto-dismiss
  - Verify retry buttons work
  - Verify loading states display during async operations
  - Ensure all tests pass, ask the user if questions arise

### Phase 8: Accessibility and Testing

- [x] 49. Add ARIA labels to all interactive elements
  - Add aria-label to all buttons
  - Add aria-label to table rows
  - Add aria-label to form inputs (use htmlFor on labels)
  - Add aria-required to required form inputs
  - Add aria-invalid to invalid form inputs
  - Add aria-describedby to link error messages to inputs
  - _Requirements: 11.1, 11.8_

- [x] 50. Implement keyboard navigation for all interactions
  - Add Tab navigation through all interactive elements
  - Add Enter key support for buttons and table rows
  - Add Escape key support to close forms
  - Test keyboard-only navigation through entire workflow
  - _Requirements: 11.5_

- [x] 51. Verify color contrast ratios meet WCAG AA standards
  - Check all text colors against backgrounds
  - Ensure 4.5:1 contrast ratio for normal text
  - Ensure 3:1 contrast ratio for large text
  - Use priority colors with sufficient contrast (red-900, orange-900, yellow-900, green-900, blue-900)
  - _Requirements: 11.7_

- [x] 52. Add screen reader support
  - Add aria-live="polite" to loading spinners
  - Add aria-busy="true" during loading
  - Add role="alert" to error messages
  - Add role="status" to success messages
  - Add sr-only announcement region for dynamic content changes
  - Announce list updates to screen readers
  - _Requirements: 11.3, 11.4_

- [ ]* 53. Write unit tests for TriagePendingPage component
  - Test page renders without errors
  - Test pending appointments load and display
  - Test empty state displays when no appointments
  - Test appointment selection displays patient details
  - Test vital signs form submission
  - Test triage form submission
  - Test error handling for API failures
  - Test auto-refresh functionality
  - Use React Testing Library and Jest
  - _Requirements: All_

- [ ]* 54. Write property-based tests for sorting logic
  - **Property 1: Pending List Accuracy**
  - **Validates: Requirements 2.1, 2.2, 2.8**
  - Generate random sets of appointments with various dates and times
  - Verify sorting is correct (date ascending, then time ascending)
  - Verify no duplicates exist in sorted list
  - Verify all appointments preserved after sorting
  - Use fast-check library

- [ ]* 55. Write property-based tests for validation logic
  - **Property 5: Vital Signs Validation Completeness**
  - **Validates: Requirements 5.7, 12.1, 12.2**
  - Generate invalid vital signs inputs (out-of-range, non-numeric, missing fields)
  - Verify form rejects all invalid inputs
  - Verify appropriate error messages display
  - Verify no API call made for invalid inputs
  - Use fast-check library

- [ ]* 56. Write property-based tests for error handling
  - **Property 6: Error Message Clarity**
  - **Validates: Requirements 9.1, 9.2, 9.3, 9.4**
  - Generate API error responses with various status codes
  - Verify UI displays correct Spanish error message for each error code
  - Verify error messages are user-friendly (no technical jargon)
  - Use fast-check library

- [ ]* 57. Write integration tests with Cypress
  - Test complete triage workflow end-to-end
  - Test appointment selection and patient details display
  - Test vital signs recording
  - Test triage submission
  - Test error handling (409 conflict, 404 not found)
  - Test auto-refresh functionality
  - Use Cypress with MSW for API mocking

- [x] 58. Checkpoint - Verify accessibility and testing complete
  - Run all unit tests and verify 80%+ coverage
  - Run property-based tests and verify all properties pass
  - Run integration tests and verify all scenarios pass
  - Test with screen reader (NVDA or JAWS)
  - Test keyboard-only navigation
  - Ensure all tests pass, ask the user if questions arise

### Phase 9: Performance Optimization

- [x] 59. Add React.memo to table row components
  - Wrap AppointmentRow with React.memo
  - Add custom comparison function to check appointment.id and isSelected
  - Verify rows don't re-render unnecessarily
  - _Requirements: 10.1_

- [x] 60. Add useMemo for sorted appointment list
  - Wrap sorting logic in useMemo hook
  - Add appointments array as dependency
  - Verify sorting only runs when appointments change
  - _Requirements: 10.2_

- [x] 61. Add useCallback for event handlers
  - Wrap handleSelectAppointment in useCallback
  - Wrap handleCancelSelection in useCallback
  - Wrap handleVitalSignsSubmit in useCallback
  - Wrap handleTriageSubmit in useCallback
  - Add appropriate dependencies
  - _Requirements: 10.2_

- [x] 62. Implement debouncing for manual refresh button
  - Use lodash debounce or custom debounce hook
  - Debounce manual refresh with 500ms delay
  - Prevent multiple simultaneous API calls
  - _Requirements: 10.3_

- [x] 63. Implement cleanup on component unmount
  - Clear auto-refresh interval in useEffect cleanup
  - Abort pending API requests using AbortController
  - Clear all timeouts (auto-dismiss, debounce)
  - Verify no memory leaks with React DevTools Profiler
  - _Requirements: 10.5, 10.6_

- [x] 64. Implement lazy loading for Manchester catalog
  - Load catalog only when triage form is first opened
  - Cache catalog in component state after first load
  - Add catalogLoaded flag to prevent redundant fetches
  - _Requirements: 10.7_

- [ ] 65. Test performance with large datasets
  - Mock API to return 100 appointments
  - Measure render time from mount to list display
  - Verify 95th percentile render time is below 500ms
  - Use React DevTools Profiler to identify bottlenecks
  - _Requirements: 10.4_

- [x] 66. Checkpoint - Verify performance optimizations work
  - Run performance tests and verify targets met
  - Verify no unnecessary re-renders with React DevTools
  - Verify memory leaks are prevented
  - Verify large datasets render smoothly
  - Ensure all tests pass, ask the user if questions arise

### Phase 10: Documentation and Deployment

- [x] 67. Update README with feature documentation
  - Document new /vitals/triage page
  - Document user roles (VITAL_SIGNS, DOCTOR)
  - Document triage workflow steps
  - Add screenshots of key UI states
  - _Requirements: All_

- [x] 68. Create user guide for triage staff
  - Document how to access triage page
  - Document how to select appointments
  - Document how to record vital signs
  - Document how to perform triage
  - Document error messages and troubleshooting
  - Write in Spanish for end users

- [x] 69. Add JSDoc comments to all functions
  - Document all service functions with @param and @returns
  - Document all component props with JSDoc
  - Document all utility functions
  - Document complex logic with inline comments

- [ ] 70. Create deployment checklist
  - Verify backend endpoints are deployed and accessible
  - Verify API Gateway routes are configured
  - Verify CORS is configured for frontend domain
  - Verify JWT authentication is working
  - Verify role-based access control is configured
  - Verify environment variables are set (REACT_APP_API_BASE_URL)

- [ ] 71. Test in staging environment
  - Deploy to staging environment
  - Test complete triage workflow with real backend
  - Test with multiple concurrent users
  - Test auto-refresh with real data
  - Test error scenarios (network failures, API errors)
  - Verify performance meets requirements

- [ ] 72. Perform smoke tests before production deployment
  - Test login as VITAL_SIGNS user
  - Test navigation to /vitals/triage
  - Test pending list displays
  - Test appointment selection
  - Test vital signs recording
  - Test triage submission
  - Test error handling

- [ ] 73. Deploy to production
  - Build production bundle with `npm run build`
  - Deploy to production environment
  - Verify deployment succeeds
  - Monitor error logs for issues
  - Monitor performance metrics

- [ ] 74. Final checkpoint - Verify production deployment
  - Test complete workflow in production
  - Verify all features work correctly
  - Verify performance meets requirements
  - Monitor for errors in first 24 hours
  - Ensure all tests pass, ask the user if questions arise

## Notes

- **Property-Based Tests:** Tasks marked with `*` include property-based tests that validate universal correctness properties. These are optional for MVP but highly recommended for production quality.

- **Incremental Validation:** Checkpoint tasks ensure each phase is validated before moving to the next. This catches errors early and maintains system stability.

- **Requirements Traceability:** Each task explicitly references the requirements it implements, ensuring complete coverage.

- **React/TypeScript Stack:** All implementation uses React with TypeScript, Tailwind CSS, React Router, and Axios for API calls.

- **Accessibility Compliance:** Phase 8 ensures WCAG 2.1 AA compliance with ARIA labels, keyboard navigation, color contrast, and screen reader support.

- **Performance Guarantees:** Phase 9 validates that rendering performance meets the specified requirements (500ms for 100 appointments).

- **Spanish Localization:** All user-facing text, error messages, and labels are in Spanish to match the existing application.

- **Testing Strategy:** Uses React Testing Library for unit tests, fast-check for property-based tests, and Cypress for integration tests. Aim for 80%+ code coverage.

- **Error Handling:** All API errors follow REST conventions with appropriate HTTP status codes (400, 404, 409, 500) and user-friendly Spanish error messages.

- **Auto-Refresh:** The 30-second auto-refresh continues in the background even during user interactions, ensuring the list stays current.

## Total Estimated Time: 32-42 hours (4-5 days)
