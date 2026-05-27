---
inclusion: auto
---

# MedFlow HIS - Project Standards for Kiro

This steering file provides standards and guidelines for Kiro when working on the MedFlow HIS project.

## Project Overview

MedFlow HIS is a Hospital Information System built with:
- **Backend**: Spring Boot 3.2.4 microservices with DDD
- **Frontend**: React 18 + TypeScript + Vite
- **Architecture**: Microservices with Service Discovery (Eureka)
- **Methodology**: Spec-Driven Design + TDD

## Development Methodology

### 1. Spec-Driven Design (MANDATORY)

Before implementing ANY feature:

1. **Create spec directory**: `.kiro/specs/{feature-name}/`
2. **Write requirements.md**: Define WHAT is needed
3. **Write design.md**: Define HOW to build it
4. **Write tasks.md**: Break down into implementable tasks
5. **Implement with TDD**: Follow Red-Green-Refactor cycle

### 2. Test-Driven Development (MANDATORY)

For EVERY task:

1. **RED**: Write failing test first
2. **GREEN**: Write minimum code to pass
3. **REFACTOR**: Improve code while keeping tests green

**Test Coverage Requirements:**
- Minimum: 80% overall
- Business logic: 90%+
- Critical paths: 100%

## Code Standards

### Java/Spring Boot

**Package Structure:**
```
com.medflow.{service}/
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Data access
├── model/           # JPA entities
├── dto/             # Data Transfer Objects
├── config/          # Configuration
├── security/        # Security components
├── exception/       # Custom exceptions
└── util/            # Utilities
```

**Naming Conventions:**
- Classes: PascalCase (e.g., `UserService`)
- Methods: camelCase (e.g., `findUserById`)
- Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- Packages: lowercase (e.g., `com.medflow.auth`)

**Annotations:**
- Use `@Service` for business logic
- Use `@Repository` for data access
- Use `@RestController` for REST endpoints
- Use `@Transactional` for database transactions

### TypeScript/React

**Component Structure:**
```typescript
// Functional components with TypeScript
interface Props {
  // Define props
}

export const ComponentName: React.FC<Props> = ({ prop1, prop2 }) => {
  // Component logic
  return (
    // JSX
  );
};
```

**Naming Conventions:**
- Components: PascalCase (e.g., `UserProfile`)
- Files: PascalCase for components (e.g., `UserProfile.tsx`)
- Hooks: camelCase with 'use' prefix (e.g., `useAuth`)
- Constants: UPPER_SNAKE_CASE

## Git Standards

### Branch Naming

- `feature/feature-name` - New features
- `fix/bug-description` - Bug fixes
- `docs/what-changed` - Documentation
- `refactor/what-refactored` - Code refactoring
- `test/what-tested` - Adding tests

### Commit Messages (Conventional Commits)

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code formatting
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples:**
```
feat(auth): implement JWT validation
fix(patient): resolve registration form validation
docs(api-gateway): add routing documentation
test(clinical): add unit tests for triage service
```

## Architecture Guidelines

### Microservices

Each microservice MUST:
- Have its own database schema
- Register with Eureka on startup
- Implement health checks (`/actuator/health`)
- Use Spring Boot Actuator
- Follow DDD principles

### Database per Service

```
medflow_auth_db       → Auth Service
medflow_patient_db    → Patient Service
medflow_clinical_db   → Clinical Service
medflow_lab_db        → Lab Service
medflow_pharmacy_db   → Pharmacy Service
medflow_billing_db    → Billing Service
```

### API Design

**REST Endpoints:**
```
GET    /api/{resource}           # List all
GET    /api/{resource}/{id}      # Get one
POST   /api/{resource}           # Create
PUT    /api/{resource}/{id}      # Update
DELETE /api/{resource}/{id}      # Delete
```

**Response Format:**
```json
{
  "data": {},
  "message": "Success",
  "timestamp": "2026-04-10T20:00:00Z"
}
```

**Error Format:**
```json
{
  "error": "Error message",
  "status": 400,
  "timestamp": "2026-04-10T20:00:00Z",
  "path": "/api/patients"
}
```

## Documentation Requirements

### Code Documentation

**Java:**
```java
/**
 * Service for managing patient data.
 * 
 * @author MedFlow Team
 * @version 1.0.0
 */
@Service
public class PatientService {
    
    /**
     * Finds a patient by ID.
     * 
     * @param id the patient ID
     * @return the patient if found
     * @throws PatientNotFoundException if patient not found
     */
    public Patient findById(Long id) {
        // Implementation
    }
}
```

**TypeScript:**
```typescript
/**
 * Hook for managing authentication state
 * 
 * @returns Authentication context with user and methods
 */
export const useAuth = () => {
  // Implementation
};
```

### README Requirements

Each microservice MUST have a README.md with:
- Purpose and responsibilities
- Technology stack
- How to run locally
- How to run tests
- API endpoints
- Environment variables
- Dependencies

## Testing Standards

### Test Structure (AAA Pattern)

```java
@Test
void shouldDoSomething() {
    // ARRANGE - Setup
    // ACT - Execute
    // ASSERT - Verify
}
```

### Test Naming

Format: `should[ExpectedBehavior]When[Condition]`

Examples:
- `shouldReturnUserWhenIdExists()`
- `shouldThrowExceptionWhenEmailIsInvalid()`
- `shouldSavePatientWhenDataIsValid()`

### Test Types

1. **Unit Tests**: Test individual classes/methods
2. **Integration Tests**: Test component integration
3. **E2E Tests**: Test complete user flows

## Security Standards

### JWT Tokens

- Use HS256 algorithm
- Expiration: 24 hours
- Include user ID and roles in claims
- Validate on every request (API Gateway)

### Password Storage

- Use BCrypt with strength 12
- Never store plain text passwords
- Implement password complexity rules

### CORS Configuration

```yaml
allowed-origins: http://localhost:3000
allowed-methods: GET, POST, PUT, DELETE
allowed-headers: Authorization, Content-Type
```

## Performance Standards

### Response Times

- API Gateway routing: < 50ms
- Database queries: < 100ms
- API endpoints: < 500ms
- Page load: < 2s

### Scalability

- Support 1000 concurrent users
- Handle 10,000 requests/minute
- Database connection pooling

## Error Handling

### Exception Hierarchy

```
RuntimeException
├── BusinessException
│   ├── PatientNotFoundException
│   ├── InvalidTriageException
│   └── InsufficientStockException
└── TechnicalException
    ├── DatabaseException
    └── ExternalServiceException
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(PatientNotFoundException ex) {
        // Return 404
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Return 500
    }
}
```

## Deployment Standards

### Docker

Each service MUST have:
- Multi-stage Dockerfile
- Health check configuration
- Environment variable support
- Minimal image size

### Environment Variables

Use `.env` files for configuration:
- `.env.example` - Template with all variables
- `.env` - Local development (gitignored)
- `.env.docker` - Docker environment

## Monitoring and Logging

### Logging Levels

- **ERROR**: System errors, exceptions
- **WARN**: Warnings, deprecated usage
- **INFO**: Important business events
- **DEBUG**: Detailed debugging info

### Log Format

```
[timestamp] [level] [service] [class] - message
```

Example:
```
2026-04-10 20:00:00 INFO auth-service UserService - User logged in: user@example.com
```

## Code Review Checklist

Before submitting code:

- [ ] All tests pass
- [ ] Code coverage meets requirements
- [ ] No compiler warnings
- [ ] Code follows naming conventions
- [ ] Documentation is updated
- [ ] Spec is followed
- [ ] No hardcoded values
- [ ] Error handling is implemented
- [ ] Logging is appropriate
- [ ] Security best practices followed

## Quick Reference

### Important Files

- `PROJECT_CONTEXT.md` - Complete project context
- `ARCHITECTURE_DDD.md` - Architecture documentation
- `CURRENT_STATUS.md` - Current progress
- `docs/development/SPEC_DRIVEN_DESIGN.md` - Spec methodology
- `docs/development/TDD_GUIDE.md` - TDD guide

### Commands

```bash
# Run tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# Run service locally
mvn spring-boot:run

# Build Docker image
docker build -t service-name .

# Run all services
docker-compose up -d
```

---

**Version**: 1.0.0  
**Last Updated**: April 10, 2026  
**Maintained by**: MedFlow Team
