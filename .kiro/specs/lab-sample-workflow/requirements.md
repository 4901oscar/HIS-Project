# Requirements Document - Laboratory Sample Management System

## Introduction

El Sistema de Gestión de Muestras de Laboratorio (Laboratory Sample Management System) es un módulo integral para el sistema hospitalario MedFlow que permite a los técnicos de laboratorio gestionar el flujo completo de trabajo desde la recolección de muestras hasta la entrega de resultados al médico. El sistema implementa un flujo de trabajo de 4 pasos (wizard/stepper) donde cada paso corresponde a un estado diferente de la cita de laboratorio.

## Glossary

- **Lab_System**: El módulo de gestión de muestras de laboratorio dentro de MedFlow
- **Lab_Technician**: Usuario técnico de laboratorio que opera el sistema
- **Lab_Order**: Orden de laboratorio que contiene múltiples pruebas solicitadas por un médico
- **Appointment**: Cita asociada a una orden de laboratorio con un estado específico
- **Sample**: Muestra biológica recolectada del paciente (sangre, orina, etc.)
- **Test**: Prueba o examen de laboratorio individual dentro de una orden
- **Result_File**: Archivo digital (PDF o imagen) que contiene los resultados de una prueba
- **Wizard_Interface**: Interfaz de usuario tipo stepper con pasos numerados y barra de progreso
- **Doctor**: Médico que solicitó las pruebas de laboratorio
- **Patient**: Paciente del cual se recolectan las muestras
- **Clinical_Service**: Microservicio backend que gestiona citas y órdenes clínicas
- **Lab_Service**: Microservicio backend que gestiona operaciones específicas de laboratorio
- **Frontend_App**: Aplicación React que presenta la interfaz de usuario

## Requirements

### Requirement 1: Appointment Status Management

**User Story:** Como técnico de laboratorio, quiero que el sistema gestione los estados de las citas de laboratorio, para que pueda seguir el progreso del flujo de trabajo de manera estructurada.

#### Acceptance Criteria

1. THE Lab_System SHALL support four appointment statuses: LAB_SAMPLE_COLLECTION, LAB_SAMPLE_PENDING, LAB_PROCESSING, and LAB_RESULTS_READY
2. WHEN a Lab_Order is created by a Doctor, THE Lab_System SHALL initialize the Appointment status to LAB_SAMPLE_COLLECTION
3. THE Lab_System SHALL persist appointment status changes in the PostgreSQL database
4. WHEN an Appointment status is updated, THE Lab_System SHALL validate that the transition is allowed according to the workflow rules
5. THE Lab_System SHALL reject invalid status transitions and return a descriptive error message

### Requirement 2: Laboratory Appointment List View

**User Story:** Como técnico de laboratorio, quiero ver una lista de todas las citas de laboratorio pendientes, para que pueda seleccionar cuál atender.

#### Acceptance Criteria

1. THE Frontend_App SHALL display a list of all Appointments with laboratory-related statuses
2. WHEN the Lab_Technician views the list, THE Frontend_App SHALL show for each Appointment: patient name, appointment date/time, current status, and Lab_Order identifier
3. THE Frontend_App SHALL display an "Atender" button for each Appointment in the list
4. WHEN the Lab_Technician clicks the "Atender" button, THE Frontend_App SHALL play an audio notification sound
5. WHEN the audio notification completes, THE Frontend_App SHALL navigate to the wizard form view for that Appointment
6. THE Frontend_App SHALL filter the list to show only Appointments with statuses: LAB_SAMPLE_COLLECTION, LAB_SAMPLE_PENDING, LAB_PROCESSING, or LAB_RESULTS_READY

### Requirement 3: Wizard Interface Navigation

**User Story:** Como técnico de laboratorio, quiero una interfaz tipo wizard con pasos numerados, para que pueda entender visualmente en qué etapa del proceso me encuentro.

#### Acceptance Criteria

1. THE Frontend_App SHALL display a Wizard_Interface with 4 numbered steps and a progress bar
2. THE Wizard_Interface SHALL label the steps as: "1. Recolección de Muestras", "2. Validar Muestras", "3. Procesar Exámenes", "4. Resultados Listos"
3. WHEN an Appointment is loaded, THE Frontend_App SHALL highlight the current step based on the Appointment status
4. THE Frontend_App SHALL map LAB_SAMPLE_COLLECTION to step 1, LAB_SAMPLE_PENDING to step 2, LAB_PROCESSING to step 3, and LAB_RESULTS_READY to step 4
5. THE Frontend_App SHALL display only the form content and actions relevant to the current step
6. THE Frontend_App SHALL visually indicate completed steps, the current step, and pending steps using distinct styling

### Requirement 4: Sample Collection Step (Step 1)

**User Story:** Como técnico de laboratorio, quiero recolectar muestras del paciente, para que pueda iniciar el procesamiento de las pruebas solicitadas.

#### Acceptance Criteria

1. WHEN an Appointment has status LAB_SAMPLE_COLLECTION, THE Frontend_App SHALL display step 1 of the wizard
2. THE Frontend_App SHALL display a list of all Tests included in the Lab_Order
3. THE Frontend_App SHALL show for each Test: test name, test type, and sample type required
4. THE Frontend_App SHALL display a "Recolectar Muestras" button
5. WHEN the Lab_Technician clicks "Recolectar Muestras", THE Lab_System SHALL update the Appointment status to LAB_SAMPLE_PENDING
6. WHEN the status update succeeds, THE Frontend_App SHALL refresh the wizard to display step 2

### Requirement 5: Sample Validation Step (Step 2)

**User Story:** Como técnico de laboratorio, quiero validar que las muestras recolectadas son adecuadas, para que pueda aceptarlas o solicitar nuevas muestras si es necesario.

#### Acceptance Criteria

1. WHEN an Appointment has status LAB_SAMPLE_PENDING, THE Frontend_App SHALL display step 2 of the wizard
2. THE Frontend_App SHALL display a list of all Tests with their pending Samples
3. THE Frontend_App SHALL display two action buttons: "Aceptar muestras" and "Solicitar nueva muestra"
4. WHEN the Lab_Technician clicks "Aceptar muestras", THE Lab_System SHALL update the Appointment status to LAB_PROCESSING
5. WHEN the Lab_Technician clicks "Solicitar nueva muestra", THE Lab_System SHALL update the Appointment status back to LAB_SAMPLE_COLLECTION
6. WHEN the status update succeeds, THE Frontend_App SHALL refresh the wizard to display the corresponding step

### Requirement 6: Test Processing and Results Upload (Step 3)

**User Story:** Como técnico de laboratorio, quiero procesar los exámenes y cargar los resultados, para que estén disponibles para revisión médica.

#### Acceptance Criteria

1. WHEN an Appointment has status LAB_PROCESSING, THE Frontend_App SHALL display step 3 of the wizard
2. THE Frontend_App SHALL display a list of all Tests in the Lab_Order
3. THE Frontend_App SHALL provide a file upload control for each Test that accepts PDF and image files (JPEG, PNG)
4. WHEN the Lab_Technician selects a Result_File, THE Frontend_App SHALL validate that the file format is PDF, JPEG, or PNG
5. WHEN the Lab_Technician uploads a Result_File, THE Lab_System SHALL store the file and associate it with the corresponding Test
6. THE Frontend_App SHALL display uploaded Result_Files with their filenames and upload timestamps
7. THE Frontend_App SHALL display a "Marcar como completado" button
8. WHEN the Lab_Technician clicks "Marcar como completado", THE Lab_System SHALL verify that at least one Result_File has been uploaded for each Test
9. IF all Tests have uploaded results, THEN THE Lab_System SHALL update the Appointment status to LAB_RESULTS_READY
10. IF any Test lacks results, THEN THE Lab_System SHALL display an error message indicating which Tests need results

### Requirement 7: Results Ready and Delivery (Step 4)

**User Story:** Como técnico de laboratorio, quiero revisar los resultados finales y enviarlos al médico, para que el médico pueda consultarlos y continuar con el tratamiento del paciente.

#### Acceptance Criteria

1. WHEN an Appointment has status LAB_RESULTS_READY, THE Frontend_App SHALL display step 4 of the wizard
2. THE Frontend_App SHALL display a list of all Tests with their uploaded Result_Files
3. THE Frontend_App SHALL provide a preview or download link for each Result_File
4. THE Frontend_App SHALL display a "Enviar a doctor" button
5. WHEN the Lab_Technician clicks "Enviar a doctor", THE Lab_System SHALL update the Appointment status to CONSULTATION
6. WHEN the status changes to CONSULTATION, THE Lab_System SHALL make the results accessible to the Doctor who created the Lab_Order
7. WHEN the status update succeeds, THE Frontend_App SHALL display a success message and return to the appointment list view

### Requirement 8: Audio Notification System

**User Story:** Como técnico de laboratorio, quiero recibir una notificación sonora al atender una cita, para que tenga confirmación auditiva de la acción similar a otros módulos del sistema.

#### Acceptance Criteria

1. THE Frontend_App SHALL include an audio notification file in a supported web format (MP3 or WAV)
2. WHEN the Lab_Technician clicks the "Atender" button, THE Frontend_App SHALL play the audio notification
3. THE Frontend_App SHALL handle audio playback errors gracefully without blocking navigation
4. THE audio notification SHALL have a duration between 0.5 and 2 seconds
5. THE Frontend_App SHALL use the same audio notification pattern as the triage and doctor modules for consistency

### Requirement 9: Status Transition Validation

**User Story:** Como desarrollador del sistema, quiero que las transiciones de estado sean validadas, para que se mantenga la integridad del flujo de trabajo.

#### Acceptance Criteria

1. THE Lab_System SHALL define valid status transitions: LAB_SAMPLE_COLLECTION → LAB_SAMPLE_PENDING, LAB_SAMPLE_PENDING → LAB_PROCESSING, LAB_SAMPLE_PENDING → LAB_SAMPLE_COLLECTION, LAB_PROCESSING → LAB_RESULTS_READY, LAB_RESULTS_READY → CONSULTATION
2. WHEN a status update request is received, THE Lab_System SHALL verify that the requested transition is in the valid transitions list
3. IF the transition is invalid, THEN THE Lab_System SHALL return an HTTP 400 error with a message indicating the current status and the invalid target status
4. IF the transition is valid, THEN THE Lab_System SHALL proceed with the status update
5. THE Lab_System SHALL log all status transition attempts including timestamp, user, appointment identifier, and transition result

### Requirement 10: Lab Order and Test Data Retrieval

**User Story:** Como técnico de laboratorio, quiero ver todos los detalles de las pruebas solicitadas, para que pueda realizar el trabajo correctamente.

#### Acceptance Criteria

1. WHEN the wizard form loads, THE Lab_System SHALL retrieve the Lab_Order associated with the Appointment
2. THE Lab_System SHALL retrieve all Tests included in the Lab_Order
3. THE Frontend_App SHALL display for each Test: test identifier, test name, test type, sample type required, and current processing status
4. WHEN a Lab_Order contains multiple Tests, THE Frontend_App SHALL display them in a structured list or table format
5. THE Lab_System SHALL return Test data within 500 milliseconds for Lab_Orders containing up to 50 Tests

### Requirement 11: File Upload and Storage

**User Story:** Como técnico de laboratorio, quiero cargar archivos de resultados de manera segura, para que los resultados estén disponibles para el médico.

#### Acceptance Criteria

1. THE Lab_System SHALL accept Result_File uploads in PDF, JPEG, and PNG formats
2. THE Lab_System SHALL validate that uploaded files do not exceed 10 MB in size
3. WHEN a Result_File is uploaded, THE Lab_System SHALL generate a unique filename to prevent collisions
4. THE Lab_System SHALL store Result_Files in a persistent storage location accessible to the Clinical_Service
5. THE Lab_System SHALL store metadata for each Result_File including: original filename, upload timestamp, file size, Lab_Technician identifier, and associated Test identifier
6. THE Lab_System SHALL return a success response with the stored file identifier within 3 seconds for files up to 10 MB
7. IF a file upload fails, THEN THE Lab_System SHALL return an error message indicating the failure reason

### Requirement 12: Microservice Communication

**User Story:** Como arquitecto del sistema, quiero que los microservicios se comuniquen correctamente, para que el flujo de trabajo funcione de extremo a extremo.

#### Acceptance Criteria

1. THE Frontend_App SHALL communicate with the Clinical_Service for appointment status updates
2. THE Frontend_App SHALL communicate with the Lab_Service for test-specific operations and file uploads
3. THE Clinical_Service SHALL expose REST API endpoints for: retrieving lab appointments, updating appointment status, and retrieving Lab_Order details
4. THE Lab_Service SHALL expose REST API endpoints for: retrieving Tests by Lab_Order, uploading Result_Files, and retrieving Result_Files
5. WHEN the Appointment status changes to CONSULTATION, THE Clinical_Service SHALL notify the Doctor module that results are available
6. THE Lab_System SHALL use HTTP status codes correctly: 200 for success, 400 for validation errors, 404 for not found, 500 for server errors

### Requirement 13: Error Handling and User Feedback

**User Story:** Como técnico de laboratorio, quiero recibir mensajes claros cuando algo falla, para que pueda entender qué salió mal y cómo corregirlo.

#### Acceptance Criteria

1. WHEN a network error occurs, THE Frontend_App SHALL display a user-friendly error message in Spanish
2. WHEN a validation error occurs, THE Frontend_App SHALL display the specific validation failure reason
3. WHEN a file upload fails, THE Frontend_App SHALL indicate which file failed and why
4. WHEN a status transition is rejected, THE Frontend_App SHALL explain why the transition is not allowed
5. THE Frontend_App SHALL display error messages for at least 5 seconds or until the user dismisses them
6. THE Frontend_App SHALL log errors to the browser console for debugging purposes

### Requirement 14: Responsive Design and Accessibility

**User Story:** Como técnico de laboratorio, quiero que la interfaz sea clara y fácil de usar, para que pueda trabajar eficientemente.

#### Acceptance Criteria

1. THE Frontend_App SHALL display the wizard interface in a responsive layout that adapts to screen sizes from 1024px width and above
2. THE Frontend_App SHALL use clear visual hierarchy with appropriate font sizes, spacing, and contrast ratios
3. THE Frontend_App SHALL provide visual feedback for interactive elements on hover and click
4. THE Frontend_App SHALL disable action buttons during API requests to prevent duplicate submissions
5. THE Frontend_App SHALL display loading indicators during data fetching and file uploads
6. THE Frontend_App SHALL use consistent styling with the existing MedFlow design system

### Requirement 15: Data Persistence and Consistency

**User Story:** Como administrador del sistema, quiero que todos los datos se persistan correctamente, para que no se pierda información crítica.

#### Acceptance Criteria

1. THE Lab_System SHALL persist all Appointment status changes in the PostgreSQL database within a transaction
2. THE Lab_System SHALL persist all Result_File metadata in the PostgreSQL database
3. WHEN a status update fails, THE Lab_System SHALL roll back any partial changes to maintain data consistency
4. THE Lab_System SHALL ensure that each Test can have multiple Result_Files associated with it
5. THE Lab_System SHALL maintain referential integrity between Appointments, Lab_Orders, Tests, and Result_Files
6. THE Lab_System SHALL use database constraints to prevent orphaned records

## Notes

### Parser and Serializer Requirements

This feature does not require custom parsers or serializers beyond standard JSON serialization provided by Spring Boot (Jackson) and React (native JSON). All data exchange uses standard JSON format over HTTP REST APIs.

### Testing Considerations

- Status transition validation is a critical area for property-based testing (testing all possible state combinations)
- File upload functionality should be tested with various file sizes and formats
- The wizard navigation logic should be tested to ensure correct step display based on appointment status
- Integration tests should verify the complete workflow from sample collection to results delivery
- Audio notification should be tested across different browsers for compatibility

### Future Enhancements

- Support for barcode scanning during sample collection
- Automatic test result parsing from laboratory equipment
- Email notifications to patients when results are ready
- Quality control checks and sample rejection reasons
- Batch processing for multiple appointments
- Results approval workflow before sending to doctor
