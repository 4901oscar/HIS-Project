# Backend Services - Microservicios de Negocio

Esta carpeta contiene los microservicios de negocio de MedFlow HIS basados en **Domain-Driven Design (DDD)**.

## 🏥 Servicios (6 Microservicios)

### 1. Auth Service (Puerto 8081)
- **Dominio**: Autenticación y Autorización
- **Base de Datos**: `medflow_auth_db`
- **Responsabilidades**:
  - Login de usuarios (empleados)
  - Gestión de tokens JWT
  - Validación de permisos por rol (RBAC)
  - Gestión de sesiones
  - Refresh tokens

### 2. Patient Service (Puerto 8082)
- **Dominio**: Gestión de Pacientes
- **Base de Datos**: `medflow_patient_db`
- **Responsabilidades**:
  - Registro de pacientes (admisión)
  - Actualización de datos personales
  - Almacenamiento de hash biométrico (huella)
  - Generación de QR de identidad
  - Historial de visitas
  - Búsqueda de pacientes

### 3. Clinical Service (Puerto 8083)
- **Dominio**: Gestión Clínica (Motor Médico)
- **Base de Datos**: `medflow_clinical_db`
- **Responsabilidades**:
  - Triaje Manchester (clasificación de urgencia)
  - Citas médicas y cálculo de slots
  - Registro de signos vitales
  - Consultas médicas
  - Prescripción de recetas
  - Órdenes de laboratorio
  - Historial clínico

### 4. Lab Service (Puerto 8084)
- **Dominio**: Laboratorio Clínico
- **Base de Datos**: `medflow_lab_db`
- **Responsabilidades**:
  - Recepción de órdenes de laboratorio
  - Trazabilidad de muestras
  - Generación de códigos de barras
  - Registro de resultados
  - Almacenamiento de PDFs de exámenes
  - Notificación de resultados listos

### 5. Pharmacy Service (Puerto 8085)
- **Dominio**: Farmacia
- **Base de Datos**: `medflow_pharmacy_db`
- **Responsabilidades**:
  - Control de inventario de medicamentos
  - Recepción de recetas médicas
  - Validación de recetas pagadas
  - Dispensación de medicamentos
  - Registro de entrega física
  - Alertas de stock bajo
  - Gestión de lotes y vencimientos

### 6. Billing Service (Puerto 8086)
- **Dominio**: Facturación y Pagos
- **Base de Datos**: `medflow_billing_db`
- **Responsabilidades**:
  - Gestión de cuenta corriente del paciente
  - Generación de cargos (consultas, exámenes, medicamentos)
  - Integración de métodos de pago
  - Emisión de facturas
  - Integración con SAT (facturación electrónica)
  - Reportes financieros
  - Control de pagos parciales

## Arquitectura

Cada microservicio:
- Es independiente y autónomo
- Tiene su propia base de datos (o esquema)
- Se registra en Eureka Server
- Se comunica a través del API Gateway
- Implementa Spring Boot + Spring Data JPA
- Usa PostgreSQL como base de datos

## Estructura de cada servicio

```
service-name/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/medflow/service/
│   │   │       ├── controller/
│   │   │       ├── service/
│   │   │       ├── repository/
│   │   │       ├── model/
│   │   │       ├── dto/
│   │   │       └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-docker.yml
│   └── test/
├── pom.xml
├── Dockerfile
└── README.md
```

## Tecnologías

- **Framework**: Spring Boot 3.x
- **ORM**: Spring Data JPA + Hibernate
- **Database**: PostgreSQL 15
- **Service Discovery**: Eureka Client
- **API Documentation**: SpringDoc OpenAPI
- **Security**: Spring Security + JWT
- **Validation**: Jakarta Validation
- **Logging**: SLF4J + Logback

## Ejecución

### Local (sin Docker)
```bash
cd auth-service
mvn spring-boot:run
```

### Docker
```bash
# Desde la raíz del proyecto
docker-compose up auth-service patient-service clinical-service
```

## Comunicación entre servicios

Los servicios se comunican usando:
1. **REST API** a través del API Gateway
2. **Feign Client** para llamadas síncronas
3. **Events** (futuro) para comunicación asíncrona

## Base de Datos

Cada servicio tiene su propio esquema en PostgreSQL:
- `auth_schema`: Auth Service
- `patient_schema`: Patient Service
- `clinical_schema`: Clinical Service

## Próximos Pasos

1. Implementar Auth Service con JWT
2. Implementar Patient Service con CRUD
3. Implementar Clinical Service con módulos
4. Agregar tests unitarios e integración
5. Implementar comunicación entre servicios
