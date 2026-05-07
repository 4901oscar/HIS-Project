# Doctor Follow-Up Appointment Scheduling Bugfix Design

## Overview

The doctor's consultation form currently uses a basic date input field for scheduling follow-up appointments without any validation of doctor availability, time slots, or business rules. This creates operational problems including double-booking, scheduling on days off, and selecting past dates. The fix will replace the simple date input with a full calendar and time slot selection UI similar to the admission and web booking systems, ensuring real-time availability validation and preventing scheduling conflicts.

## Glossary

- **Bug_Condition (C)**: The condition that triggers the bug - when a doctor attempts to schedule a follow-up appointment using the simple date/notes fields without availability validation
- **Property (P)**: The desired behavior when scheduling follow-up appointments - the system should display a visual calendar with validated time slots based on doctor shifts and availability
- **Preservation**: Existing consultation form behavior (saving consultations, creating appointments, associating with patients) that must remain unchanged by the fix
- **PatientConsultationForm**: The React component in `frontend-medflow/src/pages/doctor/PatientConsultationForm.tsx` that handles the doctor's consultation workflow
- **Calendar Component**: The reusable calendar UI component from `BookAppointmentForm.tsx` and `ActivateAppointments.tsx` that displays dates with blocking logic
- **Time Slot**: A 30-minute appointment window generated from doctor shift times (shiftStart to shiftEnd)
- **Doctor Shift**: The working hours defined by `shiftStart` and `shiftEnd` properties on the Doctor entity
- **Days Off**: Dates when a doctor is not available, stored in the `DayOff` entity and retrieved via `getDoctorDaysOff` service
- **Hold Mechanism**: The temporary reservation system used in web booking to prevent double-booking during the selection process

## Bug Details

### Bug Condition

The bug manifests when a doctor checks the "Agendar cita de seguimiento" checkbox in the consultation form. The current implementation shows only a simple HTML date input and a notes field without any validation of doctor availability, time slots, or business rules.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type FollowUpSchedulingAttempt
  OUTPUT: boolean
  
  RETURN input.followUpCheckboxChecked == true
         AND input.uiComponent == 'simple-date-input'
         AND NOT hasCalendarComponent(input.ui)
         AND NOT hasTimeSlotSelection(input.ui)
         AND NOT hasAvailabilityValidation(input.backend)
END FUNCTION
```

### Examples

- **Example 1**: Doctor checks "Agendar cita de seguimiento" → System shows `<input type="date">` and notes field → Doctor can select any date including past dates, weekends, or days off → No validation occurs
- **Example 2**: Doctor selects a date that already has 16 appointments booked in 30-minute slots (full day) → System allows the selection → Creates appointment conflict
- **Example 3**: Doctor selects today's date at 3:00 PM when current time is 3:15 PM → System allows past time selection → Creates invalid appointment
- **Edge case**: Doctor selects a date when the doctor is on vacation (day off) → System allows the selection → Creates appointment on unavailable day

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Consultation form submission and validation must continue to work exactly as before
- Creating appointments in the database with patient association must remain unchanged
- Saving consultation data (diagnosis, medications, lab orders) must remain unchanged
- Navigation between consultation form sections must remain unchanged
- Error handling and success messages for consultation submission must remain unchanged
- The ability to add notes to follow-up appointments must remain unchanged

**Scope:**
All inputs and interactions that do NOT involve the follow-up appointment scheduling section should be completely unaffected by this fix. This includes:
- Patient information display
- Vital signs display
- Triage information display
- Clinical evaluation fields (chief complaint, symptoms, diagnosis)
- Destination selection (LAB, PHARMACY, EXTERNAL_RX, DISCHARGE)
- Lab test selection
- Medication prescription
- Form submission and navigation

## Hypothesized Root Cause

Based on the bug description and code analysis, the root causes are:

1. **Missing Calendar Component**: The follow-up section uses a simple HTML `<input type="date">` instead of the custom Calendar component used in `BookAppointmentForm.tsx` and `ActivateAppointments.tsx`
   - Lines 800-810 in `PatientConsultationForm.tsx` show the simple implementation
   - No calendar blocking logic for past dates or days off

2. **Missing Time Slot Selection**: The current implementation has no time selection at all
   - Only date is captured, no time field exists
   - No integration with doctor shift times or 30-minute slot generation
   - No availability checking against existing appointments

3. **Missing Doctor Data Loading**: The form does not load doctor information or days off
   - No `listActiveDoctors()` call
   - No `getDoctorDaysOff()` call
   - No state management for doctor shifts and availability

4. **Missing Availability Validation**: No backend validation of slot availability
   - No call to `getAvailableSlotsForDate()` service
   - No real-time checking of appointment conflicts
   - No filtering of past time slots for today's date

5. **Missing Hold Mechanism**: No temporary slot reservation during selection
   - Web booking uses `holdSlot()` and `releaseHold()` to prevent double-booking
   - Follow-up scheduling has no such protection

## Correctness Properties

Property 1: Bug Condition - Calendar-Based Scheduling with Validation

_For any_ follow-up appointment scheduling attempt where the doctor checks the "Agendar cita de seguimiento" checkbox, the fixed form SHALL display a visual calendar component that blocks past dates and doctor days off, show available 30-minute time slots based on doctor shifts, validate slot availability in real-time, filter past time slots if today is selected, and automatically assign the current consultation's patient to the appointment.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7**

Property 2: Preservation - Consultation Form Behavior

_For any_ interaction with the consultation form that does NOT involve the follow-up appointment scheduling section (patient display, vital signs, diagnosis entry, destination selection, lab orders, prescriptions, form submission), the fixed code SHALL produce exactly the same behavior as the original code, preserving all existing functionality for consultation management.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6**

## Fix Implementation

### Changes Required

Assuming our root cause analysis is correct:

**File**: `frontend-medflow/src/pages/doctor/PatientConsultationForm.tsx`

**Function**: `PatientConsultationForm` component

**Specific Changes**:

1. **Add Calendar Component Import**: Import the Calendar component logic from reference implementations
   - Extract the Calendar component from `BookAppointmentForm.tsx` or `ActivateAppointments.tsx`
   - Import helper functions: `toDateStr`, `shiftSlots`, `fmt`, `DAY_NAMES`, `MONTH_NAMES`
   - Consider creating a shared component in `src/components/Calendar/` for reusability

2. **Add Doctor Data Loading**: Load doctor information and days off on component mount
   - Import `listActiveDoctors` and `getDoctorDaysOff` from `doctorService`
   - Add state: `doctors`, `daysOffMap`, `loadingDoctorData`
   - Add useEffect to load data when component mounts
   - Build `daysOffMap` structure: `Record<string, string[]>` mapping doctor IDs to date strings

3. **Add Time Slot State Management**: Add state for date/time selection and availability
   - Add state: `followUpDate` (string), `followUpTime` (string), `availableSlots` (string[]), `loadingSlots` (boolean)
   - Replace existing `followUpDate` state (currently just stores date string)
   - Add `followUpTime` state for selected time slot

4. **Add Calendar Blocking Logic**: Implement date blocking based on doctor availability
   - Create `isBlocked` function that checks if ALL doctors are off on a given date
   - Use `daysOffMap` to determine blocked dates
   - Pass `isBlocked` to Calendar component

5. **Add Time Slot Generation**: Generate available slots based on doctor shifts
   - Create `shiftSlotsForDate` function that generates 30-minute slots
   - Filter doctors who are NOT on day-off for the selected date
   - Combine all available doctor shift slots
   - Sort and deduplicate slots

6. **Add Time Slot Filtering**: Filter past slots when today is selected
   - Create `displaySlots` function that filters slots based on current time
   - Compare slot time with current time in minutes
   - Keep only slots at least 30 minutes in the future

7. **Add Availability Checking**: Load available slots when date changes
   - Add useEffect that triggers on `followUpDate` change
   - Call `getAvailableSlotsForDate(followUpDate)` service
   - Update `availableSlots` state
   - Handle loading and error states

8. **Replace UI Components**: Replace simple date input with calendar and time slot grid
   - Remove `<input type="date">` from lines 800-810
   - Add Calendar component with `selected`, `onSelect`, and `isBlocked` props
   - Add time slot grid similar to `BookAppointmentForm.tsx` (3-column grid)
   - Show slot availability status (available, selected, taken)
   - Add loading spinner while slots are being fetched

9. **Update Form Validation**: Add validation for time slot selection
   - Update `handleSubmit` to check both `followUpDate` and `followUpTime`
   - Show error if follow-up is checked but time is not selected
   - Validate that selected slot is still available before submission

10. **Update Appointment Creation**: Pass time to backend when creating follow-up appointment
    - Modify the consultation registration payload to include `followUpTime`
    - Ensure backend `registerConsultation` or separate follow-up creation endpoint accepts time parameter
    - Note: May require backend changes if current API doesn't accept time

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write tests that simulate doctor interactions with the follow-up scheduling section. Run these tests on the UNFIXED code to observe failures and understand the root cause.

**Test Cases**:
1. **Missing Calendar Test**: Check "Agendar cita de seguimiento" checkbox → Verify that a simple date input is rendered instead of a Calendar component (will pass on unfixed code, demonstrating the bug)
2. **No Time Selection Test**: Check follow-up checkbox → Verify that no time slot selection UI is present (will pass on unfixed code)
3. **Past Date Selection Test**: Select a past date in the simple date input → Verify that the form allows submission (will pass on unfixed code, demonstrating lack of validation)
4. **Day Off Selection Test**: Select a date when the doctor is on vacation → Verify that the form allows submission (will pass on unfixed code)
5. **No Availability Check Test**: Select any date → Verify that no API call to `getAvailableSlotsForDate` is made (will pass on unfixed code)

**Expected Counterexamples**:
- Simple HTML date input is rendered instead of Calendar component
- No time slot selection UI is present
- Past dates and days off are not blocked
- No availability validation occurs
- Possible causes: missing Calendar component, missing doctor data loading, missing time slot logic, missing availability service calls

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL followUpAttempt WHERE isBugCondition(followUpAttempt) DO
  result := renderFollowUpSection_fixed(followUpAttempt)
  ASSERT hasCalendarComponent(result)
  ASSERT hasTimeSlotSelection(result)
  ASSERT pastDatesAreBlocked(result)
  ASSERT daysOffAreBlocked(result)
  ASSERT availabilityIsValidated(result)
  ASSERT pastTimeSlotsAreFiltered(result)
  ASSERT patientIsAutoAssigned(result)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL consultationFormInteraction WHERE NOT isBugCondition(consultationFormInteraction) DO
  ASSERT originalForm(consultationFormInteraction) = fixedForm(consultationFormInteraction)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-follow-up interactions

**Test Plan**: Observe behavior on UNFIXED code first for non-follow-up interactions, then write property-based tests capturing that behavior.

**Test Cases**:
1. **Patient Display Preservation**: Verify that patient information display continues to work correctly after fix
2. **Vital Signs Preservation**: Verify that vital signs display continues to work correctly after fix
3. **Diagnosis Entry Preservation**: Verify that entering diagnosis and clinical notes continues to work correctly after fix
4. **Destination Selection Preservation**: Verify that selecting LAB, PHARMACY, EXTERNAL_RX, DISCHARGE continues to work correctly after fix
5. **Lab Order Preservation**: Verify that selecting lab tests continues to work correctly after fix
6. **Prescription Preservation**: Verify that adding medications continues to work correctly after fix
7. **Form Submission Preservation**: Verify that submitting the consultation form (without follow-up) continues to work correctly after fix
8. **Navigation Preservation**: Verify that navigating away from the form continues to work correctly after fix

### Unit Tests

- Test Calendar component renders correctly with selected date
- Test date blocking logic for past dates
- Test date blocking logic for doctor days off
- Test time slot generation from doctor shifts
- Test time slot filtering for today's date
- Test availability checking when date changes
- Test form validation with follow-up checkbox checked
- Test form validation with follow-up checkbox unchecked
- Test appointment creation with date and time

### Property-Based Tests

- Generate random dates and verify that past dates are always blocked
- Generate random doctor shift configurations and verify that slots are correctly generated
- Generate random current times and verify that past slots are always filtered when today is selected
- Generate random days off configurations and verify that blocked dates are correctly identified
- Test that all non-follow-up form interactions produce identical results before and after fix

### Integration Tests

- Test full consultation flow with follow-up appointment scheduling
- Test that selecting a date loads available slots from backend
- Test that selecting a time slot updates the form state correctly
- Test that submitting the form creates both consultation and follow-up appointment
- Test that canceling the form does not create any appointments
- Test that unchecking the follow-up checkbox clears the selection
- Test that switching between dates updates available slots correctly
- Test visual feedback (loading spinners, disabled states, error messages)
