# API Endpoints — MedFlow HIS

**Base URL:** `http://localhost:8080/api`

Todos los endpoints (excepto login y registro) requieren el header:
```
Authorization: Bearer {jwt_token}
```

Clinical Service además requiere:
```
X-User-Id: {user_uuid}
```

---

## Auth Service — `/api/auth`

### POST /api/auth/login
Login de empleados y pacientes.

**Request:**
```json
{
  "username": "admin",
  "password": "Admin1234"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": "uuid",
    "username": "admin",
    "email": "admin@medflow.com",
    "fullName": "Administrador Sistema",
    "roles": ["ADMIN"],
    "active": true
  }
}
```

---

### POST /api/auth/register
Registro de paciente desde el portal público (auto-registro).

**Request:**
```json
{
  "dpi": "1234567890101",
  "firstName": "Juan",
  "firstLastName": "Pérez",
  "email": "juan@example.com",
  "phone": "55551234",
  "birthDate": "1990-05-15",
  "gender": "M",
  "password": "MiPassword123"
}
```

**Response 200:**
```json
{
  "message": "Cuenta creada. Revisa tu email para activarla.",
  "email": "juan@example.com"
}
```

---

### GET /api/auth/activate?token={token}
Activa la cuenta del paciente desde el link del email.

---

### POST /api/auth/logout
Revoca el token actual (lo agrega a la blacklist).

---

### POST /api/auth/internal/create-patient
Crea una cuenta de paciente desde admisión (uso interno, requiere rol ADMISSION o ADMIN).

**Request:**
```json
{
  "dpi": "1234567890101",
  "firstName": "Juan",
  "firstLastName": "Pérez",
  "email": "juan@example.com",
  "phone": "55551234",
  "birthDate": "1990-05-15",
  "gender": "M"
}
```

**Response 200:**
```json
{
  "userId": "uuid",
  "patientId": "uuid",
  "username": "juan.perez",
  "temporaryPassword": "Temp1234!",
  "message": "Cuenta creada. Se envió contraseña temporal al email."
}
```

---

### GET /api/auth/users/{userId}
Obtiene el nombre completo de un usuario por ID.

**Response 200:**
```json
{
  "id": "uuid",
  "fullName": "Juan Pérez"
}
```

---

## Patient Service — `/api/patients`

### POST /api/patients
Crea un nuevo paciente.

**Request:**
```json
{
  "dpi": "1234567890101",
  "nit": "12345678",
  "firstName": "Juan",
  "secondName": "Carlos",
  "firstLastName": "Pérez",
  "secondLastName": "García",
  "email": "juan@example.com",
  "phone": "55551234",
  "birthDate": "1990-05-15",
  "gender": "M",
  "department": "Guatemala",
  "municipality": "Guatemala",
  "zone": "10",
  "address": "5a Avenida 10-05"
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "dpi": "1234567890101",
  "firstName": "Juan",
  "firstLastName": "Pérez",
  "email": "juan@example.com",
  "phone": "55551234",
  "birthDate": "1990-05-15",
  "gender": "M",
  "createdAt": "2026-05-15T10:00:00Z"
}
```

---

### GET /api/patients/{id}
Obtiene un paciente por su UUID.

---

### GET /api/patients/dpi/{dpi}
Busca un paciente por su DPI (número de identificación).

---

### GET /api/patients/search?query={texto}
Búsqueda general por nombre, DPI o email. Devuelve un array de pacientes.

---

### PUT /api/patients/{id}
Actualiza los datos de un paciente.

**Request (solo los campos a actualizar):**
```json
{
  "email": "nuevo@example.com",
  "phone": "55559999",
  "address": "Nueva dirección"
}
```

---

## Clinical Service — `/api/clinical`

### Citas

#### GET /api/clinical/appointments/slots
Consulta los slots disponibles para un doctor en una fecha.

**Query params:** `doctorId`, `date` (YYYY-MM-DD)

**Response 200:**
```json
{
  "doctorId": "uuid",
  "date": "2026-05-15",
  "availableSlots": ["08:00:00", "08:30:00", "09:00:00", "09:30:00"]
}
```

---

#### GET /api/clinical/appointments
Lista citas con filtros opcionales.

**Query params opcionales:**
- `queue=triage` — citas en estado VITAL_SIGNS (para la cola de signos vitales)
- `status=SCHEDULED` — filtrar por estado
- `doctorId=uuid` — citas de un doctor específico

**Response 200:** Array de citas con `patientName`, `patientDpi`, `status`, etc.

---

#### POST /api/clinical/appointments
Crea una nueva cita.

**Request:**
```json
{
  "patientId": "uuid",
  "doctorId": "uuid",
  "appointmentDate": "2026-05-15",
  "appointmentTime": "09:00:00",
  "notes": "Primera consulta"
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "patientId": "uuid",
  "patientName": "Juan Pérez",
  "patientDpi": "1234567890101",
  "doctorId": "uuid",
  "appointmentDate": "2026-05-15",
  "appointmentTime": "09:00:00",
  "status": "SCHEDULED",
  "qrCodeBase64": "data:image/png;base64,...",
  "createdAt": "2026-05-15T08:00:00Z"
}
```

---

#### PUT /api/clinical/appointments/{id}/activate
Activa una cita (SCHEDULED → VITAL_SIGNS). Llamado por admisión cuando el paciente llega.

---

#### DELETE /api/clinical/appointments/{id}
Cancela una cita.

---

#### GET /api/clinical/appointments/{id}/triage
Obtiene el triaje de una cita específica.

---

#### GET /api/clinical/appointments/{id}/vital-signs
Obtiene los signos vitales de una cita específica.

---

#### GET /api/clinical/appointments/{id}/prescription
Obtiene la receta de una cita específica.

---

#### PATCH /api/clinical/appointments/{id}/dispense-medication
Marca la receta de una cita como despachada. Llamado por farmacia.

---

### Signos Vitales

#### POST /api/clinical/vital-signs
Registra los signos vitales de un paciente.

**Request:**
```json
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
```

**Response 200:**
```json
{
  "id": "uuid",
  "patientId": "uuid",
  "systolicPressure": 120,
  "diastolicPressure": 80,
  "heartRate": 75,
  "respiratoryRate": 16,
  "temperature": 36.5,
  "oxygenSaturation": 98,
  "weight": 70.5,
  "height": 1.75,
  "bmi": 23.02,
  "recordedAt": "2026-05-15T09:15:00Z"
}
```

---

### Triaje

#### POST /api/clinical/triage
Realiza el triaje Manchester.

**Request:**
```json
{
  "appointmentId": "uuid",
  "patientId": "uuid",
  "motifId": "uuid-del-motivo",
  "discriminatorIds": ["uuid-disc-1", "uuid-disc-2"]
}
```

**Response 200:**
```json
{
  "id": "uuid",
  "patientId": "uuid",
  "priorityLevel": "NARANJA",
  "priorityDescription": "Muy urgente",
  "maxWaitTimeMinutes": 10,
  "performedAt": "2026-05-15T09:20:00Z"
}
```

---

### Catálogo Manchester

#### GET /api/clinical/manchester/motifs
Lista todos los motivos de triaje disponibles.

#### GET /api/clinical/manchester/motifs/{motifId}/discriminators
Lista los discriminadores de un motivo específico.

---

### Consultas

#### POST /api/clinical/consultations
Registra una consulta médica.

**Request:**
```json
{
  "patientId": "uuid",
  "appointmentId": "uuid",
  "chiefComplaint": "Dolor de cabeza persistente",
  "symptoms": "Dolor frontal, náuseas, fotofobia",
  "primaryDiagnosis": "Migraña sin aura (G43.0)",
  "secondaryDiagnoses": ["Tensión muscular cervical"],
  "medicalNotes": "Episodios recurrentes en últimas 2 semanas",
  "treatmentPlan": "Analgésicos, reposo, seguimiento en 1 semana",
  "hasLabOrders": false,
  "hasPrescription": true,
  "followUpDate": "2026-05-22",
  "followUpTime": "09:00:00"
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "patientId": "uuid",
  "doctorId": "uuid",
  "chiefComplaint": "Dolor de cabeza persistente",
  "primaryDiagnosis": "Migraña sin aura (G43.0)",
  "secondaryDiagnoses": ["Tensión muscular cervical"],
  "consultationDate": "2026-05-15T09:30:00Z"
}
```

---

### Recetas

#### POST /api/clinical/prescriptions
Genera una receta médica.

**Request:**
```json
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
```

**Response 201:**
```json
{
  "id": "uuid",
  "prescriptionCode": "RX-20260515-001",
  "patientId": "uuid",
  "doctorId": "uuid",
  "medications": [...],
  "status": "PENDING",
  "issuedAt": "2026-05-15T09:35:00Z"
}
```

---

### Órdenes de Laboratorio

#### POST /api/clinical/lab-orders
Genera una orden de laboratorio.

**Request:**
```json
{
  "consultationId": "uuid",
  "patientId": "uuid",
  "appointmentId": "uuid",
  "testNames": ["Hemograma Completo", "Glucosa en Ayunas"]
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "orderCode": "LAB-20260515-001",
  "patientId": "uuid",
  "doctorId": "uuid",
  "testNames": ["Hemograma Completo", "Glucosa en Ayunas"],
  "status": "PENDING",
  "orderedAt": "2026-05-15T09:40:00Z"
}
```

---

### Historial Clínico

#### GET /api/clinical/history/{patientId}
Obtiene el historial clínico completo de un paciente.

**Response 200:**
```json
{
  "patient": { "id": "uuid", "firstName": "Juan", ... },
  "consultations": [...],
  "vitalSigns": [...],
  "prescriptions": [...],
  "labOrders": [...]
}
```

---

### Doctores y Clínicas

#### GET /api/clinical/doctors
Lista todos los doctores activos.

#### POST /api/clinical/doctors
Crea un nuevo doctor (requiere ADMIN).

#### GET /api/clinical/clinics
Lista todas las clínicas.

#### POST /api/clinical/clinics
Crea una nueva clínica (requiere ADMIN).

---

## Lab Service — `/api/lab`

### GET /api/lab/orders
Lista las órdenes de laboratorio. Filtros opcionales: `status`, `patientId`.

### POST /api/lab/orders/{orderId}/collect-sample
Registra la recolección de muestra.

### POST /api/lab/orders/{orderId}/validate-sample
Valida la muestra recolectada.

### POST /api/lab/orders/{orderId}/process
Marca la muestra como en procesamiento.

### POST /api/lab/orders/{orderId}/upload-result
Sube el resultado (multipart/form-data con el archivo PDF o imagen).

### GET /api/lab/exam-types
Lista el catálogo de tipos de exámenes.

---

## Pharmacy Service — `/api/pharmacy`

### GET /api/pharmacy/prescriptions/pending
Lista las recetas pendientes de despacho.

### GET /api/pharmacy/medications
Lista el catálogo de medicamentos con stock actual.

### POST /api/pharmacy/medications
Agrega un medicamento al catálogo.

### PUT /api/pharmacy/medications/{id}
Actualiza un medicamento (precio, stock, estado).

---

## Billing Service — `/api/billing`

### GET /api/billing/service-items
Lista el catálogo de servicios con precios.

**Response 200:**
```json
[
  { "id": "uuid", "code": "CONS-GEN", "name": "Consulta General", "category": "CONSULTATION", "price": 150.00, "status": "ACTIVE" },
  { "id": "uuid", "code": "LAB-HEM", "name": "Hemograma Completo", "category": "LABORATORY", "price": 75.00, "status": "ACTIVE" }
]
```

### POST /api/billing/invoices
Crea una factura con cargos.

**Request:**
```json
{
  "patientId": "uuid",
  "appointmentId": "uuid",
  "customerName": "Juan Pérez",
  "customerNit": "CF",
  "charges": [
    {
      "type": "CONSULTATION",
      "description": "Consulta General",
      "quantity": 1,
      "unitPrice": 150.00
    },
    {
      "type": "LABORATORY",
      "description": "Hemograma Completo",
      "quantity": 1,
      "unitPrice": 75.00
    }
  ]
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "invoiceNumber": "FAC-20260515-001",
  "patientId": "uuid",
  "subtotal": 225.00,
  "discountAmount": 0.00,
  "total": 225.00,
  "status": "PENDING",
  "createdAt": "2026-05-15T10:00:00Z"
}
```

### POST /api/billing/invoices/{invoiceId}/pay
Procesa el pago de una factura.

**Request:**
```json
{
  "amount": 250.00,
  "method": "CASH"
}
```

**Response 200:**
```json
{
  "changeAmount": 25.00,
  "paidAt": "2026-05-15T10:05:00Z",
  "status": "PAID"
}
```

Métodos de pago disponibles: `CASH`, `CARD`, `TRANSFER`

### GET /api/billing/invoices/{id}
Obtiene el detalle de una factura.

---

## Códigos de error comunes

| Código | Significado |
|--------|-------------|
| 400 | Request inválido (campos faltantes o formato incorrecto) |
| 401 | Token JWT faltante, inválido o expirado |
| 403 | El usuario no tiene el rol requerido para esta acción |
| 404 | Recurso no encontrado |
| 409 | Conflicto (ej: DPI ya registrado, slot ya ocupado) |
| 429 | Rate limit excedido (100 req/min por IP) |
| 503 | Servicio no disponible |
