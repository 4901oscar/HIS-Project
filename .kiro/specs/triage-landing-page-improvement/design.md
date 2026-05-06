# Design Document: Triage Landing Page Improvement

## Overview

This design document specifies the technical implementation for improving the post-login user experience for the Triage role (VITAL_SIGNS) in the MedFlow system. The change involves modifying the role-to-route mapping in the login flow to redirect triage users directly to the pending appointments dashboard instead of the DPI search page.

**Scope:** Frontend-only changes affecting:
1. LoginPage component's role routing logic
2. App.tsx routing configuration (removal of `/vitals` route)
3. VitalSignsCapture.tsx component (complete deletion)

**Impact:** Medium-risk change involving multiple file modifications and route removal.

**Rationale:** The current redirect to `/vitals` (DPI search page) violates the fundamental architectural principle: **"Todo debe estar amarrado a la cita"**. The `/vitals` route allows registering vital signs without an associated appointment, breaking system traceability. This change enforces the appointment-first workflow by:
1. Redirecting triage users directly to `/vitals/triage` (pending appointments dashboard)
2. Completely removing the `/vitals` route and VitalSignsCapture component
3. Ensuring all vital signs capture flows require an associated appointment context

## Architecture

### System Context

```mermaid
graph LR
    A[User Login] --> B[LoginPage Component]
    B --> C{Role Check}
    C -->|VITAL_SIGNS| D[/vitals/triage]
    C -->|Other Roles| E[Existing Routes]
    D --> F[TriagePendingPage]
    F --> G[TriageVitalSignsCapture]
    G --> F
```

### Component Interaction

The change affects the post-authentication redirect logic in `LoginPage.tsx`:

1. User submits login credentials
2. Authentication service validates credentials
3. LoginPage receives user data with roles
4. **Modified behavior:** `roleRoutes` map returns `/vitals/triage` for VITAL_SIGNS role
5. React Router navigates to the mapped route
6. TriagePendingPage renders with pending appointments list

### Affected Components

- **LoginPage.tsx** (Modified): Contains the `roleRoutes` mapping object
- **App.tsx** (Modified): Remove `/vitals` route definition
- **VitalSignsCapture.tsx** (Deleted): Component file to be completely removed
- **TriagePendingPage.tsx** (No changes): Target landing page for triage users
- **TriageVitalSignsCapture.tsx** (No changes): Maintains existing navigation back to `/vitals/triage`

## Components and Interfaces

### LoginPage Component

**File:** `frontend-medflow/src/pages/LoginPage.tsx`

**Current Implementation:**
```typescript
const roleRoutes: Record<string, string> = {
  'ADMIN': '/administrator',
  'ADMISSION': '/admission',
  'VITAL_SIGNS': '/vitals',  // Current: DPI search page
  'DOCTOR': '/doctor',
  'LABORATORY': '/lab',
  'PHARMACY': '/pharmacy',
  'CASHIER': '/cashier',
  'PATIENT': '/',
};
```

**Modified Implementation:**
```typescript
const roleRoutes: Record<string, string> = {
  'ADMIN': '/administrator',
  'ADMISSION': '/admission',
  'VITAL_SIGNS': '/vitals/triage',  // New: Triage dashboard
  'DOCTOR': '/doctor',
  'LABORATORY': '/lab',
  'PHARMACY': '/pharmacy',
  'CASHIER': '/cashier',
  'PATIENT': '/',
};
```

**Redirect Logic (Unchanged):**
```typescript
const from = (location.state as { from?: string })?.from;
const primaryRole = response.user.roles[0] ?? '';
const redirectPath = from ?? roleRoutes[primaryRole] ?? '/dashboard';
navigate(redirectPath);
```

**Behavior:**
- If `from` location exists (user was redirected to login), navigate to `from`
- Otherwise, use `roleRoutes[primaryRole]` mapping
- Fallback to `/dashboard` if role not found

### TriagePendingPage Component

**File:** `frontend-medflow/src/pages/vitals/TriagePendingPage.tsx`

**Route:** `/vitals/triage`

**Responsibilities:**
- Display list of active appointments pending triage
- Auto-refresh every 30 seconds
- Provide manual refresh with debounce protection
- Navigate to TriageVitalSignsCapture when appointment is selected

**No changes required** - component already implements the desired landing page functionality.

### TriageVitalSignsCapture Component

**File:** `frontend-medflow/src/pages/vitals/TriageVitalSignsCapture.tsx`

**Route:** `/vitals/triage/capture` (accessed via navigation state from TriagePendingPage)

**Navigation Behavior (Already Implemented):**
- After saving vital signs: `navigate('/vitals/triage')` (line 103)
- On cancel: `navigate('/vitals/triage')` (line 109)

**No changes required** - component already navigates back to the triage dashboard.

### App.tsx Routing Configuration

**File:** `frontend-medflow/src/App.tsx`

**Current Implementation:**
```typescript
<Route
  path="/vitals"
  element={
    <ProtectedRoute requiredRole="VITAL_SIGNS">
      <VitalSignsCapture />
    </ProtectedRoute>
  }
/>
```

**Modified Implementation:**
```typescript
// Route removed - vital signs capture must be done through appointment context
```

**Rationale for Removal:**
- Enforces architectural principle: "Todo debe estar amarrado a la cita"
- Prevents registration of vital signs without appointment context
- Eliminates workflow that bypasses appointment-based traceability
- All vital signs capture must now flow through `/vitals/triage` → `/vitals/triage/capture`

### VitalSignsCapture Component

**File:** `frontend-medflow/src/pages/vitals/VitalSignsCapture.tsx`

**Action:** Complete deletion

**Rationale:**
- Component allows DPI-based search and vital signs registration without appointment
- Violates system's appointment-first architecture
- Functionality is replaced by TriageVitalSignsCapture which requires appointment context
- Removing this component enforces proper workflow through appointment dashboard

## Data Models

No data model changes required. This is a configuration change affecting routing behavior only.

### Navigation State Interface

**Existing interface used by TriageVitalSignsCapture:**
```typescript
interface LocationState {
  appointmentId: string;
  patientId: string;
}
```

This interface remains unchanged and continues to be passed via React Router's navigation state.

## Error Handling

### Error Scenarios

1. **Invalid Role in roleRoutes Map**
   - **Current behavior:** Falls back to `/dashboard`
   - **No change required:** Existing fallback mechanism handles this case

2. **User Attempts to Access Removed /vitals Route**
   - **Behavior:** Route no longer exists, React Router will show 404 or redirect
   - **Mitigation:** Users are redirected to `/vitals/triage` on login, preventing direct access attempts
   - **Impact:** Minimal - route was only accessible via login redirect or manual navigation

3. **User with VITAL_SIGNS Role Lacks Route Access**
   - **Mitigation:** Route protection middleware already validates role permissions
   - **No change required:** Existing route guards remain in place

4. **Navigation State Missing in TriageVitalSignsCapture**
   - **Current behavior:** Component displays error message and provides "Back to Triage" button
   - **No change required:** Error handling already implemented (lines 52-54, 147-159)

5. **Authentication State Loss During Redirect**
   - **Mitigation:** React Router preserves authentication context during navigation
   - **No change required:** Existing auth context management handles this

### Validation

No additional validation required. The change is a static configuration update with no runtime validation needs.

## Testing Strategy

### Test Approach

This feature involves a **static configuration change** (modifying a route mapping object), not a function with complex logic or varying inputs. Property-based testing is **not applicable** for this type of change.

**Rationale for excluding PBT:**
- The change is a single constant value in a configuration object
- There are no inputs to vary or edge cases to explore through randomization
- The behavior is deterministic: role X always maps to route Y
- Testing involves verifying specific examples (role → route mappings)

### Testing Strategy

**Unit Tests:**
- Verify `roleRoutes` object contains correct mapping for VITAL_SIGNS role
- Test redirect logic with VITAL_SIGNS role returns `/vitals/triage`
- Test redirect logic with `from` location overrides role route
- Test fallback behavior when role not found in map
- Verify other role mappings remain unchanged

**Integration Tests:**
- Test complete login flow for VITAL_SIGNS user redirects to `/vitals/triage`
- Test navigation from TriagePendingPage to TriageVitalSignsCapture
- Test navigation back to TriagePendingPage after saving vital signs
- Test navigation back to TriagePendingPage on cancel

**Manual Testing:**
- Login with VITAL_SIGNS role and verify landing on `/vitals/triage`
- Verify pending appointments list displays correctly
- Select appointment and verify navigation to vital signs capture
- Save vital signs and verify redirect back to triage dashboard
- Cancel vital signs capture and verify redirect back to triage dashboard
- Login with other roles and verify no regression in their redirect behavior

**Regression Testing:**
- Verify all other role redirects remain unchanged (ADMIN, ADMISSION, DOCTOR, etc.)
- Verify `/vitals` route no longer exists (returns 404 or redirects)
- Verify VitalSignsCapture component is completely removed from codebase
- Verify no broken imports or references to VitalSignsCapture
- Verify appointment-based vital signs capture through `/vitals/triage` works correctly

### Test Coverage Goals

- **Unit test coverage:** 100% of modified code (single line change)
- **Integration test coverage:** All navigation paths involving triage workflow
- **Regression test coverage:** All role-based redirects

### Testing Tools

- **Unit tests:** Jest + React Testing Library
- **Integration tests:** Jest + React Testing Library with React Router mocks
- **Manual tests:** Browser-based testing in development environment

## Implementation Notes

### Change Summary

**Multiple file modifications required:**

1. **LoginPage.tsx** - Single line modification:
```diff
const roleRoutes: Record<string, string> = {
  'ADMIN': '/administrator',
  'ADMISSION': '/admission',
- 'VITAL_SIGNS': '/vitals',
+ 'VITAL_SIGNS': '/vitals/triage',
  'DOCTOR': '/doctor',
  'LABORATORY': '/lab',
  'PHARMACY': '/pharmacy',
  'CASHIER': '/cashier',
  'PATIENT': '/',
};
```

2. **App.tsx** - Remove route definition:
```diff
- <Route
-   path="/vitals"
-   element={
-     <ProtectedRoute requiredRole="VITAL_SIGNS">
-       <VitalSignsCapture />
-     </ProtectedRoute>
-   }
- />
```

3. **VitalSignsCapture.tsx** - Delete entire file:
```bash
rm frontend-medflow/src/pages/vitals/VitalSignsCapture.tsx
```

4. **App.tsx** - Remove import statement:
```diff
- import VitalSignsCapture from './pages/vitals/VitalSignsCapture';
```

### Deployment Considerations

- **Zero downtime:** Changes are frontend-only, no backend coordination required
- **No database migrations:** No data model changes
- **No API changes:** No backend service modifications
- **Breaking change:** `/vitals` route will no longer be accessible
- **Migration path:** Users attempting to access `/vitals` will need to use `/vitals/triage` instead
- **Rollback strategy:** Revert changes to LoginPage.tsx, App.tsx, and restore VitalSignsCapture.tsx from version control

### Performance Impact

- **No performance impact:** Configuration changes with no runtime overhead
- **Reduced bundle size:** Removal of VitalSignsCapture component (~300 lines) reduces bundle
- **No additional network requests:** Same number of API calls as before
- **Improved workflow efficiency:** Users land directly on their primary work screen

### Security Considerations

- **No security impact:** Route protection middleware remains unchanged
- **Authorization:** Existing role-based access control continues to apply
- **Authentication:** No changes to authentication flow or token handling
- **Improved data integrity:** Enforces appointment context for all vital signs, improving traceability

## Design Decisions

### Decision 1: Modify roleRoutes vs. Add Conditional Logic

**Options Considered:**
1. Modify the `roleRoutes` mapping (chosen)
2. Add conditional logic after the mapping lookup

**Decision:** Modify the `roleRoutes` mapping directly

**Rationale:**
- Simpler and more maintainable
- Consistent with existing pattern for all other roles
- No additional complexity or branching logic
- Single source of truth for role-to-route mappings

### Decision 2: Route Path Selection

**Options Considered:**
1. `/vitals/triage` (chosen)
2. `/triage`
3. `/vitals/pending`

**Decision:** Use `/vitals/triage`

**Rationale:**
- Route already exists and is fully implemented
- Maintains consistency with existing `/vitals` namespace
- No additional routing configuration required
- Component already handles all required functionality

### Decision 3: Complete Removal of /vitals Route

**Options Considered:**
1. Remove `/vitals` route and component completely (chosen)
2. Keep `/vitals` accessible for backward compatibility
3. Redirect `/vitals` to `/vitals/triage`

**Decision:** Remove `/vitals` route and VitalSignsCapture component completely

**Rationale:**
- Enforces fundamental architectural principle: "Todo debe estar amarrado a la cita"
- Prevents workflow that bypasses appointment-based traceability
- Eliminates potential for data integrity issues (vital signs without appointment context)
- Simplifies codebase by removing unused component
- Forces all users to follow proper appointment-first workflow
- No legitimate use case exists for registering vital signs without appointment

## Validation Criteria

### Functional Validation

- [ ] VITAL_SIGNS user redirects to `/vitals/triage` after login
- [ ] Pending appointments list displays correctly on landing
- [ ] Navigation to vital signs capture works from dashboard
- [ ] Navigation back to dashboard works after saving vital signs
- [ ] Navigation back to dashboard works on cancel
- [ ] All other role redirects remain unchanged
- [ ] `/vitals` route returns 404 or appropriate error
- [ ] VitalSignsCapture.tsx file is completely removed
- [ ] No import errors or broken references to VitalSignsCapture
- [ ] Appointment-based vital signs capture works correctly through `/vitals/triage`

### Non-Functional Validation

- [ ] No performance degradation observed
- [ ] No console errors or warnings
- [ ] No accessibility regressions
- [ ] No visual layout issues
- [ ] Browser back button works correctly

### Regression Validation

- [ ] ADMIN role redirects to `/administrator`
- [ ] ADMISSION role redirects to `/admission`
- [ ] DOCTOR role redirects to `/doctor`
- [ ] LABORATORY role redirects to `/lab`
- [ ] PHARMACY role redirects to `/pharmacy`
- [ ] CASHIER role redirects to `/cashier`
- [ ] PATIENT role redirects to `/`
- [ ] Appointment-based vital signs capture through `/vitals/triage` functions correctly
- [ ] No console errors related to missing VitalSignsCapture component
- [ ] Build process completes without errors
