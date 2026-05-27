# Implementation Plan: Clinic Management CRUD

## Overview

This implementation plan breaks down the Clinic Management CRUD feature into discrete, actionable coding tasks. The implementation follows hexagonal architecture with clear separation between domain, application, and infrastructure layers. Each task builds incrementally on previous work, with checkpoints to ensure quality and correctness.

The implementation covers:
- Database schema and migration
- Domain model with business logic
- Repository layer with JPA
- Application services and use cases
- REST API endpoints
- Frontend service layer
- React UI components with validation

## Tasks

### Phase 1: Database and Domain Foundation

- [x] 1. Create database migration for clinics table
  - Create Flyway migration script in `backend-services/clinical-service/src/main/resources/db/migration/`
  - Define table `clinical_schema.clinics` with columns: id (UUID), codigo (TEXT with unique constraint), nombre (TEXT), descripcion (TEXT), estado (ENUM: ACTIVE, INACTIVE, DELETED), created_at (TIMESTAMP), created_by (TEXT), updated_at (TIMESTAMP), updated_by (TEXT)
  - Add unique constraint on `codigo` column
  - Add indexes on `codigo` and `estado` columns for query performance
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8, 9.9_

- [x] 2. Implement domain model and value objects
  - [x] 2.1 Create Clinic domain model in `domain/model/Clinic.java`
    - Define Clinic aggregate root with fields: id, codigo, nombre, descripcion, estado, createdAt, createdBy, updatedAt, updatedBy
    - Implement factory method `create()` for new clinic creation with validation
    - Implement method `update()` for updating clinic fields with validation
    - Implement method `delete()` for soft delete (sets estado to DELETED)
    - Add validation logic for codigo (numeric only), nombre (alphanumeric), descripcion (alphanumeric)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.6, 1.7, 3.2, 3.4, 3.5, 3.6, 4.1, 11.3_
  
  - [x] 2.2 Create ClinicStatus enum in `domain/model/ClinicStatus.java`
    - Define enum values: ACTIVE, INACTIVE, DELETED
    - _Requirements: 1.6, 2.5, 9.6_
  
  - [x] 2.3 Create domain exceptions in `domain/exception/`
    - Create `ClinicNotFoundException.java` for missing clinic errors
    - Create `DuplicateClinicCodeException.java` for unique constraint violations
    - Create `InvalidClinicDataException.java` for validation errors
    - _Requirements: 1.5, 1.9, 3.3, 3.9, 4.4_

- [ ] 3. Checkpoint - Verify domain model compiles
  - Ensure all domain classes compile without errors
  - Ensure domain model has no dependencies on infrastructure layer
  - Ask the user if questions arise

### Phase 2: Repository Layer

- [x] 4. Implement repository interfaces and adapters
  - [x] 4.1 Create ClinicRepository interface in `domain/repository/ClinicRepository.java`
    - Define method signatures: `save(Clinic)`, `findById(UUID)`, `findAll()`, `findByEstado(ClinicStatus)`, `existsByCodigo(String)`, `findByCodigo(String)`
    - _Requirements: 1.10, 2.1, 2.2, 3.1, 4.1, 11.4_
  
  - [x] 4.2 Create JPA entity in `infrastructure/persistence/entity/ClinicEntity.java`
    - Map to `clinical_schema.clinics` table
    - Define JPA annotations for all columns
    - Add unique constraint on codigo field
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 9.8_
  
  - [x] 4.3 Create Spring Data JPA repository in `infrastructure/persistence/repository/ClinicJpaRepository.java`
    - Extend `JpaRepository<ClinicEntity, UUID>`
    - Add custom query methods: `findByEstado(ClinicStatus)`, `existsByCodigo(String)`, `findByCodigo(String)`
    - _Requirements: 2.2, 2.5, 11.4_
  
  - [x] 4.4 Create repository adapter in `infrastructure/persistence/adapter/ClinicRepositoryAdapter.java`
    - Implement `ClinicRepository` interface
    - Inject `ClinicJpaRepository`
    - Implement mapper methods between `Clinic` domain model and `ClinicEntity`
    - Handle unique constraint violations and throw `DuplicateClinicCodeException`
    - _Requirements: 1.5, 3.3, 11.2, 11.4_

- [ ] 5. Checkpoint - Verify repository layer compiles
  - Ensure all repository classes compile without errors
  - Ensure repository adapter properly maps between domain and persistence layers
  - Ask the user if questions arise

### Phase 3: Application Layer

- [x] 6. Implement use cases and services
  - [x] 6.1 Create CreateClinicUseCase in `application/usecase/CreateClinicUseCase.java`
    - Inject `ClinicRepository`
    - Validate input data (required fields, format validation)
    - Check for duplicate codigo using `existsByCodigo()`
    - Create Clinic domain model using factory method
    - Save clinic and return created entity
    - Extract username from security context for audit fields
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.9, 1.10, 5.1, 5.2, 5.3, 5.4, 5.6, 8.1, 8.2_
  
  - [x] 6.2 Create ListClinicsUseCase in `application/usecase/ListClinicsUseCase.java`
    - Inject `ClinicRepository`
    - Implement method to list all clinics
    - Implement method to filter by estado
    - Sort results by createdAt descending
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_
  
  - [x] 6.3 Create UpdateClinicUseCase in `application/usecase/UpdateClinicUseCase.java`
    - Inject `ClinicRepository`
    - Find existing clinic by ID, throw `ClinicNotFoundException` if not found
    - Validate input data (format validation)
    - Check for duplicate codigo if codigo is being changed
    - Update clinic using domain model method
    - Save updated clinic
    - Extract username from security context for audit fields
    - Preserve original createdAt and createdBy values
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.9, 3.10, 5.1, 5.2, 5.3, 5.4, 5.6, 8.3, 8.4_
  
  - [x] 6.4 Create DeleteClinicUseCase in `application/usecase/DeleteClinicUseCase.java`
    - Inject `ClinicRepository`
    - Find existing clinic by ID, throw `ClinicNotFoundException` if not found
    - Perform soft delete using domain model method
    - Save updated clinic with DELETED status
    - Extract username from security context for audit fields
    - _Requirements: 4.1, 4.2, 4.4, 4.5, 4.6, 8.3, 8.4_

- [ ] 7. Checkpoint - Verify application layer compiles
  - Ensure all use case classes compile without errors
  - Ensure use cases properly orchestrate domain logic
  - Ask the user if questions arise

### Phase 4: REST API Layer

- [x] 8. Implement DTOs and REST controllers
  - [x] 8.1 Create request/response DTOs in `infrastructure/rest/dto/`
    - Create `CreateClinicRequest.java` with fields: codigo, nombre, descripcion
    - Create `UpdateClinicRequest.java` with fields: codigo, nombre, descripcion, estado
    - Create `ClinicResponse.java` with all clinic fields including audit information
    - Add Jakarta validation annotations (@NotNull, @NotBlank, @Pattern) to request DTOs
    - _Requirements: 1.2, 1.3, 1.4, 1.9, 2.3, 3.2, 3.4, 3.5, 3.6, 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.3, 6.4, 10.9_
  
  - [x] 8.2 Create ClinicController in `infrastructure/rest/controller/ClinicController.java`
    - Inject all use cases (Create, List, Update, Delete)
    - Implement POST `/api/clinics` endpoint for creating clinics
    - Implement GET `/api/clinics` endpoint for listing clinics with optional estado query parameter
    - Implement PUT `/api/clinics/{id}` endpoint for updating clinics
    - Implement DELETE `/api/clinics/{id}` endpoint for deleting clinics
    - Add `@PreAuthorize("hasRole('ADMIN')")` to create, update, delete endpoints
    - Add `@PreAuthorize("isAuthenticated()")` to list endpoint
    - Return appropriate HTTP status codes (201, 200, 400, 403, 404)
    - Add exception handler for domain exceptions
    - _Requirements: 1.8, 3.8, 4.3, 5.5, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.10, 11.6_
  
  - [x] 8.3 Create global exception handler in `infrastructure/rest/exception/ClinicExceptionHandler.java`
    - Handle `ClinicNotFoundException` → return 404
    - Handle `DuplicateClinicCodeException` → return 400
    - Handle `InvalidClinicDataException` → return 400
    - Handle validation errors → return 400 with field details
    - Handle authorization errors → return 403
    - _Requirements: 5.5, 10.6, 10.7, 10.8_

- [ ] 9. Checkpoint - Verify REST layer compiles and service starts
  - Ensure all REST classes compile without errors
  - Start the clinical-service and verify it registers with Eureka
  - Verify endpoints are accessible through API Gateway
  - Ask the user if questions arise

### Phase 5: Frontend Service Layer

- [x] 10. Implement frontend API service
  - [x] 10.1 Create TypeScript types in `frontend-medflow/src/types/clinic.ts`
    - Define `Clinic` interface with all fields
    - Define `CreateClinicRequest` interface
    - Define `UpdateClinicRequest` interface
    - Define `ClinicStatus` enum (ACTIVE, INACTIVE, DELETED)
    - _Requirements: 2.3, 9.6_
  
  - [x] 10.2 Create clinic service in `frontend-medflow/src/services/clinicService.ts`
    - Implement `createClinic(data: CreateClinicRequest)` function
    - Implement `getClinics(estado?: ClinicStatus)` function
    - Implement `updateClinic(id: string, data: UpdateClinicRequest)` function
    - Implement `deleteClinic(id: string)` function
    - Use axios with proper error handling
    - Include JWT token in request headers
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.9_

- [ ] 11. Checkpoint - Verify frontend service layer compiles
  - Ensure TypeScript types and service compile without errors
  - Ask the user if questions arise

### Phase 6: Frontend UI Components

- [ ] 12. Implement clinic management components
  - [x] 12.1 Create ClinicList component in `frontend-medflow/src/components/ClinicList/ClinicList.tsx`
    - Display table of clinics with columns: codigo, nombre, descripcion, estado
    - Add filter dropdown for estado (All, ACTIVE, INACTIVE, DELETED)
    - Add "Create Clinic" button (visible only for ADMIN role)
    - Add "Edit" and "Delete" action buttons per row (visible only for ADMIN role)
    - Fetch clinics on component mount and when filter changes
    - Handle loading and error states
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.4_
  
  - [x] 12.2 Create ClinicForm component in `frontend-medflow/src/components/ClinicForm/ClinicForm.tsx`
    - Create reusable form for both create and edit modes
    - Add input fields: codigo, nombre, descripcion, estado (for edit mode only)
    - Implement frontend validation with real-time feedback
    - Validate codigo is numeric only using regex pattern
    - Validate nombre is alphanumeric only using regex pattern
    - Validate descripcion is alphanumeric only using regex pattern
    - Display validation error messages below each field
    - Disable submit button until all validations pass
    - Handle form submission for both create and update
    - Show success/error notifications after submission
    - _Requirements: 1.2, 1.3, 1.4, 1.9, 3.2, 3.4, 3.5, 3.6, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_
  
  - [x] 12.3 Create ClinicManagementPage in `frontend-medflow/src/pages/administrator/ClinicManagementPage.tsx`
    - Integrate ClinicList component
    - Integrate ClinicForm component in modal or separate view
    - Handle navigation between list and form views
    - Implement create clinic flow
    - Implement edit clinic flow
    - Implement delete clinic confirmation dialog
    - Add role-based rendering (ADMIN only page)
    - _Requirements: 1.8, 3.8, 4.3, 7.1, 7.2, 7.3_
  
  - [x] 12.4 Add routing for clinic management page
    - Add route in `frontend-medflow/src/App.tsx` for `/admin/clinics`
    - Protect route with ProtectedRoute component requiring ADMIN role
    - Add navigation link in admin menu/sidebar
    - _Requirements: 7.1, 7.2, 7.3_

- [ ] 13. Checkpoint - Verify frontend components render correctly
  - Ensure all React components compile without errors
  - Manually test component rendering in browser
  - Verify role-based access control works in UI
  - Ask the user if questions arise

### Phase 7: Integration and Testing

- [ ] 14. Integration and end-to-end wiring
  - [ ] 14.1 Verify API Gateway routing configuration
    - Ensure `/api/clinics/**` routes to clinical-service
    - Verify JWT validation is enabled for all clinic endpoints
    - Verify role-based access control is enforced
    - _Requirements: 7.1, 7.2, 7.3, 7.5, 11.9_
  
  - [ ] 14.2 Test complete create clinic flow
    - Test frontend form validation
    - Test backend validation
    - Test successful clinic creation
    - Test duplicate codigo error handling
    - Test unauthorized access (non-ADMIN user)
    - Verify audit fields are populated correctly
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 8.1, 8.2_
  
  - [ ] 14.3 Test complete list clinics flow
    - Test listing all clinics
    - Test filtering by each estado value
    - Test sorting by createdAt descending
    - Verify any authenticated user can access
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 7.4_
  
  - [ ] 14.4 Test complete update clinic flow
    - Test frontend form validation
    - Test backend validation
    - Test successful clinic update
    - Test duplicate codigo error handling (when changing codigo)
    - Test not found error handling
    - Test unauthorized access (non-ADMIN user)
    - Verify audit fields are updated correctly
    - Verify createdAt and createdBy are preserved
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 5.1, 5.2, 5.3, 5.4, 5.5, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 8.3, 8.4_
  
  - [ ] 14.5 Test complete delete clinic flow
    - Test successful soft delete
    - Test not found error handling
    - Test unauthorized access (non-ADMIN user)
    - Verify clinic record is retained in database with DELETED status
    - Verify all original field values are preserved except estado
    - Verify audit fields are updated correctly
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 8.3, 8.4_

- [ ]* 15. Write unit tests for domain model
  - Create test class `ClinicTest.java` in `src/test/java/.../domain/model/`
  - Test clinic creation with valid data
  - Test clinic creation with invalid codigo (non-numeric)
  - Test clinic creation with invalid nombre (non-alphanumeric)
  - Test clinic creation with invalid descripcion (non-alphanumeric)
  - Test clinic update with valid data
  - Test clinic update with invalid data
  - Test soft delete sets estado to DELETED
  - _Requirements: 1.2, 1.3, 1.4, 3.4, 3.5, 3.6, 4.1, 4.6_

- [ ]* 16. Write unit tests for use cases
  - Create test classes for each use case in `src/test/java/.../application/usecase/`
  - Test CreateClinicUseCase with valid and invalid inputs
  - Test CreateClinicUseCase duplicate codigo handling
  - Test ListClinicsUseCase with and without filters
  - Test UpdateClinicUseCase with valid and invalid inputs
  - Test UpdateClinicUseCase not found handling
  - Test DeleteClinicUseCase with valid ID
  - Test DeleteClinicUseCase not found handling
  - Mock repository dependencies
  - _Requirements: 1.5, 1.9, 2.1, 2.2, 3.3, 3.9, 4.4, 5.5_

- [ ]* 17. Write integration tests for REST API
  - Create test class `ClinicControllerIntegrationTest.java` in `src/test/java/.../infrastructure/rest/`
  - Test POST `/api/clinics` with valid and invalid data
  - Test GET `/api/clinics` with and without filters
  - Test PUT `/api/clinics/{id}` with valid and invalid data
  - Test DELETE `/api/clinics/{id}` with valid and invalid IDs
  - Test authorization for ADMIN-only endpoints
  - Test HTTP status codes for success and error cases
  - Use @SpringBootTest and MockMvc
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 10.5, 10.6, 10.7, 10.8_

- [ ] 18. Final checkpoint - Complete system verification
  - Run all tests and ensure they pass
  - Verify database migration runs successfully
  - Verify service starts and registers with Eureka
  - Verify all endpoints are accessible through API Gateway
  - Verify frontend application builds successfully
  - Verify complete user flows work end-to-end
  - Ask the user if questions arise

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation and provide opportunities for user feedback
- The implementation follows hexagonal architecture with clear layer separation
- All audit fields (createdAt, createdBy, updatedAt, updatedBy) must be populated automatically
- Soft delete is used instead of physical deletion to preserve historical data
- Role-based access control is enforced at both API Gateway and controller levels
- Validation is implemented at both frontend and backend for defense in depth
- The package structure follows existing MedFlow conventions (`com.medframe.clinical`)
