# Implementation Plan: Patient Service

## Overview

This implementation plan follows TDD methodology (Red-Green-Refactor) to build the Patient Service, a simple MVC CRUD microservice for managing patient demographic data. The service uses Spring Boot 3.2.4, Java 17, PostgreSQL, and registers with Eureka for service discovery.

**Architecture**: MVC (Controller → Service → Repository → Entity)  
**Database**: PostgreSQL (patient_schema)  
**Port**: 8082  
**Estimated Time**: 6-8 hours

## Tasks

- [ ] 1. Project setup and configuration
  - Create Maven project structure with Spring Boot 3.2.4 and Java 17
  - Add dependencies: Spring Web, Spring Data JPA, PostgreSQL, Eureka Client, Validation, BCrypt, Lombok, Spring Boot Actuator
  - Create application.yml with database, Eureka, and actuator configuration
  - Create application-docker.yml for Docker environment
  - Create SecurityConfig with BCryptPasswordEncoder bean (strength 10)
  - _Requirements: C1, C2, D1, D2_

- [ ] 2. Database schema and entities
  - [ ] 2.1 Create database schema SQL script
    - Write schema.sql with patient_schema, patients table, addresses table, emergency_contacts table
    - Add indexes on dpi, email, first_name, first_last_name
    - Add unique constraints on dpi and email
    - _Requirements: FR1, C3, NFR5_
  
  - [ ] 2.2 Implement Patient entity
    - Create Patient entity with all fields (id, dpi, firstName, secondName, firstLastName, secondLastName, dateOfBirth, gender, email, phone, password, status, createdAt, updatedAt)
    - Add @OneToOne relationship with Address
    - Add @OneToMany relationship with EmergencyContact
    - Implement getFullName() helper method
    - Add @PrePersist and @PreUpdate lifecycle callbacks
    - _Requirements: FR1, BR1, BR2_
  
  - [ ] 2.3 Implement Address entity
    - Create Address entity with fields (id, department, municipality, zone, fullAddress, postalCode, patient)
    - Add @OneToOne relationship with Patient
    - _Requirements: FR1_
  
  - [ ] 2.4 Implement EmergencyContact entity
    - Create EmergencyContact entity with fields (id, fullName, relationship, phone, patient)
    - Add @ManyToOne relationship with Patient
    - Create Relationship enum (FATHER, MOTHER, SPOUSE, SON, DAUGHTER, BROTHER, SISTER, OTHER)
    - _Requirements: FR1_
  
  - [ ] 2.5 Create Gender and PatientStatus enums
    - Create Gender enum (MALE, FEMALE, OTHER)
    - Create PatientStatus enum (ACTIVE, INACTIVE)
    - _Requirements: FR1_

- [ ] 3. Repository layer
  - [ ] 3.1 Create PatientRepository interface
    - Extend JpaRepository<Patient, String>
    - Add findByDpi(String dpi) method
    - Add findByEmailIgnoreCase(String email) method
    - Add existsByDpi(String dpi) method
    - Add existsByEmail(String email) method
    - Add searchByName(@Param("query") String query, Pageable pageable) with @Query annotation
    - _Requirements: FR4, BR1, BR2_
  
  - [ ] 3.2 Create AddressRepository interface
    - Extend JpaRepository<Address, String>
    - _Requirements: FR1_
  
  - [ ] 3.3 Create EmergencyContactRepository interface
    - Extend JpaRepository<EmergencyContact, String>
    - Add findByPatientId(String patientId) method
    - _Requirements: FR1_
  
  - [ ]* 3.4 Write repository integration tests
    - Test PatientRepository.findByDpi()
    - Test PatientRepository.findByEmailIgnoreCase()
    - Test PatientRepository.searchByName()
    - Test cascade operations (save patient with address and contacts)
    - Use @DataJpaTest and test database
    - _Requirements: FR1, FR4_

- [ ] 4. Validation service (pure functions - ideal for PBT)
  - [ ] 4.1 Implement ValidationService
    - Create ValidationService with validateDpi(String dpi) method
    - Implement validateEmail(String email) method
    - Implement validatePhone(String phone) method
    - Implement validateDateOfBirth(LocalDate dateOfBirth) method
    - Implement validateName(String name, String fieldName) method
    - Implement validatePatientRegistration(PatientRegistrationRequest request) method
    - Use regex patterns for DPI (13 digits), email (standard format), phone (8 digits)
    - _Requirements: FR3_
  
  - [ ]* 4.2 Write property test for DPI validation
    - **Property 3: DPI Validation Consistency**
    - **Validates: Requirements FR3**
    - Generate random strings and verify validation accepts only 13-digit strings
    - Test with valid DPIs (13 digits), invalid DPIs (12 digits, 14 digits, alphanumeric)
    - _Requirements: FR3_
  
  - [ ]* 4.3 Write property test for email validation
    - **Property 4: Email Validation Consistency**
    - **Validates: Requirements FR3**
    - Generate random strings and verify validation accepts only valid email formats
    - Test with valid emails, invalid emails (missing @, missing domain, etc.)
    - _Requirements: FR3_
  
  - [ ]* 4.4 Write property test for phone validation
    - **Property 5: Phone Validation Consistency**
    - **Validates: Requirements FR3**
    - Generate random strings and verify validation accepts only 8-digit strings
    - Test with valid phones (8 digits), invalid phones (7 digits, 9 digits, alphanumeric)
    - _Requirements: FR3_
  
  - [ ]* 4.5 Write unit tests for ValidationService
    - Test validateDateOfBirth with future dates, past dates, edge cases
    - Test validateName with empty strings, null, max length (100 chars)
    - Test validatePatientRegistration with complete valid/invalid requests
    - _Requirements: FR3_

- [ ] 5. Password generation service (pure function - ideal for PBT)
  - [ ] 5.1 Implement PasswordGeneratorService
    - Create PasswordGeneratorService with generateTemporaryPassword() method
    - Use SecureRandom for cryptographically secure randomness
    - Generate 8-character password with at least 1 uppercase, 1 lowercase, 1 digit
    - Implement shuffleString() helper method to randomize character positions
    - _Requirements: FR2, NFR4_
  
  - [ ]* 5.2 Write property test for password strength
    - **Property 1: Password Generation Strength**
    - **Validates: Requirements FR2**
    - Generate 100 passwords and verify each has exactly 8 chars, at least 1 uppercase, 1 lowercase, 1 digit
    - _Requirements: FR2_
  
  - [ ]* 5.3 Write property test for password uniqueness
    - **Property 2: Password Generation Uniqueness**
    - **Validates: Requirements FR2**
    - Generate 1000 passwords and verify all are unique (no duplicates)
    - _Requirements: FR2_
  
  - [ ]* 5.4 Write unit tests for PasswordGeneratorService
    - Test password length is exactly 8 characters
    - Test password contains required character types
    - Test SecureRandom is used (not predictable)
    - _Requirements: FR2_

- [ ] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. DTOs (Request and Response classes)
  - [ ] 7.1 Create request DTOs
    - Create PatientRegistrationRequest with validation annotations (@NotBlank, @Pattern, @Email, @Past, @Valid)
    - Create AddressRequest with validation annotations
    - Create EmergencyContactRequest with validation annotations
    - Create PatientUpdateRequest with optional fields
    - _Requirements: FR1, FR3, FR5_
  
  - [ ] 7.2 Create response DTOs
    - Create PatientRegistrationResponse (id, dpi, fullName, email, temporaryPassword, message)
    - Create PatientResponse (complete patient data)
    - Create AddressResponse
    - Create EmergencyContactResponse
    - Create PatientSearchResult (id, dpi, fullName, dateOfBirth, phone, email)
    - Create PatientCredentialsResponse (email, dpi, note)
    - _Requirements: FR5_

- [ ] 8. Custom exceptions
  - Create PatientNotFoundException
  - Create DuplicateDpiException
  - Create DuplicateEmailException
  - Create ValidationException
  - Create UnauthorizedException
  - _Requirements: BR1, BR2_

- [ ] 9. Global exception handler
  - [ ] 9.1 Implement GlobalExceptionHandler
    - Create @RestControllerAdvice class
    - Handle PatientNotFoundException → 404 NOT_FOUND
    - Handle DuplicateDpiException, DuplicateEmailException → 409 CONFLICT
    - Handle ValidationException → 400 BAD_REQUEST
    - Handle MethodArgumentNotValidException → 400 BAD_REQUEST with field details
    - Handle UnauthorizedException → 403 FORBIDDEN
    - Handle generic Exception → 500 INTERNAL_SERVER_ERROR
    - Create ErrorResponse DTO (error, message, timestamp)
    - _Requirements: BR1, BR2_
  
  - [ ]* 9.2 Write unit tests for GlobalExceptionHandler
    - Test each exception handler returns correct HTTP status and error format
    - Test MethodArgumentNotValidException includes field-level details
    - _Requirements: BR1, BR2_

- [ ] 10. Patient service (business logic)
  - [ ] 10.1 Implement PatientService.registerPatient()
    - Inject PatientRepository, AddressRepository, EmergencyContactRepository, PasswordGeneratorService, ValidationService, PasswordEncoder
    - Validate input using ValidationService
    - Check DPI uniqueness using PatientRepository.existsByDpi()
    - Check email uniqueness using PatientRepository.existsByEmail()
    - Generate temporary password using PasswordGeneratorService
    - Hash password using PasswordEncoder (BCrypt)
    - Create Patient entity with all fields
    - Create Address entity and link to patient
    - Create EmergencyContact entities and link to patient
    - Save patient (cascades to address and contacts)
    - Return PatientRegistrationResponse with plain password
    - _Requirements: US-1, US-2, FR1, FR2, BR1, BR2_
  
  - [ ] 10.2 Implement PatientService.getPatient()
    - Find patient by ID using PatientRepository.findById()
    - Throw PatientNotFoundException if not found
    - Map Patient entity to PatientResponse DTO
    - _Requirements: US-4, FR5_
  
  - [ ] 10.3 Implement PatientService.searchPatients()
    - Auto-detect search type (dpi: 13 digits, email: contains @, name: default)
    - Search by DPI using PatientRepository.findByDpi()
    - Search by email using PatientRepository.findByEmailIgnoreCase()
    - Search by name using PatientRepository.searchByName() with PageRequest.of(0, 50)
    - Map results to PatientSearchResult DTOs
    - _Requirements: US-3, FR4_
  
  - [ ] 10.4 Implement PatientService.updatePatient()
    - Find patient by ID
    - Check authorization: PATIENT role can only update own record
    - Validate updated fields (phone, email) using ValidationService
    - Check email uniqueness (excluding current patient)
    - Update allowed fields: phone, email, address, emergency contacts
    - Replace emergency contacts (delete old, add new)
    - Set updatedAt timestamp
    - Save and return PatientResponse
    - _Requirements: US-5, BR4_
  
  - [ ] 10.5 Implement PatientService.getCredentials()
    - Find patient by ID
    - Return PatientCredentialsResponse with email, dpi, and note about password
    - _Requirements: US-2, FR5_
  
  - [ ] 10.6 Implement helper methods
    - Implement mapToPatientResponse(Patient patient) method
    - Implement mapToSearchResult(Patient patient) method
    - _Requirements: FR5_
  
  - [ ]* 10.7 Write unit tests for PatientService
    - Test registerPatient with valid data
    - Test registerPatient with duplicate DPI (should throw DuplicateDpiException)
    - Test registerPatient with duplicate email (should throw DuplicateEmailException)
    - Test getPatient with valid ID
    - Test getPatient with invalid ID (should throw PatientNotFoundException)
    - Test searchPatients by DPI, name, email
    - Test updatePatient with valid data
    - Test updatePatient authorization (PATIENT can only update own record)
    - Test updatePatient with duplicate email (should throw DuplicateEmailException)
    - Use Mockito to mock repositories and services
    - _Requirements: US-1, US-2, US-3, US-4, US-5_

- [ ] 11. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 12. Controller layer (REST endpoints)
  - [ ] 12.1 Implement PatientController
    - Create @RestController with @RequestMapping("/api/patients")
    - Inject PatientService
    - Implement POST /api/patients (registerPatient) → 201 CREATED
    - Implement GET /api/patients/{id} (getPatient) → 200 OK
    - Implement GET /api/patients/search (searchPatients) → 200 OK
    - Implement PUT /api/patients/{id} (updatePatient) → 200 OK
    - Implement GET /api/patients/{id}/credentials (getCredentials) → 200 OK
    - Add @Valid annotations for request body validation
    - Extract X-User-Id and X-User-Roles headers for updatePatient
    - _Requirements: FR5_
  
  - [ ]* 12.2 Write integration tests for PatientController
    - Test POST /api/patients with valid request → 201 CREATED
    - Test POST /api/patients with duplicate DPI → 409 CONFLICT
    - Test POST /api/patients with invalid data → 400 BAD_REQUEST
    - Test GET /api/patients/{id} with valid ID → 200 OK
    - Test GET /api/patients/{id} with invalid ID → 404 NOT_FOUND
    - Test GET /api/patients/search?query=Juan&searchType=name → 200 OK
    - Test GET /api/patients/search?query=1234567890123&searchType=dpi → 200 OK
    - Test PUT /api/patients/{id} with valid data → 200 OK
    - Test PUT /api/patients/{id} with unauthorized user → 403 FORBIDDEN
    - Test GET /api/patients/{id}/credentials → 200 OK
    - Use @SpringBootTest and MockMvc or TestRestTemplate
    - _Requirements: FR5_

- [ ] 13. Property test for full name concatenation
  - [ ]* 13.1 Write property test for getFullName()
    - **Property 6: Full Name Concatenation**
    - **Validates: Requirements FR1**
    - Generate random patient names with various combinations of optional fields
    - Verify getFullName() produces correct space-separated concatenation
    - Test with all fields present, only required fields, various combinations
    - _Requirements: FR1_

- [ ] 14. Application configuration and main class
  - [ ] 14.1 Create PatientServiceApplication main class
    - Create @SpringBootApplication class with main method
    - Enable Eureka client with @EnableDiscoveryClient
    - _Requirements: C2, D2_
  
  - [ ] 14.2 Verify application.yml configuration
    - Verify server.port=8082
    - Verify spring.application.name=patient-service
    - Verify datasource configuration with environment variables
    - Verify JPA configuration (ddl-auto=validate, default_schema=patient_schema)
    - Verify Eureka client configuration
    - Verify actuator endpoints (health, info, metrics, prometheus)
    - _Requirements: C1, C2, D1, D2_

- [ ] 15. Docker and deployment
  - [ ] 15.1 Create Dockerfile
    - Use eclipse-temurin:17-jre-alpine base image
    - Copy JAR file to /app/app.jar
    - Expose port 8082
    - Set JAVA_OPTS environment variable (-Xms256m -Xmx512m)
    - Set ENTRYPOINT to run JAR with JAVA_OPTS
    - _Requirements: C1_
  
  - [ ] 15.2 Update docker-compose.yml
    - Add patient-service service
    - Set environment variables (SPRING_PROFILES_ACTIVE=docker, DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD, EUREKA_SERVER_URL)
    - Add depends_on: postgres, eureka-server
    - Map port 8082:8082
    - Add to medflow-network
    - _Requirements: D1, D2, D3_
  
  - [ ] 15.3 Create database initialization script
    - Create init-patient-schema.sql in docker/postgres/init-scripts/
    - Include CREATE SCHEMA, CREATE TABLE statements, indexes, constraints
    - Ensure script is idempotent (IF NOT EXISTS)
    - _Requirements: C3, D3_

- [ ] 16. Documentation
  - [ ] 16.1 Create README.md
    - Add project overview and architecture description
    - Document prerequisites (Java 17, Maven, PostgreSQL, Eureka)
    - Add build instructions (mvn clean install)
    - Add run instructions (local and Docker)
    - Document API endpoints with examples
    - Add configuration guide (environment variables)
    - Add testing instructions (mvn test)
    - _Requirements: Overall acceptance criteria_
  
  - [ ] 16.2 Create API documentation
    - Document all 5 endpoints with request/response examples
    - Include error response examples (400, 404, 409, 403, 500)
    - Document required headers (X-User-Id, X-User-Roles)
    - Add authentication/authorization notes
    - _Requirements: FR5_

- [ ] 17. Final integration testing
  - [ ]* 17.1 Write end-to-end integration tests
    - Test complete patient registration flow (POST → GET)
    - Test search flow (POST multiple patients → search by name)
    - Test update flow (POST → PUT → GET)
    - Test error scenarios (duplicate DPI, duplicate email, not found)
    - Use @SpringBootTest with real database (Testcontainers or H2)
    - _Requirements: Overall acceptance criteria_
  
  - [ ]* 17.2 Test Eureka registration
    - Start Eureka server
    - Start patient-service
    - Verify service registers as PATIENT-SERVICE in Eureka
    - Verify health check endpoint /actuator/health returns UP
    - _Requirements: C2, D2_
  
  - [ ]* 17.3 Test API Gateway integration
    - Start API Gateway, Eureka, and patient-service
    - Test requests through API Gateway (http://localhost:8080/api/patients)
    - Verify JWT validation and routing work correctly
    - Verify X-User-Id and X-User-Roles headers are passed
    - _Requirements: D1_

- [ ] 18. Final checkpoint - Ensure all tests pass
  - Run mvn clean test to ensure all unit and integration tests pass
  - Run mvn clean install to build the JAR
  - Test Docker build: docker build -t patient-service .
  - Test Docker Compose: docker-compose up patient-service
  - Verify service starts successfully and registers with Eureka
  - Verify health check: curl http://localhost:8082/actuator/health
  - Ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties for pure functions (password generation, validation)
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end flows
- Follow TDD methodology: Write test first (Red), implement code (Green), refactor (Refactor)
- Estimated time: 6-8 hours for a simple CRUD service

## Testing Strategy

### Property-Based Tests (PBT)
- Focus on pure functions: PasswordGeneratorService, ValidationService
- Use JUnit QuickCheck or jqwik for property-based testing in Java
- Generate random inputs and verify properties hold for all inputs

### Unit Tests
- Test business logic in isolation using Mockito
- Test edge cases, error conditions, boundary values
- Target >= 80% code coverage

### Integration Tests
- Test REST endpoints with MockMvc or TestRestTemplate
- Test repository operations with @DataJpaTest
- Test complete flows with @SpringBootTest

### End-to-End Tests
- Test complete user scenarios (registration → search → update)
- Test integration with Eureka and API Gateway
- Use Testcontainers for real PostgreSQL database
