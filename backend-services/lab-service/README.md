# Lab Service

Laboratory Management Microservice for MedFlow HIS

## Overview

The Lab Service handles laboratory order management, sample collection tracking, and lab result uploads for the MedFlow Hospital Information System. It provides a complete workflow from receiving lab orders to delivering results to patients and doctors.

**Architecture**: MVC (Model-View-Controller)  
**Port**: 8084  
**Database Schema**: lab_schema  
**Service Discovery**: Registered as LAB-SERVICE in Eureka

## Features

- **Lab Order Management**: Receive and track laboratory orders from Clinical Service
- **Sample Collection**: Register sample collection with technician tracking
- **Result Upload**: Upload PDF lab results with automatic order completion
- **Patient Results**: Query lab results with role-based access control
- **Status Workflow**: PENDING → IN_PROGRESS → COMPLETED
- **Spanish Error Messages**: All error messages in Spanish for user-friendly experience

## API Endpoints

### Lab Orders

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/lab/orders/notify` | Receive order from Clinical Service | SYSTEM |
| GET | `/api/lab/orders` | List orders (optional status filter) | LABORATORY, ADMIN |
| GET | `/api/lab/orders/{id}` | Get order by ID | LABORATORY, DOCTOR, ADMIN |
| PUT | `/api/lab/orders/{id}/collect` | Register sample collection | LABORATORY |
| PUT | `/api/lab/orders/{id}/result` | Upload result PDF | LABORATORY |

### Lab Results

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/lab/results/{patientId}` | Get patient results | DOCTOR, PATIENT, ADMIN |

## Request/Response Examples

### Create Lab Order
```bash
POST /api/lab/orders/notify
Content-Type: application/json

{
  "orderCode": "AB12CD34",
  "patientId": "patient-uuid",
  "doctorId": "doctor-uuid",
  "testNames": ["Hemograma", "Glucosa"]
}

Response 201:
{
  "id": "order-uuid",
  "orderCode": "AB12CD34",
  "patientId": "patient-uuid",
  "doctorId": "doctor-uuid",
  "testNames": ["Hemograma", "Glucosa"],
  "status": "PENDING",
  "orderedAt": "2024-01-15T10:30:00"
}
```

### Collect Sample
```bash
PUT /api/lab/orders/{orderId}/collect
X-User-Id: tech-uuid

Response 200:
{
  "id": "order-uuid",
  "status": "IN_PROGRESS",
  "sampleId": "sample-uuid",
  ...
}
```

### Upload Result
```bash
PUT /api/lab/orders/{orderId}/result
Content-Type: multipart/form-data
X-User-Id: tech-uuid

file: result.pdf

Response 200:
{
  "id": "order-uuid",
  "status": "COMPLETED",
  ...
}
```

### Get Patient Results
```bash
GET /api/lab/results/{patientId}
X-User-Id: user-uuid
X-User-Role: DOCTOR

Response 200:
[
  {
    "id": "result-uuid",
    "orderId": "order-uuid",
    "patientId": "patient-uuid",
    "resultFilePath": "/path/to/result.pdf",
    "uploadedAt": "2024-01-15T14:30:00",
    "uploadedBy": "tech-uuid"
  }
]
```

## Configuration

### application.yml
```yaml
server:
  port: 8084

spring:
  application:
    name: lab-service
  datasource:
    url: jdbc:postgresql://localhost:5432/medflow_db
    username: medflow_user
    password: medflow_pass
  jpa:
    properties:
      hibernate:
        default_schema: lab_schema

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

lab:
  results:
    storage-path: ./lab-results
```

### Environment Variables (Docker)
- `DB_HOST`: PostgreSQL host (default: postgres)
- `DB_PORT`: PostgreSQL port (default: 5432)
- `DB_NAME`: Database name (default: medflow_db)
- `DB_USER`: Database user (default: medflow_user)
- `DB_PASSWORD`: Database password (default: medflow_pass)
- `EUREKA_SERVER_URL`: Eureka server URL (default: http://eureka-server:8761/eureka/)
- `LAB_RESULTS_PATH`: Path for storing PDF results (default: /app/lab-results)

## Business Rules

1. **Status Transitions**:
   - Only PENDING orders can transition to IN_PROGRESS
   - Only IN_PROGRESS orders can transition to COMPLETED
   - Invalid transitions return 409 Conflict

2. **Role-Based Access**:
   - PATIENT role can only view their own results
   - DOCTOR and ADMIN can view any patient's results
   - Unauthorized access returns 403 Forbidden

3. **Result Ordering**:
   - Patient results are always returned in descending order by upload date
   - Most recent results appear first

4. **File Validation**:
   - Only PDF files are accepted for lab results
   - Files must not be empty
   - Invalid files return 400 Bad Request

## Database Schema

```sql
CREATE SCHEMA lab_schema;

CREATE TABLE lab_schema.lab_orders (
    id VARCHAR(36) PRIMARY KEY,
    order_code VARCHAR(8) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    test_names TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ordered_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE lab_schema.samples (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    collected_at TIMESTAMP NOT NULL,
    collected_by VARCHAR(36) NOT NULL
);

CREATE TABLE lab_schema.lab_results (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    result_file_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL,
    uploaded_by VARCHAR(36) NOT NULL
);
```

## Running Locally

### Prerequisites
- Java 17
- Maven 3.8+
- PostgreSQL 14+
- Eureka Server running on port 8761

### Steps
1. Create database schema:
```bash
psql -U medflow_user -d medflow_db -f src/main/resources/db/migration/V1__create_lab_schema.sql
```

2. Build the application:
```bash
mvn clean package
```

3. Run the application:
```bash
java -jar target/lab-service-1.0.0.jar
```

4. Verify health:
```bash
curl http://localhost:8084/actuator/health
```

## Running with Docker

### Build Image
```bash
docker build -t medflow/lab-service:1.0.0 .
```

### Run Container
```bash
docker run -d \
  --name lab-service \
  -p 8084:8084 \
  -e DB_HOST=postgres \
  -e DB_PORT=5432 \
  -e DB_NAME=medflow_db \
  -e DB_USER=medflow_user \
  -e DB_PASSWORD=medflow_pass \
  -e EUREKA_SERVER_URL=http://eureka-server:8761/eureka/ \
  -e SPRING_PROFILES_ACTIVE=docker \
  medflow/lab-service:1.0.0
```

### Docker Compose
```yaml
lab-service:
  build: ./backend-services/lab-service
  ports:
    - "8084:8084"
  environment:
    - SPRING_PROFILES_ACTIVE=docker
    - DB_HOST=postgres
    - EUREKA_SERVER_URL=http://eureka-server:8761/eureka/
  depends_on:
    - postgres
    - eureka-server
```

## Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn verify
```

### Test Coverage
```bash
mvn clean test jacoco:report
# Report available at: target/site/jacoco/index.html
```

## Error Handling

All errors return a consistent JSON structure with Spanish messages:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "No Encontrado",
  "message": "Orden de laboratorio no encontrada con ID: invalid-id",
  "path": "/api/lab/orders/invalid-id"
}
```

### Common Error Codes
- **400 Bad Request**: Invalid input data or validation errors
- **403 Forbidden**: Unauthorized access (e.g., patient accessing other's results)
- **404 Not Found**: Order or result not found
- **409 Conflict**: Invalid status transition
- **500 Internal Server Error**: Unexpected server errors

## Dependencies

### External Services
- **Clinical Service** (8083): Sends lab order notifications
- **Patient Service** (8082): Patient demographic data (via HTTP)
- **API Gateway** (8080): Request routing and JWT validation
- **Eureka Server** (8761): Service discovery and registration

### Database
- **PostgreSQL**: lab_schema for all lab-related data
- **No cross-schema joins**: Data isolation enforced

## Architecture Notes

- **MVC Pattern**: Clean separation of concerns (Controller → Service → Repository)
- **Zero Cross-Schema Joins**: Complete data isolation from other services
- **File Storage**: MVP uses filesystem; future versions will use S3/MinIO
- **Stateless**: No session state; all authentication via JWT headers
- **Idempotent Migrations**: Database migrations can be run multiple times safely

## Monitoring

### Health Check
```bash
curl http://localhost:8084/actuator/health
```

### Metrics
```bash
curl http://localhost:8084/actuator/metrics
```

### Eureka Dashboard
Check service registration at: http://localhost:8761

## Future Enhancements

- [ ] S3/MinIO integration for PDF storage
- [ ] Result download endpoint with streaming
- [ ] Lab order cancellation workflow
- [ ] Email notifications for completed results
- [ ] Result preview/thumbnail generation
- [ ] Batch result upload
- [ ] Advanced search and filtering

## License

Copyright © 2024 MedFlow HIS. All rights reserved.

---

**Version**: 1.0.0  
**Last Updated**: 2024-01-15  
**Maintainer**: MedFlow Development Team
