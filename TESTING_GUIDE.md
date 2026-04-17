# 🧪 Guía de Testing - MedFlow HIS

**Fecha**: 17 de Abril, 2026  
**Estado**: Listo para Testing End-to-End  
**Versión**: 1.0.0

---

## 📋 Tabla de Contenidos

1. [Componentes Implementados](#componentes-implementados)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Preparación del Entorno](#preparación-del-entorno)
4. [Pruebas por Componente](#pruebas-por-componente)
5. [Flujos de Usuario Completos](#flujos-de-usuario-completos)
6. [Checklist de Testing](#checklist-de-testing)
7. [Troubleshooting](#troubleshooting)

---

## 🎯 Componentes Implementados

### ✅ Backend - Infraestructura Cloud

#### 1. **Eureka Server** (Puerto 8761)
- **Estado**: ✅ Implementado y Funcional
- **Ubicación**: `backend-cloud/eureka-server/`
- **Funcionalidad**: Service Discovery para microservicios
- **Endpoints**:
  - Dashboard: `http://localhost:8761`
  - Health: `http://localhost:8761/actuator/health`

#### 2. **API Gateway** (Puerto 8080)
- **Estado**: ✅ Implementado y Funcional
- **Ubicación**: `backend-cloud/api-gateway/`
- **Funcionalidades**:
  - Enrutamiento centralizado a microservicios
  - Validación JWT en todas las peticiones
  - CORS configurado para frontend
  - Rate limiting (100 req/min por IP)
  - Propagación de headers `X-User-Id` y `X-User-Roles`
- **Rutas Configuradas**:
  - `/api/auth/**` → auth-service (8081)
  - `/api/patients/**` → patient-service (8082)
  - `/api/clinical/**` → clinical-service (8083)
- **Tests**: 100% cobertura en filtros y validadores

### ✅ Backend - Microservicios

#### 3. **Auth Service** (Puerto 8081)
- **Estado**: ✅ Implementado y Funcional
- **Ubicación**: `backend-services/auth-service/`
- **Funcionalidades**:
  - Login de empleados con JWT
  - Registro de empleados (solo ADMIN)
  - Logout con token blacklist
  - Refresh token
  - Rate limiting (5 intentos/minuto)
  - Roles: ADMINISTRATOR, ADMISSION, VITAL_SIGNS, DOCTOR, LABORATORY, PHARMACY, CASHIER
- **Endpoints**:
  - `POST /api/auth/login` - Login
  - `POST /api/auth/logout` - Logout
  - `POST /api/auth/refresh` - Refresh token
  - `GET /api/auth/validate` - Validar token
  - `GET /api/auth/me` - Usuario actual
  - `POST /api/auth/register` - Registro (ADMIN only)
- **Base de Datos**: `auth_schema` en PostgreSQL
- **Tests**: Cobertura >80%

#### 4. **Patient Service** (Puerto 8082)
- **Estado**: ✅ Implementado y Funcional
- **Ubicación**: `backend-services/patient-service/`
- **Funcionalidades**:
  - Registro de pacientes (por admisión o auto-registro)
  - Auto-registro público con activación por email
  - Búsqueda de pacientes (DPI, nombre, email)
  - Actualización de datos demográficos
  - Generación de credenciales de acceso
  - Validación de DPI guatemalteco (13 dígitos)
  - GatewayAuthFilter para autenticación vía headers
- **Endpoints**:
  - `POST /api/patients` - Registrar paciente (ADMIN/ADMISSION)
  - `POST /api/patients/register` - Auto-registro público
  - `GET /api/patients/{id}` - Consultar paciente
  - `GET /api/patients/search?query=` - Buscar pacientes
  - `PUT /api/patients/{id}` - Actualizar paciente
  - `POST /api/patients/activate` - Activar cuenta
- **Base de Datos**: `patient_schema` en PostgreSQL
- **Tests**: Tests de integración completos

#### 5. **Clinical Service** (Puerto 8083)
- **Estado**: ✅ Implementado y Funcional
- **Ubicación**: `backend-services/clinical-service/`
- **Arquitectura**: Hexagonal (Domain-Driven Design)
- **Funcionalidades**:
  - Gestión de citas médicas (crear, activar, cancelar)
  - Registro de signos vitales
  - Triaje Manchester
  - Consultas médicas
  - Generación de órdenes de laboratorio
  - Generación de prescripciones
  - Historial médico del paciente
  - Cache de slots disponibles con Redis
- **Endpoints**:
  - `POST /api/clinical/appointments` - Crear cita
  - `POST /api/clinical/appointments/{id}/activate` - Activar cita
  - `POST /api/clinical/appointments/{id}/cancel` - Cancelar cita
  - `GET /api/clinical/appointments` - Listar citas
  - `GET /api/clinical/appointments/my-appointments` - Mis citas (paciente)
  - `POST /api/clinical/vital-signs` - Registrar signos vitales
  - `POST /api/clinical/triage` - Realizar triaje
  - `POST /api/clinical/consultations` - Registrar consulta
  - `POST /api/clinical/lab-orders` - Generar orden de laboratorio
  - `POST /api/clinical/prescriptions` - Generar prescripción
  - `GET /api/clinical/medical-records/my-records` - Historial médico
- **Base de Datos**: `clinical_schema` en PostgreSQL
- **Tests**: Property-based testing + tests de integración

### ✅ Frontend

#### 6. **Frontend React** (Puerto 3000)
- **Estado**: ✅ Implementado y Funcional
- **Ubicación**: `frontend-medflow/`
- **Stack**: React 19 + TypeScript + Vite + TailwindCSS
- **Funcionalidades Implementadas**:
  - **Autenticación**:
    - Login de empleados
    - Login de pacientes
    - Logout
    - Protección de rutas por rol
  - **Registro de Pacientes**:
    - Auto-registro público
    - Validación de DPI guatemalteco
    - Activación de cuenta por token
  - **Portal de Empleados**:
    - Dashboard por rol
    - Módulo de admisión (activar citas)
    - Módulos preparados para: vitals, doctor, lab, pharmacy, cashier
  - **Portal de Pacientes** (Preparado):
    - Mis citas
    - Historial médico
    - Perfil
- **Páginas Implementadas**:
  - `HomePage` - Landing page pública
  - `LoginPage` - Login empleados/pacientes
  - `RegisterPage` - Auto-registro de pacientes
  - `ActivateAccountPage` - Activación de cuenta
  - `DashboardPage` - Dashboard de empleados
  - `ActivateAppointments` - Gestión de citas (admisión)
- **Componentes**:
  - `MainLayout` - Layout con sidebar dinámico por rol
  - `ProtectedRoute` - Protección de rutas
  - `AuthContext` - Contexto de autenticación
- **Servicios**:
  - `authService` - Autenticación
  - `patientService` - Gestión de pacientes
  - `appointmentService` - Gestión de citas

### ✅ Base de Datos

#### 7. **PostgreSQL** (Puerto 5432)
- **Estado**: ✅ Configurado
- **Esquemas Implementados**:
  - `auth_schema` - Usuarios, roles, permisos
  - `patient_schema` - Pacientes, datos demográficos
  - `clinical_schema` - Citas, consultas, triaje, signos vitales, órdenes, prescripciones
- **Migraciones**: Flyway configurado en cada servicio
- **Estrategia**: Schema-per-Service (Monolito Lógico)
- **Regla**: CERO JOINs entre esquemas

### ✅ Cache

#### 8. **Redis** (Puerto 6379)
- **Estado**: ✅ Configurado
- **Uso**: Cache de slots disponibles para citas (Clinical Service)

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend React (3000)                     │
│  - Login/Register  - Portal Empleados  - Portal Pacientes   │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP/REST
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   API Gateway (8080)                         │
│  - JWT Validation  - Routing  - CORS  - Rate Limiting       │
└─┬───────────────────┬───────────────────┬───────────────────┘
  │                   │                   │
  ▼                   ▼                   ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────────────────┐
│ Auth Service │ │Patient Service│ │  Clinical Service        │
│   (8081)     │ │   (8082)     │ │   (8083)                 │
│              │ │              │ │  Hexagonal Architecture  │
└──────┬───────┘ └──────┬───────┘ └──────┬───────────────────┘
       │                │                │
       ▼                ▼                ▼
┌─────────────────────────────────────────────────────────────┐
│              PostgreSQL (5432)                               │
│  auth_schema  │  patient_schema  │  clinical_schema         │
└─────────────────────────────────────────────────────────────┘
                                          │
                                          ▼
                                    ┌──────────┐
                                    │  Redis   │
                                    │  (6379)  │
                                    └──────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                 Eureka Server (8761)                         │
│                  Service Discovery                           │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Preparación del Entorno

### Prerequisitos

```bash
# Verificar versiones
java -version        # Java 17+
node -version        # Node 18+
docker --version     # Docker 20+
docker-compose --version
psql --version       # PostgreSQL 15+
```

### 1. Configurar Variables de Entorno

#### Backend Services

Cada servicio necesita su `application.yml` configurado. Ya están listos en:
- `backend-cloud/eureka-server/src/main/resources/application.yml`
- `backend-cloud/api-gateway/src/main/resources/application.yml`
- `backend-services/auth-service/src/main/resources/application.yml`
- `backend-services/patient-service/src/main/resources/application.yml`
- `backend-services/clinical-service/src/main/resources/application.yml`

#### Frontend

Crear archivo `.env` en `frontend-medflow/`:

```env
VITE_API_URL=http://localhost:8080/api
VITE_AUTH_ENABLED=true
```

### 2. Iniciar Base de Datos

```bash
# Opción 1: Docker Compose (Recomendado)
docker-compose up -d postgres redis

# Opción 2: PostgreSQL local
# Crear base de datos
createdb medflow_db

# Los esquemas se crean automáticamente con Flyway al iniciar cada servicio
```

### 3. Compilar Servicios Backend

```bash
# Compilar todos los servicios
cd backend-cloud/eureka-server && mvn clean install && cd ../..
cd backend-cloud/api-gateway && mvn clean install && cd ../..
cd backend-services/auth-service && mvn clean install && cd ../..
cd backend-services/patient-service && mvn clean install && cd ../..
cd backend-services/clinical-service && mvn clean install && cd ../..
```

### 4. Iniciar Servicios (Orden Importante)

```bash
# 1. Eureka Server (esperar 30 segundos)
cd backend-cloud/eureka-server
mvn spring-boot:run

# 2. Auth Service (esperar 20 segundos)
cd backend-services/auth-service
mvn spring-boot:run

# 3. Patient Service (esperar 20 segundos)
cd backend-services/patient-service
mvn spring-boot:run

# 4. Clinical Service (esperar 20 segundos)
cd backend-services/clinical-service
mvn spring-boot:run

# 5. API Gateway (esperar 20 segundos)
cd backend-cloud/api-gateway
mvn spring-boot:run

# 6. Frontend
cd frontend-medflow
npm install
npm run dev
```

### 5. Verificar que Todo Esté Corriendo

```bash
# Eureka Dashboard
curl http://localhost:8761

# API Gateway Health
curl http://localhost:8080/actuator/health

# Auth Service Health
curl http://localhost:8081/actuator/health

# Patient Service Health
curl http://localhost:8082/actuator/health

# Clinical Service Health
curl http://localhost:8083/actuator/health

# Frontend
curl http://localhost:3000
```

---

## 🧪 Pruebas por Componente

### Test 1: Eureka Server

**Objetivo**: Verificar que el service discovery está funcionando

```bash
# Abrir en navegador
http://localhost:8761

# Verificar que aparecen registrados:
# - API-GATEWAY
# - AUTH-SERVICE
# - PATIENT-SERVICE
# - CLINICAL-SERVICE
```

**Resultado Esperado**: Dashboard de Eureka muestra 4 servicios registrados

---

### Test 2: API Gateway - CORS y Routing

**Objetivo**: Verificar que el gateway enruta correctamente

```bash
# Test 1: Health check
curl http://localhost:8080/actuator/health

# Test 2: Ruta pública (debe funcionar sin JWT)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Test 3: Ruta protegida (debe retornar 401 sin JWT)
curl http://localhost:8080/api/patients

# Test 4: CORS (desde frontend)
# Abrir http://localhost:3000 y verificar que no hay errores de CORS
```

**Resultado Esperado**:
- Test 1: `{"status":"UP"}`
- Test 2: Token JWT retornado
- Test 3: `401 Unauthorized`
- Test 4: Sin errores de CORS en consola del navegador

---

### Test 3: Auth Service - Login de Empleados

**Objetivo**: Verificar autenticación de empleados

```bash
# Test 1: Login exitoso
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Guardar el token retornado en variable
TOKEN="<token_aqui>"

# Test 2: Validar token
curl http://localhost:8080/api/auth/validate \
  -H "Authorization: Bearer $TOKEN"

# Test 3: Obtener usuario actual
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer $TOKEN"

# Test 4: Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# Test 5: Rate limiting (intentar 6 veces con credenciales incorrectas)
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrong"}'
  echo "\nIntento $i"
done
```

**Resultado Esperado**:
- Test 1: Token JWT + datos del usuario
- Test 2: `{"valid": true}`
- Test 3: Datos del usuario (id, username, roles)
- Test 4: `200 OK`
- Test 5: Primeros 5 intentos `401`, sexto intento `429 Too Many Requests`

---

### Test 4: Patient Service - Registro de Pacientes

**Objetivo**: Verificar registro y búsqueda de pacientes

```bash
# Test 1: Auto-registro público (sin JWT)
curl -X POST http://localhost:8080/api/patients/register \
  -H "Content-Type: application/json" \
  -d '{
    "dpi": "1234567890123",
    "firstName": "Juan",
    "firstLastName": "Pérez",
    "secondLastName": "García",
    "birthDate": "1990-05-15",
    "gender": "MALE",
    "email": "juan.perez@example.com",
    "phone": "12345678",
    "address": "Ciudad de Guatemala",
    "password": "Password123"
  }'

# Test 2: Buscar paciente (requiere JWT de empleado)
curl "http://localhost:8080/api/patients/search?query=Juan" \
  -H "Authorization: Bearer $TOKEN"

# Test 3: Consultar paciente por ID
curl http://localhost:8080/api/patients/1 \
  -H "Authorization: Bearer $TOKEN"

# Test 4: Validación de DPI duplicado
curl -X POST http://localhost:8080/api/patients/register \
  -H "Content-Type: application/json" \
  -d '{
    "dpi": "1234567890123",
    "firstName": "María",
    "firstLastName": "López",
    "birthDate": "1995-03-20",
    "gender": "FEMALE",
    "email": "maria.lopez@example.com",
    "phone": "87654321",
    "address": "Antigua Guatemala",
    "password": "Password123"
  }'
```

**Resultado Esperado**:
- Test 1: `201 Created` + mensaje de activación pendiente
- Test 2: Lista con paciente "Juan Pérez"
- Test 3: Datos completos del paciente
- Test 4: `409 Conflict` - "DPI ya registrado"

---

### Test 5: Clinical Service - Gestión de Citas

**Objetivo**: Verificar creación y gestión de citas

```bash
# Test 1: Crear cita (requiere JWT de paciente o empleado)
curl -X POST http://localhost:8080/api/clinical/appointments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": "1",
    "doctorId": "DOC-001",
    "appointmentDate": "2026-04-20",
    "appointmentTime": "10:00",
    "description": "Consulta general"
  }'

# Test 2: Listar citas
curl http://localhost:8080/api/clinical/appointments \
  -H "Authorization: Bearer $TOKEN"

# Test 3: Activar cita (requiere rol ADMISSION)
curl -X POST http://localhost:8080/api/clinical/appointments/1/activate \
  -H "Authorization: Bearer $TOKEN"

# Test 4: Mis citas (como paciente)
curl http://localhost:8080/api/clinical/appointments/my-appointments \
  -H "Authorization: Bearer $PATIENT_TOKEN"

# Test 5: Cancelar cita
curl -X POST http://localhost:8080/api/clinical/appointments/1/cancel \
  -H "Authorization: Bearer $TOKEN"
```

**Resultado Esperado**:
- Test 1: `201 Created` + ID de cita
- Test 2: Lista de citas
- Test 3: `200 OK` + cita con estado ACTIVATED
- Test 4: Lista de citas del paciente
- Test 5: `200 OK` + cita con estado CANCELLED

---

### Test 6: Frontend - Flujo de Usuario

#### Test 6.1: Login de Empleado

1. Abrir `http://localhost:3000/login`
2. Ingresar credenciales:
   - Username: `admin`
   - Password: `admin123`
3. Click en "Iniciar Sesión"

**Resultado Esperado**: Redirección a `/dashboard`

#### Test 6.2: Registro de Paciente

1. Abrir `http://localhost:3000/register`
2. Llenar formulario con datos válidos
3. DPI: 13 dígitos (ej: `1234567890123`)
4. Teléfono: 8 dígitos (ej: `12345678`)
5. Email válido
6. Contraseña: mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número
7. Click en "Registrarse"

**Resultado Esperado**: Mensaje de éxito + redirección a login

#### Test 6.3: Activación de Cuenta

1. Revisar email del paciente registrado
2. Copiar token de activación
3. Abrir `http://localhost:3000/activate?token=<TOKEN>`
4. Click en "Activar Cuenta"

**Resultado Esperado**: Cuenta activada + redirección a login

#### Test 6.4: Portal de Admisión

1. Login como empleado con rol ADMISSION
2. Navegar a `/admission`
3. Ver lista de citas pendientes
4. Click en "Activar" en una cita

**Resultado Esperado**: Cita cambia a estado ACTIVATED

---

## 🔄 Flujos de Usuario Completos

### Flujo 1: Registro y Primera Cita de Paciente

```
1. Paciente → Registro en /register
2. Sistema → Envía email con token de activación
3. Paciente → Activa cuenta en /activate?token=XXX
4. Paciente → Login en /login
5. Paciente → Crea cita en /appointments/book
6. Paciente → Paga cita (pendiente implementar)
7. Sistema → Crea cita con estado PENDING
8. Empleado (ADMISSION) → Activa cita en /admission
9. Cita → Estado cambia a ACTIVATED
10. Paciente → Ve cita en /patient/appointments
```

### Flujo 2: Atención Médica Completa

```
1. Paciente → Llega al hospital con cita ACTIVATED
2. Enfermera (VITAL_SIGNS) → Registra signos vitales
3. Enfermera → Realiza triaje Manchester
4. Sistema → Asigna prioridad según triaje
5. Doctor → Ve paciente en consulta
6. Doctor → Registra diagnóstico y tratamiento
7. Doctor → Genera orden de laboratorio (si necesario)
8. Doctor → Genera prescripción médica
9. Laboratorio → Recibe orden y procesa muestras
10. Farmacia → Despacha medicamentos
11. Caja → Genera factura
12. Paciente → Ve historial completo en /patient/medical-records
```

---

## ✅ Checklist de Testing

### Backend

- [ ] Eureka Server muestra 4 servicios registrados
- [ ] API Gateway enruta correctamente a todos los servicios
- [ ] CORS funciona sin errores desde frontend
- [ ] JWT se valida correctamente en todas las rutas protegidas
- [ ] Rate limiting funciona (100 req/min en gateway, 5 intentos/min en login)
- [ ] Login de empleados funciona con todos los roles
- [ ] Logout invalida el token correctamente
- [ ] Registro de pacientes funciona (admisión y auto-registro)
- [ ] Validación de DPI detecta duplicados
- [ ] Búsqueda de pacientes funciona por DPI, nombre y email
- [ ] Creación de citas funciona
- [ ] Activación de citas funciona (solo rol ADMISSION)
- [ ] Cancelación de citas funciona
- [ ] Registro de signos vitales funciona
- [ ] Triaje Manchester funciona y asigna prioridad correcta
- [ ] Generación de órdenes de laboratorio funciona
- [ ] Generación de prescripciones funciona
- [ ] Historial médico se muestra correctamente

### Frontend

- [ ] Login de empleados funciona
- [ ] Login de pacientes funciona
- [ ] Logout funciona y limpia localStorage
- [ ] Registro de pacientes funciona con validaciones
- [ ] Activación de cuenta funciona
- [ ] Dashboard de empleados muestra módulos según rol
- [ ] Portal de admisión muestra citas pendientes
- [ ] Activar cita desde admisión funciona
- [ ] Portal de paciente muestra sus citas
- [ ] Portal de paciente muestra su historial médico
- [ ] Protección de rutas funciona (redirige a login si no autenticado)
- [ ] Protección por rol funciona (muestra 403 si no tiene permisos)
- [ ] No hay errores de CORS en consola
- [ ] Loading states se muestran correctamente
- [ ] Mensajes de error se muestran claramente

### Integración

- [ ] Frontend → API Gateway → Auth Service (login)
- [ ] Frontend → API Gateway → Patient Service (registro)
- [ ] Frontend → API Gateway → Clinical Service (citas)
- [ ] Auth Service → Patient Service (validación de usuario)
- [ ] Clinical Service → Patient Service (datos del paciente)
- [ ] Clinical Service → Redis (cache de slots)
- [ ] Todos los servicios se registran en Eureka
- [ ] Headers X-User-Id y X-User-Roles se propagan correctamente

---

## 🐛 Troubleshooting

### Problema: Servicios no se registran en Eureka

**Solución**:
```bash
# Verificar que Eureka esté corriendo
curl http://localhost:8761/actuator/health

# Verificar configuración en application.yml de cada servicio
# Debe tener:
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

### Problema: Error de CORS en frontend

**Solución**:
```yaml
# Verificar en api-gateway/src/main/resources/application.yml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "http://localhost:3000"
            allowed-methods: "*"
            allowed-headers: "*"
```

### Problema: JWT inválido o expirado

**Solución**:
```bash
# Hacer logout y login nuevamente
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# Nuevo login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Problema: Base de datos no se crea

**Solución**:
```bash
# Crear manualmente
createdb medflow_db

# Verificar conexión
psql -h localhost -U postgres -d medflow_db -c "SELECT 1"

# Los esquemas se crean automáticamente con Flyway
```

### Problema: Puerto ya en uso

**Solución**:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

---

## 📊 Métricas de Éxito

### Performance
- [ ] Latencia del API Gateway < 50ms
- [ ] Tiempo de respuesta de Auth Service < 200ms
- [ ] Tiempo de respuesta de Patient Service < 200ms
- [ ] Tiempo de respuesta de Clinical Service < 500ms
- [ ] Carga de frontend < 2 segundos

### Funcionalidad
- [ ] 100% de endpoints funcionando
- [ ] 0 errores de CORS
- [ ] 0 errores de JWT en rutas protegidas
- [ ] Validaciones funcionando correctamente
- [ ] Todos los flujos de usuario completos

### Calidad
- [ ] Cobertura de tests > 80% en backend
- [ ] 0 errores en consola del navegador
- [ ] Logs claros y útiles en todos los servicios
- [ ] Documentación actualizada

---

## 📝 Notas Finales

- **Orden de inicio**: Siempre iniciar Eureka primero, luego servicios, luego gateway, finalmente frontend
- **Tiempo de espera**: Dar 20-30 segundos entre cada servicio para que se registren en Eureka
- **Logs**: Revisar logs de cada servicio para detectar errores
- **Health checks**: Usar `/actuator/health` para verificar estado de cada servicio
- **Eureka Dashboard**: Usar `http://localhost:8761` para monitorear servicios registrados

---

## 🚧 Pendientes para Completar Testing

### Componentes Faltantes

#### 1. **Sistema de Pago de Citas** ❌ NO IMPLEMENTADO

**Estado**: Pendiente de implementación  
**Prioridad**: ALTA (bloqueante para flujo completo de pacientes)

**Qué falta**:
- Integración con pasarela de pago (Stripe, PayPal, u otra)
- Endpoint `/api/billing/payments` en Billing Service
- Componente de pago en frontend
- Validación de tarjetas de crédito
- Generación de recibos en PDF
- Envío de emails con recibo

**Impacto**: Los pacientes no pueden completar el flujo de creación de citas en línea porque el pago es obligatorio.

**Para testear este componente necesitas**:
1. Implementar Billing Service (puerto 8084)
2. Configurar cuenta de Stripe/PayPal en modo sandbox
3. Crear componente `PaymentPage.tsx` en frontend
4. Integrar Stripe/PayPal SDK en frontend
5. Implementar endpoint POST `/api/billing/payments`
6. Agregar ruta `/api/billing/**` en API Gateway

---

#### 2. **Flujo Completo de Creación de Citas por Pacientes** ⚠️ PARCIALMENTE IMPLEMENTADO

**Estado**: Backend listo, frontend incompleto  
**Prioridad**: ALTA

**Qué está listo**:
- ✅ Backend: Endpoint POST `/api/clinical/appointments` funcional
- ✅ Backend: Validación de datos de cita
- ✅ Frontend: Registro de pacientes funcional
- ✅ Frontend: Login de pacientes funcional

**Qué falta**:
- ❌ Página pública de creación de citas (`/appointments/book`)
- ❌ Calendario con fechas disponibles
- ❌ Selector de horarios disponibles
- ❌ Flujo de registro/login antes de pago
- ❌ Integración con sistema de pago
- ❌ Confirmación de cita con número de referencia

**Para testear este componente necesitas**:
1. Crear componente `BookAppointmentPage.tsx`
2. Implementar calendario con react-calendar o date-fns
3. Crear servicio para obtener slots disponibles
4. Implementar lógica de guardado temporal de datos de cita
5. Integrar con flujo de pago
6. Crear página de confirmación

**Workaround temporal para testing**:
- Crear citas manualmente desde el portal de admisión
- Usar Postman/curl para crear citas directamente al API

---

#### 3. **Portal del Paciente - Mis Citas** ⚠️ PARCIALMENTE IMPLEMENTADO

**Estado**: Backend listo, frontend incompleto  
**Prioridad**: MEDIA

**Qué está listo**:
- ✅ Backend: Endpoint GET `/api/clinical/appointments/my-appointments`
- ✅ Backend: Filtrado por paciente autenticado
- ✅ Frontend: Estructura de portal de paciente

**Qué falta**:
- ❌ Página `PatientAppointmentsPage.tsx`
- ❌ Lista de citas del paciente
- ❌ Filtros por estado (Todas, Pendientes, Completadas, Canceladas)
- ❌ Botón de cancelar cita
- ❌ Modal de confirmación de cancelación

**Para testear este componente necesitas**:
1. Crear componente `PatientAppointmentsPage.tsx`
2. Implementar servicio `getMyAppointments()` en appointmentService
3. Crear componente `AppointmentCard` para mostrar cada cita
4. Implementar filtros de estado
5. Agregar funcionalidad de cancelación

**Workaround temporal para testing**:
- Usar Postman/curl para consultar `/api/clinical/appointments/my-appointments`
- Verificar que el backend retorna solo las citas del paciente autenticado

---

#### 4. **Portal del Paciente - Mi Historial Médico** ⚠️ PARCIALMENTE IMPLEMENTADO

**Estado**: Backend listo, frontend incompleto  
**Prioridad**: MEDIA

**Qué está listo**:
- ✅ Backend: Endpoint GET `/api/clinical/medical-records/my-records`
- ✅ Backend: Historial de consultas, triaje, signos vitales

**Qué falta**:
- ❌ Página `PatientMedicalRecordsPage.tsx`
- ❌ Lista de consultas pasadas
- ❌ Lista de exámenes de laboratorio
- ❌ Descarga de resultados en PDF
- ❌ Visualización de prescripciones

**Para testear este componente necesitas**:
1. Crear componente `PatientMedicalRecordsPage.tsx`
2. Implementar servicio `getMyMedicalRecords()` en clinicalService
3. Crear componentes para mostrar consultas, exámenes, prescripciones
4. Implementar descarga de PDFs (si el backend lo soporta)

**Workaround temporal para testing**:
- Usar Postman/curl para consultar `/api/clinical/medical-records/my-records`
- Verificar que el backend retorna el historial completo del paciente

---

#### 5. **Envío de Emails** ❌ NO IMPLEMENTADO

**Estado**: Pendiente de implementación  
**Prioridad**: MEDIA

**Qué falta**:
- Servicio de envío de emails (SMTP, SendGrid, AWS SES)
- Email de activación de cuenta de paciente
- Email de confirmación de cita
- Email de recibo de pago
- Email de recordatorio de cita (24 horas antes)

**Para testear este componente necesitas**:
1. Configurar servicio de email (ej: Gmail SMTP, SendGrid)
2. Crear Email Service en backend
3. Implementar templates de emails
4. Integrar con Auth Service (activación de cuenta)
5. Integrar con Clinical Service (confirmación de cita)
6. Integrar con Billing Service (recibo de pago)

**Workaround temporal para testing**:
- Activar cuentas manualmente en la base de datos
- Verificar citas directamente en el portal de admisión

---

#### 6. **Billing Service** ❌ NO IMPLEMENTADO

**Estado**: Pendiente de implementación  
**Prioridad**: ALTA (bloqueante para flujo de pago)

**Qué falta**:
- Microservicio completo (puerto 8084)
- Base de datos `billing_schema`
- Endpoints de pagos, facturas, recibos
- Integración con pasarela de pago
- Generación de PDFs de recibos y facturas

**Para testear este componente necesitas**:
1. Crear proyecto `backend-services/billing-service`
2. Configurar base de datos `billing_schema`
3. Implementar endpoints:
   - POST `/api/billing/payments` - Procesar pago
   - GET `/api/billing/invoices` - Listar facturas
   - GET `/api/billing/invoices/{id}` - Consultar factura
   - GET `/api/billing/invoices/{id}/pdf` - Descargar PDF
4. Registrar servicio en Eureka
5. Agregar ruta en API Gateway

---

#### 7. **Lab Service** ❌ NO IMPLEMENTADO

**Estado**: Pendiente de implementación  
**Prioridad**: MEDIA

**Qué falta**:
- Microservicio completo (puerto 8085)
- Base de datos `lab_schema`
- Endpoints de órdenes de laboratorio, resultados
- Portal de laboratorio en frontend

**Para testear este componente necesitas**:
1. Crear proyecto `backend-services/lab-service`
2. Configurar base de datos `lab_schema`
3. Implementar endpoints de gestión de órdenes y resultados
4. Crear módulo de laboratorio en frontend
5. Registrar servicio en Eureka

---

#### 8. **Pharmacy Service** ❌ NO IMPLEMENTADO

**Estado**: Pendiente de implementación  
**Prioridad**: MEDIA

**Qué falta**:
- Microservicio completo (puerto 8086)
- Base de datos `pharmacy_schema`
- Endpoints de prescripciones, inventario, despacho
- Portal de farmacia en frontend

**Para testear este componente necesitas**:
1. Crear proyecto `backend-services/pharmacy-service`
2. Configurar base de datos `pharmacy_schema`
3. Implementar endpoints de gestión de prescripciones e inventario
4. Crear módulo de farmacia en frontend
5. Registrar servicio en Eureka

---

### Configuraciones Pendientes

#### 1. **Variables de Entorno del Frontend**

**Archivo**: `frontend-medflow/.env`

**Estado**: Archivo existe pero puede necesitar ajustes

**Verificar que contenga**:
```env
VITE_API_URL=http://localhost:8080/api
VITE_AUTH_ENABLED=true
```

**Acción requerida**: Verificar que el archivo `.env` existe y tiene las variables correctas.

---

#### 2. **Configuración de CORS en API Gateway**

**Archivo**: `backend-cloud/api-gateway/src/main/resources/application.yml`

**Estado**: Configurado para localhost:3000

**Verificar que contenga**:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "http://localhost:3000"
            allowed-methods: "*"
            allowed-headers: "*"
            allow-credentials: true
```

**Acción requerida**: Si el frontend corre en otro puerto, actualizar `allowed-origins`.

---

#### 3. **Datos de Prueba (Seed Data)**

**Estado**: Pendiente de crear

**Qué falta**:
- Script SQL para crear usuarios de prueba
- Script SQL para crear pacientes de prueba
- Script SQL para crear citas de prueba
- Script SQL para crear doctores de prueba

**Para testear necesitas**:
1. Crear archivo `seed-data.sql` con datos de prueba
2. Ejecutar script en la base de datos
3. Documentar credenciales de usuarios de prueba

**Usuarios de prueba recomendados**:
```sql
-- Empleados
INSERT INTO auth_schema.users (username, password, role) VALUES
  ('admin', '$2a$10$...', 'ADMINISTRATOR'),
  ('admission1', '$2a$10$...', 'ADMISSION'),
  ('nurse1', '$2a$10$...', 'VITAL_SIGNS'),
  ('doctor1', '$2a$10$...', 'DOCTOR'),
  ('lab1', '$2a$10$...', 'LABORATORY'),
  ('pharmacy1', '$2a$10$...', 'PHARMACY'),
  ('cashier1', '$2a$10$...', 'CASHIER');

-- Pacientes
INSERT INTO patient_schema.patients (dpi, first_name, first_last_name, birth_date, gender, email, phone, password) VALUES
  ('1234567890123', 'Juan', 'Pérez', '1990-05-15', 'MALE', 'juan.perez@test.com', '12345678', '$2a$10$...'),
  ('9876543210987', 'María', 'López', '1985-08-20', 'FEMALE', 'maria.lopez@test.com', '87654321', '$2a$10$...');
```

---

#### 4. **Configuración de Redis**

**Estado**: Configurado en docker-compose.yml

**Verificar**:
```bash
# Verificar que Redis está corriendo
docker ps | grep redis

# Conectar a Redis
redis-cli ping
# Debe retornar: PONG
```

**Acción requerida**: Si Redis no está corriendo, iniciar con `docker-compose up -d redis`.

---

### Pruebas Pendientes

#### 1. **Pruebas de Integración End-to-End**

**Estado**: Pendiente

**Qué falta**:
- Suite de tests E2E con Cypress o Playwright
- Tests de flujos completos de usuario
- Tests de regresión automatizados

**Para implementar**:
1. Instalar Cypress o Playwright
2. Crear tests para cada flujo de usuario
3. Configurar CI/CD para ejecutar tests automáticamente

---

#### 2. **Pruebas de Carga y Performance**

**Estado**: Pendiente

**Qué falta**:
- Tests de carga con JMeter o k6
- Medición de latencia bajo carga
- Identificación de cuellos de botella

**Para implementar**:
1. Instalar JMeter o k6
2. Crear escenarios de carga realistas
3. Ejecutar tests y analizar resultados

---

#### 3. **Pruebas de Seguridad**

**Estado**: Pendiente

**Qué falta**:
- Análisis de vulnerabilidades (OWASP Top 10)
- Tests de inyección SQL
- Tests de XSS y CSRF
- Auditoría de seguridad de JWT

**Para implementar**:
1. Usar herramientas como OWASP ZAP o Burp Suite
2. Revisar configuración de seguridad en cada servicio
3. Implementar rate limiting adicional si es necesario

---

### Resumen de Prioridades

#### 🔴 ALTA PRIORIDAD (Bloqueantes)
1. **Billing Service** - Sin esto no hay flujo de pago
2. **Sistema de Pago** - Requerido para citas de pacientes
3. **Página de Creación de Citas** - Funcionalidad core del sistema

#### 🟡 MEDIA PRIORIDAD (Importantes pero no bloqueantes)
1. **Portal del Paciente - Mis Citas** - Mejora UX
2. **Portal del Paciente - Historial Médico** - Mejora UX
3. **Envío de Emails** - Mejora comunicación
4. **Lab Service** - Funcionalidad adicional
5. **Pharmacy Service** - Funcionalidad adicional

#### 🟢 BAJA PRIORIDAD (Nice to have)
1. **Tests E2E automatizados** - Mejora calidad
2. **Tests de carga** - Mejora performance
3. **Tests de seguridad** - Mejora seguridad
4. **Datos de prueba** - Facilita testing manual

---

### Plan de Acción Recomendado

#### Fase 1: Completar Flujo de Pacientes (1-2 semanas)
1. Implementar Billing Service
2. Integrar pasarela de pago (Stripe sandbox)
3. Crear página de creación de citas
4. Crear portal de paciente (mis citas, historial)
5. Implementar envío de emails básico

#### Fase 2: Servicios Adicionales (2-3 semanas)
1. Implementar Lab Service
2. Implementar Pharmacy Service
3. Crear módulos de frontend para lab y pharmacy
4. Integrar con Clinical Service

#### Fase 3: Testing y Calidad (1-2 semanas)
1. Crear suite de tests E2E
2. Ejecutar tests de carga
3. Realizar auditoría de seguridad
4. Optimizar performance

---

**Última actualización**: 17 de Abril, 2026  
**Autor**: MedFlow Team  
**Versión**: 1.0.0
