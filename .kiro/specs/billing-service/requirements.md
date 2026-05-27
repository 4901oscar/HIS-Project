# Requirements: Billing Service

## 1. Overview

El **Billing Service** es el microservicio responsable de la facturación interna del hospital, procesamiento de pagos y generación de recibos en el sistema MedFlow HIS.

**Arquitectura**: MVC (Capas) - CRUD simple  
**Base de Datos**: billing_schema (PostgreSQL)  
**Puerto**: 8086  
**Registro en Eureka**: BILLING-SERVICE

## 2. User Stories

### US-1: Registrar Cargos de Servicios
**Como** cajero,  
**Quiero** registrar los servicios prestados a un paciente,  
**Para** generar la factura correspondiente.

**Acceptance Criteria:**
- [ ] Puedo agregar cargos por: consulta médica, laboratorio, medicamentos, otros
- [ ] Cada cargo tiene: descripción, cantidad, precio unitario, subtotal
- [ ] El sistema calcula el total automáticamente

### US-2: Procesar Pago
**Como** cajero,  
**Quiero** registrar el pago de una factura,  
**Para** marcarla como pagada y generar el recibo.

**Acceptance Criteria:**
- [ ] Puedo registrar el método de pago (CASH, CARD, TRANSFER)
- [ ] El sistema valida que el monto pagado cubra el total
- [ ] Se genera un número de recibo único
- [ ] La factura queda marcada como PAID

### US-3: Consultar Facturas de un Paciente
**Como** cajero o paciente,  
**Quiero** ver el historial de facturas de un paciente,  
**Para** revisar los cobros realizados.

**Acceptance Criteria:**
- [ ] Puedo ver facturas ordenadas por fecha descendente
- [ ] Cada factura muestra: número, fecha, cargos, total, estado, método de pago
- [ ] Paciente solo puede ver sus propias facturas

### US-4: Aplicar Descuento
**Como** cajero,  
**Quiero** aplicar un descuento a una factura,  
**Para** reflejar exoneraciones o descuentos autorizados.

**Acceptance Criteria:**
- [ ] Puedo aplicar descuento en porcentaje o monto fijo
- [ ] El sistema recalcula el total con el descuento
- [ ] Solo facturas PENDING pueden recibir descuentos

## 3. Functional Requirements

### FR1: Gestión de Facturas
- Crear factura con: invoiceNumber (auto-generado), patientId, charges, status
- Estados: PENDING → PAID | CANCELLED
- Calcular total automáticamente de la suma de cargos

### FR2: Gestión de Cargos
- Tipos de cargo: CONSULTATION, LABORATORY, MEDICATION, OTHER
- Campos: description, quantity, unitPrice, subtotal (quantity * unitPrice)

### FR3: Procesamiento de Pagos
- Registrar pago con: invoiceId, amount, paymentMethod, paidAt, receivedBy
- Validar que amount >= invoice.total
- Calcular cambio si amount > total

### FR4: Endpoints REST

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | /api/billing/invoices | Crear factura con cargos | CASHIER, ADMIN |
| GET | /api/billing/invoices | Listar facturas (filtro por status) | CASHIER, ADMIN |
| GET | /api/billing/invoices/{id} | Consultar factura | CASHIER, ADMIN |
| POST | /api/billing/invoices/{id}/pay | Procesar pago | CASHIER |
| PUT | /api/billing/invoices/{id}/discount | Aplicar descuento | CASHIER, ADMIN |
| DELETE | /api/billing/invoices/{id} | Cancelar factura | CASHIER, ADMIN |
| GET | /api/billing/invoices/patient/{patientId} | Facturas de un paciente | PATIENT, CASHIER, ADMIN |

## 4. Non-Functional Requirements

- Tiempo de respuesta < 300ms
- Transacciones ACID para procesamiento de pagos
- Números de factura únicos y secuenciales (INV-YYYYMMDD-XXXX)
- Registro en Eureka como BILLING-SERVICE

## 5. Business Rules

- BR1: Solo facturas PENDING pueden ser pagadas
- BR2: Solo facturas PENDING pueden ser canceladas o recibir descuentos
- BR3: El monto pagado debe ser >= total de la factura
- BR4: Paciente solo puede ver sus propias facturas
- BR5: CERO JOINs con otros esquemas
- BR6: Mensajes de error en español
- BR7: Total nunca puede ser negativo

## 6. Dependencies

- Patient Service (8082): Para datos demográficos del paciente
- API Gateway (8080): Enrutamiento y validación JWT
- Eureka Server (8761): Service Discovery

---
**Created**: April 16, 2026 | **Status**: ✅ Ready | **Version**: 1.0.0
