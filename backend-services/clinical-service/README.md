# Clinical Service

Clinical management microservice for MedFlow HIS. Implements Manchester Triage System, appointment scheduling with real-time slot management, vital signs recording, medical consultations, prescriptions, and lab orders.

## Overview

The Clinical Service is the core microservice of MedFlow HIS, built with **Hexagonal Architecture** (Ports and Adapters) to handle complex clinical business logic. It provides:

- **Manchester Triage System**: Automatic priority calculation (Red, Orange, Yellow, Green, Blue)
- **Appointment Management**: Real-time slot availability using Redis cache
- **Vital Signs Recording**: Physiological measurements with automatic BMI calculation
- **Medical Consultations**: Complete consultation records with CIE-10 diagnoses
- **Prescription Generation**: Medical prescriptions with unique codes
- **Lab Order Generation**: Laboratory test orders with unique codes
- **Medical History**: Comprehensive patient clinical history aggregation

**Key Features:**
- Hexagonal architecture with pure domain logic
- Redis-based real-time appointment slot management
- Manchester Triage Algorithm implementation
- Circuit breaker for external service calls
- Eventual consistency for pharmacy/lab notifications
- 105+ tests including property-based tests
- Docker support with health checks

## Prerequisites

### Required Software
- **Java 17** or higher
- **Maven 3.8+** for building
- **PostgreSQL 15+** for database
- **Redis 7+** for appointment slot caching
- **Eureka Server** running on port 8761

### Optional
- **Docker** and **Docker Compose** for containerized deployment
- **Patient Service** on port 8082 (for patient data)
- **Pharmacy Service** on port 8085 (for prescription notifications)
- **Lab Service** on port 8084 (for lab order notifications)

## Architecture

### Hexagonal Architecture Overview

The Clinical Service follows Hexagonal Architecture (Ports and Adapters) to isolate business logic from infrastructure concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                     CLINICAL SERVICE (8083)                      │
│                                                                  │
│  ┌────────────────────────────────────────────────────────┐    │
│  │              INFRASTRUCTURE LAYER (IN)                  │    │
│  │  REST Controllers: Triage, VitalSigns, Appointment,    │    │
│  │  Consultation, Prescription, LabOrder, MedicalHistory  │    │
│  └─────────────────────┬──────────────────────────────────┘    │
│                        │                                        │
│  ┌─────────────────────▼──────────────────────────────────┐    │
│  │              APPLICATION LAYER (Use Cases)              │    │
│  │  - PerformTriageUseCase                                 │    │
│  │  - RecordVitalSignsUseCase                              │    │
│  │  - ManageAppointmentUseCase                             │    │
│  │  - RegisterConsultationUseCase                          │    │
│  │  - GeneratePrescriptionUseCase                          │    │
│  │  - GenerateLabOrderUseCase                              │    │
│  │  - GetMedicalHistoryUseCase                             │    │
│  └─────────────────────┬───────────────────────────────────┘    │
│                        │                                        │
│  ┌─────────────────────▼───────────────────────────────────┐    │
│  │                  DOMAIN LAYER                           │    │
│  │  Domain Services:                                       │    │
│  │  - TriageEngine (Manchester Algorithm)                  │    │
│  │  - AppointmentManager (Slot Management)                 │    │
│  │  - VitalSignsRecorder, ConsultationManager              │    │
│  │  - PrescriptionGenerator, LabOrderGenerator             │    │
│  │  - MedicalHistoryAggregator                             │    │
│  │                                                          │    │
│  │  Domain Model:                                          │    │
│  │  - Triage, VitalSigns, Appointment, Consultation        │    │
│  │  - Prescription, LabOrder, ManchesterMotif              │    │
│  └─────────────────────┬───────────────────────────────────┘    │
│                        │                                        │
│  ┌─────────────────────▼───────────────────────────────────┐    │
│  │         INFRASTRUCTURE LAYER (OUT - Adapters)           │    │
│  │  - JPA Repositories (PostgreSQL)                        │    │
│  │  - Redis Cache (Appointment Slots)                      │    │
│  │  - Feign Clients (Patient, Pharmacy, Lab Services)     │    │
│  └─────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

1. **Hexagonal Architecture**: Isolates complex business logic (Manchester Triage) from infrastructure
2. **Redis for Real-Time Slots**: Ensures atomic slot reservation and prevents double-booking
3. **Eventual Consistency**: Pharmacy/Lab notifications don't block transactions
4. **Circuit Breaker**: Protects against cascading failures from Patient Service
5. **Zero Cross-Schema JOINs**: Uses HTTP calls instead of database joins

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_USERNAME` | PostgreSQL username | `medflow_user` | No |
| `DB_PASSWORD` | PostgreSQL password | `medflow_pass` | No |
| `REDIS_HOST` | Redis server host | `localhost` | No |
| `REDIS_PORT` | Redis server port | `6379` | No |
| `REDIS_PASSWORD` | Redis password | - | No |
| `EUREKA_SERVER_URL` | Eureka server URL | `http://localhost:8761/eureka/` | No |
| `PATIENT_SERVICE_URL` | Patient Service URL | `http://localhost:8082` | No |
| `PHARMACY_SERVICE_URL` | Pharmacy Service URL | `http://localhost:8085` | No |
| `LAB_SERVICE_URL` | Lab Service URL | `http://localhost:8084` | No |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | - | No |

### application.yml Settings

```yaml
server:
  port: 8083

spring:
  application:
    name: clinical-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/medflow_db?currentSchema=clinical_schema
    username: ${DB_USERNAME:medflow_user}
    password: ${DB_PASSWORD:medflow_pass}
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        default_schema: clinical_schema
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}

services:
  patient-service:
    url: ${PATIENT_SERVICE_URL:http://localhost:8082}
  pharmacy-service:
    url: ${PHARMACY_SERVICE_URL:http://localhost:8085}
  lab-service:
    url: ${LAB_SERVICE_URL:http://localhost:8084}
```

### Database Schema

The service uses the `clinical_schema` schema in PostgreSQL with tables:

- **triages**: Manchester triage records
- **vital_signs**: Patient vital signs measurements
- **appointments**: Medical appointments with slot management
- **consultations**: Medical consultation records
- **prescriptions**: Medical prescriptions
- **lab_orders**: Laboratory test orders
- **manchester_motifs**: Triage motifs catalog
- **manchester_discriminators**: Triage discriminators catalog

Schema is automatically created via Flyway migrations.

## Running Locally

### Step 1: Start PostgreSQL and Redis

```bash
# Using Docker
docker run -d \
  --name medflow-postgres \
  -e POSTGRES_DB=medflow_db \
  -e POSTGRES_USER=medflow_user \
  -e POSTGRES_PASSWORD=medflow_pass \
  -p 5432:5432 \
  postgres:15-alpine

docker run -d \
  --name medflow-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### Step 2: Start Eureka Server

```bash
cd backend-cloud/eureka-server
mvn spring-boot:run

# Verify at http://localhost:8761
```

### Step 3: Start Patient Service (Optional but Recommended)

```bash
cd backend-services/patient-service
mvn spring-boot:run

# Runs on port 8082
```

### Step 4: Build and Run Clinical Service

```bash
cd backend-services/clinical-service

# Build the project
mvn clean package -DskipTests

# Run the service
mvn spring-boot:run

# Or run the JAR directly
java -jar target/clinical-service-1.0.0.jar
```

### Step 5: Verify Service is Running

```bash
# Check health endpoint
curl http://localhost:8083/actuator/health

# Expected response:
# {"status":"UP"}

# Check Eureka registration
# Visit http://localhost:8761 and verify CLINICAL-SERVICE is registered
```

## Running with Docker

### Using Docker Compose (Recommended)

```bash
# Start all services (from project root)
docker-compose up -d postgres redis eureka-server patient-service clinical-service

# View logs
docker-compose logs -f clinical-service

# Stop services
docker-compose down
```

### Using Docker Directly

```bash
# Build image
docker build -t medflow/clinical-service:latest .

# Run container
docker run -d \
  --name medflow-clinical-service \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/ \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/medflow_db?currentSchema=clinical_schema \
  -e SPRING_DATA_REDIS_HOST=redis \
  --network medflow-network \
  medflow/clinical-service:latest
```

## API Endpoints

### 1. Perform Triage (Manchester System)

Calculate patient priority using Manchester Triage System.

**Endpoint:** `POST /api/clinical/triage`

**Headers:**
```
X-User-Id: <doctor-id>
X-User-Roles: DOCTOR
```

**Request:**
```json
{
  "patientId": "patient-123",
  "motifId": "M01",
  "discriminatorIds": ["D01", "D02"]
}
```

**Response (200 OK):**
```json
{
  "id": "triage-456",
  "patientId": "patient-123",
  "priorityLevel": "ORANGE",
  "priorityDescription": "Muy urgente",
  "maxWaitTimeMinutes": 10,
  "performedAt": "2026-04-14T10:30:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8083/api/clinical/triage \
  -H "Content-Type: application/json" \
  -H "X-User-Id: doctor-123" \
  -H "X-User-Roles: DOCTOR" \
  -d '{
    "patientId": "patient-123",
    "motifId": "M01",
    "discriminatorIds": ["D01", "D02"]
  }'
```

---

### 2. Record Vital Signs

Record patient vital signs with automatic BMI calculation.

**Endpoint:** `POST /api/clinical/vital-signs`

**Headers:**
```
X-User-Id: <user-id>
X-User-Roles: VITAL_SIGNS or DOCTOR
```

**Request:**
```json
{
  "patientId": "patient-123",
  "systolicPressure": 120,
  "diastolicPressure": 80,
  "heartRate": 75,
  "respiratoryRate": 16,
  "temperature": 36.5,
  "oxygenSaturation": 98,
  "weight": 70.5,
  "height": 175.0
}
```

**Response (200 OK):**
```json
{
  "id": "vitals-789",
  "patientId": "patient-123",
  "systolicPressure": 120,
  "diastolicPressure": 80,
  "heartRate": 75,
  "respiratoryRate": 16,
  "temperature": 36.5,
  "oxygenSaturation": 98,
  "weight": 70.5,
  "height": 175.0,
  "bmi": 23.02,
  "recordedAt": "2026-04-14T10:15:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8083/api/clinical/vital-signs \
  -H "Content-Type: application/json" \
  -H "X-User-Id: nurse-456" \
  -H "X-User-Roles: VITAL_SIGNS" \
  -d '{
    "patientId": "patient-123",
    "systolicPressure": 120,
    "diastolicPressure": 80,
    "heartRate": 75,
    "respiratoryRate": 16,
    "temperature": 36.5,
    "oxygenSaturation": 98,
    "weight": 70.5,
    "height": 175.0
  }'
```

---

### 3. Get Available Appointment Slots

Query real-time available appointment slots for a doctor.

**Endpoint:** `GET /api/clinical/appointments/slots?doctorId=<id>&date=<YYYY-MM-DD>`

**Response (200 OK):**
```json
{
  "doctorId": "doctor-123",
  "date": "2026-04-15",
  "availableSlots": [
    "08:00:00",
    "08:30:00",
    "09:00:00",
    "09:30:00",
    "10:00:00"
  ]
}
```

**cURL Example:**
```bash
curl "http://localhost:8083/api/clinical/appointments/slots?doctorId=doctor-123&date=2026-04-15"
```

---

### 4. Create Appointment

Create a new medical appointment with atomic slot reservation.

**Endpoint:** `POST /api/clinical/appointments`

**Headers:**
```
X-User-Id: <user-id>
X-User-Roles: ADMISSION or ADMIN
```

**Request:**
```json
{
  "patientId": "patient-123",
  "doctorId": "doctor-123",
  "appointmentDate": "2026-04-15",
  "appointmentTime": "09:00:00",
  "notes": "Consulta de control"
}
```

**Response (201 CREATED):**
```json
{
  "id": "appointment-789",
  "patientId": "patient-123",
  "doctorId": "doctor-123",
  "appointmentDate": "2026-04-15",
  "appointmentTime": "09:00:00",
  "status": "SCHEDULED",
  "createdAt": "2026-04-14T11:00:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8083/api/clinical/appointments \
  -H "Content-Type: application/json" \
  -H "X-User-Id: admission-456" \
  -H "X-User-Roles: ADMISSION" \
  -d '{
    "patientId": "patient-123",
    "doctorId": "doctor-123",
    "appointmentDate": "2026-04-15",
    "appointmentTime": "09:00:00",
    "notes": "Consulta de control"
  }'
```

---

### 5. Activate Appointment

Change appointment status from SCHEDULED to ACTIVE.

**Endpoint:** `PUT /api/clinical/appointments/{id}/activate`

**Response (200 OK):**
```
(Empty body)
```

**cURL Example:**
```bash
curl -X PUT http://localhost:8083/api/clinical/appointments/appointment-789/activate
```

---

### 6. Cancel Appointment

Cancel appointment and release slot in Redis.

**Endpoint:** `DELETE /api/clinical/appointments/{id}`

**Response (204 NO CONTENT):**
```
(Empty body)
```

**cURL Example:**
```bash
curl -X DELETE http://localhost:8083/api/clinical/appointments/appointment-789
```

---

### 7. Register Consultation

Register a complete medical consultation.

**Endpoint:** `POST /api/clinical/consultations`

**Headers:**
```
X-User-Id: <doctor-id>
X-User-Roles: DOCTOR
```

**Request:**
```json
{
  "patientId": "patient-123",
  "appointmentId": "appointment-789",
  "chiefComplaint": "Dolor de cabeza persistente",
  "symptoms": "Cefalea intensa, fotofobia",
  "primaryDiagnosis": "G43.9",
  "secondaryDiagnoses": ["R51"],
  "medicalNotes": "Paciente refiere dolor desde hace 3 días",
  "treatmentPlan": "Analgésicos y reposo"
}
```

**Response (201 CREATED):**
```json
{
  "id": "consultation-456",
  "patientId": "patient-123",
  "doctorId": "doctor-123",
  "chiefComplaint": "Dolor de cabeza persistente",
  "primaryDiagnosis": "G43.9",
  "secondaryDiagnoses": ["R51"],
  "consultationDate": "2026-04-15T09:15:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8083/api/clinical/consultations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: doctor-123" \
  -H "X-User-Roles: DOCTOR" \
  -d '{
    "patientId": "patient-123",
    "appointmentId": "appointment-789",
    "chiefComplaint": "Dolor de cabeza persistente",
    "symptoms": "Cefalea intensa, fotofobia",
    "primaryDiagnosis": "G43.9",
    "secondaryDiagnoses": ["R51"],
    "medicalNotes": "Paciente refiere dolor desde hace 3 días",
    "treatmentPlan": "Analgésicos y reposo"
  }'
```

---

### 8. Generate Prescription

Generate medical prescription with unique code.

**Endpoint:** `POST /api/clinical/prescriptions`

**Headers:**
```
X-User-Id: <doctor-id>
X-User-Roles: DOCTOR
```

**Request:**
```json
{
  "consultationId": "consultation-456",
  "patientId": "patient-123",
  "medications": [
    {
      "name": "Ibuprofeno",
      "dosage": "400mg",
      "frequency": "Cada 8 horas",
      "durationDays": 5,
      "route": "Oral",
      "specialInstructions": "Tomar con alimentos"
    }
  ]
}
```

**Response (201 CREATED):**
```json
{
  "id": "prescription-789",
  "prescriptionCode": "A1B2C3D4",
  "patientId": "patient-123",
  "doctorId": "doctor-123",
  "medications": [
    {
      "name": "Ibuprofeno",
      "dosage": "400mg",
      "frequency": "Cada 8 horas",
      "durationDays": 5,
      "route": "Oral",
      "specialInstructions": "Tomar con alimentos"
    }
  ],
  "status": "PENDING",
  "issuedAt": "2026-04-15T09:30:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8083/api/clinical/prescriptions \
  -H "Content-Type: application/json" \
  -H "X-User-Id: doctor-123" \
  -H "X-User-Roles: DOCTOR" \
  -d '{
    "consultationId": "consultation-456",
    "patientId": "patient-123",
    "medications": [{
      "name": "Ibuprofeno",
      "dosage": "400mg",
      "frequency": "Cada 8 horas",
      "durationDays": 5,
      "route": "Oral",
      "specialInstructions": "Tomar con alimentos"
    }]
  }'
```

---

### 9. Generate Lab Order

Generate laboratory test order with unique code.

**Endpoint:** `POST /api/clinical/lab-orders`

**Headers:**
```
X-User-Id: <doctor-id>
X-User-Roles: DOCTOR
```

**Request:**
```json
{
  "consultationId": "consultation-456",
  "patientId": "patient-123",
  "testNames": ["Hemograma completo", "Glucosa en sangre"]
}
```

**Response (201 CREATED):**
```json
{
  "id": "laborder-123",
  "orderCode": "E5F6G7H8",
  "patientId": "patient-123",
  "doctorId": "doctor-123",
  "testNames": ["Hemograma completo", "Glucosa en sangre"],
  "status": "PENDING",
  "orderedAt": "2026-04-15T09:35:00"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8083/api/clinical/lab-orders \
  -H "Content-Type: application/json" \
  -H "X-User-Id: doctor-123" \
  -H "X-User-Roles: DOCTOR" \
  -d '{
    "consultationId": "consultation-456",
    "patientId": "patient-123",
    "testNames": ["Hemograma completo", "Glucosa en sangre"]
  }'
```

---

### 10. Get Medical History

Retrieve complete medical history for a patient.

**Endpoint:** `GET /api/clinical/history/{patientId}`

**Headers:**
```
X-User-Id: <user-id>
X-User-Roles: DOCTOR or PATIENT
```

**Response (200 OK):**
```json
{
  "patient": {
    "id": "patient-123",
    "fullName": "Juan Pérez",
    "dateOfBirth": "1980-05-15",
    "gender": "M"
  },
  "consultations": [
    {
      "id": "consultation-456",
      "chiefComplaint": "Dolor de cabeza persistente",
      "primaryDiagnosis": "G43.9",
      "consultationDate": "2026-04-15T09:15:00"
    }
  ],
  "vitalSigns": [
    {
      "systolicPressure": 120,
      "diastolicPressure": 80,
      "heartRate": 75,
      "recordedAt": "2026-04-14T10:15:00"
    }
  ],
  "prescriptions": [
    {
      "prescriptionCode": "A1B2C3D4",
      "medications": ["Ibuprofeno 400mg"],
      "issuedAt": "2026-04-15T09:30:00"
    }
  ],
  "labOrders": [
    {
      "orderCode": "E5F6G7H8",
      "testNames": ["Hemograma completo"],
      "orderedAt": "2026-04-15T09:35:00"
    }
  ]
}
```

**cURL Example:**
```bash
curl http://localhost:8083/api/clinical/history/patient-123 \
  -H "X-User-Id: doctor-123" \
  -H "X-User-Roles: DOCTOR"
```

## Manchester Triage System

The Clinical Service implements the **Manchester Triage System**, a standardized clinical risk assessment tool used in emergency departments worldwide.

### Priority Levels

| Level | Color | Max Wait Time | Description |
|-------|-------|---------------|-------------|
| RED | Rojo | 0 minutes | Inmediato - Life-threatening |
| ORANGE | Naranja | 10 minutes | Muy urgente - Serious condition |
| YELLOW | Amarillo | 60 minutes | Urgente - Moderate urgency |
| GREEN | Verde | 120 minutes | Poco urgente - Minor condition |
| BLUE | Azul | 240 minutes | No urgente - Non-urgent |

### How It Works

1. **Select Motif**: Choose the chief complaint (e.g., chest pain, trauma, dyspnea)
2. **Select Discriminators**: Choose clinical discriminators from the motif
3. **Calculate Priority**: System automatically calculates the highest priority (lowest wait time) from selected discriminators
4. **Assign Queue**: Patient is added to the appropriate priority queue

### Algorithm

The Manchester Algorithm selects the **maximum priority** (minimum wait time) from all selected discriminators:

```
Priority = MIN(discriminator1.waitTime, discriminator2.waitTime, ...)
```

Example:
- Discriminator 1: YELLOW (60 min)
- Discriminator 2: ORANGE (10 min)
- **Result: ORANGE (10 min)** ← Highest priority wins

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

- **Unit Tests**: Domain services, use cases, controllers
- **Integration Tests**: Full clinical flows, database operations
- **Property-Based Tests**: Manchester algorithm, BMI calculation, slot management (17 properties using jqwik)

### Test Categories

**Domain Layer Tests:**
- Manchester Triage Algorithm (deterministic, priority selection)
- Vital Signs validation and BMI calculation
- Appointment state transitions
- Prescription/Lab Order code generation

**Application Layer Tests:**
- Use case implementations with role validation
- Permission checking

**Infrastructure Layer Tests:**
- Repository operations with TestContainers
- Redis cache operations
- Feign client integration with WireMock
- Controller integration tests with MockMvc

## Troubleshooting

### Service Won't Start

**Problem**: Application fails to start

**Solutions:**
- Check PostgreSQL is running: `docker ps | grep postgres`
- Check Redis is running: `docker ps | grep redis`
- Verify Eureka Server is running: `curl http://localhost:8761`
- Review logs: `docker-compose logs clinical-service`

### Database Connection Errors

**Problem**: `Connection refused` or `Authentication failed`

**Solutions:**
- Verify PostgreSQL credentials in `application.yml`
- Check database URL and schema name
- Ensure schema exists: `psql -U medflow_user -d medflow_db -c "\dn"`
- Test connection: `psql -h localhost -U medflow_user -d medflow_db`

### Redis Connection Errors

**Problem**: `Unable to connect to Redis`

**Solutions:**
- Verify Redis is running: `redis-cli ping` (should return PONG)
- Check Redis host and port in configuration
- Test connection: `redis-cli -h localhost -p 6379`

### Appointment Slot Conflicts

**Problem**: Getting 409 Conflict when creating appointments

**Solutions:**
- Check Redis for occupied slots: `redis-cli SMEMBERS appointment:slots:doctor-123:2026-04-15`
- Verify slot is actually available
- Check for race conditions (multiple simultaneous requests)
- Clear Redis cache if needed: `redis-cli FLUSHDB`

### Patient Service Unavailable

**Problem**: Circuit breaker open, patient data unavailable

**Solutions:**
- Verify Patient Service is running on port 8082
- Check circuit breaker status in logs
- Wait for circuit breaker to close (10 seconds)
- Review Resilience4j configuration

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
clinical-service/
├── src/main/java/com/medframe/clinical/
│   ├── domain/                      # DOMAIN LAYER (Pure Business Logic)
│   │   ├── model/                   # Entities
│   │   ├── port/in/                 # Input Ports (Use Cases)
│   │   ├── port/out/                # Output Ports (Repositories, Clients)
│   │   └── service/                 # Domain Services
│   ├── application/                 # APPLICATION LAYER
│   │   ├── usecase/                 # Use Case Implementations
│   │   └── service/                 # Application Services
│   ├── infrastructure/              # INFRASTRUCTURE LAYER
│   │   ├── rest/                    # REST Controllers (IN)
│   │   ├── persistence/             # JPA Repositories (OUT)
│   │   ├── client/                  # Feign Clients (OUT)
│   │   └── cache/                   # Redis Cache (OUT)
│   └── config/                      # Configuration
└── src/main/resources/
    ├── application.yml
    ├── application-docker.yml
    └── db/migration/                # Flyway migrations
```

### Building

```bash
# Clean build
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Build Docker image
docker build -t medflow/clinical-service:latest .
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
- Spring Data JPA
- Spring Data Redis
- Spring Cloud Netflix Eureka Client
- Spring Cloud OpenFeign
- Resilience4j (Circuit Breaker)
- PostgreSQL Driver
- Flyway (Database Migrations)

### Testing Dependencies
- JUnit 5
- Mockito
- Spring Boot Test
- jqwik 1.8.2 (Property-Based Testing)
- Testcontainers (PostgreSQL, Redis)
- WireMock (HTTP Client Testing)

## License

Copyright © 2026 MedFlow Team. All rights reserved.

## Support

For issues or questions:
- Review logs: `docker-compose logs clinical-service`
- Check health: `curl http://localhost:8083/actuator/health`
- Contact: MedFlow Development Team

---

**Version**: 1.0.0  
**Last Updated**: April 14, 2026  
**Service Port**: 8083  
**Eureka Name**: CLINICAL-SERVICE  
**Architecture**: Hexagonal (Ports and Adapters)
