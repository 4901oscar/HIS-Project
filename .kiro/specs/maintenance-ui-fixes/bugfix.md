# Bugfix Requirements Document

## Introduction

Este documento describe las correcciones necesarias para resolver tres problemas identificados en los módulos de mantenimiento del frontend:

1. **Navegación inconsistente**: Los diferentes mantenimientos (Empleados, Doctores, Medicamentos, Servicios, Exámenes) utilizan patrones de navegación diferentes para volver al dashboard de administrador
2. **Campos obligatorios sin indicador visual**: Algunos formularios no muestran el asterisco rojo (*) en todos los campos requeridos, causando confusión al usuario
3. **Citas no visibles para pacientes**: Los usuarios con rol PATIENT no pueden ver sus citas en el dashboard debido a un problema en la consulta de citas

Estos problemas afectan la experiencia de usuario y la consistencia de la interfaz.

## Bug Analysis

### Current Behavior (Defect)

#### 1. Navegación Inconsistente

1.1 WHEN el usuario está en EmployeeFormPage THEN el botón de volver muestra "← Gestión de Personal" y navega a `/administrator/empleados`

1.2 WHEN el usuario está en DoctorManagementPage con viewMode === 'list' THEN aparece el botón "VINCULAR DOCTOR" duplicado (uno en el header de la página y otro en DoctorList)

1.3 WHEN el usuario está en ServiciosPage, ExamenesPage o MedicamentosPage THEN el botón de volver es una flecha simple que navega a `/administrator`

#### 2. Campos Obligatorios Sin Asterisco

2.1 WHEN el usuario visualiza DoctorForm THEN los campos obligatorios "Seleccionar Doctor", "Especialidad", "Hora de Inicio del Turno" y "Hora de Fin del Turno" no muestran el asterisco rojo (*) en sus labels

2.2 WHEN el usuario visualiza otros formularios de mantenimiento THEN algunos campos obligatorios pueden no mostrar consistentemente el indicador visual de campo requerido

#### 3. Citas No Aparecen para Pacientes

3.1 WHEN un usuario con rol PATIENT inicia sesión y accede a PatientDashboard THEN la lista de citas aparece vacía aunque el paciente tenga citas registradas

3.2 WHEN el sistema llama a `listMyAppointments()` THEN el endpoint `/api/clinical/appointments/my` no retorna las citas del paciente porque busca por userId en lugar de patientId

### Expected Behavior (Correct)

#### 1. Navegación Uniforme

4.1 WHEN el usuario está en cualquier página de mantenimiento (EmployeeFormPage, DoctorManagementPage, ServiciosPage, ExamenesPage, MedicamentosPage) THEN el botón de volver SHALL ser una flecha simple que navega a `/administrator`

4.2 WHEN el usuario está en DoctorManagementPage con viewMode === 'list' THEN el botón "VINCULAR DOCTOR" SHALL aparecer solo una vez en el header de la página

4.3 WHEN el usuario está en DoctorManagementPage con viewMode !== 'list' THEN el botón de volver SHALL mostrar "← VOLVER A LA LISTA" y ejecutar handleCancel

#### 2. Indicadores Visuales Consistentes

5.1 WHEN el usuario visualiza DoctorForm en modo creación THEN el label "Seleccionar Doctor" SHALL mostrar `<span className="text-red-500">*</span>` después del texto

5.2 WHEN el usuario visualiza DoctorForm THEN los labels "Especialidad", "Hora de Inicio del Turno" y "Hora de Fin del Turno" SHALL mostrar `<span className="text-red-500">*</span>` después del texto

5.3 WHEN el usuario visualiza cualquier formulario de mantenimiento THEN todos los campos obligatorios SHALL mostrar consistentemente el asterisco rojo

#### 3. Citas Visibles para Pacientes

6.1 WHEN un usuario con rol PATIENT inicia sesión y accede a PatientDashboard THEN el sistema SHALL mostrar todas las citas del paciente ordenadas por fecha y hora

6.2 WHEN el sistema llama a `listMyAppointments()` THEN el endpoint `/api/clinical/appointments/my` SHALL retornar las citas asociadas al patientId correspondiente al userId autenticado

### Unchanged Behavior (Regression Prevention)

#### Navegación General

7.1 WHEN el usuario navega entre diferentes secciones del sistema THEN el sistema SHALL CONTINUE TO mantener el estado de autenticación y los datos del usuario

7.2 WHEN el usuario hace clic en botones de cancelar en formularios THEN el sistema SHALL CONTINUE TO descartar los cambios y volver a la vista anterior

#### Funcionalidad de Formularios

8.1 WHEN el usuario completa y envía formularios válidos THEN el sistema SHALL CONTINUE TO guardar los datos correctamente

8.2 WHEN el usuario ingresa datos inválidos en formularios THEN el sistema SHALL CONTINUE TO mostrar mensajes de validación apropiados

8.3 WHEN el usuario edita registros existentes THEN el sistema SHALL CONTINUE TO cargar y actualizar los datos correctamente

#### Visualización de Datos

9.1 WHEN el usuario visualiza listas de registros (doctores, medicamentos, servicios, exámenes) THEN el sistema SHALL CONTINUE TO mostrar todos los registros con sus datos completos

9.2 WHEN el usuario utiliza filtros y búsquedas THEN el sistema SHALL CONTINUE TO filtrar los resultados correctamente

9.3 WHEN el usuario visualiza el historial clínico en PatientDashboard (consultas, signos vitales, recetas, laboratorio) THEN el sistema SHALL CONTINUE TO mostrar los datos correctamente

#### Gestión de Doctores

10.1 WHEN el usuario crea, edita o desactiva doctores THEN el sistema SHALL CONTINUE TO ejecutar estas operaciones correctamente

10.2 WHEN el usuario gestiona días libres de doctores THEN el sistema SHALL CONTINUE TO funcionar correctamente
