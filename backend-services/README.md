# Backend Services - Microservicios de Negocio

Esta carpeta contiene los microservicios de negocio de MedFlow HIS.

## Servicios

### 1. Auth Service (Puerto 8081)
- **Propósito**: Autenticación y autorización
- **Responsabilidades**:
  - Login de usuarios (empleados)
  - Gestión de tokens JWT
  - Validación de permisos por rol
  - Gestión de sesiones

### 2. Patient Service (Puerto 8082)
- **Propósito**: Gestión de pacientes
- **Responsabilidades**:
  - Registro de pacientes
  - Actualización de datos personales
  - Historial de pacientes
  - Búsqueda de pacientes

### 3. Clinical Service (Puerto 8083)
- **Propósito**: Gestión clínica
- **Responsabilidades**:
  - Citas médicas
  - Consultas médicas
  - Signos vitales
  - Recetas médicas
  - Órdenes de laboratorio
  - Dispensación de farmacia
  - Facturación

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
