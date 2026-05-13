# Flujo de Atención al Paciente — MedFlow HIS

Este documento describe el flujo completo desde que un paciente llega al hospital hasta que sale con su receta y resultados de laboratorio. Cada paso corresponde a un rol específico y a llamadas reales a la API.

---

## Diagrama del flujo completo

```
[ADMISIÓN]          Registra paciente → Activa cita
        │
        ▼
[SIGNOS VITALES]    Captura signos vitales
        │
        ▼
[DOCTOR]            Realiza triaje Manchester → Asigna prioridad
        │
        ▼
[DOCTOR]            Consulta médica → Diagnóstico + receta + orden de lab
        │
        ├──────────────────────────────────────┐
        ▼                                      ▼
[LABORATORIO]       Procesa muestra        [FARMACIA]    Despacha medicamentos
        │                                      │
        ▼                                      ▼
[CAJA]              Cobra consulta, exámenes y medicamentos
        │
        ▼
[PACIENTE]          Ve su historial, recetas y resultados en el portal
```

---

## Estados de una cita

Una cita pasa por los siguientes estados en orden:

```
SCHEDULED → VITAL_SIGNS → CONSULTATION → COMPLETED
                                       ↘ CANCELLED
                                       ↘ MISSED
```

| Estado | Significado | Quién lo activa |
|--------|-------------|-----------------|
| `SCHEDULED` | Cita agendada, esperando que el paciente llegue | Admisión al crear la cita |
| `VITAL_SIGNS` | Paciente llegó, esperando signos vitales | Admisión al activar la cita |
| `CONSULTATION` | Signos vitales registrados, esperando al doctor | Signos Vitales al guardar |
| `COMPLETED` | Consulta terminada | Doctor al finalizar consulta |
| `CANCELLED` | Cita cancelada | Admisión |
| `MISSED` | Paciente no llegó | Scheduler automático |

Adicionalmente, cuando hay laboratorio, la cita puede pasar por estados intermedios:
```
CONSULTATION → LAB_SAMPLE_COLLECTION → LAB_SAMPLE_PENDING → LAB_PROCESSING → LAB_RESULTS_READY → COMPLETED
```

---

## Paso 1 — Registro del paciente (Rol: ADMISSION)

Si el paciente es nuevo, admisión lo registra. Esto crea simultáneamente:
1. Un registro en `patient_schema.patients` (datos demográficos)
2. Una cuenta de usuario en `auth_schema.users` con rol `PATIENT`
3. Se envía un email con la contraseña temporal al paciente

```
POST /api/patients
{
  "dpi": "1234567890101",
  "firstName": "Juan",
  "firstLastName": "Pérez",
  "email": "juan@example.com",
  "phone": "55551234",
  "birthDate": "1990-05-15",
  "gender": "M",
  "address": "Ciudad de Guatemala"
}
```

Si el paciente ya existe, se busca por DPI:
```
GET /api/patients/dpi/1234567890101
```

---

## Paso 2 — Agendamiento de cita (Rol: ADMISSION)

Admisión consulta los slots disponibles para un doctor en una fecha y crea la cita.

```
GET /api/clinical/appointments/slots?doctorId={uuid}&date=2026-05-15
→ { "availableSlots": ["08:00:00", "08:30:00", "09:00:00", ...] }

POST /api/clinical/appointments
{
  "patientId": "uuid-del-paciente",
  "doctorId": "uuid-del-doctor",
  "appointmentDate": "2026-05-15",
  "appointmentTime": "09:00:00",
  "notes": "Primera consulta"
}
→ { "id": "uuid", "status": "SCHEDULED", ... }
```

Los slots se reservan atómicamente en Redis para evitar doble reserva.

---

## Paso 3 — Activación de cita (Rol: ADMISSION)

Cuando el paciente llega físicamente, admisión activa la cita. Esto cambia el estado de `SCHEDULED` a `VITAL_SIGNS` y la cita aparece en la cola de signos vitales.

```
PUT /api/clinical/appointments/{id}/activate
```

---

## Paso 4 — Registro de signos vitales (Rol: VITAL_SIGNS)

El personal de enfermería ve la lista de citas en estado `VITAL_SIGNS` y registra los signos vitales del paciente. El BMI se calcula automáticamente si se proveen peso y altura.

```
POST /api/clinical/vital-signs
{
  "appointmentId": "uuid",
  "patientId": "uuid",
  "systolicPressure": 120,
  "diastolicPressure": 80,
  "heartRate": 75,
  "respiratoryRate": 16,
  "temperature": 36.5,
  "oxygenSaturation": 98,
  "weight": 70.5,
  "height": 1.75
}
→ { "bmi": 23.02, ... }
```

Al guardar, la cita pasa a estado `CONSULTATION`.

---

## Paso 5 — Triaje Manchester (Rol: DOCTOR)

El doctor realiza el triaje seleccionando un motivo de consulta y los discriminadores que aplican. El algoritmo Manchester calcula automáticamente el nivel de prioridad.

```
POST /api/clinical/triage
{
  "appointmentId": "uuid",
  "patientId": "uuid",
  "motifId": "uuid-del-motivo",
  "discriminatorIds": ["uuid-discriminador-1", "uuid-discriminador-2"]
}
→ {
    "priorityLevel": "NARANJA",
    "priorityDescription": "Muy urgente",
    "maxWaitTimeMinutes": 10
  }
```

### Niveles de prioridad Manchester

| Color | Nivel | Tiempo máximo de espera |
|-------|-------|------------------------|
| 🔴 ROJO | Emergencia | 0 minutos (atención inmediata) |
| 🟠 NARANJA | Muy urgente | 10 minutos |
| 🟡 AMARILLO | Urgente | 60 minutos |
| 🟢 VERDE | Poco urgente | 120 minutos |
| 🔵 AZUL | No urgente | 240 minutos |

El catálogo de motivos y discriminadores se gestiona desde el panel de administrador.

---

## Paso 6 — Consulta médica (Rol: DOCTOR)

El doctor registra el diagnóstico, plan de tratamiento y notas. Puede indicar si el paciente necesita laboratorio o receta.

```
POST /api/clinical/consultations
{
  "patientId": "uuid",
  "appointmentId": "uuid",
  "chiefComplaint": "Dolor de cabeza persistente",
  "symptoms": "Dolor frontal, náuseas, fotofobia",
  "primaryDiagnosis": "Migraña sin aura (G43.0)",
  "secondaryDiagnoses": ["Tensión muscular cervical"],
  "medicalNotes": "Episodios recurrentes en últimas 2 semanas",
  "treatmentPlan": "Analgésicos, reposo, seguimiento en 1 semana",
  "hasLabOrders": true,
  "hasPrescription": true
}
```

---

## Paso 7a — Receta médica (Rol: DOCTOR)

Si el paciente necesita medicamentos, el doctor genera la receta. El código de receta se genera automáticamente (formato `RX-YYYYMMDD-NNN`).

```
POST /api/clinical/prescriptions
{
  "consultationId": "uuid",
  "patientId": "uuid",
  "medications": [
    {
      "name": "Ibuprofeno",
      "dosage": "400mg",
      "frequency": "Cada 8 horas",
      "durationDays": 5,
      "route": "Oral",
      "specialInstructions": "Tomar con alimentos",
      "dosageAmount": 1,
      "dosageUnit": "pastilla",
      "frequencyHours": 8,
      "totalQuantity": 15
    }
  ]
}
→ { "prescriptionCode": "RX-20260515-001", "status": "PENDING", ... }
```

Clinical-service notifica automáticamente a pharmacy-service (fire-and-forget).

---

## Paso 7b — Orden de laboratorio (Rol: DOCTOR)

Si el paciente necesita exámenes, el doctor genera la orden.

```
POST /api/clinical/lab-orders
{
  "consultationId": "uuid",
  "patientId": "uuid",
  "appointmentId": "uuid",
  "testNames": ["Hemograma Completo", "Glucosa en Ayunas"]
}
→ { "orderCode": "LAB-20260515-001", "status": "PENDING", ... }
```

Clinical-service notifica automáticamente a lab-service (fire-and-forget). La cita pasa a estado `LAB_SAMPLE_COLLECTION`.

---

## Paso 8 — Flujo de laboratorio (Rol: LABORATORY)

El técnico de laboratorio sigue un flujo de 4 pasos:

**Paso 1 — Recolección de muestra**
```
POST /api/lab/orders/{orderId}/collect-sample
{ "collectedBy": "uuid-tecnico", "notes": "Muestra en buen estado" }
```
Estado de cita: `LAB_SAMPLE_COLLECTION` → `LAB_SAMPLE_PENDING`

**Paso 2 — Validación de muestra**
```
POST /api/lab/orders/{orderId}/validate-sample
{ "validatedBy": "uuid-tecnico", "isValid": true }
```

**Paso 3 — Procesamiento**
```
POST /api/lab/orders/{orderId}/process
```
Estado de cita: `LAB_SAMPLE_PENDING` → `LAB_PROCESSING`

**Paso 4 — Subida de resultados**
```
POST /api/lab/orders/{orderId}/upload-result
(multipart/form-data con el PDF del resultado)
```
Estado de cita: `LAB_PROCESSING` → `LAB_RESULTS_READY`

---

## Paso 9 — Despacho de farmacia (Rol: PHARMACY)

El farmacéutico ve las recetas pendientes, verifica el stock y despacha.

```
GET /api/pharmacy/prescriptions/pending
→ lista de recetas con estado PENDING

PATCH /api/clinical/appointments/{appointmentId}/dispense-medication
→ marca la receta como DISPENSED y actualiza el inventario
```

---

## Paso 10 — Cobro en caja (Rol: CASHIER)

El cajero crea la factura con los cargos correspondientes y procesa el pago.

```
POST /api/billing/invoices
{
  "patientId": "uuid",
  "appointmentId": "uuid",
  "customerName": "Juan Pérez",
  "customerNit": "CF",
  "charges": [
    { "type": "CONSULTATION", "description": "Consulta General", "quantity": 1, "unitPrice": 150.00 },
    { "type": "LABORATORY", "description": "Hemograma Completo", "quantity": 1, "unitPrice": 75.00 }
  ]
}
→ { "invoiceNumber": "FAC-20260515-001", "total": 225.00, "status": "PENDING" }

POST /api/billing/invoices/{invoiceId}/pay
{
  "amount": 225.00,
  "method": "CASH"
}
→ { "changeAmount": 0.00, "status": "PAID" }
```

---

## Paso 11 — Portal del paciente (Rol: PATIENT)

El paciente puede ver su historial completo desde el portal web.

```
GET /api/clinical/history/{patientId}
→ {
    "patient": { ... },
    "consultations": [ ... ],
    "vitalSigns": [ ... ],
    "prescriptions": [ ... ],
    "labOrders": [ ... ]
  }
```

El paciente accede con el email y la contraseña temporal que recibió cuando fue registrado en admisión. Solo puede ver su propio historial.

---

## Flujo de agendamiento en línea (público)

Los pacientes también pueden agendar citas desde el portal web público sin necesidad de ir a admisión:

1. Visitan `/appointment` en el sitio web
2. Seleccionan doctor, fecha y hora
3. Pagan en línea (`/payment`)
4. Reciben confirmación por email

Este flujo crea la cita en estado `SCHEDULED` igual que si la hubiera creado admisión.
