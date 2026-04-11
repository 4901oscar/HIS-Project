# Tasks: API Gateway Implementation

## Overview
This document breaks down the API Gateway implementation into testable tasks following **Test-Driven Development (TDD)** methodology. Each task follows the **Red-Green-Refactor** cycle.

**TDD Cycle:**
1. 🔴 **RED**: Write a failing test first
2. 🟢 **GREEN**: Write minimal code to make the test pass
3. 🔵 **REFACTOR**: Improve code quality without changing behavior

---

## Phase 1: Project Setup

### [ ] 1. Create Maven Project Structure
**Description**: Set up the basic Spring Boot project with all necessary dependencies.

**Sub-tasks:**
- [ ] 1.1 Create `pom.xml` with Spring Boot 3.2.4 and Spring Cloud 2023.0.1
- [ ] 1.2 Add dependencies: Spring Cloud Gateway, Eureka Client, JJWT, Actuator
- [ ] 1.3 Create package structure: `com.medflow.gateway`
- [ ] 1.4 Create `GatewayApplication.java` main class with `@EnableDiscoveryClient`
- [ ] 1.5 Create `application.yml` with basic configuration
- [ ] 1.6 Create `application-docker.yml` for Docker profile
- [ ] 1.7 Verify project builds: `mvn clean compile`

**Acceptance Criteria:**
- Maven build succeeds
- Application starts without errors
- Actuator health endpoint responds at `/actuator/health`

**Estimated Time**: 30 minutes

---

## Phase 2: JWT Validation (TDD)

### [ ] 2. Implement JWT Validator (Core Logic)

#### [ ] 2.1 Write Tests for JwtValidator (RED)
**Description**: Write failing tests for JWT validation logic.

**Tests to write:**
```java
// JwtValidatorTest.java
@Test void shouldValidateValidJwt()
@Test void shouldRejectExpiredJwt()
@Test void shouldRejectInvalidSignature()
@Test void shouldRejectMalformedJwt()
@Test void shouldExtractUserIdFromClaims()
@Test void shouldExtractRolesFromClaims()
```

**Expected**: All tests fail (class doesn't exist yet)

**Estimated Time**: 20 minutes

---

#### [ ] 2.2 Implement JwtValidator (GREEN)
**Description**: Write minimal code to make tests pass.

**Implementation:**
- [ ] Create `JwtValidator.java` component
- [ ] Implement `validateAndGetClaims(String token)` method
- [ ] Use JJWT library to parse and validate token
- [ ] Handle `ExpiredJwtException`, `SignatureException`, `MalformedJwtException`
- [ ] Extract claims: `userId`, `roles`, `exp`

**Expected**: All tests pass

**Estimated Time**: 30 minutes

---

#### [ ] 2.3 Refactor JwtValidator (REFACTOR)
**Description**: Improve code quality without breaking tests.

**Refactoring:**
- [ ] Extract exception handling to separate methods
- [ ] Add logging for validation failures
- [ ] Add JavaDoc comments
- [ ] Verify tests still pass

**Expected**: All tests still pass, code is cleaner

**Estimated Time**: 15 minutes

---

### [ ] 3. Implement JWT Authentication Filter (TDD)

#### [ ] 3.1 Write Tests for JwtAuthenticationFilter (RED)
**Description**: Write failing tests for JWT filter logic.

**Tests to write:**
```java
// JwtAuthenticationFilterTest.java
@Test void shouldAllowPublicPaths()
@Test void shouldReject401WhenTokenMissing()
@Test void shouldReject401WhenTokenInvalid()
@Test void shouldReject401WhenTokenExpired()
@Test void shouldAddUserIdHeaderWhenTokenValid()
@Test void shouldAddUserRolesHeaderWhenTokenValid()
@Test void shouldAddRequestIdHeader()
```

**Expected**: All tests fail (class doesn't exist yet)

**Estimated Time**: 25 minutes

---

#### [ ] 3.2 Implement JwtAuthenticationFilter (GREEN)
**Description**: Write minimal code to make tests pass.

**Implementation:**
- [ ] Create `JwtAuthenticationFilter.java` implementing `GlobalFilter`
- [ ] Implement `filter()` method
- [ ] Check if path is public (skip validation)
- [ ] Extract token from `Authorization` header
- [ ] Call `JwtValidator` to validate token
- [ ] Add headers: `X-User-Id`, `X-User-Roles`, `X-Request-Id`
- [ ] Return 401 on validation failure
- [ ] Set filter order to `-100`

**Expected**: All tests pass

**Estimated Time**: 40 minutes

---

#### [ ] 3.3 Refactor JwtAuthenticationFilter (REFACTOR)
**Description**: Improve code quality without breaking tests.

**Refactoring:**
- [ ] Extract token extraction to separate method
- [ ] Extract error response creation to separate method
- [ ] Add logging for authentication events
- [ ] Verify tests still pass

**Expected**: All tests still pass, code is cleaner

**Estimated Time**: 15 minutes

---

## Phase 3: Route Configuration (TDD)

### [ ] 4. Implement Gateway Routes

#### [ ] 4.1 Write Tests for Route Configuration (RED)
**Description**: Write failing integration tests for routing.

**Tests to write:**
```java
// GatewayRoutingTest.java (Integration Test with WireMock)
@Test void shouldRouteToAuthService()
@Test void shouldRouteToPatientService()
@Test void shouldRouteToClinicalService()
@Test void shouldRouteToLabService()
@Test void shouldRouteToPharmacyService()
@Test void shouldRouteToBillingService()
@Test void shouldReturn404ForUnknownRoute()
```

**Expected**: All tests fail (routes not configured yet)

**Estimated Time**: 30 minutes

---

#### [ ] 4.2 Implement GatewayConfig (GREEN)
**Description**: Configure routes to all microservices.

**Implementation:**
- [ ] Create `GatewayConfig.java` configuration class
- [ ] Define `RouteLocator` bean
- [ ] Configure routes for all 6 services using `lb://SERVICE-NAME`
- [ ] Map paths: `/api/auth/**`, `/api/patients/**`, etc.

**Expected**: All routing tests pass

**Estimated Time**: 25 minutes

---

#### [ ] 4.3 Refactor GatewayConfig (REFACTOR)
**Description**: Improve route configuration.

**Refactoring:**
- [ ] Extract route definitions to constants
- [ ] Add comments for each route
- [ ] Verify tests still pass

**Expected**: All tests still pass, configuration is clearer

**Estimated Time**: 10 minutes

---

## Phase 4: Rate Limiting (TDD)

### [ ] 5. Implement Rate Limiting

#### [ ] 5.1 Write Tests for RateLimitBucket (RED)
**Description**: Write failing tests for rate limit logic.

**Tests to write:**
```java
// RateLimitBucketTest.java
@Test void shouldAllowRequestsUnderLimit()
@Test void shouldRejectRequestsOverLimit()
@Test void shouldResetAfterTimeWindow()
@Test void shouldTrackRemainingRequests()
```

**Expected**: All tests fail (class doesn't exist yet)

**Estimated Time**: 20 minutes

---

#### [ ] 5.2 Implement RateLimitBucket (GREEN)
**Description**: Implement rate limiting logic.

**Implementation:**
- [ ] Create `RateLimitBucket.java` class
- [ ] Implement `tryConsume()` method
- [ ] Implement `getRemaining()` method
- [ ] Implement `resetIfNeeded()` method
- [ ] Use sliding window: 100 requests per minute

**Expected**: All tests pass

**Estimated Time**: 25 minutes

---

#### [ ] 5.3 Write Tests for RateLimitFilter (RED)
**Description**: Write failing tests for rate limit filter.

**Tests to write:**
```java
// RateLimitFilterTest.java
@Test void shouldAllowRequestUnderLimit()
@Test void shouldReturn429WhenLimitExceeded()
@Test void shouldAddRateLimitHeaders()
@Test void shouldTrackPerIpAddress()
```

**Expected**: All tests fail (class doesn't exist yet)

**Estimated Time**: 20 minutes

---

#### [ ] 5.4 Implement RateLimitFilter (GREEN)
**Description**: Implement rate limit filter.

**Implementation:**
- [ ] Create `RateLimitFilter.java` implementing `GlobalFilter`
- [ ] Extract client IP from request
- [ ] Use `RateLimitBucket` to check limit
- [ ] Add headers: `X-RateLimit-Limit`, `X-RateLimit-Remaining`
- [ ] Return 429 if limit exceeded
- [ ] Set filter order to `-200` (before JWT validation)

**Expected**: All tests pass

**Estimated Time**: 30 minutes

---

#### [ ] 5.5 Refactor Rate Limiting (REFACTOR)
**Description**: Improve rate limiting code.

**Refactoring:**
- [ ] Extract IP extraction to utility method
- [ ] Add cleanup for old buckets (memory management)
- [ ] Add logging for rate limit events
- [ ] Verify tests still pass

**Expected**: All tests still pass, code is cleaner

**Estimated Time**: 15 minutes

---

## Phase 5: CORS Configuration

### [ ] 6. Implement CORS Support

#### [ ] 6.1 Write Tests for CORS (RED)
**Description**: Write failing tests for CORS configuration.

**Tests to write:**
```java
// CorsConfigurationTest.java
@Test void shouldAllowLocalhostOrigin()
@Test void shouldAllowAllMethods()
@Test void shouldAllowAllHeaders()
@Test void shouldHandlePreflightRequest()
```

**Expected**: All tests fail (CORS not configured yet)

**Estimated Time**: 15 minutes

---

#### [ ] 6.2 Implement CorsConfiguration (GREEN)
**Description**: Configure CORS for frontend.

**Implementation:**
- [ ] Create `CorsConfiguration.java` configuration class
- [ ] Define `CorsWebFilter` bean
- [ ] Allow origin: `http://localhost:3000`
- [ ] Allow methods: GET, POST, PUT, DELETE, OPTIONS
- [ ] Allow all headers
- [ ] Set `maxAge` to 3600 seconds

**Expected**: All tests pass

**Estimated Time**: 20 minutes

---

#### [ ] 6.3 Refactor CORS Configuration (REFACTOR)
**Description**: Improve CORS configuration.

**Refactoring:**
- [ ] Extract allowed origins to configuration property
- [ ] Add comments explaining CORS settings
- [ ] Verify tests still pass

**Expected**: All tests still pass, configuration is clearer

**Estimated Time**: 10 minutes

---

## Phase 6: Error Handling

### [ ] 7. Implement Global Exception Handler

#### [ ] 7.1 Write Tests for Error Handling (RED)
**Description**: Write failing tests for exception handling.

**Tests to write:**
```java
// GlobalExceptionHandlerTest.java
@Test void shouldReturn401ForJwtException()
@Test void shouldReturn429ForRateLimitException()
@Test void shouldReturn503ForServiceUnavailable()
@Test void shouldReturn500ForUnknownException()
@Test void shouldReturnConsistentErrorFormat()
```

**Expected**: All tests fail (handler doesn't exist yet)

**Estimated Time**: 20 minutes

---

#### [ ] 7.2 Implement GlobalExceptionHandler (GREEN)
**Description**: Implement global error handling.

**Implementation:**
- [ ] Create `GlobalExceptionHandler.java` implementing `ErrorWebExceptionHandler`
- [ ] Create `ErrorResponse.java` DTO
- [ ] Map exceptions to HTTP status codes
- [ ] Return consistent JSON error format
- [ ] Add timestamp and path to error response

**Expected**: All tests pass

**Estimated Time**: 30 minutes

---

#### [ ] 7.3 Refactor Error Handling (REFACTOR)
**Description**: Improve error handling code.

**Refactoring:**
- [ ] Extract error response creation to builder pattern
- [ ] Add logging for all exceptions
- [ ] Verify tests still pass

**Expected**: All tests still pass, code is cleaner

**Estimated Time**: 15 minutes

---

## Phase 7: Integration Testing

### [ ] 8. End-to-End Integration Tests

#### [ ] 8.1 Write E2E Tests (RED)
**Description**: Write comprehensive integration tests.

**Tests to write:**
```java
// GatewayE2ETest.java
@Test void shouldAuthenticateAndRouteSuccessfully()
@Test void shouldRejectInvalidToken()
@Test void shouldEnforceRateLimit()
@Test void shouldHandleServiceUnavailable()
@Test void shouldAddUserHeadersToRequest()
@Test void shouldReturnCorsHeaders()
```

**Expected**: Some tests may fail initially

**Estimated Time**: 40 minutes

---

#### [ ] 8.2 Fix Integration Issues (GREEN)
**Description**: Fix any issues found in E2E tests.

**Implementation:**
- [ ] Debug failing tests
- [ ] Fix configuration issues
- [ ] Ensure all filters work together correctly
- [ ] Verify filter execution order

**Expected**: All E2E tests pass

**Estimated Time**: 30 minutes

---

#### [ ] 8.3 Verify Test Coverage (REFACTOR)
**Description**: Ensure test coverage meets requirements.

**Verification:**
- [ ] Run `mvn test jacoco:report`
- [ ] Verify overall coverage >= 80%
- [ ] Verify business logic coverage >= 90%
- [ ] Add tests for any uncovered code

**Expected**: Coverage requirements met

**Estimated Time**: 20 minutes

---

## Phase 8: Docker & Deployment

### [ ] 9. Docker Configuration

#### [ ] 9.1 Create Dockerfile
**Description**: Create multi-stage Dockerfile for the gateway.

**Implementation:**
- [ ] Create `Dockerfile` with multi-stage build
- [ ] Stage 1: Build with Maven
- [ ] Stage 2: Runtime with JRE
- [ ] Add health check
- [ ] Expose port 8080

**Acceptance Criteria:**
- Docker image builds successfully
- Container starts without errors
- Health check passes

**Estimated Time**: 20 minutes

---

#### [ ] 9.2 Update docker-compose.yml
**Description**: Add API Gateway to docker-compose.

**Implementation:**
- [ ] Add `api-gateway` service to `docker-compose.yml`
- [ ] Configure environment variables
- [ ] Set depends_on: eureka-server
- [ ] Configure network
- [ ] Map port 8080:8080

**Acceptance Criteria:**
- Gateway starts with `docker-compose up`
- Gateway registers with Eureka
- Health check passes

**Estimated Time**: 15 minutes

---

#### [ ] 9.3 Test Docker Deployment
**Description**: Verify gateway works in Docker environment.

**Testing:**
- [ ] Start Eureka Server: `docker-compose up eureka-server`
- [ ] Start API Gateway: `docker-compose up api-gateway`
- [ ] Verify registration in Eureka dashboard
- [ ] Test health endpoint: `curl http://localhost:8080/actuator/health`
- [ ] Test routing (mock service if needed)

**Acceptance Criteria:**
- Gateway registers with Eureka
- Health check returns UP
- Routing works correctly

**Estimated Time**: 25 minutes

---

## Phase 9: Documentation

### [ ] 10. Create Documentation

#### [ ] 10.1 Create README.md
**Description**: Comprehensive README for the API Gateway.

**Sections to include:**
- [ ] Overview and purpose
- [ ] Architecture diagram
- [ ] Prerequisites
- [ ] Configuration
- [ ] Running locally
- [ ] Running with Docker
- [ ] Testing
- [ ] Troubleshooting

**Estimated Time**: 30 minutes

---

#### [ ] 10.2 Create QUICKSTART.md
**Description**: Quick start guide for developers.

**Sections to include:**
- [ ] 5-minute setup guide
- [ ] Common commands
- [ ] Testing the gateway
- [ ] Common issues

**Estimated Time**: 15 minutes

---

#### [ ] 10.3 Create API Documentation
**Description**: Document gateway behavior and configuration.

**Content:**
- [ ] Route mappings
- [ ] Request/response headers
- [ ] Error responses
- [ ] Configuration properties
- [ ] Environment variables

**Estimated Time**: 20 minutes

---

## Phase 10: Final Verification

### [ ] 11. Final Testing & Validation

#### [ ] 11.1 Run All Tests
**Description**: Verify all tests pass.

**Verification:**
- [ ] Run unit tests: `mvn test`
- [ ] Run integration tests: `mvn verify`
- [ ] Check test coverage: `mvn jacoco:report`
- [ ] All tests pass
- [ ] Coverage >= 80%

**Estimated Time**: 10 minutes

---

#### [ ] 11.2 Manual Testing
**Description**: Manual verification of key scenarios.

**Test scenarios:**
- [ ] Start gateway locally
- [ ] Test public endpoint (no JWT): `/api/auth/login`
- [ ] Test protected endpoint without JWT (should fail)
- [ ] Test protected endpoint with valid JWT (should succeed)
- [ ] Test rate limiting (send 101 requests)
- [ ] Test CORS from browser
- [ ] Verify Eureka registration

**Estimated Time**: 30 minutes

---

#### [ ] 11.3 Code Review Checklist
**Description**: Self-review before committing.

**Checklist:**
- [ ] All tests pass
- [ ] No hardcoded secrets
- [ ] Logging is appropriate (no sensitive data)
- [ ] Error messages are clear
- [ ] Code follows project standards
- [ ] Documentation is complete
- [ ] No TODO comments left
- [ ] Git commit messages are clear

**Estimated Time**: 15 minutes

---

#### [ ] 11.4 Commit and Push
**Description**: Commit changes to Git.

**Steps:**
- [ ] Create feature branch: `git checkout -b feature/api-gateway`
- [ ] Stage changes: `git add .`
- [ ] Commit: `git commit -m "feat: implement API Gateway with JWT validation, rate limiting, and routing"`
- [ ] Push: `git push origin feature/api-gateway`
- [ ] Create pull request to `develop` branch

**Estimated Time**: 10 minutes

---

## Summary

### Total Estimated Time: ~10 hours

### Task Breakdown:
- **Phase 1**: Project Setup (30 min)
- **Phase 2**: JWT Validation (2 hours)
- **Phase 3**: Route Configuration (1 hour)
- **Phase 4**: Rate Limiting (2 hours)
- **Phase 5**: CORS Configuration (45 min)
- **Phase 6**: Error Handling (1 hour)
- **Phase 7**: Integration Testing (1.5 hours)
- **Phase 8**: Docker & Deployment (1 hour)
- **Phase 9**: Documentation (1 hour)
- **Phase 10**: Final Verification (1 hour)

### Key Milestones:
1. ✅ JWT validation working
2. ✅ All routes configured
3. ✅ Rate limiting functional
4. ✅ All tests passing (>80% coverage)
5. ✅ Docker deployment working
6. ✅ Documentation complete

### Dependencies:
- Eureka Server must be running
- JWT secret must be configured
- Docker and Docker Compose installed

---

**Created**: April 10, 2026  
**Author**: MedFlow Team  
**Status**: 📋 Ready for Implementation  
**Version**: 1.0.0
