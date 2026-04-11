# MedFlow HIS - Estructura de Perfiles

## ✅ Estructura Creada

### 📁 Páginas por Rol

Estructura de carpetas creada en `src/pages/`:

- ✅ `administrator/` - Crear/Actualizar/Desactivar perfiles
  - `AdministratorDashboard.tsx`
  
- ✅ `admission/` - Crear/Validar/Activar citas
  - `ActivateAppointments.tsx` ✨ **Vista completa implementada**
  - `index.ts`
  
- ✅ `vitals/` - Captura de signos vitales (Enfermería)
  - `VitalSignsCapture.tsx`
  
- ✅ `doctor/` - Consulta y Triaje Manchester
  - `DoctorConsultation.tsx`
  
- ✅ `lab/` - Gestión de muestras
  - `LabSampleManagement.tsx`
  
- ✅ `pharmacy/` - Despacho de medicinas
  - `PharmacyDispense.tsx`
  
- ✅ `cashier/` - Caja y Facturación (SAT/FEL)
  - `CashierBilling.tsx`

### 🎨 Componentes

- ✅ `Layout/MainLayout.tsx` - **Layout principal con Sidebar dinámico**
  - Sidebar responsive (mobile hamburger menu)
  - Menú filtrado por rol de usuario
  - Logo MedFlow
  - Sección de usuario
  - Botón de logout
  
### 🔌 Servicios API

- ✅ `services/appointmentService.ts` - **Servicio con placeholders Axios**
  - `getAppointments()` - Obtener todas las citas
  - `activateAppointment(id)` - Activar cita
  - `createAppointment(data)` - Crear nueva cita
  - `validateAppointment(id)` - Validar cita
  - Mock data incluido para desarrollo
  - Código Axios preparado y comentado para backend

### 📄 Páginas de Ejemplo

- ✅ `DashboardPage.tsx` - Dashboard con estadísticas y acciones rápidas

## 🚀 Cómo Usar

### 1. Vista de Activar Citas (Admisión)

```tsx
import ActivateAppointments from './pages/admission/ActivateAppointments';

// En tu router
<Route path="/admission/activate" element={<ActivateAppointments />} />
```

### 2. MainLayout (en cualquier página)

```tsx
import MainLayout from './components/Layout/MainLayout';

const MiPagina = () => {
  return (
    <MainLayout userRole="ADMISSION" userName="Juan Pérez">
      {/* Tu contenido aquí */}
    </MainLayout>
  );
};
```

### 3. Roles Disponibles

- `ADMINISTRATOR`
- `ADMISSION`
- `VITAL_SIGNS`
- `DOCTOR`
- `LABORATORY`
- `PHARMACY`
- `CASHIER`

El Sidebar mostrará automáticamente solo los módulos permitidos para cada rol.

## 🎨 Características del MainLayout

- ✅ **Sidebar responsive** con hamburger menu en móvil
- ✅ **Filtrado por rol** - muestra solo módulos autorizados
- ✅ **Resaltado de ruta activa**
- ✅ **Logo MedFlow** con branding
- ✅ **Info de usuario** con inicial en círculo
- ✅ **Botón de logout**
- ✅ **Header con fecha y título dinámico**
- ✅ **Iconos de Heroicons**

## 📊 Vista de Activar Citas

La vista `ActivateAppointments` incluye:

- ✅ **Tabla de citas pendientes** con:
  - ID de cita
  - Nombre del paciente
  - DPI
  - Fecha y hora
  - Doctor/Especialidad
  - Estado (Pending/Activated/Cancelled)
  - Botón de activación
  
- ✅ **Búsqueda en tiempo real** por:
  - Nombre de paciente
  - DPI
  - ID de cita
  
- ✅ **Mensajes de éxito/error**
- ✅ **Estados visuales** con badges de colores
- ✅ **Loading states**
- ✅ **Botón de refrescar**

## 🔧 Servicios API

Todos los servicios siguen el patrón del `AGENT.md`:

```typescript
// Estructura preparada para backend
const API_URL = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080';

export const activateAppointment = async (id: string) => {
  // BACKEND READY: Descomentar cuando el endpoint esté disponible
  // return await axios.post(`${API_URL}/appointments/${id}/activate`);
  
  // Mock actual
  console.log(`Mock: Activating appointment ${id}`);
  return { status: 200, data: { message: "Activated" } };
};
```

## 📦 Dependencias Instaladas

- ✅ `@heroicons/react` - Iconos para UI

## 🎯 Próximos Pasos

1. **Configurar Router** - Añadir rutas para cada módulo
2. **Implementar Auth Context** - Gestión de sesión y rol de usuario
3. **Conectar Backend** - Descomentar llamadas Axios cuando endpoints estén listos
4. **Implementar vistas restantes** - Completar módulos de otros roles
5. **Agregar variables de entorno** - Configurar `VITE_API_GATEWAY_URL`

## 🌐 Variables de Entorno

Crear archivo `.env` en la raíz de `frontend-react/`:

```env
VITE_API_GATEWAY_URL=http://localhost:8080
```

## ✨ Demo Data

El servicio incluye datos de prueba (mock) con 5 citas de ejemplo para desarrollo y testing.
