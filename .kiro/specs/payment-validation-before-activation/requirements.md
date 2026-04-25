# Requirements Document: Payment Validation Before Activation

## Introduction

La validación de pago antes de activar citas médicas garantiza que solo los pacientes que han pagado su consulta puedan aparecer en el sistema de triaje. Esta funcionalidad previene que pacientes sin pago confirmado sean atendidos, mejora el control financiero del hospital, y asegura que el flujo de caja sea consistente con la atención médica brindada.

**Problema actual**: El endpoint de activación de citas (`PUT /api/clinical/appointments/{id}/activate`) no valida el estado de pago de la factura asociada. Esto permite que citas con facturas en estado PENDING (no pagadas) sean activadas y aparezcan en triaje, generando inconsistencias entre pacientes atendidos y pagos recibidos.

**Solución**: Agregar validación en el endpoint de activación que consulte el estado de la factura en Billing Service antes de permitir la activación. Solo citas con facturas en estado PAID podrán activarse. Citas sin invoice_id (NULL) se permitirán activar como caso de compensación.

**Arquitectura**: Microservicios con DDD | **Comunicación**: REST síncrona | **Principio**: CERO JOINs entre esquemas

## Glossary

- **Clinical_Service**: Microservicio responsable de la gestión clínica y citas médicas (puerto 8083)
- **Billing_Service**: Microservicio responsable de facturación y pagos (puerto 8086)
- **Appointment_Activation**: Proceso que cambia el estado de una cita de SCHEDULED a ACTIVE para que aparezca en triaje
- **Billing_Service_Client**: Cliente REST que comunica Clinical Service con Billing Service
- **Invoice_Status**: Estado de una factura que puede ser PENDING, PAID o CANCELLED
- **Payment_Validation**: Verificación del estado de pago de una factura antes de activar una cita
- **Triaje**: Sistema donde aparecen las citas activas para ser atendidas por enfermería
- **Compensation_Case**: Escenario donde una cita tiene invoice_id = NULL porque Billing Service falló durante la creación
- **Circuit_Breaker**: Patrón de resiliencia que previene llamadas a servicios no disponibles

## Requirements

### Requirement 1: Validación de Estado de Pago al Activar Cita

**User Story:** Como recepcionista, quiero que el sistema valide que el paciente ha pagado antes de activar su cita, para que solo pacientes con pago confirmado aparezcan en triaje.

#### Acceptance Criteria

1. WHEN se intenta activar una cita con invoice_id NOT NULL, THE Clinical_Service SHALL llamar a Billing_Service_Client para obtener el estado de la factura
2. WHEN Billing_Service retorna una factura con status = PAID, THE Clinical_Service SHALL permitir la activación de la cita
3. WHEN Billing_Service retorna una factura con status = PENDING, THE Clinical_Service SHALL rechazar la activación con HTTP 400 Bad Request
4. WHEN Billing_Service retorna una factura con status = CANCELLED, THE Clinical_Service SHALL rechazar la activación con HTTP 400 Bad Request
5. THE mensaje de error para factura PENDING SHALL ser: "La cita no puede activarse. El paciente debe pagar en caja primero."
6. THE mensaje de error para factura CANCELLED SHALL ser: "La cita no puede activarse. La factura ha sido cancelada."
7. THE sistema SHALL registrar en logs cada intento de activación con: appointmentId, invoiceId, invoiceStatus, resultado
8. WHEN la validación es exitosa, THE sistema SHALL cambiar el estado de la cita a ACTIVE

### Requirement 2: Manejo de Citas sin Factura (Compensation Case)

**User Story:** Como recepcionista, quiero poder activar citas que no tienen factura asociada (invoice_id = NULL), para manejar casos donde el sistema de facturación estuvo no disponible durante la creación.

#### Acceptance Criteria

1. WHEN se intenta activar una cita con invoice_id = NULL, THE Clinical_Service SHALL permitir la activación sin llamar a Billing_Service
2. WHEN se activa una cita con invoice_id = NULL, THE sistema SHALL registrar un log de nivel WARN indicando activación sin validación de pago
3. WHEN se activa una cita con invoice_id = NULL, THE sistema SHALL cambiar el estado de la cita a ACTIVE
4. THE sistema SHALL incluir en la respuesta un campo de advertencia indicando que la cita no tiene factura asociada
5. THE sistema SHALL permitir que el cajero cree manualmente la factura después de la activación
6. THE sistema SHALL exponer métrica appointments_activated_without_invoice_total contando activaciones sin factura
7. WHEN se activan más de 5 citas sin factura en 1 hora, THE sistema SHALL generar alerta de advertencia

### Requirement 3: Resiliencia ante Fallos de Billing Service

**User Story:** Como sistema, quiero manejar fallos de Billing Service durante la validación de pago sin bloquear completamente la activación de citas.

#### Acceptance Criteria

1. WHEN Billing_Service_Client retorna timeout o error de conexión, THE Clinical_Service SHALL rechazar la activación con HTTP 503 Service Unavailable
2. WHEN Billing_Service retorna HTTP 404 (factura no encontrada), THE Clinical_Service SHALL rechazar la activación con HTTP 400 Bad Request
3. WHEN Billing_Service retorna HTTP 500, THE Clinical_Service SHALL rechazar la activación con HTTP 503 Service Unavailable
4. THE mensaje de error para timeout SHALL ser: "El sistema de facturación no está disponible. Intente nuevamente en unos momentos."
5. THE mensaje de error para factura no encontrada SHALL ser: "La factura asociada a esta cita no existe en el sistema."
6. THE Billing_Service_Client SHALL implementar retry con 2 intentos y backoff exponencial (500ms, 1s)
7. THE Billing_Service_Client SHALL tener timeout de 3 segundos por cada intento de llamada HTTP
8. THE sistema SHALL registrar métricas de fallos: payment_validation_failures_total con labels: reason (timeout, not_found, server_error)

### Requirement 4: Endpoint para Consultar Estado de Factura en Billing Service

**User Story:** Como Billing Service, quiero exponer un endpoint para que Clinical Service pueda consultar el estado de una factura por su ID.

#### Acceptance Criteria

1. THE Billing_Service SHALL exponer endpoint GET /api/billing/invoices/{invoiceId}
2. WHEN se recibe una petición con invoiceId válido, THE Billing_Service SHALL retornar HTTP 200 OK con InvoiceResponse
3. WHEN se recibe una petición con invoiceId inexistente, THE Billing_Service SHALL retornar HTTP 404 Not Found
4. THE InvoiceResponse SHALL incluir campos: id, invoiceNumber, patientId, appointmentId, status, total, createdAt
5. THE endpoint SHALL ser accesible sin autenticación (comunicación interna entre microservicios)
6. THE endpoint SHALL registrar en logs el servicio que consultó la factura (header X-Service-Name)
7. THE endpoint SHALL tener tiempo de respuesta menor a 200ms en el percentil 95
8. THE Billing_Service SHALL crear índice en invoices.id para optimizar consultas por ID

### Requirement 5: Modificación del Endpoint de Activación de Citas

**User Story:** Como desarrollador, quiero modificar el endpoint de activación de citas para incluir la validación de pago, manteniendo compatibilidad con el comportamiento actual.

#### Acceptance Criteria

1. THE endpoint PUT /api/clinical/appointments/{id}/activate SHALL mantener su firma actual
2. WHEN se recibe una petición de activación, THE sistema SHALL validar que la cita existe y está en estado SCHEDULED
3. WHEN la cita está en estado diferente a SCHEDULED, THE sistema SHALL retornar HTTP 400 Bad Request con mensaje: "Solo se pueden activar citas en estado SCHEDULED"
4. WHEN la cita tiene invoice_id NOT NULL, THE sistema SHALL ejecutar Payment_Validation antes de activar
5. WHEN Payment_Validation es exitosa, THE sistema SHALL actualizar el estado de la cita a ACTIVE
6. WHEN Payment_Validation falla, THE sistema SHALL retornar el error correspondiente sin modificar la cita
7. THE endpoint SHALL incluir header X-User-Id para auditoría
8. THE endpoint SHALL retornar AppointmentResponse con el estado actualizado de la cita

### Requirement 6: Validación de Estados de Factura

**User Story:** Como sistema, quiero validar que solo facturas en estado PAID permitan la activación de citas, rechazando cualquier otro estado.

#### Acceptance Criteria

1. THE sistema SHALL definir enum InvoiceStatus con valores: PENDING, PAID, CANCELLED
2. WHEN InvoiceStatus = PAID, THE Payment_Validation SHALL retornar éxito
3. WHEN InvoiceStatus = PENDING, THE Payment_Validation SHALL retornar error con código PAYMENT_PENDING
4. WHEN InvoiceStatus = CANCELLED, THE Payment_Validation SHALL retornar error con código INVOICE_CANCELLED
5. THE sistema SHALL mapear códigos de error a mensajes en español para el frontend
6. THE sistema SHALL registrar en logs el estado de la factura en cada validación
7. THE sistema SHALL exponer métrica payment_validation_results_total con labels: status (paid, pending, cancelled)
8. THE sistema SHALL validar que el campo status en InvoiceResponse no sea NULL

### Requirement 7: Auditoría de Activaciones de Citas

**User Story:** Como auditor, quiero que el sistema registre todas las activaciones de citas con información de pago, para facilitar auditorías financieras.

#### Acceptance Criteria

1. WHEN se activa una cita exitosamente, THE sistema SHALL registrar en logs: appointmentId, invoiceId, invoiceStatus, userId, timestamp
2. WHEN se rechaza una activación por pago pendiente, THE sistema SHALL registrar en logs: appointmentId, invoiceId, invoiceStatus, userId, timestamp, reason
3. WHEN se activa una cita sin factura, THE sistema SHALL registrar en logs: appointmentId, userId, timestamp, warning="no_invoice"
4. THE logs SHALL tener nivel INFO para activaciones exitosas
5. THE logs SHALL tener nivel WARN para activaciones sin factura
6. THE logs SHALL tener nivel ERROR para rechazos por pago pendiente o factura cancelada
7. THE sistema SHALL incluir en logs el tiempo de respuesta de Billing_Service
8. THE logs SHALL estar en formato JSON estructurado para facilitar análisis

### Requirement 8: Mensajes de Error Claros para el Usuario

**User Story:** Como recepcionista, quiero recibir mensajes de error claros cuando no puedo activar una cita, para saber qué acción tomar.

#### Acceptance Criteria

1. WHEN la factura está PENDING, THE sistema SHALL retornar mensaje: "La cita no puede activarse. El paciente debe pagar en caja primero."
2. WHEN la factura está CANCELLED, THE sistema SHALL retornar mensaje: "La cita no puede activarse. La factura ha sido cancelada."
3. WHEN la factura no existe, THE sistema SHALL retornar mensaje: "La factura asociada a esta cita no existe en el sistema."
4. WHEN Billing Service no está disponible, THE sistema SHALL retornar mensaje: "El sistema de facturación no está disponible. Intente nuevamente en unos momentos."
5. WHEN la cita no está en estado SCHEDULED, THE sistema SHALL retornar mensaje: "Solo se pueden activar citas en estado SCHEDULED"
6. THE mensajes de error SHALL estar en español
7. THE respuesta de error SHALL incluir campo errorCode para manejo programático en el frontend
8. THE respuesta de error SHALL incluir timestamp y requestId para trazabilidad

### Requirement 9: Configuración de Validación de Pago

**User Story:** Como administrador del sistema, quiero poder habilitar o deshabilitar la validación de pago mediante configuración, para facilitar despliegues graduales.

#### Acceptance Criteria

1. THE sistema SHALL leer configuración desde application.yml en la propiedad payment.validation.enabled
2. WHEN payment.validation.enabled = true, THE sistema SHALL ejecutar Payment_Validation al activar citas
3. WHEN payment.validation.enabled = false, THE sistema SHALL activar citas sin validar el estado de pago
4. THE valor por defecto de payment.validation.enabled SHALL ser true
5. THE sistema SHALL registrar en logs al iniciar si la validación de pago está habilitada o deshabilitada
6. THE sistema SHALL exponer métrica payment_validation_enabled con valor 1 (habilitado) o 0 (deshabilitado)
7. THE sistema SHALL permitir cambiar la configuración sin reiniciar (usando @RefreshScope)
8. WHEN se deshabilita la validación, THE sistema SHALL registrar un log de nivel WARN

### Requirement 10: QR Inteligente con Validación de Pago

**User Story:** Como paciente, quiero que al escanear el código QR de mi cita se me muestre el estado de pago, para saber si puedo proceder con la activación o debo pagar primero.

#### Acceptance Criteria

1. WHEN se escanea un QR de cita con factura PAID, THE sistema SHALL mostrar mensaje: "Cita pagada - Puede activarse"
2. WHEN se escanea un QR de cita con factura PENDING, THE sistema SHALL mostrar mensaje: "Cita no pagada - Debe pagar en caja primero"
3. WHEN se escanea un QR de cita con factura CANCELLED, THE sistema SHALL mostrar mensaje: "Factura cancelada - Contacte recepción"
4. WHEN se escanea un QR de cita con invoice_id = NULL, THE sistema SHALL mostrar mensaje: "Cita sin factura - Contacte recepción"
5. THE sistema SHALL permitir activación automática solo si la factura está PAID
6. THE sistema SHALL incluir botón "Activar Cita" habilitado solo para facturas PAID
7. THE sistema SHALL mostrar información de la cita: fecha, hora, doctor, estado de pago
8. THE sistema SHALL registrar en logs cada escaneo de QR con resultado de validación

### Requirement 11: UI de Recepción con Estado de Pago

**User Story:** Como recepcionista, quiero ver el estado de pago de cada cita en la lista del día, para saber cuáles puedo activar sin necesidad de verificar manualmente.

#### Acceptance Criteria

1. WHEN se muestra la lista de citas del día, THE sistema SHALL incluir columna "Estado de Pago" con valores: PAGADA, PENDIENTE, CANCELADA, SIN FACTURA
2. WHEN una cita tiene factura PAID, THE botón "Activar" SHALL estar habilitado con color verde
3. WHEN una cita tiene factura PENDING, THE botón "Activar" SHALL estar deshabilitado con tooltip: "El paciente debe pagar en caja primero"
4. WHEN una cita tiene factura CANCELLED, THE botón "Activar" SHALL estar deshabilitado con tooltip: "Factura cancelada - Contacte administración"
5. WHEN una cita tiene invoice_id = NULL, THE botón "Activar" SHALL estar habilitado con tooltip: "Cita sin factura - Activar bajo responsabilidad"
6. THE sistema SHALL actualizar el estado de pago en tiempo real cuando se modifica una factura
7. THE sistema SHALL permitir filtrar citas por estado de pago: "Solo pagadas", "Solo pendientes", "Todas"
8. THE sistema SHALL mostrar contador de citas por estado: "Pagadas: 5, Pendientes: 3, Total: 8"

### Requirement 12: Testing de Integración con Billing Service

**User Story:** Como desarrollador, quiero tener tests de integración que validen el comportamiento de la validación de pago en diferentes escenarios.

#### Acceptance Criteria

1. THE sistema SHALL incluir test de integración para activación exitosa con factura PAID
2. THE sistema SHALL incluir test de integración para rechazo con factura PENDING
3. THE sistema SHALL incluir test de integración para rechazo con factura CANCELLED
4. THE sistema SHALL incluir test de integración para activación exitosa con invoice_id = NULL
5. THE sistema SHALL incluir test de integración para manejo de timeout de Billing Service
6. THE sistema SHALL incluir test de integración para manejo de factura no encontrada (404)
7. THE sistema SHALL incluir test de integración para manejo de error de servidor (500)
8. THE tests SHALL usar WireMock o similar para simular respuestas de Billing Service

## Non-Functional Requirements

### Performance

- **NFR-1**: El tiempo de respuesta para activar una cita (incluyendo llamada a Billing Service) SHALL ser menor a 1 segundo en el percentil 95
- **NFR-2**: El timeout de llamada HTTP a Billing Service SHALL ser 3 segundos máximo
- **NFR-3**: El sistema SHALL soportar al menos 50 activaciones de citas concurrentes sin degradación

### Availability

- **NFR-4**: La disponibilidad de Clinical Service SHALL ser >= 99.9% independientemente del estado de Billing Service
- **NFR-5**: El sistema SHALL rechazar activaciones cuando Billing Service no está disponible (fail-safe)
- **NFR-6**: El sistema SHALL permitir activaciones de citas sin factura (invoice_id = NULL) incluso si Billing Service está caído

### Reliability

- **NFR-7**: La tasa de éxito de validación de pago SHALL ser >= 99% cuando Billing Service está disponible
- **NFR-8**: El sistema SHALL garantizar que NUNCA se activa una cita con factura PENDING o CANCELLED
- **NFR-9**: El sistema SHALL garantizar que citas con invoice_id = NULL pueden activarse sin validación

### Security

- **NFR-10**: Todas las llamadas entre Clinical Service y Billing Service SHALL incluir el header X-Service-Name para auditoría
- **NFR-11**: El sistema SHALL validar que el usuario tiene rol RECEPTION o ADMIN antes de activar citas
- **NFR-12**: El sistema SHALL NO exponer detalles internos de errores de Billing Service al frontend

### Maintainability

- **NFR-13**: El código de validación de pago SHALL estar aislado en un componente PaymentValidator
- **NFR-14**: La configuración de validación SHALL ser externalizada en application.yml
- **NFR-15**: El sistema SHALL incluir logs estructurados para facilitar debugging de problemas de validación

### Testability

- **NFR-16**: El sistema SHALL incluir al menos 3 property-based tests validando las propiedades de correctitud del diseño
- **NFR-17**: El sistema SHALL incluir integration tests con Billing Service mockeado simulando: éxito, timeout, error 404, error 500
- **NFR-18**: El sistema SHALL incluir tests unitarios para PaymentValidator con cobertura >= 90%

## Business Rules

- **BR-1**: Solo citas con factura en estado PAID pueden activarse
- **BR-2**: Citas sin factura (invoice_id = NULL) pueden activarse como caso de compensación
- **BR-3**: Citas con factura PENDING deben rechazarse con mensaje claro al usuario
- **BR-4**: Citas con factura CANCELLED no pueden activarse bajo ninguna circunstancia
- **BR-5**: Solo citas en estado SCHEDULED pueden activarse
- **BR-6**: La validación de pago puede deshabilitarse mediante configuración para despliegues graduales
- **BR-7**: Los mensajes de error SIEMPRE deben estar en español
- **BR-9**: El QR debe mostrar el estado de pago al ser escaneado antes de permitir activación
- **BR-10**: La UI de recepción debe mostrar visualmente qué citas pueden activarse según su estado de pago
- **BR-11**: Citas con invoice_id = NULL pueden activarse pero con advertencia visual
- **BR-12**: El estado de pago debe actualizarse en tiempo real en la UI cuando cambia una factura

## Dependencies

- **Clinical Service (8083)**: Servicio que ejecuta la validación de pago al activar citas
- **Billing Service (8086)**: Servicio que provee el estado de las facturas
- **API Gateway (8080)**: Para enrutamiento de peticiones HTTP
- **PostgreSQL**: Base de datos con esquemas clinical_schema y billing_schema
- **Resilience4j**: Librería para retry patterns
- **appointment-billing-integration**: Feature existente que crea facturas automáticamente
- **QR Scanner**: Funcionalidad que permite escanear códigos QR de citas
- **Reception UI**: Interfaz de recepción que muestra lista de citas del día

---

**Created**: 2025-01-XX  
**Author**: MedFlow Team  
**Status**: ✅ Ready for Review  
**Version**: 1.0.0
