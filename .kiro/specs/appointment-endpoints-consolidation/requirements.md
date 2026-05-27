# Requirements Document

## Introduction

Este documento especifica los requisitos para la refactorización de los endpoints de listado de citas en el Clinical Service. Actualmente existen 13 endpoints GET con alta redundancia y duplicación de código. El objetivo es consolidar endpoints redundantes, crear una estructura de datos unificada, y eliminar código duplicado, reduciendo de 13 a 8 endpoints mientras se mantiene toda la funcionalidad existente.

## Glossary

- **Clinical_Service**: Microservicio backend que gestiona citas médicas, triaje, y flujo clínico
- **Appointment**: Cita médica con información de paciente, doctor, fecha, hora, estado y pago
- **AppointmentController**: Controlador REST que expone endpoints de gestión de citas
- **Queue_Endpoint**: Endpoint que filtra citas por estado específico para mostrar colas de trabajo
- **Unified_DTO**: Estructura de datos única (AppointmentListItemResponse) que sirve para todos los portales
- **Payment_Status**: Estado de pago de una cita (PAID, PENDING, CANCELLED, NO_INVOICE, ERROR)
- **Portal**: Interfaz de usuario específica (admisión, caja, triaje, laboratorio, farmacia, doctor, paciente)
- **Filter_Parameter**: Query parameter que permite filtrar resultados por criterios específicos

## Requirements

### Requirement 1: Consolidar Endpoints de Cola por Estado

**User Story:** Como desarrollador backend, quiero consolidar los endpoints que solo filtran por estado, para reducir duplicación de código y simplificar el mantenimiento.

#### Acceptance Criteria

1. THE Clinical_Service SHALL eliminar el endpoint GET /appointments/payment-queue
2. THE Clinical_Service SHALL eliminar el endpoint GET /appointments/lab-queue
3. THE Clinical_Service SHALL eliminar el endpoint GET /appointments/pharmacy-queue
4. THE Clinical_Service SHALL eliminar el endpoint GET /appointments/pending-triage
5. WHEN se eliminen estos endpoints, THE Clinical_Service SHALL mantener toda la funcionalidad mediante el endpoint principal con filtros

### Requirement 2: Endpoint Principal con Filtros Flexibles

**User Story:** Como desarrollador frontend, quiero un endpoint principal con query parameters flexibles, para poder filtrar citas sin necesidad de múltiples endpoints específicos.

#### Acceptance Criteria

1. THE Clinical_Service SHALL modificar GET /appointments para aceptar query parameters opcionales
2. WHEN se proporcione el parámetro status, THE Clinical_Service SHALL filtrar citas por uno o múltiples estados
3. WHEN se proporcione el parámetro date, THE Clinical_Service SHALL filtrar citas por fecha específica
4. WHEN se proporcione el parámetro queue, THE Clinical_Service SHALL filtrar citas por tipo de cola (payment, lab, pharmacy, triage)
5. WHEN no se proporcionen filtros, THE Clinical_Service SHALL retornar todas las citas
6. WHEN se proporcionen múltiples filtros, THE Clinical_Service SHALL aplicar operación AND entre ellos

### Requirement 3: Estructura de Datos Unificada

**User Story:** Como desarrollador frontend, quiero una estructura de datos consistente en todos los endpoints de listado, para reutilizar componentes de UI y simplificar el código cliente.

#### Acceptance Criteria

1. THE Clinical_Service SHALL crear el DTO AppointmentListItemResponse con información básica, paciente, doctor, pago, clínica, QR y metadatos
2. THE Clinical_Service SHALL incluir información de paciente (id, fullName, dpi, phone, email) en el Unified_DTO
3. THE Clinical_Service SHALL incluir información de doctor (id, name, specialty) en el Unified_DTO
4. THE Clinical_Service SHALL incluir información de pago (invoiceId, invoiceNumber, status, statusLabel, statusColor, amount, canActivate, tooltip) en el Unified_DTO
5. THE Clinical_Service SHALL incluir información clínica opcional (hasVitalSigns, hasTriage, manchesterLevel, hasLabOrders, hasPrescriptions, hasConsultation) en el Unified_DTO
6. THE Clinical_Service SHALL incluir información de QR opcional (hasQR, qrCodeBase64) en el Unified_DTO
7. THE Clinical_Service SHALL incluir metadatos (isToday, isPast, isUpcoming, canEdit, canCancel, canActivate) en el Unified_DTO

### Requirement 4: Mantener Endpoints con Lógica Especial

**User Story:** Como desarrollador backend, quiero preservar endpoints que tienen lógica de negocio específica, para no perder funcionalidad durante la refactorización.

#### Acceptance Criteria

1. THE Clinical_Service SHALL mantener GET /appointments/my con autenticación de paciente
2. THE Clinical_Service SHALL mantener GET /appointments/doctor con autenticación de doctor
3. THE Clinical_Service SHALL mantener GET /appointments/today con validación de pago
4. THE Clinical_Service SHALL mantener GET /appointments/admission-queue con validación de pago
5. THE Clinical_Service SHALL mantener GET /appointments/{id}/triage con información específica de triaje
6. THE Clinical_Service SHALL mantener GET /appointments/{id}/qr-status con información específica de QR
7. THE Clinical_Service SHALL mantener GET /appointments/available con disponibilidad de horarios
8. THE Clinical_Service SHALL mantener GET /appointments/slots con disponibilidad de horarios

### Requirement 5: Migrar Endpoints Mantenidos a Unified_DTO

**User Story:** Como desarrollador backend, quiero que los endpoints mantenidos usen la estructura de datos unificada, para garantizar consistencia en toda la API.

#### Acceptance Criteria

1. WHEN se llame GET /appointments/my, THE Clinical_Service SHALL retornar lista de AppointmentListItemResponse
2. WHEN se llame GET /appointments/doctor, THE Clinical_Service SHALL retornar lista de AppointmentListItemResponse
3. WHEN se llame GET /appointments/today, THE Clinical_Service SHALL retornar lista de AppointmentListItemResponse
4. WHEN se llame GET /appointments/admission-queue, THE Clinical_Service SHALL retornar lista de AppointmentListItemResponse
5. FOR ALL endpoints de listado, THE Clinical_Service SHALL usar el mismo mapper unificado

### Requirement 6: Mapper Unificado con Lógica de Validación de Pago

**User Story:** Como desarrollador backend, quiero un mapper centralizado que incluya validación de pago, para eliminar duplicación de lógica de mapeo.

#### Acceptance Criteria

1. THE Clinical_Service SHALL crear el método mapToUnifiedResponse que convierta Appointment a AppointmentListItemResponse
2. WHEN se mapee una cita, THE Clinical_Service SHALL invocar PaymentValidator para obtener estado de pago
3. WHEN la validación de pago sea exitosa, THE Clinical_Service SHALL asignar paymentStatus PAID con color verde
4. WHEN la validación de pago falle por pago pendiente, THE Clinical_Service SHALL asignar paymentStatus PENDING con color naranja
5. WHEN la validación de pago falle por factura cancelada, THE Clinical_Service SHALL asignar paymentStatus CANCELLED con color rojo
6. WHEN la cita no tenga factura, THE Clinical_Service SHALL asignar paymentStatus NO_INVOICE con color gris
7. WHEN ocurra error de servicio, THE Clinical_Service SHALL asignar paymentStatus ERROR con color rojo

### Requirement 7: Información Clínica Opcional

**User Story:** Como desarrollador frontend, quiero información clínica agregada en el DTO, para mostrar indicadores visuales sin hacer múltiples llamadas.

#### Acceptance Criteria

1. WHEN una cita tenga triaje registrado, THE Clinical_Service SHALL asignar hasVitalSigns true y hasTriage true
2. WHEN una cita tenga nivel Manchester asignado, THE Clinical_Service SHALL incluir manchesterLevel en el DTO
3. WHEN una cita tenga órdenes de laboratorio, THE Clinical_Service SHALL asignar hasLabOrders true
4. WHEN una cita tenga prescripciones, THE Clinical_Service SHALL asignar hasPrescriptions true
5. WHEN una cita tenga consulta completada, THE Clinical_Service SHALL asignar hasConsultation true

### Requirement 8: Metadatos de Cita

**User Story:** Como desarrollador frontend, quiero metadatos calculados en el backend, para simplificar la lógica de UI y garantizar consistencia.

#### Acceptance Criteria

1. WHEN la fecha de cita sea hoy, THE Clinical_Service SHALL asignar isToday true
2. WHEN la fecha de cita sea anterior a hoy, THE Clinical_Service SHALL asignar isPast true
3. WHEN la fecha de cita sea posterior a hoy, THE Clinical_Service SHALL asignar isUpcoming true
4. WHEN el estado de cita permita edición, THE Clinical_Service SHALL asignar canEdit true
5. WHEN el estado de cita permita cancelación, THE Clinical_Service SHALL asignar canCancel true
6. WHEN el estado de cita y pago permitan activación, THE Clinical_Service SHALL asignar canActivate true

### Requirement 9: Compatibilidad con Frontend Existente

**User Story:** Como desarrollador frontend, quiero que la migración sea gradual, para evitar romper funcionalidad existente durante el despliegue.

#### Acceptance Criteria

1. THE Clinical_Service SHALL mantener endpoints deprecados durante un período de transición
2. WHEN se llame un endpoint deprecado, THE Clinical_Service SHALL registrar warning en logs
3. WHEN se llame un endpoint deprecado, THE Clinical_Service SHALL incluir header Deprecation con fecha de eliminación
4. THE Clinical_Service SHALL documentar mapeo de endpoints antiguos a nuevos en README

### Requirement 10: Documentación de API

**User Story:** Como desarrollador frontend, quiero documentación clara de los nuevos endpoints, para entender cómo migrar el código cliente.

#### Acceptance Criteria

1. THE Clinical_Service SHALL documentar query parameters del endpoint GET /appointments
2. THE Clinical_Service SHALL documentar estructura completa de AppointmentListItemResponse
3. THE Clinical_Service SHALL documentar ejemplos de uso para cada caso de filtrado
4. THE Clinical_Service SHALL documentar mapeo de endpoints antiguos a nuevos
5. THE Clinical_Service SHALL documentar período de deprecación y fecha de eliminación

### Requirement 11: Tests de Integración

**User Story:** Como desarrollador backend, quiero tests de integración que validen la funcionalidad consolidada, para garantizar que no se pierde funcionalidad durante la refactorización.

#### Acceptance Criteria

1. THE Clinical_Service SHALL crear test que valide filtrado por status único
2. THE Clinical_Service SHALL crear test que valide filtrado por múltiples status
3. THE Clinical_Service SHALL crear test que valide filtrado por fecha
4. THE Clinical_Service SHALL crear test que valide filtrado por queue
5. THE Clinical_Service SHALL crear test que valide combinación de múltiples filtros
6. THE Clinical_Service SHALL crear test que valide estructura de AppointmentListItemResponse
7. THE Clinical_Service SHALL crear test que valide información de pago en respuesta
8. THE Clinical_Service SHALL crear test que valide metadatos calculados

### Requirement 12: Métricas de Reducción de Código

**User Story:** Como líder técnico, quiero métricas que demuestren la reducción de código, para justificar el esfuerzo de refactorización.

#### Acceptance Criteria

1. THE Clinical_Service SHALL reducir el número de endpoints de 13 a 8 (38% de reducción)
2. THE Clinical_Service SHALL eliminar al menos 200 líneas de código duplicado
3. THE Clinical_Service SHALL consolidar lógica de mapeo en un solo método
4. THE Clinical_Service SHALL consolidar lógica de filtrado en un solo método
