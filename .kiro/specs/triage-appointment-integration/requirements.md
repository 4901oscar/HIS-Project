# Requirements Document

## Introduction

This document specifies the requirements for integrating the Triage system with the Appointments system in the clinical-service. The integration enables triage staff to view active appointments awaiting triage and associates triage records with specific appointments, creating a complete patient flow from appointment booking through triage completion.

The integration follows a unidirectional relationship where Triage references Appointment via `appointmentId`, without modifying the Appointment entity. This design decision optimizes query performance using NOT EXISTS patterns while maintaining clear domain boundaries.

## Glossary

- **Triage_System**: The Manchester Triage System implementation that assesses patient priority based on vital signs and discriminators
- **Appointment_System**: The appointment management system that handles patient appointment scheduling, activation, and lifecycle
- **Triage_Record**: A single triage assessment performed for a patient, containing priority level, discriminators, and vital signs reference
- **Active_Appointment**: An appointment in ACTIVE status that has been activated via QR scan and is ready for triage
- **Pending_Triage_Appointment**: An active appointment that does not yet have an associated triage record
- **Triage_Staff**: Healthcare personnel authorized to perform triage assessments (doctors, nurses)
- **Clinical_Service**: The backend microservice that manages clinical operations including appointments and triage
- **Appointment_Repository**: Data access layer for appointment persistence and queries
- **Triage_Repository**: Data access layer for triage persistence and queries

## Requirements

### Requirement 1: Link Triage to Appointment

**User Story:** As a triage staff member, I want each triage record to be linked to a specific appointment, so that I can track which appointments have completed triage and maintain a complete patient flow record.

#### Acceptance Criteria

1. THE Triage_Record SHALL contain an appointmentId field that references an Appointment
2. WHEN creating a Triage_Record, THE Triage_System SHALL require a valid appointmentId
3. WHEN creating a Triage_Record, THE Triage_System SHALL validate that the referenced Appointment exists
4. WHEN creating a Triage_Record, THE Triage_System SHALL validate that the referenced Appointment is in ACTIVE status
5. IF an appointmentId does not reference an existing Appointment, THEN THE Triage_System SHALL reject the triage creation with error "Appointment not found"
6. IF an appointmentId references an Appointment that is not in ACTIVE status, THEN THE Triage_System SHALL reject the triage creation with error "Appointment must be in ACTIVE status for triage"

### Requirement 2: Prevent Duplicate Triage per Appointment

**User Story:** As a system administrator, I want to ensure each appointment can only have one triage record, so that we maintain data integrity and prevent duplicate assessments.

#### Acceptance Criteria

1. THE Triage_System SHALL enforce a unique constraint on the appointmentId field
2. WHEN creating a Triage_Record, THE Triage_System SHALL verify that no existing Triage_Record exists for the given appointmentId
3. IF a Triage_Record already exists for an appointmentId, THEN THE Triage_System SHALL reject the creation with error "Triage already exists for this appointment"
4. FOR ALL valid Triage_Records, querying by appointmentId SHALL return at most one result

### Requirement 3: Query Triage by Appointment

**User Story:** As a client application, I want to retrieve the triage record associated with a specific appointment, so that I can display triage results to patients and staff.

#### Acceptance Criteria

1. THE Triage_Repository SHALL provide a method findByAppointmentId that accepts an appointmentId and returns the associated Triage_Record
2. WHEN querying for a Triage_Record by appointmentId, THE Triage_Repository SHALL return the Triage_Record if it exists
3. WHEN querying for a Triage_Record by appointmentId that has no associated triage, THE Triage_Repository SHALL return empty result
4. THE Clinical_Service SHALL expose endpoint GET /api/clinical/appointments/{id}/triage that returns the triage for a specific appointment
5. WHEN calling GET /api/clinical/appointments/{id}/triage for an appointment with triage, THE Clinical_Service SHALL return HTTP 200 with the triage data
6. WHEN calling GET /api/clinical/appointments/{id}/triage for an appointment without triage, THE Clinical_Service SHALL return HTTP 404 with message "No triage found for this appointment"

### Requirement 4: List Appointments Pending Triage

**User Story:** As a triage staff member, I want to see a list of all active appointments that are waiting for triage, so that I can prioritize and process patients efficiently.

#### Acceptance Criteria

1. THE Appointment_Repository SHALL provide a method findPendingTriage that returns all Active_Appointments without associated Triage_Records
2. WHEN querying for Pending_Triage_Appointments, THE Appointment_Repository SHALL use a NOT EXISTS subquery to identify appointments without triage
3. THE Clinical_Service SHALL expose endpoint GET /api/clinical/appointments/pending-triage that returns the list of pending triage appointments
4. WHEN calling GET /api/clinical/appointments/pending-triage, THE Clinical_Service SHALL return HTTP 200 with an array of appointments in ACTIVE status that have no triage
5. FOR ALL appointments returned by findPendingTriage, the appointment status SHALL be ACTIVE
6. FOR ALL appointments returned by findPendingTriage, no Triage_Record SHALL exist with matching appointmentId

### Requirement 5: Accept Appointment ID in Triage Creation

**User Story:** As a triage staff member, I want to provide the appointment ID when creating a triage record, so that the system can link the triage to the correct appointment.

#### Acceptance Criteria

1. THE Triage_System SHALL accept appointmentId as a required parameter in the triage creation request
2. WHEN receiving a triage creation request, THE Triage_System SHALL validate that appointmentId is not null or empty
3. IF appointmentId is null or empty, THEN THE Triage_System SHALL reject the request with HTTP 400 and error "appointmentId is required"
4. THE Clinical_Service SHALL modify endpoint POST /api/clinical/triage to accept appointmentId in the request body
5. WHEN creating a Triage_Record, THE Triage_System SHALL store the appointmentId in the Triage_Record

### Requirement 6: Database Schema Migration

**User Story:** As a database administrator, I want the database schema to support the triage-appointment relationship, so that the system can persist and query the integration correctly.

#### Acceptance Criteria

1. THE Clinical_Service SHALL create a database migration that adds column appointment_id to the triage table
2. THE appointment_id column SHALL be of type VARCHAR(255) and SHALL NOT allow null values
3. THE Clinical_Service SHALL create a unique index on triage(appointment_id) to enforce one-triage-per-appointment constraint
4. THE Clinical_Service SHALL create a non-unique index on triage(appointment_id) to optimize findByAppointmentId queries
5. THE database migration SHALL execute successfully on PostgreSQL database
6. WHEN the migration completes, THE triage table SHALL contain the appointment_id column with the unique constraint

### Requirement 7: Maintain Existing Triage Validations

**User Story:** As a system administrator, I want all existing triage validations to remain in effect, so that the integration does not compromise existing business rules.

#### Acceptance Criteria

1. WHEN creating a Triage_Record, THE Triage_System SHALL continue to validate that the patient has vital signs recorded
2. WHEN creating a Triage_Record, THE Triage_System SHALL continue to validate that at least one valid discriminator is provided
3. WHEN creating a Triage_Record, THE Triage_System SHALL continue to calculate priority level using the Manchester algorithm
4. FOR ALL Triage_Records created after integration, all existing validation rules SHALL apply in addition to appointment validations

### Requirement 8: Preserve Appointment Immutability

**User Story:** As a system architect, I want the Appointment entity to remain unchanged by this integration, so that we maintain separation of concerns and avoid circular dependencies.

#### Acceptance Criteria

1. THE Appointment_System SHALL NOT add a triageId field to the Appointment entity
2. THE Appointment_System SHALL NOT modify the Appointment domain model to reference Triage
3. THE Appointment_System SHALL NOT add triage-related business logic to the Appointment entity
4. FOR ALL Appointment operations, the Appointment entity structure SHALL remain identical to pre-integration state

### Requirement 9: Query Performance Optimization

**User Story:** As a system administrator, I want triage-appointment queries to execute efficiently, so that the system can handle high patient volumes without performance degradation.

#### Acceptance Criteria

1. THE Appointment_Repository findPendingTriage query SHALL complete in less than 500ms for databases with up to 10,000 appointments
2. THE Triage_Repository findByAppointmentId query SHALL complete in less than 50ms using the appointmentId index
3. WHEN executing findPendingTriage, THE Appointment_Repository SHALL use a NOT EXISTS subquery rather than a LEFT JOIN to optimize performance
4. THE database SHALL maintain an index on triage(appointment_id) to support O(log n) lookup performance

### Requirement 10: API Response Format Consistency

**User Story:** As a frontend developer, I want the new endpoints to follow the same response format as existing clinical-service endpoints, so that I can integrate them consistently.

#### Acceptance Criteria

1. WHEN GET /api/clinical/appointments/pending-triage returns results, THE response SHALL be a JSON array of appointment objects
2. WHEN GET /api/clinical/appointments/{id}/triage returns a triage, THE response SHALL be a JSON object with triage fields matching the existing POST /api/clinical/triage response format
3. WHEN an error occurs, THE Clinical_Service SHALL return a JSON error response with fields: timestamp, status, error, message, path
4. FOR ALL new endpoints, HTTP status codes SHALL follow REST conventions: 200 for success, 404 for not found, 400 for validation errors, 500 for server errors

## Correctness Properties

### Property 1: Appointment-Triage Uniqueness (Invariant)

**Property:** For any appointmentId, there exists at most one Triage_Record with that appointmentId.

**Test Strategy:** Property-based test
- Generate random sets of triage creation requests with duplicate appointmentIds
- Verify that only the first creation succeeds and subsequent attempts fail with "Triage already exists"
- Verify that querying by appointmentId always returns 0 or 1 results, never more

**Rationale:** This is a critical invariant that must hold across all operations. Property-based testing with random inputs will verify the constraint under various scenarios.

### Property 2: Pending Triage Query Correctness (Metamorphic)

**Property:** The set of appointments returned by findPendingTriage SHALL equal the set of ACTIVE appointments minus the set of appointments with triage records.

**Test Strategy:** Property-based test
- Generate random sets of appointments (various statuses) and triage records
- Compute expected result: ACTIVE appointments without triage
- Verify findPendingTriage returns exactly the expected set
- Verify no appointment in the result has an associated triage record

**Rationale:** This metamorphic property verifies the query logic by comparing it against a simple set operation. Property-based testing ensures correctness across various data distributions.

### Property 3: Triage Creation Validation Completeness (Error Conditions)

**Property:** Triage creation SHALL fail for all invalid inputs: non-existent appointmentId, non-ACTIVE appointment, duplicate appointmentId, missing vital signs, invalid discriminators.

**Test Strategy:** Property-based test
- Generate invalid triage creation requests (bad appointmentId, wrong status, duplicates, etc.)
- Verify each invalid request fails with appropriate error message
- Verify no Triage_Record is persisted for failed requests
- Verify database state remains consistent after failed attempts

**Rationale:** Error condition testing ensures the system properly rejects invalid inputs. Property-based testing generates diverse invalid inputs to verify comprehensive validation.

### Property 4: Query by Appointment Round-Trip (Idempotence)

**Property:** After creating a Triage_Record with appointmentId X, querying findByAppointmentId(X) SHALL return the created record. Querying multiple times SHALL return the same result.

**Test Strategy:** Property-based test
- Generate random valid triage creation requests
- Create triage record and capture the result
- Query by appointmentId and verify returned record matches created record
- Query multiple times and verify idempotence (same result each time)

**Rationale:** This verifies the create-query round-trip works correctly and that queries are idempotent. Property-based testing ensures this holds for diverse triage data.

### Property 5: Appointment Status Validation (Invariant)

**Property:** All Triage_Records SHALL reference appointments in ACTIVE status at creation time.

**Test Strategy:** Property-based test
- Generate triage creation requests with appointments in various statuses (SCHEDULED, CANCELLED, COMPLETED, MISSED)
- Verify only ACTIVE appointments allow triage creation
- Verify non-ACTIVE appointments fail with "Appointment must be in ACTIVE status"
- Verify no Triage_Record exists for non-ACTIVE appointments

**Rationale:** This invariant ensures triage only occurs for activated appointments. Property-based testing verifies the constraint across all possible appointment statuses.

### Property 6: Index Performance Guarantee (Performance Property)

**Property:** findByAppointmentId query execution time SHALL be O(log n) and complete in less than 50ms for databases with up to 100,000 triage records.

**Test Strategy:** Performance test (not property-based)
- Create database with 100,000 triage records
- Execute findByAppointmentId 100 times with random appointmentIds
- Measure execution time for each query
- Verify 95th percentile is below 50ms
- Verify query plan uses the appointmentId index

**Rationale:** This is a performance property that requires measurement rather than property-based testing. We use statistical sampling to verify performance guarantees.

### Property 7: Migration Idempotence (Idempotence)

**Property:** Running the database migration multiple times SHALL produce the same final schema state as running it once.

**Test Strategy:** Integration test (not property-based)
- Execute migration on clean database → verify schema
- Execute migration again → verify schema unchanged
- Execute migration third time → verify schema unchanged
- Verify no errors occur on repeated execution

**Rationale:** Migration idempotence is critical for deployment safety. This is tested with a fixed sequence rather than property-based testing since migrations are deterministic.

### Property 8: Existing Validation Preservation (Invariant)

**Property:** All Triage_Records created after integration SHALL satisfy all pre-integration validation rules (vital signs exist, discriminators valid, priority calculated correctly).

**Test Strategy:** Property-based test
- Generate random triage creation requests with appointmentId
- Verify vital signs validation still applies (fails without vital signs)
- Verify discriminator validation still applies (fails with invalid discriminators)
- Verify priority calculation still uses Manchester algorithm
- Compare behavior with pre-integration triage creation

**Rationale:** This ensures the integration doesn't break existing functionality. Property-based testing verifies all validation rules still apply across diverse inputs.

## Notes

- **Parser/Serializer Requirements:** This feature does not introduce new parsers or serializers. All data formats (JSON request/response) use existing Spring Boot serialization.

- **Property-Based Testing Scope:** Properties 1-5 and 8 are suitable for property-based testing as they verify logic that varies meaningfully with input. Properties 6 and 7 are better suited for integration/performance tests as they test infrastructure behavior.

- **Database Migration:** The migration must be reversible for rollback scenarios. Consider creating a down migration that removes the appointment_id column and indexes.

- **Backward Compatibility:** Existing triage records without appointmentId will need to be handled. Consider making appointmentId nullable initially and migrating data before enforcing NOT NULL constraint.
