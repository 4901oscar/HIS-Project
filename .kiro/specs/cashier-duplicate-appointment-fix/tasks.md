# Implementation Plan

- [x] 1. Write bug condition exploration test
  - **Property 1: Bug Condition** - Payment Queue Excludes Additional Service Payments
  - **CRITICAL**: This test MUST FAIL on unfixed code - failure confirms the bug exists
  - **DO NOT attempt to fix the test or the code when it fails**
  - **NOTE**: This test encodes the expected behavior - it will validate the fix when it passes after implementation
  - **GOAL**: Surface counterexamples that demonstrate the bug exists
  - **Scoped PBT Approach**: For deterministic bugs, scope the property to the concrete failing case(s) to ensure reproducibility
  - Test implementation details from Bug Condition in design:
    - Test that `matchesQueueFilter(appointment, "payment")` returns `false` when `appointment.status = PENDING_LAB_PAYMENT`
    - Test that `matchesQueueFilter(appointment, "payment")` returns `false` when `appointment.status = PENDING_PHARMACY_PAYMENT`
    - Test case-insensitive filter handling: "payment", "PAYMENT", "Payment"
  - The test assertions should match the Expected Behavior Properties from design:
    - For all appointments where status is PENDING_LAB_PAYMENT or PENDING_PHARMACY_PAYMENT and queue filter is "payment", the function SHALL return false
  - Run test on UNFIXED code
  - **EXPECTED OUTCOME**: Test FAILS (this is correct - it proves the bug exists)
  - Document counterexamples found to understand root cause:
    - Example: `matchesQueueFilter(appointment with PENDING_LAB_PAYMENT, "payment")` returns `true` instead of `false`
    - Example: `matchesQueueFilter(appointment with PENDING_PHARMACY_PAYMENT, "payment")` returns `true` instead of `false`
  - Mark task complete when test is written, run, and failure is documented
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 2. Write preservation property tests (BEFORE implementing fix)
  - **Property 2: Preservation** - Non-Buggy Filter Behavior Unchanged
  - **IMPORTANT**: Follow observation-first methodology
  - Observe behavior on UNFIXED code for non-buggy inputs:
    - Observe: `matchesQueueFilter(appointment with PENDING_PAYMENT, "payment")` returns `true`
    - Observe: `matchesQueueFilter(appointment with SCHEDULED, "payment")` returns `true`
    - Observe: `matchesQueueFilter(appointment with LABORATORY, "lab")` returns `true`
    - Observe: `matchesQueueFilter(appointment with PHARMACY, "pharmacy")` returns `true`
    - Observe: `matchesQueueFilter(appointment with VITAL_SIGNS, "triage")` returns `true`
    - Observe: `matchesQueueFilter(appointment with PENDING_PAYMENT, "admission")` returns `true`
    - Observe: `matchesQueueFilter(appointment with SCHEDULED, "admission")` returns `true`
    - Observe: `matchesQueueFilter(appointment with PENDING_LAB_PAYMENT, "lab")` returns `false`
    - Observe: `matchesQueueFilter(appointment with PENDING_PHARMACY_PAYMENT, "pharmacy")` returns `false`
    - Observe: `matchesQueueFilter(appointment with any status, null)` returns `true`
    - Observe: `matchesQueueFilter(appointment with any status, "")` returns `true`
    - Observe: `matchesQueueFilter(appointment with any status, "unknown")` returns `true`
  - Write property-based tests capturing observed behavior patterns from Preservation Requirements:
    - For all appointments where status is PENDING_PAYMENT and queue filter is "payment", the function SHALL return `true`
    - For all appointments where status is SCHEDULED and queue filter is "payment", the function SHALL return `true`
    - For all appointments where status is LABORATORY and queue filter is "lab" or "laboratory", the function SHALL return `true`
    - For all appointments where status is PHARMACY and queue filter is "pharmacy", the function SHALL return `true`
    - For all appointments where status is VITAL_SIGNS and queue filter is "triage", the function SHALL return `true`
    - For all appointments where status is PENDING_PAYMENT or SCHEDULED and queue filter is "admission", the function SHALL return `true`
    - For all appointments where status is PENDING_LAB_PAYMENT and queue filter is "lab", the function SHALL return `false`
    - For all appointments where status is PENDING_PHARMACY_PAYMENT and queue filter is "pharmacy", the function SHALL return `false`
    - For all appointments with any status and queue filter is null or empty, the function SHALL return `true`
    - For all appointments with any status and queue filter is unknown, the function SHALL return `true` (default behavior)
  - Property-based testing generates many test cases for stronger guarantees
  - Run tests on UNFIXED code
  - **EXPECTED OUTCOME**: Tests PASS (this confirms baseline behavior to preserve)
  - Mark task complete when tests are written, run, and passing on unfixed code
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

- [x] 3. Fix for duplicate appointment display in payment queue

  - [x] 3.1 Implement the fix in matchesQueueFilter method
    - Open file: `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/controller/AppointmentController.java`
    - Locate method `matchesQueueFilter` (line 231)
    - Remove the conditions that include `PENDING_LAB_PAYMENT` and `PENDING_PHARMACY_PAYMENT` from the "payment" case
    - Change from:
      ```java
      case "payment":
          return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
                 appointment.getStatus() == AppointmentStatus.PENDING_LAB_PAYMENT ||
                 appointment.getStatus() == AppointmentStatus.PENDING_PHARMACY_PAYMENT ||
                 appointment.getStatus() == AppointmentStatus.SCHEDULED;
      ```
    - Change to:
      ```java
      case "payment":
          return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
                 appointment.getStatus() == AppointmentStatus.SCHEDULED;
      ```
    - Add explanatory comment before the "payment" case:
      ```java
      // "payment" queue: Only consultation payments (PENDING_PAYMENT, SCHEDULED)
      // Excludes PENDING_LAB_PAYMENT and PENDING_PHARMACY_PAYMENT to prevent
      // duplicate display (those belong to their respective service queues)
      ```
    - Verify that "lab" and "pharmacy" cases remain unchanged (they correctly only include LABORATORY and PHARMACY states)
    - Verify that "admission" case remains unchanged (it correctly only includes PENDING_PAYMENT and SCHEDULED states)
    - _Bug_Condition: isBugCondition(input) where input.queueFilter = "payment" AND (input.appointment.status = PENDING_LAB_PAYMENT OR input.appointment.status = PENDING_PHARMACY_PAYMENT)_
    - _Expected_Behavior: For all appointments where status is PENDING_LAB_PAYMENT or PENDING_PHARMACY_PAYMENT and queue filter is "payment", matchesQueueFilter SHALL return false_
    - _Preservation: For all appointments and queue filter combinations where the bug condition does NOT hold, matchesQueueFilter SHALL produce exactly the same result as the original function_
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

  - [x] 3.2 Verify bug condition exploration test now passes
    - **Property 1: Expected Behavior** - Payment Queue Excludes Additional Service Payments
    - **IMPORTANT**: Re-run the SAME test from task 1 - do NOT write a new test
    - The test from task 1 encodes the expected behavior
    - When this test passes, it confirms the expected behavior is satisfied
    - Run bug condition exploration test from step 1
    - **EXPECTED OUTCOME**: Test PASSES (confirms bug is fixed)
    - Verify that:
      - `matchesQueueFilter(appointment with PENDING_LAB_PAYMENT, "payment")` now returns `false`
      - `matchesQueueFilter(appointment with PENDING_PHARMACY_PAYMENT, "payment")` now returns `false`
      - Case-insensitive handling works correctly for all variations
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 3.3 Verify preservation tests still pass
    - **Property 2: Preservation** - Non-Buggy Filter Behavior Unchanged
    - **IMPORTANT**: Re-run the SAME tests from task 2 - do NOT write new tests
    - Run preservation property tests from step 2
    - **EXPECTED OUTCOME**: Tests PASS (confirms no regressions)
    - Confirm all tests still pass after fix:
      - PENDING_PAYMENT still included in payment queue
      - SCHEDULED still included in payment queue
      - Lab queue filter unchanged (LABORATORY only)
      - Pharmacy queue filter unchanged (PHARMACY only)
      - Triage queue filter unchanged (VITAL_SIGNS only)
      - Admission queue filter unchanged (PENDING_PAYMENT and SCHEDULED)
      - PENDING_LAB_PAYMENT still NOT in lab queue
      - PENDING_PHARMACY_PAYMENT still NOT in pharmacy queue
      - Null/empty filter behavior unchanged
      - Unknown filter default behavior unchanged
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8_

- [x] 4. Checkpoint - Ensure all tests pass
  - Run all unit tests for AppointmentController
  - Run all integration tests for appointment endpoints
  - Verify no test failures or regressions
  - If any issues arise, review the fix and consult with the user before proceeding
  - Confirm that:
    - Bug condition exploration test passes (bug is fixed)
    - Preservation tests pass (no regressions)
    - All existing tests continue to pass
  - Ask the user if questions arise or if manual testing is needed
