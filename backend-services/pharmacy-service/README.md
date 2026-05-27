# Pharmacy Service

Microservicio responsable de la gestión de inventario de medicamentos y el despacho de recetas médicas en el sistema MedFlow HIS.

## Características

- **Gestión de Recetas**: Recepción y despacho de prescripciones médicas desde Clinical Service
- **Inventario de Medicamentos**: Control de stock con alertas de stock bajo
- **Despacho Transaccional**: Operaciones atómicas que garantizan consistencia entre stock y dispensaciones
- **Control de Acceso**: Validación de roles para operaciones sensibles

## Arquitectura

- **Patrón**: MVC (Model-View-Controller)
- **Puerto**: 8085
- **Base de Datos**: PostgreSQL (pharmacy_schema)
- **Registro**: Eureka (PHARMACY-SERVICE)

## Tecnologías

- Java 17
- Spring Boot 3.2.4
- Spring Data JPA
- PostgreSQL 15
- Flyway (migraciones)
- Spring Cloud Netflix Eureka
- Lombok
- Maven

## Estructura del Proyecto

```
pharmacy-service/
├── src/main/java/com/medflow/pharmacy/
│   ├── controller/          # REST Controllers
│   │   ├── PrescriptionController.java
│   │   └── MedicationController.java
│   ├── service/             # Business Logic
│   │   ├── PrescriptionService.java
│   │   └── InventoryService.java
│   ├── repository/          # Data Access
│   │   ├── PrescriptionRepository.java
│   │   ├── MedicationRepository.java
│   │   └── DispensationRepository.java
│   ├── model/               # Domain Entities
│   │   ├── Prescription.java
│   │   ├── Medication.java
│   │   ├── Dispensation.java
│   │   └── PrescriptionStatus.java
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/
│   │   └── response/
│   ├── exception/           # Exception Handling
│   │   ├── GlobalExceptionHandler.java
│   │   └── ...
│   └── PharmacyServiceApplication.java
└── src/main/resources/
    ├── application.yml
    ├── application-docker.yml
    └── db/migration/
        └── V1__create_pharmacy_schema.sql
```

## API Endpoints

### Prescripciones

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/api/pharmacy/prescriptions/notify` | Recibir receta desde Clinical Service | SYSTEM |
| GET | `/api/pharmacy/prescriptions` | Listar recetas (filtro por status) | PHARMACY, ADMIN |
| GET | `/api/pharmacy/prescriptions/{code}` | Buscar receta por código | PHARMACY, ADMIN |
| PUT | `/api/pharmacy/prescriptions/{id}/dispense` | Despachar receta | PHARMACY |
| GET | `/api/pharmacy/prescriptions/patient/{patientId}` | Recetas de un paciente | PATIENT, DOCTOR, ADMIN |

### Medicamentos

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/api/pharmacy/medications` | Listar medicamentos | PHARMACY, ADMIN |
| POST | `/api/pharmacy/medications` | Agregar medicamento | PHARMACY, ADMIN |
| PUT | `/api/pharmacy/medications/{id}/stock` | Actualizar stock | PHARMACY, ADMIN |
| GET | `/api/pharmacy/medications/low-stock` | Medicamentos con stock bajo | PHARMACY, ADMIN |

## Reglas de Negocio

- **BR1**: Solo recetas PENDING pueden ser despachadas
- **BR2**: No se puede despachar si stock insuficiente → error 409
- **BR3**: Paciente solo puede ver sus propias recetas
- **BR4**: CERO JOINs con otros esquemas
- **BR5**: Mensajes de error en español
- **BR6**: Stock nunca puede ser negativo

## Configuración

### Variables de Entorno

```yaml
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/medflow_db?currentSchema=pharmacy_schema
SPRING_DATASOURCE_USERNAME=medflow_user
SPRING_DATASOURCE_PASSWORD=medflow_pass

# Eureka
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://localhost:8761/eureka/
```

## Ejecución Local

### Prerrequisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- Eureka Server corriendo en puerto 8761

### Pasos

1. **Compilar el proyecto**:
```bash
mvn clean install
```

2. **Ejecutar la aplicación**:
```bash
mvn spring-boot:run
```

3. **Verificar salud**:
```bash
curl http://localhost:8085/actuator/health
```

## Ejecución con Docker

### Construir imagen

```bash
docker build -t pharmacy-service .
```

### Ejecutar con docker-compose

```bash
docker-compose up pharmacy-service
```

## Testing

### Ejecutar tests

```bash
mvn test
```

### Cobertura de tests

```bash
mvn jacoco:report
```

## Modelo de Datos

### Prescriptions

```sql
CREATE TABLE pharmacy_schema.prescriptions (
    id VARCHAR(36) PRIMARY KEY,
    prescription_code VARCHAR(8) UNIQUE NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    doctor_id VARCHAR(36) NOT NULL,
    medications_json TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    issued_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Medications

```sql
CREATE TABLE pharmacy_schema.medications (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    unit VARCHAR(50) NOT NULL,
    current_stock INTEGER NOT NULL DEFAULT 0,
    min_stock INTEGER NOT NULL DEFAULT 10,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

### Dispensations

```sql
CREATE TABLE pharmacy_schema.dispensations (
    id VARCHAR(36) PRIMARY KEY,
    prescription_id VARCHAR(36) NOT NULL,
    patient_id VARCHAR(36) NOT NULL,
    dispensed_at TIMESTAMP NOT NULL,
    dispensed_by VARCHAR(36) NOT NULL,
    dispensed_medications_json TEXT NOT NULL
);
```

## Dependencias

- **Clinical Service** (8083): Envía notificaciones de nuevas recetas
- **Patient Service** (8082): Para datos demográficos del paciente
- **API Gateway** (8080): Enrutamiento y validación JWT
- **Eureka Server** (8761): Service Discovery

## Monitoreo

### Actuator Endpoints

- Health: `http://localhost:8085/actuator/health`
- Info: `http://localhost:8085/actuator/info`
- Metrics: `http://localhost:8085/actuator/metrics`
- Prometheus: `http://localhost:8085/actuator/prometheus`

## Troubleshooting

### Error: "Stock insuficiente"

- Verificar stock actual del medicamento
- Actualizar stock si es necesario: `PUT /api/pharmacy/medications/{id}/stock`

### Error: "Solo se pueden despachar recetas con estado PENDIENTE"

- Verificar estado de la prescripción
- Solo prescripciones PENDING pueden ser despachadas

### Error de conexión a base de datos

- Verificar que PostgreSQL esté corriendo
- Verificar credenciales en application.yml
- Verificar que el schema pharmacy_schema exista

## Licencia

MedFlow HIS © 2026

## Contacto

MedFlow Team
