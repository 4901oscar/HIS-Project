# Implementation Plan: Doctor Auto-Assignment System

## Overview

This implementation plan breaks down the Doctor Auto-Assignment System into discrete, executable coding tasks. The system extends the existing Clinical Service microservice with automatic doctor assignment based on workload balancing, shift schedules, and capacity constraints.

**Implementation Language**: Java (Spring Boot)

**Architecture Pattern**: Hexagonal Architecture (Domain-Driven Design)

**Key Integration Points**:
- Extends existing `AppointmentManager` and `RedisAppointmentSlotCache`
- Integrates with PostgreSQL for persistence
- Uses Redis for atomic slot reservation
- Maintains backward compatibility with manual doctor selection

## Tasks

### Phase 1: Domain Model and Core Logic

- [x] 1. Create Doctor and DoctorAvailability domain entities
  - [x] 1.1 Implement Doctor entity with shift validation
    - Create `Doctor.java` in domain model package
    - Add fields: id, name, specialty, shiftStart, shiftEnd, status, createdAt
    - Implement `validateShift()` method to enforce 8-hour constraint
    - Implement `worksAt(LocalTime)` method to check if doctor works at specific time
    - Implement `deactivate()` method with status validation
    - Add DoctorStatus enum (ACTIVE, INACTIVE)
    - _Requirements: 1.1, 1.2, 1.4_
  
  - [ ]* 1.2 Write property test for Doctor entity
    - **Property 1: Doctor Record Round-Trip Preservation**
    - **Property 2: Shift Duration Validation**
    - **Validates: Requirements 1.1, 1.2**
    - Generate random valid doctors with jqwik
    - Test round-trip persistence (save and retrieve)
    - Test shift validation accepts only 8-hour shifts
    - Test shift validation rejects non-8-hour shifts
  
  - [x] 1.3 Implement DoctorAvailability entity
    - Create `DoctorAvailability.java` in domain model package
    - Add fields: id, doctorId, date, isAvailable, reason, createdAt, createdBy
    - Implement `markUnavailable(String reason)` method
    - Implement `restore()` method
    - _Requirements: 2.1_
  
  - [ ]* 1.4 Write property test for DoctorAvailability entity
    - **Property 4: Day-Off Record Persistence**
    - **Validates: Requirements 2.1**
    - Test round-trip persistence for day-off records
    - Test markUnavailable and restore methods

- [x] 2. Implement DoctorAssignmentService with workload balancing algorithm
  - [x] 2.1 Create DoctorAssignmentService domain service
    - Create `DoctorAssignmentService.java` in domain services package
    - Inject DoctorRepository, DoctorAvailabilityRepository, AppointmentRepository
    - Implement `assignDoctor(LocalDate date, LocalTime time)` method
    - Implement `findDoctorsWorkingAt(LocalTime)` helper method
    - Implement `filterAvailableDoctors(List<Doctor>, LocalDate)` helper method
    - Implement `calculateWorkload(List<Doctor>, LocalDate)` helper method
    - Implement `selectLeastLoadedDoctor(List<Doctor>, Map<String, Integer>)` helper method
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.6, 4.7_
  
  - [ ]* 2.2 Write property tests for assignment algorithm
    - **Property 3: Inactive Doctor Exclusion from Assignment**
    - **Property 5: Day-Off Exclusion from Assignment**
    - **Property 12: Shift-Based Doctor Filtering**
    - **Property 13: Workload Counting Accuracy**
    - **Property 14: Least-Loaded Doctor Selection**
    - **Property 15: Alphabetical Tiebreaker**
    - **Validates: Requirements 1.4, 2.2, 4.1, 4.2, 4.3, 4.4, 4.6, 4.7**
    - Generate random doctor pools with varying workloads
    - Test that inactive doctors are never assigned
    - Test that doctors with day-offs are never assigned
    - Test that only doctors working at requested time are considered
    - Test that assigned doctor has minimum workload
    - Test alphabetical tiebreaker when workloads are equal
  
  - [ ]* 2.3 Write unit tests for assignment edge cases
    - Test NoAvailableDoctorException when no doctors work at requested time
    - Test NoAvailableDoctorException when all doctors have day-offs
    - Test assignment with single available doctor
    - Test assignment with all doctors having equal workload

- [x] 3. Implement ShiftManager domain service
  - [x] 3.1 Create ShiftManager domain service
    - Create `ShiftManager.java` in domain services package
    - Inject DoctorRepository and DoctorAvailabilityRepository
    - Implement `createDoctor(String name, String specialty, LocalTime shiftStart, LocalTime shiftEnd)` method
    - Implement `markDayOff(String doctorId, LocalDate date, String reason)` method
    - Implement `removeDayOff(String doctorId, LocalDate date)` method
    - Add validation for doctor existence in markDayOff
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.5_
  
  - [ ]* 3.2 Write property tests for ShiftManager
    - **Property 6: Vacation Period Bulk Creation**
    - **Property 7: Day-Off Removal Restoration**
    - **Validates: Requirements 2.4, 2.5**
    - Test bulk day-off creation for date ranges
    - Test that removing day-off restores availability
    - Test DoctorNotFoundException for invalid doctor IDs

- [x] 4. Implement CapacityController domain service
  - [x] 4.1 Create CapacityController domain service
    - Create `CapacityController.java` in domain services package
    - Inject DoctorRepository, DoctorAvailabilityRepository, AppointmentSlotCache
    - Define constants: SLOTS_PER_HOUR_PER_DOCTOR = 2, SLOT_DURATION_MINUTES = 30
    - Implement `findAvailableSlots(LocalDate date)` method
    - Implement `generateAllSlots()` helper method (48 slots from 00:00 to 23:30)
    - Implement `hasCapacity(LocalTime slot, LocalDate date)` helper method
    - _Requirements: 3.1, 3.3, 3.4, 3.5, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_
  
  - [ ]* 4.2 Write property tests for CapacityController
    - **Property 8: Hourly Capacity Constraint**
    - **Property 9: Capacity Calculation with Availability**
    - **Property 10: Occupied Slot Counting Accuracy**
    - **Property 11: Full Slot Exclusion**
    - **Property 17: Daily Slot Generation Completeness**
    - **Property 18: Shift Identification Correctness**
    - **Validates: Requirements 2.3, 3.1, 3.3, 3.4, 3.5, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6**
    - Test that no doctor exceeds 2 appointments per hour
    - Test capacity calculation with day-offs
    - Test occupied slot counting across multiple doctors
    - Test that full slots are excluded from available slots
    - Test that exactly 48 slots are generated
    - Test shift identification for various time slots
  
  - [ ]* 4.3 Write unit tests for capacity edge cases
    - Test capacity when all doctors have day-offs
    - Test capacity with single doctor in shift
    - Test capacity at shift boundaries (00:00, 08:00, 16:00, 24:00)

- [x] 5. Checkpoint - Domain layer validation
  - Ensure all domain tests pass
  - Verify domain entities have no infrastructure dependencies
  - Ask the user if questions arise

### Phase 2: Infrastructure Layer

- [x] 6. Create database schema and migrations
  - [x] 6.1 Create Flyway migration for doctors table
    - Create migration file `V{version}__create_doctors_table.sql`
    - Define doctors table with columns: id, name, specialty, shift_start, shift_end, status, created_at
    - Add CHECK constraint for status (ACTIVE, INACTIVE)
    - Add CHECK constraint for shift duration (8 hours)
    - Create index on (shift_start, shift_end, status)
    - Create index on (status)
    - _Requirements: 9.1_
  
  - [x] 6.2 Create Flyway migration for doctor_availability table
    - Create migration file `V{version}__create_doctor_availability_table.sql`
    - Define doctor_availability table with columns: id, doctor_id, date, is_available, reason, created_at, created_by
    - Add foreign key constraint to doctors table
    - Add unique constraint on (doctor_id, date)
    - Create index on (doctor_id, date)
    - Create index on (date)
    - _Requirements: 9.2, 9.3_
  
  - [x] 6.3 Create Flyway migration for appointments table index
    - Create migration file `V{version}__add_appointments_doctor_date_index.sql`
    - Create composite index on appointments(doctor_id, appointment_date)
    - _Requirements: 9.4, 9.5_

- [x] 7. Implement JPA repositories
  - [x] 7.1 Create DoctorRepository interface and implementation
    - Create `DoctorRepository.java` interface in domain ports package
    - Define methods: save, findById, findByShiftAndStatus, findAllActive
    - Create `JpaDoctorRepository.java` in infrastructure package
    - Implement Spring Data JPA repository
    - Add custom query methods for shift-based filtering
    - _Requirements: 1.1, 1.3, 4.1_
  
  - [x] 7.2 Create DoctorAvailabilityRepository interface and implementation
    - Create `DoctorAvailabilityRepository.java` interface in domain ports package
    - Define methods: save, findByDoctorIdAndDate, deleteByDoctorIdAndDate
    - Create `JpaDoctorAvailabilityRepository.java` in infrastructure package
    - Implement Spring Data JPA repository
    - _Requirements: 2.1, 2.5_
  
  - [ ]* 7.3 Write integration tests for repositories with Testcontainers
    - **Property 1: Doctor Record Round-Trip Preservation** (infrastructure validation)
    - **Property 4: Day-Off Record Persistence** (infrastructure validation)
    - Set up PostgreSQL Testcontainer
    - Test doctor CRUD operations
    - Test day-off CRUD operations
    - Test findByShiftAndStatus query
    - Test findByDoctorIdAndDate query
    - Test foreign key constraints
    - Test unique constraints

- [x] 8. Extend Redis cache integration
  - [x] 8.1 Review existing RedisAppointmentSlotCache implementation
    - Read `RedisAppointmentSlotCache.java` to understand current implementation
    - Verify reserveSlot and releaseSlot methods exist
    - Verify getOccupiedSlots method exists
    - Document any needed extensions
    - _Requirements: 10.1, 10.3, 10.4, 10.5_
  
  - [x] 8.2 Add capacity aggregation methods if needed
    - If not present, add `getOccupiedSlotsForMultipleDoctors(List<String> doctorIds, LocalDate date)` method
    - Implement using Redis pipelining for performance
    - _Requirements: 10.2_
  
  - [ ]* 8.3 Write integration tests for Redis cache with Testcontainers
    - **Property 24: Slot Count Aggregation Accuracy**
    - **Property 25: Slot Reservation Release Round-Trip**
    - **Validates: Requirements 10.2, 10.4**
    - Set up Redis Testcontainer
    - Test slot reservation atomicity
    - Test slot release
    - Test occupied slot counting across multiple doctors
    - Test round-trip reservation and release

- [x] 9. Checkpoint - Infrastructure layer validation
  - Ensure all infrastructure tests pass
  - Verify database migrations run successfully
  - Verify Redis operations are atomic
  - Ask the user if questions arise

### Phase 3: Application and API Layer

- [x] 10. Create application use cases
  - [x] 10.1 Create DoctorUseCase for doctor management
    - Create `DoctorUseCase.java` in application layer
    - Inject ShiftManager
    - Implement `createDoctor(CreateDoctorCommand)` method
    - Implement `updateDoctor(String id, UpdateDoctorCommand)` method
    - Implement `deactivateDoctor(String id)` method
    - Implement `listActiveDoctors()` method
    - Implement `markDaysOff(String doctorId, LocalDate startDate, LocalDate endDate, String reason)` method
    - Implement `removeDayOff(String doctorId, LocalDate date)` method
    - Create command DTOs: CreateDoctorCommand, UpdateDoctorCommand
    - _Requirements: 1.1, 1.3, 1.4, 2.1, 2.4, 2.5_
  
  - [x] 10.2 Modify AppointmentUseCase to support automatic assignment
    - Read existing `AppointmentUseCase.java`
    - Inject DoctorAssignmentService
    - Modify `createAppointment` method to make doctorId optional
    - Add logic to invoke DoctorAssignmentService when doctorId is null
    - Ensure backward compatibility when doctorId is provided
    - _Requirements: 4.5, 7.6_
  
  - [x] 10.3 Modify AppointmentManager to integrate assignment service
    - Read existing `AppointmentManager.java`
    - Inject DoctorAssignmentService
    - Modify `createAppointment` method signature to accept optional doctorId
    - Add automatic assignment logic before slot reservation
    - Maintain existing slot reservation and rollback logic
    - _Requirements: 4.5, 7.6_
  
  - [ ]* 10.4 Write unit tests for use cases
    - **Property 16: Doctor ID Persistence in Appointment**
    - **Property 21: Automatic Assignment Invocation**
    - **Validates: Requirements 4.5, 7.6**
    - Mock domain services
    - Test createDoctor with valid and invalid shifts
    - Test markDaysOff for date ranges
    - Test createAppointment with and without doctorId
    - Test that automatic assignment is invoked when doctorId is null
    - Test that assigned doctorId is persisted in appointment

- [x] 11. Create REST API controllers
  - [x] 11.1 Create DoctorController with CRUD endpoints
    - Create `DoctorController.java` in API layer
    - Inject DoctorUseCase
    - Implement POST /api/clinical/doctors endpoint
    - Implement PUT /api/clinical/doctors/{id} endpoint
    - Implement DELETE /api/clinical/doctors/{id} endpoint (soft delete)
    - Implement GET /api/clinical/doctors endpoint
    - Implement POST /api/clinical/doctors/{id}/days-off endpoint
    - Implement DELETE /api/clinical/doctors/{id}/days-off/{date} endpoint
    - Add @PreAuthorize annotations for ADMINISTRATOR role
    - Create request/response DTOs
    - Add validation annotations (@Valid, @NotNull, etc.)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_
  
  - [x] 11.2 Modify AppointmentController to support automatic assignment
    - Read existing `AppointmentController.java`
    - Modify POST /api/clinical/appointments endpoint
    - Make doctorId parameter optional in request DTO
    - Update response DTO to include doctor name and specialty
    - Add logic to fetch doctor details for response
    - Ensure backward compatibility with manual doctor selection
    - _Requirements: 7.5, 7.6, 8.1, 11.7_
  
  - [x] 11.3 Modify slot query endpoint to be capacity-aware
    - Modify GET /api/clinical/appointments/slots endpoint
    - Remove doctorId query parameter
    - Inject CapacityController
    - Call findAvailableSlots(date) instead of doctor-specific query
    - Update response DTO to include availableCapacity and totalCapacity per slot
    - Add time filtering logic for current date
    - _Requirements: 5.1, 5.6, 6.1, 6.2, 6.4, 6.5, 11.6_
  
  - [ ]* 11.4 Write API integration tests
    - **Property 19: Future Slot Time Filtering**
    - **Property 22: Appointment Response Completeness**
    - **Property 23: Appointment History Data Completeness**
    - **Validates: Requirements 6.2, 6.4, 6.5, 8.1, 8.4, 8.5**
    - Use @SpringBootTest with MockMvc
    - Test POST /api/clinical/doctors with valid and invalid data
    - Test PUT /api/clinical/doctors/{id}
    - Test DELETE /api/clinical/doctors/{id}
    - Test GET /api/clinical/doctors
    - Test POST /api/clinical/doctors/{id}/days-off
    - Test DELETE /api/clinical/doctors/{id}/days-off/{date}
    - Test POST /api/clinical/appointments without doctorId
    - Test POST /api/clinical/appointments with doctorId (backward compatibility)
    - Test GET /api/clinical/appointments/slots without doctorId parameter
    - Test time filtering for current date vs future dates
    - Test that appointment response includes doctorId, doctorName, doctorSpecialty
    - Test error responses (400, 404, 422, 409)

- [x] 12. Implement error handling and validation
  - [x] 12.1 Create domain exception classes
    - Create `DoctorNotFoundException.java`
    - Create `InvalidShiftDurationException.java`
    - Create `NoAvailableDoctorException.java`
    - Create `SlotNotAvailableException.java`
    - All exceptions extend RuntimeException
    - _Requirements: Error Handling_
  
  - [x] 12.2 Create global exception handler for API layer
    - Create or modify `GlobalExceptionHandler.java`
    - Add @ExceptionHandler for DoctorNotFoundException → 404
    - Add @ExceptionHandler for InvalidShiftDurationException → 400
    - Add @ExceptionHandler for NoAvailableDoctorException → 422
    - Add @ExceptionHandler for SlotNotAvailableException → 409
    - Add @ExceptionHandler for generic exceptions → 500
    - Return standardized error response format
    - _Requirements: Error Handling_
  
  - [ ]* 12.3 Write unit tests for exception handling
    - Test each exception handler returns correct HTTP status
    - Test error response format includes timestamp, status, error, message, path
    - Test that internal error details are not exposed in 500 responses

- [x] 13. Checkpoint - Application and API layer validation
  - Ensure all API tests pass
  - Test end-to-end flow: create doctor → mark day-off → query slots → create appointment
  - Verify automatic assignment works correctly
  - Verify backward compatibility with manual doctor selection
  - Ask the user if questions arise

### Phase 4: Frontend Integration

- [x] 14. Modify appointment booking form
  - [x] 14.1 Remove doctor selection dropdown from booking form
    - Locate appointment booking form component (React/Angular/Vue)
    - Remove doctor selection field from form
    - Update form validation to remove doctorId requirement
    - Update form submission to not include doctorId in request
    - _Requirements: 7.1, 7.5_
  
  - [x] 14.2 Update slot query to use capacity-aware endpoint
    - Modify slot query API call to remove doctorId parameter
    - Update slot display to show availableCapacity and totalCapacity
    - Add visual indicator for slots with limited capacity
    - _Requirements: 5.6, 7.2, 7.3_
  
  - [x] 14.3 Implement time filtering for current date
    - Add logic to filter out past time slots when date is today
    - Use client-side time to determine current slot
    - Combine with backend capacity filtering
    - _Requirements: 6.1, 6.3, 6.5_
  
  - [x] 14.4 Update appointment confirmation to show assigned doctor
    - Modify confirmation component to display doctor name and specialty
    - Extract doctor information from API response
    - Display message: "Cita confirmada con Dr. [Name] - [Specialty]"
    - _Requirements: 8.1, 8.2, 8.3_
  
  - [x] 14.5 Update appointment history to show doctor information
    - Modify appointment list component to display doctor name and specialty
    - Ensure doctor information is fetched with appointment data
    - Display appointment status alongside doctor information
    - _Requirements: 8.4, 8.5_

- [x] 15. Create doctor management UI (Administrator)
  - [x] 15.1 Create doctor list view
    - Create component to display all active doctors
    - Show doctor name, specialty, shift times, status
    - Add search and filter functionality
    - Add "Create Doctor" button
    - Restrict access to ADMINISTRATOR role
    - _Requirements: 11.5_
  
  - [x] 15.2 Create doctor form (create/edit)
    - Create form component with fields: name, specialty, shiftStart, shiftEnd
    - Add validation for shift times (HH:mm format)
    - Add validation for 8-hour shift duration
    - Display validation errors inline
    - Support both create and edit modes
    - _Requirements: 1.2, 11.1, 11.2_
  
  - [x] 15.3 Implement doctor deactivation
    - Add "Deactivate" button to doctor list
    - Show confirmation dialog before deactivation
    - Call DELETE /api/clinical/doctors/{id} endpoint
    - Update list after successful deactivation
    - _Requirements: 1.4, 11.3_

- [x] 16. Create day-off management UI (Administrator)
  - [x] 16.1 Create day-off calendar view
    - Create calendar component showing doctor availability
    - Display day-off records on calendar
    - Color-code days: available (green), day-off (red)
    - Allow selection of doctor to view their calendar
    - _Requirements: 2.1, 2.2_
  
  - [x] 16.2 Implement day-off creation
    - Add "Mark Day Off" button to calendar
    - Create form with fields: startDate, endDate, reason
    - Support single-day and multi-day selection
    - Call POST /api/clinical/doctors/{id}/days-off endpoint
    - Update calendar after successful creation
    - _Requirements: 2.1, 2.4, 11.4_
  
  - [x] 16.3 Implement day-off removal
    - Add "Remove Day Off" action to calendar day-off entries
    - Show confirmation dialog before removal
    - Call DELETE /api/clinical/doctors/{id}/days-off/{date} endpoint
    - Update calendar after successful removal
    - _Requirements: 2.5, 11.4_

- [ ]* 17. End-to-end testing
  - Set up E2E testing framework (Cypress, Playwright, or Selenium)
  - Test complete appointment booking flow without doctor selection
  - Test slot availability updates when doctors have day-offs
  - Test doctor management CRUD operations
  - Test day-off management operations
  - Test time filtering for current date
  - Test appointment confirmation shows assigned doctor
  - Test appointment history shows doctor information
  - Test role-based access control for administrator features

- [ ] 18. Checkpoint - Frontend integration validation
  - Ensure all frontend components render correctly
  - Test user flows manually
  - Verify responsive design on mobile devices
  - Ask the user if questions arise

### Phase 5: Configuration Parser (Optional Enhancement)

- [ ] 19. Implement JSON configuration parser
  - [ ] 19.1 Create DoctorConfigurationParser
    - Create `DoctorConfigurationParser.java` in infrastructure layer
    - Implement `parse(String json)` method using Jackson
    - Validate JSON schema (name, specialty, shiftStart, shiftEnd)
    - Validate shift times are in HH:mm format
    - Validate shift duration is 8 hours
    - Validate doctor IDs are unique within configuration
    - Return descriptive validation errors for invalid configurations
    - _Requirements: 12.1, 12.2, 12.5, 12.6_
  
  - [ ] 19.2 Create DoctorConfigurationPrinter
    - Create `DoctorConfigurationPrinter.java` in infrastructure layer
    - Implement `serialize(List<Doctor>)` method using Jackson
    - Format output as valid JSON with proper indentation
    - Include all doctor fields in output
    - _Requirements: 12.3_
  
  - [ ]* 19.3 Write property tests for parser and serializer
    - **Property 26: JSON Configuration Parsing Validity**
    - **Property 27: Doctor Object Serialization Validity**
    - **Property 28: Configuration Round-Trip Preservation**
    - **Property 29: Shift Time Format Validation**
    - **Property 30: Doctor ID Uniqueness Validation**
    - **Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5, 12.6**
    - Generate random valid JSON configurations
    - Test parsing valid configurations produces Doctor objects
    - Test parsing invalid configurations returns errors (not exceptions)
    - Test serializing Doctor objects produces valid JSON
    - Test round-trip: parse(serialize(doctor)) equals original
    - Test shift time format validation
    - Test doctor ID uniqueness validation
  
  - [ ] 19.4 Create bulk import endpoint
    - Add POST /api/clinical/doctors/import endpoint to DoctorController
    - Accept JSON configuration file in request body
    - Parse configuration using DoctorConfigurationParser
    - Create all doctors in configuration using ShiftManager
    - Return summary: total doctors created, any errors
    - Add @PreAuthorize for ADMINISTRATOR role
    - _Requirements: 12.1, 12.2_
  
  - [ ] 19.5 Create export endpoint
    - Add GET /api/clinical/doctors/export endpoint to DoctorController
    - Query all active doctors
    - Serialize using DoctorConfigurationPrinter
    - Return JSON configuration file
    - Set Content-Disposition header for file download
    - Add @PreAuthorize for ADMINISTRATOR role
    - _Requirements: 12.3_
  
  - [ ]* 19.6 Write integration tests for import/export endpoints
    - Test POST /api/clinical/doctors/import with valid configuration
    - Test POST /api/clinical/doctors/import with invalid configuration
    - Test GET /api/clinical/doctors/export returns valid JSON
    - Test round-trip: export → import produces equivalent doctors

- [ ] 20. Final checkpoint - Complete system validation
  - Run all tests (unit, integration, property-based, E2E)
  - Verify all 30 correctness properties pass
  - Test complete user flows end-to-end
  - Verify performance meets requirements
  - Verify security controls are in place
  - Ask the user if questions arise

## Notes

- **Tasks marked with `*` are optional** and can be skipped for faster MVP delivery
- **Each task references specific requirements** for traceability back to requirements document
- **Checkpoints ensure incremental validation** at major phase boundaries
- **Property tests validate universal correctness properties** defined in design document
- **Unit tests validate specific examples and edge cases** not covered by properties
- **Integration tests validate infrastructure and API layers** with real dependencies
- **The implementation follows hexagonal architecture** with clear separation of concerns
- **Backward compatibility is maintained** for manual doctor selection throughout

## Testing Configuration

**Property-Based Testing Library**: jqwik 1.7.4

**Integration Testing**: Spring Boot Test + Testcontainers (PostgreSQL + Redis)

**API Testing**: MockMvc + RestAssured

**E2E Testing**: Cypress or Playwright (optional)

**Minimum Property Test Iterations**: 100 per property

**Test Coverage Target**: 80% overall, 100% for domain layer

## Implementation Order Rationale

1. **Phase 1 (Domain)**: Establishes core business logic with no external dependencies
2. **Phase 2 (Infrastructure)**: Implements persistence and caching adapters
3. **Phase 3 (Application/API)**: Orchestrates domain services and exposes REST endpoints
4. **Phase 4 (Frontend)**: Integrates UI with backend APIs
5. **Phase 5 (Parser)**: Optional enhancement for bulk operations

This order ensures each layer can be tested independently before integration.
