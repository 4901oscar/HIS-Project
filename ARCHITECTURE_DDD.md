# 🏗️ MedFlow HIS - Arquitectura DDD (Domain-Driven Design)

## 📐 Principios de Diseño

Esta arquitectura sigue los principios de **Domain-Driven Design (DDD)** y **Microservicios**:

1. **Bounded Context**: Cada microservicio representa un dominio específico del hospital
2. **Database per Service**: Cada servicio tiene su propia base de datos (esquema)
3. **Loose Coupling**: Los servicios se comunican a través de APIs REST
4. **High Cohesion**: Cada servicio agrupa funcionalidad relacionada
5. **Single Responsibility**: Un servicio, una responsabilidad

## 🏗️ Capa 1: Infraestructura (El Esqueleto)

Estos servicios NO tienen lógica de negocio, pero son esenciales para el ecosistema.

### 1. Discovery Server (Eureka) ✅
```
Nombre: discovery-server (eureka-server)
Puerto: 8761
Responsabilidad: Service Registry - "Directorio telefónico"
Tecnología: Spring Cloud Netflix Eureka
Estado: ✅ IMPLEMENTADO
```

**¿Qué hace?**
- Los microservicios se registran aquí al arrancar
- Permite descubrimiento dinámico de servicios
- Monitorea la salud de cada servicio (heartbeat)

### 2. API Gateway ⏳
```
Nombre: api-gateway
Puerto: 8080
Responsabilidad: Punto de entrada único - "Recepcionista"
Tecnología: Spring Cloud Gateway
Estado: ⏳ PENDIENTE
```

**¿Qué hace?**
- Recibe TODAS las peticiones del frontend React
- Valida tokens JWT antes de enrutar
- Redirige peticiones al microservicio correcto
- Implementa rate limiting y circuit breaker
- Maneja CORS

**Rutas que manejará:**
```
/api/auth/**        → auth-service:8081
/api/patients/**    → patient-service:8082
/api/clinical/**    → clinical-service:8083
/api/lab/**         → lab-service:8084
/api/pharmacy/**    → pharmacy-service:8085
/api/billing/**     → billing-service:8086
```

## 🏥 Capa 2: Microservicios de Negocio (Core del Hospital)

Cada servicio representa un **Bounded Context** del dominio hospitalario.

### 1. Auth Service
```
Nombre: auth-service
Puerto: 8081
Base de Datos: medflow_auth_db
Dominio: Autenticación y Autorización
```

**Responsabilidades:**
- ✅ Login de empleados (staff)
- ✅ Emisión de tokens JWT
- ✅ Validación de tokens
- ✅ Gestión de roles (RBAC)
  - Administrador
  - Admisión
  - Enfermería
  - Doctor
  - Laboratorio
  - Farmacia
  - Caja
- ✅ Refresh tokens
- ✅ Logout y revocación de tokens

**Entidades principales:**
- `User` (empleado)
- `Role`
- `Permission`
- `RefreshToken`

**Endpoints:**
```
POST   /api/auth/login
POST   /api/auth/logout
POST   /api/auth/refresh
GET    /api/auth/validate
GET    /api/auth/me
```

---

### 2. Patient Service
```
Nombre: patient-service
Puerto: 8082
Base de Datos: medflow_patient_db
Dominio: Gestión de Pacientes
```

**Responsabilidades:**
- ✅ Registro de pacientes (admisión)
- ✅ Datos biográficos (nombre, edad, dirección, etc.)
- ✅ Almacenamiento de hash biométrico (huella digital)
- ✅ Generación de QR de identidad del paciente
- ✅ Búsqueda de pacientes
- ✅ Historial de visitas
- ✅ Actualización de datos personales

**Entidades principales:**
- `Patient`
- `BiometricData`
- `PatientQR`
- `Address`
- `EmergencyContact`

**Endpoints:**
```
POST   /api/patients/register
GET    /api/patients/{id}
GET    /api/patients/search?query=
PUT    /api/patients/{id}
GET    /api/patients/{id}/qr
POST   /api/patients/{id}/biometric
GET    /api/patients/{id}/visits
```

**Integraciones:**
- Genera QR que contiene: ID paciente, nombre, fecha de nacimiento
- Almacena hash de huella (no la huella real por seguridad)

---

### 3. Clinical Service
```
Nombre: clinical-service
Puerto: 8083
Base de Datos: medflow_clinical_db
Dominio: Gestión Clínica (El Motor Médico)
```

**Responsabilidades:**
- ✅ **Triaje Manchester**: Clasificación de urgencia (Rojo, Naranja, Amarillo, Verde, Azul)
- ✅ **Signos Vitales**: Registro por enfermería
- ✅ **Historial Clínico**: Consultas médicas
- ✅ **Prescripción de Recetas**: Generación de recetas médicas
- ✅ **Órdenes de Laboratorio**: Solicitud de exámenes
- ✅ **Gestión de Citas**: Cálculo de slots disponibles (usando Redis)
- ✅ **Consulta Médica**: Diagnóstico, tratamiento, notas

**Entidades principales:**
- `Appointment` (Cita)
- `Triage` (Triaje Manchester)
- `VitalSigns` (Signos vitales)
- `MedicalRecord` (Historial clínico)
- `Prescription` (Receta)
- `LabOrder` (Orden de laboratorio)
- `Diagnosis`

**Endpoints:**
```
# Citas
POST   /api/clinical/appointments
GET    /api/clinical/appointments/available-slots
GET    /api/clinical/appointments/{id}
PUT    /api/clinical/appointments/{id}/activate

# Triaje
POST   /api/clinical/triage
GET    /api/clinical/triage/{patientId}

# Signos Vitales
POST   /api/clinical/vital-signs
GET    /api/clinical/vital-signs/{patientId}

# Consulta Médica
POST   /api/clinical/consultations
GET    /api/clinical/consultations/{id}
POST   /api/clinical/consultations/{id}/prescription
POST   /api/clinical/consultations/{id}/lab-order

# Historial
GET    /api/clinical/patients/{patientId}/history
```

**Algoritmo Manchester:**
```
Rojo (Inmediato)    → 0 minutos
Naranja (Muy urgente) → 10 minutos
Amarillo (Urgente)   → 60 minutos
Verde (Poco urgente) → 120 minutos
Azul (No urgente)    → 240 minutos
```

**Integraciones:**
- Redis para caché de slots de citas
- Comunica con Lab Service para órdenes
- Comunica con Pharmacy Service para recetas

---

### 4. Lab Service
```
Nombre: lab-service
Puerto: 8084
Base de Datos: medflow_lab_db
Dominio: Laboratorio Clínico
```

**Responsabilidades:**
- ✅ Recepción de órdenes de laboratorio
- ✅ Trazabilidad de muestras
- ✅ Generación de códigos de barras para muestras
- ✅ Registro de resultados
- ✅ Almacenamiento de PDFs de exámenes
- ✅ Notificación de resultados listos

**Entidades principales:**
- `LabOrder` (Orden de laboratorio)
- `Sample` (Muestra)
- `LabResult` (Resultado)
- `LabTest` (Tipo de examen)

**Endpoints:**
```
POST   /api/lab/orders
GET    /api/lab/orders/{id}
POST   /api/lab/samples
GET    /api/lab/samples/{barcode}
POST   /api/lab/results
GET    /api/lab/results/{orderId}
GET    /api/lab/results/{orderId}/pdf
```

**Flujo:**
```
1. Doctor genera orden → Clinical Service
2. Lab Service recibe orden
3. Laboratorista genera código de barras para muestra
4. Muestra se procesa
5. Resultados se cargan (PDF)
6. Notificación a Clinical Service
```

---

### 5. Pharmacy Service
```
Nombre: pharmacy-service
Puerto: 8085
Base de Datos: medflow_pharmacy_db
Dominio: Farmacia
```

**Responsabilidades:**
- ✅ Control de inventario de medicamentos
- ✅ Recepción de recetas médicas
- ✅ Validación de recetas pagadas
- ✅ Dispensación de medicamentos
- ✅ Registro de entrega física
- ✅ Alertas de stock bajo
- ✅ Gestión de lotes y fechas de vencimiento

**Entidades principales:**
- `Medication` (Medicamento)
- `Inventory` (Inventario)
- `Prescription` (Receta)
- `Dispensation` (Dispensación)
- `Batch` (Lote)

**Endpoints:**
```
# Inventario
GET    /api/pharmacy/medications
POST   /api/pharmacy/medications
PUT    /api/pharmacy/medications/{id}
GET    /api/pharmacy/inventory/low-stock

# Recetas
GET    /api/pharmacy/prescriptions/pending
POST   /api/pharmacy/prescriptions/{id}/dispense
GET    /api/pharmacy/prescriptions/{id}/validate

# Dispensación
POST   /api/pharmacy/dispense
GET    /api/pharmacy/dispense/history
```

**Flujo:**
```
1. Doctor genera receta → Clinical Service
2. Paciente paga → Billing Service marca como pagada
3. Pharmacy Service valida que esté pagada
4. Farmacéutico dispensa medicamentos
5. Se actualiza inventario
6. Se registra entrega física
```

---

### 6. Billing Service
```
Nombre: billing-service
Puerto: 8086
Base de Datos: medflow_billing_db
Dominio: Facturación y Pagos
```

**Responsabilidades:**
- ✅ Gestión de cuenta corriente del paciente
- ✅ Generación de cargos (consultas, exámenes, medicamentos)
- ✅ Integración de métodos de pago
  - Efectivo
  - Tarjeta de crédito/débito
  - Transferencia
- ✅ Emisión de facturas
- ✅ Integración con SAT (facturación electrónica)
- ✅ Reportes financieros
- ✅ Control de pagos parciales

**Entidades principales:**
- `Invoice` (Factura)
- `Payment` (Pago)
- `Charge` (Cargo)
- `PatientAccount` (Cuenta del paciente)
- `PaymentMethod`

**Endpoints:**
```
# Cargos
POST   /api/billing/charges
GET    /api/billing/charges/{patientId}

# Pagos
POST   /api/billing/payments
GET    /api/billing/payments/{id}

# Facturas
POST   /api/billing/invoices
GET    /api/billing/invoices/{id}
GET    /api/billing/invoices/{id}/pdf

# Cuenta del Paciente
GET    /api/billing/accounts/{patientId}
GET    /api/billing/accounts/{patientId}/balance

# Reportes
GET    /api/billing/reports/daily
GET    /api/billing/reports/monthly
```

**Flujo de Facturación:**
```
1. Servicio genera cargo (consulta, examen, medicamento)
2. Billing Service registra cargo en cuenta del paciente
3. Paciente paga en caja
4. Se genera factura
5. Se envía a SAT (factura electrónica)
6. Se marca como pagado en el servicio origen
```

---

## 🗄️ Bases de Datos (PostgreSQL)

Cada servicio tiene su propio esquema en PostgreSQL:

```sql
-- Esquemas separados para aislamiento
CREATE SCHEMA medflow_auth_db;
CREATE SCHEMA medflow_patient_db;
CREATE SCHEMA medflow_clinical_db;
CREATE SCHEMA medflow_lab_db;
CREATE SCHEMA medflow_pharmacy_db;
CREATE SCHEMA medflow_billing_db;
```

**Ventajas:**
- ✅ Aislamiento de datos
- ✅ Escalabilidad independiente
- ✅ Despliegue independiente
- ✅ Fallas aisladas (si uno cae, los demás siguen)

---

## 🔄 Comunicación entre Servicios

### Síncrona (REST API)
```
Clinical Service ──REST──> Lab Service
                          (Crear orden de laboratorio)

Pharmacy Service ──REST──> Billing Service
                          (Validar pago de receta)
```

### Asíncrona (Eventos - Futuro)
```
Billing Service ──Event──> Clinical Service
                          (Pago confirmado)

Lab Service ──Event──> Clinical Service
                      (Resultados listos)
```

---

## 📊 Arquitectura Visual

```
┌─────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                      │
│                     Port: 3000                           │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│                   API GATEWAY (8080)                     │
│  - Validación JWT                                        │
│  - Enrutamiento                                          │
│  - Rate Limiting                                         │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ Discovery    │  │ Auth Service │  │Patient Service│
│ Server       │  │   (8081)     │  │   (8082)     │
│  (8761)      │  │              │  │              │
└──────────────┘  └──────────────┘  └──────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Clinical Svc  │  │  Lab Service │  │Pharmacy Svc  │
│   (8083)     │  │   (8084)     │  │   (8085)     │
└──────────────┘  └──────────────┘  └──────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │Billing Svc   │
                  │   (8086)     │
                  └──────────────┘
                         │
                         ▼
                  ┌──────────────┐
                  │ PostgreSQL   │
                  │  (6 schemas) │
                  └──────────────┘
```

---

## 🚀 Orden de Implementación

### Fase 1: Infraestructura ✅
1. ✅ Discovery Server (Eureka) - COMPLETADO
2. ⏳ API Gateway

### Fase 2: Servicios Core
3. ⏳ Auth Service (primero, todos lo necesitan)
4. ⏳ Patient Service
5. ⏳ Clinical Service

### Fase 3: Servicios Especializados
6. ⏳ Lab Service
7. ⏳ Pharmacy Service
8. ⏳ Billing Service

---

## 📝 Convenciones de Código

### Estructura de cada microservicio:
```
service-name/
├── src/
│   ├── main/
│   │   ├── java/com/medflow/{service}/
│   │   │   ├── controller/     # REST endpoints
│   │   │   ├── service/        # Lógica de negocio
│   │   │   ├── repository/     # Acceso a datos
│   │   │   ├── model/          # Entidades JPA
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── config/         # Configuración
│   │   │   ├── security/       # Seguridad
│   │   │   ├── exception/      # Manejo de errores
│   │   │   └── util/           # Utilidades
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-docker.yml
│   └── test/
├── pom.xml
├── Dockerfile
└── README.md
```

---

## 🎯 Próximos Pasos

1. ✅ Discovery Server implementado
2. **Implementar API Gateway** ← SIGUIENTE
3. Implementar Auth Service
4. Implementar Patient Service
5. Implementar Clinical Service
6. Implementar Lab Service
7. Implementar Pharmacy Service
8. Implementar Billing Service

---

**Versión**: 1.0.0  
**Última actualización**: Abril 2026  
**Arquitecto**: MedFlow Team
