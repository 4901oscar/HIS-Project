# Maintenance UI Fixes - Bugfix Design

## Overview

Este documento formaliza el diseño de solución para tres problemas de interfaz de usuario identificados en los módulos de mantenimiento del sistema MedFlow:

1. **Navegación inconsistente**: Diferentes páginas de mantenimiento utilizan patrones de navegación distintos para volver al dashboard de administrador
2. **Campos obligatorios sin indicador visual**: El formulario DoctorForm no muestra asteriscos rojos en todos los campos requeridos
3. **Citas no visibles para pacientes**: Los usuarios con rol PATIENT no pueden ver sus citas debido a un problema en el endpoint del backend

La estrategia de corrección se enfoca en:
- Unificar el patrón de navegación usando una flecha simple que navega a `/administrator`
- Agregar asteriscos rojos (`<span className="text-red-500">*</span>`) en todos los labels de campos obligatorios
- Corregir el endpoint `/api/clinical/appointments/my` para buscar citas por `patientId` en lugar de `userId`

## Glossary

- **Bug_Condition (C)**: Las condiciones que activan cada uno de los tres bugs
- **Property (P)**: El comportamiento deseado cuando se corrigen los bugs
- **Preservation**: Funcionalidad existente que debe permanecer sin cambios
- **MainLayout**: Componente de layout principal usado en todas las páginas de administrador
- **DoctorForm**: Componente de formulario para vincular/editar doctores
- **PatientDashboard**: Dashboard del paciente que muestra citas y historial médico
- **appointmentService**: Servicio frontend que consume el endpoint de citas

## Bug Details

### Bug Condition 1: Navegación Inconsistente

El bug se manifiesta cuando el usuario navega entre diferentes páginas de mantenimiento y encuentra patrones de navegación inconsistentes.

**Formal Specification:**
```
FUNCTION isBugCondition1(input)
  INPUT: input of type { page: string, viewMode?: string }
  OUTPUT: boolean
  
  RETURN (input.page === 'EmployeeFormPage' AND buttonText === '← Gestión de Personal')
         OR (input.page === 'DoctorManagementPage' AND input.viewMode === 'list' AND duplicateButtonExists)
         OR (input.page IN ['ServiciosPage', 'ExamenesPage', 'MedicamentosPage'] AND buttonIsSimpleArrow)
END FUNCTION
```

### Bug Condition 2: Campos Obligatorios Sin Asterisco

El bug se manifiesta cuando el usuario visualiza el formulario DoctorForm y no puede identificar claramente cuáles campos son obligatorios.

**Formal Specification:**
```
FUNCTION isBugCondition2(input)
  INPUT: input of type { component: string, mode: string, fieldName: string }
  OUTPUT: boolean
  
  RETURN input.component === 'DoctorForm'
         AND input.fieldName IN ['Seleccionar Doctor', 'Especialidad', 'Hora de Inicio del Turno', 'Hora de Fin del Turno']
         AND NOT hasRedAsterisk(input.fieldName)
END FUNCTION
```

### Bug Condition 3: Citas No Aparecen para Pacientes

El bug se manifiesta cuando un usuario con rol PATIENT accede a su dashboard y la lista de citas aparece vacía aunque tenga citas registradas.

**Formal Specification:**
```
FUNCTION isBugCondition3(input)
  INPUT: input of type { userRole: string, hasAppointments: boolean }
  OUTPUT: boolean
  
  RETURN input.userRole === 'PATIENT'
         AND input.hasAppointments === true
         AND appointmentListIsEmpty()
         AND endpointSearchesByUserId()
END FUNCTION
```

### Examples

**Bug 1 - Navegación Inconsistente:**
- Usuario en EmployeeFormPage ve "← Gestión de Personal" → Debería ver flecha simple
- Usuario en DoctorManagementPage (modo lista) ve botón "VINCULAR DOCTOR" duplicado → Debería aparecer solo una vez
- Usuario en ServiciosPage ve flecha simple → Correcto, pero inconsistente con EmployeeFormPage

**Bug 2 - Campos Sin Asterisco:**
- Campo "Seleccionar Doctor" en modo creación → No muestra asterisco rojo
- Campo "Especialidad" → No muestra asterisco rojo
- Campo "Hora de Inicio del Turno" → No muestra asterisco rojo
- Campo "Hora de Fin del Turno" → No muestra asterisco rojo

**Bug 3 - Citas No Visibles:**
- Paciente con DPI "1234567890101" tiene 3 citas registradas
- Al acceder a PatientDashboard, la lista muestra "No tienes citas registradas"
- El endpoint busca por `userId` pero las citas están asociadas a `patientId`

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**

**Navegación General:**
- El sistema debe continuar manteniendo el estado de autenticación al navegar
- Los botones de cancelar en formularios deben continuar descartando cambios
- La navegación entre secciones del sistema debe continuar funcionando correctamente

**Funcionalidad de Formularios:**
- Los formularios deben continuar validando datos correctamente
- El envío de formularios válidos debe continuar guardando datos
- Los mensajes de error de validación deben continuar mostrándose apropiadamente
- La carga y actualización de registros existentes debe continuar funcionando

**Visualización de Datos:**
- Las listas de registros (doctores, medicamentos, servicios, exámenes) deben continuar mostrándose correctamente
- Los filtros y búsquedas deben continuar funcionando
- El historial clínico en PatientDashboard (consultas, signos vitales, recetas, laboratorio) debe continuar mostrándose correctamente

**Gestión de Doctores:**
- La creación, edición y desactivación de doctores debe continuar funcionando
- La gestión de días libres debe continuar funcionando correctamente

**Scope:**
Todas las funcionalidades que NO involucran:
- Botones de navegación "volver" en páginas de mantenimiento
- Labels de campos obligatorios en DoctorForm
- Consulta de citas para usuarios PATIENT

deben permanecer completamente sin cambios.

## Hypothesized Root Cause

### Bug 1: Navegación Inconsistente

Basado en el análisis del código, las causas más probables son:

1. **Falta de Patrón Unificado**: Cada página de mantenimiento fue desarrollada de forma independiente sin seguir un patrón de navegación consistente
   - EmployeeFormPage usa un botón con texto "← Gestión de Personal"
   - DoctorManagementPage tiene lógica condicional compleja con botones duplicados
   - ServiciosPage, ExamenesPage y MedicamentosPage usan flecha simple (patrón correcto)

2. **Duplicación de Botones en DoctorManagementPage**: El botón "VINCULAR DOCTOR" aparece tanto en el header de la página como en el componente DoctorList cuando `viewMode === 'list'`

3. **Lógica Condicional Incorrecta**: En DoctorManagementPage, el botón de volver solo aparece cuando `viewMode !== 'list'`, pero debería estar siempre visible

### Bug 2: Campos Obligatorios Sin Asterisco

1. **Omisión en el Desarrollo**: Los labels de los campos obligatorios en DoctorForm no incluyen el `<span className="text-red-500">*</span>` que sí está presente en otros formularios del sistema

2. **Inconsistencia con Otros Formularios**: EmployeeFormPage y otros formularios sí muestran asteriscos rojos en campos obligatorios, indicando que existe un patrón establecido que no se siguió en DoctorForm

### Bug 3: Citas No Aparecen para Pacientes

1. **Búsqueda por Campo Incorrecto**: El endpoint `/api/clinical/appointments/my` en el backend busca citas usando `userId` del token JWT, pero las citas están asociadas a `patientId` en la base de datos

2. **Falta de Mapeo userId → patientId**: El sistema no realiza la conversión necesaria de `userId` (del usuario autenticado) a `patientId` (del registro de paciente) antes de consultar las citas

3. **Modelo de Datos**: Las citas se almacenan con referencia a `patientId` (tabla patients), no a `userId` (tabla users), lo cual es correcto desde el punto de vista del dominio, pero requiere un paso adicional de mapeo

## Correctness Properties

Property 1: Bug Condition 1 - Navegación Uniforme en Mantenimientos

_For any_ página de mantenimiento (EmployeeFormPage, DoctorManagementPage, ServiciosPage, ExamenesPage, MedicamentosPage), el botón de volver SHALL ser una flecha simple que navega a `/administrator`, y en DoctorManagementPage el botón "VINCULAR DOCTOR" SHALL aparecer solo una vez en el header cuando viewMode === 'list'.

**Validates: Requirements 4.1, 4.2, 4.3**

Property 2: Bug Condition 2 - Indicadores Visuales en DoctorForm

_For any_ campo obligatorio en DoctorForm ("Seleccionar Doctor" en modo creación, "Especialidad", "Hora de Inicio del Turno", "Hora de Fin del Turno"), el label SHALL mostrar `<span className="text-red-500">*</span>` después del texto del label.

**Validates: Requirements 5.1, 5.2, 5.3**

Property 3: Bug Condition 3 - Citas Visibles para Pacientes

_For any_ usuario con rol PATIENT que tenga citas registradas, el endpoint `/api/clinical/appointments/my` SHALL retornar todas las citas asociadas al patientId correspondiente al userId autenticado, y PatientDashboard SHALL mostrar estas citas ordenadas por fecha y hora.

**Validates: Requirements 6.1, 6.2**

Property 4: Preservation - Funcionalidad Existente

_For any_ funcionalidad que NO involucre los tres bugs identificados (navegación en mantenimientos, labels en DoctorForm, consulta de citas para pacientes), el sistema SHALL producir exactamente el mismo comportamiento que antes de las correcciones, preservando toda la funcionalidad existente.

**Validates: Requirements 7.1, 7.2, 8.1, 8.2, 8.3, 9.1, 9.2, 9.3, 10.1, 10.2**

## Fix Implementation

### Changes Required

Asumiendo que nuestro análisis de causa raíz es correcto:

#### Bug 1: Navegación Inconsistente

**File**: `frontend-medflow/src/pages/administrator/EmployeeFormPage.tsx`

**Changes**:
1. **Simplificar botón de volver**: Reemplazar el botón con texto "← Gestión de Personal" por una flecha simple
   - Cambiar de: `<button onClick={() => navigate('/administrator/empleados')} className="...">← Gestión de Personal</button>`
   - A: `<button onClick={() => navigate('/administrator')} className="text-gray-400 hover:text-gray-600"><svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" /></svg></button>`
   - Actualizar la navegación de `/administrator/empleados` a `/administrator`

**File**: `frontend-medflow/src/pages/administrator/DoctorManagementPage.tsx`

**Changes**:
1. **Eliminar botón duplicado**: Remover el botón "VINCULAR DOCTOR" del header cuando viewMode === 'list'
   - El botón ya existe en el componente DoctorList
   - Mantener solo el botón de volver en el header

2. **Simplificar lógica de navegación**: 
   - Cuando viewMode === 'list': mostrar flecha simple que navega a `/administrator`
   - Cuando viewMode !== 'list': mostrar botón "← VOLVER A LA LISTA" que ejecuta handleCancel

**File**: `frontend-medflow/src/components/DoctorList/DoctorList.tsx`

**Changes**:
1. **Mantener botón "Vincular Doctor"**: Este componente ya tiene el botón correcto, no requiere cambios

#### Bug 2: Campos Obligatorios Sin Asterisco

**File**: `frontend-medflow/src/components/DoctorForm/DoctorForm.tsx`

**Changes**:
1. **Agregar asterisco al campo "Seleccionar Doctor"** (solo en modo creación):
   - Cambiar: `<label className="block text-sm font-medium text-gray-700 mb-1">Seleccionar Doctor *</label>`
   - A: `<label className="block text-sm font-medium text-gray-700 mb-1">Seleccionar Doctor <span className="text-red-500">*</span></label>`

2. **Agregar asterisco al campo "Especialidad"**:
   - Cambiar: `<label className="block text-sm font-medium text-gray-700 mb-1">Especialidad *</label>`
   - A: `<label className="block text-sm font-medium text-gray-700 mb-1">Especialidad <span className="text-red-500">*</span></label>`

3. **Agregar asterisco al campo "Hora de Inicio del Turno"**:
   - Cambiar: `<label className="block text-sm font-medium text-gray-700 mb-1">Hora de Inicio del Turno *</label>`
   - A: `<label className="block text-sm font-medium text-gray-700 mb-1">Hora de Inicio del Turno <span className="text-red-500">*</span></label>`

4. **Agregar asterisco al campo "Hora de Fin del Turno"**:
   - Cambiar: `<label className="block text-sm font-medium text-gray-700 mb-1">Hora de Fin del Turno *</label>`
   - A: `<label className="block text-sm font-medium text-gray-700 mb-1">Hora de Fin del Turno <span className="text-red-500">*</span></label>`

#### Bug 3: Citas No Aparecen para Pacientes

**File**: `backend-services/clinical-service/src/main/java/com/medflow/clinical/controller/AppointmentController.java` (hipotético)

**Changes**:
1. **Modificar endpoint `/api/clinical/appointments/my`**:
   - Obtener `userId` del token JWT autenticado
   - Buscar el registro de `Patient` asociado a ese `userId`
   - Consultar citas usando el `patientId` obtenido
   - Retornar las citas ordenadas por fecha y hora

2. **Agregar manejo de errores**:
   - Si no existe un registro de Patient para el userId, retornar lista vacía o error 404
   - Validar que el usuario tenga rol PATIENT

**Pseudocódigo del cambio en el backend**:
```java
@GetMapping("/my")
public ResponseEntity<List<AppointmentResponse>> getMyAppointments(Authentication auth) {
    String userId = auth.getName(); // userId del token JWT
    
    // Buscar el patientId asociado al userId
    Patient patient = patientRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundException("Patient record not found"));
    
    // Consultar citas por patientId
    List<Appointment> appointments = appointmentRepository.findByPatientId(patient.getId());
    
    // Ordenar por fecha y hora
    appointments.sort(Comparator.comparing(Appointment::getAppointmentDate)
        .thenComparing(Appointment::getAppointmentTime));
    
    return ResponseEntity.ok(appointments.stream()
        .map(this::toResponse)
        .collect(Collectors.toList()));
}
```

## Testing Strategy

### Validation Approach

La estrategia de testing sigue un enfoque de dos fases: primero, demostrar los bugs en el código sin corregir mediante tests exploratorios, luego verificar que las correcciones funcionan y preservan el comportamiento existente.

### Exploratory Bug Condition Checking

**Goal**: Demostrar los tres bugs ANTES de implementar las correcciones. Confirmar o refutar el análisis de causa raíz.

**Test Plan**: Escribir tests que verifiquen el comportamiento actual (buggy) y documenten los problemas encontrados.

**Test Cases**:

1. **Bug 1 - Navegación Inconsistente**:
   - **Test 1.1**: Renderizar EmployeeFormPage y verificar que el botón muestra "← Gestión de Personal" (fallará después del fix)
   - **Test 1.2**: Renderizar DoctorManagementPage en modo 'list' y contar botones "VINCULAR DOCTOR" (debería encontrar 2, fallará después del fix)
   - **Test 1.3**: Verificar que ServiciosPage, ExamenesPage y MedicamentosPage usan flecha simple (pasará antes y después)

2. **Bug 2 - Campos Sin Asterisco**:
   - **Test 2.1**: Renderizar DoctorForm en modo creación y verificar que el label "Seleccionar Doctor" NO contiene `<span className="text-red-500">*</span>` (fallará después del fix)
   - **Test 2.2**: Verificar que los labels "Especialidad", "Hora de Inicio del Turno" y "Hora de Fin del Turno" NO contienen asterisco rojo (fallará después del fix)

3. **Bug 3 - Citas No Aparecen**:
   - **Test 3.1**: Crear un usuario PATIENT con userId="user123" y patientId="patient456"
   - **Test 3.2**: Crear 2 citas asociadas a patientId="patient456"
   - **Test 3.3**: Llamar al endpoint `/api/clinical/appointments/my` con token de userId="user123"
   - **Test 3.4**: Verificar que retorna lista vacía (fallará después del fix, debería retornar 2 citas)

**Expected Counterexamples**:
- Bug 1: Botones de navegación inconsistentes entre páginas de mantenimiento
- Bug 2: Labels sin asterisco rojo en campos obligatorios de DoctorForm
- Bug 3: Endpoint retorna lista vacía porque busca por userId en lugar de patientId

### Fix Checking

**Goal**: Verificar que para todos los inputs donde las condiciones de bug se cumplen, el código corregido produce el comportamiento esperado.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition1(input) DO
  result := renderPage_fixed(input)
  ASSERT navigationButtonIsSimpleArrow(result)
  ASSERT navigatesToAdministrator(result)
  ASSERT noDuplicateButtons(result)
END FOR

FOR ALL input WHERE isBugCondition2(input) DO
  result := renderDoctorForm_fixed(input)
  ASSERT labelHasRedAsterisk(result, input.fieldName)
END FOR

FOR ALL input WHERE isBugCondition3(input) DO
  result := getMyAppointments_fixed(input.userId)
  ASSERT result.length > 0
  ASSERT result.every(appt => appt.patientId === getPatientId(input.userId))
END FOR
```

### Preservation Checking

**Goal**: Verificar que para todos los inputs donde las condiciones de bug NO se cumplen, el código corregido produce exactamente el mismo resultado que el código original.

**Pseudocode:**
```
FOR ALL input WHERE NOT (isBugCondition1(input) OR isBugCondition2(input) OR isBugCondition3(input)) DO
  ASSERT originalBehavior(input) = fixedBehavior(input)
END FOR
```

**Testing Approach**: Property-based testing es recomendado para preservation checking porque:
- Genera muchos casos de prueba automáticamente
- Detecta casos edge que tests manuales podrían omitir
- Proporciona garantías fuertes de que el comportamiento no cambió

**Test Plan**: Observar comportamiento en código SIN CORREGIR primero para funcionalidades no afectadas, luego escribir property-based tests capturando ese comportamiento.

**Test Cases**:

1. **Preservación de Formularios**:
   - Verificar que la validación de datos en todos los formularios continúa funcionando igual
   - Verificar que el envío de formularios válidos continúa guardando datos correctamente
   - Verificar que los mensajes de error continúan mostrándose apropiadamente

2. **Preservación de Listas y Filtros**:
   - Verificar que las listas de doctores, medicamentos, servicios y exámenes se muestran igual
   - Verificar que los filtros y búsquedas continúan funcionando correctamente

3. **Preservación de Dashboard de Paciente**:
   - Verificar que las tabs de historial clínico (consultas, signos vitales, recetas, laboratorio) continúan funcionando
   - Verificar que la carga de datos continúa funcionando correctamente

4. **Preservación de Gestión de Doctores**:
   - Verificar que la creación, edición y desactivación de doctores continúa funcionando
   - Verificar que la gestión de días libres continúa funcionando

### Unit Tests

**Bug 1 - Navegación**:
- Test que verifica que EmployeeFormPage renderiza flecha simple
- Test que verifica que el botón navega a `/administrator`
- Test que verifica que DoctorManagementPage no tiene botones duplicados
- Test que verifica que el botón "VOLVER A LA LISTA" aparece solo cuando viewMode !== 'list'

**Bug 2 - Asteriscos**:
- Test que verifica que cada campo obligatorio en DoctorForm tiene asterisco rojo
- Test que verifica que el asterisco está dentro de un `<span className="text-red-500">*</span>`

**Bug 3 - Citas**:
- Test unitario del endpoint que verifica el mapeo userId → patientId
- Test que verifica que las citas se ordenan por fecha y hora
- Test que verifica el manejo de error cuando no existe registro de Patient

### Property-Based Tests

**Navegación**:
- Generar diferentes estados de páginas de mantenimiento y verificar que todas usan el mismo patrón de navegación
- Generar diferentes viewModes en DoctorManagementPage y verificar que los botones aparecen correctamente

**Formularios**:
- Generar datos de formulario aleatorios y verificar que la validación continúa funcionando igual
- Generar diferentes combinaciones de campos y verificar que los asteriscos aparecen en todos los obligatorios

**Citas**:
- Generar usuarios PATIENT aleatorios con diferentes cantidades de citas y verificar que todas se retornan
- Generar usuarios con otros roles y verificar que el endpoint maneja correctamente los casos edge

### Integration Tests

**Flujo completo de navegación**:
- Test que navega desde dashboard de administrador a cada página de mantenimiento y verifica el botón de volver
- Test que verifica que al hacer clic en volver se regresa al dashboard

**Flujo completo de formulario de doctor**:
- Test que abre DoctorForm, verifica asteriscos, completa el formulario y verifica que se guarda correctamente

**Flujo completo de citas de paciente**:
- Test que crea un usuario PATIENT, crea citas, inicia sesión y verifica que las citas aparecen en el dashboard
- Test que verifica que las citas se ordenan correctamente por fecha y hora
