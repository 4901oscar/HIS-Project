# Implementation Plan: Triage Landing Page Improvement

## Overview

This implementation plan addresses the improvement of the post-login experience for the Triage role (VITAL_SIGNS) by modifying the role-to-route mapping and removing the standalone vital signs capture route. The changes enforce the architectural principle "Todo debe estar amarrado a la cita" by ensuring all vital signs are captured within an appointment context.

**Key Changes:**
1. Modify `LoginPage.tsx` to redirect VITAL_SIGNS role to `/vitals/triage`
2. Remove `/vitals` route from `App.tsx`
3. Delete `VitalSignsCapture.tsx` component file
4. Verify navigation flows remain intact

**Technology Stack:** TypeScript, React, React Router

## Tasks

- [x] 1. Modify LoginPage role routing configuration
  - Update `roleRoutes` mapping object in `LoginPage.tsx`
  - Change `'VITAL_SIGNS': '/vitals'` to `'VITAL_SIGNS': '/vitals/triage'`
  - Verify other role mappings remain unchanged
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [ ]* 1.1 Write unit tests for LoginPage role routing
  - Test VITAL_SIGNS role maps to `/vitals/triage`
  - Test redirect logic with `from` location override
  - Test fallback behavior when role not found
  - Test all other role mappings remain unchanged
  - _Requirements: 1.1, 1.4, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8_

- [x] 2. Remove /vitals route from App.tsx
  - [x] 2.1 Remove VitalSignsCapture import statement
    - Delete `import VitalSignsCapture from './pages/vitals/VitalSignsCapture';`
    - _Requirements: 3.1, 3.5_
  
  - [x] 2.2 Remove /vitals route definition
    - Delete the `<Route path="/vitals" ... />` block
    - Verify no other routes reference VitalSignsCapture
    - _Requirements: 3.1, 3.3_

- [x] 3. Delete VitalSignsCapture component file
  - Delete `frontend-medflow/src/pages/vitals/VitalSignsCapture.tsx`
  - Verify no remaining imports or references to this component
  - _Requirements: 3.2, 3.4_

- [x] 4. Checkpoint - Verify build and no broken imports
  - Run build process to ensure no compilation errors
  - Verify no console errors related to missing VitalSignsCapture
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 5. Write integration tests for triage login flow
  - Test complete login flow for VITAL_SIGNS user redirects to `/vitals/triage`
  - Test navigation from TriagePendingPage to TriageVitalSignsCapture
  - Test navigation back to TriagePendingPage after saving vital signs
  - Test navigation back to TriagePendingPage on cancel
  - _Requirements: 1.1, 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ]* 6. Write regression tests for other roles
  - Test ADMIN role redirects to `/administrator`
  - Test ADMISSION role redirects to `/admission`
  - Test DOCTOR role redirects to `/doctor`
  - Test LABORATORY role redirects to `/lab`
  - Test PHARMACY role redirects to `/pharmacy`
  - Test CASHIER role redirects to `/cashier`
  - Test PATIENT role redirects to `/`
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8_

- [ ] 7. Final checkpoint - Manual testing verification
  - Login with VITAL_SIGNS role and verify landing on `/vitals/triage`
  - Verify pending appointments list displays correctly
  - Select appointment and verify navigation to vital signs capture
  - Save vital signs and verify redirect back to triage dashboard
  - Cancel vital signs capture and verify redirect back to triage dashboard
  - Verify `/vitals` route returns 404 or appropriate error
  - Login with other roles and verify no regression in redirect behavior
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- This is a frontend-only change with no backend modifications required
- The change enforces the architectural principle: "Todo debe estar amarrado a la cita"
- All vital signs capture must now flow through appointment context
- Zero downtime deployment - changes are configuration-only
