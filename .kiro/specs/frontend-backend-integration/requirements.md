# Requirements Document: Frontend-Backend Integration

## Introduction

Este documento define los requisitos para actualizar y mejorar el frontend de MedFlow para que tenga correlación completa y se integre correctamente con el backend de microservicios implementado. El frontend React actual tiene servicios con código Axios comentado y tipos TypeScript que no coinciden completamente con los DTOs del backend. Este spec asegura que el frontend pueda comunicarse exitosamente con el API Gateway y los microservicios.

**Flujos principales del sistema:**
1. **Registro de pacientes en línea**: Los pacientes pueden auto-registrarse proporcionando sus datos demográficos completos
2. **Creación de citas por pacientes**: Los pacientes pueden crear citas en línea (requiere registro/login + pago)
3. **Portal del paciente**: Los pacientes pueden ver su historial de citas y exámenes médicos
4. **Gestión por recepción**: El personal de admisión puede registrar pacientes y crear citas manualmente

## Glossary

- **Frontend**: Aplicación React + TypeScript + Vite que corre en localhost:3000
- **API_Gateway**: Punto de entrada único en localhost:8080 que enruta peticiones a microservicios
- **Auth_Service**: Microservicio de autenticación (puerto 8081) que maneja login/logout con JWT
- **Patient_Service**: Microservicio de gestión de pacientes (puerto 8082)
- **Clinical_Service**: Microservicio de gestión clínica (puerto 8083) con arquitectura hexagonal
- **JWT**: JSON Web Token usado para autenticación stateless
- **DTO**: Data Transfer Object - estructura de datos del backend
- **Axios_Interceptor**: Middleware que intercepta peticiones HTTP para agregar headers automáticamente
- **TypeScript_Type**: Definición de tipos en TypeScript que debe coincidir con DTOs del backend
- **Patient_Portal**: Área del frontend donde pacientes registrados pueden ver su información médica
- **Public_Registration**: Proceso de auto-registro de pacientes sin intervención de personal
- **Appointment_Booking**: Proceso de creación de cita que requiere registro/login + pago en línea

## Requirements

### Requirement 1: Configuración de Axios con API Gateway

**User Story:** Como desarrollador frontend, quiero que todos los servicios apunten al API Gateway, para tener un punto de entrada único y simplificar la configuración.

#### Acceptance Criteria

1. THE Frontend SHALL configurar Axios con baseURL apuntando a `http://localhost:8080/api`
2. THE Frontend SHALL leer la baseURL desde variable de entorno `VITE_API_URL`
3. WHEN la variable de entorno no existe, THE Frontend SHALL usar `http://localhost:8080/api` como valor por defecto
4. THE Frontend SHALL crear un archivo `.env` con la configuración correcta
5. THE Frontend SHALL documentar las variables de entorno en `.env.example`

### Requirement 2: Interceptor JWT para Autenticación

**User Story:** Como sistema de autenticación, quiero que todas las peticiones HTTP incluyan automáticamente el token JWT, para que el API Gateway pueda validar la autenticación.

#### Acceptance Criteria

1. THE Frontend SHALL crear un Axios interceptor que agregue el header `Authorization: Bearer <token>` a todas las peticiones
2. WHEN el token no existe en localStorage, THE Axios_Interceptor SHALL omitir el header Authorization
3. WHEN el token existe en localStorage, THE Axios_Interceptor SHALL incluir el token en todas las peticiones
4. THE Axios_Interceptor SHALL aplicarse a todos los servicios (authService, patientService, appointmentService)
5. WHEN una petición retorna 401 Unauthorized, THE Frontend SHALL redirigir al usuario a la página de login

### Requirement 3: Integración del Auth Service

**User Story:** Como usuario del sistema, quiero poder iniciar sesión con mis credenciales de empleado, para acceder a las funcionalidades según mi rol.

#### Acceptance Criteria

1. WHEN el usuario envía credenciales válidas, THE Auth_Service SHALL retornar un token JWT y datos del usuario
2. THE Frontend SHALL enviar petición POST a `/api/auth/login` con `{username, password}`
3. THE Frontend SHALL almacenar el token JWT en localStorage con key `auth_token`
4. THE Frontend SHALL almacenar los datos del usuario en localStorage con key `user_data`
5. THE Frontend SHALL actualizar el AuthContext con los datos del usuario recibidos
6. WHEN el login falla, THE Frontend SHALL mostrar mensaje de error claro al usuario
7. THE Frontend SHALL manejar errores de red (timeout, conexión rechazada) con mensajes apropiados

### Requirement 4: Alineación de Tipos TypeScript con DTOs del Backend

**User Story:** Como desarrollador frontend, quiero que los tipos TypeScript coincidan exactamente con los DTOs del backend, para evitar errores de tipo y facilitar el desarrollo.

#### Acceptance Criteria

1. THE Frontend SHALL definir tipo `LoginRequest` que coincida con `com.medflow.auth.dto.LoginRequest`
2. THE Frontend SHALL definir tipo `LoginResponse` que coincida con `com.medflow.auth.dto.LoginResponse`
3. THE Frontend SHALL definir tipo `UserResponse` que coincida con `com.medflow.auth.dto.UserResponse`
4. THE Frontend SHALL definir tipo `CreatePatientRequest` que coincida con `com.medflow.patient.dto.CreatePatientRequest`
5. THE Frontend SHALL definir tipo `PatientResponse` que coincida con `com.medflow.patient.dto.PatientResponse`
6. THE Frontend SHALL definir tipo `CreateAppointmentRequest` que coincida con `CreateAppointmentRequest` del Clinical Service
7. THE Frontend SHALL definir tipo `AppointmentResponse` que coincida con `AppointmentResponse` del Clinical Service
8. THE Frontend SHALL usar enums TypeScript para `Gender` (MALE, FEMALE, OTHER) que coincidan con el backend

### Requirement 5: Integración del Patient Service

**User Story:** Como personal de admisión, quiero registrar nuevos pacientes desde el frontend, para que sus datos se almacenen en el backend.

#### Acceptance Criteria

1. THE Frontend SHALL enviar petición POST a `/api/patients` con datos del paciente
2. THE Frontend SHALL incluir todos los campos requeridos: dpi, firstName, firstLastName, birthDate, gender, email, phone
3. WHEN el registro es exitoso, THE Frontend SHALL mostrar mensaje de éxito con el ID del paciente creado
4. WHEN el DPI está duplicado, THE Frontend SHALL mostrar error "DPI ya registrado"
5. WHEN el email está duplicado, THE Frontend SHALL mostrar error "Email ya registrado"
6. THE Frontend SHALL validar formato de DPI (13 dígitos) antes de enviar
7. THE Frontend SHALL validar formato de teléfono (8 dígitos) antes de enviar
8. THE Frontend SHALL validar formato de email antes de enviar

### Requirement 5.1: Registro Público de Pacientes (Auto-registro)

**User Story:** Como paciente, quiero poder registrarme en línea sin ayuda de recepción, para poder crear citas médicas posteriormente.

#### Acceptance Criteria

1. THE Frontend SHALL proporcionar una página pública de registro accesible sin autenticación
2. THE Frontend SHALL solicitar todos los datos demográficos requeridos: DPI, nombre completo, fecha de nacimiento, género, dirección, teléfono, email
3. THE Frontend SHALL solicitar contraseña y confirmación de contraseña
4. THE Frontend SHALL validar que la contraseña tenga al menos 8 caracteres, 1 mayúscula, 1 minúscula y 1 número
5. THE Frontend SHALL validar que contraseña y confirmación coincidan
6. THE Frontend SHALL enviar petición POST a `/api/patients/register` con datos del paciente y contraseña
7. WHEN el registro es exitoso, THE Frontend SHALL mostrar mensaje "Registro exitoso. Ya puedes iniciar sesión"
8. WHEN el registro es exitoso, THE Frontend SHALL redirigir automáticamente a la página de login
9. THE Frontend SHALL manejar errores de duplicación (DPI o email) con mensajes claros

### Requirement 5.2: Login de Pacientes

**User Story:** Como paciente registrado, quiero poder iniciar sesión con mi email y contraseña, para acceder a mi portal de paciente.

#### Acceptance Criteria

1. THE Frontend SHALL permitir login con email y contraseña (no con DPI)
2. THE Frontend SHALL enviar petición POST a `/api/auth/login` con `{username: email, password}`
3. WHEN el login es exitoso, THE Frontend SHALL almacenar el token JWT y datos del usuario
4. WHEN el login es exitoso, THE Frontend SHALL redirigir al portal del paciente
5. THE Frontend SHALL distinguir entre usuarios tipo "PATIENT" y usuarios tipo "EMPLOYEE" (staff)
6. WHEN un paciente intenta acceder a rutas de staff, THE Frontend SHALL mostrar error "Acceso denegado"

### Requirement 6: Búsqueda de Pacientes

**User Story:** Como personal del hospital, quiero buscar pacientes por DPI, nombre o email, para encontrar rápidamente sus registros.

#### Acceptance Criteria

1. THE Frontend SHALL enviar petición GET a `/api/patients/search?query={searchTerm}`
2. WHEN el usuario escribe en el campo de búsqueda, THE Frontend SHALL enviar la petición después de 500ms de inactividad (debounce)
3. THE Frontend SHALL mostrar los resultados en una tabla con: ID, DPI, nombre completo, fecha de nacimiento, teléfono
4. WHEN no hay resultados, THE Frontend SHALL mostrar mensaje "No se encontraron pacientes"
5. THE Frontend SHALL limitar los resultados mostrados a 50 pacientes
6. THE Frontend SHALL manejar errores de búsqueda con mensajes apropiados

### Requirement 7: Integración del Clinical Service - Citas

**User Story:** Como personal de admisión, quiero gestionar citas médicas desde el frontend, para coordinar la atención de pacientes.

#### Acceptance Criteria

1. THE Frontend SHALL enviar petición GET a `/api/clinical/appointments` para obtener lista de citas
2. THE Frontend SHALL enviar petición POST a `/api/clinical/appointments` para crear nueva cita
3. THE Frontend SHALL enviar petición POST a `/api/clinical/appointments/{id}/activate` para activar una cita
4. THE Frontend SHALL incluir campos requeridos al crear cita: patientId, doctorId, appointmentDate, appointmentTime
5. WHEN una cita se activa exitosamente, THE Frontend SHALL actualizar el estado visual de la cita
6. THE Frontend SHALL mostrar estados de cita con badges de colores: PENDING (amarillo), ACTIVATED (verde), CANCELLED (rojo)
7. THE Frontend SHALL validar que appointmentDate sea una fecha futura antes de enviar

### Requirement 7.1: Creación de Citas por Pacientes (Booking Online)

**User Story:** Como paciente, quiero poder crear citas médicas en línea, para agendar mi consulta sin tener que llamar o ir al hospital.

#### Acceptance Criteria

1. THE Frontend SHALL proporcionar una página pública de creación de citas accesible sin autenticación inicial
2. THE Frontend SHALL solicitar: fecha de cita, hora de cita, descripción del motivo de consulta
3. THE Frontend SHALL mostrar calendario con fechas disponibles
4. THE Frontend SHALL mostrar horarios disponibles para la fecha seleccionada
5. WHEN el paciente NO está autenticado, THE Frontend SHALL guardar temporalmente los datos de la cita
6. WHEN el paciente NO está autenticado, THE Frontend SHALL mostrar opciones: "Iniciar sesión" o "Registrarse"
7. WHEN el paciente selecciona "Registrarse", THE Frontend SHALL mostrar formulario de registro
8. WHEN el paciente completa el registro, THE Frontend SHALL hacer login automático
9. WHEN el paciente está autenticado, THE Frontend SHALL mostrar pantalla de pago
10. WHEN el pago es exitoso, THE Frontend SHALL crear la cita enviando POST a `/api/clinical/appointments`
11. WHEN la cita se crea exitosamente, THE Frontend SHALL mostrar confirmación con número de cita
12. THE Frontend SHALL enviar email de confirmación al paciente (si el backend lo soporta)

### Requirement 7.2: Portal del Paciente - Mis Citas

**User Story:** Como paciente autenticado, quiero ver todas mis citas médicas, para saber cuándo tengo consultas programadas.

#### Acceptance Criteria

1. THE Frontend SHALL proporcionar una página "Mis Citas" accesible solo para pacientes autenticados
2. THE Frontend SHALL enviar petición GET a `/api/clinical/appointments/my-appointments` para obtener citas del paciente
3. THE Frontend SHALL mostrar lista de citas con: fecha, hora, descripción, estado, doctor (si aplica)
4. THE Frontend SHALL permitir filtrar citas por estado: Todas, Pendientes, Completadas, Canceladas
5. THE Frontend SHALL permitir cancelar citas pendientes
6. WHEN el paciente cancela una cita, THE Frontend SHALL enviar petición POST a `/api/clinical/appointments/{id}/cancel`
7. THE Frontend SHALL mostrar mensaje de confirmación antes de cancelar

### Requirement 7.3: Portal del Paciente - Mi Historial Médico

**User Story:** Como paciente autenticado, quiero ver mi historial médico (exámenes, resultados), para tener acceso a mi información de salud.

#### Acceptance Criteria

1. THE Frontend SHALL proporcionar una página "Mi Historial" accesible solo para pacientes autenticados
2. THE Frontend SHALL enviar petición GET a `/api/clinical/medical-records/my-records` para obtener historial del paciente
3. THE Frontend SHALL mostrar lista de consultas pasadas con: fecha, doctor, diagnóstico, tratamiento
4. THE Frontend SHALL mostrar lista de exámenes de laboratorio con: fecha, tipo de examen, estado, resultados (si disponibles)
5. THE Frontend SHALL permitir descargar resultados de exámenes en PDF
6. WHEN no hay historial disponible, THE Frontend SHALL mostrar mensaje "No tienes historial médico aún"

### Requirement 7.4: Sistema de Pago de Citas

**User Story:** Como paciente, quiero pagar mi cita en línea de forma segura, para confirmar mi reserva sin tener que ir al hospital.

#### Acceptance Criteria

1. THE Frontend SHALL mostrar pantalla de pago después de que el paciente esté autenticado
2. THE Frontend SHALL mostrar resumen de la cita: fecha, hora, descripción, monto a pagar
3. THE Frontend SHALL integrar con pasarela de pago (por definir: Stripe, PayPal, u otra)
4. THE Frontend SHALL solicitar información de pago: número de tarjeta, fecha de expiración, CVV, nombre del titular
5. THE Frontend SHALL validar formato de tarjeta de crédito antes de enviar
6. THE Frontend SHALL enviar petición POST a `/api/billing/payments` con datos de pago y appointmentId
7. WHEN el pago es exitoso, THE Frontend SHALL crear la cita automáticamente
8. WHEN el pago es exitoso, THE Frontend SHALL mostrar confirmación con número de cita y recibo
9. WHEN el pago falla, THE Frontend SHALL mostrar mensaje de error claro y permitir reintentar
10. THE Frontend SHALL permitir descargar recibo de pago en PDF
11. THE Frontend SHALL enviar email con recibo de pago (si el backend lo soporta)

### Requirement 8: Manejo de Errores Consistente

**User Story:** Como usuario del sistema, quiero ver mensajes de error claros y consistentes, para entender qué salió mal y cómo solucionarlo.

#### Acceptance Criteria

1. WHEN una petición retorna 400 Bad Request, THE Frontend SHALL mostrar los errores de validación específicos de cada campo
2. WHEN una petición retorna 401 Unauthorized, THE Frontend SHALL redirigir al login y mostrar mensaje "Sesión expirada"
3. WHEN una petición retorna 403 Forbidden, THE Frontend SHALL mostrar mensaje "No tienes permisos para esta acción"
4. WHEN una petición retorna 404 Not Found, THE Frontend SHALL mostrar mensaje "Recurso no encontrado"
5. WHEN una petición retorna 409 Conflict, THE Frontend SHALL mostrar el mensaje de conflicto específico (ej: "DPI duplicado")
6. WHEN una petición retorna 429 Too Many Requests, THE Frontend SHALL mostrar mensaje "Demasiadas peticiones, intenta más tarde"
7. WHEN una petición retorna 500 Internal Server Error, THE Frontend SHALL mostrar mensaje "Error del servidor, contacta soporte"
8. WHEN hay error de red (timeout, conexión rechazada), THE Frontend SHALL mostrar mensaje "Error de conexión, verifica tu red"

### Requirement 9: Logout y Limpieza de Sesión

**User Story:** Como usuario del sistema, quiero poder cerrar sesión correctamente, para que mis datos no queden expuestos.

#### Acceptance Criteria

1. WHEN el usuario hace click en logout, THE Frontend SHALL enviar petición POST a `/api/auth/logout`
2. THE Frontend SHALL incluir el token JWT en el header Authorization de la petición de logout
3. THE Frontend SHALL limpiar localStorage (remover `auth_token` y `user_data`)
4. THE Frontend SHALL actualizar el AuthContext a estado no autenticado
5. THE Frontend SHALL redirigir al usuario a la página de login
6. WHEN la petición de logout falla, THE Frontend SHALL limpiar localStorage de todas formas
7. THE Frontend SHALL mostrar mensaje de confirmación "Sesión cerrada exitosamente"

### Requirement 10: Variables de Entorno y Configuración

**User Story:** Como desarrollador, quiero configurar fácilmente las URLs del backend mediante variables de entorno, para poder cambiar entre desarrollo, staging y producción.

#### Acceptance Criteria

1. THE Frontend SHALL leer `VITE_API_URL` desde archivo `.env`
2. THE Frontend SHALL proporcionar archivo `.env.example` con todas las variables necesarias
3. THE Frontend SHALL documentar cada variable de entorno en `.env.example`
4. THE Frontend SHALL usar valores por defecto sensatos cuando las variables no están definidas
5. THE Frontend SHALL validar que `VITE_API_URL` termine sin slash (/) para evitar URLs malformadas

### Requirement 11: Loading States y UX

**User Story:** Como usuario del sistema, quiero ver indicadores visuales cuando se están cargando datos, para saber que el sistema está procesando mi petición.

#### Acceptance Criteria

1. WHEN una petición HTTP está en progreso, THE Frontend SHALL mostrar un indicador de loading
2. THE Frontend SHALL deshabilitar botones de submit mientras una petición está en progreso
3. WHEN una petición tarda más de 3 segundos, THE Frontend SHALL mostrar mensaje "Cargando, por favor espera..."
4. THE Frontend SHALL usar spinners o skeletons apropiados para cada tipo de contenido
5. WHEN una petición se completa, THE Frontend SHALL ocultar el indicador de loading inmediatamente

### Requirement 12: Validación de Formularios

**User Story:** Como usuario del sistema, quiero que los formularios validen mis datos antes de enviarlos, para evitar errores y recibir feedback inmediato.

#### Acceptance Criteria

1. THE Frontend SHALL validar campos requeridos antes de permitir submit
2. THE Frontend SHALL validar formato de DPI (13 dígitos numéricos) en tiempo real
3. THE Frontend SHALL validar formato de teléfono (8 dígitos numéricos) en tiempo real
4. THE Frontend SHALL validar formato de email en tiempo real
5. THE Frontend SHALL validar que fechas de nacimiento no sean futuras
6. THE Frontend SHALL validar que el paciente tenga al menos 1 día de nacido
7. THE Frontend SHALL validar formato de contraseña (mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número)
8. THE Frontend SHALL validar que contraseña y confirmación coincidan
9. THE Frontend SHALL mostrar mensajes de error debajo de cada campo inválido
10. THE Frontend SHALL deshabilitar el botón de submit mientras haya errores de validación
11. THE Frontend SHALL mostrar indicador visual (checkmark verde) cuando un campo es válido

### Requirement 13: Gestión de Estado de Sesión

**User Story:** Como sistema, quiero mantener el estado de la sesión del usuario de forma segura, para proporcionar una experiencia fluida.

#### Acceptance Criteria

1. THE Frontend SHALL persistir el estado de autenticación en localStorage
2. THE Frontend SHALL verificar la validez del token JWT al cargar la aplicación
3. WHEN el token es válido, THE Frontend SHALL restaurar la sesión del usuario automáticamente
4. WHEN el token es inválido o expirado, THE Frontend SHALL limpiar localStorage y redirigir a login
5. THE Frontend SHALL distinguir entre sesiones de pacientes y sesiones de staff
6. THE Frontend SHALL redirigir a la página apropiada según el tipo de usuario (paciente → portal, staff → dashboard)
7. THE Frontend SHALL mantener la URL destino cuando redirige a login (para volver después de autenticarse)

### Requirement 14: Rutas Públicas vs Protegidas

**User Story:** Como sistema, quiero controlar el acceso a diferentes páginas según el estado de autenticación y rol del usuario.

#### Acceptance Criteria

1. THE Frontend SHALL definir rutas públicas accesibles sin autenticación:
   - `/` (home)
   - `/register` (registro de pacientes)
   - `/login` (login)
   - `/appointments/book` (inicio de creación de cita)
2. THE Frontend SHALL definir rutas protegidas para pacientes autenticados:
   - `/patient/dashboard` (portal del paciente)
   - `/patient/appointments` (mis citas)
   - `/patient/medical-records` (mi historial)
   - `/patient/profile` (mi perfil)
3. THE Frontend SHALL definir rutas protegidas para staff autenticado:
   - `/dashboard` (dashboard de staff)
   - `/admission/*` (módulo de admisión)
   - `/vitals/*` (módulo de signos vitales)
   - `/doctor/*` (módulo de doctor)
   - `/lab/*` (módulo de laboratorio)
   - `/pharmacy/*` (módulo de farmacia)
   - `/cashier/*` (módulo de caja)
4. WHEN un usuario no autenticado intenta acceder a ruta protegida, THE Frontend SHALL redirigir a login
5. WHEN un paciente intenta acceder a ruta de staff, THE Frontend SHALL mostrar error 403 "Acceso denegado"
6. WHEN un staff intenta acceder a ruta de paciente, THE Frontend SHALL mostrar error 403 "Acceso denegado"

## Special Requirements Guidance

### Parser and Serializer Requirements

Este spec no requiere parsers o serializers personalizados. La serialización JSON es manejada automáticamente por Axios y el backend Spring Boot.

### Round-Trip Properties

No aplica para este spec, ya que no estamos implementando parsers personalizados.

## Iteration and Feedback Rules

- El modelo DEBE hacer modificaciones si el usuario solicita cambios
- El modelo DEBE incorporar todo el feedback del usuario antes de proceder
- El modelo DEBE ofrecer volver a pasos anteriores si se identifican gaps

## Phase Completion

Después de completar este documento, el modelo DEBE detenerse. El usuario hará click en un botón en la UI para avanzar a la siguiente fase.

---

**Created**: 2026-04-15  
**Author**: MedFlow Team  
**Status**: Draft  
**Version**: 1.0.0
