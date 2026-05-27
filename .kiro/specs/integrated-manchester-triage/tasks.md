# Implementation Plan: Integrated Manchester Triage

## Overview

This implementation plan converts the two-step progressive workflow design into discrete coding tasks. The feature integrates Manchester Triage System classification directly into the vital signs capture screen, enabling staff to complete both vital signs capture (Step 1) and Manchester classification (Step 2) in the same interface with independent save operations at each step.

**Key Implementation Approach:**
- Enhance existing `TriageVitalSignsCapture.tsx` component with two-step workflow logic
- Add new service function to fetch existing vital signs
- Implement conditional UI visibility based on workflow state
- Add Manchester catalog loading and selection UI
- Implement client-side priority calculation
- Update pending triage list to show appointment status

## Tasks

- [x] 1. Set up Manchester service integration and state management
  - Import Manchester service functions (`getManchesterCatalog`)
  - Add new state variables for two-step workflow control (`vitalSignsSaved`, `vitalSignsLocked`, `savingVitalSigns`, `vitalSignsError`, `vitalSignsSuccess`)
  - Add new state variables for Manchester catalog (`catalogLoading`, `catalogError`, `motifs`, `discriminators`)
  - Add new state variables for Manchester selection (`selectedMotifId`, `selectedDiscriminatorIds`, `calculatedPriority`)
  - Add new state variables for Step 2 submission (`savingTriage`, `triageError`, `triageSuccess`)
  - Define `PRIORITY_MAP` constant with color and description mappings for all priority levels
  - _Requirements: 1.1, 1.4, 7.3, 7.4_

- [x] 2. Implement component initialization with parallel data loading
  - [x] 2.1 Create initialization effect to load Manchester catalog and check existing vital signs
    - Use `Promise.all` or parallel async calls to fetch Manchester catalog and existing vital signs
    - Handle Manchester catalog fetch errors with retry option
    - Handle existing vital signs fetch (404 is expected if no vital signs exist)
    - Set appropriate loading states during initialization
    - _Requirements: 1.1, 1.2, 1.3, 12.1_
  
  - [ ]* 2.2 Write property test for state initialization from existing vital signs
    - **Property 15: State Initialization from Existing Vital Signs**
    - **Validates: Requirements 12.2, 12.3, 12.4, 12.5**
  
  - [x] 2.3 Add service function to fetch existing vital signs by appointment
    - Create `getVitalSignsByAppointment(appointmentId: string)` in `clinicalService.ts`
    - Return existing vital signs data or throw 404 if not found
    - _Requirements: 12.1_

- [x] 3. Implement Step 1: Vital signs capture and submission
  - [x] 3.1 Modify vital signs form to support field locking
    - Add `disabled` prop to all vital signs input fields based on `vitalSignsLocked || savingVitalSigns`
    - Preserve existing validation rules and field layout
    - _Requirements: 2.2, 2.3, 2.6, 9.4_
  
  - [ ]* 3.2 Write property test for vital signs field editability
    - **Property 12: Vital Signs Field Editability**
    - **Validates: Requirements 2.6, 9.4**
  
  - [x] 3.3 Create Step 1 validation function
    - Implement `validateVitalSigns()` function that checks only vital signs fields
    - Return validation result with error messages array
    - Enforce ranges: systolic [50-250], diastolic [30-150], heart rate [20-300], respiratory rate [5-60], temperature [30-45], oxygen saturation [50-100]
    - _Requirements: 9.1, 2.2_
  
  - [ ]* 3.4 Write property test for Step 1 validation completeness
    - **Property 6: Step 1 Validation Completeness**
    - **Validates: Requirements 9.1**
  
  - [ ]* 3.5 Write property test for vital signs validation rules
    - **Property 8: Vital Signs Validation Rules**
    - **Validates: Requirements 2.2**
  
  - [x] 3.6 Implement Step 1 submission handler
    - Create `handleSaveVitalSigns` function that validates and submits vital signs only
    - Call `recordVitalSigns` API with vital signs data
    - On success: set `vitalSignsSaved = true`, `vitalSignsLocked = true`, `vitalSignsSuccess = true`
    - On error: display error message and keep form editable
    - Show loading state during submission ("Guardando...")
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.7, 9.8_
  
  - [x] 3.7 Add conditional rendering for Step 1 button
    - Show "Guardar Signos Vitales" button only when `!vitalSignsSaved && !triageSuccess`
    - Hide button after Step 1 completes
    - _Requirements: 8.1, 8.2, 9.6_
  
  - [ ]* 3.8 Write property test for Step 1 button visibility
    - **Property 10: Step 1 Button Visibility**
    - **Validates: Requirements 8.1, 8.2, 9.6**
  
  - [x] 3.9 Add Step 1 success message display
    - Show green success alert with message "✓ Signos vitales guardados exitosamente" when `vitalSignsSuccess === true`
    - Include helper text "Ahora puede completar la clasificación Manchester"
    - _Requirements: 9.3, 17.1_
  
  - [x] 3.10 Add Step 1 error message display
    - Show red error alert with extracted error message when `vitalSignsError !== null`
    - _Requirements: 9.7, 11.1, 11.3_

- [ ] 4. Checkpoint - Ensure Step 1 implementation is complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Implement Manchester catalog loading and filtering
  - [x] 5.1 Create derived data for filtered discriminators
    - Use `useMemo` to filter discriminators by `selectedMotifId` and `active === true`
    - Return empty array if no motif selected
    - _Requirements: 5.1_
  
  - [ ]* 5.2 Write property test for discriminator filtering by motif
    - **Property 2: Discriminator Filtering by Motif**
    - **Validates: Requirements 5.1**
  
  - [x] 5.3 Create derived data for sorted motifs
    - Use `useMemo` to filter motifs by `active === true` and sort alphabetically by description
    - Use Spanish locale for sorting (`localeCompare('es')`)
    - _Requirements: 4.1, 4.3_
  
  - [ ]* 5.4 Write property test for active motif filtering
    - **Property 1: Active Motif Filtering**
    - **Validates: Requirements 4.1, 4.3**
  
  - [x] 5.5 Add catalog loading state display
    - Show spinner and "Cargando catálogo Manchester..." text while `catalogLoading === true`
    - Disable motif and discriminator controls during loading
    - _Requirements: 1.2, 16.1, 16.2, 16.3, 16.4_
  
  - [x] 5.6 Add catalog error display with retry option
    - Show red error alert with error message when `catalogError !== null`
    - Include "Reintentar" button that reloads the page
    - _Requirements: 1.3_

- [x] 6. Implement Manchester classification UI (Step 2)
  - [x] 6.1 Add conditional rendering for Manchester section
    - Show Manchester classification section only when `vitalSignsSaved === true`
    - Hide section completely when `vitalSignsSaved === false`
    - Use appropriate spacing and visual distinction from vital signs form
    - _Requirements: 3.1, 3.2, 3.6_
  
  - [ ]* 6.2 Write property test for Manchester section visibility
    - **Property 9: Manchester Section Visibility**
    - **Validates: Requirements 3.1, 3.2**
  
  - [x] 6.3 Implement motif selection dropdown
    - Create dropdown populated with sorted active motifs
    - Display motif description as option text
    - Show placeholder "Seleccione motivo de consulta" when no selection
    - Mark as required field
    - Handle selection change to update `selectedMotifId`
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_
  
  - [x] 6.4 Implement discriminator selection area
    - Display filtered discriminators as checkboxes
    - Show message "Seleccione un motivo para ver discriminadores" when no motif selected
    - Show message "No hay discriminadores disponibles para este motivo" when motif has no discriminators
    - Allow multiple discriminator selection
    - Update `selectedDiscriminatorIds` array on checkbox change
    - _Requirements: 5.2, 5.3, 6.1, 6.2, 6.3, 6.4, 6.5_
  
  - [x] 6.5 Implement motif change handler to clear discriminator selections
    - When motif selection changes, clear `selectedDiscriminatorIds` array
    - _Requirements: 5.4_
  
  - [ ]* 6.6 Write property test for discriminator selection clearing
    - **Property 3: Discriminator Selection Clearing**
    - **Validates: Requirements 5.4**

- [x] 7. Implement priority level calculation and preview
  - [x] 7.1 Create priority calculation function
    - Implement `calculateMaxPriority(discriminators)` that returns highest priority level
    - Use priority ordering: RED > ORANGE > YELLOW > GREEN > BLUE
    - Return null if no discriminators selected
    - _Requirements: 7.1_
  
  - [ ]* 7.2 Write property test for maximum priority calculation
    - **Property 4: Maximum Priority Calculation**
    - **Validates: Requirements 7.1**
  
  - [x] 7.3 Add effect to update calculated priority when discriminator selection changes
    - Use `useEffect` to recalculate priority when `selectedDiscriminatorIds` changes
    - Update within 100ms of selection change
    - Set `calculatedPriority` state with level and description
    - _Requirements: 7.1, 7.5_
  
  - [x] 7.4 Implement priority level preview display
    - Show color-coded badge with priority level and description when `calculatedPriority !== null`
    - Use `PRIORITY_MAP` for color classes and description text
    - Display in prominent preview area within Manchester section
    - _Requirements: 7.2, 7.3, 7.4_
    - _Requirements: 7.2, 7.3, 7.4_
  
  - [ ]* 7.5 Write property test for priority display mapping
    - **Property 5: Priority Display Mapping**
    - **Validates: Requirements 7.3, 7.4**

- [x] 8. Implement Step 2: Manchester classification submission
  - [x] 8.1 Create Step 2 validation function
    - Implement `validateManchesterSelection()` that checks only motif and discriminator selection
    - Return validation result with error messages array
    - Fail if no motif selected OR no discriminators selected
    - _Requirements: 10.1, 10.2_
  
  - [ ]* 8.2 Write property test for Step 2 validation completeness
    - **Property 7: Step 2 Validation Completeness**
    - **Validates: Requirements 10.1, 10.2**
  
  - [x] 8.3 Implement Step 2 submission handler
    - Create `handleSaveTriage` function that validates and submits Manchester classification only
    - Call `performTriage` API with appointmentId, patientId, motifId, and discriminatorIds
    - On success: set `triageSuccess = true`, show success message, navigate after 2 seconds
    - On error: display error message and keep form editable
    - Show loading state during submission ("Guardando...")
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_
  
  - [x] 8.4 Add conditional rendering for Step 2 button
    - Show "Guardar Clasificación Manchester" button only when `vitalSignsSaved === true && triageSuccess === false`
    - _Requirements: 8.3_
  
  - [ ]* 8.5 Write property test for Step 2 button visibility
    - **Property 11: Step 2 Button Visibility**
    - **Validates: Requirements 8.3**
  
  - [x] 8.6 Add Step 2 success message display
    - Show green success alert with message "✓ Clasificación Manchester registrada exitosamente" when `triageSuccess === true`
    - Include countdown message "Redirigiendo a Triaje Pendiente..."
    - Hide form to prevent duplicate submissions
    - _Requirements: 10.4, 17.2, 17.3, 17.4_
  
  - [x] 8.7 Add Step 2 error message display
    - Show red error alert with extracted error message when `triageError !== null`
    - _Requirements: 10.6, 11.2, 11.3_

- [ ] 9. Checkpoint - Ensure Step 2 implementation is complete
  - Ensure all tests pass, ask the user if questions arise.

- [x] 10. Implement error handling and form preservation
  - [x] 10.1 Ensure form data preservation on API errors
    - Verify that form state and Manchester selections remain unchanged when API errors occur
    - Re-enable submit buttons after errors
    - _Requirements: 11.3, 11.4_
  
  - [ ]* 10.2 Write property test for form data preservation on error
    - **Property 13: Form Data Preservation on Error**
    - **Validates: Requirements 11.4**
  
  - [x] 10.3 Implement user-friendly error message extraction
    - Ensure `extractErrorMessage` utility extracts readable error messages from API responses
    - Avoid displaying raw error objects or stack traces
    - _Requirements: 11.5_
  
  - [ ]* 10.4 Write property test for error message extraction
    - **Property 14: Error Message Extraction**
    - **Validates: Requirements 11.5**

- [x] 11. Update cancel functionality and navigation
  - [x] 11.1 Update cancel button behavior
    - Ensure "Cancelar" button is visible at all times
    - Navigate to `/vitals/triage` without saving when clicked before Step 1
    - Navigate to `/vitals/triage` when clicked after Step 1 (vital signs remain saved)
    - Disable cancel button during submission (`savingVitalSigns || savingTriage`)
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

- [x] 12. Update pending triage list with appointment status
  - [x] 12.1 Modify TriagePendingPage component to display appointment status
    - Add status label "Signos vitales registrados" for appointments with vital signs but no Manchester classification
    - Add status label "Pendiente de signos vitales" for appointments without vital signs
    - Use distinct colors to differentiate between states (e.g., blue for vital signs recorded, gray for pending)
    - Continue displaying appointments with vital signs recorded in the pending triage queue
    - _Requirements: 14.1, 14.2, 14.3, 14.4_

- [x] 13. Verify responsive layout and accessibility
  - [x] 13.1 Test responsive grid layouts
    - Verify vital signs fields display in 4-column grid on large screens
    - Verify vital signs fields display in 2-column grid on medium screens
    - Verify vital signs fields display in 1-column grid on small screens
    - Verify Manchester section maintains readability on all screen sizes
    - _Requirements: 18.1, 18.2, 18.3, 18.4, 18.5_
  
  - [x] 13.2 Verify patient information display preservation
    - Ensure patient information card displays full name, DPI, and email
    - Ensure "Llamar Paciente" button with speech synthesis works correctly
    - Maintain existing styling and layout
    - _Requirements: 15.1, 15.2, 15.3, 15.4_

- [ ] 14. Final checkpoint - Integration testing and verification
  - Ensure all tests pass, ask the user if questions arise.
  - Verify complete two-step workflow from component mount to successful submission
  - Test resume capability with existing vital signs
  - Verify all error scenarios display appropriate messages
  - Confirm navigation flow works correctly

## Notes

- Tasks marked with `*` are optional property-based tests and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties across all inputs
- Unit tests (not listed as separate tasks) should be written alongside implementation tasks
- The design uses TypeScript with React, so all implementation will be in TypeScript
- Existing component structure and styling should be preserved where possible
- Manchester catalog loading and vital signs checking happen in parallel for optimal performance
