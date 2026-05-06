# Requirements Document: Integrated Manchester Triage

## Introduction

This feature integrates the Manchester Triage System classification directly into the vital signs capture screen using a **two-step progressive workflow**. Currently, the triage workflow captures vital signs but does not complete the triage classification, requiring staff to return to the pending list without finishing the process. This integration enables staff to complete both vital signs capture and Manchester classification in the same screen through a sequential process:

**Step 1:** Capture and save vital signs → appointment status changes to indicate vital signs recorded
**Step 2:** Complete Manchester classification → appointment moves to doctor queue

This approach allows staff to save progress at each step, with the ability to resume incomplete triage sessions. If vital signs are saved but Manchester classification is not completed, the appointment remains in the triage queue with a distinct status, and vital signs are locked to prevent modification.

## Glossary

- **Triage_System**: The integrated vital signs and Manchester classification capture interface with two-step workflow
- **Manchester_Catalog**: The complete set of motifs and discriminators used for triage classification
- **Motif**: A chief complaint or presenting problem category in the Manchester system (e.g., "Chest Pain", "Abdominal Pain")
- **Discriminator**: A specific clinical indicator associated with a motif that determines priority level
- **Priority_Level**: The calculated urgency classification (RED, ORANGE, YELLOW, GREEN, BLUE)
- **Vital_Signs_Form**: The existing form section for capturing physiological measurements
- **Classification_Section**: The new UI section for Manchester triage classification (hidden until vital signs are saved)
- **Staff_User**: The healthcare worker performing triage (nurse or triage technician with VITAL_SIGNS role)
- **Step_1_Complete**: State where vital signs have been saved but Manchester classification is pending
- **Step_2_Complete**: State where both vital signs and Manchester classification have been saved

## Requirements

### Requirement 1: Load Manchester Catalog on Component Mount

**User Story:** As a Staff_User, I want the Manchester catalog to load automatically when I open the capture screen, so that I can immediately begin the triage process without delays.

#### Acceptance Criteria

1. WHEN THE Triage_System mounts, THE Triage_System SHALL fetch the Manchester_Catalog from the backend
2. WHILE the Manchester_Catalog is loading, THE Triage_System SHALL display a loading indicator
3. IF the Manchester_Catalog fetch fails, THEN THE Triage_System SHALL display an error message with retry option
4. WHEN the Manchester_Catalog loads successfully, THE Triage_System SHALL store motifs and discriminators in component state
5. THE Triage_System SHALL fetch motifs and discriminators in parallel for optimal performance

### Requirement 2: Display Vital Signs Form

**User Story:** As a Staff_User, I want to see the vital signs form when I open the capture screen, so that I can capture physiological measurements.

#### Acceptance Criteria

1. THE Triage_System SHALL display the Vital_Signs_Form with all existing fields (systolic pressure, diastolic pressure, heart rate, respiratory rate, temperature, oxygen saturation, weight, height)
2. THE Triage_System SHALL maintain all existing validation rules for vital signs fields
3. THE Triage_System SHALL preserve the existing layout and styling of the Vital_Signs_Form
4. THE Triage_System SHALL display required field indicators for mandatory vital signs
5. WHEN the appointment already has vital signs recorded, THE Triage_System SHALL load the existing vital signs data into the form fields
6. WHEN the appointment already has vital signs recorded, THE Triage_System SHALL disable all vital signs input fields to prevent modification

### Requirement 3: Hide Manchester Classification Section Until Step 1 Complete

**User Story:** As a Staff_User, I want the Manchester classification section to be hidden until I save vital signs, so that I follow the correct workflow sequence.

#### Acceptance Criteria

1. WHEN the appointment does NOT have vital signs recorded, THE Triage_System SHALL hide the Classification_Section completely
2. WHEN the appointment has vital signs recorded (Step_1_Complete), THE Triage_System SHALL display the Classification_Section below the Vital_Signs_Form
3. THE Classification_Section SHALL include a motif selection dropdown
4. THE Classification_Section SHALL include a discriminator selection area
5. THE Classification_Section SHALL include a priority level preview display
6. THE Classification_Section SHALL be visually distinct from the Vital_Signs_Form using appropriate spacing and styling

### Requirement 4: Populate Motif Selection Dropdown

**User Story:** As a Staff_User, I want to select a chief complaint from a dropdown, so that I can identify the patient's primary presenting problem.

#### Acceptance Criteria

1. THE Triage_System SHALL populate the motif dropdown with all active motifs from the Manchester_Catalog
2. THE Triage_System SHALL display motif description as the dropdown option text
3. THE Triage_System SHALL sort motifs alphabetically by description
4. WHEN no motif is selected, THE Triage_System SHALL display placeholder text "Seleccione motivo de consulta"
5. THE Triage_System SHALL mark the motif selection as required

### Requirement 5: Filter Discriminators by Selected Motif

**User Story:** As a Staff_User, I want to see only relevant discriminators for the selected motif, so that I can quickly identify applicable clinical indicators without confusion.

#### Acceptance Criteria

1. WHEN a motif is selected, THE Triage_System SHALL filter discriminators to show only those associated with the selected motif
2. WHEN no motif is selected, THE Triage_System SHALL display a message "Seleccione un motivo para ver discriminadores"
3. WHEN a motif has no associated discriminators, THE Triage_System SHALL display a message "No hay discriminadores disponibles para este motivo"
4. WHEN the selected motif changes, THE Triage_System SHALL clear any previously selected discriminators
5. THE Triage_System SHALL display filtered discriminators within 100 milliseconds of motif selection

### Requirement 6: Enable Multiple Discriminator Selection

**User Story:** As a Staff_User, I want to select multiple discriminators, so that I can accurately capture all relevant clinical indicators present in the patient.

#### Acceptance Criteria

1. THE Triage_System SHALL display discriminators as selectable checkboxes
2. THE Triage_System SHALL allow selection of one or more discriminators
3. WHEN a discriminator is selected, THE Triage_System SHALL visually indicate the selection with a checkmark
4. WHEN a discriminator is deselected, THE Triage_System SHALL remove the visual selection indicator
5. THE Triage_System SHALL require at least one discriminator to be selected before submission

### Requirement 7: Calculate and Display Priority Level Preview

**User Story:** As a Staff_User, I want to see the calculated priority level as I select discriminators, so that I can verify the triage classification before saving.

#### Acceptance Criteria

1. WHEN at least one discriminator is selected, THE Triage_System SHALL calculate the highest priority level from selected discriminators
2. THE Triage_System SHALL display the calculated Priority_Level in a preview area
3. THE Triage_System SHALL display the Priority_Level using color-coded badges (RED: red background, ORANGE: orange background, YELLOW: yellow background, GREEN: green background, BLUE: blue background)
4. THE Triage_System SHALL display the Priority_Level description text (e.g., "Inmediato", "Muy urgente")
5. WHEN discriminator selection changes, THE Triage_System SHALL update the Priority_Level preview within 100 milliseconds

### Requirement 8: Separate Submission Buttons for Two-Step Workflow

**User Story:** As a Staff_User, I want separate buttons for saving vital signs and saving Manchester classification, so that I can save my progress at each step.

#### Acceptance Criteria

1. WHEN vital signs have NOT been saved, THE Triage_System SHALL display a "Guardar Signos Vitales" button
2. WHEN vital signs have been saved, THE Triage_System SHALL hide the "Guardar Signos Vitales" button
3. WHEN vital signs have been saved, THE Triage_System SHALL display a "Guardar Clasificación Manchester" button
4. WHEN the "Guardar Signos Vitales" button is clicked, THE Triage_System SHALL validate only vital signs fields
5. WHEN the "Guardar Clasificación Manchester" button is clicked, THE Triage_System SHALL validate only motif and discriminator selections
6. THE Triage_System SHALL display validation errors specific to the step being submitted

### Requirement 9: Save Vital Signs (Step 1)

**User Story:** As a Staff_User, I want to save vital signs independently, so that I can preserve my work even if I need to leave before completing Manchester classification.

#### Acceptance Criteria

1. WHEN the "Guardar Signos Vitales" button is clicked, THE Triage_System SHALL validate all required vital signs fields
2. IF validation passes, THE Triage_System SHALL call recordVitalSigns with vital signs data
3. WHEN recordVitalSigns succeeds, THE Triage_System SHALL display a success message "✓ Signos vitales guardados exitosamente"
4. WHEN recordVitalSigns succeeds, THE Triage_System SHALL disable all vital signs input fields
5. WHEN recordVitalSigns succeeds, THE Triage_System SHALL reveal the Classification_Section
6. WHEN recordVitalSigns succeeds, THE Triage_System SHALL hide the "Guardar Signos Vitales" button
7. IF recordVitalSigns fails, THE Triage_System SHALL display an error message and keep the form editable
8. WHILE recordVitalSigns is in progress, THE Triage_System SHALL disable the button and display "Guardando..." text

### Requirement 10: Save Manchester Classification (Step 2)

**User Story:** As a Staff_User, I want to save Manchester classification after vital signs are recorded, so that I complete the triage process and send the patient to the doctor queue.

#### Acceptance Criteria

1. WHEN the "Guardar Clasificación Manchester" button is clicked, THE Triage_System SHALL validate that a motif is selected
2. WHEN the "Guardar Clasificación Manchester" button is clicked, THE Triage_System SHALL validate that at least one discriminator is selected
3. IF validation passes, THE Triage_System SHALL call performTriage with appointmentId, patientId, motifId, and discriminatorIds
4. WHEN performTriage succeeds, THE Triage_System SHALL display a success message "✓ Clasificación Manchester registrada exitosamente"
5. WHEN performTriage succeeds, THE Triage_System SHALL navigate to the pending triage list after 2 seconds
6. IF performTriage fails, THE Triage_System SHALL display an error message and keep the form editable
7. WHILE performTriage is in progress, THE Triage_System SHALL disable the button and display "Guardando..." text

### Requirement 11: Handle Submission Errors Gracefully

**User Story:** As a Staff_User, I want clear error messages when submission fails, so that I can understand what went wrong and take corrective action.

#### Acceptance Criteria

1. IF recordVitalSigns fails, THEN THE Triage_System SHALL display the error message in a red alert box
2. IF performTriage fails, THEN THE Triage_System SHALL display the error message in a red alert box
3. WHEN an error occurs, THE Triage_System SHALL re-enable the submit button
4. WHEN an error occurs, THE Triage_System SHALL preserve all form data so the Staff_User can retry
5. THE Triage_System SHALL extract user-friendly error messages from API responses

### Requirement 12: Load Existing Vital Signs on Component Mount

**User Story:** As a Staff_User, I want the system to load existing vital signs if they were already saved, so that I can resume an incomplete triage session.

#### Acceptance Criteria

1. WHEN the component mounts, THE Triage_System SHALL check if the appointment has existing vital signs
2. IF existing vital signs are found, THE Triage_System SHALL load the vital signs data into the form fields
3. IF existing vital signs are found, THE Triage_System SHALL disable all vital signs input fields
4. IF existing vital signs are found, THE Triage_System SHALL display the Classification_Section
5. IF existing vital signs are found, THE Triage_System SHALL hide the "Guardar Signos Vitales" button
6. IF no existing vital signs are found, THE Triage_System SHALL display editable vital signs fields and hide the Classification_Section

**User Story:** As a Staff_User, I want to cancel the triage process and return to the pending list, so that I can defer the patient or handle urgent situations.

#### Acceptance Criteria

1. THE Triage_System SHALL display a "Cancelar" button
2. WHEN the "Cancelar" button is clicked, THE Triage_System SHALL navigate back to the pending triage list
3. WHEN the "Cancelar" button is clicked, THE Triage_System SHALL not save any vital signs or triage data
4. WHILE submission is in progress, THE Triage_System SHALL disable the "Cancelar" button

### Requirement 13: Maintain Cancel Functionality

**User Story:** As a Staff_User, I want to cancel the triage process and return to the pending list, so that I can defer the patient or handle urgent situations.

#### Acceptance Criteria

1. THE Triage_System SHALL display a "Cancelar" button at all times
2. WHEN the "Cancelar" button is clicked before saving vital signs, THE Triage_System SHALL navigate back to the pending triage list without saving
3. WHEN the "Cancelar" button is clicked after saving vital signs but before completing Manchester, THE Triage_System SHALL navigate back to the pending triage list (vital signs remain saved, appointment stays in queue)
4. WHILE submission is in progress, THE Triage_System SHALL disable the "Cancelar" button

### Requirement 14: Display Appointment Status in Pending List

**User Story:** As a Staff_User, I want to see which appointments have vital signs recorded but need Manchester classification, so that I can prioritize completing those triage sessions.

#### Acceptance Criteria

1. WHEN an appointment has vital signs recorded but no Manchester classification, THE pending triage list SHALL display a status label "Signos vitales registrados"
2. WHEN an appointment has no vital signs recorded, THE pending triage list SHALL display a status label "Pendiente de signos vitales"
3. THE status labels SHALL use distinct colors to differentiate between states
4. THE Triage_System SHALL continue to display appointments with vital signs recorded in the pending triage queue until Manchester classification is completed

**User Story:** As a Staff_User, I want to see patient information at the top of the screen, so that I can verify I am working with the correct patient.

#### Acceptance Criteria

1. THE Triage_System SHALL display patient full name, DPI, and email in an information card
2. THE Triage_System SHALL display the "Llamar Paciente" button with speech synthesis functionality
3. THE Triage_System SHALL maintain the existing styling and layout of the patient information card
4. THE Triage_System SHALL display the patient information above the vital signs form

### Requirement 15: Preserve Existing Patient Information Display

**User Story:** As a Staff_User, I want to see patient information at the top of the screen, so that I can verify I am working with the correct patient.

#### Acceptance Criteria

1. THE Triage_System SHALL display patient full name, DPI, and email in an information card
2. THE Triage_System SHALL display the "Llamar Paciente" button with speech synthesis functionality
3. THE Triage_System SHALL maintain the existing styling and layout of the patient information card
4. THE Triage_System SHALL display the patient information above the vital signs form

### Requirement 16: Display Loading State During Catalog Fetch

**User Story:** As a Staff_User, I want to see a loading indicator while the Manchester catalog loads, so that I know the system is working and not frozen.

#### Acceptance Criteria

1. WHILE the Manchester_Catalog is loading, THE Triage_System SHALL display a spinner animation
2. WHILE the Manchester_Catalog is loading, THE Triage_System SHALL display text "Cargando catálogo Manchester..."
3. WHILE the Manchester_Catalog is loading, THE Triage_System SHALL disable the motif and discriminator selection controls
4. WHEN the Manchester_Catalog loads successfully, THE Triage_System SHALL hide the loading indicator and enable selection controls

### Requirement 17: Display Success Confirmation

**User Story:** As a Staff_User, I want to see a success message after saving each step, so that I have confirmation that my data was recorded correctly.

#### Acceptance Criteria

1. WHEN vital signs are saved successfully, THE Triage_System SHALL display a green success alert with message "✓ Signos vitales guardados exitosamente"
2. WHEN Manchester classification is saved successfully, THE Triage_System SHALL display a green success alert with message "✓ Clasificación Manchester registrada exitosamente"
3. WHEN Manchester classification is saved successfully, THE success alert SHALL display a countdown message "Redirigiendo a Triaje Pendiente..."
4. WHEN the final success alert is displayed, THE Triage_System SHALL hide the form to prevent duplicate submissions

### Requirement 18: Maintain Responsive Layout

**User Story:** As a Staff_User, I want the interface to work on different screen sizes, so that I can use it on tablets or desktop computers.

#### Acceptance Criteria

1. THE Triage_System SHALL use responsive grid layouts that adapt to screen width
2. THE Triage_System SHALL display vital signs fields in a 4-column grid on large screens
3. THE Triage_System SHALL display vital signs fields in a 2-column grid on medium screens
4. THE Triage_System SHALL display vital signs fields in a 1-column grid on small screens
5. THE Classification_Section SHALL maintain readability and usability on all screen sizes
