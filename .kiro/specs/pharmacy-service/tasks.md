# Implementation Plan: Pharmacy Service

## Overview

**Architecture**: MVC | **Port**: 8085 | **Schema**: pharmacy_schema  
**Estimated Time**: 5-7 hours

## Tasks

- [x] 1. Project setup
  - Maven project with Spring Boot 3.2.4, Java 17
  - Dependencies: Spring Web, Spring Data JPA, PostgreSQL, Eureka Client, Validation, Lombok, Actuator
  - application.yml and application-docker.yml
  - _Requirements: FR4_

- [x] 2. Domain model
  - [x] 2.1 Create PrescriptionStatus enum (PENDING, DISPENSED, CANCELLED)
  - [x] 2.2 Implement Prescription entity
  - [x] 2.3 Implement Medication entity (name, unit, currentStock, minStock, active)
  - [x] 2.4 Implement Dispensation entity
  - _Requirements: FR1, FR2, FR3_

- [x] 3. Repository layer
  - [x] 3.1 PrescriptionRepository (findByStatus, findByPatientId, findByPrescriptionCode)
  - [x] 3.2 MedicationRepository (findByNameIgnoreCase, findByCurrentStockLessThan, findByActiveTrue)
  - [x] 3.3 DispensationRepository (findByPatientId)
  - _Requirements: FR1, FR2, FR3_

- [x] 4. DTOs
  - [x] 4.1 Request: PrescriptionNotificationRequest, MedicationRequest, StockUpdateRequest
  - [x] 4.2 Response: PrescriptionResponse, MedicationResponse, DispensationResponse
  - _Requirements: FR4_

- [x] 5. Custom exceptions and GlobalExceptionHandler
  - PrescriptionNotFoundException → 404
  - InsufficientStockException → 409
  - InvalidPrescriptionStatusException → 409
  - UnauthorizedException → 403
  - All messages in Spanish
  - _Requirements: BR5_

- [x] 6. Service layer
  - [x] 6.1 PrescriptionService.receivePrescription() - save with PENDING status
  - [x] 6.2 PrescriptionService.getPrescriptions() - list with status filter
  - [x] 6.3 PrescriptionService.getByCode() - find by prescriptionCode
  - [x] 6.4 PrescriptionService.dispense() - @Transactional: validate stock, deduct, create dispensation, update status
  - [x] 6.5 PrescriptionService.getPatientPrescriptions() - validate PATIENT role access
  - [x] 6.6 InventoryService.getMedications() - list active medications
  - [x] 6.7 InventoryService.addMedication() - add to catalog
  - [x] 6.8 InventoryService.updateStock() - update currentStock (never negative)
  - [x] 6.9 InventoryService.getLowStockMedications() - stock < minStock
  - _Requirements: FR1, FR2, FR3, BR1, BR2, BR3, BR6_

  - [ ]* 6.10 Unit tests for PrescriptionService
    - Test dispense with sufficient stock → success
    - Test dispense with insufficient stock → InsufficientStockException
    - Test dispense non-PENDING prescription → InvalidPrescriptionStatusException
    - Test PATIENT role restriction
    - _Requirements: BR1, BR2, BR3, BR6_

  - [ ]* 6.11 Unit tests for InventoryService
    - Test stock update never goes negative
    - Test low stock alert threshold
    - _Requirements: BR6_

- [x] 7. Controller layer
  - [x] 7.1 PrescriptionController (POST notify, GET list, GET by code, PUT dispense, GET by patient)
  - [x] 7.2 MedicationController (GET list, POST add, PUT stock, GET low-stock)
  - _Requirements: FR4_

  - [ ]* 7.3 Integration tests with MockMvc
    - Test dispense flow end-to-end
    - Test insufficient stock → 409
    - Test PATIENT access restriction
    - _Requirements: FR4_

- [x] 8. Database schema migration
  - V1__create_pharmacy_schema.sql with seed data for common medications
  - _Requirements: FR2_

- [x] 9. Docker and deployment
  - [x] 9.1 Dockerfile (port 8085)
  - [x] 9.2 Update docker-compose.yml
  - _Requirements: FR4_

- [x] 10. README.md

- [x] 11. Final checkpoint
  - mvn clean test, docker build, verify Eureka registration

## Notes
- @Transactional on dispense() is critical — stock deduction and dispensation must be atomic
- Stock can never go negative (constraint in service layer + DB CHECK constraint)
- CERO JOINs with other schemas
- Estimated: 5-7 hours
