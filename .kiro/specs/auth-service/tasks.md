# Tasks: Auth Service Implementation

## Overview
Implementation of Auth Service following **Test-Driven Development (TDD)** methodology with **Red-Green-Refactor** cycle.

**TDD Cycle:**
1. 🔴 **RED**: Write failing test
2. 🟢 **GREEN**: Write minimal code to pass
3. 🔵 **REFACTOR**: Improve code quality

**Total Estimated Time**: ~8-10 hours

---

## Phase 1: Project Setup

### [x] 1. Create Maven Project Structure
**Description**: Set up Spring Boot project with dependencies.

**Sub-tasks:**
- [x] 1.1 Create `pom.xml` with Spring Boot 3.2.4
- [x] 1.2 Add dependencies: Spring Security, JJWT, JPA, PostgreSQL, Eureka Client
- [x] 1.3 Create package structure: `com.medflow.auth`
- [x] 1.4 Create `AuthServiceApplication.java` with `@EnableDiscoveryClient`
- [x] 1.5 Create `application.yml` with configuration
- [x] 1.6 Verify build: `mvn clean compile`

**Acceptance Criteria:**
- Maven build succeeds
- Application starts without errors

**Estimated Time**: 30 minutes

---

## Phase 2: Database Setup

### [ ] 2. Create Database Schema and Entities

#### [ ] 2.1 Create SQL Schema (RED)
**Description**: Create database schema and tables.

**Implementation:**
- [ ] Create `schema.sql` in `src/main/resources`
- [ ] Define `auth_schema` with tables: users, roles, user_roles
- [ ] Add indexes on username, email

**Estimated Time**: 20 minutes

---

#### [ ] 2.2 Create JPA Entities (GREEN)
**Description**: Create User and Role entities.

**Implementation:**
- [ ] Create `User.java` entity
- [ ] Create `Role.java` entity with RoleName enum
- [ ] Create `UserRepository.java` interface
- [ ] Create `RoleRepository.java` interface
- [ ] Add method: `Optional<User> findByUsername(String username)`
- [ ] Add method: `Optional<User> findByEmail(String email)`

**Estimated Time**: 30 minutes

---

#### [ ] 2.3 Test Repositories (RED-GREEN)
**Description**: Write tests for repositories.

**Tests:**
```java
@Test void shouldSaveUser()
@Test void shouldFindUserByUsername()
@Test void shouldFindUserByEmail()
@Test void shouldReturnEmptyWhenUserNotFound()
```

**Estimated Time**: 25 minutes

---

## Phase 3: JWT Service (TDD)

### [ ] 3. Implement JWT Generation and Validation

#### [ ] 3.1 Write Tests for JwtService (RED)
**Description**: Write failing tests for JWT logic.

**Tests:**
```java
@Test void shouldGenerateValidJwt()
@Test void shouldExtractUserIdFromJwt()
@Test void shouldExtractRolesFromJwt()
@Test void shouldValidateValidJwt()
@Test void shouldRejectExpiredJwt()
@Test void shouldRejectInvalidSignature()
@Test void shouldRejectMalformedJwt()
```

**Estimated Time**: 30 minutes

---

#### [ ] 3.2 Implement JwtService (GREEN)
**Description**: Implement JWT generation and validation.

**Implementation:**
- [ ] Create `JwtService.java`
- [ ] Implement `generateToken(User user)`
- [ ] Implement `isValid(String token)`
- [ ] Implement `extractClaims(String token)`
- [ ] Implement `extractUserId(String token)`
- [ ] Configure secret key and expiration from properties

**Estimated Time**: 40 minutes

---

#### [ ] 3.3 Refactor JwtService (REFACTOR)
**Description**: Improve code quality.

**Refactoring:**
- [ ] Extract claim extraction to helper methods
- [ ] Add logging for token generation/validation
- [ ] Add JavaDoc comments

**Estimated Time**: 15 minutes

---

## Phase 4: Authentication Service (TDD)

### [ ] 4. Implement Login Logic

#### [ ] 4.1 Write Tests for AuthService (RED)
**Description**: Write failing tests for authentication.

**Tests:**
```java
@Test void shouldLoginWithValidCredentials()
@Test void shouldRejectInvalidPassword()
@Test void shouldRejectNonExistentUser()
@Test void shouldRejectInactiveUser()
@Test void shouldReturnJwtOnSuccessfulLogin()
```

**Estimated Time**: 30 minutes

---

#### [ ] 4.2 Implement AuthService (GREEN)
**Description**: Implement authentication logic.

**Implementation:**
- [ ] Create `AuthService.java`
- [ ] Implement `login(String username, String password)`
- [ ] Validate credentials with BCrypt
- [ ] Check user active status
- [ ] Generate JWT using JwtService
- [ ] Return LoginResponse with token

**Estimated Time**: 40 minutes

---

#### [ ] 4.3 Implement Password Encoding (GREEN)
**Description**: Configure BCrypt password encoder.

**Implementation:**
- [ ] Create `SecurityConfig.java`
- [ ] Define `PasswordEncoder` bean with BCrypt (strength 10)
- [ ] Update tests to use encoded passwords

**Estimated Time**: 15 minutes

---

## Phase 5: Token Blacklist (TDD)

### [ ] 5. Implement Logout Logic

#### [ ] 5.1 Write Tests for TokenBlacklistService (RED)
**Description**: Write tests for blacklist logic.

**Tests:**
```java
@Test void shouldAddTokenToBlacklist()
@Test void shouldDetectBlacklistedToken()
@Test void shouldNotDetectNonBlacklistedToken()
@Test void shouldCleanupExpiredTokens()
```

**Estimated Time**: 20 minutes

---

#### [ ] 5.2 Implement TokenBlacklistService (GREEN)
**Description**: Implement in-memory blacklist.

**Implementation:**
- [ ] Create `TokenBlacklistService.java`
- [ ] Implement `addToBlacklist(String token)`
- [ ] Implement `isBlacklisted(String token)`
- [ ] Implement `scheduleCleanup(String token, long delay)`
- [ ] Use ConcurrentHashMap for thread safety

**Estimated Time**: 30 minutes

---

#### [ ] 5.3 Implement Logout in AuthService (GREEN)
**Description**: Add logout method.

**Implementation:**
- [ ] Add `logout(String token)` method to AuthService
- [ ] Call TokenBlacklistService to add token to blacklist
- [ ] Write tests for logout

**Estimated Time**: 20 minutes

---

## Phase 6: REST Controllers (TDD)

### [ ] 6. Implement REST Endpoints

#### [ ] 6.1 Write Tests for AuthController (RED)
**Description**: Write integration tests for endpoints.

**Tests:**
```java
@Test void shouldLoginWithValidCredentials()
@Test void shouldReturn401WithInvalidCredentials()
@Test void shouldLogoutSuccessfully()
@Test void shouldRefreshToken()
@Test void shouldValidateToken()
@Test void shouldGetCurrentUser()
```

**Estimated Time**: 40 minutes

---

#### [ ] 6.2 Implement AuthController (GREEN)
**Description**: Implement REST endpoints.

**Implementation:**
- [ ] Create `AuthController.java`
- [ ] Implement `POST /api/auth/login`
- [ ] Implement `POST /api/auth/logout`
- [ ] Implement `POST /api/auth/refresh`
- [ ] Implement `GET /api/auth/validate`
- [ ] Implement `GET /api/auth/me`
- [ ] Create DTOs: LoginRequest, LoginResponse, ValidateResponse, UserResponse

**Estimated Time**: 50 minutes

---

#### [ ] 6.3 Implement Error Handling (GREEN)
**Description**: Global exception handler.

**Implementation:**
- [ ] Create `GlobalExceptionHandler.java`
- [ ] Handle `BadCredentialsException` → 401
- [ ] Handle `InvalidTokenException` → 401
- [ ] Handle `UserNotFoundException` → 404
- [ ] Handle `AccountDisabledException` → 403
- [ ] Return consistent error format

**Estimated Time**: 25 minutes

---

## Phase 7: Rate Limiting (TDD)

### [ ] 7. Implement Login Rate Limiting

#### [ ] 7.1 Write Tests for LoginRateLimiter (RED)
**Description**: Write tests for rate limiting.

**Tests:**
```java
@Test void shouldAllowUnder5Attempts()
@Test void shouldBlockAfter5Attempts()
@Test void shouldResetAfterSuccessfulLogin()
@Test void shouldTrackPerIpAddress()
```

**Estimated Time**: 20 minutes

---

#### [ ] 7.2 Implement LoginRateLimiter (GREEN)
**Description**: Implement rate limiting logic.

**Implementation:**
- [ ] Create `LoginRateLimiter.java`
- [ ] Track attempts per IP address
- [ ] Limit to 5 attempts per minute
- [ ] Reset on successful login
- [ ] Throw `TooManyAttemptsException` when exceeded

**Estimated Time**: 30 minutes

---

#### [ ] 7.3 Integrate with AuthController (GREEN)
**Description**: Add rate limiting to login endpoint.

**Implementation:**
- [ ] Inject LoginRateLimiter in AuthController
- [ ] Check rate limit before authentication
- [ ] Reset attempts on successful login
- [ ] Return 429 when limit exceeded

**Estimated Time**: 15 minutes

---

## Phase 8: Integration Testing

### [ ] 8. End-to-End Integration Tests

#### [ ] 8.1 Write E2E Tests (RED)
**Description**: Comprehensive integration tests.

**Tests:**
```java
@Test void shouldCompleteLoginFlow()
@Test void shouldCompleteLogoutFlow()
@Test void shouldCompleteRefreshFlow()
@Test void shouldEnforceRateLimit()
@Test void shouldRejectBlacklistedToken()
@Test void shouldIntegrateWithEureka()
```

**Estimated Time**: 40 minutes

---

#### [ ] 8.2 Fix Integration Issues (GREEN)
**Description**: Fix any issues found.

**Implementation:**
- [ ] Debug failing tests
- [ ] Fix configuration issues
- [ ] Verify all endpoints work together

**Estimated Time**: 30 minutes

---

#### [ ] 8.3 Verify Test Coverage (REFACTOR)
**Description**: Ensure coverage >= 80%.

**Verification:**
- [ ] Run `mvn test jacoco:report`
- [ ] Verify coverage >= 80%
- [ ] Add tests for uncovered code

**Estimated Time**: 20 minutes

---

## Phase 9: Docker & Deployment

### [ ] 9. Docker Configuration

#### [ ] 9.1 Create Dockerfile
**Description**: Multi-stage Dockerfile.

**Implementation:**
- [ ] Create `Dockerfile`
- [ ] Stage 1: Build with Maven
- [ ] Stage 2: Runtime with JRE
- [ ] Add health check
- [ ] Expose port 8081

**Estimated Time**: 20 minutes

---

#### [ ] 9.2 Update docker-compose.yml
**Description**: Add auth-service to docker-compose.

**Implementation:**
- [ ] Add `auth-service` service
- [ ] Configure environment variables
- [ ] Set depends_on: eureka-server, postgres
- [ ] Map port 8081:8081

**Estimated Time**: 15 minutes

---

#### [ ] 9.3 Test Docker Deployment
**Description**: Verify service works in Docker.

**Testing:**
- [ ] Start PostgreSQL: `docker-compose up postgres`
- [ ] Start Eureka: `docker-compose up eureka-server`
- [ ] Start Auth Service: `docker-compose up auth-service`
- [ ] Test login endpoint
- [ ] Verify Eureka registration

**Estimated Time**: 25 minutes

---

## Phase 10: Documentation & Final Verification

### [ ] 10. Documentation

#### [ ] 10.1 Create README.md
**Description**: Service documentation.

**Sections:**
- [ ] Overview
- [ ] Prerequisites
- [ ] Configuration
- [ ] Running locally
- [ ] Running with Docker
- [ ] API endpoints
- [ ] Testing

**Estimated Time**: 30 minutes

---

#### [ ] 10.2 Create QUICKSTART.md
**Description**: Quick start guide.

**Sections:**
- [ ] 5-minute setup
- [ ] Common commands
- [ ] Testing endpoints
- [ ] Troubleshooting

**Estimated Time**: 15 minutes

---

### [ ] 11. Final Verification

#### [ ] 11.1 Run All Tests
**Description**: Verify all tests pass.

**Verification:**
- [ ] Run `mvn test`
- [ ] Run `mvn verify`
- [ ] Check coverage report
- [ ] All tests pass

**Estimated Time**: 10 minutes

---

#### [ ] 11.2 Manual Testing
**Description**: Manual verification.

**Test scenarios:**
- [ ] Login with valid credentials
- [ ] Login with invalid credentials
- [ ] Logout
- [ ] Refresh token
- [ ] Validate token
- [ ] Get current user
- [ ] Rate limiting (6 attempts)

**Estimated Time**: 30 minutes

---

#### [ ] 11.3 Code Review & Commit
**Description**: Final review and commit.

**Checklist:**
- [ ] All tests pass
- [ ] No hardcoded secrets
- [ ] Logging appropriate
- [ ] Code follows standards
- [ ] Documentation complete

**Steps:**
- [ ] Create branch: `git checkout -b feature/auth-service`
- [ ] Commit: `git commit -m "feat: implement auth service with JWT and RBAC"`
- [ ] Push: `git push origin feature/auth-service`
- [ ] Create PR to `develop`

**Estimated Time**: 15 minutes

---

## Summary

### Total Estimated Time: ~8-10 hours

### Phase Breakdown:
- **Phase 1**: Project Setup (30 min)
- **Phase 2**: Database Setup (1.25 hours)
- **Phase 3**: JWT Service (1.5 hours)
- **Phase 4**: Authentication Service (1.5 hours)
- **Phase 5**: Token Blacklist (1.25 hours)
- **Phase 6**: REST Controllers (2 hours)
- **Phase 7**: Rate Limiting (1 hour)
- **Phase 8**: Integration Testing (1.5 hours)
- **Phase 9**: Docker & Deployment (1 hour)
- **Phase 10**: Documentation & Verification (1.5 hours)

### Key Milestones:
1. ✅ JWT generation/validation working
2. ✅ Login/logout functional
3. ✅ Rate limiting implemented
4. ✅ All tests passing (>80% coverage)
5. ✅ Docker deployment working
6. ✅ Documentation complete

### Dependencies:
- Eureka Server running
- PostgreSQL running
- JWT secret configured

---

**Created**: April 13, 2026  
**Author**: MedFlow Team  
**Status**: 📋 Ready for Implementation  
**Version**: 1.0.0
