# Implementation Plan: Lab Service

## Overview

**Architecture**: MVC | **Port**: 8084 | **Schema**: lab_schema  
**Estimated Time**: 4-6 hours

## Tasks

- [ ] 1. Project setup
  - Maven project with Spring Boot 3.2.4, Java 17
  - Dependencies: Spring Web, Spring Data JPA, PostgreSQL, Eureka Client, Validation, Lombok, Actuator
  - application.yml and application-docker.yml
  - _Requirements: FR4_

- [ ] 2. Domain model
  - [ ] 2.1 Create OrderStatus enum (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)
  - [ ] 2.2 Implement LabOrder entity with all fields and schema annotation
  - [ ] 2.3 Implement Sample entity
  - [ ] 2.4 Implement LabResult entity
  - [ ] 2.5 Create StringListConverter for testNames JSON column
  - _Requirements: FR1, FR2, FR3_

- [ ] 3. Repository layer
  - [ ] 3.1 LabOrderRepository (findByStatus, findByPatientId, findByOrderCode)
  - [ ] 3.2 SampleRepository (findByOrderId)
  - [ ] 3.3 LabResultRepository (findByPatientIdOrderByUploadedAtDesc)
  - _Requirements: FR1, FR2, FR3_

- [ ] 4. DTOs
  - [ ] 4.1 Request DTOs: LabOrderNotificationRequest, CollectSampleRequest, UploadResultRequest
  - [ ] 4.2 Response DTOs: LabOrderResponse, LabResultResponse
  - _Requirements: FR4_

- [ ] 5. Custom exceptions and GlobalExceptionHandler
  - LabOrderNotFoundException → 404
  - InvalidOrderStatusException → 409
  - UnauthorizedException → 403
  - All messages in Spanish
  - _Requirements: BR5_

- [ ] 6. Service layer
  - [ ] 6.1 LabOrderService.receiveOrder() - save incoming order with PENDING status
  - [ ] 6.2 LabOrderService.getOrders() - list with optional status filter
  - [ ] 6.3 LabOrderService.collectSample() - validate PENDING→IN_PROGRESS, create Sample
  - [ ] 6.4 LabResultService.uploadResult() - validate IN_PROGRESS→COMPLETED, save LabResult
  - [ ] 6.5 LabResultService.getPatientResults() - validate PATIENT role access
  - _Requirements: FR1, FR2, FR3, BR1, BR2, BR3_

  - [ ]* 6.6 Unit tests for LabOrderService
    - Test status transitions (valid and invalid)
    - Test PATIENT role restriction
    - _Requirements: BR1, BR2, BR3_

- [ ] 7. Controller layer
  - [ ] 7.1 LabOrderController (POST notify, GET orders, PUT collect, PUT result)
  - [ ] 7.2 LabResultController (GET results by patientId)
  - _Requirements: FR4_

  - [ ]* 7.3 Integration tests with MockMvc
    - Test all endpoints with valid/invalid data
    - Test status transition errors → 409
    - _Requirements: FR4_

- [ ] 8. Database schema migration
  - Create V1__create_lab_schema.sql (idempotent)
  - _Requirements: FR1_

- [ ] 9. Docker and deployment
  - [ ] 9.1 Dockerfile (eclipse-temurin:17-jre-alpine, port 8084)
  - [ ] 9.2 Update docker-compose.yml
  - _Requirements: FR4_

- [ ] 10. README.md
  - Overview, endpoints, configuration, run instructions
  - _Requirements: Overall_

- [ ] 11. Final checkpoint
  - mvn clean test, docker build, verify Eureka registration

## Notes
- MVP: PDF stored in filesystem (path saved in DB). Future: S3/MinIO.
- CERO JOINs with other schemas
- All error messages in Spanish
- Estimated: 4-6 hours
