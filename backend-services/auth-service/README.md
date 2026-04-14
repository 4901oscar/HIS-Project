# Auth Service

Authentication and Authorization microservice for MedFlow HIS. Handles user login, JWT token generation/validation, and role-based access control (RBAC).

## Overview

The Auth Service is a Spring Boot microservice that provides:

- **User Authentication**: Login for staff and patients with username/password
- **JWT Token Management**: Generation, validation, refresh, and blacklisting
- **Role-Based Access Control (RBAC)**: 8 roles (ADMIN, DOCTOR, PATIENT, etc.)
- **Rate Limiting**: Protection against brute-force attacks (5 attempts/minute)
- **Token Blacklist**: Logout functionality with token invalidation
- **Service Discovery**: Registers with Eureka Server

**Key Features:**
- BCrypt password hashing (strength 10)
- JWT tokens with 24-hour expiration
- Stateless authentication (except blacklist)
- RESTful API with 5 endpoints
- PostgreSQL database with dedicated schema
- Docker support with health checks

## Prerequisites

### Required Software
- **Java 17** or higher
- **Maven 3.8+** for building
- **PostgreSQL 15+** for database
- **Eureka Server** running on port 8761

### Optional
- **Docker** and **Docker Compose** for containerized deployment
- **Redis** (future enhancement for distributed blacklist)

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_USERNAME` | PostgreSQL username | `medflow_user` | No |
| `DB_PASSWORD` | PostgreSQL password | `medflow_pass` | No |
| `JWT_SECRET` | Secret key for JWT signing (256-bit min) | `default-secret-key...` | **Yes (Production)** |
| `EUREKA_SERVER_URL` | Eureka server URL | `http://localhost:8761/eureka/` | No |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | - | No |

### application.yml Settings

```yaml
server:
  port: 8081

spring:
  application:
    name: auth-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/medflow_db?currentSchema=auth_schema
    username: ${DB_USERNAME:medflow_user}
    password: ${DB_PASSWORD:medflow_pass}
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: auth_schema

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}

jwt:
  secret: ${JWT_SECRET:default-secret-key-change-in-production-256-bits-minimum}
  expiration: 86400000 # 24 hours in milliseconds
```

### Database Schema

The service uses the `auth_schema` schema in PostgreSQL with three tables:

- **users**: User accounts (staff and patients)
- **roles**: System roles (8 predefined roles)
- **user_roles**: Many-to-many relationship

Schema is automatically created on startup via `schema.sql`.

## Running Locally

### Step 1: Start PostgreSQL

```bash
# Using Docker
docker run -d \
  --name medflow-postgres \
  -e POSTGRES_DB=medflow_db \
  -e POSTGRES_USER=medflow_user \
  -e POSTGRES_PASSWORD=medflow_pass \
  -p 5432:5432 \
  postgres:15-alpine

# Or use existing PostgreSQL installation
# Ensure database 'medflow_db' exists
```

### Step 2: Start Eureka Server

```bash
# Navigate to eureka-server directory
cd backend-cloud/eureka-server

# Run Eureka Server
mvn spring-boot:run

# Verify at http://localhost:8761
```

### Step 3: Set JWT Secret (Production)

```bash
# Linux/Mac
export JWT_SECRET="your-secure-256-bit-secret-key-here-change-this-in-production"

# Windows (PowerShell)
$env:JWT_SECRET="your-secure-256-bit-secret-key-here-change-this-in-production"
```

### Step 4: Build and Run Auth Service

```bash
# Navigate to auth-service directory
cd backend-services/auth-service

# Build the project
mvn clean package -DskipTests

# Run the service
mvn spring-boot:run

# Or run the JAR directly
java -jar target/auth-service-1.0.0.jar
```

### Step 5: Verify Service is Running

```bash
# Check health endpoint
curl http://localhost:8081/actuator/health

# Expected response:
# {"status":"UP"}

# Check Eureka registration
# Visit http://localhost:8761 and verify AUTH-SERVICE is registered
```

## Running with Docker

### Using Docker Compose (Recommended)

```bash
# Start all services (from project root)
docker-compose up -d postgres eureka-server auth-service

# View logs
docker-compose logs -f auth-service

# Stop services
docker-compose down
```

### Using Docker Directly

```bash
# Build image
docker build -t medflow/auth-service:latest .

# Run container
docker run -d \
  --name medflow-auth-service \
  -p 8081:8081 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/ \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/medflow_db?currentSchema=auth_schema \
  -e JWT_SECRET=your-secret-key \
  --network medflow-network \
  medflow/auth-service:latest
```

## API Endpoints

### 1. Login

Authenticate user and receive JWT token.

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "username": "doctor1",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiIxMjMiLCJ1c2VybmFtZSI6ImRvY3RvcjEiLCJyb2xlcyI6IkRPQ1RPUixBRE1JTiIsImlhdCI6MTcxMzAyNDAwMCwiZXhwIjoxNzEzMTEwNDAwfQ.signature",
  "expiresIn": 86400,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "doctor1",
    "email": "doctor1@medflow.com",
    "fullName": "Dr. Juan Pérez",
    "roles": ["DOCTOR", "ADMIN"],
    "active": true
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"doctor1","password":"password123"}'
```

---

### 2. Logout

Invalidate JWT token (add to blacklist).

**Endpoint:** `POST /api/auth/logout`

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 3. Refresh Token

Generate new JWT from valid token.

**Endpoint:** `POST /api/auth/refresh`

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 4. Validate Token

Validate JWT and extract claims (used by API Gateway).

**Endpoint:** `GET /api/auth/validate?token=<jwt-token>`

**Response (200 OK - Valid Token):**
```json
{
  "valid": true,
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "doctor1",
  "roles": "DOCTOR,ADMIN"
}
```

**Response (200 OK - Invalid Token):**
```json
{
  "valid": false,
  "userId": null,
  "username": null,
  "roles": null
}
```

**cURL Example:**
```bash
curl "http://localhost:8081/api/auth/validate?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 5. Get Current User

Get authenticated user information.

**Endpoint:** `GET /api/auth/me`

**Headers:**
```
Authorization: Bearer <jwt-token>
```

**Response (200 OK):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "doctor1",
  "email": "doctor1@medflow.com",
  "fullName": "Dr. Juan Pérez",
  "roles": ["DOCTOR", "ADMIN"],
  "active": true
}
```

**cURL Example:**
```bash
curl http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Testing

### Run All Tests

```bash
# Run unit and integration tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Coverage

The project maintains **>80% code coverage** with:

- **Unit Tests**: Service layer, JWT logic, rate limiting
- **Integration Tests**: Full authentication flows, database operations
- **Property-Based Tests**: JWT validation, rate limiting edge cases

### Manual Testing

See [QUICKSTART.md](./QUICKSTART.md) for step-by-step manual testing instructions.

## Architecture

### Component Overview

```
┌─────────────────────────────────────────┐
│         AuthController (REST)           │
│  - POST /login, /logout, /refresh       │
│  - GET /validate, /me                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Service Layer                   │
│  - AuthService (business logic)         │
│  - JwtService (token operations)        │
│  - TokenBlacklistService (logout)       │
│  - LoginRateLimiter (security)          │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Repository Layer                │
│  - UserRepository (JPA)                 │
│  - RoleRepository (JPA)                 │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      PostgreSQL (auth_schema)           │
│  - users, roles, user_roles             │
└─────────────────────────────────────────┘
```

### Key Components

**AuthController**: REST endpoints for authentication operations

**AuthService**: Business logic for login, logout, token refresh

**JwtService**: JWT generation, validation, and claim extraction

**TokenBlacklistService**: In-memory blacklist for invalidated tokens

**LoginRateLimiter**: Rate limiting to prevent brute-force attacks

**UserRepository**: JPA repository for user data access

**RoleRepository**: JPA repository for role data access

### Database Schema

**users table:**
- id (UUID, primary key)
- username (unique, indexed)
- password (BCrypt hashed)
- email (unique, indexed)
- full_name
- active (boolean)
- created_at, updated_at

**roles table:**
- id (bigserial, primary key)
- name (enum: ADMIN, DOCTOR, PATIENT, etc.)
- description

**user_roles table:**
- user_id (foreign key to users)
- role_id (foreign key to roles)
- Composite primary key

## Security

### Password Security

- **BCrypt Hashing**: All passwords hashed with BCrypt (strength 10)
- **No Plain Text**: Passwords never stored or logged in plain text
- **Validation**: Minimum 8 characters (enforced at application level)

### JWT Security

- **Algorithm**: HS256 (HMAC with SHA-256)
- **Secret Key**: 256-bit minimum, configurable via environment variable
- **Expiration**: 24 hours (86400 seconds)
- **Claims**: userId, username, roles, issued-at, expiration
- **Signature Verification**: All tokens verified before use

### Rate Limiting

- **Login Attempts**: Maximum 5 attempts per minute per IP address
- **Tracking**: Per-IP address using ConcurrentHashMap
- **Reset**: Automatic reset on successful login
- **Response**: HTTP 429 (Too Many Requests) when limit exceeded

### Token Blacklist

- **Logout**: Tokens added to blacklist on logout
- **Storage**: In-memory ConcurrentHashMap (Phase 1)
- **Cleanup**: Automatic removal after token expiration
- **Validation**: All tokens checked against blacklist before use

### Best Practices

1. **Always use HTTPS** in production
2. **Change default JWT secret** before deployment
3. **Rotate JWT secrets** periodically
4. **Monitor failed login attempts** for security threats
5. **Use strong passwords** (minimum 8 characters)
6. **Implement Redis** for distributed blacklist in production

## Troubleshooting

### Service Won't Start

**Problem**: Application fails to start

**Solutions:**
- Check PostgreSQL is running: `docker ps | grep postgres`
- Verify database exists: `psql -U medflow_user -d medflow_db -c "\l"`
- Check Eureka Server is running: `curl http://localhost:8761`
- Review logs: `docker-compose logs auth-service`

### Database Connection Errors

**Problem**: `Connection refused` or `Authentication failed`

**Solutions:**
- Verify PostgreSQL credentials in `application.yml`
- Check database URL and schema name
- Ensure PostgreSQL accepts connections: `pg_hba.conf`
- Test connection: `psql -h localhost -U medflow_user -d medflow_db`

### JWT Validation Fails

**Problem**: Tokens rejected as invalid

**Solutions:**
- Verify JWT secret matches across services
- Check token expiration (24 hours)
- Ensure token not in blacklist (after logout)
- Validate token format: `Authorization: Bearer <token>`

### Rate Limiting Issues

**Problem**: Getting 429 errors unexpectedly

**Solutions:**
- Wait 1 minute for rate limit reset
- Check IP address extraction (X-Forwarded-For header)
- Review rate limiter configuration
- Clear rate limiter state (restart service)

### Eureka Registration Fails

**Problem**: Service not appearing in Eureka dashboard

**Solutions:**
- Verify Eureka URL in configuration
- Check network connectivity to Eureka Server
- Review Eureka client logs
- Wait 30 seconds for registration to complete

## Development

### Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/medflow/auth/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── service/         # Business logic
│   │   │   ├── repository/      # JPA repositories
│   │   │   ├── domain/          # Entities (User, Role)
│   │   │   ├── dto/             # Request/Response DTOs
│   │   │   ├── exception/       # Custom exceptions
│   │   │   └── config/          # Configuration classes
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-docker.yml
│   │       └── schema.sql
│   └── test/
│       └── java/com/medflow/auth/
│           ├── controller/      # Controller tests
│           ├── service/         # Service tests
│           └── repository/      # Repository tests
├── Dockerfile
├── pom.xml
└── README.md
```

### Building

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Build Docker image
docker build -t medflow/auth-service:latest .
```

### Code Quality

```bash
# Run tests with coverage
mvn clean test jacoco:report

# Check coverage (target: >80%)
open target/site/jacoco/index.html
```

## Dependencies

### Core Dependencies
- Spring Boot 3.2.4
- Spring Security
- Spring Data JPA
- JJWT 0.11.5 (JWT library)
- PostgreSQL Driver
- Eureka Client

### Testing Dependencies
- JUnit 5
- Mockito
- Spring Boot Test
- jqwik (Property-Based Testing)
- Testcontainers (PostgreSQL)

## License

Copyright © 2026 MedFlow Team. All rights reserved.

## Support

For issues or questions:
- Check [QUICKSTART.md](./QUICKSTART.md) for common scenarios
- Review logs: `docker-compose logs auth-service`
- Contact: MedFlow Development Team

---

**Version**: 1.0.0  
**Last Updated**: April 13, 2026  
**Service Port**: 8081  
**Eureka Name**: AUTH-SERVICE
