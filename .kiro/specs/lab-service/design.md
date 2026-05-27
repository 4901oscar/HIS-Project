# Design: Lab Service

## 1. Architecture Overview

**Arquitectura**: MVC (Controller → Service → Repository → Entity)  
**Puerto**: 8084 | **Schema**: lab_schema

```
┌─────────────────────────────────────┐
│         LAB SERVICE (8084)          │
│  ┌─────────────────────────────┐   │
│  │     Controller Layer        │   │
│  │  - LabOrderController       │   │
│  │  - LabResultController      │   │
│  └──────────────┬──────────────┘   │
│  ┌──────────────▼──────────────┐   │
│  │      Service Layer          │   │
│  │  - LabOrderService          │   │
│  │  - LabResultService         │   │
│  └──────────────┬──────────────┘   │
│  ┌──────────────▼──────────────┐   │
│  │    Repository Layer         │   │
│  │  - LabOrderRepository       │   │
│  │  - SampleRepository         │   │
│  │  - LabResultRepository      │   │
│  └──────────────┬──────────────┘   │
│  ┌──────────────▼──────────────┐   │
│  │  PostgreSQL (lab_schema)    │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

## 2. Directory Structure

```
lab-service/
├── controller/
│   ├── LabOrderController.java
│   └── LabResultController.java
├── service/
│   ├── LabOrderService.java
│   └── LabResultService.java
├── repository/
│   ├── LabOrderRepository.java
│   ├── SampleRepository.java
│   └── LabResultRepository.java
├── model/
│   ├── LabOrder.java          # Entity
│   ├── Sample.java            # Entity
│   ├── LabResult.java         # Entity
│   └── OrderStatus.java       # Enum
├── dto/
│   ├── request/
│   │   ├── LabOrderNotificationRequest.java
│   │   ├── CollectSampleRequest.java
│   │   └── UploadResultRequest.java
│   └── response/
│       ├── LabOrderResponse.java
│       └── LabResultResponse.java
├── exception/
│   ├── LabOrderNotFoundException.java
│   ├── InvalidOrderStatusException.java
│   └── GlobalExceptionHandler.java
└── config/
    └── SecurityConfig.java
```

## 3. Data Model

### Entities

```java
@Entity @Table(name = "lab_orders", schema = "lab_schema")
public class LabOrder {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String orderCode;       // From Clinical Service (8 chars)
    private String patientId;
    private String doctorId;
    @Convert(converter = StringListConverter.class)
    private List<String> testNames;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;     // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    private LocalDateTime orderedAt;
    private LocalDateTime updatedAt;
}

@Entity @Table(name = "samples", schema = "lab_schema")
public class Sample {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String orderId;
    private LocalDateTime collectedAt;
    private String collectedBy;
}

@Entity @Table(name = "lab_results", schema = "lab_schema")
public class LabResult {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String orderId;
    private String patientId;
    private String resultFilePath;  // PDF path
    private LocalDateTime uploadedAt;
    private String uploadedBy;
}
```

### Database Schema

```sql
CREATE SCHEMA IF NOT EXISTS lab_schema;

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

CREATE INDEX idx_lab_orders_patient ON lab_schema.lab_orders(patient_id);
CREATE INDEX idx_lab_orders_status ON lab_schema.lab_orders(status);
CREATE INDEX idx_lab_results_patient ON lab_schema.lab_results(patient_id);
```

## 4. API Design

### POST /api/lab/orders/notify
Recibe notificación de Clinical Service.
```json
Request: { "orderCode": "AB12CD34", "patientId": "...", "doctorId": "...", "testNames": ["Hemograma", "Glucosa"] }
Response 201: { "id": "...", "orderCode": "AB12CD34", "status": "PENDING" }
```

### GET /api/lab/orders?status=PENDING
```json
Response 200: [ { "id": "...", "orderCode": "...", "patientId": "...", "testNames": [...], "status": "PENDING", "orderedAt": "..." } ]
```

### PUT /api/lab/orders/{id}/collect
```json
Request: {} (headers: X-User-Id)
Response 200: { "id": "...", "status": "IN_PROGRESS", "sampleId": "..." }
```

### PUT /api/lab/orders/{id}/result
```json
Request: multipart/form-data { file: PDF }
Response 200: { "id": "...", "status": "COMPLETED", "resultId": "..." }
```

### GET /api/lab/results/{patientId}
```json
Response 200: [ { "id": "...", "orderId": "...", "resultFilePath": "...", "uploadedAt": "..." } ]
```

## 5. Correctness Properties

### Property 1: Order Status Transitions are Valid
Solo transiciones válidas: PENDING→IN_PROGRESS, IN_PROGRESS→COMPLETED, cualquier→CANCELLED.
Transiciones inválidas lanzan InvalidOrderStatusException.

### Property 2: Results are Returned in Descending Date Order
Para cualquier paciente con múltiples resultados, GET /api/lab/results/{patientId} retorna ordenados por uploadedAt DESC.

### Property 3: Patient Can Only Access Own Results
Usuario con rol PATIENT solo puede ver resultados donde patientId == userId del JWT.

## 6. Configuration

```yaml
server:
  port: 8084
spring:
  application:
    name: lab-service
  datasource:
    url: jdbc:postgresql://localhost:5432/medflow_db
  jpa:
    properties:
      hibernate:
        default_schema: lab_schema
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER_URL:http://localhost:8761/eureka/}
```

---
**Created**: April 16, 2026 | **Status**: ✅ Ready | **Version**: 1.0.0
