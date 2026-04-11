# Spec Template - MedFlow HIS

Use this template to create new specs for features.

## How to Use

1. Copy this entire directory structure to `.kiro/specs/{feature-name}/`
2. Fill in each section
3. Follow Spec-Driven Design methodology
4. Implement with TDD

---

## requirements.md Template

```markdown
# Requirements: [Feature Name]

## 1. Overview

[Brief description of the feature - 2-3 sentences]

## 2. User Stories

### US-1: [Story Title]
**As a** [role],  
**I want** [action],  
**So that** [benefit].

**Acceptance Criteria:**
- [ ] [Criterion 1]
- [ ] [Criterion 2]
- [ ] [Criterion 3]

### US-2: [Story Title]
**As a** [role],  
**I want** [action],  
**So that** [benefit].

**Acceptance Criteria:**
- [ ] [Criterion 1]
- [ ] [Criterion 2]

## 3. Functional Requirements

- **FR1**: The system must [requirement]
- **FR2**: The system must [requirement]
- **FR3**: The system must [requirement]
- **FR4**: The system must [requirement]
- **FR5**: The system must [requirement]

## 4. Non-Functional Requirements

### Performance
- **NFR1**: [Performance requirement]
- **NFR2**: [Response time requirement]

### Security
- **NFR3**: [Security requirement]
- **NFR4**: [Authentication/Authorization requirement]

### Scalability
- **NFR5**: [Scalability requirement]
- **NFR6**: [Concurrent users requirement]

### Reliability
- **NFR7**: [Availability requirement]
- **NFR8**: [Error handling requirement]

## 5. Business Rules

- **BR1**: [Business rule]
- **BR2**: [Business rule]
- **BR3**: [Business rule]

## 6. Constraints

- **C1**: [Technical constraint]
- **C2**: [Business constraint]
- **C3**: [Time/Budget constraint]

## 7. Assumptions

- **A1**: [Assumption]
- **A2**: [Assumption]
- **A3**: [Assumption]

## 8. Dependencies

- **D1**: [Dependency on other service/feature]
- **D2**: [External system dependency]
- **D3**: [Library/Framework dependency]

## 9. Acceptance Criteria (Overall)

- [ ] All functional requirements are met
- [ ] All non-functional requirements are met
- [ ] All user stories are completed
- [ ] All tests pass (unit, integration, E2E)
- [ ] Code coverage >= 80%
- [ ] Documentation is complete
- [ ] Security review passed
- [ ] Performance benchmarks met

## 10. Out of Scope

- [What is NOT included in this feature]
- [Future enhancements]
- [Related but separate features]

## 11. Success Metrics

- [How to measure success]
- [KPIs to track]
- [User satisfaction metrics]

---

**Created**: [Date]  
**Author**: [Name]  
**Status**: Draft | In Review | Approved | Implemented
```

---

## design.md Template

```markdown
# Design: [Feature Name]

## 1. Architecture Overview

### High-Level Architecture

```
[ASCII diagram or description]
```

### Component Interaction

```
[Sequence diagram or flow description]
```

## 2. Components

### [Component Name 1]

**Responsibility**: [What this component does]

**Dependencies**: [What it depends on]

**Interfaces**:
- [Interface 1]
- [Interface 2]

### [Component Name 2]

**Responsibility**: [What this component does]

**Dependencies**: [What it depends on]

**Interfaces**:
- [Interface 1]
- [Interface 2]

## 3. Data Model

### Entities

#### [Entity Name]

```java
@Entity
@Table(name = "table_name")
public class EntityName {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String field1;
    private String field2;
    
    // Relationships
    @ManyToOne
    private RelatedEntity related;
}
```

### DTOs

#### [DTO Name]

```java
public class DtoName {
    private Long id;
    private String field1;
    private String field2;
}
```

### Database Schema

```sql
CREATE TABLE table_name (
    id BIGSERIAL PRIMARY KEY,
    field1 VARCHAR(255) NOT NULL,
    field2 VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 4. API Design

### Endpoints

#### [Endpoint 1]

**Method**: GET  
**Path**: `/api/resource`  
**Description**: [What it does]

**Request Headers**:
```
Authorization: Bearer <token>
Content-Type: application/json
```

**Query Parameters**:
- `param1` (optional): [Description]
- `param2` (required): [Description]

**Response** (200 OK):
```json
{
  "data": [
    {
      "id": 1,
      "field1": "value1"
    }
  ],
  "message": "Success"
}
```

**Error Responses**:
- `400 Bad Request`: [When this happens]
- `401 Unauthorized`: [When this happens]
- `404 Not Found`: [When this happens]
- `500 Internal Server Error`: [When this happens]

#### [Endpoint 2]

**Method**: POST  
**Path**: `/api/resource`  
**Description**: [What it does]

**Request Body**:
```json
{
  "field1": "value1",
  "field2": "value2"
}
```

**Response** (201 Created):
```json
{
  "data": {
    "id": 1,
    "field1": "value1",
    "field2": "value2"
  },
  "message": "Created successfully"
}
```

## 5. Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.4
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA
- **Security**: Spring Security + JWT
- **Testing**: JUnit 5, Mockito, TestContainers

### Additional Libraries
- [Library 1]: [Purpose]
- [Library 2]: [Purpose]

### Frontend (if applicable)
- **Framework**: React 18
- **Language**: TypeScript
- **Build Tool**: Vite
- **Styling**: TailwindCSS

## 6. Security Design

### Authentication
- [How authentication works]
- [Token generation/validation]

### Authorization
- [Role-based access control]
- [Permission model]

### Data Protection
- [Encryption at rest]
- [Encryption in transit]
- [Sensitive data handling]

### Security Headers
```yaml
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
X-XSS-Protection: 1; mode=block
```

## 7. Error Handling

### Exception Hierarchy

```
BusinessException
├── [CustomException1]
├── [CustomException2]
└── [CustomException3]
```

### Error Response Format

```json
{
  "error": "Error message",
  "status": 400,
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/resource",
  "details": ["Detail 1", "Detail 2"]
}
```

### Error Codes

| Code | Description | HTTP Status |
|------|-------------|-------------|
| E001 | [Error description] | 400 |
| E002 | [Error description] | 404 |
| E003 | [Error description] | 500 |

## 8. Testing Strategy

### Unit Tests

**What to test**:
- [Component 1] logic
- [Component 2] validation
- [Component 3] calculations

**Coverage target**: 90%

### Integration Tests

**What to test**:
- Database operations
- Service integration
- API endpoints

**Coverage target**: 80%

### E2E Tests

**Scenarios**:
1. [Happy path scenario]
2. [Error scenario]
3. [Edge case scenario]

### Test Data

**Setup**:
- [Test data requirements]
- [Database seeding]

**Cleanup**:
- [How to clean up after tests]

## 9. Performance Considerations

### Optimization Strategies
- [Strategy 1]
- [Strategy 2]

### Caching
- [What to cache]
- [Cache invalidation strategy]

### Database Optimization
- [Indexes to create]
- [Query optimization]

### Monitoring
- [Metrics to track]
- [Alerts to configure]

## 10. Deployment

### Environment Variables

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/db_name
DB_USERNAME=user
DB_PASSWORD=password

# Service
SERVICE_PORT=8081
EUREKA_URL=http://localhost:8761/eureka/

# Security
JWT_SECRET=secret-key
JWT_EXPIRATION=86400000
```

### Docker Configuration

```yaml
# docker-compose.yml snippet
service-name:
  build: ./path
  ports:
    - "8081:8081"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
  depends_on:
    - postgres
    - eureka-server
```

### Health Checks

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
```

## 11. Migration Strategy

### Database Migrations

```sql
-- V1__initial_schema.sql
CREATE TABLE ...
```

### Data Migration

- [How to migrate existing data]
- [Rollback strategy]

### Backward Compatibility

- [How to maintain compatibility]
- [Deprecation strategy]

## 12. Monitoring and Logging

### Logging Strategy

**Log Levels**:
- ERROR: [What to log]
- WARN: [What to log]
- INFO: [What to log]
- DEBUG: [What to log]

**Log Format**:
```
[timestamp] [level] [service] [class] - message
```

### Metrics to Track

- Request count
- Response time
- Error rate
- [Custom metric 1]
- [Custom metric 2]

### Alerts

- [Alert condition 1]
- [Alert condition 2]

## 13. Documentation

### API Documentation

- Use SpringDoc OpenAPI
- Available at: `/swagger-ui.html`

### Code Documentation

- JavaDoc for all public methods
- README for service overview

### User Documentation

- [User guide location]
- [API usage examples]

---

**Created**: [Date]  
**Author**: [Name]  
**Reviewed by**: [Name]  
**Status**: Draft | In Review | Approved | Implemented
```

---

## tasks.md Template

```markdown
# Tasks: [Feature Name]

## Overview

This document breaks down the implementation into manageable tasks following TDD.

## Task Breakdown

### Task 1: Project Setup
- [ ] 1.1 Create Maven project structure
- [ ] 1.2 Add dependencies to pom.xml
- [ ] 1.3 Configure application.yml
- [ ] 1.4 Setup test configuration
- [ ] 1.5 Create package structure

**Estimated Time**: [X hours]  
**Dependencies**: None

---

### Task 2: [Component Name] - Unit Tests (TDD)

#### 2.1 Write Tests (RED)
- [ ] Test: [Test description]
- [ ] Test: [Test description]
- [ ] Test: [Test description]

#### 2.2 Implement Code (GREEN)
- [ ] Implement [method/class]
- [ ] Implement [method/class]

#### 2.3 Refactor
- [ ] Refactor [what to improve]
- [ ] Add documentation
- [ ] Verify all tests pass

**Estimated Time**: [X hours]  
**Dependencies**: Task 1

---

### Task 3: [Component Name] - Integration Tests

#### 3.1 Setup Test Environment
- [ ] Configure TestContainers
- [ ] Setup test database
- [ ] Create test data

#### 3.2 Write Integration Tests
- [ ] Test: [Integration scenario]
- [ ] Test: [Integration scenario]

#### 3.3 Verify
- [ ] All integration tests pass
- [ ] Coverage meets requirements

**Estimated Time**: [X hours]  
**Dependencies**: Task 2

---

### Task 4: API Endpoints (TDD)

#### 4.1 Write Controller Tests (RED)
- [ ] Test: GET /api/resource
- [ ] Test: POST /api/resource
- [ ] Test: PUT /api/resource/{id}
- [ ] Test: DELETE /api/resource/{id}

#### 4.2 Implement Controllers (GREEN)
- [ ] Implement GET endpoint
- [ ] Implement POST endpoint
- [ ] Implement PUT endpoint
- [ ] Implement DELETE endpoint

#### 4.3 Refactor
- [ ] Extract common logic
- [ ] Add validation
- [ ] Add error handling

**Estimated Time**: [X hours]  
**Dependencies**: Task 3

---

### Task 5: Error Handling

- [ ] 5.1 Create custom exceptions
- [ ] 5.2 Implement GlobalExceptionHandler
- [ ] 5.3 Write tests for error scenarios
- [ ] 5.4 Verify error responses

**Estimated Time**: [X hours]  
**Dependencies**: Task 4

---

### Task 6: Security Implementation

- [ ] 6.1 Configure Spring Security
- [ ] 6.2 Implement JWT validation
- [ ] 6.3 Add role-based authorization
- [ ] 6.4 Write security tests
- [ ] 6.5 Verify security works end-to-end

**Estimated Time**: [X hours]  
**Dependencies**: Task 4

---

### Task 7: Database Integration

- [ ] 7.1 Create database schema
- [ ] 7.2 Implement repositories
- [ ] 7.3 Write repository tests
- [ ] 7.4 Test with TestContainers
- [ ] 7.5 Verify data persistence

**Estimated Time**: [X hours]  
**Dependencies**: Task 2

---

### Task 8: Service Integration

- [ ] 8.1 Register with Eureka
- [ ] 8.2 Configure service discovery
- [ ] 8.3 Test service registration
- [ ] 8.4 Verify health checks

**Estimated Time**: [X hours]  
**Dependencies**: Task 4

---

### Task 9: Documentation

- [ ] 9.1 Write README.md
- [ ] 9.2 Add JavaDoc comments
- [ ] 9.3 Configure SpringDoc OpenAPI
- [ ] 9.4 Create API usage examples
- [ ] 9.5 Update architecture docs

**Estimated Time**: [X hours]  
**Dependencies**: Task 8

---

### Task 10: E2E Testing

- [ ] 10.1 Write E2E test scenarios
- [ ] 10.2 Implement E2E tests
- [ ] 10.3 Run full test suite
- [ ] 10.4 Verify coverage >= 80%

**Estimated Time**: [X hours]  
**Dependencies**: Task 9

---

### Task 11: Docker Configuration

- [ ] 11.1 Create Dockerfile
- [ ] 11.2 Update docker-compose.yml
- [ ] 11.3 Test Docker build
- [ ] 11.4 Test Docker deployment
- [ ] 11.5 Verify health checks in Docker

**Estimated Time**: [X hours]  
**Dependencies**: Task 10

---

### Task 12: Final Validation

- [ ] 12.1 Run all tests
- [ ] 12.2 Check code coverage
- [ ] 12.3 Verify against acceptance criteria
- [ ] 12.4 Code review
- [ ] 12.5 Update documentation
- [ ] 12.6 Mark spec as complete

**Estimated Time**: [X hours]  
**Dependencies**: Task 11

---

## Summary

**Total Tasks**: 12  
**Estimated Total Time**: [X hours]  
**Critical Path**: Tasks 1 → 2 → 3 → 4 → 8 → 10 → 11 → 12

## Notes

- Follow TDD strictly: RED → GREEN → REFACTOR
- Run tests after each subtask
- Update this document as you progress
- Mark tasks complete with [x]

---

**Created**: [Date]  
**Author**: [Name]  
**Status**: Not Started | In Progress | Completed
```

---

## Quick Start

1. **Copy template**:
   ```bash
   mkdir -p .kiro/specs/my-feature
   cp docs/specs/SPEC_TEMPLATE.md .kiro/specs/my-feature/
   ```

2. **Create spec files**:
   ```bash
   cd .kiro/specs/my-feature
   # Copy requirements template to requirements.md
   # Copy design template to design.md
   # Copy tasks template to tasks.md
   ```

3. **Fill in templates**:
   - Start with requirements.md
   - Then design.md
   - Finally tasks.md

4. **Implement with TDD**:
   - Follow tasks.md
   - RED → GREEN → REFACTOR for each task

---

**Version**: 1.0.0  
**Last Updated**: April 10, 2026
