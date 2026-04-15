# Implementation Plan: Patient Appointment Scheduling

## Overview

This implementation plan covers the development of a Spring Boot microservice for online appointment scheduling with payment processing, QR code generation, and email notifications. The service follows Domain-Driven Design (DDD) architecture and includes comprehensive property-based testing using jqwik to validate 20 correctness properties.

**Key Technologies**: Spring Boot 3.x, Java 17, PostgreSQL, jqwik, ZXing, JavaMail

**Architecture**: DDD with clear separation of presentation, application, domain, and infrastructure layers

## Tasks

- [ ] 1. Project setup and configuration
  - [x] 1.1 Create Spring Boot project structure with Maven
    - Initialize Spring Boot 3.x project with required dependencies
    - Configure parent POM with Spring Cloud dependencies
    - Set up project directory structure following DDD layers
    - _Requirements: 10.1, 10.2_

  - [x] 1.2 Configure application.yml with database, Eureka, and email settings
    - Configure PostgreSQL datasource connection
    - Set up Eureka client registration (port 8082)
    - Configure JavaMail SMTP settings
    - Add custom appointment configuration properties
    - _Requirements: 1.3, 10.1_

  - [x] 1.3 Add Maven dependencies for Spring Boot, PostgreSQL, ZXing, jqwik
    - Add spring-boot-starter-web, spring-boot-starter-data-jpa
    - Add spring-boot-starter-validation, spring-boot-starter-mail
    - Add spring-cloud-starter-netflix-eureka-client
    - Add ZXing core and javase for QR code generation
    - Add jqwik for property-based testing
    - Add Testcontainers for integration tests
    - _Requirements: 10.1_

  - [x] 1.4 Create Dockerfile for containerized deployment
    - Multi-stage build with Maven and JRE
    - Expose port 8082
    - Add health check endpoint
    - _Requirements: 10.1_

- [ ] 2. Database schema and migrations
  - [x] 2.1 Create Flyway migration V1__create_doctors_table.sql
    - Define doctors table with all required fields
    - Add indexes for active status and specialty
    - Insert 3 sample doctors for testing
    - _Requirements: 2.2_

  - [x] 2.2 Create Flyway migration V2__create_doctor_schedules_table.sql
    - Define doctor_schedules table with foreign key to doctors
    - Add constraints for day_of_week (1-7) and time_range validation
    - Add unique constraint on (doctor_id, day_of_week, start_time)
    - Insert sample schedules (Monday-Friday, 8:00-17:00)
    - _Requirements: 2.2, 2.3_

  - [x] 2.3 Create Flyway migration V3__create_appointments_table.sql
    - Define appointments table with patient info, appointment details, payment, and QR fields
    - Add DPI length check constraint (exactly 13 digits)
    - Add appointment time check constraint (06:00-20:00)
    - Create indexes for date_time, status, payment_status, dpi, correo
    - Add unique index to prevent double-booking (doctor_id, date, time)
    - _Requirements: 2.1, 2.2, 2.3, 3.1, 3.3_

- [ ] 3. Domain layer implementation
  - [x] 3.1 Implement PatientInfo value object with validation
    - Create @Embeddable class with all patient fields (DPI, NIT, names, birth date, phone, email)
    - Implement validateDPI() method (exactly 13 numeric digits)
    - Implement validateNIT() method ("C/F" or 1-8 digits)
    - Implement validateEmail() and validatePhone() methods
    - Implement getFullName() method
    - _Requirements: 3.1, 3.3, 3.4, 11.2, 11.3, 11.4, 11.5_

  - [x] 3.2 Write property test for DPI validation (Property 3)
    - **Property 3: DPI Validation**
    - **Validates: Requirements 3.3, 11.4**
    - Generate random strings with jqwik
    - Verify only 13-digit numeric strings pass validation
    - Test edge cases: 12 digits, 14 digits, alphanumeric, special characters

  - [ ] 3.3 Write property test for NIT validation (Property 4)
    - **Property 4: NIT Validation**
    - **Validates: Requirements 3.4, 11.5**
    - Generate random strings with jqwik
    - Verify only "C/F" or 1-8 digit numbers pass validation
    - Test edge cases: 0 digits, 9 digits, alphanumeric

  - [ ] 3.4 Write property test for email format validation (Property 18)
    - **Property 18: Email Format Validation**
    - **Validates: Requirements 11.2**
    - Generate random strings with jqwik
    - Verify only valid email formats pass (contains @, valid domain)
    - Test edge cases: missing @, invalid domain, special characters

  - [ ] 3.5 Write property test for phone format validation (Property 19)
    - **Property 19: Phone Format Validation**
    - **Validates: Requirements 11.3**
    - Generate random strings with jqwik
    - Verify only 8-digit phone numbers pass (Guatemala format)
    - Test edge cases: 7 digits, 9 digits, alphanumeric

  - [ ] 3.6 Implement PaymentRecord value object
    - Create @Embeddable class with payment status, amount, transaction ID, method, timestamp
    - Implement approve() method to set status to APPROVED
    - Implement reject() method to set status to REJECTED
    - Implement isApproved() method
    - _Requirements: 6.5, 6.6, 8.5_

  - [ ] 3.7 Implement AppointmentQR value object
    - Create @Embeddable class with QR code data, valid_from, valid_until
    - Implement isValid(currentTime) method (check time window)
    - Implement getValidityWindow() method for display
    - _Requirements: 7.3, 7.4, 7.8_

  - [ ] 3.8 Write property test for QR validity window calculation (Property 13)
    - **Property 13: QR Validity Window Calculation**
    - **Validates: Requirements 7.3, 7.4**
    - Generate random appointment times with jqwik
    - Verify validity window is always [appointmentTime - 1 hour, appointmentTime + 30 minutes]
    - Test edge cases: midnight appointments, end of day appointments

  - [ ] 3.9 Implement Appointment aggregate root entity
    - Create @Entity class with all fields (patient info, appointment details, payment, QR)
    - Add @Version field for optimistic locking
    - Implement confirmPayment() business method
    - Implement rejectPayment() business method
    - Implement generateQRCode() business method
    - Implement isQRCodeValid() business method
    - _Requirements: 4.1, 6.5, 6.6, 7.1, 8.5_

  - [ ] 3.10 Write property test for payment state transitions (Property 8)
    - **Property 8: Payment Approval State Transition**
    - **Validates: Requirements 6.5**
    - Generate random appointments in PENDING_PAYMENT status
    - Call confirmPayment() method
    - Verify status transitions to CONFIRMED
    - Verify payment record is updated correctly

  - [ ] 3.11 Implement Doctor entity
    - Create @Entity class with doctor code, name, specialty, email, phone, active status
    - Add @OneToMany relationship to DoctorSchedule
    - Implement getFullName() method
    - _Requirements: 2.2_

  - [ ] 3.12 Implement DoctorSchedule entity
    - Create @Entity class with doctor reference, day_of_week, start_time, end_time, active status
    - Add @ManyToOne relationship to Doctor
    - Implement isWorkingOn(DayOfWeek) method
    - Implement isWorkingAt(LocalTime) method
    - _Requirements: 2.2, 2.3_

  - [ ] 3.13 Create AppointmentStatus and PaymentStatus enums
    - Define AppointmentStatus: PENDING_PAYMENT, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    - Define PaymentStatus: PENDING, APPROVED, REJECTED
    - _Requirements: 6.5, 6.6, 8.5_

- [ ] 4. Repository layer implementation
  - [ ] 4.1 Create AppointmentRepository interface
    - Extend JpaRepository<Appointment, Long>
    - Add findByAppointmentId(String) method
    - Add countByDateAndTime(LocalDate, LocalTime) method
    - Add countByDoctorAndDateAndTime(Doctor, LocalDate, LocalTime) method
    - Add countByDate(LocalDate) method
    - _Requirements: 2.5, 4.1_

  - [ ] 4.2 Create DoctorRepository interface
    - Extend JpaRepository<Doctor, Long>
    - Add findByDayOfWeek(int) custom query
    - Add findAvailableDoctors(int dayOfWeek, LocalTime time) custom query
    - Add countByDayOfWeekAndTime(int, LocalTime) custom query
    - _Requirements: 2.2, 2.3_

  - [ ] 4.3 Create DoctorScheduleRepository interface
    - Extend JpaRepository<DoctorSchedule, Long>
    - Add findByDoctorAndIsActive(Doctor, boolean) method
    - _Requirements: 2.2_

- [ ] 5. Service layer - Availability calculation
  - [ ] 5.1 Implement AvailabilityService with slot calculation logic
    - Implement calculateAvailableSlots(startDate, daysAhead) method
    - For each date and hour, count doctors working at that time
    - Calculate total capacity: doctors × 2 appointments per hour
    - Count existing appointments for each slot
    - Calculate available count: capacity - booked
    - Return only slots with availableCount > 0
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [ ] 5.2 Write property test for availability calculation (Property 1)
    - **Property 1: Availability Calculation Follows Business Rules**
    - **Validates: Requirements 2.2, 2.3, 2.5**
    - Generate random doctor schedules and existing appointments with jqwik
    - Call calculateAvailableSlots() method
    - Verify available slots = (doctors working × 2) - booked appointments
    - Verify no doctor has more than 2 appointments in the same hour
    - Test edge cases: no doctors, all slots full, partial bookings

  - [ ] 5.3 Write property test for slot sorting (Property 2)
    - **Property 2: Available Slots Are Sorted**
    - **Validates: Requirements 2.4**
    - Generate random unsorted slot lists with jqwik
    - Call calculateAvailableSlots() method
    - Verify output is sorted by date ascending, then time ascending
    - Test edge cases: single slot, multiple days, same day different times

  - [ ] 5.4 Write property test for slot availability validation (Property 6)
    - **Property 6: Slot Availability Validation**
    - **Validates: Requirements 4.1**
    - Generate random slot selections with varying availability
    - Call isSlotAvailable() method
    - Verify correct identification of available vs unavailable slots
    - Test edge cases: last available slot, fully booked, no doctors working

  - [ ] 5.5 Implement isSlotAvailable(date, time) method
    - Count doctors working at the specified time
    - Calculate total capacity
    - Count existing appointments
    - Return true if bookedAppointments < totalCapacity
    - _Requirements: 4.1_

  - [ ] 5.6 Implement isSlotAvailableWithLock(date, time) method for pessimistic locking
    - Use @Lock(LockModeType.PESSIMISTIC_WRITE) annotation
    - Same logic as isSlotAvailable but with database lock
    - Used during appointment creation to prevent concurrent booking
    - _Requirements: 4.1_

- [ ] 6. Service layer - Appointment booking
  - [ ] 6.1 Implement AppointmentService with createAppointment method
    - Validate patient information using PatientInfo.validate()
    - Check slot availability with pessimistic lock
    - Assign doctor using least-loaded strategy
    - Generate unique appointment ID (APT-YYYYMMDD-XXX format)
    - Create Appointment entity with PENDING_PAYMENT status
    - Save appointment to database
    - Handle DataIntegrityViolationException for concurrent booking
    - Return AppointmentBookingResponse with payment URL
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.6, 4.1, 4.2_

  - [ ] 6.2 Write property test for required field validation (Property 5)
    - **Property 5: Required Field Validation**
    - **Validates: Requirements 3.6, 4.2**
    - Generate random incomplete booking requests with jqwik
    - Call createAppointment() method
    - Verify all requests with missing mandatory fields are rejected
    - Verify error message: "Por favor complete todos los campos obligatorios"
    - Test edge cases: one missing field, multiple missing fields, all fields missing

  - [ ] 6.3 Write property test for validation error specificity (Property 17)
    - **Property 17: Validation Error Specificity**
    - **Validates: Requirements 11.1**
    - Generate random invalid data for each field with jqwik
    - Call createAppointment() method
    - Verify error message specifically identifies which field is invalid
    - Test all fields: DPI, NIT, email, phone, date, time

  - [ ] 6.4 Write property test for valid data preservation on error (Property 20)
    - **Property 20: Valid Data Preservation on Error**
    - **Validates: Requirements 11.6**
    - Generate random partial data with some valid and some invalid fields
    - Trigger validation error
    - Verify all fields that passed validation are preserved in response
    - Test edge cases: first field invalid, last field invalid, middle field invalid

  - [ ] 6.5 Implement assignDoctorForSlot(date, time) helper method
    - Get all doctors working at the specified time
    - Find doctor with least appointments at that time (load balancing)
    - Return the least-loaded doctor
    - Throw NoDoctorAvailableException if no doctors available
    - _Requirements: 2.2, 2.3_

  - [ ] 6.6 Implement generateAppointmentId(date) helper method
    - Format: APT-YYYYMMDD-XXX
    - Count existing appointments for the date
    - Increment sequence number
    - Return formatted appointment ID
    - _Requirements: 7.2, 8.2_

- [ ] 7. Service layer - Payment processing
  - [ ] 7.1 Implement MockPaymentService with processPayment method
    - Simulate 1-3 second processing delay
    - Validate card number format (13-19 digits)
    - Validate expiry date (not expired)
    - Simulate 80% approval rate using random number generator
    - Generate transaction ID for approved payments
    - Return PaymentResult with status and transaction ID or rejection reason
    - _Requirements: 6.2, 6.3, 6.4, 6.5, 6.6_

  - [ ] 7.2 Implement AppointmentService.processPayment method
    - Find appointment by ID
    - Verify appointment status is PENDING_PAYMENT
    - Call PaymentService.processPayment()
    - If approved: call appointment.confirmPayment(), generate QR code, send email
    - If rejected: call appointment.rejectPayment(), release slot
    - Save updated appointment
    - Return PaymentResponse with QR code or rejection reason
    - _Requirements: 6.5, 6.6, 6.7, 6.8, 6.9, 9.1, 9.2, 9.3_

  - [ ] 7.3 Write property test for payment rejection releases slot (Property 9)
    - **Property 9: Payment Rejection Releases Slot**
    - **Validates: Requirements 6.6, 6.7, 9.3**
    - Generate random appointments with payment rejection
    - Call processPayment() method
    - Verify appointment status is CANCELLED
    - Verify slot is available again for booking
    - Test edge cases: last slot, multiple rejections

  - [ ] 7.4 Write property test for access control based on payment (Property 10)
    - **Property 10: Access Control Based on Payment**
    - **Validates: Requirements 6.9**
    - Generate random appointments with varying payment status
    - Verify clinic service access granted only if payment status is APPROVED
    - Test edge cases: PENDING, REJECTED, APPROVED

  - [ ] 7.5 Write property test for post-payment status (Property 16)
    - **Property 16: Post-Payment Status**
    - **Validates: Requirements 8.5**
    - Generate random appointments
    - Process successful payment
    - Verify appointment status is CONFIRMED
    - Verify payment status is APPROVED

- [ ] 8. Service layer - QR code generation
  - [ ] 8.1 Implement QRCodeService with generateQRCode method
    - Build QR data string: "{appointmentId}|{patientName}|{date}|{time}"
    - Use ZXing MultiFormatWriter to encode QR code
    - Generate BitMatrix with 300x300 dimensions
    - Convert to PNG byte array using MatrixToImageWriter
    - Encode to Base64 string
    - Return data URI: "data:image/png;base64,{base64String}"
    - Handle exceptions and throw QRCodeGenerationException
    - _Requirements: 7.1, 7.2_

  - [ ] 8.2 Write property test for QR code uniqueness (Property 11)
    - **Property 11: QR Code Uniqueness**
    - **Validates: Requirements 7.1**
    - Generate random set of confirmed appointments with jqwik
    - Generate QR codes for all appointments
    - Verify each QR code is unique (no duplicates)
    - Test edge cases: same patient different times, different patients same time

  - [ ] 8.3 Write property test for QR code round-trip preservation (Property 12)
    - **Property 12: QR Code Round-Trip Preservation**
    - **Validates: Requirements 7.2**
    - Generate random appointments with jqwik
    - Encode appointment data into QR code
    - Decode QR code back to string
    - Verify appointment ID, patient name, date, and time are preserved
    - Test edge cases: special characters in names, edge dates

  - [ ] 8.4 Implement QRCodeService.validateQRCode method
    - Parse QR data string by pipe delimiter
    - Extract appointment ID, patient name, date, time
    - Find appointment in database
    - Verify appointment status is CONFIRMED
    - Calculate validity window: [appointmentTime - 1h, appointmentTime + 30m]
    - Check if current time is within validity window
    - Return QRValidationResult with valid/invalid status and reason
    - _Requirements: 7.3, 7.4, 7.8_

  - [ ] 8.5 Implement QRCodeService.generateQRCodeImage helper method
    - Generate QR code image as byte array
    - Used for email attachment
    - _Requirements: 7.6_

- [ ] 9. Service layer - Email notifications
  - [ ] 9.1 Create AsyncConfig for async email processing
    - Configure ThreadPoolTaskExecutor with 5 core threads, 10 max threads
    - Set queue capacity to 100
    - Set thread name prefix: "async-email-"
    - _Requirements: 7.7_

  - [ ] 9.2 Implement EmailService with sendAppointmentConfirmation method
    - Mark method with @Async annotation
    - Create MimeMessage with MimeMessageHelper
    - Set recipient to patient email
    - Set subject: "Confirmación de Cita - MedFlow HIS"
    - Build HTML email content with appointment details
    - Attach QR code image as inline image
    - Include QR validity window in email body
    - Send email using JavaMailSender
    - Add @Retryable annotation (3 attempts, 2s backoff)
    - Log success or failure
    - _Requirements: 7.6, 7.7, 7.8, 8.4_

  - [ ] 9.3 Write property test for email contains validity window (Property 14)
    - **Property 14: Email Contains Validity Window**
    - **Validates: Requirements 7.8**
    - Generate random appointments with jqwik
    - Build email content
    - Verify email contains QR validity window (start and end times)
    - Test edge cases: different time zones, midnight appointments

  - [ ] 9.4 Write property test for confirmation completeness (Property 15)
    - **Property 15: Confirmation Contains Required Fields**
    - **Validates: Requirements 8.2, 8.3**
    - Generate random appointments with jqwik
    - Build confirmation response
    - Verify confirmation contains: patient name, date, time, type, QR code, validity window
    - Test edge cases: missing optional fields, long names

  - [ ] 9.5 Create HTML email template (appointment-confirmation.html)
    - Design responsive HTML template with MedFlow branding
    - Include appointment details section
    - Include QR code image placeholder
    - Include validity window with important styling
    - Include instructions for clinic visit
    - _Requirements: 7.6, 7.8, 8.4_

  - [ ] 9.6 Implement @Recover method for email failure handling
    - Log failure after 3 retry attempts
    - Alert admin or queue for manual retry
    - Appointment remains confirmed even if email fails
    - _Requirements: 7.7_

- [ ] 10. Checkpoint - Core business logic complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 11. Presentation layer - REST API controllers
  - [ ] 11.1 Create DTO classes for API requests and responses
    - AppointmentBookingRequest with PatientInfoDTO and AppointmentDetailsDTO
    - AppointmentBookingResponse with appointmentId, status, paymentAmount, paymentUrl
    - AvailableSlotDTO with date, time, availableCount, doctorsAvailable
    - PaymentRequest with paymentMethod, card details
    - PaymentResponse with status, transactionId, qrCode, validity window
    - QRValidationRequest and QRValidationResponse
    - Add Bean Validation annotations (@NotNull, @NotBlank, @Email, @Pattern)
    - _Requirements: 3.1, 3.3, 3.4, 3.5, 3.6, 11.2, 11.3, 11.4, 11.5_

  - [ ] 11.2 Write property test for summary field completeness (Property 7)
    - **Property 7: Summary Contains Required Fields**
    - **Validates: Requirements 5.2**
    - Generate random appointments with jqwik
    - Build summary DTO
    - Verify summary contains: patient name, date, time, appointment type
    - Test edge cases: missing optional fields, long names

  - [ ] 11.3 Implement AppointmentController with REST endpoints
    - GET /api/appointments/available-slots (query params: date, daysAhead)
    - POST /api/appointments/book (request body: AppointmentBookingRequest)
    - POST /api/appointments/{appointmentId}/payment (request body: PaymentRequest)
    - GET /api/appointments/{appointmentId} (path variable: appointmentId)
    - POST /api/appointments/validate-qr (request body: QRValidationRequest)
    - Add @Valid annotations for request validation
    - Add appropriate HTTP status codes (200, 201, 400, 402, 404, 409)
    - _Requirements: 1.1, 2.1, 4.1, 5.1, 5.2, 6.1, 8.2_

  - [ ] 11.4 Write unit tests for AppointmentController
    - Test GET /api/appointments/available-slots returns 200 with slots
    - Test POST /api/appointments/book returns 201 with booking response
    - Test POST /api/appointments/{id}/payment returns 200 on approval
    - Test POST /api/appointments/{id}/payment returns 402 on rejection
    - Test GET /api/appointments/{id} returns 200 with appointment details
    - Test GET /api/appointments/{id} returns 404 for non-existent appointment
    - Use MockMvc for controller testing

- [ ] 12. Exception handling
  - [ ] 12.1 Create custom exception classes
    - AppointmentException (base exception)
    - SlotNotAvailableException
    - InvalidDPIException
    - InvalidNITException
    - AppointmentNotFoundException
    - PaymentProcessingException
    - QRCodeGenerationException
    - NoDoctorAvailableException
    - EmailSendingException
    - _Requirements: 3.3, 3.4, 4.3, 6.6, 9.1, 9.2, 11.1_

  - [ ] 12.2 Implement AppointmentExceptionHandler with @RestControllerAdvice
    - Handle SlotNotAvailableException → 409 Conflict
    - Handle InvalidDPIException, InvalidNITException → 400 Bad Request
    - Handle AppointmentNotFoundException → 404 Not Found
    - Handle PaymentProcessingException → 402 Payment Required
    - Handle MethodArgumentNotValidException → 400 with "Por favor complete todos los campos obligatorios"
    - Handle DataIntegrityViolationException → 409 Conflict
    - Handle generic Exception → 500 Internal Server Error
    - Return ErrorResponse DTO with timestamp, status, error, message, details
    - _Requirements: 3.5, 3.6, 4.3, 6.6, 9.1, 9.2, 9.4, 11.1, 11.6, 11.7_

  - [ ] 12.3 Create ErrorResponse DTO
    - Fields: timestamp, status, error, message, details (list of strings)
    - Used by global exception handler
    - _Requirements: 11.1_

- [ ] 13. Integration tests
  - [ ] 13.1 Write integration test for complete booking workflow
    - Use @SpringBootTest and Testcontainers for PostgreSQL
    - Test: Get available slots → Book appointment → Process payment → Verify confirmation
    - Verify database state after each step
    - Verify email sent (use mock SMTP server)
    - Test happy path with payment approval

  - [ ] 13.2 Write integration test for payment rejection workflow
    - Test: Book appointment → Process payment (rejected) → Verify slot released
    - Verify appointment status is CANCELLED
    - Verify slot is available again

  - [ ] 13.3 Write integration test for concurrent booking prevention
    - Use multiple threads to book the same slot simultaneously
    - Verify only one booking succeeds
    - Verify other bookings receive SlotNotAvailableException
    - Test pessimistic locking and database constraint enforcement

  - [ ] 13.4 Write integration test for QR code validation
    - Generate QR code for appointment
    - Validate QR code within validity window → should succeed
    - Validate QR code before validity window → should fail
    - Validate QR code after validity window → should fail

  - [ ] 13.5 Write integration test for email sending with retry
    - Mock SMTP server to fail first 2 attempts
    - Verify email is sent on 3rd attempt
    - Verify @Retryable annotation works correctly

- [ ] 14. Configuration and deployment
  - [ ] 14.1 Create application-docker.yml for Docker environment
    - Override datasource URL to use Docker container name
    - Override Eureka server URL to use Docker container name
    - _Requirements: 10.1_

  - [ ] 14.2 Add Flyway configuration to pom.xml
    - Add flyway-core dependency
    - Configure Flyway to run migrations on startup
    - _Requirements: 10.1_

  - [ ] 14.3 Create README.md with setup and running instructions
    - Document prerequisites (Java 17, Maven, PostgreSQL, Docker)
    - Document how to run locally
    - Document how to run with Docker
    - Document API endpoints
    - Document environment variables
    - _Requirements: 10.1_

  - [ ] 14.4 Add Spring Boot Actuator for health checks
    - Add spring-boot-starter-actuator dependency
    - Configure health endpoint
    - Add to Dockerfile health check
    - _Requirements: 10.2_

- [ ] 15. Sample data and testing utilities
  - [ ] 15.1 Create SQL script to insert sample doctors and schedules
    - Insert 3-5 doctors with different specialties
    - Insert schedules for Monday-Friday, 8:00-17:00
    - Insert schedules for Saturday, 8:00-12:00
    - _Requirements: 2.2_

  - [ ] 15.2 Create test data builders for unit tests
    - AppointmentBuilder for creating test appointments
    - PatientInfoBuilder for creating test patient info
    - DoctorBuilder for creating test doctors
    - DoctorScheduleBuilder for creating test schedules
    - Use builder pattern for flexible test data creation

- [ ] 16. Final checkpoint - All tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property-based tests validate universal correctness properties using jqwik
- Integration tests verify component interactions and external integrations
- Pessimistic locking prevents concurrent booking conflicts
- Email sending is asynchronous to avoid blocking the payment response
- Mock payment gateway simulates 80% approval rate for testing
- QR code validity window: 1 hour before to 30 minutes after appointment
- DPI must be exactly 13 numeric digits (Guatemala format)
- NIT must be "C/F" or 1-8 numeric digits
- Spanish error messages per RN05: "Por favor complete todos los campos obligatorios"
- Fast loading times ≤2 seconds per RN01
- Service registers with Eureka on port 8082
- Database schema enforces business rules with constraints and indexes
