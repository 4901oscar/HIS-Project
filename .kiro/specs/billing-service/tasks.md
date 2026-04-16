# Implementation Plan: Billing Service

## Overview

**Architecture**: MVC | **Port**: 8086 | **Schema**: billing_schema  
**Estimated Time**: 5-7 hours

## Tasks

- [ ] 1. Project setup
  - Maven project with Spring Boot 3.2.4, Java 17
  - Dependencies: Spring Web, Spring Data JPA, PostgreSQL, Eureka Client, Validation, Lombok, Actuator
  - application.yml and application-docker.yml
  - _Requirements: FR4_

- [ ] 2. Domain model
  - [ ] 2.1 Create InvoiceStatus enum (PENDING, PAID, CANCELLED)
  - [ ] 2.2 Create ChargeType enum (CONSULTATION, LABORATORY, MEDICATION, OTHER)
  - [ ] 2.3 Create PaymentMethod enum (CASH, CARD, TRANSFER)
  - [ ] 2.4 Implement Invoice entity with @OneToMany charges relationship
  - [ ] 2.5 Implement Charge entity with @ManyToOne invoice relationship
  - [ ] 2.6 Implement Payment entity
  - _Requirements: FR1, FR2, FR3_

- [ ] 3. Repository layer
  - [ ] 3.1 InvoiceRepository (findByPatientId, findByStatus, findByInvoiceNumber)
  - [ ] 3.2 ChargeRepository (findByInvoiceId)
  - [ ] 3.3 PaymentRepository (findByInvoiceId)
  - _Requirements: FR1, FR2, FR3_

- [ ] 4. DTOs
  - [ ] 4.1 Request: CreateInvoiceRequest (with nested ChargeRequest), ProcessPaymentRequest, ApplyDiscountRequest
  - [ ] 4.2 Response: InvoiceResponse (with charges), PaymentResponse (with change amount)
  - _Requirements: FR4_

- [ ] 5. Custom exceptions and GlobalExceptionHandler
  - InvoiceNotFoundException → 404
  - InvalidInvoiceStatusException → 409
  - InsufficientPaymentException → 400
  - UnauthorizedException → 403
  - All messages in Spanish
  - _Requirements: BR6_

- [ ] 6. Service layer
  - [ ] 6.1 InvoiceService.createInvoice() - generate invoice number, calculate subtotals and total
  - [ ] 6.2 InvoiceService.getInvoices() - list with status filter
  - [ ] 6.3 InvoiceService.getById() - find by ID
  - [ ] 6.4 InvoiceService.applyDiscount() - validate PENDING, recalculate total (never negative)
  - [ ] 6.5 InvoiceService.cancelInvoice() - validate PENDING → CANCELLED
  - [ ] 6.6 InvoiceService.getPatientInvoices() - validate PATIENT role access
  - [ ] 6.7 PaymentService.processPayment() - @Transactional: validate status, validate amount, create payment, update invoice
  - _Requirements: FR1, FR2, FR3, BR1, BR2, BR3, BR4, BR7_

  - [ ]* 6.8 Unit tests for InvoiceService
    - Test total calculation = sum of charges - discount
    - Test total never negative after discount
    - Test status transitions (valid and invalid)
    - Test PATIENT role restriction
    - _Requirements: BR1, BR2, BR4, BR7_

  - [ ]* 6.9 Unit tests for PaymentService
    - Test payment with exact amount → change = 0
    - Test payment with excess amount → correct change
    - Test payment with insufficient amount → InsufficientPaymentException
    - Test payment on non-PENDING invoice → InvalidInvoiceStatusException
    - _Requirements: BR1, BR3_

- [ ] 7. Controller layer
  - [ ] 7.1 InvoiceController (POST create, GET list, GET by id, POST pay, PUT discount, DELETE cancel, GET by patient)
  - _Requirements: FR4_

  - [ ]* 7.2 Integration tests with MockMvc
    - Test create invoice and verify total calculation
    - Test payment flow end-to-end
    - Test insufficient payment → 400
    - Test PATIENT access restriction
    - _Requirements: FR4_

- [ ] 8. Invoice number generator
  - Implement generateInvoiceNumber() → INV-YYYYMMDD-XXXX (sequential per day)
  - _Requirements: FR1_

  - [ ]* 8.1 Property test: Invoice numbers are unique
    - Generate multiple invoices on same day, verify all numbers unique
    - _Requirements: FR1_

- [ ] 9. Database schema migration
  - V1__create_billing_schema.sql with CHECK constraints (subtotal >= 0, total >= 0)
  - _Requirements: FR1_

- [ ] 10. Docker and deployment
  - [ ] 10.1 Dockerfile (port 8086)
  - [ ] 10.2 Update docker-compose.yml
  - _Requirements: FR4_

- [ ] 11. README.md

- [ ] 12. Final checkpoint
  - mvn clean test, docker build, verify Eureka registration

## Notes
- @Transactional on processPayment() is critical
- Use BigDecimal for all monetary values (never double/float)
- Total can never be negative (service layer + DB CHECK constraint)
- CERO JOINs with other schemas
- Estimated: 5-7 hours
