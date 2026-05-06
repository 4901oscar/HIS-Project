# Implementation Plan: Laboratory Sample Management System

## Overview

This implementation plan breaks down the Laboratory Sample Management System into discrete coding tasks. The system implements a 4-step wizard interface for managing lab workflows from sample collection to results delivery. The implementation follows a microservice architecture with Spring Boot backend services (Clinical Service and Lab Service) and a React/TypeScript frontend.

**Key Implementation Areas:**
- Backend: Extend appointment status enum, add state transition methods, create new REST endpoints
- Lab Service: Add file upload endpoints, result validation, and test completeness checking
- Frontend: Build wizard interface with 4 steps, implement file upload, add audio notifications
- Testing: Property-based tests for state machine, file validation, and UI rendering

## Tasks

- [x] 1. Set up database schema and migrations
  - Create Flyway migration for lab_results table extensions
  - Add columns: test_name (VARCHAR 255), original_filename (VARCHAR 500), file_size (BIGINT)
  - Create index on test_name for query performance
  - Test migration rollback and re-apply
  - _Requirements: 15.1, 15.2, 15.5_

- [x] 2. Extend Appointment domain model with lab workflow states
  - [x] 2.1 Add new status enum values to AppointmentStatus
    - Add LAB_SAMPLE_COLLECTION, LAB_SAMPLE_PENDING, LAB_PROCESSING, LAB_RESULTS_READY
    - Update any status-related validation or mapping logic
    - _Requirements: 1.1, 1.2_
  
  - [x] 2.2 Implement state transition methods in Appointment entity
    - Implement collectLabSamples() method (LAB_SAMPLE_COLLECTION → LAB_SAMPLE_PENDING)
    - Implement acceptLabSamples() method (LAB_SAMPLE_PENDING → LAB_PROCESSING)
    - Implement rejectLabSamples() method (LAB_SAMPLE_PENDING → LAB_SAMPLE_COLLECTION)
    - Implement completeLabProcessing() method (LAB_PROCESSING → LAB_RESULTS_READY)
    - Implement sendLabResultsToDoctor() method (LAB_RESULTS_READY → CONSULTATION)
    - Each method validates current state and throws IllegalStateException for invalid transitions
    - _Requirements: 1.4, 1.5, 9.1, 9.2_
  
  - [ ]* 2.3 Write property test for state transition validation
    - **Property 1: State Transition Validation**
    - **Validates: Requirements 1.4, 1.5, 9.2, 9.3, 9.4**
    - Generate all possible (currentStatus, targetStatus) combinations
    - Verify valid transitions are accepted and invalid transitions throw IllegalStateException
    - Verify error messages contain current and target status information

- [x] 3. Checkpoint - Verify domain model changes
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Implement Clinical Service REST endpoints for lab workflow
  - [x] 4.1 Add PUT /api/clinical/appointments/{id}/lab/collect-samples endpoint
    - Validate appointment exists and is in LAB_SAMPLE_COLLECTION state
    - Call appointment.collectLabSamples() domain method
    - Save and return updated appointment
    - Return HTTP 400 with descriptive error for invalid transitions
    - _Requirements: 4.5, 9.3, 12.6_
  
  - [x] 4.2 Add PUT /api/clinical/appointments/{id}/lab/accept-samples endpoint
    - Validate appointment exists and is in LAB_SAMPLE_PENDING state
    - Call appointment.acceptLabSamples() domain method
    - Save and return updated appointment
    - _Requirements: 5.4, 9.3, 12.6_
  
  - [x] 4.3 Add PUT /api/clinical/appointments/{id}/lab/reject-samples endpoint
    - Validate appointment exists and is in LAB_SAMPLE_PENDING state
    - Call appointment.rejectLabSamples() domain method
    - Save and return updated appointment
    - _Requirements: 5.5, 9.3, 12.6_
  
  - [x] 4.4 Add PUT /api/clinical/appointments/{id}/lab/complete-processing endpoint
    - Validate appointment exists and is in LAB_PROCESSING state
    - Get lab order for this appointment
    - Call Lab Service to verify all tests have results
    - If validation passes, call appointment.completeLabProcessing()
    - If validation fails, return HTTP 400 with list of missing tests
    - Save and return updated appointment
    - _Requirements: 6.8, 6.9, 6.10, 9.3, 12.6_
  
  - [x] 4.5 Add PUT /api/clinical/appointments/{id}/lab/send-to-doctor endpoint
    - Validate appointment exists and is in LAB_RESULTS_READY state
    - Call appointment.sendLabResultsToDoctor() domain method
    - Save and return updated appointment
    - _Requirements: 7.5, 7.6, 9.3, 12.6_
  
  - [ ]* 4.6 Write unit tests for Clinical Service lab endpoints
    - Test successful state transitions
    - Test invalid state transition error handling
    - Test appointment not found scenarios
    - Test HTTP status codes (200, 400, 404)
    - _Requirements: 12.6, 13.4_

- [x] 5. Extend Lab Service domain models
  - [x] 5.1 Extend LabResult entity with new fields
    - Add testName field (String, not null)
    - Add originalFilename field (String, nullable)
    - Add fileSize field (Long, nullable)
    - Update constructors and builder
    - _Requirements: 11.5, 15.4_
  
  - [x] 5.2 Create LabOrderWithTestsResponse DTO
    - Include order details: id, orderCode, patientId, doctorId, appointmentId
    - Include list of TestDetail objects
    - TestDetail includes: testName, testType, sampleType, hasResult flag
    - _Requirements: 10.1, 10.2, 10.3_
  
  - [x] 5.3 Create LabResultResponse DTO
    - Include: id, orderId, testName, originalFilename, fileSize, uploadedAt, uploadedBy
    - Include downloadUrl or file path for retrieval
    - _Requirements: 7.2, 11.5_
  
  - [x] 5.4 Create ValidationResponse DTO
    - Include isValid boolean flag
    - Include list of missing test names
    - Include descriptive message
    - _Requirements: 6.10, 13.2_

- [x] 6. Implement Lab Service file upload and validation
  - [x] 6.1 Create FileStorageService for file operations
    - Implement store(MultipartFile, String uniqueFilename) method
    - Generate unique filenames using UUID + timestamp + original extension
    - Store files to configured storage path
    - Return stored file path
    - Handle file storage exceptions
    - _Requirements: 11.3, 11.4, 11.7_
  
  - [x] 6.2 Implement file validation methods in LabResultService
    - Implement validateFileFormat() - accept only PDF, JPEG, PNG
    - Implement validateFileSize() - reject files > 10 MB
    - Throw descriptive exceptions for validation failures
    - _Requirements: 6.4, 11.1, 11.2, 13.2_
  
  - [ ]* 6.3 Write property test for file format validation
    - **Property 5: File Format Validation**
    - **Validates: Requirements 6.4, 11.1**
    - Generate files with various MIME types
    - Verify only PDF, JPEG, PNG are accepted
    - Verify other formats are rejected with descriptive error
  
  - [ ]* 6.4 Write property test for file size validation
    - **Property 6: File Size Validation**
    - **Validates: Requirements 11.2**
    - Generate files with various sizes including boundary values (10MB, 10MB+1)
    - Verify files <= 10MB are accepted
    - Verify files > 10MB are rejected
  
  - [ ]* 6.5 Write property test for filename uniqueness
    - **Property 7: Filename Uniqueness**
    - **Validates: Requirements 11.3**
    - Generate sequences of file uploads with same original names
    - Verify all generated filenames are unique

- [x] 7. Implement Lab Service REST endpoints
  - [x] 7.1 Add GET /api/lab/orders/by-appointment/{appointmentId} endpoint
    - Query lab_orders table by appointmentId
    - Retrieve all tests for the order
    - For each test, check if results exist (hasResult flag)
    - Return LabOrderWithTestsResponse
    - _Requirements: 10.1, 10.2, 10.3, 10.5, 12.4_
  
  - [x] 7.2 Add POST /api/lab/orders/{orderId}/tests/{testName}/results endpoint
    - Accept multipart/form-data with file field
    - Validate file format and size
    - Generate unique filename
    - Store file using FileStorageService
    - Create LabResult record with metadata
    - Return LabResultResponse with file ID
    - Handle upload failures with descriptive errors
    - _Requirements: 6.5, 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 12.4_
  
  - [x] 7.3 Add GET /api/lab/orders/{orderId}/results endpoint
    - Query lab_results table by orderId
    - Return list of LabResultResponse objects
    - Include file metadata and download URLs
    - _Requirements: 7.2, 7.3, 12.4_
  
  - [x] 7.4 Add GET /api/lab/orders/{orderId}/validation/all-tests-complete endpoint
    - Get lab order with test names
    - Get all results for this order
    - Compare test names to identify missing tests
    - Return ValidationResponse with isValid flag and missing tests list
    - _Requirements: 6.8, 6.10, 12.4_
  
  - [ ]* 7.5 Write property test for results completeness validation
    - **Property 8: Results Completeness Validation**
    - **Validates: Requirements 6.8, 6.10**
    - Generate lab orders with varying numbers of tests (1-50)
    - Generate result sets with varying completeness
    - Verify validation correctly identifies missing tests
  
  - [ ]* 7.6 Write integration tests for Lab Service endpoints
    - Test file upload and retrieval flow
    - Test validation endpoint with complete and incomplete results
    - Test concurrent file uploads for different tests
    - Test error handling for invalid order IDs
    - _Requirements: 11.6, 12.4, 15.4_

- [x] 8. Checkpoint - Verify backend services
  - Run all backend tests (unit, property, integration)
  - Verify API endpoints with manual testing or Postman
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Create frontend API client functions
  - [x] 9.1 Create clinicalApi.ts with appointment status update functions
    - Implement collectLabSamples(appointmentId)
    - Implement acceptLabSamples(appointmentId)
    - Implement rejectLabSamples(appointmentId)
    - Implement completeLabProcessing(appointmentId)
    - Implement sendLabResultsToDoctor(appointmentId)
    - All functions include X-User-Id header
    - Handle HTTP errors and return descriptive messages
    - _Requirements: 12.1, 12.3, 13.1, 13.2_
  
  - [x] 9.2 Create labApi.ts with lab service functions
    - Implement getLabOrderByAppointmentId(appointmentId)
    - Implement uploadTestResult(orderId, testName, file)
    - Implement getLabOrderResults(orderId)
    - Implement validateAllTestsComplete(orderId)
    - Handle multipart/form-data for file uploads
    - Handle HTTP errors and return descriptive messages in Spanish
    - _Requirements: 12.2, 12.4, 13.1, 13.2, 13.3_
  
  - [ ]* 9.3 Write property test for HTTP status code handling
    - **Property 10: HTTP Status Code Correctness**
    - **Validates: Requirements 12.6**
    - Mock various API responses (200, 400, 404, 500)
    - Verify client functions handle each status code correctly
  
  - [ ]* 9.4 Write property test for error message localization
    - **Property 11: Error Message Localization and Specificity**
    - **Validates: Requirements 13.1, 13.2, 13.3**
    - Generate various error conditions
    - Verify error messages are in Spanish
    - Verify error messages contain specific details

- [x] 10. Implement wizard progress bar component
  - [x] 10.1 Create WizardProgressBar.tsx component
    - Display 4 numbered steps with labels in Spanish
    - Accept currentStep prop (1-4)
    - Apply distinct styling for completed, current, and pending steps
    - Use Tailwind CSS classes for styling
    - Display progress line between steps
    - _Requirements: 3.1, 3.2, 3.6, 14.2, 14.6_
  
  - [ ]* 10.2 Write property test for step visual state indication
    - **Property 14: Step Visual State Indication**
    - **Validates: Requirements 3.6**
    - Generate various currentStep values (1-4)
    - Verify correct CSS classes applied to completed, current, pending steps
    - Verify visual distinction is clear

- [x] 11. Implement wizard container and navigation logic
  - [x] 11.1 Create LabSampleWorkflow.tsx container component
    - Accept appointmentId from route params
    - Fetch appointment data on mount
    - Fetch lab order data on mount
    - Implement getStepFromStatus() mapping function
    - Maintain currentStep state based on appointment status
    - Render WizardProgressBar with currentStep
    - Render appropriate step component based on currentStep
    - Handle loading and error states
    - _Requirements: 3.3, 3.4, 3.5, 10.1, 14.5_
  
  - [ ]* 11.2 Write property test for status-to-step mapping
    - **Property 2: Status-to-Step Mapping Consistency**
    - **Validates: Requirements 3.3, 3.4, 4.1, 5.1, 6.1, 7.1**
    - Generate appointments with all four lab statuses
    - Verify correct step number (1-4) is returned for each status
  
  - [ ]* 11.3 Write property test for wizard step content isolation
    - **Property 13: Wizard Step Content Isolation**
    - **Validates: Requirements 3.5**
    - Generate various currentStep values
    - Verify only content for that step is rendered
    - Verify content from other steps is hidden

- [x] 12. Implement Step 1: Sample Collection
  - [x] 12.1 Create SampleCollectionStep.tsx component
    - Display "Paso 1: Recolección de Muestras" heading
    - Display list of tests from lab order
    - Show test name, test type, sample type for each test
    - Display "Recolectar Muestras" button
    - Handle button click to call collectLabSamples API
    - Disable button during API request
    - Show loading indicator during API request
    - Handle success: refresh wizard to show step 2
    - Handle errors: display error message in Spanish
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 13.1, 13.2, 14.4, 14.5_
  
  - [ ]* 12.2 Write property test for test information completeness
    - **Property 4: Test Information Completeness**
    - **Validates: Requirements 4.2, 4.3, 10.3**
    - Generate lab orders with various test data
    - Verify all required fields are rendered (test name, type, sample type)

- [x] 13. Implement Step 2: Sample Validation
  - [x] 13.1 Create SampleValidationStep.tsx component
    - Display "Paso 2: Validar Muestras" heading
    - Display list of tests with pending samples
    - Display "Aceptar muestras" button
    - Display "Solicitar nueva muestra" button
    - Handle "Aceptar muestras" click to call acceptLabSamples API
    - Handle "Solicitar nueva muestra" click to call rejectLabSamples API
    - Disable buttons during API requests
    - Show loading indicator during API requests
    - Handle success: refresh wizard to show appropriate step
    - Handle errors: display error message in Spanish
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 13.1, 13.2, 14.4, 14.5_

- [x] 14. Implement Step 3: Test Processing and Results Upload
  - [x] 14.1 Create TestResultUpload.tsx component
    - Accept testName prop
    - Display file input control
    - Accept PDF, JPEG, PNG files only (HTML5 accept attribute)
    - Validate file format on selection
    - Validate file size (<= 10MB) on selection
    - Display validation errors in Spanish
    - Display uploaded file name and timestamp
    - Emit onUpload event with file
    - _Requirements: 6.3, 6.4, 6.6, 11.1, 11.2, 13.3_
  
  - [x] 14.2 Create TestProcessingStep.tsx component
    - Display "Paso 3: Procesar Exámenes" heading
    - Display list of tests from lab order
    - Render TestResultUpload component for each test
    - Track uploaded results in local state (Map<testName, File>)
    - Handle file upload for each test via uploadTestResult API
    - Display "Marcar como completado" button
    - Handle button click to validate all tests have results
    - If validation fails, display error with missing test names
    - If validation passes, call completeLabProcessing API
    - Disable button during API requests
    - Show loading indicator during file uploads
    - Handle errors: display error message in Spanish
    - _Requirements: 6.1, 6.2, 6.5, 6.7, 6.8, 6.9, 6.10, 13.1, 13.2, 13.3, 14.4, 14.5_
  
  - [ ]* 14.3 Write property test for multiple results per test
    - **Property 12: Multiple Results Per Test**
    - **Validates: Requirements 15.4**
    - Generate sequences of file uploads for same test
    - Verify all results are stored without overwriting

- [x] 15. Implement Step 4: Results Ready and Delivery
  - [x] 15.1 Create ResultsList.tsx component
    - Accept results prop (array of LabResult)
    - Display test name for each result
    - Display original filename
    - Display upload timestamp
    - Provide download or preview link
    - _Requirements: 7.2, 7.3_
  
  - [x] 15.2 Create ResultsReadyStep.tsx component
    - Display "Paso 4: Resultados Listos" heading
    - Fetch lab order results on mount
    - Render ResultsList component with fetched results
    - Display "Enviar a doctor" button
    - Handle button click to call sendLabResultsToDoctor API
    - Disable button during API request
    - Show loading indicator during API request
    - Handle success: display success message and navigate to appointment list
    - Handle errors: display error message in Spanish
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 13.1, 13.2, 14.4, 14.5_

- [x] 16. Checkpoint - Verify wizard implementation
  - Test wizard navigation through all 4 steps
  - Test file upload functionality
  - Test error handling and validation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 17. Implement audio notification system
  - [x] 17.1 Add audio notification file to public assets
    - Add notification.mp3 or notification.wav to /public/sounds/
    - Ensure file duration is between 0.5 and 2 seconds
    - Use same audio file as triage and doctor modules for consistency
    - _Requirements: 8.1, 8.4, 8.5_
  
  - [x] 17.2 Create AudioNotification.ts utility
    - Implement playNotificationSound() function
    - Use HTML5 Audio API
    - Handle audio playback errors gracefully (catch and log)
    - Do not block execution on audio failure
    - _Requirements: 8.2, 8.3, 13.6_
  
  - [ ]* 17.3 Write property test for audio playback error resilience
    - **Property 15: Audio Playback Error Resilience**
    - **Validates: Requirements 8.3**
    - Mock audio playback failures
    - Verify navigation proceeds successfully despite audio errors

- [x] 18. Integrate audio notification with appointment list
  - [x] 18.1 Update appointment list "Atender" button handler
    - Call playNotificationSound() when button is clicked
    - Wait 500ms for audio to play
    - Navigate to /lab/workflow/{appointmentId}
    - Handle navigation even if audio fails
    - _Requirements: 2.3, 2.4, 2.5, 8.2, 8.3_
  
  - [ ]* 18.2 Write property test for laboratory appointment filtering
    - **Property 3: Laboratory Appointment Filtering**
    - **Validates: Requirements 2.1, 2.6**
    - Generate appointments with mixed statuses
    - Verify filtering returns only lab-related statuses
    - Verify other statuses are excluded
  
  - [ ]* 18.3 Write property test for appointment field rendering
    - **Property 20: Appointment Field Rendering**
    - **Validates: Requirements 2.2**
    - Generate appointments with various data
    - Verify all required fields are rendered (patient name, date, time, status, order ID)

- [x] 19. Implement error handling and user feedback
  - [x] 19.1 Create ErrorMessage.tsx component
    - Accept error message prop
    - Display error in Spanish with clear styling
    - Auto-dismiss after 5 seconds or allow manual dismissal
    - Use consistent styling with MedFlow design system
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 14.6_
  
  - [x] 19.2 Integrate error handling in all step components
    - Wrap API calls in try-catch blocks
    - Extract error messages from API responses
    - Display errors using ErrorMessage component
    - Log errors to console for debugging
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6_
  
  - [ ]* 19.3 Write property test for invalid transition error messages
    - **Property 9: Invalid Transition Error Messages**
    - **Validates: Requirements 9.3, 13.4**
    - Generate invalid state transition attempts
    - Verify error messages contain current and target status
  
  - [ ]* 19.4 Write property test for console error logging
    - **Property 18: Console Error Logging**
    - **Validates: Requirements 13.6**
    - Generate various error conditions
    - Verify errors are logged to console

- [x] 20. Implement responsive design and accessibility
  - [x] 20.1 Apply responsive layout to wizard components
    - Ensure wizard works on screens >= 1024px width
    - Use Tailwind responsive classes
    - Test on various screen sizes
    - _Requirements: 14.1, 14.6_
  
  - [x] 20.2 Add visual feedback for interactive elements
    - Add hover states to buttons
    - Add focus states for keyboard navigation
    - Add active states for clicks
    - Ensure sufficient color contrast ratios
    - _Requirements: 14.2, 14.3, 14.6_
  
  - [ ]* 20.3 Write property test for button disabling during API requests
    - **Property 16: Button Disabling During API Requests**
    - **Validates: Requirements 14.4**
    - Simulate API requests in progress
    - Verify all action buttons are disabled
    - Verify buttons re-enable when request completes
  
  - [ ]* 20.4 Write property test for loading indicator display
    - **Property 17: Loading Indicator Display**
    - **Validates: Requirements 14.5**
    - Simulate various asynchronous operations
    - Verify loading indicator displays during operation
    - Verify loading indicator hides when operation completes

- [x] 21. Add routing configuration for lab workflow
  - [x] 21.1 Add route for lab workflow wizard
    - Add route /lab/workflow/:appointmentId
    - Map to LabSampleWorkflow component
    - Ensure route is protected (requires authentication)
    - Ensure route is accessible only to LABORATORY role
    - _Requirements: 2.5, 3.1_
  
  - [x] 21.2 Update appointment list to link to lab workflow
    - Update "Atender" button to navigate to /lab/workflow/{appointmentId}
    - Ensure navigation includes audio notification
    - _Requirements: 2.3, 2.4, 2.5_

- [x] 22. Checkpoint - Verify frontend integration
  - Test complete user workflow from appointment list to results delivery
  - Test audio notifications
  - Test error handling and validation
  - Test responsive design
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 23. Write end-to-end integration tests
  - [ ]* 23.1 Write E2E test for complete workflow
    - Test: Lab technician views appointment list
    - Test: Lab technician clicks "Atender" (audio plays, navigates to wizard)
    - Test: Lab technician collects samples (step 1 → step 2)
    - Test: Lab technician accepts samples (step 2 → step 3)
    - Test: Lab technician uploads results for all tests
    - Test: Lab technician marks as complete (step 3 → step 4)
    - Test: Lab technician sends results to doctor (step 4 → consultation)
    - _Requirements: All requirements_
  
  - [ ]* 23.2 Write E2E test for sample rejection flow
    - Test: Lab technician rejects samples (step 2 → step 1)
    - Test: Lab technician collects new samples (step 1 → step 2)
    - Test: Lab technician accepts samples and continues workflow
    - _Requirements: 5.5, 5.6_
  
  - [ ]* 23.3 Write E2E test for incomplete results validation
    - Test: Lab technician attempts to complete with missing results
    - Test: Error message displays missing test names
    - Test: Lab technician uploads missing results
    - Test: Lab technician successfully completes processing
    - _Requirements: 6.8, 6.9, 6.10, 13.2_

- [ ] 24. Performance optimization and testing
  - [ ]* 24.1 Verify test data retrieval performance
    - Test: Retrieve lab order with 50 tests
    - Verify response time < 500ms
    - _Requirements: 10.5_
  
  - [ ]* 24.2 Verify file upload performance
    - Test: Upload 10MB file
    - Verify response time < 3 seconds
    - _Requirements: 11.6_
  
  - [ ] 24.3 Add database indexes for performance
    - Verify index on appointments.status exists
    - Verify index on lab_orders.appointment_id exists
    - Verify index on lab_results.order_id exists
    - Verify index on lab_results.test_name exists
    - _Requirements: 10.5, 11.6_

- [ ] 25. Add configuration and deployment preparation
  - [ ] 25.1 Add configuration to Clinical Service
    - Add lab.workflow.enabled property to application.yml
    - Add lab.workflow.statuses list to application.yml
    - Document configuration options
    - _Requirements: 1.1_
  
  - [ ] 25.2 Add configuration to Lab Service
    - Add file.upload.max-size property (10485760 bytes)
    - Add file.upload.allowed-types list (PDF, JPEG, PNG)
    - Add file.upload.storage-path property
    - Document configuration options
    - _Requirements: 11.1, 11.2, 11.4_
  
  - [ ] 25.3 Create deployment documentation
    - Document database migration steps
    - Document service deployment order (Lab Service first, then Clinical Service)
    - Document frontend deployment steps
    - Document rollback plan
    - Document smoke test procedures
    - _Requirements: All requirements_

- [ ] 26. Final checkpoint - Complete system verification
  - Run all tests (unit, property, integration, E2E)
  - Verify all acceptance criteria are met
  - Test complete workflow manually
  - Review code for security issues
  - Review code for performance issues
  - Ensure all tests pass, ask the user if questions arise.

## Notes

### Property-Based Testing
- All property tests use jqwik (Java) for backend and fast-check (TypeScript) for frontend
- Minimum 100 iterations per property test
- Property tests are marked with `*` and are optional for faster MVP
- Each property test explicitly references the property number and requirements it validates

### Testing Strategy
- Unit tests validate specific examples and edge cases
- Property tests validate universal correctness properties
- Integration tests verify end-to-end API functionality
- E2E tests verify complete user workflows

### Implementation Order
- Backend domain model and state machine first (tasks 1-3)
- Backend REST endpoints second (tasks 4-8)
- Frontend API clients third (task 9)
- Frontend UI components fourth (tasks 10-16)
- Audio notifications and integration fifth (tasks 17-18)
- Error handling and polish sixth (tasks 19-20)
- Routing and final integration seventh (tasks 21-22)
- Testing and optimization eighth (tasks 23-24)
- Configuration and deployment last (tasks 25-26)

### Key Technical Decisions
- Use Spring Boot 3.2.4 with Java 17
- Use React 19 with TypeScript
- Use jqwik 1.8.2 for property-based testing (backend)
- Use fast-check 4.7.0 for property-based testing (frontend)
- Use Tailwind CSS for styling
- Use Axios for HTTP requests
- Use HTML5 Audio API for notifications
- Store files in persistent storage (configurable path)
- Use multipart/form-data for file uploads
- Use Flyway for database migrations

### Requirements Coverage
All 15 requirements and 93 acceptance criteria are covered by implementation tasks. Each task explicitly references the requirements it implements for traceability.
