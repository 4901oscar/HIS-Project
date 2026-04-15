# Requirements Document

## Introduction

This document specifies the requirements for implementing online appointment scheduling and payment functionality for the MedFlow HIS patient service. The system will provide patients with a digital channel to schedule general medical appointments and complete payment transactions from any internet-enabled device, reducing wait times at the clinic on the day of their visit.

This implementation follows the MedFlow HIS business rules (RN01-RN26) and is scoped for a university project with simulated payment processing. The system does NOT include appointment cancellation or rescheduling functionality.

## Glossary

- **Patient**: An external user accessing the clinic website to schedule an appointment
- **Appointment_System**: The web portal and backend services that manage appointment scheduling
- **Payment_Gateway**: The simulated payment platform that processes mock card transactions for university project purposes
- **QR_Generator**: The system component that generates unique QR codes for confirmed appointments
- **Appointment_Slot**: A specific date and time available for scheduling an appointment
- **Appointment_Confirmation**: The system response containing appointment details and QR code sent to the patient
- **Doctor**: A medical professional with assigned work shifts who can attend appointments
- **DPI**: Documento Personal de Identificación (Personal Identification Document) - Guatemala national ID
- **NIT**: Número de Identificación Tributaria (Tax Identification Number) - Guatemala tax ID

## Requirements

### Requirement 1: Web Portal Access and Performance (RN01)

**User Story:** As a patient, I want to access the clinic website from any device with fast loading times, so that I can schedule appointments conveniently.

#### Acceptance Criteria

1. THE Appointment_System SHALL display a homepage with available scheduling options
2. THE Appointment_System SHALL be accessible from desktop computers, tablets, and mobile phones
3. THE Appointment_System SHALL maintain availability 24 hours per day, 7 days per week
4. THE Appointment_System SHALL display appointment availability within 2 seconds of page load (RN01)
5. THE Appointment_System SHALL display service information within 2 seconds of page load (RN01)
6. THE Appointment_System SHALL display pricing information within 2 seconds of page load (RN01)

### Requirement 2: Available Appointment Display (RN23)

**User Story:** As a patient, I want to see available appointment dates and times based on doctor availability, so that I can choose a slot that fits my schedule.

#### Acceptance Criteria

1. WHEN a patient selects the schedule appointment option, THE Appointment_System SHALL display available appointment slots
2. THE Appointment_System SHALL calculate total available appointments based on the number of doctors assigned to each work shift (RN23)
3. THE Appointment_System SHALL limit each doctor to a maximum of 2 appointments per hour (RN23)
4. THE Appointment_System SHALL display appointment slots organized by date and time
5. THE Appointment_System SHALL display only appointment slots that are currently available for booking
6. THE Appointment_System SHALL refresh availability data within 5 seconds of a slot being booked by another patient

### Requirement 3: Patient Information Collection (RN02, RN05, RN26)

**User Story:** As a patient, I want to provide my personal information for appointment booking, so that the clinic can identify me and contact me.

#### Acceptance Criteria

1. THE Appointment_System SHALL collect the following mandatory fields (RN26):
   - DPI (Documento Personal de Identificación)
   - NIT (Número de Identificación Tributaria)
   - Primer Nombre (First Name)
   - Primer Apellido (First Last Name)
   - Fecha Nacimiento (Birth Date)
   - Teléfono (Phone)
   - Correo (Email)
   - Síntomas/Motivo de consulta (Symptoms/Reason for consultation)
   - Día y hora de la cita (Day and time of appointment)

2. THE Appointment_System SHALL collect the following optional fields (RN26):
   - Segundo Nombre (Second Name)
   - Segundo Apellido (Second Last Name)

3. WHEN a patient enters a DPI, THE Appointment_System SHALL validate it contains exactly 13 numeric digits (RN02)
4. WHEN a patient enters a NIT, THE Appointment_System SHALL validate it is either "C/F" or a valid legal format number (RN02)
5. WHEN a patient attempts to save with incomplete mandatory fields, THE Appointment_System SHALL display the message "Por favor complete todos los campos obligatorios" (RN05)
6. WHEN a patient attempts to save with incomplete mandatory fields, THE Appointment_System SHALL prevent form submission (RN05)

### Requirement 4: Appointment Selection and Validation

**User Story:** As a patient, I want to select my preferred appointment date and time, so that I can book a slot that works for me.

#### Acceptance Criteria

1. WHEN a patient selects an appointment slot, THE Appointment_System SHALL validate the selected date and time are still available
2. WHEN a patient selects an appointment slot, THE Appointment_System SHALL validate all required patient information is provided
3. IF an appointment slot is no longer available, THEN THE Appointment_System SHALL display an error message and return the patient to the available slots display
4. THE Appointment_System SHALL support only general appointment types (no specialty appointments)

### Requirement 5: Appointment Confirmation Review

**User Story:** As a patient, I want to review my appointment details before payment, so that I can verify the information is correct.

#### Acceptance Criteria

1. WHEN a patient completes appointment selection, THE Appointment_System SHALL display a summary of the appointment details
2. THE Appointment_System SHALL display patient name, selected date, selected time, and appointment type in the summary
3. WHEN a patient requests to change appointment details, THE Appointment_System SHALL return to the appointment slot selection screen
4. WHEN a patient confirms the appointment details, THE Appointment_System SHALL proceed to the payment screen

### Requirement 6: Online Payment Processing (RN25)

**User Story:** As a patient, I want to pay for my appointment online using a simulated payment system, so that I can complete my booking for this university project.

#### Acceptance Criteria

1. WHEN a patient confirms appointment details, THE Appointment_System SHALL display the payment screen with the appointment cost
2. THE Appointment_System SHALL use a mock payment gateway that simulates credit card and debit card payment processing
3. WHEN a patient submits payment information, THE Payment_Gateway SHALL simulate transaction processing
4. THE Payment_Gateway SHALL randomly simulate approved and rejected transactions for testing purposes
5. WHEN the Payment_Gateway approves a transaction, THE Appointment_System SHALL mark the appointment as paid and confirmed
6. IF the Payment_Gateway rejects a transaction, THEN THE Appointment_System SHALL stop the appointment booking process (RN25)
7. IF payment cannot be completed, THEN THE Appointment_System SHALL release the appointment slot (RN25)
8. IF payment is absent, THEN THE Appointment_System SHALL prevent appointment confirmation (RN25)
9. THE Appointment_System SHALL NOT provide clinic service access without confirmed payment (RN25)

### Requirement 7: QR Code Generation and Delivery (RN24)

**User Story:** As a patient, I want to receive a QR code for my appointment with time-based validity, so that I can check in at the clinic during the valid window.

#### Acceptance Criteria

1. WHEN payment is confirmed, THE QR_Generator SHALL generate a unique QR code for the appointment
2. THE QR_Generator SHALL encode the appointment ID, patient name, appointment date, and appointment time in the QR code
3. THE QR_Generator SHALL set the QR code validity period to start 1 hour before the appointment time (RN24)
4. THE QR_Generator SHALL set the QR code validity period to end 30 minutes after the appointment time (RN24)
5. WHEN a QR code is generated, THE Appointment_System SHALL send the QR code to the patient's email address
6. WHEN a QR code is generated, THE Appointment_System SHALL display the QR code on the confirmation screen
7. THE Appointment_System SHALL send the appointment confirmation email within 30 seconds of payment confirmation
8. THE Appointment_System SHALL include the QR code validity window in the confirmation email (RN24)

### Requirement 8: Appointment Confirmation

**User Story:** As a patient, I want to receive confirmation of my appointment, so that I have a record of my scheduled visit.

#### Acceptance Criteria

1. WHEN payment is confirmed, THE Appointment_System SHALL display a confirmation screen with appointment details
2. THE Appointment_Confirmation SHALL include patient name, appointment date, appointment time, appointment type, and QR code
3. THE Appointment_Confirmation SHALL include the QR code validity window (1 hour before to 30 minutes after appointment time)
4. THE Appointment_System SHALL send a confirmation email containing all appointment details and the QR code
5. THE Appointment_System SHALL register the appointment status as paid and pending attention in the system

### Requirement 9: Payment Failure Handling (RN25)

**User Story:** As a patient, I want clear feedback when payment fails, so that I understand why my appointment was not confirmed.

#### Acceptance Criteria

1. WHEN payment is rejected, THE Appointment_System SHALL display the reason for rejection if provided by the Payment_Gateway
2. WHEN payment is rejected, THE Appointment_System SHALL display a message indicating the appointment booking has been stopped (RN25)
3. WHEN payment is rejected, THE Appointment_System SHALL release the reserved appointment slot
4. WHEN payment cannot be completed, THE Appointment_System SHALL prevent appointment confirmation (RN25)
5. THE Appointment_System SHALL allow the patient to start a new appointment booking process after payment failure

### Requirement 10: System Availability and Performance (RN01)

**User Story:** As a patient, I want the appointment system to be fast and reliable, so that I can complete my booking without delays or errors.

#### Acceptance Criteria

1. THE Appointment_System SHALL respond to user interactions within 2 seconds under normal load conditions (RN01)
2. THE Appointment_System SHALL maintain 99.5% uptime during business hours (6:00 AM to 10:00 PM local time)
3. WHEN system load exceeds capacity, THE Appointment_System SHALL display a queue message with estimated wait time
4. THE Appointment_System SHALL support at least 100 concurrent users scheduling appointments simultaneously

### Requirement 11: Data Validation and Error Handling (RN02, RN05)

**User Story:** As a patient, I want clear error messages when I make mistakes, so that I can correct them and complete my booking.

#### Acceptance Criteria

1. WHEN a patient enters invalid data, THE Appointment_System SHALL display a specific error message indicating which field is invalid
2. WHEN a patient enters an invalid email address, THE Appointment_System SHALL validate the format and display an error message
3. WHEN a patient enters an invalid phone number, THE Appointment_System SHALL validate the format and display an error message
4. WHEN a patient enters an invalid DPI, THE Appointment_System SHALL validate it contains exactly 13 numeric digits and display an error message (RN02)
5. WHEN a patient enters an invalid NIT, THE Appointment_System SHALL validate it is either "C/F" or valid legal format and display an error message (RN02)
6. WHEN validation fails, THE Appointment_System SHALL preserve all valid data entered by the patient
7. WHEN mandatory fields are incomplete, THE Appointment_System SHALL display "Por favor complete todos los campos obligatorios" (RN05)


### Requirement 12: Scope Limitations

**User Story:** As a system administrator, I want to understand the scope boundaries of this implementation, so that I know what functionality is NOT included.

#### Acceptance Criteria

1. THE Appointment_System SHALL NOT provide appointment cancellation functionality
2. THE Appointment_System SHALL NOT provide appointment rescheduling functionality
3. THE Appointment_System SHALL NOT support specialty appointment types (only general appointments)
4. THE Payment_Gateway SHALL use simulated/mock payment processing for university project purposes

## Business Rules Reference

This requirements document implements the following MedFlow HIS business rules:

- **RN01**: Fast information loading - Web portal displays appointment availability, services, and prices with fast loading times (≤2 seconds)
- **RN02**: ID document format - DPI must be exactly 13 numeric digits; NIT can be "C/F" or valid legal format
- **RN05**: Mandatory form fields - System prevents saving with empty mandatory fields and shows specific error message
- **RN23**: Appointment availability - Total appointments based on available doctors per shift, maximum 2 appointments per doctor per hour
- **RN24**: QR code availability - QR code valid from 1 hour before appointment to 30 minutes after appointment
- **RN25**: Payment enforcement - System stops service if payment cannot be completed, is rejected, or is absent
- **RN26**: Form fields - Specific mandatory and optional fields for appointment scheduling (DPI, NIT, names, birth date, phone, email, symptoms, appointment time)
