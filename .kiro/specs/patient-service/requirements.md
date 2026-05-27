# Requirements: Patient Service

## 1. Overview

El **Patient Service** es el microservicio responsable de la gestión de datos demográficos de pacientes en el sistema MedFlow HIS. Maneja el registro de pacientes, generación de acceso (Email/DPI + contraseña temporal), y búsqueda de pacientes.

**Arquitectura**: MVC (Capas) - CRUD simple  
**Base de Datos**: patient_schema (PostgreSQL)  
**Puerto**: 8082  
**Registro en Eureka**: PATIENT-SERVICE

## 2. User Stories

### US-1: Registro de Paciente (Admisión)
**Como** personal de admisión,  
**Quiero** registrar un nuevo paciente con sus datos demográficos,  
**Para** que pueda acceder a los servicios del hospital.

**Acceptance Criteria:**
- [ ] Puedo ingresar: DPI, nombre completo, fecha de nacimiento, género, dirección, teléfono, email
- [ ] El sistema valida que el DPI no esté duplicado
- [ ] El sistema valida que el email no esté duplicado
- [ ] Se genera automáticamente una contraseña temporal
- [ ] El paciente recibe credenciales: Email/DPI + contraseña temporal
- [ ] El sistema retorna el ID del paciente creado

### US-2: Generación de Acceso para Paciente
**Como** sistema,  
**Quiero** generar credenciales de acceso para el paciente,  
**Para** que pueda iniciar sesión en el portal del paciente.

**Acceptance Criteria:**
- [ ] Se genera contraseña temporal alfanumérica de 8 caracteres
- [ ] La contraseña se almacena hasheada (BCrypt)
- [ ] El paciente puede usar Email o DPI como username
- [ ] Las credenciales se muestran al personal de admisión para entregar al paciente

### US-3: Búsqueda de Pacientes
**Como** personal del hospital,  
**Quiero** buscar pacientes por DPI, nombre o email,  
**Para** encontrar rápidamente el registro del paciente.

**Acceptance Criteria:**
- [ ] Puedo buscar por DPI exacto
- [ ] Puedo buscar por nombre (búsqueda parcial, case-insensitive)
- [ ] Puedo buscar por email exacto
- [ ] Los resultados muestran: ID, DPI, nombre completo, fecha de nacimiento, teléfono
- [ ] Máximo 50 resultados por búsqueda

### US-4: Consulta de Datos del Paciente
**Como** personal del hospital,  
**Quiero** ver todos los datos demográficos de un paciente,  
**Para** verificar su información antes de atenderlo.

**Acceptance Criteria:**
- [ ] Puedo consultar paciente por ID
- [ ] Se muestran todos los datos demográficos
- [ ] Se muestra la dirección completa
- [ ] Se muestran los contactos de emergencia
- [ ] Se muestra la fecha de registro

### US-5: Actualización de Datos del Paciente
**Como** paciente o personal de admisión,  
**Quiero** actualizar los datos demográficos del paciente,  
**Para** mantener la información actualizada.

**Acceptance Criteria:**
- [ ] Puedo actualizar: dirección, teléfono, email, contactos de emergencia
- [ ] NO puedo actualizar: DPI, nombre, fecha de nacimiento (requiere proceso especial)
- [ ] El sistema valida que el nuevo email no esté duplicado
- [ ] Se registra la fecha de última actualización

## 3. Functional Requirements

### FR1: Datos Demográficos del Paciente
El servicio DEBE almacenar:

**Datos Básicos**:
- DPI (Documento Personal de Identificación) - Único, requerido
- Nombre completo (Primer nombre, segundo nombre, primer apellido, segundo apellido)
- Fecha de nacimiento - Requerido
- Género (Masculino, Femenino, Otro) - Requerido
- Email - Único, requerido
- Teléfono - Requerido

**Dirección**:
- Departamento
- Municipio
- Zona
- Dirección completa
- Código postal (opcional)

**Contactos de Emergencia** (1-3 contactos):
- Nombre completo
- Relación (Padre, Madre, Esposo/a, Hijo/a, Hermano/a, Otro)
- Teléfono

**Metadatos**:
- Fecha de registro
- Fecha de última actualización
- Estado (Activo, Inactivo)

### FR2: Generación de Contraseña Temporal
El servicio DEBE:
- Generar contraseña alfanumérica de 8 caracteres
- Incluir al menos: 1 mayúscula, 1 minúscula, 1 número
- Hashear la contraseña con BCrypt (strength 10)
- Almacenar solo el hash, nunca la contraseña en texto plano

### FR3: Validaciones
El servicio DEBE validar:
- **DPI**: 13 dígitos numéricos, único en el sistema
- **Email**: Formato válido, único en el sistema
- **Teléfono**: 8 dígitos numéricos (Guatemala)
- **Fecha de nacimiento**: No puede ser futura, paciente debe tener al menos 1 día de nacido
- **Nombre**: No vacío, máximo 100 caracteres por campo

### FR4: Búsqueda de Pacientes
El servicio DEBE soportar búsqueda por:
- **DPI exacto**: Búsqueda exacta de 13 dígitos
- **Nombre**: Búsqueda parcial, case-insensitive (LIKE %nombre%)
- **Email exacto**: Búsqueda exacta, case-insensitive

Resultados limitados a 50 registros.

### FR5: Endpoints REST

| Método | Endpoint | Descripción | Roles Permitidos |
|--------|----------|-------------|------------------|
| POST | /api/patients | Registrar nuevo paciente | ADMIN, ADMISSION |
| GET | /api/patients/{id} | Consultar paciente por ID | ADMIN, ADMISSION, DOCTOR, VITAL_SIGNS, LABORATORY, PHARMACY, CASHIER |
| GET | /api/patients/search?query= | Buscar pacientes | ADMIN, ADMISSION, DOCTOR |
| PUT | /api/patients/{id} | Actualizar datos del paciente | ADMIN, ADMISSION, PATIENT (solo su propio registro) |
| GET | /api/patients/{id}/credentials | Ver credenciales generadas | ADMIN, ADMISSION |

**Nota**: La validación de roles se hace en el API Gateway mediante JWT.

## 4. Non-Functional Requirements

### NFR1: Performance
- **Tiempo de respuesta**: < 200ms para consultas simples
- **Tiempo de respuesta**: < 500ms para búsquedas
- **Throughput**: Soportar 500 peticiones/segundo

### NFR2: Availability
- **Uptime**: 99.9%
- **Startup time**: < 20 segundos
- **Graceful shutdown**: Completar peticiones en curso

### NFR3: Scalability
- Soportar escalamiento horizontal (múltiples instancias)
- Stateless (no guardar estado en memoria)
- Base de datos: PostgreSQL con índices en DPI, email

### NFR4: Security
- Contraseñas hasheadas con BCrypt
- No exponer contraseñas en logs
- Validar permisos en cada endpoint
- Sanitizar inputs para prevenir SQL injection

### NFR5: Data Integrity
- DPI único (constraint en BD)
- Email único (constraint en BD)
- Transacciones ACID para operaciones de escritura

## 5. Business Rules

### BR1: Unicidad de DPI
- Un DPI solo puede estar registrado una vez en el sistema
- Si se intenta registrar un DPI duplicado, retornar error 409 Conflict

### BR2: Unicidad de Email
- Un email solo puede estar registrado una vez en el sistema
- Si se intenta registrar un email duplicado, retornar error 409 Conflict

### BR3: Contraseña Temporal
- La contraseña temporal es válida hasta que el paciente la cambie
- El paciente DEBE cambiar la contraseña en su primer login (implementado en auth-service)

### BR4: Actualización de Datos Sensibles
- DPI, nombre y fecha de nacimiento NO pueden ser actualizados vía API
- Requieren proceso manual con verificación de identidad

### BR5: Comunicación entre Servicios
- **CERO JOINs** con otros esquemas (auth_schema, clinical_schema, etc.)
- Si clinical-service necesita datos del paciente, debe llamar a patient-service vía HTTP

## 6. Constraints

### C1: Tecnología
- DEBE usar Spring Boot 3.2.4
- DEBE usar Java 17
- DEBE usar Spring Data JPA
- DEBE usar PostgreSQL

### C2: Arquitectura
- DEBE usar arquitectura MVC (Capas)
- DEBE registrarse en Eureka como PATIENT-SERVICE
- DEBE exponer health check en /actuator/health

### C3: Base de Datos
- DEBE usar esquema `patient_schema` en PostgreSQL
- NO DEBE hacer JOINs con otros esquemas
- DEBE usar índices en: DPI, email

## 7. Assumptions

### A1: API Gateway
- API Gateway valida JWT antes de llegar a este servicio
- Headers `X-User-Id` y `X-User-Roles` están disponibles en cada petición

### A2: Auth Service
- Auth Service maneja el login del paciente
- Auth Service valida las credenciales (Email/DPI + contraseña)

### A3: Eureka Server
- Eureka Server está corriendo y disponible
- Patient Service se registra exitosamente en Eureka

## 8. Dependencies

### D1: API Gateway
- **Tipo**: Infraestructura
- **Estado**: ✅ Implementado
- **Necesario para**: Enrutamiento y validación JWT

### D2: Eureka Server
- **Tipo**: Infraestructura
- **Estado**: ✅ Implementado
- **Necesario para**: Service Discovery

### D3: PostgreSQL
- **Tipo**: Base de Datos
- **Estado**: ⏳ Pendiente
- **Necesario para**: Almacenamiento de datos

### D4: Auth Service
- **Tipo**: Microservicio
- **Estado**: ⏳ Pendiente
- **Necesario para**: Login del paciente (testing)

## 9. Acceptance Criteria (Overall)

### Funcionalidad
- [ ] Puedo registrar un paciente con todos sus datos
- [ ] Se genera contraseña temporal automáticamente
- [ ] Puedo buscar pacientes por DPI, nombre o email
- [ ] Puedo consultar datos completos de un paciente
- [ ] Puedo actualizar datos del paciente
- [ ] Validaciones funcionan correctamente (DPI único, email único)

### Integración
- [ ] Servicio se registra en Eureka como PATIENT-SERVICE
- [ ] API Gateway enruta correctamente a /api/patients/**
- [ ] Headers X-User-Id y X-User-Roles se reciben correctamente

### Testing
- [ ] Todos los tests unitarios pasan
- [ ] Todos los tests de integración pasan
- [ ] Cobertura de código >= 80%

### Documentación
- [ ] README.md completo
- [ ] API endpoints documentados
- [ ] Configuración documentada

### Deployment
- [ ] Dockerfile funciona
- [ ] docker-compose.yml actualizado
- [ ] Health checks funcionan

## 10. Out of Scope

### No incluido en esta versión:
- ❌ Generación de QR de identidad - Versión futura
- ❌ Almacenamiento de hash biométrico (huella digital) - Descartado del MVP
- ❌ Historial de visitas - Manejado por clinical-service
- ❌ Fotografía del paciente - Versión futura
- ❌ Documentos adjuntos (PDF, imágenes) - Versión futura
- ❌ Notificaciones por email/SMS - Versión futura

### Razones:
- Mantener el servicio simple y enfocado en datos demográficos
- QR y biometría se agregarán en versión futura
- Historial de visitas es responsabilidad de clinical-service

## 11. Success Metrics

### Métricas de Éxito:
- **Tiempo de registro**: < 5 segundos
- **Tiempo de búsqueda**: < 2 segundos
- **Error rate**: < 1%
- **Availability**: > 99.9%

### Cómo medir:
- Usar `/actuator/metrics` de Spring Boot
- Logs de acceso con tiempos de respuesta
- Monitoreo con Prometheus (futuro)

## 12. Risks and Mitigations

### R1: DPI duplicado
**Riesgo**: Intentar registrar un paciente con DPI ya existente  
**Mitigación**: 
- Constraint UNIQUE en base de datos
- Validación en capa de servicio antes de insertar
- Retornar error 409 Conflict con mensaje claro

### R2: Email duplicado
**Riesgo**: Intentar registrar un paciente con email ya existente  
**Mitigación**:
- Constraint UNIQUE en base de datos
- Validación en capa de servicio antes de insertar
- Retornar error 409 Conflict con mensaje claro

### R3: Contraseña temporal insegura
**Riesgo**: Contraseña temporal débil o predecible  
**Mitigación**:
- Generar contraseña con SecureRandom
- Incluir mayúsculas, minúsculas y números
- Hashear con BCrypt (strength 10)

### R4: Pérdida de datos
**Riesgo**: Datos del paciente se pierden o corrompen  
**Mitigación**:
- Backups automáticos de PostgreSQL
- Transacciones ACID
- Validaciones estrictas antes de guardar

---

**Created**: April 13, 2026  
**Author**: MedFlow Team  
**Status**: ✅ Approved  
**Version**: 1.0.0
