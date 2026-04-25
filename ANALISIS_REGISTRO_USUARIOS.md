# 📊 Análisis: Estado Actual y Cambios Necesarios para Registro de Usuarios

## 🎯 Objetivo
Implementar dos flujos de registro diferenciados:
1. **Empleados** → Username personalizado + Rol específico
2. **Pacientes** → Username = DPI + Rol PATIENT + Registro en patient_schema.patients

---

## ✅ Estado Actual del Sistema

### **1. Endpoints Existentes**

#### **A) Registro de Empleados** ✅ YA EXISTE
- **Endpoint:** `POST /api/users/empleados`
- **Controlador:** `UserManagementController.createEmployee()`
- **Servicio:** `UserManagementService.createEmployee()`
- **DTO:** `CreateEmployeeRequest`
- **Funcionalidad:**
  - ✅ Crea usuario en `auth_schema.users`
  - ✅ Asigna rol específico (ADMIN, DOCTOR, ADMISSION, etc.)
  - ✅ Genera contraseña temporal
  - ✅ Envía email con credenciales
  - ✅ Requiere autenticación de ADMIN
  - ✅ Username se genera automáticamente (ej: "juan.perez")

**Request Actual:**
```json
{
  "firstName": "Juan",
  "secondName": "Carlos",
  "firstLastName": "Pérez",
  "secondLastName": "García",
  "email": "doctor@hospital.com",
  "phone": "12345678",
  "roleName": "DOCTOR"
}
```

**Response Actual:**
```json
{
  "empleado": {
    "id": "uuid",
    "username": "juan.perez",
    "email": "doctor@hospital.com",
    "firstName": "Juan",
    "roles": ["DOCTOR"],
    "active": true
  },
  "contrasenaTemporalParaEntregar": "Abc123xy",
  "mensaje": "Empleado registrado..."
}
```

---

#### **B) Registro de Pacientes (Interno)** ⚠️ PARCIALMENTE IMPLEMENTADO
- **Endpoint:** `POST /api/auth/internal/create-patient`
- **Controlador:** `AuthController.createPatientAccount()`
- **Servicio:** `AuthService.createPatientAccount()`
- **DTO:** `CreatePatientAccountRequest`
- **Funcionalidad:**
  - ✅ Crea usuario en `auth_schema.users`
  - ✅ Username = DPI
  - ✅ Asigna rol PATIENT
  - ✅ Genera contraseña temporal
  - ✅ Envía email con credenciales
  - ❌ **NO crea registro en `patient_schema.patients`**
  - ❌ **NO tiene validación de campos médicos obligatorios**

**Request Actual:**
```json
{
  "dpi": "1234567890123",
  "email": "juan@gmail.com",
  "firstName": "Juan",
  "secondName": "Carlos",
  "firstLastName": "Pérez",
  "secondLastName": "García",
  "phone": "12345678"
}
```

**Campos que FALTAN para patient_schema.patients:**
- ❌ `nit`
- ❌ `birthDate` (OBLIGATORIO)
- ❌ `gender` (OBLIGATORIO)
- ❌ `department`
- ❌ `municipality`
- ❌ `zone`
- ❌ `address`

---

#### **C) Registro Público de Pacientes** ✅ YA EXISTE (pero incompleto)
- **Endpoint:** `POST /api/auth/register`
- **Controlador:** `AuthController.register()`
- **Servicio:** `AuthService.register()`
- **DTO:** `RegisterRequest`
- **Funcionalidad:**
  - ✅ Crea usuario en `auth_schema.users`
  - ✅ Username = DPI
  - ✅ Asigna rol PATIENT
  - ✅ Activa cuenta automáticamente
  - ❌ **NO crea registro en `patient_schema.patients`**
  - ❌ **NO tiene campos médicos obligatorios**

---

#### **D) Login** ✅ YA FUNCIONA CORRECTAMENTE
- **Endpoint:** `POST /api/auth/login`
- **Funcionalidad:**
  - ✅ Busca por `username` O por `email`
  - ✅ Valida contraseña
  - ✅ Genera JWT
  - ✅ Retorna datos del usuario

**Request:**
```json
{
  "username": "1234567890123",  // Puede ser DPI, username o email
  "password": "SecurePass123"
}
```

---

## 🔴 Problemas Identificados

### **1. Registro de Pacientes Incompleto**
- ❌ No se crea registro en `patient_schema.patients`
- ❌ Faltan campos médicos obligatorios (birthDate, gender)
- ❌ No hay transacción entre auth-service y patient-service

### **2. Inconsistencia de Datos**
- ❌ Usuario existe en `auth_schema.users` pero no en `patient_schema.patients`
- ❌ Al obtener citas, falla porque no encuentra datos del paciente

### **3. Falta de Validación**
- ❌ No valida que DPI tenga 13 dígitos
- ❌ No valida formato de fecha de nacimiento
- ❌ No valida género (M/F)

---

## 🔧 Cambios Necesarios

### **Cambio 1: Actualizar `CreatePatientAccountRequest`**
**Archivo:** `backend-services/auth-service/src/main/java/com/medflow/auth/dto/CreatePatientAccountRequest.java`

**Agregar campos:**
```java
@NotBlank(message = "La fecha de nacimiento es requerida")
@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Formato de fecha inválido (YYYY-MM-DD)")
private String birthDate;

@NotBlank(message = "El género es requerido")
@Pattern(regexp = "M|F", message = "El género debe ser M o F")
private String gender;

private String nit;
private String department;
private String municipality;
private String zone;
private String address;
```

---

### **Cambio 2: Actualizar `RegisterRequest`**
**Archivo:** `backend-services/auth-service/src/main/java/com/medflow/auth/dto/RegisterRequest.java`

**Agregar los mismos campos médicos obligatorios**

---

### **Cambio 3: Crear Cliente Feign para Patient-Service**
**Archivo NUEVO:** `backend-services/auth-service/src/main/java/com/medflow/auth/client/PatientServiceClient.java`

```java
@FeignClient(name = "patient-service", path = "/api/patients")
public interface PatientServiceClient {
    
    @PostMapping
    PatientResponse createPatient(@RequestBody CreatePatientRequest request);
}
```

---

### **Cambio 4: Crear DTO para Patient-Service**
**Archivo NUEVO:** `backend-services/auth-service/src/main/java/com/medflow/auth/dto/CreatePatientRequest.java`

```java
@Data
public class CreatePatientRequest {
    private String id;  // UUID del usuario en auth
    private String dpi;
    private String nit;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String birthDate;
    private String gender;
    private String email;
    private String phone;
    private String department;
    private String municipality;
    private String zone;
    private String address;
    private String authUserId;  // Mismo que id
    private Boolean active;
}
```

---

### **Cambio 5: Actualizar `AuthService.createPatientAccount()`**
**Archivo:** `backend-services/auth-service/src/main/java/com/medflow/auth/service/AuthService.java`

**Agregar:**
1. Inyectar `PatientServiceClient`
2. Después de crear usuario en `auth_schema.users`, llamar a patient-service
3. Usar `@Transactional` para rollback si falla

```java
@Transactional
public CreatePatientAccountResponse createPatientAccount(CreatePatientAccountRequest request) {
    // 1. Validaciones existentes...
    
    // 2. Crear usuario en auth_schema.users
    User saved = userRepository.save(user);
    
    // 3. Crear paciente en patient_schema.patients
    try {
        CreatePatientRequest patientRequest = new CreatePatientRequest();
        patientRequest.setId(saved.getId().toString());
        patientRequest.setDpi(request.getDpi());
        patientRequest.setNit(request.getNit());
        patientRequest.setFirstName(request.getFirstName());
        patientRequest.setSecondName(request.getSecondName());
        patientRequest.setFirstLastName(request.getFirstLastName());
        patientRequest.setSecondLastName(request.getSecondLastName());
        patientRequest.setBirthDate(request.getBirthDate());
        patientRequest.setGender(request.getGender());
        patientRequest.setEmail(request.getEmail());
        patientRequest.setPhone(request.getPhone());
        patientRequest.setDepartment(request.getDepartment());
        patientRequest.setMunicipality(request.getMunicipality());
        patientRequest.setZone(request.getZone());
        patientRequest.setAddress(request.getAddress());
        patientRequest.setAuthUserId(saved.getId().toString());
        patientRequest.setActive(true);
        
        patientServiceClient.createPatient(patientRequest);
        
    } catch (Exception e) {
        log.error("Error creando paciente en patient-service", e);
        throw new RuntimeException("Error al crear registro de paciente");
    }
    
    // 4. Enviar email...
    // 5. Retornar response...
}
```

---

### **Cambio 6: Actualizar `AuthService.register()`**
**Archivo:** `backend-services/auth-service/src/main/java/com/medflow/auth/service/AuthService.java`

**Aplicar la misma lógica de transacción con patient-service**

---

### **Cambio 7: Crear Endpoint en Patient-Service**
**Archivo:** `backend-services/patient-service/src/main/java/com/medflow/patient/controller/PatientController.java`

**Agregar:**
```java
@PostMapping
public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody CreatePatientRequest request) {
    PatientResponse response = patientService.createPatient(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

### **Cambio 8: Agregar Dependencia Feign en Auth-Service**
**Archivo:** `backend-services/auth-service/pom.xml`

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

**Habilitar Feign en Application:**
```java
@EnableFeignClients
@SpringBootApplication
public class AuthServiceApplication {
    // ...
}
```

---

## 📋 Resumen de Archivos a Modificar

### **Auth-Service:**
1. ✏️ `CreatePatientAccountRequest.java` - Agregar campos médicos
2. ✏️ `RegisterRequest.java` - Agregar campos médicos
3. ✏️ `AuthService.java` - Agregar lógica de transacción con patient-service
4. ➕ `PatientServiceClient.java` - NUEVO (Feign Client)
5. ➕ `CreatePatientRequest.java` - NUEVO (DTO para patient-service)
6. ✏️ `AuthServiceApplication.java` - Agregar @EnableFeignClients
7. ✏️ `pom.xml` - Agregar dependencia Feign

### **Patient-Service:**
8. ✏️ `PatientController.java` - Agregar endpoint POST /api/patients
9. ✏️ `PatientService.java` - Implementar createPatient()
10. ➕ `CreatePatientRequest.java` - NUEVO (DTO)
11. ➕ `PatientResponse.java` - NUEVO (DTO)

---

## 🎯 Flujo Final Esperado

### **Registro de Empleado:**
```
Admin → POST /api/users/empleados
    ↓
UserManagementService.createEmployee()
    ↓
Crea en auth_schema.users
    ↓
Username = "juan.perez"
Rol = DOCTOR
    ↓
Retorna credenciales temporales
```

### **Registro de Paciente:**
```
Admin/Sistema → POST /api/auth/internal/create-patient
    ↓
AuthService.createPatientAccount()
    ↓
1. Crea en auth_schema.users
   Username = DPI
   Rol = PATIENT
    ↓
2. Llama a PatientServiceClient.createPatient()
    ↓
3. Crea en patient_schema.patients
   auth_user_id = user.id
    ↓
4. Si falla, rollback completo
    ↓
Retorna credenciales temporales
```

### **Login:**
```
Usuario → POST /api/auth/login
{
  "username": "1234567890123",  // DPI o email
  "password": "SecurePass123"
}
    ↓
AuthService.login()
    ↓
Busca por username O email
    ↓
Valida contraseña
    ↓
Genera JWT
    ↓
Retorna token + datos usuario
```

---

## ✅ Validaciones Necesarias

### **Para Empleados:**
- ✅ Email único
- ✅ Rol válido (ADMIN, DOCTOR, ADMISSION, etc.)
- ✅ Teléfono 8 dígitos
- ✅ Username generado automáticamente

### **Para Pacientes:**
- ✅ DPI único (13 dígitos)
- ✅ Email único
- ✅ Fecha de nacimiento válida (YYYY-MM-DD)
- ✅ Género válido (M/F)
- ✅ Teléfono 8 dígitos
- ✅ Username = DPI

---

## 🚀 Próximos Pasos

1. ¿Quieres que cree un **spec** para implementar estos cambios?
2. ¿O prefieres que implemente los cambios directamente?
3. ¿Necesitas alguna aclaración sobre el análisis?
