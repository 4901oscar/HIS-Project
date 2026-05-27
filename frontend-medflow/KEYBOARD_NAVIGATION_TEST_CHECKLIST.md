# Keyboard Navigation Manual Test Checklist

Use this checklist to manually verify keyboard navigation in the Triage Pending Page.

## Prerequisites
- Start the development server: `npm run dev`
- Navigate to: `http://localhost:5173/vitals/triage`
- Ensure you're logged in with VITAL_SIGNS or DOCTOR role

---

## Test 1: Tab Navigation Through Page Elements

**Steps**:
1. Load the page
2. Press **Tab** key repeatedly
3. Observe focus indicator moving through elements

**Expected Results**:
- [ ] Focus moves to "Actualizar" (Refresh) button
- [ ] Focus moves to first appointment row in table
- [ ] Focus moves to subsequent appointment rows
- [ ] Focus indicator is clearly visible on each element
- [ ] Tab order is logical (top to bottom, left to right)

---

## Test 2: Enter Key on Appointment Row

**Steps**:
1. Press **Tab** until an appointment row is focused
2. Press **Enter** key

**Expected Results**:
- [ ] Appointment is selected (row is highlighted)
- [ ] Patient details card appears on the right
- [ ] Focus moves to patient details card
- [ ] Patient name, DPI, and appointment details are displayed

---

## Test 3: Space Key on Appointment Row

**Steps**:
1. Press **Tab** until a different appointment row is focused
2. Press **Space** key

**Expected Results**:
- [ ] Appointment is selected (row is highlighted)
- [ ] Patient details card appears on the right
- [ ] Page does NOT scroll (default Space behavior is prevented)
- [ ] Focus moves to patient details card

---

## Test 4: Tab Through Patient Details Buttons

**Steps**:
1. Select an appointment (using Enter or Space)
2. Press **Tab** key repeatedly

**Expected Results**:
- [ ] Focus moves to "Registrar Signos Vitales" button
- [ ] Focus moves to "Realizar Triaje" button (may be disabled)
- [ ] Focus moves to "Cancelar" button
- [ ] All buttons show clear focus indicator

---

## Test 5: Escape Key Closes Vital Signs Form

**Steps**:
1. Select an appointment
2. Press **Tab** to "Registrar Signos Vitales" button
3. Press **Enter** to open form
4. Verify form is displayed
5. Press **Escape** key

**Expected Results**:
- [ ] Vital signs form opens when Enter is pressed
- [ ] Form displays all input fields
- [ ] Escape key closes the form
- [ ] Form disappears from view
- [ ] No error messages appear

---

## Test 6: Tab Through Vital Signs Form Inputs

**Steps**:
1. Open vital signs form
2. Press **Tab** key repeatedly through all inputs

**Expected Results**:
- [ ] Focus moves to "Sist. (mmHg)" input
- [ ] Focus moves to "Diast. (mmHg)" input
- [ ] Focus moves to "FC (lpm)" input
- [ ] Focus moves to "FR (rpm)" input
- [ ] Focus moves to "Temperatura (°C)" input
- [ ] Focus moves to "SpO2 (%)" input
- [ ] Focus moves to "Peso (kg)" input (optional)
- [ ] Focus moves to "Talla (cm)" input (optional)
- [ ] Focus moves to "Cancelar" button
- [ ] Focus moves to "Guardar Signos Vitales" button

---

## Test 7: Submit Vital Signs Form with Enter

**Steps**:
1. Open vital signs form
2. Fill all required fields using Tab and typing
3. Press **Tab** to "Guardar Signos Vitales" button
4. Press **Enter** key

**Expected Results**:
- [ ] Form submits successfully
- [ ] Success message appears: "Signos vitales registrados exitosamente"
- [ ] Form closes automatically
- [ ] "Realizar Triaje" button becomes enabled

---

## Test 8: Escape Key Closes Triage Form

**Steps**:
1. Complete vital signs workflow
2. Press **Tab** to "Realizar Triaje" button
3. Press **Enter** to open triage form
4. Wait for Manchester catalog to load
5. Press **Escape** key

**Expected Results**:
- [ ] Triage form opens when Enter is pressed
- [ ] Manchester catalog loads (motifs and discriminators)
- [ ] Escape key closes the form
- [ ] Form disappears from view
- [ ] No error messages appear

---

## Test 9: Tab Through Triage Form Elements

**Steps**:
1. Open triage form
2. Press **Tab** key repeatedly

**Expected Results**:
- [ ] Focus moves to "Motivo de Consulta" dropdown
- [ ] Focus moves to first discriminator checkbox
- [ ] Focus moves through all discriminator checkboxes
- [ ] Focus moves to "Cancelar" button
- [ ] Focus moves to "Registrar Triaje" button

---

## Test 10: Arrow Keys in Motif Dropdown

**Steps**:
1. Open triage form
2. Press **Tab** to "Motivo de Consulta" dropdown
3. Press **Enter** or **Space** to open dropdown
4. Press **Arrow Down** and **Arrow Up** keys

**Expected Results**:
- [ ] Dropdown opens
- [ ] Arrow Down moves to next option
- [ ] Arrow Up moves to previous option
- [ ] Enter key selects the focused option
- [ ] Dropdown closes after selection

---

## Test 11: Space Key Toggles Checkboxes

**Steps**:
1. Open triage form
2. Press **Tab** to first discriminator checkbox
3. Press **Space** key to toggle
4. Press **Tab** to next checkbox
5. Press **Space** key to toggle

**Expected Results**:
- [ ] Space key checks the checkbox
- [ ] Space key unchecks the checkbox when pressed again
- [ ] Visual indicator shows checkbox state
- [ ] Counter updates: "X discriminador(es) seleccionado(s)"

---

## Test 12: Complete Workflow Keyboard-Only

**Steps**:
1. Start at page load
2. Use ONLY keyboard (no mouse) to:
   - Select an appointment
   - Record vital signs
   - Perform triage
   - Submit triage

**Expected Results**:
- [ ] Can select appointment using Tab + Enter
- [ ] Can open vital signs form using Tab + Enter
- [ ] Can fill all vital signs inputs using Tab + typing
- [ ] Can submit vital signs using Tab + Enter
- [ ] Can open triage form using Tab + Enter
- [ ] Can select motif using Tab + Arrow keys + Enter
- [ ] Can select discriminators using Tab + Space
- [ ] Can submit triage using Tab + Enter
- [ ] Success message appears: "Triaje registrado exitosamente — Prioridad: [level]"
- [ ] Appointment is removed from pending list

---

## Test 13: Focus Indicator Visibility

**Steps**:
1. Press **Tab** through all interactive elements
2. Observe focus indicators

**Expected Results**:
- [ ] Focus indicator is visible on all elements
- [ ] Focus indicator has sufficient contrast (visible against background)
- [ ] Focus indicator is not hidden by other elements
- [ ] Focus indicator follows WCAG 2.1 AA guidelines

---

## Test 14: Disabled Button Behavior

**Steps**:
1. Select an appointment (without recording vital signs)
2. Press **Tab** to "Realizar Triaje" button
3. Press **Enter** key

**Expected Results**:
- [ ] Button is visually disabled (opacity-50)
- [ ] Button has `aria-disabled="true"` attribute
- [ ] Enter key does NOT trigger action
- [ ] Tooltip shows: "Primero registre los signos vitales"

---

## Test 15: Cancel Button Keyboard Access

**Steps**:
1. Select an appointment
2. Press **Tab** to "Cancelar" button
3. Press **Enter** key

**Expected Results**:
- [ ] Selection is cleared
- [ ] Patient details card disappears
- [ ] Focus returns to appointment list
- [ ] No error messages appear

---

## Summary

**Total Tests**: 15  
**Passed**: ___  
**Failed**: ___  

**Notes**:
_Add any observations or issues found during testing_

---

## Accessibility Verification

Additional checks for WCAG 2.1 AA compliance:

- [ ] All interactive elements are reachable via keyboard
- [ ] Focus order is logical and intuitive
- [ ] Focus indicator is visible on all elements
- [ ] No keyboard traps (can Tab out of all elements)
- [ ] Escape key works to close modals/forms
- [ ] Enter/Space keys work on all buttons
- [ ] Form inputs have associated labels
- [ ] Error messages are announced to screen readers
- [ ] Loading states are announced to screen readers

---

## Browser Testing

Test in multiple browsers to ensure compatibility:

- [ ] Chrome/Edge (Chromium)
- [ ] Firefox
- [ ] Safari (if available)

---

## Screen Reader Testing (Optional)

If you have access to screen readers, test with:

- [ ] NVDA (Windows)
- [ ] JAWS (Windows)
- [ ] VoiceOver (macOS)

**Expected**: All elements are announced correctly, and navigation is smooth.
