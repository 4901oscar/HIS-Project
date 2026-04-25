# Implementation Plan: Triage-Appointment Integration

## Overview

This implementation plan breaks down the integration of the Triage and Appointment systems into discrete coding tasks. The integration adds a unidirectional relationship where Triage references Appointment via `appointmentId`, enabling triage staff to view active appointments awaiting triage while maintaining domain boundaries and query performance.

The implementation follows a bottom-up approach: database schema → domain model → repository layer → application layer → REST layer → testing. Each phase builds on the previous one, ensuring incremental validation and integration.

## Tasks

### Phase 1: Database Migration

- [x] 1. Create database migration for appointment_id column
  - Create file `backend-services/clinical-service/src/main/resources/db/migration/V6__add_appointment_id_to_triage.sql`
  - Add `appointment_id VARCHAR(255)` column to `clinical_schema.triages` table
  - Create UNIQUE index `idx_triages_appointment_unique` on `appointment_id`
  - Create regular BTREE index `idx_triages_appointment` for query optimization
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 2. Checkpoint - Verify migration executes successfully
  - Run migration on local development database
  - Verify `appointment_id` column exists in `triages` table
  - Verify both indexes are created
  - Ensure all tests pass, ask the user if questions arise

### Phase 2: Domain Model Updates

- [x] 3. Add appointmentId field to Triage domain model
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/domain/model/Triage.java`
  - Add `private String appointmentId` field
  - Add getter `getAppointmentId()` and setter `setAppointmentId(String)`
  - Update constructor to accept `appointmentId` parameter
  - _Requirements: 1.1, 1.2_

- [x] 4. Update TriageEntity JPA entity
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/persistence/entity/TriageEntity.java`
  - Add `@Column(name = "appointment_id")` field
  - Update `toDomain()` method to map `appointmentId`
  - Update `fromDomain()` method to map `appointmentId`
  - _Requirements: 1.1_

- [ ]* 5. Write unit tests for Triage domain model
  - Test `appointmentId` getter/setter
  - Test constructor with `appointmentId`
  - Test domain-to-entity mapping includes `appointmentId`
  - _Requirements: 1.1_

### Phase 3: Repository Layer

- [x] 6. Add findByAppointmentId method to TriageRepository interface
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/domain/port/out/TriageRepository.java`
  - Add method signature `Optional<Triage> findByAppointmentId(String appointmentId)`
  - _Requirements: 3.1_

- [x] 7. Implement findByAppointmentId in TriageRepositoryAdapter
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/persistence/adapter/TriageRepositoryAdapter.java`
  - Add JPA query method `Optional<TriageEntity> findByAppointmentId(String appointmentId)` to JPA repository
  - Implement adapter method that calls JPA repository and maps to domain
  - _Requirements: 3.1, 3.2, 3.3_

- [ ]* 8. Write property test for findByAppointmentId
  - **Property 4: Query by Appointment Round-Trip**
  - **Validates: Requirements 3.1, 3.2, 3.3**
  - Generate random valid triage records with appointmentIds
  - Create triage and verify findByAppointmentId returns the same record
  - Verify querying multiple times returns identical results (idempotence)

- [x] 9. Add findPendingTriage method to AppointmentRepository interface
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/domain/port/out/AppointmentRepository.java`
  - Add method signature `List<Appointment> findPendingTriage()`
  - _Requirements: 4.1_

- [x] 10. Implement findPendingTriage with NOT EXISTS query
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/persistence/adapter/AppointmentRepositoryAdapter.java`
  - Create `@Query` with NOT EXISTS subquery: `SELECT a FROM AppointmentEntity a WHERE a.status = 'ACTIVE' AND NOT EXISTS (SELECT 1 FROM TriageEntity t WHERE t.appointmentId = a.id)`
  - Implement adapter method that maps results to domain
  - _Requirements: 4.1, 4.2, 4.5, 4.6_

- [ ]* 11. Write property test for findPendingTriage correctness
  - **Property 2: Pending Triage Query Correctness**
  - **Validates: Requirements 4.1, 4.2, 4.5, 4.6**
  - Generate random sets of appointments (various statuses) and triage records
  - Compute expected result: ACTIVE appointments without triage
  - Verify findPendingTriage returns exactly the expected set
  - Verify no appointment in result has an associated triage record

- [x] 12. Checkpoint - Verify repository layer works correctly
  - Run all repository tests
  - Verify findByAppointmentId returns correct results
  - Verify findPendingTriage returns only ACTIVE appointments without triage
  - Ensure all tests pass, ask the user if questions arise

### Phase 4: Application Layer - Validation Logic

- [x] 13. Create DuplicateTriageException
  - Create file `backend-services/clinical-service/src/main/java/com/medframe/clinical/domain/exception/DuplicateTriageException.java`
  - Extend `RuntimeException`
  - Add constructor accepting error message
  - _Requirements: 2.3_

- [x] 14. Add appointment validation to TriageEngine
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/domain/service/TriageEngine.java`
  - Add `AppointmentRepository` dependency via constructor injection
  - Update `performTriage()` method signature to accept `appointmentId` as first parameter
  - Add validation: appointment exists (throw `AppointmentNotFoundException` if not)
  - Add validation: appointment status is ACTIVE (throw `IllegalStateException` if not)
  - Add validation: no duplicate triage exists (throw `DuplicateTriageException` if exists)
  - Set `appointmentId` on created Triage object
  - _Requirements: 1.3, 1.4, 1.5, 1.6, 2.2, 2.3, 5.1_

- [ ]* 15. Write property test for appointment validation
  - **Property 5: Appointment Status Validation**
  - **Validates: Requirements 1.4, 1.6**
  - Generate triage creation requests with appointments in various statuses (SCHEDULED, CANCELLED, COMPLETED, MISSED, ACTIVE)
  - Verify only ACTIVE appointments allow triage creation
  - Verify non-ACTIVE appointments fail with "Appointment must be in ACTIVE status"
  - Verify no Triage record exists for non-ACTIVE appointments

- [ ]* 16. Write property test for duplicate prevention
  - **Property 1: Appointment-Triage Uniqueness**
  - **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
  - Generate random sets of triage creation requests with duplicate appointmentIds
  - Verify only the first creation succeeds
  - Verify subsequent attempts fail with "Triage already exists for this appointment"
  - Verify querying by appointmentId always returns 0 or 1 results, never more

- [ ]* 17. Write property test for validation completeness
  - **Property 3: Triage Creation Validation Completeness**
  - **Validates: Requirements 1.3, 1.4, 1.5, 1.6, 2.2, 7.1, 7.2, 7.3**
  - Generate invalid triage creation requests (non-existent appointmentId, wrong status, duplicates, missing vital signs, invalid discriminators)
  - Verify each invalid request fails with appropriate error message
  - Verify no Triage record is persisted for failed requests
  - Verify database state remains consistent after failed attempts

- [ ]* 18. Write property test for existing validation preservation
  - **Property 8: Existing Validation Preservation**
  - **Validates: Requirements 7.1, 7.2, 7.3, 7.4**
  - Generate random triage creation requests with appointmentId
  - Verify vital signs validation still applies (fails without vital signs)
  - Verify discriminator validation still applies (fails with invalid discriminators)
  - Verify priority calculation still uses Manchester algorithm
  - Compare behavior with pre-integration triage creation

### Phase 5: Application Layer - Use Cases

- [x] 19. Update PerformTriageUseCase to accept appointmentId
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/application/port/in/PerformTriageUseCase.java`
  - Update method signature to accept `appointmentId` as first parameter
  - _Requirements: 5.1, 5.2_

- [x] 20. Update PerformTriageUseCaseImpl implementation
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/application/usecase/PerformTriageUseCaseImpl.java`
  - Update `performTriage()` to pass `appointmentId` to `TriageEngine`
  - Add exception handling for `DuplicateTriageException` (409 Conflict)
  - Add exception handling for `AppointmentNotFoundException` (404 Not Found)
  - Add exception handling for `IllegalStateException` for appointment status (400 Bad Request)
  - _Requirements: 1.5, 1.6, 2.3, 5.5_

- [x] 21. Create GetAppointmentTriageUseCase interface
  - Create file `backend-services/clinical-service/src/main/java/com/medframe/clinical/application/port/in/GetAppointmentTriageUseCase.java`
  - Add method `Optional<Triage> getAppointmentTriage(String appointmentId)`
  - _Requirements: 3.1_

- [x] 22. Implement GetAppointmentTriageUseCaseImpl
  - Create file `backend-services/clinical-service/src/main/java/com/medframe/clinical/application/usecase/GetAppointmentTriageUseCaseImpl.java`
  - Inject `TriageRepository` dependency
  - Implement method that calls `triageRepository.findByAppointmentId(appointmentId)`
  - _Requirements: 3.1, 3.2, 3.3_

- [x] 23. Create ListPendingTriageAppointmentsUseCase interface
  - Create file `backend-services/clinical-service/src/main/java/com/medframe/clinical/application/port/in/ListPendingTriageAppointmentsUseCase.java`
  - Add method `List<Appointment> listPendingTriageAppointments()`
  - _Requirements: 4.1_

- [x] 24. Implement ListPendingTriageAppointmentsUseCaseImpl
  - Create file `backend-services/clinical-service/src/main/java/com/medframe/clinical/application/usecase/ListPendingTriageAppointmentsUseCaseImpl.java`
  - Inject `AppointmentRepository` dependency
  - Implement method that calls `appointmentRepository.findPendingTriage()`
  - _Requirements: 4.1, 4.3, 4.4_

- [x] 25. Checkpoint - Verify application layer works correctly
  - Run all use case tests
  - Verify PerformTriageUseCase validates appointmentId correctly
  - Verify GetAppointmentTriageUseCase returns correct triage
  - Verify ListPendingTriageAppointmentsUseCase returns correct appointments
  - Ensure all tests pass, ask the user if questions arise

### Phase 6: REST Layer - DTOs and Controllers

- [x] 26. Update TriageRequest DTO to include appointmentId
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/dto/request/TriageRequest.java`
  - Add `@NotBlank(message = "appointmentId es requerido")` field `private String appointmentId`
  - Add getter and setter for `appointmentId`
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 27. Update TriageController.performTriage endpoint
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/controller/TriageController.java`
  - Extract `appointmentId` from `TriageRequest`
  - Pass `appointmentId` as first parameter to `performTriageUseCase.performTriage()`
  - _Requirements: 5.4, 5.5_

- [x] 28. Add GET /appointments/{id}/triage endpoint to AppointmentController
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/controller/AppointmentController.java`
  - Inject `GetAppointmentTriageUseCase` dependency
  - Add `@GetMapping("/{id}/triage")` method
  - Call `getAppointmentTriageUseCase.getAppointmentTriage(id)`
  - Return 200 with TriageResponse if found
  - Return 404 with error message "No triage found for this appointment" if not found
  - _Requirements: 3.4, 3.5, 3.6_

- [x] 29. Add GET /appointments/pending-triage endpoint to AppointmentController
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/controller/AppointmentController.java`
  - Inject `ListPendingTriageAppointmentsUseCase` dependency
  - Add `@GetMapping("/pending-triage")` method
  - Call `listPendingTriageAppointmentsUseCase.listPendingTriageAppointments()`
  - Map results to `List<AppointmentResponse>`
  - Return 200 with array of appointments (empty array if none)
  - _Requirements: 4.3, 4.4_

- [x] 30. Add exception handler for DuplicateTriageException
  - Modify `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/exception/GlobalExceptionHandler.java`
  - Add `@ExceptionHandler(DuplicateTriageException.class)` method
  - Return HTTP 409 Conflict with error message
  - Follow existing error response format (timestamp, status, error, message, path)
  - _Requirements: 2.3, 10.3, 10.4_

- [ ]* 31. Write integration tests for POST /triage with appointmentId
  - Test successful triage creation with valid appointmentId
  - Test 400 error when appointmentId is missing
  - Test 404 error when appointment not found
  - Test 400 error when appointment not ACTIVE
  - Test 409 error when triage already exists for appointment
  - Verify response format matches existing triage response
  - _Requirements: 5.3, 5.4, 5.5, 10.2, 10.3, 10.4_

- [ ]* 32. Write integration tests for GET /appointments/{id}/triage
  - Test 200 response when triage exists for appointment
  - Test 404 response when no triage exists for appointment
  - Verify response format matches TriageResponse
  - _Requirements: 3.4, 3.5, 3.6, 10.2, 10.3, 10.4_

- [ ]* 33. Write integration tests for GET /appointments/pending-triage
  - Test 200 response with array of pending appointments
  - Test empty array when no pending appointments
  - Verify only ACTIVE appointments without triage are returned
  - Verify response format matches AppointmentResponse array
  - _Requirements: 4.3, 4.4, 10.1, 10.3, 10.4_

### Phase 7: Performance Testing and Optimization

- [x] 34. Write performance test for findByAppointmentId query
  - Create database with 100,000 triage records
  - Execute findByAppointmentId 100 times with random appointmentIds
  - Measure execution time for each query
  - Verify 95th percentile is below 50ms
  - Verify query plan uses the appointmentId index
  - _Requirements: 9.2, 9.4_

- [x] 35. Write performance test for findPendingTriage query
  - Create database with 10,000 appointments (mix of statuses and triage associations)
  - Execute findPendingTriage 50 times
  - Measure execution time for each query
  - Verify 95th percentile is below 500ms
  - Verify query uses NOT EXISTS pattern (check query plan)
  - _Requirements: 9.1, 9.3_

- [x] 36. Checkpoint - Verify performance meets requirements
  - Review performance test results
  - Verify all queries meet performance targets
  - Verify indexes are being used correctly
  - Ensure all tests pass, ask the user if questions arise

### Phase 8: Documentation and Final Integration

- [x] 37. Update API documentation
  - Update Swagger/OpenAPI annotations for modified POST /triage endpoint
  - Add Swagger/OpenAPI annotations for GET /appointments/{id}/triage
  - Add Swagger/OpenAPI annotations for GET /appointments/pending-triage
  - Document all request/response formats and error codes
  - _Requirements: 10.1, 10.2, 10.3, 10.4_

- [-] 38. Update README and deployment documentation
  - Document new endpoints in clinical-service README
  - Add migration instructions for V6 migration
  - Document rollback procedure
  - Add monitoring and alerting recommendations
  - _Requirements: 6.5, 6.6_

- [x] 39. Final checkpoint - End-to-end verification
  - Run full test suite (unit + integration + property tests)
  - Verify all endpoints work correctly via Postman/curl
  - Verify database migration executes successfully
  - Test rollback procedure
  - Ensure all tests pass, ask the user if questions arise

## Notes

- **Property-Based Tests:** Tasks marked with `*` include property-based tests that validate universal correctness properties. These are optional for MVP but highly recommended for production quality.

- **Incremental Validation:** Checkpoint tasks ensure each phase is validated before moving to the next. This catches errors early and maintains system stability.

- **Requirements Traceability:** Each task explicitly references the requirements it implements, ensuring complete coverage.

- **Java/Spring Boot Stack:** All implementation uses Java with Spring Boot, JPA, and existing clinical-service patterns.

- **Performance Guarantees:** Phase 7 validates that query performance meets the specified requirements (50ms for findByAppointmentId, 500ms for findPendingTriage).

- **Backward Compatibility:** The migration initially allows nullable `appointment_id` to support existing triage records. A future migration can enforce NOT NULL after data migration.

- **Error Handling:** All validation errors follow REST conventions with appropriate HTTP status codes (400, 404, 409) and consistent error response format.
