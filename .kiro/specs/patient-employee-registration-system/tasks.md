# Implementation Tasks: Patient-Employee Registration System

## Phase 1: Update DTOs and Validation

### 1.1 Update CreatePatientAccountRequest DTO
- [ ] 1.1.1 Add birthDate field with @NotBlank and @Pattern validation (YYYY-MM-DD)
- [ ] 1.1.2 Add gender field with @NotBlank and @Pattern validation (M|F)
- [ ] 1.1.3 Add optional fields: nit, department, municipality, zone, address
- [ ] 1.1.4 Update validation messages in Spanish
- [ ] 1.1.5 Add @Size validation for DPI (min=13, max=13)

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/dto/CreatePatientAccountRequest.java`

### 1.2 Create CreatePatientInternalRequest DTO
- [ ] 1.2.1 Create new DTO class in auth-service
- [ ] 1.2.2 Add all fields: id, dpi, nit, firstName, secondName, firstLastName, secondLastName, birthDate, gender, email, phone, department, municipality, zone, address, authUserId, active
- [ ] 1.2.3 Add Lombok @Data and @Builder annotations
- [ ] 1.2.4 Add validation annotations for required fields

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/dto/CreatePatientInternalRequest.java` (NEW)

### 1.3 Create PatientResponse DTO
- [ ] 1.3.1 Create new DTO class in auth-service
- [ ] 1.3.2 Add fields: id, dpi, fullName, email, authUserId, active
- [ ] 1.3.3 Add Lombok @Data annotation

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/dto/PatientResponse.java` (NEW)

### 1.4 Update RegisterRequest DTO (Optional - for public registration)
- [ ] 1.4.1 Add birthDate field with validation
- [ ] 1.4.2 Add gender field with validation
- [ ] 1.4.3 Add optional medical fields

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/dto/RegisterRequest.java`

## Phase 2: Implement Feign Client

### 2.1 Add Feign Dependencies
- [ ] 2.1.1 Add spring-cloud-starter-openfeign dependency to auth-service pom.xml
- [ ] 2.1.2 Verify Spring Cloud version compatibility

**File:** `backend-services/auth-service/pom.xml`

### 2.2 Create PatientServiceClient Interface
- [ ] 2.2.1 Create new interface with @FeignClient annotation
- [ ] 2.2.2 Set name="patient-service" and path="/api/patients"
- [ ] 2.2.3 Define createPatient method with @PostMapping("/internal")
- [ ] 2.2.4 Add proper request/response types

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/client/PatientServiceClient.java` (NEW)

### 2.3 Enable Feign Clients
- [ ] 2.3.1 Add @EnableFeignClients annotation to AuthServiceApplication
- [ ] 2.3.2 Verify Eureka client is enabled

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/AuthServiceApplication.java`

## Phase 3: Update Auth Service Logic

### 3.1 Update AuthService.createPatientAccount()
- [ ] 3.1.1 Inject PatientServiceClient dependency
- [ ] 3.1.2 Add validation for birthDate format and past date
- [ ] 3.1.3 Add validation for gender (M or F)
- [ ] 3.1.4 After saving user, create CreatePatientInternalRequest
- [ ] 3.1.5 Call patientServiceClient.createPatient() inside try-catch
- [ ] 3.1.6 Handle FeignException and throw RuntimeException for rollback
- [ ] 3.1.7 Add logging for patient creation success/failure
- [ ] 3.1.8 Ensure @Transactional annotation is present

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/service/AuthService.java`

### 3.2 Update AuthService.register() (Optional)
- [ ] 3.2.1 Apply same patient-service integration logic
- [ ] 3.2.2 Add validation for medical fields
- [ ] 3.2.3 Ensure transactional rollback on failure

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/service/AuthService.java`

### 3.3 Add Validation Helper Methods
- [ ] 3.3.1 Create validateBirthDate() method
- [ ] 3.3.2 Create validateGender() method
- [ ] 3.3.3 Create validateDpiFormat() method

**File:** `backend-services/auth-service/src/main/java/com/medflow/auth/service/AuthService.java`

## Phase 4: Implement Patient Service Endpoint

### 4.1 Create Patient Service DTOs
- [ ] 4.1.1 Create CreatePatientInternalRequest DTO in patient-service
- [ ] 4.1.2 Create PatientResponse DTO in patient-service
- [ ] 4.1.3 Add validation annotations
- [ ] 4.1.4 Add Lombok annotations

**Files:**
- `backend-services/patient-service/src/main/java/com/medflow/patient/dto/CreatePatientInternalRequest.java` (NEW)
- `backend-services/patient-service/src/main/java/com/medflow/patient/dto/PatientResponse.java` (NEW)

### 4.2 Update Patient Entity
- [ ] 4.2.1 Verify all fields exist in Patient entity
- [ ] 4.2.2 Add missing fields if necessary (nit, department, municipality, zone, address)
- [ ] 4.2.3 Verify @Column annotations and constraints

**File:** `backend-services/patient-service/src/main/java/com/medflow/patient/domain/Patient.java`

### 4.3 Create PatientService.createPatient()
- [ ] 4.3.1 Create method to map CreatePatientInternalRequest to Patient entity
- [ ] 4.3.2 Save patient to patient_schema.patients
- [ ] 4.3.3 Return PatientResponse with created patient data
- [ ] 4.3.4 Add error handling for database constraints
- [ ] 4.3.5 Add logging for patient creation

**File:** `backend-services/patient-service/src/main/java/com/medflow/patient/service/PatientService.java`

### 4.4 Create PatientController Internal Endpoint
- [ ] 4.4.1 Add POST /api/patients/internal endpoint
- [ ] 4.4.2 Accept CreatePatientInternalRequest with @Valid
- [ ] 4.4.3 Call patientService.createPatient()
- [ ] 4.4.4 Return 201 Created with PatientResponse
- [ ] 4.4.5 Add exception handling for validation errors

**File:** `backend-services/patient-service/src/main/java/com/medflow/patient/controller/PatientController.java`

## Phase 5: Error Handling and Logging

### 5.1 Add Custom Exceptions
- [ ] 5.1.1 Create PatientCreationException in auth-service
- [ ] 5.1.2 Create InvalidMedicalDataException in auth-service
- [ ] 5.1.3 Update GlobalExceptionHandler to handle new exceptions

**Files:**
- `backend-services/auth-service/src/main/java/com/medflow/auth/exception/PatientCreationException.java` (NEW)
- `backend-services/auth-service/src/main/java/com/medflow/auth/exception/InvalidMedicalDataException.java` (NEW)
- `backend-services/auth-service/src/main/java/com/medflow/auth/exception/GlobalExceptionHandler.java`

### 5.2 Add Logging
- [ ] 5.2.1 Add INFO logs for successful patient creation
- [ ] 5.2.2 Add ERROR logs for patient-service failures
- [ ] 5.2.3 Add DEBUG logs for validation steps
- [ ] 5.2.4 Ensure no sensitive data (passwords) in logs

**Files:**
- `backend-services/auth-service/src/main/java/com/medflow/auth/service/AuthService.java`
- `backend-services/patient-service/src/main/java/com/medflow/patient/service/PatientService.java`

## Phase 6: Testing

### 6.1 Unit Tests - Auth Service
- [ ] 6.1.1 Test createPatientAccount() with valid data
- [ ] 6.1.2 Test createPatientAccount() with invalid birthDate
- [ ] 6.1.3 Test createPatientAccount() with invalid gender
- [ ] 6.1.4 Test createPatientAccount() with duplicate DPI
- [ ] 6.1.5 Test createPatientAccount() with patient-service failure (mock FeignException)
- [ ] 6.1.6 Verify rollback when patient creation fails
- [ ] 6.1.7 Test createEmployee() still works correctly

**File:** `backend-services/auth-service/src/test/java/com/medflow/auth/service/AuthServiceTest.java`

### 6.2 Unit Tests - Patient Service
- [ ] 6.2.1 Test createPatient() with valid data
- [ ] 6.2.2 Test createPatient() with duplicate DPI
- [ ] 6.2.3 Test createPatient() with missing required fields
- [ ] 6.2.4 Verify patient entity is saved correctly

**File:** `backend-services/patient-service/src/test/java/com/medflow/patient/service/PatientServiceTest.java`

### 6.3 Integration Tests
- [ ] 6.3.1 Test complete patient registration flow (auth-service → patient-service)
- [ ] 6.3.2 Test rollback when patient-service is down
- [ ] 6.3.3 Test employee registration still works
- [ ] 6.3.4 Test login with DPI after patient registration
- [ ] 6.3.5 Verify no orphan records in database after failures

**Files:**
- `backend-services/auth-service/src/test/java/com/medflow/auth/integration/PatientRegistrationIntegrationTest.java` (NEW)

### 6.4 API Tests (Manual or Postman)
- [ ] 6.4.1 Test POST /api/users/empleados with valid employee data
- [ ] 6.4.2 Test POST /api/auth/internal/create-patient with valid patient data
- [ ] 6.4.3 Test POST /api/auth/internal/create-patient with invalid birthDate
- [ ] 6.4.4 Test POST /api/auth/internal/create-patient with invalid gender
- [ ] 6.4.5 Test POST /api/auth/login with DPI
- [ ] 6.4.6 Test POST /api/auth/login with email
- [ ] 6.4.7 Verify patient data in both auth_schema.users and patient_schema.patients

## Phase 7: Database Verification

### 7.1 Verify Schema Compatibility
- [ ] 7.1.1 Verify auth_schema.users has all required columns
- [ ] 7.1.2 Verify patient_schema.patients has all required columns
- [ ] 7.1.3 Verify unique constraints on dpi and email
- [ ] 7.1.4 Verify foreign key relationship (auth_user_id)

**Files:**
- `backend-services/auth-service/src/main/resources/schema.sql`
- `backend-services/patient-service/src/main/resources/db/migration/V4__create_patient_schema.sql`

### 7.2 Add Database Indexes (if missing)
- [ ] 7.2.1 Verify index on patients.dpi
- [ ] 7.2.2 Verify index on patients.email
- [ ] 7.2.3 Verify index on patients.auth_user_id

**File:** `backend-services/patient-service/src/main/resources/db/migration/V4__create_patient_schema.sql`

## Phase 8: Configuration

### 8.1 Update Application Configuration
- [ ] 8.1.1 Verify Eureka client configuration in auth-service
- [ ] 8.1.2 Verify Eureka client configuration in patient-service
- [ ] 8.1.3 Add Feign client timeout configuration
- [ ] 8.1.4 Add Feign client retry configuration (optional)

**Files:**
- `backend-services/auth-service/src/main/resources/application.yml`
- `backend-services/patient-service/src/main/resources/application.yml`

### 8.2 Update Docker Configuration (if applicable)
- [ ] 8.2.1 Verify service discovery works in Docker environment
- [ ] 8.2.2 Update docker-compose.yml if needed

**File:** `docker-compose.yml`

## Phase 9: Documentation

### 9.1 Update API Documentation
- [ ] 9.1.1 Document POST /api/users/empleados endpoint
- [ ] 9.1.2 Document POST /api/auth/internal/create-patient endpoint
- [ ] 9.1.3 Document POST /api/patients/internal endpoint
- [ ] 9.1.4 Add request/response examples
- [ ] 9.1.5 Document error codes and messages

**File:** `API_DOCUMENTATION.md` or Swagger/OpenAPI spec

### 9.2 Update README
- [ ] 9.2.1 Document new patient registration flow
- [ ] 9.2.2 Document required fields for patient registration
- [ ] 9.2.3 Document transactional behavior
- [ ] 9.2.4 Add troubleshooting section

**Files:**
- `backend-services/auth-service/README.md`
- `backend-services/patient-service/README.md`

## Phase 10: Deployment and Verification

### 10.1 Local Testing
- [ ] 10.1.1 Start Eureka server
- [ ] 10.1.2 Start auth-service
- [ ] 10.1.3 Start patient-service
- [ ] 10.1.4 Verify service registration in Eureka dashboard
- [ ] 10.1.5 Test employee registration
- [ ] 10.1.6 Test patient registration
- [ ] 10.1.7 Verify data in both databases

### 10.2 Integration Environment Testing
- [ ] 10.2.1 Deploy to integration environment
- [ ] 10.2.2 Run smoke tests
- [ ] 10.2.3 Verify transactional rollback works
- [ ] 10.2.4 Monitor logs for errors

### 10.3 Production Readiness
- [ ] 10.3.1 Review security configurations
- [ ] 10.3.2 Review error handling
- [ ] 10.3.3 Review logging (no sensitive data)
- [ ] 10.3.4 Review performance (response times)
- [ ] 10.3.5 Create rollback plan

## Summary

**Total Tasks:** 10 phases, 80+ individual tasks

**Critical Path:**
1. Phase 1: Update DTOs (foundation)
2. Phase 2: Implement Feign Client (communication)
3. Phase 3: Update Auth Service (core logic)
4. Phase 4: Implement Patient Service (data storage)
5. Phase 6: Testing (verification)

**Estimated Effort:**
- Phase 1-4: 2-3 days (core implementation)
- Phase 5: 1 day (error handling)
- Phase 6: 2-3 days (testing)
- Phase 7-10: 1-2 days (verification and deployment)

**Total:** 6-9 days for complete implementation

**Dependencies:**
- Eureka server must be running
- PostgreSQL with both schemas must be available
- SMTP server for email notifications
- Spring Cloud OpenFeign library

**Risks:**
- Feign client configuration issues
- Transactional rollback across services
- Network latency between services
- Database constraint violations
