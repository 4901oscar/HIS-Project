# Implementation Plan: Clinical Service

## Overview

This implementation plan follows TDD methodology (Red-Green-Refactor) to build the Clinical Service, the most complex microservice in MedFlow HIS. It uses Hexagonal Architecture (Ports and Adapters) due to heavy business logic including the Manchester Triage Algorithm and real-time appointment slot management with Redis.

**Architecture**: Hexagonal (Domain → Application → Infrastructure)
**Database**: PostgreSQL (clinical_schema)
**Cache**: Redis (appointment slots)
**Port**: 8083
**Estimated Time**: 16-20 hours

## Tasks

- [x] 1. Project setup and configuration
  - Create Maven project structure with Spring Boot 3.2.4 and Java 17
  - Add dependencies: Spring Web, Spring Data JPA, Spring Data Redis, PostgreSQL, Eureka Client, OpenFeign, Resilience4j, Validation, Lombok, Actuator, jqwik (PBT), TestContainers
  - Create application.yml with database, Redis, Eureka, Feign, Resilience4j configuration
  - Create application-docker.yml for Docker environment
  - Create BeanConfiguration, RedisConfiguration, FeignConfiguration, AsyncConfiguration
  - _Requirements: C1, C2, D1, D2_

- [x] 2. Domain model - Entities
  - [x] 2.1 Create PriorityLevel enum
    - Define RED(0), ORANGE(10), YELLOW(60), GREEN(120), BLUE(240) with maxWaitMinutes and description
    - _Requirements: Requirement 1_

  - [x] 2.2 Implement Triage domain entity
    - Fields: id, patientId, doctorId, motifId, discriminatorIds, priorityLevel, maxWaitTimeMinutes, performedAt, performedBy
    - _Requirements: Requirement 1_

  - [x] 2.3 Implement VitalSigns domain entity
    - Fields: id, patientId, systolicPressure, diastolicPressure, heartRate, respiratoryRate, temperature, oxygenSaturation, weight, height, bmi, recordedAt, recordedBy
    - Implement calculateBMI() business method
    - Implement isValid() validation method with physiological ranges
    - _Requirements: Requirement 2_

  - [x] 2.4 Implement Appointment domain entity
    - Fields: id, patientId, doctorId, appointmentDate, appointmentTime, status, notes, createdAt, createdBy
    - Create AppointmentStatus enum (SCHEDULED, ACTIVE, COMPLETED, CANCELLED)
    - Implement activate(), complete(), cancel() state transition methods with validation
    - _Requirements: Requirement 3_

  - [x] 2.5 Implement Consultation domain entity
    - Fields: id, patientId, doctorId, appointmentId, chiefComplaint, symptoms, primaryDiagnosis, secondaryDiagnoses, medicalNotes, treatmentPlan, consultationDate, performedBy
    - Implement addSecondaryDiagnosis() method
    - _Requirements: Requirement 4_

  - [x] 2.6 Implement Prescription domain entity
    - Fields: id, consultationId, patientId, doctorId, prescriptionCode, medications, status, issuedAt, issuedBy
    - Create PrescriptionStatus enum (PENDING, DISPENSED, CANCELLED)
    - Create Medication inner class (name, dosage, frequency, durationDays, route, specialInstructions)
    - _Requirements: Requirement 5_

  - [x] 2.7 Implement LabOrder domain entity
    - Fields: id, consultationId, patientId, doctorId, orderCode, testNames, status, orderedAt, orderedBy
    - Create LabOrderStatus enum (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
    - _Requirements: Requirement 6_

  - [x] 2.8 Implement ManchesterMotif and ManchesterDiscriminator domain entities
    - ManchesterMotif: id, code, description, category, active, discriminators
    - ManchesterDiscriminator: id, code, description, priorityLevel, active
    - _Requirements: Requirement 8_

- [x] 3. Domain ports (interfaces)
  - [x] 3.1 Create input ports (use case interfaces)
    - PerformTriageUseCase
    - RecordVitalSignsUseCase
    - ManageAppointmentUseCase (findAvailableSlots, createAppointment, activateAppointment, cancelAppointment)
    - RegisterConsultationUseCase
    - GeneratePrescriptionUseCase
    - GenerateLabOrderUseCase
    - GetMedicalHistoryUseCase
    - _Requirements: Requirements 1-7_

  - [x] 3.2 Create output ports (repository and client interfaces)
    - TriageRepository, VitalSignsRepository, AppointmentRepository
    - ConsultationRepository, PrescriptionRepository, LabOrderRepository
    - ManchesterCatalogRepository
    - PatientServiceClient, PharmacyServiceClient, LabServiceClient
    - AppointmentSlotCache
    - _Requirements: Requirements 1-10_

- [x] 4. Domain services (business logic - pure functions ideal for PBT)
  - [x] 4.1 Implement TriageEngine domain service
    - Inject VitalSignsRepository and ManchesterCatalogRepository (via ports)
    - Implement performTriage() method
    - Implement calculatePriorityLevel() - selects minimum wait time (maximum priority) from discriminators
    - Throw VitalSignsNotFoundException if no vital signs found
    - _Requirements: Requirement 1_

  - [x]* 4.2 Write property test: Manchester Algorithm Selects Maximum Priority
    - **Property 1: Manchester Algorithm Selects Maximum Priority**
    - **Validates: Requirements 1.3, 1.4**
    - Generate random combinations of discriminators with different priority levels
    - Verify calculated priority equals the minimum wait time among all discriminators
    - _Requirements: Requirement 1_

  - [x]* 4.3 Write property test: Manchester Algorithm is Deterministic
    - **Property 2: Manchester Algorithm is Deterministic**
    - **Validates: Requirements 1.8**
    - Execute triage 3 times with same discriminators, verify same priority level each time
    - _Requirements: Requirement 1_

  - [x] 4.4 Implement VitalSignsRecorder domain service
    - Implement recordVitalSigns() method
    - Call calculateBMI() on entity
    - Validate ranges using isValid()
    - Throw InvalidVitalSignsException if out of range
    - _Requirements: Requirement 2_

  - [x]* 4.5 Write property test: BMI Calculation is Correct
    - **Property 3: BMI Calculation is Correct**
    - **Validates: Requirements 2.3**
    - Generate random valid weight/height pairs, verify BMI = weight / (height_m)^2
    - _Requirements: Requirement 2_

  - [x]* 4.6 Write property test: Vital Signs Validation Respects Physiological Ranges
    - **Property 4: Vital Signs Validation Respects Physiological Ranges**
    - **Validates: Requirements 2.4**
    - Generate random values, verify isValid() returns true iff all values in range
    - _Requirements: Requirement 2_

  - [x] 4.7 Implement AppointmentManager domain service
    - Implement generateDailySlots() - 18 slots from 08:00 to 16:30 (30 min each)
    - Implement findAvailableSlots() - query Redis, filter occupied slots
    - Implement createAppointment() - validate patient, reserve slot atomically in Redis, save to DB, rollback Redis on DB failure
    - Implement cancelAppointment() - cancel entity, release slot in Redis
    - _Requirements: Requirement 3_

  - [x]* 4.8 Write property test: Daily Slots Generation Produces Exactly 18 Slots
    - **Property 6: Daily Slots Generation Produces Exactly 18 Slots**
    - **Validates: Requirements 3.2**
    - Verify generateDailySlots() always returns 18 slots, first=08:00, last=16:30, 30 min apart
    - _Requirements: Requirement 3_

  - [x]* 4.9 Write property test: Appointment State Transitions are Valid
    - **Property 8: Appointment State Transitions are Valid**
    - **Validates: Requirements 3.6, 3.7**
    - Test all valid transitions (SCHEDULED→ACTIVE, ACTIVE→COMPLETED, SCHEDULED→CANCELLED, ACTIVE→CANCELLED)
    - Test all invalid transitions throw IllegalStateException
    - _Requirements: Requirement 3_

  - [x] 4.10 Implement ConsultationManager domain service
    - Implement registerConsultation() method
    - Complete associated appointment if appointmentId provided
    - _Requirements: Requirement 4_

  - [x] 4.11 Implement PrescriptionGenerator domain service
    - Implement generatePrescription() method
    - Implement generateUniqueCode() - 8-char alphanumeric using SecureRandom
    - Notify PharmacyServiceClient asynchronously (catch and log failures, don't throw)
    - _Requirements: Requirement 5_

  - [x]* 4.12 Write property test: Prescription Codes are Unique and Well-Formed
    - **Property 11: Prescription Codes are Unique and Well-Formed**
    - **Validates: Requirements 5.3, 5.8**
    - Generate multiple prescriptions, verify all codes are 8 chars, match [A-Z0-9]{8}, all unique
    - _Requirements: Requirement 5_

  - [x]* 4.13 Write property test: Prescription Notification Failures Do Not Fail Transaction
    - **Property 12: Prescription Notification Failures Do Not Fail Transaction**
    - **Validates: Requirements 5.7**
    - Mock PharmacyServiceClient to throw exception, verify prescription is still saved
    - _Requirements: Requirement 5_

  - [x] 4.14 Implement LabOrderGenerator domain service
    - Implement generateLabOrder() method
    - Implement generateUniqueCode() - 8-char alphanumeric using SecureRandom
    - Notify LabServiceClient asynchronously (catch and log failures, don't throw)
    - _Requirements: Requirement 6_

  - [x]* 4.15 Write property test: Lab Order Codes are Unique and Well-Formed
    - **Property 13: Lab Order Codes are Unique and Well-Formed**
    - **Validates: Requirements 6.3, 6.8**
    - Generate multiple lab orders, verify all codes are 8 chars, match [A-Z0-9]{8}, all unique
    - _Requirements: Requirement 6_

  - [x]* 4.16 Write property test: Lab Order Notification Failures Do Not Fail Transaction
    - **Property 14: Lab Order Notification Failures Do Not Fail Transaction**
    - **Validates: Requirements 6.7**
    - Mock LabServiceClient to throw exception, verify lab order is still saved
    - _Requirements: Requirement 6_

  - [x] 4.17 Implement MedicalHistoryAggregator domain service
    - Implement getMedicalHistory() method
    - Validate PATIENT role can only access own history (throw UnauthorizedException otherwise)
    - Call PatientServiceClient.getPatient() for demographics
    - Aggregate consultations, vital signs, prescriptions, lab orders ordered by date DESC
    - _Requirements: Requirement 7_

  - [x]* 4.18 Write property test: Patients Can Only Access Their Own Medical History
    - **Property 15: Patients Can Only Access Their Own Medical History**
    - **Validates: Requirements 7.6**
    - Generate random patientId/userId pairs, verify PATIENT role only succeeds when IDs match
    - _Requirements: Requirement 7_

  - [x]* 4.19 Write property test: Consultations Ordered by Date Descending
    - **Property 16: Consultations in Medical History are Ordered by Date Descending**
    - **Validates: Requirements 7.2**
    - Save multiple consultations with random dates, verify history returns them DESC
    - _Requirements: Requirement 7_

- [x] 5. Checkpoint - Ensure all domain tests pass
  - Run all domain service tests, ensure they pass before proceeding to application layer.

- [x] 6. Application layer - Use case implementations
  - [x] 6.1 Implement PermissionValidator application service
    - Extract X-User-Roles from HttpServletRequest header
    - Implement requireRole(String... allowedRoles) - throw ForbiddenException if not authorized
    - Implement getUserId() and getUserRoles() helpers
    - _Requirements: Requirement 9_

  - [x] 6.2 Implement PerformTriageUseCaseImpl
    - Validate DOCTOR role via PermissionValidator
    - Delegate to TriageEngine
    - Persist via TriageRepository
    - _Requirements: Requirement 1, 9_

  - [x] 6.3 Implement RecordVitalSignsUseCaseImpl
    - Validate VITAL_SIGNS or DOCTOR role
    - Delegate to VitalSignsRecorder
    - _Requirements: Requirement 2, 9_

  - [x] 6.4 Implement ManageAppointmentUseCaseImpl
    - Validate ADMISSION or ADMIN role for all appointment operations
    - Delegate to AppointmentManager
    - _Requirements: Requirement 3, 9_

  - [x] 6.5 Implement RegisterConsultationUseCaseImpl
    - Validate DOCTOR role
    - Delegate to ConsultationManager
    - _Requirements: Requirement 4, 9_

  - [x] 6.6 Implement GeneratePrescriptionUseCaseImpl
    - Validate DOCTOR role
    - Delegate to PrescriptionGenerator
    - _Requirements: Requirement 5, 9_

  - [x] 6.7 Implement GenerateLabOrderUseCaseImpl
    - Validate DOCTOR role
    - Delegate to LabOrderGenerator
    - _Requirements: Requirement 6, 9_

  - [x] 6.8 Implement GetMedicalHistoryUseCaseImpl
    - Validate DOCTOR or PATIENT role
    - Delegate to MedicalHistoryAggregator
    - _Requirements: Requirement 7, 9_

  - [ ]* 6.9 Write unit tests for use case implementations
    - Test each use case with valid role → success
    - Test each use case with invalid role → ForbiddenException
    - Use Mockito to mock domain services and repositories
    - _Requirements: Requirement 9_

- [x] 7. Infrastructure - Persistence adapter (JPA)
  - [x] 7.1 Create JPA entities
    - TriageEntity, VitalSignsEntity, AppointmentEntity, ConsultationEntity
    - PrescriptionEntity, LabOrderEntity
    - ManchesterMotifEntity, ManchesterDiscriminatorEntity
    - Add proper @Table(schema = "clinical_schema") annotations
    - Add indexes on patient_id, doctor_id+date, prescription_code, order_code
    - Create StringListConverter and MedicationListConverter for JSON columns
    - _Requirements: Requirements 1-8_

  - [x] 7.2 Create JPA Spring Data repositories
    - JpaTriageRepository, JpaVitalSignsRepository, JpaAppointmentRepository
    - JpaConsultationRepository, JpaPrescriptionRepository, JpaLabOrderRepository
    - JpaManchesterMotifRepository, JpaManchesterDiscriminatorRepository
    - Add custom query methods (findByPatientId, findFirstByPatientIdOrderByRecordedAtDesc, etc.)
    - _Requirements: Requirements 1-8_

  - [x] 7.3 Create repository adapter implementations
    - Implement all output port interfaces (TriageRepository, VitalSignsRepository, etc.)
    - Create mapper classes (TriageMapper, VitalSignsMapper, etc.) for domain ↔ JPA entity conversion
    - _Requirements: Requirements 1-8_

  - [ ]* 7.4 Write repository integration tests
    - Test VitalSignsRepositoryAdapter.findLatestByPatientId() returns most recent
    - Test AppointmentRepositoryAdapter with date filtering
    - Test PrescriptionRepositoryAdapter.existsByCode() uniqueness check
    - Use @DataJpaTest with TestContainers PostgreSQL
    - _Requirements: Requirements 1-8_

- [x] 8. Infrastructure - Redis cache adapter
  - [x] 8.1 Implement RedisAppointmentSlotCache
    - Implement AppointmentSlotCache output port
    - Key pattern: appointment:slots:{doctorId}:{date}
    - Implement getOccupiedSlots() - return Set<LocalTime> from Redis SET
    - Implement reserveSlot() - atomic SADD, return true if added (false if already exists)
    - Implement releaseSlot() - SREM from Redis SET
    - Set TTL of 7 days on keys
    - _Requirements: Requirement 3_

  - [ ]* 8.2 Write property test: Appointment Slot Reservation is Atomic
    - **Property 7: Appointment Slot Reservation is Atomic**
    - **Validates: Requirements 3.3**
    - On successful creation: verify slot in Redis AND appointment in DB
    - On failed creation: verify slot NOT in Redis
    - _Requirements: Requirement 3_

  - [ ]* 8.3 Write property test: Appointment Cancellation Maintains Consistency
    - **Property 9: Appointment Cancellation Maintains Consistency**
    - **Validates: Requirements 3.8**
    - Cancel appointment, verify slot released in Redis AND status=CANCELLED in DB
    - _Requirements: Requirement 3_

- [x] 9. Infrastructure - HTTP client adapters (Feign)
  - [x] 9.1 Implement PatientServiceClientAdapter
    - Create PatientServiceFeignClient Feign interface (GET /api/patients/{id})
    - Implement PatientServiceClient output port
    - Add @CircuitBreaker and @Retry annotations (Resilience4j)
    - Implement fallback method returning ServiceUnavailableException
    - Propagate JWT via FeignConfiguration RequestInterceptor
    - _Requirements: Requirement 10_

  - [x] 9.2 Implement PharmacyServiceClientAdapter
    - Create PharmacyServiceFeignClient Feign interface (POST /api/pharmacy/prescriptions/notify)
    - Implement PharmacyServiceClient output port with @Async
    - Catch all exceptions and log (eventual consistency - never throw)
    - _Requirements: Requirement 5_

  - [x] 9.3 Implement LabServiceClientAdapter
    - Create LabServiceFeignClient Feign interface (POST /api/lab/orders/notify)
    - Implement LabServiceClient output port with @Async
    - Catch all exceptions and log (eventual consistency - never throw)
    - _Requirements: Requirement 6_

  - [ ]* 9.4 Write integration tests for HTTP clients with WireMock
    - Test PatientServiceClientAdapter.getPatient() with 200 response
    - Test PatientServiceClientAdapter.getPatient() with 404 → PatientNotFoundException
    - Test PatientServiceClientAdapter circuit breaker opens after failures
    - Test PharmacyServiceClientAdapter swallows exceptions
    - _Requirements: Requirements 5, 6, 10_S

- [x] 10. Infrastructure - REST adapter (Controllers and DTOs)
  - [x] 10.1 Create request DTOs with validation annotations
    - TriageRequest (@NotBlank patientId, motifId; @NotEmpty discriminatorIds)
    - VitalSignsRequest (all fields with @Min/@Max range validations, messages in Spanish)
    - CreateAppointmentRequest (@NotBlank patientId, doctorId; @NotNull @Future date; @NotNull time)
    - ConsultationRequest (@NotBlank chiefComplaint, primaryDiagnosis with CIE-10 @Pattern)
    - PrescriptionRequest with nested MedicationRequest (@Valid)
    - LabOrderRequest (@NotEmpty testNames)
    - _Requirements: Requirements 1-6, 12_

  - [x] 10.2 Create response DTOs
    - TriageResponse, VitalSignsResponse, AvailableSlotsResponse, AppointmentResponse
    - ConsultationResponse, PrescriptionResponse, LabOrderResponse, MedicalHistoryResponse
    - ErrorResponse (status, message, errors list)
    - _Requirements: Requirements 1-7_

  - [x] 10.3 Implement TriageController
    - POST /api/clinical/triage → 200 OK
    - Extract X-User-Id header, map DTO to use case call
    - _Requirements: Requirement 1_

  - [x] 10.4 Implement VitalSignsController
    - POST /api/clinical/vital-signs → 200 OK
    - _Requirements: Requirement 2_

  - [x] 10.5 Implement AppointmentController
    - GET /api/clinical/appointments/slots?doctorId=&date= → 200 OK
    - POST /api/clinical/appointments → 201 CREATED
    - PUT /api/clinical/appointments/{id}/activate → 200 OK
    - DELETE /api/clinical/appointments/{id} → 204 NO CONTENT
    - _Requirements: Requirement 3_

  - [x] 10.6 Implement ConsultationController
    - POST /api/clinical/consultations → 201 CREATED
    - _Requirements: Requirement 4_

  - [x] 10.7 Implement PrescriptionController
    - POST /api/clinical/prescriptions → 201 CREATED
    - _Requirements: Requirement 5_

  - [x] 10.8 Implement LabOrderController
    - POST /api/clinical/lab-orders → 201 CREATED
    - _Requirements: Requirement 6_

  - [x] 10.9 Implement MedicalHistoryController
    - GET /api/clinical/history/{patientId} → 200 OK
    - Extract X-User-Id and X-User-Roles headers
    - _Requirements: Requirement 7_

  - [x] 10.10 Implement GlobalExceptionHandler
    - Handle MethodArgumentNotValidException → 400 with field details in Spanish
    - Handle VitalSignsNotFoundException, PatientNotFoundException → 404
    - Handle InvalidVitalSignsException, InvalidDiscriminatorsException → 400
    - Handle SlotNotAvailableException → 409
    - Handle UnauthorizedException → 401
    - Handle ForbiddenException → 403
    - Handle ServiceUnavailableException → 503
    - Handle generic Exception → 500 (log stack trace, return generic Spanish message)
    - _Requirements: Requirement 12_

  - [ ]* 10.11 Write controller integration tests with MockMvc
    - Test POST /api/clinical/triage with valid request → 200
    - Test POST /api/clinical/triage without DOCTOR role → 403
    - Test POST /api/clinical/vital-signs with out-of-range values → 400
    - Test POST /api/clinical/appointments with occupied slot → 409
    - Test GET /api/clinical/history/{patientId} as PATIENT for own record → 200
    - Test GET /api/clinical/history/{patientId} as PATIENT for other record → 401
    - _Requirements: Requirements 1-7, 9, 12_

- [x] 11. Checkpoint - Ensure all tests pass
  - Run all tests (domain, application, infrastructure), ensure they pass before proceeding.

- [x] 12. Database schema and migration
  - [x] 12.1 Create Flyway migration script V1__create_clinical_schema.sql
    - CREATE SCHEMA IF NOT EXISTS clinical_schema
    - CREATE TABLE triages, vital_signs, appointments, consultations
    - CREATE TABLE prescriptions, lab_orders
    - CREATE TABLE manchester_motifs, manchester_discriminators
    - Add all indexes (patient_id, doctor_id+date, prescription_code, order_code)
    - Add UNIQUE constraints on prescription_code and order_code
    - _Requirements: Requirements 1-8_

  - [x] 12.2 Create seed data migration V2__seed_manchester_catalog.sql
    - Insert sample Manchester motifs (chest pain, dyspnea, trauma, etc.)
    - Insert discriminators with priority levels for each motif
    - _Requirements: Requirement 8_

- [~] 13. Property test: JSON Round-Trip Serialization
  - [ ]* 13.1 Write property test: JSON Serialization Round-Trip Preserves Data
    - **Property 17: JSON Serialization Round-Trip Preserves Data**
    - **Validates: Requirements 11.6**
    - For each domain object type (Triage, VitalSigns, Appointment, Consultation, Prescription, LabOrder)
    - Serialize to JSON with ObjectMapper, deserialize back, verify equals original
    - _Requirements: Requirement 11_

- [x] 14. Application main class and configuration
  - [x] 14.1 Create ClinicalServiceApplication main class
    - @SpringBootApplication with @EnableDiscoveryClient and @EnableFeignClients
    - _Requirements: C2, D2_

  - [x] 14.2 Verify application.yml configuration
    - server.port=8083, spring.application.name=clinical-service
    - PostgreSQL datasource with clinical_schema
    - Redis connection (host, port, timeout, pool)
    - Eureka client configuration
    - Feign client URLs (patient-service, pharmacy-service, lab-service)
    - Resilience4j circuit breaker and retry for patientService
    - Actuator endpoints (health, info, metrics, prometheus)
    - _Requirements: C1, C2, D1, D2_

- [x] 15. Docker and deployment
  - [x] 15.1 Create Dockerfile
    - Use eclipse-temurin:17-jre-alpine base image
    - Copy JAR to /app/app.jar, expose port 8083
    - Set SPRING_PROFILES_ACTIVE=docker
    - _Requirements: C1_

  - [x] 15.2 Update docker-compose.yml
    - Add clinical-service with environment variables (DB, Redis, Eureka, service URLs)
    - depends_on: postgres, redis, eureka-server, patient-service
    - Map port 8083:8083
    - _Requirements: D1, D2, D3_

  - [x] 15.3 Create database initialization script
    - Create init-clinical-schema.sql (idempotent, IF NOT EXISTS)
    - _Requirements: C3_

- [x] 16. Documentation
  - [x] 16.1 Create README.md
    - Project overview, hexagonal architecture description
    - Prerequisites, build and run instructions (local and Docker)
    - API endpoints documentation with examples
    - Configuration guide (environment variables)
    - Testing instructions
    - _Requirements: Overall acceptance criteria_

- [~] 17. Final integration testing
  - [ ]* 17.1 Write end-to-end integration tests with TestContainers
    - Test complete triage flow: record vital signs → perform triage
    - Test appointment flow: find slots → create → activate → complete
    - Test consultation flow: create consultation → generate prescription → generate lab order
    - Test medical history aggregation
    - Use TestContainers for PostgreSQL and Redis
    - _Requirements: Overall acceptance criteria_

  - [ ]* 17.2 Test Eureka registration
    - Verify service registers as CLINICAL-SERVICE in Eureka
    - Verify /actuator/health returns UP
    - _Requirements: C2, D2_

- [x] 18. Final checkpoint - Ensure all tests pass
  - Run mvn clean test to ensure all unit, integration, and property tests pass
  - Run mvn clean install to build the JAR
  - Test Docker build: docker build -t clinical-service .
  - Verify service starts and registers with Eureka
  - Verify health check: curl http://localhost:8083/actuator/health
  - Ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Follow TDD: Write test first (Red), implement (Green), refactor (Refactor)
- Hexagonal architecture: Domain layer has ZERO Spring annotations - pure Java
- CERO JOINs between schemas - use HTTP calls to Patient Service
- Redis slot reservation must be atomic (SADD returns 0 if already exists)
- Pharmacy and Lab notifications are fire-and-forget (eventual consistency)
- Circuit breaker protects against Patient Service failures
- All error messages must be in Spanish
- Estimated time: 16-20 hours

## Testing Strategy

### Property-Based Tests (PBT) - 17 Properties
- Use jqwik library for property-based testing
- Minimum 100 iterations per property (@PropertyDefaults(tries = 100))
- Focus on pure domain functions: TriageEngine, VitalSigns, AppointmentManager, code generators

### Unit Tests
- Domain services tested in isolation with Mockito
- Use case implementations tested with mocked ports
- Target >= 80% coverage on domain and application layers

### Integration Tests
- Repository adapters with @DataJpaTest + TestContainers PostgreSQL
- Redis cache adapter with TestContainers Redis
- HTTP clients with WireMock
- Controllers with MockMvc (@SpringBootTest)

### End-to-End Tests
- Complete clinical flows with real PostgreSQL and Redis (TestContainers)
- Integration with Eureka and API Gateway
