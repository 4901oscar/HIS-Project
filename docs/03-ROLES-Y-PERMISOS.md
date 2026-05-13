# Roles y Permisos — MedFlow HIS

El sistema usa RBAC (Role-Based Access Control). El rol está incluido en el JWT y el API Gateway lo valida antes de enrutar. El frontend también usa el rol para mostrar solo las rutas y acciones que corresponden a cada usuario.

---

## Los 8 roles del sistema

### ADMIN — Administrador
El superusuario. Tiene acceso a todo el sistema.

**Qué puede hacer:**
- Crear y gestionar cuentas de empleados (todos los roles)
- Gestionar el catálogo de doctores y clínicas
- Gestionar el catálogo de medicamentos
- Gestionar el catálogo de exámenes de laboratorio
- Gestionar el catálogo de servicios y precios
- Gestionar el catálogo de triaje Manchester (motivos y discriminadores)
- Ver y hacer todo lo que puede hacer cualquier otro rol

**Ruta en el frontend:** `/administrator` y subrutas

**Credenciales de demo:**
- Usuario: `admin`
- Contraseña: `Admin1234`

---

### ADMISSION — Admisión
El personal de recepción. Primer punto de contacto con el paciente.

**Qué puede hacer:**
- Registrar nuevos pacientes (crea cuenta + datos demográficos)
- Buscar pacientes existentes por DPI, nombre o email
- Crear citas para pacientes
- Activar citas cuando el paciente llega físicamente
- Cancelar citas

**Ruta en el frontend:** `/admission`

**Credenciales de demo:**
- Usuario: `admision`
- Contraseña: `Admision1234`

---

### VITAL_SIGNS — Signos Vitales
El personal de enfermería en sala de espera.

**Qué puede hacer:**
- Ver la lista de pacientes esperando signos vitales (citas en estado `VITAL_SIGNS`)
- Registrar signos vitales: presión sistólica/diastólica, frecuencia cardíaca, frecuencia respiratoria, temperatura, saturación de oxígeno, peso, altura
- El sistema calcula el BMI automáticamente

**Ruta en el frontend:** `/vitals/triage` y `/vitals/triage/capture`

---

### DOCTOR — Médico
El médico tratante.

**Qué puede hacer:**
- Ver la lista de pacientes listos para consulta (citas en estado `CONSULTATION`)
- Realizar el triaje Manchester (seleccionar motivo y discriminadores)
- Registrar la consulta médica (diagnóstico CIE-10, síntomas, plan de tratamiento)
- Generar recetas médicas con medicamentos detallados
- Generar órdenes de laboratorio
- Ver el historial clínico de cualquier paciente

**Rutas en el frontend:** `/doctor/consultas` y `/doctor/consulta/:appointmentId`

**Credenciales de demo:**
- Usuario: `doctor`
- Contraseña: `Doctor1234`

---

### LABORATORY — Laboratorio
El técnico de laboratorio.

**Qué puede hacer:**
- Ver las órdenes de laboratorio pendientes
- Registrar la recolección de muestras
- Validar muestras
- Marcar muestras en procesamiento
- Subir resultados en PDF o imagen
- Marcar resultados como listos

**Rutas en el frontend:** `/lab` y `/lab/workflow/:appointmentId`

---

### PHARMACY — Farmacia
El farmacéutico.

**Qué puede hacer:**
- Ver las recetas pendientes de despacho
- Ver el detalle de cada receta (medicamentos, dosis, instrucciones)
- Despachar medicamentos (valida stock antes)
- Gestionar el inventario de medicamentos

**Ruta en el frontend:** `/pharmacy`

---

### CASHIER — Caja
El cajero del hospital.

**Qué puede hacer:**
- Ver las facturas pendientes de pago
- Crear facturas con cargos detallados (consultas, exámenes, medicamentos)
- Procesar pagos en efectivo, tarjeta o transferencia
- Ver el historial de pagos

**Ruta en el frontend:** `/cashier`

---

### PATIENT — Paciente
El paciente del hospital. Accede al portal web.

**Qué puede hacer:**
- Ver su propio historial clínico
- Ver sus recetas médicas
- Ver sus resultados de laboratorio
- Ver sus facturas
- Actualizar sus datos de contacto
- Agendar citas en línea

**Ruta en el frontend:** `/patient`

**Cómo obtiene acceso:** Cuando admisión registra al paciente, el sistema genera automáticamente una contraseña temporal que se envía al email del paciente. El paciente activa su cuenta desde el link en el email.

---

## Matriz de permisos

| Acción | ADMIN | ADMISSION | VITAL_SIGNS | DOCTOR | LABORATORY | PHARMACY | CASHIER | PATIENT |
|--------|:-----:|:---------:|:-----------:|:------:|:----------:|:--------:|:-------:|:-------:|
| Crear empleados | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Registrar pacientes | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Crear/activar citas | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | 👁️* |
| Registrar signos vitales | ✅ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Realizar triaje | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Registrar consultas | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Generar recetas | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| Ver recetas | ✅ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | 👁️** |
| Despachar medicamentos | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Gestionar laboratorio | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | ❌ |
| Ver resultados de lab | ✅ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ | 👁️** |
| Crear facturas | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| Ver facturas | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | 👁️** |
| Ver historial clínico | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | 👁️** |
| Gestionar catálogos | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

👁️* = Solo puede agendar citas propias desde el portal público  
👁️** = Solo puede ver sus propios datos

---

## Cómo funciona el RBAC en el código

### Backend (API Gateway)
El gateway extrae el rol del JWT y lo incluye como header `X-User-Role` al enrutar. Cada servicio puede leer este header para validaciones adicionales.

### Backend (Clinical Service)
El `PermissionValidator` en clinical-service verifica el rol antes de ejecutar cada caso de uso. ADMIN bypassa todas las validaciones.

### Frontend (ProtectedRoute)
```tsx
// Ejemplo: solo DOCTOR puede acceder
<ProtectedRoute requiredRole="DOCTOR">
  <DoctorConsultation />
</ProtectedRoute>

// Ejemplo: VITAL_SIGNS o DOCTOR pueden acceder
<ProtectedRoute requiredRole={["VITAL_SIGNS", "DOCTOR"]}>
  <TriagePendingPage />
</ProtectedRoute>
```

ADMIN bypassa todas las verificaciones de rol en el frontend también.

### Frontend (Menú de navegación)
El `MainLayout` filtra los items del menú según el rol del usuario logueado. Un CASHIER solo ve las opciones de caja; un DOCTOR solo ve las opciones de consultas.
