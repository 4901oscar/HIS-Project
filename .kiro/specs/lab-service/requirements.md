# Requirements: Lab Service

## 1. Overview

El **Lab Service** es el microservicio responsable de la gestión de órdenes de laboratorio, trazabilidad de muestras y resultados en el sistema MedFlow HIS.

**Arquitectura**: MVC (Capas) - CRUD simple  
**Base de Datos**: lab_schema (PostgreSQL)  
**Puerto**: 8084  
**Registro en Eureka**: LAB-SERVICE

## 2. User Stories

### US-1: Recibir Orden de Laboratorio
**Como** técnico de laboratorio,  
**Quiero** ver las órdenes de laboratorio pendientes enviadas por el médico,  
**Para** saber qué exámenes debo procesar.

**Acceptance Criteria:**
- [ ] Puedo ver todas las órdenes con estado PENDING
- [ ] Cada orden muestra: código, paciente, exámenes solicitados, fecha, doctor
- [ ] Puedo filtrar órdenes por estado (PENDING, IN_PROGRESS, COMPLETED, CANCELLED)

### US-2: Registrar Toma de Muestra
**Como** técnico de laboratorio,  
**Quiero** registrar que tomé la muestra del paciente,  
**Para** iniciar el proceso de análisis.

**Acceptance Criteria:**
- [ ] Puedo cambiar el estado de la orden a IN_PROGRESS
- [ ] Se registra timestamp y técnico que tomó la muestra
- [ ] Se genera un ID de muestra único

### US-3: Subir Resultado de Laboratorio
**Como** técnico de laboratorio,  
**Quiero** subir el resultado del examen en formato PDF,  
**Para** que el médico y el paciente puedan verlo.

**Acceptance Criteria:**
- [ ] Puedo subir un archivo PDF como resultado
- [ ] El resultado queda asociado a la orden y al paciente
- [ ] El estado de la orden cambia a COMPLETED
- [ ] Se registra timestamp y técnico que subió el resultado

### US-4: Consultar Resultados de un Paciente
**Como** doctor o paciente,  
**Quiero** consultar los resultados de laboratorio de un paciente,  
**Para** revisar su historial de exámenes.

**Acceptance Criteria:**
- [ ] Puedo consultar resultados por patientId
- [ ] Los resultados se muestran ordenados por fecha descendente
- [ ] Paciente solo puede ver sus propios resultados (rol PATIENT)
- [ ] Doctor puede ver resultados de cualquier paciente

## 3. Functional Requirements

### FR1: Gestión de Órdenes de Laboratorio
- Recibir notificación de nueva orden desde Clinical Service
- Almacenar: orderCode, patientId, doctorId, testNames, status, orderedAt
- Estados: PENDING → IN_PROGRESS → COMPLETED | CANCELLED

### FR2: Trazabilidad de Muestras
- Registrar toma de muestra con: sampleId, orderId, collectedAt, collectedBy
- Asociar muestra a orden de laboratorio

### FR3: Gestión de Resultados
- Almacenar referencia al PDF del resultado (path o URL)
- Asociar resultado a: orderId, patientId, uploadedAt, uploadedBy
- Marcar orden como COMPLETED al subir resultado

### FR4: Endpoints REST

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | /api/lab/orders/notify | Recibir orden desde Clinical Service | SYSTEM |
| GET | /api/lab/orders | Listar órdenes (filtro por status) | LABORATORY, ADMIN |
| GET | /api/lab/orders/{id} | Consultar orden por ID | LABORATORY, DOCTOR, ADMIN |
| PUT | /api/lab/orders/{id}/collect | Registrar toma de muestra | LABORATORY |
| PUT | /api/lab/orders/{id}/result | Subir resultado PDF | LABORATORY |
| GET | /api/lab/results/{patientId} | Resultados de un paciente | DOCTOR, PATIENT, ADMIN |

## 4. Non-Functional Requirements

- Tiempo de respuesta < 300ms para consultas
- Almacenamiento de PDFs: referencia en BD, archivo en filesystem/S3 (MVP: filesystem)
- Registro en Eureka como LAB-SERVICE
- Health check en /actuator/health

## 5. Business Rules

- BR1: Solo órdenes con estado PENDING pueden pasar a IN_PROGRESS
- BR2: Solo órdenes con estado IN_PROGRESS pueden pasar a COMPLETED
- BR3: Paciente solo puede ver sus propios resultados
- BR4: CERO JOINs con otros esquemas — datos del paciente vía HTTP a patient-service
- BR5: Mensajes de error en español

## 6. Dependencies

- Clinical Service (8083): Envía notificaciones de nuevas órdenes
- Patient Service (8082): Para obtener datos demográficos del paciente
- API Gateway (8080): Enrutamiento y validación JWT
- Eureka Server (8761): Service Discovery

---
**Created**: April 16, 2026 | **Status**: ✅ Ready | **Version**: 1.0.0
