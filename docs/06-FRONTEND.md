# Frontend — MedFlow HIS

## Stack

- **React 19** con TypeScript
- **Vite** como build tool
- **Tailwind CSS** para estilos
- **React Router v7** para navegación
- **Axios** para peticiones HTTP
- **SweetAlert2** para modales y alertas
- **Heroicons** para iconos
- **Vitest** para tests

---

## Estructura de carpetas

```
frontend-medflow/src/
├── api/
│   └── index.ts              ← Cliente Axios configurado
├── components/               ← Componentes reutilizables
│   ├── ProtectedRoute/       ← Guard de autenticación y roles
│   ├── MainLayout/           ← Layout con sidebar y navegación
│   ├── triage/               ← Componentes del flujo de triaje
│   ├── lab/                  ← Componentes del flujo de laboratorio
│   ├── pharmacy/             ← Componentes de farmacia
│   └── shared/               ← Componentes compartidos (modales, botones, etc.)
├── context/
│   ├── AuthContext.tsx        ← Estado global de autenticación
│   └── PatientHistoryContext.tsx ← Estado compartido del historial del paciente
├── data/
│   └── cie10.ts              ← Catálogo de códigos CIE-10 para diagnósticos
├── hooks/
│   └── useAuth.ts            ← Hook para acceder al contexto de auth
├── pages/                    ← Páginas por módulo
│   ├── administrator/
│   ├── admission/
│   ├── cashier/
│   ├── doctor/
│   ├── lab/
│   ├── patient/
│   ├── pharmacy/
│   ├── vitals/
│   ├── HomePage.tsx
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── ActivateAccountPage.tsx
│   ├── AppointmentPage.tsx   ← Agendamiento público en línea
│   └── PaymentGatewayPage.tsx
├── services/                 ← Capa de servicios (llamadas a la API)
│   ├── authService.ts
│   ├── patientService.ts
│   ├── clinicalService.ts
│   ├── appointmentService.ts
│   ├── labService.ts
│   ├── pharmacyService.ts
│   ├── billingService.ts
│   ├── billingCatalogService.ts
│   ├── clinicService.ts
│   ├── doctorService.ts
│   ├── employeeService.ts
│   ├── manchesterService.ts
│   ├── clinicalCatalogService.ts
│   └── labCatalogService.ts
└── types/                    ← Interfaces TypeScript
    ├── appointment.ts
    ├── clinic.ts
    ├── patient.ts
    └── triage.ts
```

---

## Cliente Axios (src/api/index.ts)

El cliente Axios está configurado con dos interceptores:

**Request interceptor** — agrega automáticamente en cada petición:
- `Authorization: Bearer {token}` — leído de `localStorage.auth_token`
- `X-User-Id: {userId}` — leído de `localStorage.user_data`

**Response interceptor** — si recibe un 401 (y no es la petición de login):
- Limpia `localStorage`
- Redirige a `/login`

```typescript
const api = axios.create({
  baseURL: import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080',
});
```

---

## Autenticación (AuthContext)

El `AuthContext` mantiene el estado global del usuario logueado. Al cargar la app, lee `localStorage` para restaurar la sesión si existe.

```typescript
interface AuthContextType {
  user: AuthUser | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  hasRole: (role: string) => boolean;
  logout: () => void;
  setUser: (user: AuthUser | null) => void;
}
```

El hook `useAuth()` da acceso a este contexto desde cualquier componente.

**Nota:** `hasRole()` devuelve `true` para ADMIN sin importar qué rol se pida — ADMIN bypassa todas las verificaciones.

---

## Rutas y protección (App.tsx)

### Rutas públicas (sin autenticación)

| Ruta | Página | Descripción |
|------|--------|-------------|
| `/` | HomePage | Página principal del hospital |
| `/nosotros` | Nosotros | Información del hospital |
| `/servicios` | Servicios | Catálogo de servicios |
| `/appointment` | AppointmentPage | Agendamiento en línea |
| `/payment` | PaymentGatewayPage | Pago de cita en línea |
| `/login` | LoginPage | Login de empleados y pacientes |
| `/register` | RegisterPage | Auto-registro de pacientes |
| `/activate` | ActivateAccountPage | Activación de cuenta por email |

### Rutas protegidas por rol

| Ruta | Rol requerido | Página |
|------|--------------|--------|
| `/administrator` | ADMIN | AdministratorDashboard |
| `/administrator/empleados` | ADMIN | EmployeeManagementPage |
| `/administrator/doctores` | ADMIN | DoctorManagementPage |
| `/administrator/clinicas` | ADMIN | ClinicManagementPage |
| `/administrator/medicamentos` | ADMIN | MedicamentosPage |
| `/administrator/examenes` | ADMIN | ExamenesPage |
| `/administrator/servicios` | ADMIN | ServiciosPage |
| `/administrator/triage` | ADMIN | TriageCatalogPage |
| `/admission` | ADMISSION | ActivateAppointments |
| `/vitals/triage` | VITAL_SIGNS o DOCTOR | TriagePendingPage |
| `/vitals/triage/capture` | VITAL_SIGNS o DOCTOR | TriageVitalSignsCapture |
| `/doctor/consultas` | DOCTOR | DoctorConsultation |
| `/doctor/consulta/:appointmentId` | DOCTOR | PatientConsultationForm |
| `/lab` | LABORATORY | LabSampleManagement |
| `/lab/workflow/:appointmentId` | LABORATORY | LabSampleWorkflow |
| `/pharmacy` | PHARMACY | PharmacyPage |
| `/cashier` | CASHIER | CashierPage |
| `/patient` | PATIENT | PatientDashboard |

---

## Páginas por módulo

### Módulo Administrador

**AdministratorDashboard** — Panel principal con accesos directos a todos los catálogos y gestión.

**EmployeeManagementPage** — CRUD de empleados. Permite crear cuentas con cualquier rol (excepto PATIENT). Llama a `employeeService.ts`.

**DoctorManagementPage** — CRUD de doctores. Un doctor es un usuario con rol DOCTOR más datos adicionales (especialidad, clínica). Llama a `doctorService.ts`.

**ClinicManagementPage** — CRUD de clínicas del hospital. Llama a `clinicService.ts`.

**MedicamentosPage** — Gestión del catálogo de medicamentos (nombre, unidad, stock, precio). Llama a `clinicalCatalogService.ts`.

**ExamenesPage** — Gestión del catálogo de tipos de exámenes de laboratorio. Llama a `labCatalogService.ts`.

**ServiciosPage** — Gestión del catálogo de servicios y precios para facturación. Llama a `billingCatalogService.ts`.

**TriageCatalogPage** — Gestión del catálogo Manchester: motivos de consulta y sus discriminadores con el nivel de prioridad que activan. Llama a `manchesterService.ts`.

---

### Módulo Admisión

**ActivateAppointments** — Tabla de citas con filtros por estado y búsqueda. Muestra el nombre del paciente, DPI, doctor, fecha y hora. Botones para activar o cancelar cada cita. Llama a `appointmentService.ts`.

---

### Módulo Signos Vitales

**TriagePendingPage** — Lista de citas en estado `VITAL_SIGNS`. Muestra el nombre del paciente, hora de llegada y prioridad si ya tiene triaje. Cada fila tiene un botón para ir a capturar signos vitales.

**TriageVitalSignsCapture** — Formulario de captura de signos vitales. Campos: presión sistólica, presión diastólica, frecuencia cardíaca, frecuencia respiratoria, temperatura, saturación de oxígeno, peso, altura. El BMI se calcula y muestra en tiempo real. Al guardar, llama a `clinicalService.recordVitalSigns()`.

---

### Módulo Doctor

**DoctorConsultation** — Lista de citas en estado `CONSULTATION` asignadas al doctor logueado. Muestra nombre del paciente, hora, y si ya tiene signos vitales y triaje registrados.

**PatientConsultationForm** — Formulario completo de consulta médica. Tiene secciones para:
- Ver signos vitales y triaje previos (solo lectura)
- Realizar triaje Manchester (seleccionar motivo y discriminadores del catálogo)
- Registrar diagnóstico principal (con búsqueda de códigos CIE-10) y diagnósticos secundarios
- Síntomas, notas médicas, plan de tratamiento
- Generar receta (agregar medicamentos con dosis, frecuencia, duración)
- Generar orden de laboratorio (seleccionar exámenes del catálogo)
- Programar cita de seguimiento

---

### Módulo Laboratorio

**LabSampleManagement** — Lista de órdenes de laboratorio filtradas por estados de lab (`LAB_SAMPLE_COLLECTION`, `LAB_SAMPLE_PENDING`, `LAB_PROCESSING`, `LAB_RESULTS_READY`). Cada fila muestra el código de orden, paciente, exámenes solicitados y estado actual.

**LabSampleWorkflow** — Wizard de 4 pasos para procesar una orden:
1. **Recolección de muestra** — registra quién recolectó y notas
2. **Validación de muestra** — confirma que la muestra es válida
3. **Procesamiento** — marca la muestra como en procesamiento y permite subir el archivo de resultados
4. **Resultados listos** — confirma que los resultados están disponibles para el paciente

Componentes del wizard: `WizardProgressBar`, `SampleCollectionStep`, `SampleValidationStep`, `TestProcessingStep`, `TestResultUpload`, `ResultsReadyStep`, `ResultsList`.

---

### Módulo Farmacia

**PharmacyPage** — Cola de recetas pendientes. Muestra el código de receta, paciente, doctor y medicamentos. Botón para ver el detalle y despachar.

**PharmacyDispense** — Detalle de una receta con todos los medicamentos, dosis e instrucciones. Botón para confirmar el despacho.

**PharmacyQueue** — Componente de lista de recetas.

**PrescriptionDetail** — Componente de detalle de receta.

---

### Módulo Caja

**CashierPage** — Interfaz del cajero. Permite buscar pacientes, ver sus facturas pendientes, crear nuevas facturas con cargos del catálogo de servicios, y procesar pagos.

**PaymentModal** — Modal para procesar el pago: seleccionar método (efectivo/tarjeta/transferencia), ingresar monto recibido, calcular cambio automáticamente.

---

### Módulo Paciente

**PatientDashboard** — Portal del paciente. Muestra:
- Resumen de próximas citas
- Historial de consultas
- Recetas activas
- Resultados de laboratorio disponibles
- Facturas

---

### Páginas públicas

**HomePage** — Landing page del hospital con información general, servicios destacados y acceso al agendamiento en línea.

**AppointmentPage** — Formulario de agendamiento en línea. El paciente selecciona especialidad, doctor, fecha y hora disponible. Requiere estar registrado o registrarse en el momento.

**PaymentGatewayPage** — Pasarela de pago para citas agendadas en línea.

**LoginPage** — Formulario de login. Redirige al dashboard correspondiente según el rol del usuario.

**RegisterPage** — Auto-registro de pacientes. Crea la cuenta y envía email de activación.

**ActivateAccountPage** — Página que procesa el token de activación del email.

---

## Componentes compartidos

**ProtectedRoute** — Wrapper que verifica autenticación y rol. Si el usuario no está autenticado, redirige a `/login`. Si no tiene el rol requerido, redirige a `/`. ADMIN bypassa todas las verificaciones de rol.

**MainLayout** — Layout principal con sidebar de navegación. El menú se filtra según el rol del usuario logueado — cada rol solo ve sus opciones.

**HistorialFloatingButton + HistorialModal** — Botón flotante disponible en las páginas de doctor y signos vitales que abre un modal con el historial clínico completo del paciente actual.

---

## Servicios (src/services/)

Cada archivo de servicio encapsula las llamadas a la API para un dominio específico. Todos usan el cliente Axios de `src/api/index.ts`.

```typescript
// Ejemplo: authService.ts
export const login = async (credentials: LoginCredentials): Promise<AuthResponse> => {
  const response = await api.post<AuthResponse>('/api/auth/login', credentials);
  localStorage.setItem('auth_token', response.data.token);
  localStorage.setItem('user_data', JSON.stringify(response.data.user));
  return response.data;
};
```

Los servicios no usan mocks — todas las llamadas van al backend real a través del API Gateway.

---

## Variables de entorno

```env
# .env (en frontend-medflow/)
VITE_API_GATEWAY_URL=http://localhost:8080
```

En Docker, esta variable se pasa como build arg y queda embebida en el bundle de producción.
