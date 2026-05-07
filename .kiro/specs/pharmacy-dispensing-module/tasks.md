# Implementation Plan: Pharmacy Dispensing Module

## Overview

This implementation plan breaks down the Pharmacy Dispensing Module into discrete coding tasks. The module provides pharmacists with a queue-based interface to view paid prescriptions awaiting dispensing, access detailed prescription information, and mark medications as dispensed.

**Implementation Order**: Backend first (endpoints, repositories, DTOs), then frontend services (API integration), then frontend components (UI), and finally integration and testing.

**Technology Stack**:
- Backend: Java + Spring Boot
- Frontend: TypeScript + React
- Testing: JUnit 5 + Mockito (backend), React Testing Library + Jest (frontend)

## Tasks

- [x] 1. Set up backend repository extensions
  - [x] 1.1 Add findByAppointmentId method to ConsultationRepository interface
    - Add method signature: `Optional<Consultation> findByAppointmentId(String appointmentId)`
    - _Requirements: 6.2_
  
  - [x] 1.2 Implement findByAppointmentId in JpaConsultationRepository
    - Add JPA query method: `Optional<ConsultationEntity> findByAppointmentId(String appointmentId)`
    - JPA will auto-generate the query based on method name
    - _Requirements: 6.2_
  
  - [x] 1.3 Wire up findByAppointmentId in ConsultationRepositoryAdapter
    - Implement the domain repository method by delegating to JPA repository
    - Map ConsultationEntity to Consultation domain object
    - _Requirements: 6.2_
  
  - [x] 1.4 Verify findByConsultationId exists in PrescriptionRepository
    - Check if `List<Prescription> findByConsultationId(String consultationId)` exists
    - If missing, add to JpaPrescriptionRepository and adapter
    - _Requirements: 6.2_

- [x] 2. Create backend DTOs for prescription details
  - [x] 2.1 Create PrescriptionDetailResponse DTO class
    - Create file: `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/dto/response/PrescriptionDetailResponse.java`
    - Add fields: id, prescriptionCode, status, issuedAt, patient, doctor, medications
    - Add nested classes: PatientInfo, DoctorInfo, MedicationItemResponse
    - Use Lombok annotations: @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
    - _Requirements: 2.2, 2.3, 2.4_
  
  - [ ]* 2.2 Write unit tests for PrescriptionDetailResponse DTO
    - Test builder pattern
    - Test serialization/deserialization
    - _Requirements: 2.2, 2.3, 2.4_

- [x] 3. Implement prescription retrieval endpoint
  - [x] 3.1 Add getPrescriptionByAppointment method to AppointmentController
    - Create endpoint: `GET /api/clinical/appointments/{appointmentId}/prescription`
    - Add @PreAuthorize("hasRole('PHARMACY')") annotation
    - Implement business logic: find consultation by appointmentId, find prescription by consultationId
    - Fetch patient details from PatientServiceClient
    - Fetch doctor details from DoctorRepository
    - Map to PrescriptionDetailResponse
    - Handle errors: 404 if not found, 503 if patient-service unavailable
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_
  
  - [ ]* 3.2 Write unit tests for getPrescriptionByAppointment
    - Test successful prescription retrieval
    - Test 404 when appointment not found
    - Test 404 when consultation not found
    - Test 404 when prescription not found
    - Test 503 when patient-service unavailable
    - Test role-based access control
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 4. Implement medication dispensing endpoint
  - [x] 4.1 Add dispenseMedication method to AppointmentController
    - Create endpoint: `PATCH /api/clinical/appointments/{id}/dispense-medication`
    - Add @PreAuthorize("hasRole('PHARMACY')") annotation
    - Implement business logic: find appointment, validate status == PHARMACY, call appointment.dispenseMedication(), save appointment
    - Handle errors: 404 if not found, 400 if invalid status
    - Return 200 OK on success
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8_
  
  - [ ]* 4.2 Write unit tests for dispenseMedication
    - Test successful dispensing when status is PHARMACY
    - Test 404 when appointment not found
    - Test 400 when status is not PHARMACY
    - Test role-based access control
    - Verify appointment status transitions to COMPLETED
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

- [x] 5. Checkpoint - Ensure backend tests pass
  - Ensure all backend unit tests pass, ask the user if questions arise.

- [x] 6. Create frontend TypeScript interfaces
  - [x] 6.1 Add PrescriptionDetailResponse interface to clinicalService.ts
    - Create interface matching backend DTO structure
    - Include nested interfaces: PatientInfo, DoctorInfo, MedicationItem
    - _Requirements: 2.2, 2.3, 2.4, 2.5_

- [x] 7. Extend frontend clinical service
  - [x] 7.1 Add getPrescriptionByAppointment function to clinicalService.ts
    - Create function: `getPrescriptionByAppointment(appointmentId: string): Promise<PrescriptionDetailResponse>`
    - Call GET /api/clinical/appointments/{appointmentId}/prescription
    - Handle errors and return typed response
    - _Requirements: 2.1, 6.1_
  
  - [x] 7.2 Add dispenseMedication function to clinicalService.ts
    - Create function: `dispenseMedication(appointmentId: string): Promise<void>`
    - Call PATCH /api/clinical/appointments/{appointmentId}/dispense-medication
    - Handle errors appropriately
    - _Requirements: 3.1, 3.2_

- [x] 8. Create PharmacyQueue component
  - [x] 8.1 Create PharmacyQueue.tsx component
    - Create file: `frontend-medflow/src/components/pharmacy/PharmacyQueue.tsx`
    - Accept props: appointments, loading, onSelect, onRefresh
    - Display table with columns: Patient Name, DPI, Appointment Date, Appointment Time, Prescription Code, Action
    - Sort appointments by date ascending, then time ascending
    - Show empty state: "No hay recetas pendientes de despacho"
    - Add refresh button
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  
  - [ ]* 8.2 Write unit tests for PharmacyQueue
    - Test rendering with appointments
    - Test empty state display
    - Test row selection callback
    - Test refresh button callback
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 9. Create PrescriptionDetail component
  - [x] 9.1 Create PrescriptionDetail.tsx component
    - Create file: `frontend-medflow/src/components/pharmacy/PrescriptionDetail.tsx`
    - Accept props: appointment, prescription, onDispense, onBack, dispensing
    - Display patient information card
    - Display prescription metadata card (code, doctor, date)
    - Display medications table with all fields
    - Add "Dispensar Medicamentos" button (enabled when status is PENDING)
    - Add "Volver a la cola" button
    - Show loading state during dispense operation
    - _Requirements: 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_
  
  - [ ]* 9.2 Write unit tests for PrescriptionDetail
    - Test rendering prescription details
    - Test medication list display
    - Test dispense button enabled/disabled states
    - Test back navigation callback
    - Test dispense callback
    - _Requirements: 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

- [x] 10. Create PharmacyPage main container
  - [x] 10.1 Create PharmacyPage.tsx component
    - Create file: `frontend-medflow/src/pages/pharmacy/PharmacyPage.tsx`
    - Manage state: appointments, selectedAppointment, prescriptionDetails, loading states, errors, view
    - Implement loadQueue(): fetch appointments with status PHARMACY
    - Implement selectAppointment(): load prescription details and switch to detail view
    - Implement dispenseMedication(): call dispense endpoint, show success message, refresh queue
    - Implement returnToQueue(): switch back to queue view
    - Use MainLayout component for consistent page structure
    - Display page title "Módulo de Farmacia" and subtitle "Gestiona el despacho de medicamentos a pacientes"
    - _Requirements: 1.1, 1.6, 2.1, 3.1, 3.2, 3.3, 3.4, 5.4, 5.5_
  
  - [ ]* 10.2 Write unit tests for PharmacyPage
    - Test queue loading on mount
    - Test appointment selection flow
    - Test dispense success flow
    - Test dispense error handling
    - Test navigation between views
    - Test refresh functionality
    - _Requirements: 1.1, 2.1, 3.1, 3.2, 3.3, 3.4_

- [x] 11. Implement error handling and loading states
  - [x] 11.1 Add error handling for queue loading failures
    - Display error message: "Error al cargar la cola de farmacia"
    - Provide "Reintentar" button
    - _Requirements: 7.3, 7.5_
  
  - [x] 11.2 Add error handling for prescription loading failures
    - Display error message: "Error al cargar los detalles de la receta"
    - Provide "Reintentar" and "Volver a la cola" buttons
    - Handle status-specific messages (404, 403, 500)
    - _Requirements: 7.4, 7.5_
  
  - [x] 11.3 Add error handling for dispensing failures
    - Display backend error message or generic message
    - Handle status-specific messages (400, 404, 403, 500)
    - Re-enable "Dispensar Medicamentos" button on error
    - _Requirements: 3.5, 3.6, 3.7_
  
  - [x] 11.4 Add loading states for all async operations
    - Show loading spinner with text "Cargando cola de farmacia..." while queue loads
    - Show loading spinner with text "Cargando detalles de la receta..." while prescription loads
    - Disable "Dispensar Medicamentos" button and show loading state during dispense operation
    - _Requirements: 7.1, 7.2, 7.6_

- [x] 12. Add routing and navigation
  - [x] 12.1 Add /pharmacy route to App.tsx
    - Import PharmacyPage component
    - Add route: `<Route path="/pharmacy" element={<PharmacyPage />} />`
    - _Requirements: 5.1_
  
  - [x] 12.2 Add "Farmacia" menu item to sidebar navigation
    - Add navigation link to /pharmacy
    - Label: "Farmacia"
    - Add appropriate icon
    - _Requirements: 5.2_
  
  - [x] 12.3 Add role-based access control for /pharmacy route
    - Check if user has PHARMACY role
    - Redirect to home page or show unauthorized message if user lacks role
    - _Requirements: 5.3_

- [x] 13. Implement data refresh and consistency
  - [x] 13.1 Add automatic queue refresh after successful dispense
    - Call loadQueue() after dispenseMedication() succeeds
    - _Requirements: 8.1_
  
  - [x] 13.2 Add manual refresh functionality
    - Implement refresh button in PharmacyQueue
    - Reload queue data when clicked
    - Preserve scroll position after refresh
    - _Requirements: 8.2, 8.3_
  
  - [x] 13.3 Add queue reload when returning from detail view
    - Call loadQueue() when returnToQueue() is invoked
    - _Requirements: 8.4_

- [x] 14. Add responsive design and accessibility
  - [x] 14.1 Make PharmacyQueue table responsive
    - Add horizontal scrolling for smaller screens
    - Test on screen widths from 768px to 1920px
    - _Requirements: 9.1, 9.2_
  
  - [x] 14.2 Add semantic HTML and ARIA labels
    - Use semantic table elements (table, thead, tbody, tr, th, td)
    - Add descriptive button labels and aria-labels
    - _Requirements: 9.3, 9.4_
  
  - [x] 14.3 Ensure accessibility compliance
    - Use sufficient color contrast ratios (WCAG AA)
    - Add keyboard navigation support for all interactive elements
    - _Requirements: 9.5, 9.6_

- [ ] 15. Checkpoint - Ensure all tests pass
  - Ensure all frontend and backend tests pass, ask the user if questions arise.

- [ ]* 16. Add integration tests for complete workflow
  - [ ]* 16.1 Write integration test for pharmacy queue flow
    - Create appointment with PHARMACY status
    - Call GET /appointments?queue=pharmacy
    - Verify appointment appears in response
    - _Requirements: 1.1_
  
  - [ ]* 16.2 Write integration test for prescription retrieval flow
    - Create appointment → consultation → prescription
    - Call GET /appointments/{id}/prescription
    - Verify prescription details returned correctly
    - _Requirements: 6.1, 6.2, 6.3_
  
  - [ ]* 16.3 Write integration test for dispensing flow
    - Create appointment with PHARMACY status
    - Call PATCH /appointments/{id}/dispense-medication
    - Verify appointment status changed to COMPLETED
    - Verify appointment no longer appears in pharmacy queue
    - _Requirements: 3.1, 3.2, 4.2, 4.3, 10.3_

- [x] 17. Final verification and cleanup
  - [x] 17.1 Verify appointment status flow integration
    - Test that appointments in PHARMACY status appear in queue
    - Test that dispensing transitions appointment to COMPLETED
    - Test that COMPLETED appointments do not appear in queue
    - _Requirements: 10.1, 10.2, 10.3_
  
  - [x] 17.2 Verify error messages match requirements
    - Check all error messages match Spanish text from requirements
    - Verify status codes match requirements (200, 400, 404, 503)
    - _Requirements: 3.5, 3.6, 3.7, 4.6, 4.7, 6.4, 7.3, 7.4_
  
  - [x] 17.3 Test complete user workflow end-to-end
    - Pharmacist logs in and navigates to /pharmacy
    - Views list of pending prescriptions
    - Clicks on a prescription and views details
    - Clicks "Dispensar Medicamentos"
    - Sees success message and returns to queue
    - Verifies prescription no longer in queue
    - _Requirements: All requirements_

- [x] 18. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Backend implementation comes first to enable frontend integration testing
- All error messages are in Spanish to match existing system language
- The module follows established patterns from Lab Sample Workflow and Cashier modules
- No database schema changes are required - all necessary tables and relationships exist
