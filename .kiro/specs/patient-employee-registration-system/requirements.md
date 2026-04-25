# Requirements Document: Patient-Employee Registration System

## 1. Functional Requirements

### 1.1 Employee Registration (Existing - Maintain)

**REQ-1.1.1: Employee Account Creation**
- The system SHALL create employee accounts through POST /api/users/empleados endpoint
- The system SHALL generate unique usernames in format {firstName}.{firstLastName} (lowercase, no accents)
- The system SHALL append random digits to username if collision occurs
- The system SHALL assign specific roles (ADMIN, DOCTOR, ADMISSION, VITAL_SIGNS, LABORATORY, PHARMACY, CASHIER)
- The system SHALL NOT allow PATIENT role assignment through employee registration

**REQ-1.1.2: Employee Data Storage**
- The system SHALL store employee data only in auth_schema.users table
- The system SHALL NOT create records in patient_schema.patients for employees

**REQ-1.1.3: Employee Credentials**
- The system SHALL generate temporary passwords with 8 characters (1 uppercase, 1 lowercase, 1 digit)
- The system SHALL send temporary password via email to employee
- The system SHALL activate employee accounts immediately upon creation

### 1.2 Patient Registration (Complete Implementation)

**REQ-1.2.1: Patient Account Creation**
- The system SHALL create patient accounts through POST /api/auth/internal/create-patient endpoint
- The system SHALL use DPI (13 digits) as username for patient accounts
- The system SHALL assign PATIENT role exclusively to patient accounts
- The system SHALL validate DPI format (exactly 13 digits)

**REQ-1.2.2: Patient Medical Data Requirements**
- The system SHALL require birthDate in YYYY-MM-DD format
- The system SHALL require gender as either "M" or "F"
- The system SHALL accept optional fields: nit, department, municipality, zone, address
- The system SHALL validate birthDate is in the past

**REQ-1.2.3: Patient Dual Storage**
- The system SHALL create patient user record in auth_schema.users
- The system SHALL create patient medical record in patient_schema.patients
- The system SHALL link both records using auth_user_id field
- The system SHALL execute both creations within a single distributed transaction

**REQ-1.2.4: Patient Credentials**
- The system SHALL generate temporary passwords with 8 characters (1 uppercase, 1 lowercase, 1 digit)
- The system SHALL send temporary password via email to patient
- The system SHALL activate patient accounts immediately upon creation

### 1.3 Transactional Integrity

**REQ-1.3.1: Atomic Patient Registration**
- The system SHALL rollback user creation if patient record creation fails
- The system SHALL NOT leave orphan records in auth_schema.users
- The system SHALL NOT leave orphan records in patient_schema.patients
- The system SHALL use @Transactional annotation for automatic rollback

**REQ-1.3.2: Service Communication**
- The system SHALL use Feign Client for auth-service to patient-service communication
- The system SHALL handle FeignException when patient-service is unavailable
- The system SHALL return descriptive error messages when patient creation fails

### 1.4 Data Validation

**REQ-1.4.1: Employee Validation**
- The system SHALL validate email format for employees
- The system SHALL validate phone number has exactly 8 digits
- The system SHALL validate email uniqueness across all users
- The system SHALL validate role exists in auth_schema.roles

**REQ-1.4.2: Patient Validation**
- The system SHALL validate DPI has exactly 13 digits
- The system SHALL validate DPI uniqueness across all users
- The system SHALL validate email format for patients
- The system SHALL validate email uniqueness across all users
- The system SHALL validate phone number has exactly 8 digits
- The system SHALL validate birthDate format (YYYY-MM-DD)
- The system SHALL validate birthDate is in the past
- The system SHALL validate gender is "M" or "F"

### 1.5 Authentication

**REQ-1.5.1: Login Support**
- The system SHALL support login with username (DPI for patients, generated for employees)
- The system SHALL support login with email address
- The system SHALL validate password against hashed value
- The system SHALL generate JWT token upon successful authentication

## 2. Non-Functional Requirements

### 2.1 Performance

**REQ-2.1.1: Response Time**
- Employee registration SHALL complete within 3 seconds under normal load
- Patient registration SHALL complete within 5 seconds under normal load (includes patient-service call)

**REQ-2.1.2: Throughput**
- The system SHALL support at least 10 concurrent registration requests

### 2.2 Reliability

**REQ-2.2.1: Data Consistency**
- The system SHALL maintain 100% consistency between auth_schema.users and patient_schema.patients
- The system SHALL NOT allow orphan records in either schema

**REQ-2.2.2: Service Availability**
- The system SHALL handle patient-service unavailability gracefully
- The system SHALL return appropriate error messages when patient-service is down

### 2.3 Security

**REQ-2.3.1: Password Security**
- The system SHALL hash all passwords using BCrypt before storage
- The system SHALL generate cryptographically secure temporary passwords
- The system SHALL NOT store passwords in plain text

**REQ-2.3.2: Authorization**
- Employee registration endpoint SHALL require ADMIN role
- Patient registration endpoint SHALL be internal (not publicly exposed)

**REQ-2.3.3: Data Privacy**
- The system SHALL NOT expose temporary passwords in logs
- The system SHALL send temporary passwords only via email to registered address

### 2.4 Maintainability

**REQ-2.4.1: Code Quality**
- The system SHALL use DTOs for request/response validation
- The system SHALL use service layer for business logic
- The system SHALL use repository pattern for data access

**REQ-2.4.2: Error Handling**
- The system SHALL provide descriptive error messages for validation failures
- The system SHALL log all registration attempts with outcome
- The system SHALL distinguish between validation errors and system errors

### 2.5 Scalability

**REQ-2.5.1: Microservices Architecture**
- The system SHALL maintain separation between auth-service and patient-service
- The system SHALL use service discovery (Eureka) for inter-service communication
- The system SHALL support horizontal scaling of both services

## 3. Data Requirements

### 3.1 Auth Schema

**REQ-3.1.1: Users Table**
- The system SHALL store: id (UUID), username, password (hashed), email, first_name, second_name, first_last_name, second_last_name, phone, active, created_at, updated_at
- The system SHALL enforce unique constraints on username and email

**REQ-3.1.2: Roles Table**
- The system SHALL maintain roles: ADMIN, DOCTOR, ADMISSION, VITAL_SIGNS, LABORATORY, PHARMACY, CASHIER, PATIENT

**REQ-3.1.3: User-Roles Junction**
- The system SHALL link users to roles via user_roles table
- The system SHALL support multiple roles per user (future extensibility)

### 3.2 Patient Schema

**REQ-3.2.1: Patients Table**
- The system SHALL store: id (UUID), dpi, nit, first_name, second_name, first_last_name, second_last_name, birth_date, gender, email, phone, department, municipality, zone, address, auth_user_id, active, created_at, updated_at
- The system SHALL enforce unique constraints on dpi and email
- The system SHALL enforce NOT NULL constraints on dpi, first_name, first_last_name, birth_date, gender, email, phone

## 4. Interface Requirements

### 4.1 REST API Endpoints

**REQ-4.1.1: Employee Registration Endpoint**
- Method: POST
- Path: /api/users/empleados
- Authentication: Required (ADMIN role)
- Request Body: CreateEmployeeRequest
- Response: 201 Created with CreateEmployeeResponse
- Error Responses: 400 Bad Request, 401 Unauthorized, 409 Conflict

**REQ-4.1.2: Patient Registration Endpoint**
- Method: POST
- Path: /api/auth/internal/create-patient
- Authentication: Internal (not publicly exposed)
- Request Body: CreatePatientAccountRequest
- Response: 201 Created with CreatePatientAccountResponse
- Error Responses: 400 Bad Request, 500 Internal Server Error

**REQ-4.1.3: Patient Service Internal Endpoint**
- Method: POST
- Path: /api/patients/internal
- Authentication: Internal (service-to-service)
- Request Body: CreatePatientInternalRequest
- Response: 201 Created with PatientResponse
- Error Responses: 400 Bad Request, 500 Internal Server Error

### 4.2 Feign Client Interface

**REQ-4.2.1: Patient Service Client**
- The system SHALL define PatientServiceClient interface with @FeignClient annotation
- The system SHALL target patient-service by name (Eureka discovery)
- The system SHALL use /api/patients base path
- The system SHALL handle FeignException for service unavailability

### 4.3 Email Interface

**REQ-4.3.1: Email Notifications**
- The system SHALL send email with temporary password to new users
- The system SHALL include username and temporary password in email
- The system SHALL use EmailService for all email operations

## 5. Constraints

### 5.1 Technical Constraints

**REQ-5.1.1: Technology Stack**
- The system SHALL use Java 17+ with Spring Boot 3.x
- The system SHALL use PostgreSQL for data storage
- The system SHALL use Spring Cloud OpenFeign for inter-service communication
- The system SHALL use Spring Cloud Netflix Eureka for service discovery

**REQ-5.1.2: Database Schemas**
- The system SHALL use auth_schema for authentication data
- The system SHALL use patient_schema for patient medical data
- The system SHALL NOT mix schemas in single service

### 5.2 Business Constraints

**REQ-5.2.1: User Types**
- The system SHALL support exactly two user types: employees and patients
- The system SHALL NOT allow hybrid user types (both employee and patient)

**REQ-5.2.2: DPI Format**
- The system SHALL enforce Guatemalan DPI format (13 digits)
- The system SHALL use DPI as unique identifier for patients

## 6. Acceptance Criteria

### 6.1 Employee Registration

**AC-6.1.1: Successful Employee Creation**
- GIVEN valid employee data with role DOCTOR
- WHEN POST /api/users/empleados is called
- THEN user is created in auth_schema.users
- AND username is generated (e.g., "juan.perez")
- AND role DOCTOR is assigned
- AND temporary password is sent via email
- AND response contains user data and temporary password

**AC-6.1.2: Employee Email Uniqueness**
- GIVEN employee data with existing email
- WHEN POST /api/users/empleados is called
- THEN response is 409 Conflict
- AND error message indicates email already exists

### 6.2 Patient Registration

**AC-6.2.1: Successful Patient Creation**
- GIVEN valid patient data with DPI, birthDate, gender
- WHEN POST /api/auth/internal/create-patient is called
- THEN user is created in auth_schema.users with username = DPI
- AND patient is created in patient_schema.patients
- AND patient.auth_user_id = user.id
- AND role PATIENT is assigned
- AND temporary password is sent via email
- AND response contains user data and temporary password

**AC-6.2.2: Patient DPI Uniqueness**
- GIVEN patient data with existing DPI
- WHEN POST /api/auth/internal/create-patient is called
- THEN response is 409 Conflict
- AND error message indicates DPI already exists

**AC-6.2.3: Patient Medical Data Validation**
- GIVEN patient data with invalid birthDate format
- WHEN POST /api/auth/internal/create-patient is called
- THEN response is 400 Bad Request
- AND error message indicates invalid date format

**AC-6.2.4: Patient Service Failure Rollback**
- GIVEN valid patient data
- AND patient-service is unavailable
- WHEN POST /api/auth/internal/create-patient is called
- THEN response is 500 Internal Server Error
- AND user is NOT created in auth_schema.users
- AND patient is NOT created in patient_schema.patients

### 6.3 Authentication

**AC-6.3.1: Login with DPI**
- GIVEN patient account with DPI "1234567890123"
- WHEN POST /api/auth/login with username "1234567890123"
- THEN JWT token is generated
- AND user data is returned

**AC-6.3.2: Login with Email**
- GIVEN employee account with email "doctor@hospital.com"
- WHEN POST /api/auth/login with username "doctor@hospital.com"
- THEN JWT token is generated
- AND user data is returned

## 7. Dependencies

### 7.1 Internal Dependencies

**REQ-7.1.1: Service Dependencies**
- auth-service depends on patient-service for patient record creation
- auth-service depends on eureka-server for service discovery
- patient-service depends on eureka-server for service registration

### 7.2 External Dependencies

**REQ-7.2.1: Database**
- PostgreSQL 14+ with auth_schema and patient_schema

**REQ-7.2.2: Email Service**
- SMTP server for sending temporary password emails

**REQ-7.2.3: Libraries**
- Spring Cloud OpenFeign for inter-service communication
- Spring Security for password hashing
- Jakarta Validation for request validation
- Lombok for boilerplate reduction

## 8. Out of Scope

### 8.1 Not Included

- Public patient self-registration (only internal registration)
- Password reset functionality
- User profile updates
- User deactivation/deletion
- Role management UI
- Multi-factor authentication
- Password complexity configuration
- Email template customization
- Audit logging of registration events
- Integration with external identity providers
