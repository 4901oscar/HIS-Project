# Requirements: Pharmacy Service

## 1. Overview

El **Pharmacy Service** es el microservicio responsable de la gestión de inventario de medicamentos y el despacho de recetas médicas en el sistema MedFlow HIS.

**Arquitectura**: MVC (Capas) - CRUD simple  
**Base de Datos**: pharmacy_schema (PostgreSQL)  
**Puerto**: 8085  
**Registro en Eureka**: PHARMACY-SERVICE

## 2. User Stories

### US-1: Recibir Receta Médica
**Como** farmacéutico,  
**Quiero** ver las recetas médicas pendientes de despacho,  
**Para** preparar los medicamentos para el paciente.

**Acceptance Criteria:**
- [ ] Puedo ver todas las recetas con estado PENDING
- [ ] Cada receta muestra: código, paciente, medicamentos, dosis, frecuencia, doctor
- [ ] Puedo buscar receta por código de prescripción

### US-2: Despachar Medicamentos
**Como** farmacéutico,  
**Quiero** registrar el despacho de una receta,  
**Para** que quede constancia de que el paciente recibió sus medicamentos.

**Acceptance Criteria:**
- [ ] Puedo marcar una receta como DISPENSED
- [ ] El sistema verifica que los medicamentos estén en inventario
- [ ] Se descuenta el stock de cada medicamento despachado
- [ ] Se registra timestamp y farmacéutico que despachó

### US-3: Gestionar Inventario de Medicamentos
**Como** farmacéutico o administrador,  
**Quiero** gestionar el catálogo e inventario de medicamentos,  
**Para** mantener el stock actualizado.

**Acceptance Criteria:**
- [ ] Puedo agregar nuevos medicamentos al catálogo
- [ ] Puedo actualizar el stock de un medicamento
- [ ] Puedo ver medicamentos con stock bajo (< 10 unidades)
- [ ] El sistema alerta cuando el stock llega a 0

### US-4: Consultar Recetas de un Paciente
**Como** paciente,  
**Quiero** ver mis recetas médicas,  
**Para** conocer mis medicamentos prescritos.

**Acceptance Criteria:**
- [ ] Puedo ver mis recetas ordenadas por fecha descendente
- [ ] Cada receta muestra medicamentos, dosis y estado
- [ ] Solo puedo ver mis propias recetas

## 3. Functional Requirements

### FR1: Gestión de Recetas
- Recibir notificación de nueva receta desde Clinical Service
- Almacenar: prescriptionCode, patientId, doctorId, medications (JSON), status
- Estados: PENDING → DISPENSED | CANCELLED

### FR2: Gestión de Inventario
- Catálogo de medicamentos: name, description, unit, currentStock, minStock
- Actualizar stock al despachar receta
- Alertar cuando stock < minStock

### FR3: Despacho
- Validar disponibilidad de stock antes de despachar
- Crear registro de Dispensation con: prescriptionId, patientId, dispensedAt, dispensedBy, medications
- Descontar stock de cada medicamento

### FR4: Endpoints REST

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | /api/pharmacy/prescriptions/notify | Recibir receta desde Clinical Service | SYSTEM |
| GET | /api/pharmacy/prescriptions | Listar recetas (filtro por status) | PHARMACY, ADMIN |
| GET | /api/pharmacy/prescriptions/{code} | Buscar receta por código | PHARMACY, ADMIN |
| PUT | /api/pharmacy/prescriptions/{id}/dispense | Despachar receta | PHARMACY |
| GET | /api/pharmacy/prescriptions/patient/{patientId} | Recetas de un paciente | PATIENT, DOCTOR, ADMIN |
| GET | /api/pharmacy/medications | Listar medicamentos | PHARMACY, ADMIN |
| POST | /api/pharmacy/medications | Agregar medicamento | PHARMACY, ADMIN |
| PUT | /api/pharmacy/medications/{id}/stock | Actualizar stock | PHARMACY, ADMIN |
| GET | /api/pharmacy/medications/low-stock | Medicamentos con stock bajo | PHARMACY, ADMIN |

## 4. Non-Functional Requirements

- Tiempo de respuesta < 300ms
- Transacciones ACID para despacho (stock + dispensation atómicos)
- Registro en Eureka como PHARMACY-SERVICE

## 5. Business Rules

- BR1: Solo recetas PENDING pueden ser despachadas
- BR2: No se puede despachar si stock insuficiente → error 409
- BR3: Paciente solo puede ver sus propias recetas
- BR4: CERO JOINs con otros esquemas
- BR5: Mensajes de error en español
- BR6: Stock nunca puede ser negativo

## 6. Dependencies

- Clinical Service (8083): Envía notificaciones de nuevas recetas
- Patient Service (8082): Para datos demográficos del paciente
- API Gateway (8080): Enrutamiento y validación JWT
- Eureka Server (8761): Service Discovery

---
**Created**: April 16, 2026 | **Status**: ✅ Ready | **Version**: 1.0.0
