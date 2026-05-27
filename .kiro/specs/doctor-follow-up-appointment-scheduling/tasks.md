# Implementation Plan

## Overview
This task list implements the bugfix for doctor follow-up appointment scheduling using the bug condition methodology. The workflow follows: Explore → Preserve → Implement → Validate.

---

## Phase 1: Exploration (Before Fix)

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Simple Date Input Without Validation
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: Test the follow-up scheduling section when checkbox is checked
  - Test implementation details from Bug Condition in design:
    - When "Agendar cita de seguimiento" checkbox is checked
    - Verify that a simple HTML `<input type="date">` is rendered (not a Calendar component)
    - Verify that no time slot selection UI is present
    - Verify that past dates are NOT blocked (can be selected)
    - Verify that no API call to `getAvailableSlotsForDate` is made
    - Verify that no doctor data loading occurs
  - The test assertions should match the Expected Behavior Properties from design:
    - Should display Calendar component with date blocking
    - Should display time slot selection grid
    - Should validate availability in real-time
    - Should filter past time slots for today
    - Should auto-assign current patient
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found to understand root cause
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_

---

## Phase 2: Preservation (Before Fix)

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Consultation Form Behavior Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-follow-up interactions:
    - Patient information display
    - Vital signs display
    - Triage information display
    - Clinical evaluation fields (chief complaint, symptoms, diagnosis)
    - Destination selection (LAB, PHARMACY, EXTERNAL_RX, DISCHARGE)
    - Lab test selection
    - Medication prescription
    - Form submission without follow-up
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements:
    - Test that patient data display is unchanged
    - Test that vital signs rendering is unchanged
    - Test that diagnosis entry (CIE-10 combobox) is unchanged
    - Test that destination selection logic is unchanged
    - Test that lab test selection is unchanged
    - Test that medication prescription is unchanged
    - Test that form submission (without follow-up) is unchanged
    - Test that navigation and error handling is unchanged
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

---

## Phase 3: Implementation

- [ ] 3. Fix for doctor follow-up appointment scheduling

  - [x] 3.1 Extract and create shared Calendar component
    - Extract Calendar component from `BookAppointmentForm.tsx` (lines 90-170)
    - Create new file: `frontend-medflow/src/components/Calendar/Calendar.tsx`
    - Export Calendar component with props: `selected`, `onSelect`, `isBlocked`
    - Extract helper functions: `toDateStr`, `shiftSlots`, `fmt`, `DAY_NAMES`, `MONTH_NAMES`
    - Create `frontend-medflow/src/components/Calendar/index.ts` for exports
    - Update `BookAppointmentForm.tsx` to import from shared component
    - _Bug_Condition: isBugCondition(input) where input.followUpCheckboxChecked == true AND input.uiComponent == 'simple-date-input'_
    - _Expected_Behavior: Display Calendar component with date blocking logic_
    - _Preservation: Existing BookAppointmentForm functionality must remain unchanged_
    - _Requirements: 2.1, 2.4, 2.5_

  - [x] 3.2 Add doctor data loading to PatientConsultationForm
    - Import `listActiveDoctors` and `getDoctorDaysOff` from `doctorService`
    - Add state: `doctors: Doctor[]`, `daysOffMap: Record<string, string[]>`, `loadingDoctorData: boolean`
    - Add useEffect on component mount to load doctor data
    - Build `daysOffMap` structure mapping doctor IDs to date strings (YYYY-MM-DD format)
    - Handle loading and error states gracefully
    - _Bug_Condition: No doctor data loading occurs when follow-up checkbox is checked_
    - _Expected_Behavior: Load doctor shifts and days off to enable availability validation_
    - _Preservation: Existing data loading (appointment, vital signs, triage, catalogs) must remain unchanged_
    - _Requirements: 2.2, 2.5_

  - [x] 3.3 Add time slot state management
    - Add state: `followUpTime: string` (selected time slot)
    - Add state: `availableSlots: string[]` (available time slots from backend)
    - Add state: `loadingSlots: boolean` (loading indicator)
    - Update existing `followUpDate` state to work with Calendar component
    - Initialize states with empty/default values
    - _Bug_Condition: No time slot selection exists in current implementation_
    - _Expected_Behavior: Manage time slot selection state with availability tracking_
    - _Preservation: Existing form state management must remain unchanged_
    - _Requirements: 2.2, 2.3_

  - [x] 3.4 Implement calendar blocking logic
    - Create `isBlocked` function using useMemo
    - Check if ALL doctors are on day-off for a given date
    - Use `daysOffMap` to determine blocked dates
    - Return true if date is blocked, false otherwise
    - Ensure past dates are blocked by Calendar component itself
    - _Bug_Condition: Current implementation allows selection of days off and past dates_
    - _Expected_Behavior: Block dates when all doctors are unavailable or date is in the past_
    - _Preservation: No impact on other form logic_
    - _Requirements: 2.4, 2.5_

  - [x] 3.5 Implement time slot generation and filtering
    - Create `shiftSlotsForDate` function using useMemo
    - Filter doctors who are NOT on day-off for selected date
    - Generate 30-minute slots using `shiftSlots(shiftStart, shiftEnd)` for each available doctor
    - Combine and deduplicate slots, then sort
    - Create `displaySlots` function to filter past slots when today is selected
    - Compare slot time with current time in minutes
    - Keep only slots at least 30 minutes in the future
    - _Bug_Condition: No time slot generation or filtering exists_
    - _Expected_Behavior: Generate available time slots based on doctor shifts and filter past slots for today_
    - _Preservation: No impact on other form logic_
    - _Requirements: 2.2, 2.6_

  - [x] 3.6 Add availability checking when date changes
    - Add useEffect that triggers when `followUpDate` changes
    - Call `getAvailableSlotsForDate(followUpDate)` service
    - Update `availableSlots` state with response
    - Set `loadingSlots` to true during fetch, false after
    - Clear `followUpTime` when date changes
    - Handle errors gracefully (set empty array)
    - _Bug_Condition: No availability validation occurs in current implementation_
    - _Expected_Behavior: Load and validate available slots in real-time when date is selected_
    - _Preservation: Existing useEffect hooks must remain unchanged_
    - _Requirements: 2.3_

  - [x] 3.7 Replace UI with Calendar and time slot grid
    - Import Calendar component from shared location
    - Replace `<input type="date">` (around line 810) with Calendar component
    - Pass props: `selected={followUpDate}`, `onSelect={setFollowUpDate}`, `isBlocked={isBlocked}`
    - Add time slot selection grid below calendar (when date is selected)
    - Use 3-column grid layout similar to `BookAppointmentForm.tsx`
    - Show slot availability status: available (white), selected (cyan), taken (gray strikethrough)
    - Add loading spinner while `loadingSlots` is true
    - Show "No hay horarios para esta fecha" message when `displaySlots.length === 0`
    - Handle slot selection: update `followUpTime` state
    - _Bug_Condition: Current UI shows simple date input without validation_
    - _Expected_Behavior: Display interactive calendar with time slot selection and availability indicators_
    - _Preservation: Existing follow-up notes input and checkbox must remain unchanged_
    - _Requirements: 2.1, 2.2, 2.3, 2.6_

  - [x] 3.8 Update form validation
    - Update `handleSubmit` validation logic
    - Check that both `followUpDate` AND `followUpTime` are selected when `followUp` is true
    - Show error message if follow-up is checked but time is not selected
    - Clear time-related errors when user selects a time slot
    - Maintain existing validation for other fields
    - _Bug_Condition: Current validation only checks date, not time_
    - _Expected_Behavior: Validate both date and time selection before allowing submission_
    - _Preservation: Existing validation logic for other fields must remain unchanged_
    - _Requirements: 2.3, 2.7_

  - [x] 3.9 Update appointment creation to include time
    - Modify consultation registration payload to include `followUpTime`
    - Ensure `registerConsultation` service accepts time parameter
    - Pass both `followUpDate` and `followUpTime` to backend
    - Update success message to confirm time was included
    - Note: May require backend API changes if current endpoint doesn't accept time
    - _Bug_Condition: Current implementation doesn't pass time to backend_
    - _Expected_Behavior: Create follow-up appointment with both date and time_
    - _Preservation: Existing consultation registration logic must remain unchanged_
    - _Requirements: 2.7, 3.1_

  - [x] 3.10 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Calendar-Based Scheduling with Validation
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - Verify that:
      - Calendar component is rendered (not simple date input)
      - Time slot selection UI is present
      - Past dates are blocked
      - Days off are blocked
      - Availability checking occurs
      - Past time slots are filtered for today
      - Patient is auto-assigned
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

  - [x] 3.11 Verify preservation tests still pass
    - **Property 2: Preservation** - Consultation Form Behavior Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - Verify that all non-follow-up form interactions produce identical results:
      - Patient information display
      - Vital signs display
      - Triage information display
      - Clinical evaluation fields
      - Destination selection
      - Lab test selection
      - Medication prescription
      - Form submission without follow-up
      - Navigation and error handling
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix (no regressions)
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

---

## Phase 4: Validation

- [x] 4. Checkpoint - Ensure all tests pass
  - Run all exploration tests - should PASS (bug is fixed)
  - Run all preservation tests - should PASS (no regressions)
  - Run unit tests for Calendar component
  - Run unit tests for time slot generation and filtering
  - Run integration tests for full consultation flow with follow-up
  - Verify visual feedback (loading spinners, disabled states, error messages)
  - Test edge cases:
    - Selecting today's date filters past time slots
    - Selecting a date when all doctors are off shows no slots
    - Unchecking follow-up checkbox clears selection
    - Switching between dates updates available slots
  - Ensure all tests pass, ask the user if questions arise

---

## Notes

- **Testing Framework**: Use React Testing Library with Jest for component tests
- **Property-Based Testing**: Consider using fast-check for property-based tests if available
- **Backend Dependency**: Task 3.9 may require backend API changes - coordinate with backend team
- **Reusability**: The shared Calendar component can be reused in other parts of the application
- **Performance**: Use useMemo for expensive computations (date blocking, slot generation)
- **Accessibility**: Ensure Calendar and time slot grid are keyboard-accessible and screen-reader friendly
