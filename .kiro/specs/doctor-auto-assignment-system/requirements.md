# Requirements Document

## Introduction

This document specifies the requirements for the **Doctor Auto-Assignment System with Shift Management and Capacity Control**. The system automates doctor assignment for medical appointments based on workload balancing, shift schedules, and capacity constraints. It replaces the current manual doctor selection process with an intelligent allocation algorithm that considers doctor availability, working hours, and appointment capacity (2 appointments per hour per doctor).

## Glossary

- **Appointment_System**: The clinical service component responsible for managing medical appointments
- **Doctor_Assignment_Service**: The domain service that implements the automatic doctor assignment algorithm
- **Shift_Manager**: The component that manages doctor work schedules and availability
- **Capacity_Controller**: The component that enforces appointment capacity limits per time slot
- **Slot_Selector**: The frontend component that displays available appointment time slots
- **Doctor**: A medical professional with assigned work shifts and appointment capacity
- **Shift**: An 8-hour work period (Night: 00:00-08:00, Morning: 08:00-16:00, Evening: 16:00-24:00)
- **Time_Slot**: A 30-minute appointment window
- **Capacity**: The maximum number of appointments a doctor can handle per hour (2 appointments = 2 slots)
- **Workload**: The total number of appointments assigned to a doctor for a specific date
- **Day_Off**: A date when a doctor is marked as unavailable (vacation, personal day)
- **Administrator**: A user with permissions to manage doctor records and schedules
- **Patient**: A user who books appointments through the system

## Requirements

### Requirement 1: Doctor Management

**User Story:** As an Administrator, I want to create and manage doctor records with shift assignments, so that the system can assign doctors to appointments based on their availability.

#### Acceptance Criteria

1. THE Shift_Manager SHALL store doctor records with unique ID, full name, specialty, shift start time, shift end time, and active status
2. WHEN an Administrator creates a doctor record, THE Shift_Manager SHALL validate that shift start and end times form an 8-hour period
3. WHEN an Administrator updates a doctor's shift, THE Shift_Manager SHALL persist the new schedule to the database
4. WHEN an Administrator deactivates a doctor, THE Doctor_Assignment_Service SHALL exclude that doctor from future appointment assignments
5. THE Shift_Manager SHALL support three shift types: Night (00:00-08:00), Morning (08:00-16:00), and Evening (16:00-24:00)

### Requirement 2: Day Off and Vacation Management

**User Story:** As an Administrator, I want to mark specific dates as unavailable for doctors, so that the system does not assign appointments when doctors are on vacation or have days off.

#### Acceptance Criteria

1. WHEN an Administrator marks a date as unavailable for a doctor, THE Shift_Manager SHALL store the day-off record in the database
2. WHEN the Doctor_Assignment_Service evaluates available doctors for a date, THE Doctor_Assignment_Service SHALL exclude doctors with day-off records for that date
3. WHEN a doctor has a day off, THE Capacity_Controller SHALL reduce the total available capacity for that shift on that date
4. THE Shift_Manager SHALL allow Administrators to mark multiple consecutive dates as unavailable (vacation periods)
5. THE Shift_Manager SHALL allow Administrators to remove day-off records to restore doctor availability

### Requirement 3: Appointment Capacity Enforcement

**User Story:** As a system operator, I want the system to enforce capacity limits of 2 appointments per hour per doctor, so that doctors are not overbooked.

#### Acceptance Criteria

1. THE Capacity_Controller SHALL enforce a maximum of 2 time slots per hour per doctor (slots at :00 and :30)
2. WHEN a shift has 2 active doctors, THE Capacity_Controller SHALL allow a maximum of 4 appointments per hour for that shift
3. WHEN calculating available slots for a date and time, THE Capacity_Controller SHALL count existing appointments for each doctor in that time slot
4. IF all doctors in a shift have reached capacity for a time slot, THEN THE Capacity_Controller SHALL mark that slot as unavailable
5. WHEN a doctor has a day off, THE Capacity_Controller SHALL recalculate capacity using only available doctors for that shift

### Requirement 4: Automatic Doctor Assignment Algorithm

**User Story:** As a Patient, I want the system to automatically assign me to the doctor with the lowest workload, so that appointments are distributed fairly and I receive timely care.

#### Acceptance Criteria

1. WHEN a Patient creates an appointment, THE Doctor_Assignment_Service SHALL identify all doctors working during the requested time slot based on shift schedules
2. WHEN multiple doctors are available, THE Doctor_Assignment_Service SHALL count the total appointments for each doctor on the requested date
3. WHEN multiple doctors are available, THE Doctor_Assignment_Service SHALL assign the appointment to the doctor with the fewest appointments for that date
4. IF multiple doctors have equal workload, THEN THE Doctor_Assignment_Service SHALL assign the appointment to the doctor whose name appears first alphabetically
5. WHEN a doctor is assigned, THE Appointment_System SHALL store the doctor ID in the appointment record
6. THE Doctor_Assignment_Service SHALL exclude doctors marked as inactive from assignment consideration
7. THE Doctor_Assignment_Service SHALL exclude doctors with day-off records for the requested date from assignment consideration

### Requirement 5: Available Slot Query with Capacity Awareness

**User Story:** As a Patient, I want to see only time slots that have available capacity, so that I do not attempt to book unavailable appointments.

#### Acceptance Criteria

1. WHEN a Patient queries available slots for a date, THE Capacity_Controller SHALL generate all 30-minute time slots from 00:00 to 23:30 (48 total slots)
2. WHEN evaluating each time slot, THE Capacity_Controller SHALL identify which shift covers that time period
3. WHEN evaluating each time slot, THE Capacity_Controller SHALL count active doctors in that shift who do not have day-off records for that date
4. WHEN evaluating each time slot, THE Capacity_Controller SHALL count existing appointments for that slot across all available doctors
5. IF the number of existing appointments equals or exceeds the total capacity for that slot, THEN THE Capacity_Controller SHALL exclude that slot from the available slots response
6. THE Capacity_Controller SHALL return only time slots with at least one available appointment space

### Requirement 6: Time Slot Filtering Based on Current Time

**User Story:** As a Patient, I want the system to hide time slots that have already passed, so that I can only book future appointments.

#### Acceptance Criteria

1. WHEN a Patient queries available slots for today's date, THE Slot_Selector SHALL retrieve the current system time
2. WHEN the current time is between two 30-minute slots, THE Slot_Selector SHALL exclude all slots with start times before or equal to the current slot
3. WHEN the current time is 11:15, THE Slot_Selector SHALL show slots starting from 11:30 onward
4. WHEN a Patient queries available slots for a future date, THE Slot_Selector SHALL show all slots regardless of current time
5. THE Slot_Selector SHALL combine time-based filtering with capacity-based filtering to show only valid, future, available slots

### Requirement 7: Frontend Appointment Booking Without Doctor Selection

**User Story:** As a Patient, I want to book an appointment by selecting only date and time, so that I do not need to choose a doctor manually.

#### Acceptance Criteria

1. THE Slot_Selector SHALL display a date picker that prevents selection of past dates
2. WHEN a Patient selects a date, THE Slot_Selector SHALL query the backend for available time slots for that date
3. THE Slot_Selector SHALL display only time slots returned by the backend (capacity-aware and time-filtered)
4. THE Slot_Selector SHALL display time slots in 12-hour format with AM/PM indicators
5. WHEN a Patient submits the appointment form, THE Slot_Selector SHALL send the request without a doctor ID parameter
6. THE Appointment_System SHALL invoke the Doctor_Assignment_Service to assign a doctor automatically before creating the appointment

### Requirement 8: Appointment Confirmation with Assigned Doctor Information

**User Story:** As a Patient, I want to see which doctor was assigned to my appointment after booking, so that I know who will provide my care.

#### Acceptance Criteria

1. WHEN an appointment is successfully created, THE Appointment_System SHALL return the assigned doctor ID in the response
2. THE Slot_Selector SHALL query the doctor's full name and specialty using the doctor ID
3. THE Slot_Selector SHALL display a confirmation message showing the appointment date, time, assigned doctor name, and doctor specialty
4. WHEN a Patient views their appointment history, THE Appointment_System SHALL include the assigned doctor's name and specialty for each appointment
5. THE Appointment_System SHALL display appointment status (SCHEDULED, ACTIVE, COMPLETED, CANCELLED) alongside doctor information

### Requirement 9: Database Schema for Doctor and Availability

**User Story:** As a system architect, I want a normalized database schema for doctors and availability, so that the system can efficiently query and manage doctor schedules.

#### Acceptance Criteria

1. THE Shift_Manager SHALL persist doctor records in a `doctors` table with columns: id, name, specialty, shift_start, shift_end, status
2. THE Shift_Manager SHALL persist day-off records in a `doctor_availability` table with columns: doctor_id, date, is_available
3. THE Shift_Manager SHALL create a foreign key constraint from `doctor_availability.doctor_id` to `doctors.id`
4. THE Appointment_System SHALL create an index on `appointments.doctor_id` to optimize workload queries
5. THE Appointment_System SHALL create a composite index on `appointments(doctor_id, appointment_date)` to optimize daily workload counting

### Requirement 10: Redis Cache Integration for Slot Availability

**User Story:** As a system architect, I want to integrate doctor capacity checks with the existing Redis slot cache, so that slot availability reflects both time-based and capacity-based constraints.

#### Acceptance Criteria

1. WHEN the Capacity_Controller checks slot availability, THE Capacity_Controller SHALL query the Redis cache for occupied slots using the existing key pattern `appointment:slots:{doctorId}:{date}`
2. WHEN calculating total capacity for a time slot, THE Capacity_Controller SHALL aggregate occupied slot counts across all doctors in the relevant shift
3. WHEN a new appointment is created, THE Appointment_System SHALL reserve the slot in Redis using the assigned doctor's ID
4. WHEN an appointment is cancelled, THE Appointment_System SHALL release the slot in Redis for the assigned doctor
5. THE Capacity_Controller SHALL use Redis SET operations to ensure atomic slot reservation and prevent race conditions

### Requirement 11: Doctor Service API Endpoints

**User Story:** As a frontend developer, I want REST API endpoints to manage doctors and query availability, so that I can build the administrative and patient-facing interfaces.

#### Acceptance Criteria

1. THE Shift_Manager SHALL expose a `POST /api/clinical/doctors` endpoint to create doctor records (Administrator role required)
2. THE Shift_Manager SHALL expose a `PUT /api/clinical/doctors/{id}` endpoint to update doctor records (Administrator role required)
3. THE Shift_Manager SHALL expose a `DELETE /api/clinical/doctors/{id}` endpoint to deactivate doctors (Administrator role required)
4. THE Shift_Manager SHALL expose a `POST /api/clinical/doctors/{id}/days-off` endpoint to mark dates as unavailable (Administrator role required)
5. THE Shift_Manager SHALL expose a `GET /api/clinical/doctors` endpoint to list all active doctors
6. THE Capacity_Controller SHALL modify the existing `GET /api/clinical/appointments/slots` endpoint to remove the `doctorId` parameter and return capacity-aware slots for all doctors
7. THE Appointment_System SHALL modify the existing `POST /api/clinical/appointments` endpoint to make the `doctorId` parameter optional and invoke automatic assignment when omitted

### Requirement 12: Parser and Serializer for Doctor Shift Configuration

**User Story:** As a system administrator, I want to import and export doctor shift configurations in JSON format, so that I can bulk-load doctor schedules and back up configurations.

#### Acceptance Criteria

1. WHEN a JSON configuration file is provided, THE Shift_Configuration_Parser SHALL parse it into Doctor objects with shift schedules
2. WHEN an invalid JSON configuration file is provided, THE Shift_Configuration_Parser SHALL return a descriptive error message indicating the validation failure
3. THE Shift_Configuration_Printer SHALL format Doctor objects with shift schedules into valid JSON configuration files
4. FOR ALL valid Doctor configuration objects, parsing then printing then parsing SHALL produce an equivalent object (round-trip property)
5. THE Shift_Configuration_Parser SHALL validate that shift times are in HH:mm format and form valid 8-hour periods
6. THE Shift_Configuration_Parser SHALL validate that doctor IDs are unique within a configuration file

