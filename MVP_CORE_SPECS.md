# 🏥 MedFlow HIS - Especificaciones Core (MVP Graduación)

**Versión**: 2.0.0  
**Última actualización**: Abril 13, 2026  
**Propósito**: Documento maestro con las especificaciones reales del MVP

---

## 📋 Índice

1. [Stack Tecnológico](#stack-tecnológico)
2. [Arquitectura de Software](#arquitectura-de-software)
3. [Base de Datos](#base-de-datos)
4. [Seguridad y Autenticación](#seguridad-y-autenticación)
5. [Control de Accesos (RBAC)](#control-de-accesos-rbac)
6. [Catálogo de Microservicios](#catálogo-de-microservicios)
7. [Flujo de Trabajo con IA](#flujo-de-trabajo-con-ia)
8. [Características Descartadas](#características-descartadas)

---

## 💻 Stack Tecnológico

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **HTTP Client**: Axios
- **Routing**: React Router

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven

### Infraestructura
- **Containerization**: Docker Compose (Monorepo)
- **Service Discovery**: Spring Cloud Eureka
- **API Gateway**: Spring Cloud Gateway
- **Cache**: Redis (Para cálculo en tiempo real de slots de citas médicas)

### Base de Datos
- **Motor**: PostgreSQL (Una única instancia)
- **Estrategia**: Monolito Lógico (Schema-per-Service)

---

## 🏗️ Arquitectura de Software

### Nivel Macro (Sistema)
**Arquitectura de Microservicios**

### Nivel Micro (Código en Spring Boot)

#### Arquitectura Hexagonal (Puertos y Adaptadores)
**Uso**: Para microservicios con lógica de negocio pesada

**Ejemplo**: `clinical-service` para el Triaje Manchester

**Estructura**:
```
clinical-service/
├── domain/              # Lógica de negocio pura
│   ├── model/          # Entidades del dominio
│   ├── port/           # Interfaces (puertos)
│   └── service/        # Servicios del dominio
├── application/         # Casos de uso
├── infrastructure/      # Adaptadores
│   ├── persistence/    # Adaptador de BD
│   ├── rest/           # Adaptador REST
│   └── messaging/      # Adaptador de eventos
└── config/             # Configuración
```

#### Arquitectura MVC (Capas)
**Uso**: Para microservicios de tipo CRUD simple

**Ejemplo**: `patient-service`, `pharmacy-service`

**Estructura**:
```
service-name/
├── controller/         # REST endpoints
├── service/            # Lógica de negocio
├── repository/         # Acceso a datos
├── model/              # Entidades JPA
├── dto/                # Data Transfer Objects
└── config/             # Configuración
```

---

## 🗄️ Base de Datos

### Estrategia: Monolito Lógico (Schema-per-Service)

**Razón**: Ahorrar costos de infraestructura para el MVP

### Motor
- **PostgreSQL** (Una única instancia)

### Estructura
Esquemas aislados por dominio:

```sql
-- Esquemas separados
CREATE SCHEMA auth_schema;
CREATE SCHEMA patient_schema;
CREATE SCHEMA clinical_schema;
CREATE SCHEMA lab_schema;
CREATE SCHEMA pharmacy_schema;
CREATE SCHEMA billing_schema;
```

### Regla Estricta
**❌ CERO JOINs entre esquemas**

Si se necesitan datos de otro dominio:
- ✅ Composición de APIs (llamadas HTTP entre servicios)
- ❌ NO hacer JOINs directos entre esquemas

**Ejemplo**:
```java
// ❌ INCORRECTO
SELECT p.*, c.* 
FROM patient_schema.patients p 
JOIN clinical_schema.consultations c ON p.id = c.patient_id;

// ✅ CORRECTO
// En Clinical Service:
Patient patient = patientServiceClient.getPatient(patientId);
Consultation consultation = consultationRepository.findById(consultationId);
```

---

## 🔐 Seguridad y Autenticación

### Mecanismo
**Autenticación Stateless con JWT**

### Flujo
1. **Generación**: JWT generado en `auth-service`
2. **Almacenamiento**: localStorage de React
3. **Validación**: 
   - Axios Interceptors (Frontend)
   - API Gateway (Backend)

### Estructura del JWT
```json
{
  "sub": "user123",
  "name": "Dr. Juan Pérez",
  "role": "DOCTOR",
  "permissions": ["READ_PATIENTS", "WRITE_PRESCRIPTIONS"],
  "iat": 1713024000,
  "exp": 1713110400
}
```

### Portal del Paciente
- **Login**: Email/DPI + Contraseña temporal
- **Generación**: Contraseña temporal generada en Admisión
- **Acceso**: Solo puede ver su propio historial

---

## 👥 Control de Accesos (RBAC)

Cada usuario interactúa con una vista distinta según el **Rol inyectado en su JWT**.

### Roles del Sistema

| Rol | Código | Descripción | Permisos Principales |
|-----|--------|-------------|---------------------|
| **Administrador** | `ADMIN` | Súper Usuario | Crea cuentas de empleados, asigna roles |
| **Admisión** | `ADMISSION` | Recepción | Registra pacientes, genera QR, gestiona/activa citas |
| **Signos Vitales** | `VITAL_SIGNS` | Enfermería | Captura signos vitales en sala de espera |
| **Doctor** | `DOCTOR` | Médico | Triaje Manchester, consultas, prescribe recetas |
| **Laboratorio** | `LABORATORY` | Técnico Lab | Sube PDFs de resultados, gestiona muestras |
| **Farmacia** | `PHARMACY` | Farmacéutico | Gestiona inventario, despacha medicamentos |
| **Caja** | `CASHIER` | Cajero | Procesa facturación interna |
| **Paciente** | `PATIENT` | Paciente | Ve historial, recetas, laboratorios, actualiza datos |

### Matriz de Permisos

| Recurso | ADMIN | ADMISSION | VITAL_SIGNS | DOCTOR | LABORATORY | PHARMACY | CASHIER | PATIENT |
|---------|-------|-----------|-------------|--------|------------|----------|---------|---------|
| Crear usuarios | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Registrar pacientes | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Signos vitales | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Triaje | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Consultas | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Recetas | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | 👁️ |
| Laboratorio | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | 👁️ |
| Inventario | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Facturación | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | 👁️ |
| Ver historial | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 👁️* |

*👁️ = Solo lectura  
*👁️* = Solo su propio historial

---

## 🎯 Catálogo de Microservicios

### Infraestructura (2 servicios)

#### 1. Discovery Server (Eureka)
```
Puerto: 8761
Estado: ✅ IMPLEMENTADO
Responsabilidad: Service Registry
```

#### 2. API Gateway
```
Puerto: 8080
Estado: ✅ IMPLEMENTADO
Responsabilidad: Punto de entrada único
```

**Funciones**:
- Enrutador de peticiones
- Validación de tokens JWT
- Rate limiting
- CORS

**Rutas**:
```
/api/auth/**        → auth-service:8081
/api/patients/**    → patient-service:8082
/api/clinical/**    → clinical-service:8083
/api/lab/**         → lab-service:8084
/api/pharmacy/**    → pharmacy-service:8085
/api/billing/**     → billing-service:8086
```

### Servicios de Negocio (6 servicios)

#### 1. Auth Service
```
Puerto: 8081
Base de Datos: auth_schema
Arquitectura: MVC (CRUD simple)
Estado: ⏳ PENDIENTE
```

**Responsabilidades**:
- Gestión de credenciales
- Login de staff y pacientes
- Emisión de JWT
- Gestión de roles (RBAC)

**Entidades**:
- `User` (empleado)
- `Role`
- `Permission`

#### 2. Patient Service
```
Puerto: 8082
Base de Datos: patient_schema
Arquitectura: MVC (CRUD simple)
Estado: ⏳ PENDIENTE
```

**Responsabilidades**:
- Datos demográficos de pacientes
- Generación de acceso (Email/DPI + contraseña temporal)
- Generación de QR de identidad
- Búsqueda de pacientes

**Entidades**:
- `Patient`
- `Address`
- `EmergencyContact`

#### 3. Clinical Service
```
Puerto: 8083
Base de Datos: clinical_schema
Arquitectura: HEXAGONAL (Lógica pesada)
Estado: ⏳ PENDIENTE
```

**Responsabilidades**:
- **Motor médico principal**
- Triaje Manchester
- Historial clínico
- Gestión de citas (con Redis para slots)
- Prescripción de recetas
- Órdenes de laboratorio

**Entidades**:
- `Appointment`
- `Triage`
- `VitalSigns`
- `MedicalRecord`
- `Prescription`
- `LabOrder`

**Triaje Manchester**:
```
Rojo (Inmediato)      → 0 minutos
Naranja (Muy urgente) → 10 minutos
Amarillo (Urgente)    → 60 minutos
Verde (Poco urgente)  → 120 minutos
Azul (No urgente)     → 240 minutos
```

#### 4. Lab Service
```
Puerto: 8084
Base de Datos: lab_schema
Arquitectura: MVC (CRUD simple)
Estado: ⏳ PENDIENTE
```

**Responsabilidades**:
- Trazabilidad de muestras
- Gestión de estados de muestras
- Subida de PDFs de resultados

**Entidades**:
- `LabOrder`
- `Sample`
- `LabResult`

#### 5. Pharmacy Service
```
Puerto: 8085
Base de Datos: pharmacy_schema
Arquitectura: MVC (CRUD simple)
Estado: ⏳ PENDIENTE
```

**Responsabilidades**:
- Gestión de inventario
- Despacho de medicamentos recetados
- Control de stock

**Entidades**:
- `Medication`
- `Inventory`
- `Dispensation`

#### 6. Billing Service
```
Puerto: 8086
Base de Datos: billing_schema
Arquitectura: MVC (CRUD simple)
Estado: ⏳ PENDIENTE
```

**Responsabilidades**:
- Cobros internos del hospital
- Procesamiento de pagos
- Generación de facturas internas

**Entidades**:
- `Invoice`
- `Payment`
- `Charge`

---

## 🤖 Flujo de Trabajo con IA (Kiro)

### Estructura de Monorepo
Para que Kiro entienda el contexto global del proyecto.

### Carpeta .kiro/

#### 1. Steering (Reglas estandarizadas)
```
.kiro/steering/
├── project-standards.md    # Estándares del proyecto
├── architecture-rules.md   # Reglas de arquitectura
└── coding-conventions.md   # Convenciones de código
```

#### 2. Specs (Tareas paso a paso)
```
.kiro/specs/
├── api-gateway/
│   ├── requirements.md
│   ├── design.md
│   └── tasks.md
└── {feature-name}/
    ├── requirements.md
    ├── design.md
    └── tasks.md
```

#### 3. Hooks (Validaciones)
```
.kiro/hooks/
├── pre-commit.json
└── pre-push.json
```

### Service Abstraction (Frontend)

Los servicios de Axios se dejan preparados apuntando al Gateway, pero devolviendo **Mocks** hasta que el backend esté conectado.

**Ejemplo**:
```typescript
// src/services/patientService.ts
const USE_MOCK = true; // Cambiar a false cuando backend esté listo

export const getPatient = async (id: string) => {
  if (USE_MOCK) {
    return mockPatient; // Mock data
  }
  
  // Real API call
  const response = await axios.get(`/api/patients/${id}`);
  return response.data;
};
```

---

## ❌ Características Descartadas (Por alcance del MVP)

### 1. Autenticación Biométrica
- ❌ **Huella dactilar eliminada**
- Razón: Complejidad técnica y hardware adicional
- Alternativa: Login tradicional con Email/DPI + contraseña

### 2. Integración con SAT
- ❌ **Facturación Electrónica FEL eliminada**
- Razón: Complejidad de integración con sistemas externos
- Alternativa: Facturación interna del hospital solamente

### 3. Notificaciones Push
- ❌ Notificaciones en tiempo real eliminadas
- Razón: Requiere infraestructura adicional (WebSockets, Firebase)
- Alternativa: Notificaciones por email (futuro)

### 4. Reportes Avanzados
- ❌ Dashboard de analytics eliminado
- Razón: Enfoque en funcionalidad core
- Alternativa: Reportes básicos en cada servicio

---

## 📊 Diagrama de Arquitectura Final

```
┌─────────────────────────────────────────────────────────┐
│           FRONTEND (React 18 + Vite)                     │
│                  Port: 3000                              │
│  - Tailwind CSS                                          │
│  - Axios (con Service Abstraction + Mocks)              │
│  - React Router                                          │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│         API GATEWAY (Spring Cloud Gateway)               │
│                  Port: 8080                              │
│  - JWT Validation                                        │
│  - Routing                                               │
│  - Rate Limiting                                         │
│  - CORS                                                  │
└────────────────────────┬────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Discovery   │  │Auth Service  │  │Patient Svc   │
│  Server      │  │   (8081)     │  │   (8082)     │
│  (8761)      │  │   MVC        │  │   MVC        │
└──────────────┘  └──────────────┘  └──────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│Clinical Svc  │  │  Lab Service │  │Pharmacy Svc  │
│   (8083)     │  │   (8084)     │  │   (8085)     │
│  HEXAGONAL   │  │   MVC        │  │   MVC        │
└──────────────┘  └──────────────┘  └──────────────┘
       │                                    │
       │ Redis (Slots)                     │
       ▼                                    ▼
┌──────────────┐                    ┌──────────────┐
│    Redis     │                    │Billing Svc   │
│  (Citas)     │                    │   (8086)     │
└──────────────┘                    │   MVC        │
                                    └──────────────┘
                                           │
                                           ▼
                                    ┌──────────────┐
                                    │ PostgreSQL   │
                                    │ (6 schemas)  │
                                    └──────────────┘
```

---

## 🎯 Roadmap de Implementación

### Fase 1: Infraestructura ✅
- [x] Discovery Server (Eureka)
- [x] API Gateway

### Fase 2: Servicios Core (En Progreso)
- [ ] Auth Service
- [ ] Patient Service
- [ ] Clinical Service (con Redis)

### Fase 3: Servicios Especializados
- [ ] Lab Service
- [ ] Pharmacy Service
- [ ] Billing Service

### Fase 4: Frontend
- [ ] Actualizar frontend con Service Abstraction
- [ ] Implementar vistas por rol
- [ ] Integrar con API Gateway

### Fase 5: Testing & Deployment
- [ ] Tests end-to-end
- [ ] Docker Compose completo
- [ ] Documentación final

---

## 📞 Contacto

- **Repositorio**: https://github.com/4901oscar/HIS-Project
- **Branch Principal**: develop
- **Desarrollador**: Oscar
- **Equipo**: MedFlow Team

---

**Última actualización**: Abril 13, 2026  
**Versión del Documento**: 2.0.0  
**Mantenido por**: MedFlow Team
