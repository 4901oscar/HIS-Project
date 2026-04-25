# Implementation Plan

## Bug 1: Navegación Inconsistente

- [x] 1. Write bug condition exploration test - Navegación inconsistente
  - **Property 1: Bug Condition** - Navegación inconsistente en páginas de mantenimiento
  - **IMPORTANTE**: Escribir este test basado en propiedades ANTES de implementar la corrección
  - **OBJETIVO**: Demostrar que existen patrones de navegación inconsistentes entre las páginas de mantenimiento
  - **Enfoque PBT Acotado**: Acotar la propiedad a casos concretos que fallan: EmployeeFormPage muestra "← Gestión de Personal", DoctorManagementPage tiene botón duplicado
  - Test que verifica que EmployeeFormPage muestra "← Gestión de Personal" en lugar de flecha simple (de Bug Condition en diseño)
  - Test que verifica que DoctorManagementPage en modo 'list' tiene botón "VINCULAR DOCTOR" duplicado (de Bug Condition en diseño)
  - Test que verifica que ServiciosPage, ExamenesPage y MedicamentosPage usan flecha simple (comportamiento correcto)
  - Ejecutar test en código SIN CORREGIR
  - **RESULTADO ESPERADO**: Test FALLA (esto es correcto - demuestra que el bug existe)
  - Documentar contraejemplos encontrados (ej: "EmployeeFormPage usa texto '← Gestión de Personal' en lugar de flecha simple")
  - Marcar tarea completa cuando el test esté escrito, ejecutado y la falla documentada
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 2. Write preservation property tests - Navegación general (ANTES de implementar corrección)
  - **Property 2: Preservation** - Funcionalidad de navegación existente
  - **IMPORTANTE**: Seguir metodología de observación primero
  - Observar: El sistema mantiene el estado de autenticación al navegar entre secciones
  - Observar: Los botones de cancelar en formularios descartan cambios y vuelven a la vista anterior
  - Escribir test basado en propiedades: para todas las navegaciones que NO involucran botones de volver en mantenimientos, el comportamiento debe permanecer igual (de Preservation Requirements en diseño)
  - Verificar que el test pasa en código SIN CORREGIR
  - _Requirements: 7.1, 7.2_

- [x] 3. Corrección de navegación inconsistente

  - [x] 3.1 Simplificar botón de volver en EmployeeFormPage
    - Abrir archivo `frontend-medflow/src/pages/administrator/EmployeeFormPage.tsx`
    - Reemplazar botón con texto "← Gestión de Personal" por flecha simple SVG
    - Cambiar navegación de `/administrator/empleados` a `/administrator`
    - Usar el mismo patrón de flecha que ServiciosPage, ExamenesPage y MedicamentosPage
    - _Bug_Condition: isBugCondition1(input) donde input.page === 'EmployeeFormPage' AND buttonText === '← Gestión de Personal'_
    - _Expected_Behavior: Botón de volver SHALL ser flecha simple que navega a `/administrator` (Property 1 del diseño)_
    - _Preservation: Navegación general y funcionalidad de formularios (Requirements 7.1, 7.2, 8.1-8.3)_
    - _Requirements: 1.1, 4.1_

  - [x] 3.2 Eliminar botón duplicado en DoctorManagementPage
    - Abrir archivo `frontend-medflow/src/pages/administrator/DoctorManagementPage.tsx`
    - Remover el botón "VINCULAR DOCTOR" del header cuando viewMode === 'list'
    - El botón ya existe en el componente DoctorList, mantener solo ese
    - Simplificar lógica: cuando viewMode === 'list' mostrar flecha simple a `/administrator`
    - Cuando viewMode !== 'list' mostrar "← VOLVER A LA LISTA" que ejecuta handleCancel
    - _Bug_Condition: isBugCondition1(input) donde input.page === 'DoctorManagementPage' AND input.viewMode === 'list' AND duplicateButtonExists_
    - _Expected_Behavior: Botón "VINCULAR DOCTOR" SHALL aparecer solo una vez en el header cuando viewMode === 'list' (Property 1 del diseño)_
    - _Preservation: Gestión de doctores (Requirements 10.1, 10.2)_
    - _Requirements: 1.2, 4.2, 4.3_

  - [x] 3.3 Verificar que el test de bug condition ahora pasa
    - **Property 1: Expected Behavior** - Navegación uniforme en mantenimientos
    - **IMPORTANTE**: Re-ejecutar el MISMO test del paso 1 - NO escribir un nuevo test
    - El test del paso 1 codifica el comportamiento esperado
    - Cuando este test pasa, confirma que el comportamiento esperado está satisfecho
    - Ejecutar test de exploración de bug condition del paso 1
    - **RESULTADO ESPERADO**: Test PASA (confirma que el bug está corregido)
    - _Requirements: 4.1, 4.2, 4.3_

  - [x] 3.4 Verificar que los tests de preservación siguen pasando
    - **Property 2: Preservation** - Funcionalidad de navegación existente
    - **IMPORTANTE**: Re-ejecutar los MISMOS tests del paso 2 - NO escribir nuevos tests
    - Ejecutar tests de preservación basados en propiedades del paso 2
    - **RESULTADO ESPERADO**: Tests PASAN (confirma que no hay regresiones)
    - Confirmar que todos los tests siguen pasando después de la corrección

- [x] 4. Checkpoint - Asegurar que todos los tests de Bug 1 pasan
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas

## Bug 2: Campos Obligatorios Sin Asterisco

- [x] 5. Write bug condition exploration test - Campos sin asterisco
  - **Property 1: Bug Condition** - Campos obligatorios sin indicador visual en DoctorForm
  - **CRÍTICO**: Este test DEBE FALLAR en código sin corregir - la falla confirma que el bug existe
  - **NO intentar corregir el test o el código cuando falle**
  - **NOTA**: Este test codifica el comportamiento esperado - validará la corrección cuando pase después de la implementación
  - **OBJETIVO**: Demostrar que los campos obligatorios no tienen asteriscos rojos
  - **Enfoque PBT Acotado**: Acotar la propiedad a los campos concretos que fallan: "Seleccionar Doctor", "Especialidad", "Hora de Inicio del Turno", "Hora de Fin del Turno"
  - Test que verifica que el label "Seleccionar Doctor" NO contiene `<span className="text-red-500">*</span>` en modo creación (de Bug Condition en diseño)
  - Test que verifica que los labels "Especialidad", "Hora de Inicio del Turno" y "Hora de Fin del Turno" NO contienen asterisco rojo (de Bug Condition en diseño)
  - Las aserciones del test deben coincidir con las Expected Behavior Properties del diseño
  - Ejecutar test en código SIN CORREGIR
  - **RESULTADO ESPERADO**: Test FALLA (esto es correcto - demuestra que el bug existe)
  - Documentar contraejemplos encontrados para entender la causa raíz
  - Marcar tarea completa cuando el test esté escrito, ejecutado y la falla documentada
  - _Requirements: 2.1, 2.2_

- [x] 6. Write preservation property tests - Funcionalidad de formularios (ANTES de implementar corrección)
  - **Property 2: Preservation** - Funcionalidad existente de formularios
  - **IMPORTANTE**: Seguir metodología de observación primero
  - Observar: Los formularios validan datos correctamente en código sin corregir
  - Observar: El envío de formularios válidos guarda datos correctamente
  - Observar: Los mensajes de error de validación se muestran apropiadamente
  - Escribir tests basados en propiedades capturando patrones de comportamiento observados de Preservation Requirements
  - Los tests basados en propiedades generan muchos casos de prueba para garantías más fuertes
  - Ejecutar tests en código SIN CORREGIR
  - **RESULTADO ESPERADO**: Tests PASAN (esto confirma el comportamiento base a preservar)
  - Marcar tarea completa cuando los tests estén escritos, ejecutados y pasando en código sin corregir
  - _Requirements: 8.1, 8.2, 8.3_

- [x] 7. Corrección de campos obligatorios sin asterisco

  - [x] 7.1 Agregar asteriscos rojos en DoctorForm
    - Abrir archivo `frontend-medflow/src/components/DoctorForm/DoctorForm.tsx`
    - Agregar `<span className="text-red-500">*</span>` al label "Seleccionar Doctor" (solo en modo creación)
    - Agregar `<span className="text-red-500">*</span>` al label "Especialidad"
    - Agregar `<span className="text-red-500">*</span>` al label "Hora de Inicio del Turno"
    - Agregar `<span className="text-red-500">*</span>` al label "Hora de Fin del Turno"
    - Seguir el mismo patrón usado en otros formularios del sistema (ej: EmployeeFormPage)
    - _Bug_Condition: isBugCondition2(input) donde input.component === 'DoctorForm' AND input.fieldName IN campos obligatorios AND NOT hasRedAsterisk(input.fieldName)_
    - _Expected_Behavior: Labels SHALL mostrar `<span className="text-red-500">*</span>` después del texto (Property 2 del diseño)_
    - _Preservation: Funcionalidad de formularios (Requirements 8.1-8.3)_
    - _Requirements: 2.1, 2.2, 5.1, 5.2, 5.3_

  - [x] 7.2 Verificar que el test de bug condition ahora pasa
    - **Property 1: Expected Behavior** - Indicadores visuales en DoctorForm
    - **IMPORTANTE**: Re-ejecutar el MISMO test del paso 5 - NO escribir un nuevo test
    - El test del paso 5 codifica el comportamiento esperado
    - Cuando este test pasa, confirma que el comportamiento esperado está satisfecho
    - Ejecutar test de exploración de bug condition del paso 5
    - **RESULTADO ESPERADO**: Test PASA (confirma que el bug está corregido)
    - _Requirements: 5.1, 5.2, 5.3_

  - [x] 7.3 Verificar que los tests de preservación siguen pasando
    - **Property 2: Preservation** - Funcionalidad existente de formularios
    - **IMPORTANTE**: Re-ejecutar los MISMOS tests del paso 6 - NO escribir nuevos tests
    - Ejecutar tests de preservación basados en propiedades del paso 6
    - **RESULTADO ESPERADO**: Tests PASAN (confirma que no hay regresiones)
    - Confirmar que todos los tests siguen pasando después de la corrección

- [x] 8. Checkpoint - Asegurar que todos los tests de Bug 2 pasan
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas

## Bug 3: Citas No Visibles para Pacientes

- [x] 9. Write bug condition exploration test - Citas no aparecen
  - **Property 1: Bug Condition** - Citas no visibles para usuarios PATIENT
  - **IMPORTANTE**: Escribir este test basado en propiedades ANTES de implementar la corrección
  - **OBJETIVO**: Demostrar que el endpoint retorna lista vacía aunque el paciente tenga citas
  - **Enfoque PBT Acotado**: Acotar la propiedad al caso concreto que falla: usuario PATIENT con citas registradas recibe lista vacía
  - Test que crea usuario PATIENT con userId="user123" y patientId="patient456"
  - Test que crea 2 citas asociadas a patientId="patient456"
  - Test que llama al endpoint `/api/clinical/appointments/my` con token de userId="user123"
  - Test que verifica que retorna lista vacía (de Bug Condition en diseño)
  - Ejecutar test en código SIN CORREGIR
  - **RESULTADO ESPERADO**: Test FALLA (esto es correcto - demuestra que el bug existe)
  - Documentar contraejemplos encontrados (ej: "Endpoint busca por userId en lugar de patientId")
  - Marcar tarea completa cuando el test esté escrito, ejecutado y la falla documentada
  - _Requirements: 3.1, 3.2_

- [x] 10. Write preservation property tests - Visualización de datos (ANTES de implementar corrección)
  - **Property 2: Preservation** - Funcionalidad de visualización existente
  - **IMPORTANTE**: Seguir metodología de observación primero
  - Observar: Las listas de registros se muestran correctamente en código sin corregir
  - Observar: Los filtros y búsquedas funcionan correctamente
  - Observar: El historial clínico en PatientDashboard se muestra correctamente
  - Escribir tests basados en propiedades capturando patrones de comportamiento observados de Preservation Requirements
  - Los tests basados en propiedades generan muchos casos de prueba para garantías más fuertes
  - Ejecutar tests en código SIN CORREGIR
  - **RESULTADO ESPERADO**: Tests PASAN (esto confirma el comportamiento base a preservar)
  - Marcar tarea completa cuando los tests estén escritos, ejecutados y pasando en código sin corregir
  - _Requirements: 9.1, 9.2, 9.3_

- [x] 11. Corrección de citas no visibles para pacientes

  - [x] 11.1 Modificar endpoint para buscar por patientId
    - Localizar el endpoint `/api/clinical/appointments/my` en el backend (probablemente en AppointmentController.java)
    - Obtener userId del token JWT autenticado
    - Buscar el registro de Patient asociado a ese userId usando patientRepository.findByUserId(userId)
    - Consultar citas usando el patientId obtenido: appointmentRepository.findByPatientId(patient.getId())
    - Ordenar las citas por fecha y hora: appointments.sort(Comparator.comparing(Appointment::getAppointmentDate).thenComparing(Appointment::getAppointmentTime))
    - Agregar manejo de errores: si no existe registro de Patient, retornar lista vacía o error 404
    - Validar que el usuario tenga rol PATIENT
    - _Bug_Condition: isBugCondition3(input) donde input.userRole === 'PATIENT' AND input.hasAppointments === true AND appointmentListIsEmpty() AND endpointSearchesByUserId()_
    - _Expected_Behavior: Endpoint SHALL retornar citas asociadas al patientId correspondiente al userId autenticado (Property 3 del diseño)_
    - _Preservation: Visualización de datos (Requirements 9.1-9.3)_
    - _Requirements: 3.1, 3.2, 6.1, 6.2_

  - [x] 11.2 Verificar que el test de bug condition ahora pasa
    - **Property 1: Expected Behavior** - Citas visibles para pacientes
    - **IMPORTANTE**: Re-ejecutar el MISMO test del paso 9 - NO escribir un nuevo test
    - El test del paso 9 codifica el comportamiento esperado
    - Cuando este test pasa, confirma que el comportamiento esperado está satisfecho
    - Ejecutar test de exploración de bug condition del paso 9
    - **RESULTADO ESPERADO**: Test PASA (confirma que el bug está corregido)
    - _Requirements: 6.1, 6.2_

  - [x] 11.3 Verificar que los tests de preservación siguen pasando
    - **Property 2: Preservation** - Funcionalidad de visualización existente
    - **IMPORTANTE**: Re-ejecutar los MISMOS tests del paso 10 - NO escribir nuevos tests
    - Ejecutar tests de preservación basados en propiedades del paso 10
    - **RESULTADO ESPERADO**: Tests PASAN (confirma que no hay regresiones)
    - Confirmar que todos los tests siguen pasando después de la corrección

- [x] 12. Checkpoint - Asegurar que todos los tests de Bug 3 pasan
  - Asegurar que todos los tests pasan, preguntar al usuario si surgen dudas

## Final Checkpoint

- [x] 13. Verificación final de todos los bugs corregidos
  - Ejecutar suite completa de tests para los 3 bugs
  - Verificar que todos los tests de bug condition pasan (navegación, asteriscos, citas)
  - Verificar que todos los tests de preservación pasan (sin regresiones)
  - Realizar pruebas manuales de integración si es necesario
  - Documentar cualquier hallazgo o consideración adicional
