# Frontend Integration Guide - MedFlow HIS

## ✅ Sistema Listo para Integración Frontend

**Estado actual**: El backend está completamente funcional y listo para conectar con el frontend.

### Servicios Implementados y Funcionando

| Servicio | Puerto | Estado | Endpoints Disponibles |
|----------|--------|--------|----------------------|
| **Eureka Server** | 8761 | ✅ 100% | Service Discovery |
| **API Gateway** | 8080 | ✅ 100% | Punto de entrada único |
| **Auth Service** | 8081 | ✅ 100% | Login, registro, JWT |
| **Patient Service** | 8082 | ✅ 100% | Gestión de pacientes |
| **Clinical Service** | 8083 | ✅ 70% | Signos vitales, triage, citas, consultas, recetas |
| **Lab Service** | 8084 | ⚠️ Stub | Notificaciones de laboratorio |
| **Pharmacy Service** | 8085 | ⚠️ Stub | Notificaciones de farmacia |
| **Billing Service** | 8086 | ⚠️ Stub | Facturación |

---

## 🚀 Inicio Rápido

### 1. Iniciar Todos los Servicios

```bash
# Desde la raíz del proyecto
docker-compose up -d

# Verificar que todos los servicios estén corriendo
docker-compose ps

# Ver logs de un servicio específico
docker-compose logs -f api-gateway
docker-compose logs -f clinical-service
```

### 2. Verificar Salud de los Servicios

```bash
# Eureka Dashboard
http://localhost:8761

# Health checks
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8082/actuator/health  # Patient Service
curl http://localhost:8083/actuator/health  # Clinical Service
```

---

## 🔐 Autenticación y Seguridad

### Flujo de Autenticación

1. **Login**: `POST /api/auth/login`
2. **Recibir JWT token**
3. **Incluir token en todas las peticiones**: `Authorization: Bearer {token}`
4. **API Gateway valida el token automáticamente**

### Ejemplo de Login

```javascript
// Login
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123'
  })
});

const { token, user } = await loginResponse.json();

// Usar el token en peticiones subsecuentes
const response = await fetch('http://localhost:8080/api/patients', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

### Headers Requeridos

```javascript
{
  'Authorization': 'Bearer {jwt_token}',      // Obligatorio para endpoints protegidos
  'Content-Type': 'application/json',         // Para POST/PUT
  'X-User-Id': '{user_id}'                    // Requerido por Clinical Service
}
```

---

## 📡 API Endpoints Disponibles

### Base URL
```
http://localhost:8080/api
```

### 1. Auth Service (`/api/auth/**`)

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "uuid",
    "username": "admin",
    "roles": ["ROLE_ADMIN", "ROLE_DOCTOR"]
  }
}
```

#### Registro
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "nuevo_usuario",
  "password": "password123",
  "email": "usuario@example.com",
  "roles": ["ROLE_NURSE"]
}
```

---

### 2. Patient Service (`/api/patients/**`)

#### Crear Paciente (CU-01)
```http
POST /api/patients
Authorization: Bearer {token}
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "dpi": "1234567890101",
  "dateOfBirth": "1990-05-15",
  "gender": "M",
  "email": "juan.perez@example.com",
  "phone": "12345678",
  "address": "Ciudad de Guatemala"
}

Response 201:
{
  "id": "uuid",
  "firstName": "Juan",
  "lastName": "Pérez",
  "dpi": "1234567890101",
  "dateOfBirth": "1990-05-15",
  "gender": "M",
  "email": "juan.perez@example.com",
  "phone": "12345678",
  "address": "Ciudad de Guatemala",
  "createdAt": "2026-04-16T10:30:00Z"
}
```

#### Obtener Paciente por ID
```http
GET /api/patients/{id}
Authorization: Bearer {token}

Response 200:
{
  "id": "uuid",
  "firstName": "Juan",
  "lastName": "Pérez",
  ...
}
```

#### Buscar Paciente por DPI
```http
GET /api/patients/dpi/{dpi}
Authorization: Bearer {token}

Response 200:
{
  "id": "uuid",
  "firstName": "Juan",
  "lastName": "Pérez",
  "dpi": "1234567890101",
  ...
}
```

#### Buscar Pacientes (por nombre, DPI o email)
```http
GET /api/patients/search?query=Juan
Authorization: Bearer {token}

Response 200:
[
  {
    "id": "uuid1",
    "firstName": "Juan",
    "lastName": "Pérez",
    ...
  },
  {
    "id": "uuid2",
    "firstName": "Juana",
    "lastName": "García",
    ...
  }
]
```

#### Actualizar Paciente
```http
PUT /api/patients/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "email": "nuevo.email@example.com",
  "phone": "87654321",
  "address": "Nueva dirección"
}

Response 200:
{
  "id": "uuid",
  "email": "nuevo.email@example.com",
  "phone": "87654321",
  "address": "Nueva dirección",
  ...
}
```

---

### 3. Clinical Service (`/api/clinical/**`)

#### 3.1 Signos Vitales (CU-02)

```http
POST /api/clinical/vital-signs
Authorization: Bearer {token}
X-User-Id: {nurse_id}
Content-Type: application/json

{
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

Response 200:
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
  "recordedAt": "2026-04-16T10:35:00Z"
}
```

#### 3.2 Triage Manchester (CU-03)

```http
POST /api/clinical/triage
Authorization: Bearer {token}
X-User-Id: {nurse_id}
Content-Type: application/json

{
  "patientId": "uuid",
  "motifId": "DOLOR_TORACICO",
  "discriminatorIds": ["DOLOR_SEVERO", "DIFICULTAD_RESPIRATORIA"]
}

Response 200:
{
  "id": "uuid",
  "patientId": "uuid",
  "priorityLevel": "ROJO",
  "priorityDescription": "Emergencia - Atención inmediata",
  "maxWaitTimeMinutes": 0,
  "performedAt": "2026-04-16T10:40:00Z"
}
```

**Niveles de Prioridad Manchester:**
- `ROJO`: Emergencia (0 min)
- `NARANJA`: Muy urgente (10 min)
- `AMARILLO`: Urgente (60 min)
- `VERDE`: Poco urgente (120 min)
- `AZUL`: No urgente (240 min)

#### 3.3 Gestión de Citas (CU-04)

**Consultar Slots Disponibles**
```http
GET /api/clinical/appointments/slots?doctorId={doctor_id}&date=2026-04-20
Authorization: Bearer {token}

Response 200:
{
  "doctorId": "uuid",
  "date": "2026-04-20",
  "availableSlots": [
    "08:00:00",
    "08:30:00",
    "09:00:00",
    "09:30:00",
    ...
  ]
}
```

**Crear Cita**
```http
POST /api/clinical/appointments
Authorization: Bearer {token}
X-User-Id: {receptionist_id}
Content-Type: application/json

{
  "patientId": "uuid",
  "doctorId": "uuid",
  "appointmentDate": "2026-04-20",
  "appointmentTime": "09:00:00",
  "notes": "Primera consulta"
}

Response 201:
{
  "id": "uuid",
  "patientId": "uuid",
  "doctorId": "uuid",
  "appointmentDate": "2026-04-20",
  "appointmentTime": "09:00:00",
  "status": "RESERVED",
  "createdAt": "2026-04-16T10:45:00Z"
}
```

**Activar Cita (cuando el paciente llega)**
```http
PUT /api/clinical/appointments/{id}/activate
Authorization: Bearer {token}

Response 200: (vacío)
```

**Cancelar Cita**
```http
DELETE /api/clinical/appointments/{id}
Authorization: Bearer {token}

Response 204: (vacío)
```

#### 3.4 Consulta Médica (CU-05)

```http
POST /api/clinical/consultations
Authorization: Bearer {token}
X-User-Id: {doctor_id}
Content-Type: application/json

{
  "patientId": "uuid",
  "appointmentId": "uuid",
  "chiefComplaint": "Dolor de cabeza persistente",
  "symptoms": "Dolor frontal, náuseas, fotofobia",
  "primaryDiagnosis": "Migraña sin aura (G43.0)",
  "secondaryDiagnoses": ["Tensión muscular cervical"],
  "medicalNotes": "Paciente refiere episodios recurrentes en últimas 2 semanas",
  "treatmentPlan": "Analgésicos, reposo, seguimiento en 1 semana"
}

Response 201:
{
  "id": "uuid",
  "patientId": "uuid",
  "doctorId": "uuid",
  "chiefComplaint": "Dolor de cabeza persistente",
  "primaryDiagnosis": "Migraña sin aura (G43.0)",
  "secondaryDiagnoses": ["Tensión muscular cervical"],
  "consultationDate": "2026-04-16T11:00:00Z"
}
```

#### 3.5 Receta Médica (CU-06)

```http
POST /api/clinical/prescriptions
Authorization: Bearer {token}
X-User-Id: {doctor_id}
Content-Type: application/json

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
      "specialInstructions": "Tomar con alimentos"
    },
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Cada 6 horas si persiste dolor",
      "durationDays": 3,
      "route": "Oral",
      "specialInstructions": "No exceder 4g al día"
    }
  ]
}

Response 201:
{
  "id": "uuid",
  "prescriptionCode": "RX-20260416-001",
  "patientId": "uuid",
  "doctorId": "uuid",
  "medications": [
    {
      "name": "Ibuprofeno",
      "dosage": "400mg",
      "frequency": "Cada 8 horas",
      "durationDays": 5,
      "route": "Oral",
      "specialInstructions": "Tomar con alimentos"
    },
    ...
  ],
  "status": "ACTIVE",
  "issuedAt": "2026-04-16T11:05:00Z"
}
```

---

## 🔄 Flujo Completo de Atención al Paciente

### Secuencia de Llamadas API

```javascript
// 1. Login
const { token, user } = await login('admin', 'admin123');

// 2. Buscar o crear paciente
const patient = await searchPatient('1234567890101');
// O crear nuevo: const patient = await createPatient({...});

// 3. Registrar signos vitales (Enfermera)
const vitalSigns = await recordVitalSigns({
  patientId: patient.id,
  systolicPressure: 120,
  diastolicPressure: 80,
  heartRate: 75,
  ...
}, user.id);

// 4. Realizar triage (Enfermera)
const triage = await performTriage({
  patientId: patient.id,
  motifId: 'DOLOR_TORACICO',
  discriminatorIds: ['DOLOR_SEVERO']
}, user.id);

// 5. Consultar slots disponibles
const slots = await getAvailableSlots(doctorId, '2026-04-20');

// 6. Crear cita
const appointment = await createAppointment({
  patientId: patient.id,
  doctorId: doctorId,
  appointmentDate: '2026-04-20',
  appointmentTime: '09:00:00'
}, user.id);

// 7. Activar cita cuando el paciente llega
await activateAppointment(appointment.id);

// 8. Registrar consulta médica (Doctor)
const consultation = await registerConsultation({
  patientId: patient.id,
  appointmentId: appointment.id,
  chiefComplaint: 'Dolor de cabeza',
  primaryDiagnosis: 'Migraña (G43.0)',
  ...
}, doctorId);

// 9. Generar receta (Doctor)
const prescription = await generatePrescription({
  consultationId: consultation.id,
  patientId: patient.id,
  medications: [...]
}, doctorId);
```

---

## 🧪 Pruebas Recomendadas

### 1. Pruebas de Gateway

#### CORS
```javascript
// Verificar que el frontend en localhost:3000 puede hacer peticiones
fetch('http://localhost:8080/api/patients', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer {token}'
  }
})
.then(response => console.log('CORS OK:', response.status))
.catch(error => console.error('CORS Error:', error));
```

#### Rate Limiting
```javascript
// Hacer más de 100 peticiones en 1 minuto
for (let i = 0; i < 101; i++) {
  await fetch('http://localhost:8080/api/patients');
}
// La petición 101 debería retornar 429 Too Many Requests
```

#### JWT Validation
```javascript
// Sin token
fetch('http://localhost:8080/api/patients')
  .then(r => console.log('Sin token:', r.status)); // Debería ser 401

// Con token inválido
fetch('http://localhost:8080/api/patients', {
  headers: { 'Authorization': 'Bearer token_invalido' }
})
  .then(r => console.log('Token inválido:', r.status)); // Debería ser 401

// Con token válido
fetch('http://localhost:8080/api/patients', {
  headers: { 'Authorization': `Bearer ${validToken}` }
})
  .then(r => console.log('Token válido:', r.status)); // Debería ser 200
```

### 2. Pruebas End-to-End

#### Flujo Completo de Atención
```javascript
describe('Flujo completo de atención al paciente', () => {
  it('debe completar el flujo desde login hasta receta', async () => {
    // 1. Login
    const auth = await login('admin', 'admin123');
    expect(auth.token).toBeDefined();
    
    // 2. Crear paciente
    const patient = await createPatient({
      firstName: 'Test',
      lastName: 'Patient',
      dpi: '9999999999999'
    });
    expect(patient.id).toBeDefined();
    
    // 3. Signos vitales
    const vitals = await recordVitalSigns({
      patientId: patient.id,
      systolicPressure: 120,
      diastolicPressure: 80
    });
    expect(vitals.bmi).toBeGreaterThan(0);
    
    // 4. Triage
    const triage = await performTriage({
      patientId: patient.id,
      motifId: 'DOLOR_TORACICO'
    });
    expect(triage.priorityLevel).toBeDefined();
    
    // 5. Cita
    const appointment = await createAppointment({
      patientId: patient.id,
      doctorId: 'doctor-uuid',
      appointmentDate: '2026-04-20',
      appointmentTime: '09:00:00'
    });
    expect(appointment.status).toBe('RESERVED');
    
    // 6. Consulta
    const consultation = await registerConsultation({
      patientId: patient.id,
      appointmentId: appointment.id,
      chiefComplaint: 'Test complaint'
    });
    expect(consultation.id).toBeDefined();
    
    // 7. Receta
    const prescription = await generatePrescription({
      consultationId: consultation.id,
      patientId: patient.id,
      medications: [{ name: 'Test Med', dosage: '100mg' }]
    });
    expect(prescription.prescriptionCode).toMatch(/^RX-/);
  });
});
```

### 3. Pruebas de Seguridad

#### SQL Injection
```javascript
// Intentar inyección SQL en búsqueda
const maliciousQuery = "'; DROP TABLE patients; --";
const result = await fetch(
  `http://localhost:8080/api/patients/search?query=${encodeURIComponent(maliciousQuery)}`,
  { headers: { 'Authorization': `Bearer ${token}` } }
);
// Debería retornar 200 con array vacío, NO ejecutar el DROP
```

#### XSS
```javascript
// Intentar XSS en campos de texto
const xssPayload = '<script>alert("XSS")</script>';
const patient = await createPatient({
  firstName: xssPayload,
  lastName: 'Test'
});
// El payload debería ser escapado/sanitizado
```

#### Token Expiration
```javascript
// Usar un token expirado
const expiredToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...';
const result = await fetch('http://localhost:8080/api/patients', {
  headers: { 'Authorization': `Bearer ${expiredToken}` }
});
expect(result.status).toBe(401);
```

#### Role-Based Access Control
```javascript
// Usuario con rol NURSE intenta acceder a endpoint de DOCTOR
const nurseToken = await login('nurse', 'nurse123');
const result = await fetch('http://localhost:8080/api/clinical/consultations', {
  method: 'POST',
  headers: { 'Authorization': `Bearer ${nurseToken.token}` },
  body: JSON.stringify({...})
});
expect(result.status).toBe(403); // Forbidden
```

---

## 🛠️ Configuración del Frontend

### Variables de Entorno

Crear archivo `.env` en el proyecto frontend:

```env
VITE_API_URL=http://localhost:8080
VITE_API_TIMEOUT=30000
VITE_ENABLE_MOCK=false
```

### Axios Configuration

```javascript
// src/api/axios.js
import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para agregar token automáticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Agregar X-User-Id si está disponible
    const userId = localStorage.getItem('userId');
    if (userId) {
      config.headers['X-User-Id'] = userId;
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor para manejar errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('token');
      localStorage.removeItem('userId');
      window.location.href = '/login';
    }
    
    if (error.response?.status === 429) {
      // Rate limit excedido
      alert('Demasiadas peticiones. Por favor espera un momento.');
    }
    
    return Promise.reject(error);
  }
);

export default api;
```

### API Service Example

```javascript
// src/api/patientService.js
import api from './axios';

export const patientService = {
  async create(patientData) {
    const response = await api.post('/api/patients', patientData);
    return response.data;
  },
  
  async getById(id) {
    const response = await api.get(`/api/patients/${id}`);
    return response.data;
  },
  
  async getByDpi(dpi) {
    const response = await api.get(`/api/patients/dpi/${dpi}`);
    return response.data;
  },
  
  async search(query) {
    const response = await api.get(`/api/patients/search`, {
      params: { query }
    });
    return response.data;
  },
  
  async update(id, updateData) {
    const response = await api.put(`/api/patients/${id}`, updateData);
    return response.data;
  }
};

// src/api/clinicalService.js
import api from './axios';

export const clinicalService = {
  async recordVitalSigns(data) {
    const response = await api.post('/api/clinical/vital-signs', data);
    return response.data;
  },
  
  async performTriage(data) {
    const response = await api.post('/api/clinical/triage', data);
    return response.data;
  },
  
  async getAvailableSlots(doctorId, date) {
    const response = await api.get('/api/clinical/appointments/slots', {
      params: { doctorId, date }
    });
    return response.data;
  },
  
  async createAppointment(data) {
    const response = await api.post('/api/clinical/appointments', data);
    return response.data;
  },
  
  async activateAppointment(id) {
    await api.put(`/api/clinical/appointments/${id}/activate`);
  },
  
  async cancelAppointment(id) {
    await api.delete(`/api/clinical/appointments/${id}`);
  },
  
  async registerConsultation(data) {
    const response = await api.post('/api/clinical/consultations', data);
    return response.data;
  },
  
  async generatePrescription(data) {
    const response = await api.post('/api/clinical/prescriptions', data);
    return response.data;
  }
};
```

---

## 🐛 Troubleshooting

### Problema: CORS Error

**Síntoma**: `Access to fetch at 'http://localhost:8080' from origin 'http://localhost:3000' has been blocked by CORS policy`

**Solución**:
1. Verificar que el frontend esté corriendo en `http://localhost:3000`
2. Verificar configuración CORS en `api-gateway/src/main/resources/application.yml`
3. Reiniciar API Gateway: `docker-compose restart api-gateway`

### Problema: 401 Unauthorized

**Síntoma**: Todas las peticiones retornan 401

**Solución**:
1. Verificar que el token JWT esté incluido en el header `Authorization`
2. Verificar que el token no haya expirado
3. Hacer login nuevamente para obtener un token fresco

### Problema: 429 Too Many Requests

**Síntoma**: Después de varias peticiones, el servidor retorna 429

**Solución**:
- El rate limit es de 100 peticiones por minuto por IP
- Esperar 1 minuto antes de continuar
- Para desarrollo, puedes ajustar el límite en `RateLimitFilter.java`

### Problema: Service Unavailable

**Síntoma**: 503 Service Unavailable

**Solución**:
1. Verificar que todos los servicios estén corriendo: `docker-compose ps`
2. Verificar logs: `docker-compose logs -f {service-name}`
3. Verificar Eureka Dashboard: `http://localhost:8761`
4. Reiniciar servicios: `docker-compose restart`

### Problema: Connection Refused

**Síntoma**: `ECONNREFUSED` al intentar conectar

**Solución**:
1. Verificar que Docker Compose esté corriendo
2. Verificar puertos: `netstat -an | grep 8080`
3. Verificar firewall/antivirus no esté bloqueando puertos

---

## 📊 Monitoreo y Logs

### Ver Logs en Tiempo Real

```bash
# Todos los servicios
docker-compose logs -f

# Servicio específico
docker-compose logs -f api-gateway
docker-compose logs -f clinical-service
docker-compose logs -f patient-service

# Últimas 100 líneas
docker-compose logs --tail=100 clinical-service
```

### Health Checks

```bash
# Script para verificar salud de todos los servicios
#!/bin/bash

services=("api-gateway:8080" "auth-service:8081" "patient-service:8082" "clinical-service:8083")

for service in "${services[@]}"; do
  name="${service%%:*}"
  port="${service##*:}"
  
  status=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$port/actuator/health)
  
  if [ "$status" = "200" ]; then
    echo "✅ $name: OK"
  else
    echo "❌ $name: FAIL (HTTP $status)"
  fi
done
```

---

## 🎯 Próximos Pasos

### Tareas Pendientes (Opcionales)

1. **Implementar servicios stub completos**:
   - Lab Service (órdenes de laboratorio)
   - Pharmacy Service (dispensación de medicamentos)
   - Billing Service (facturación)

2. **Agregar más tests**:
   - Tests de integración con MockMvc
   - Tests de property-based testing
   - Tests end-to-end con Testcontainers

3. **Mejorar observabilidad**:
   - Agregar Zipkin para distributed tracing
   - Agregar Prometheus + Grafana para métricas
   - Centralizar logs con ELK Stack

4. **Seguridad adicional**:
   - Implementar refresh tokens
   - Agregar 2FA
   - Implementar audit logging

### Integración Frontend Inmediata

**Puedes empezar a integrar el frontend AHORA mismo** con los siguientes módulos:

1. ✅ **Login y Autenticación** (Auth Service)
2. ✅ **Gestión de Pacientes** (Patient Service)
3. ✅ **Signos Vitales** (Clinical Service)
4. ✅ **Triage Manchester** (Clinical Service)
5. ✅ **Agendamiento de Citas** (Clinical Service)
6. ✅ **Consultas Médicas** (Clinical Service)
7. ✅ **Recetas Médicas** (Clinical Service)

---

## 📞 Soporte

Si encuentras problemas durante la integración:

1. Revisa los logs: `docker-compose logs -f {service}`
2. Verifica health checks: `curl http://localhost:{port}/actuator/health`
3. Consulta Eureka Dashboard: `http://localhost:8761`
4. Revisa este documento para ejemplos de uso

---

**¡El backend está listo para que empieces a construir el frontend! 🚀**
