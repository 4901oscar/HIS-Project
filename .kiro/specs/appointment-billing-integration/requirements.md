# Requirements Document: Appointment-Billing Integration

## Introduction

La integración automática entre el módulo de admisión (Clinical Service) y el módulo de facturación (Billing Service) permite que al crear una cita médica se genere automáticamente una factura PENDING en el sistema de facturación. Esta integración elimina la creación manual de facturas, reduce errores humanos, mejora la trazabilidad entre citas y facturas, y minimiza el riesgo de que pacientes se vayan sin pagar.

**Problema actual**: Clinical Service genera un número de factura temporal pero NO crea la factura real en Billing Service. El cajero debe crear manualmente la factura cuando el paciente llega a caja.

**Solución**: Implementar integración síncrona REST donde al crear una cita se llama automáticamente a Billing Service para crear una factura PENDING con cargo por consulta médica.

**Arquitectura**: Microservicios con DDD | **Comunicación**: REST síncrona | **Principio**: CERO JOINs entre esquemas

## Glossary

- **Clinical_Service**: Microservicio responsable de la gestión clínica y citas médicas (puerto 8083)
- **Billing_Service**: Microservicio responsable de facturación y pagos (puerto 8086)
- **Appointment_Manager**: Componente que gestiona la creación y actualización de citas médicas
- **Billing_Service_Client**: Cliente REST que comunica Clinical Service con Billing Service
- **Invoice**: Factura generada en Billing Service con estado PENDING, PAID o CANCELLED
- **Charge**: Cargo individual dentro de una factura (consulta, laboratorio, medicamento)
- **Consultation_Price**: Precio configurado para consultas médicas (default: 150.00 GTQ)
- **Circuit_Breaker**: Patrón de resiliencia que previene llamadas a servicios no disponibles
- **Compensation_Strategy**: Estrategia para manejar fallos en Billing Service sin afectar la creación de citas
- **Bidirectional_Reference**: Referencias cruzadas entre appointments.invoice_id e invoices.appointment_id

## Requirements

### Requirement 1: Creación Automática de Factura al Agendar Cita

**User Story:** Como personal de admisión, quiero que al crear una cita médica se genere automáticamente una factura PENDING en el sistema de facturación, para que el paciente tenga un cargo registrado desde el momento de la reserva.

#### Acceptance Criteria

1. WHEN se crea una cita médica exitosamente, THE Appointment_Manager SHALL llamar a Billing_Service_Client para crear una factura
2. WHEN se llama a Billing_Service_Client, THE sistema SHALL enviar: patientId, appointmentId, y un cargo de tipo CONSULTATION con el precio configurado
3. WHEN Billing_Service retorna exitosamente, THE Appointment_Manager SHALL almacenar el invoiceId en la columna appointments.invoice_id
4. WHEN Billing_Service retorna exitosamente, THE sistema SHALL generar el código QR de la cita con el número de factura real
5. WHEN Billing_Service retorna exitosamente, THE sistema SHALL enviar email de confirmación al paciente incluyendo el número de factura
6. THE factura creada SHALL tener estado PENDING hasta que el paciente realice el pago
7. THE factura creada SHALL incluir un cargo con descripción: "Consulta médica - [Nombre Doctor] - [Fecha]"
8. THE factura creada SHALL tener el campo invoices.appointment_id apuntando al ID de la cita creada

### Requirement 2: Estrategia de Compensación ante Fallos de Billing Service

**User Story:** Como personal de admisión, quiero que la creación de citas continúe funcionando incluso si el sistema de facturación está temporalmente no disponible, para no interrumpir la atención de pacientes.

#### Acceptance Criteria

1. IF Billing_Service_Client retorna error o timeout, THEN THE Appointment_Manager SHALL continuar creando la cita con invoice_id = NULL
2. WHEN Billing_Service falla, THE sistema SHALL registrar un log de nivel WARN con el appointmentId para reconciliación manual posterior
3. WHEN Billing_Service falla, THE sistema SHALL generar el código QR de la cita con un número de factura temporal (formato: INV-yyyyMMddHHmmss)
4. WHEN Billing_Service falla, THE sistema SHALL enviar email de confirmación al paciente sin incluir información de factura
5. WHEN Billing_Service falla, THE sistema SHALL retornar HTTP 201 Created al frontend con un campo de advertencia indicando que la factura no pudo ser creada
6. THE cita creada con invoice_id = NULL SHALL tener estado SCHEDULED y ser completamente funcional
7. THE sistema SHALL permitir que el cajero cree manualmente la factura posteriormente usando el appointmentId

### Requirement 3: Resiliencia con Circuit Breaker y Retry

**User Story:** Como sistema, quiero implementar patrones de resiliencia para proteger Clinical Service de fallos en cascada cuando Billing Service está no disponible.

#### Acceptance Criteria

1. THE Billing_Service_Client SHALL implementar un circuit breaker con umbral de 50% de fallos en ventana de 10 llamadas
2. WHEN el circuit breaker detecta 5 fallos consecutivos, THE circuit breaker SHALL abrirse y rechazar llamadas inmediatamente sin intentar conectar
3. WHILE el circuit breaker está abierto, THE Billing_Service_Client SHALL retornar NULL inmediatamente activando la estrategia de compensación
4. WHEN el circuit breaker está abierto por 30 segundos, THE circuit breaker SHALL transicionar a estado half-open permitiendo 3 llamadas de prueba
5. IF las 3 llamadas de prueba son exitosas, THEN THE circuit breaker SHALL cerrarse y volver a operación normal
6. THE Billing_Service_Client SHALL implementar retry con 3 intentos y backoff exponencial (1s, 2s, 4s)
7. THE Billing_Service_Client SHALL tener timeout de 5 segundos por cada intento de llamada HTTP
8. THE sistema SHALL registrar métricas del circuit breaker: estado actual, tasa de éxito/fallo, duración en estado abierto

### Requirement 4: Configuración de Precios de Consulta

**User Story:** Como administrador del sistema, quiero configurar el precio de las consultas médicas mediante archivo de configuración, para ajustar tarifas sin modificar código.

#### Acceptance Criteria

1. THE sistema SHALL leer el precio de consulta desde application.yml en la propiedad consultation.price.default
2. THE sistema SHALL soportar configuración de precio por tipo de consulta: default, emergency, followup
3. WHEN no se especifica precio en configuración, THE sistema SHALL usar 150.00 GTQ como valor por defecto
4. WHEN se crea una factura por cita, THE sistema SHALL usar el precio configurado en consultation.price.default
5. THE precio configurado SHALL ser un BigDecimal con precisión de 2 decimales
6. THE sistema SHALL validar que el precio configurado sea mayor a cero al iniciar
7. IF el precio configurado es inválido, THEN THE sistema SHALL fallar al iniciar con mensaje de error claro

### Requirement 5: Referencias Bidireccionales entre Citas y Facturas

**User Story:** Como desarrollador, quiero mantener referencias bidireccionales entre citas y facturas, para facilitar la navegación y auditoría entre ambos sistemas.

#### Acceptance Criteria

1. THE tabla appointments SHALL tener columna invoice_id (VARCHAR 36, nullable) con índice
2. THE tabla invoices SHALL tener columna appointment_id (VARCHAR 36, nullable) con índice
3. WHEN se crea una cita con factura exitosa, THE appointments.invoice_id SHALL apuntar a invoices.id
4. WHEN se crea una factura desde una cita, THE invoices.appointment_id SHALL apuntar a appointments.id
5. THE sistema SHALL permitir consultar la factura asociada a una cita mediante appointments.invoice_id
6. THE sistema SHALL permitir consultar la cita asociada a una factura mediante invoices.appointment_id
7. THE sistema SHALL NO crear foreign keys entre esquemas (cumplir principio CERO JOINs)
8. WHEN una cita tiene invoice_id = NULL, THE sistema SHALL interpretar que la factura no fue creada automáticamente

### Requirement 6: Validación de Datos antes de Crear Factura

**User Story:** Como sistema, quiero validar que todos los datos necesarios estén disponibles antes de intentar crear la factura, para evitar errores de integración.

#### Acceptance Criteria

1. WHEN se va a crear una factura, THE Appointment_Manager SHALL validar que el patientId existe llamando a Patient_Service_Client
2. WHEN se va a crear una factura, THE Appointment_Manager SHALL validar que el doctorId existe en la base de datos
3. WHEN se va a crear una factura, THE Appointment_Manager SHALL validar que el slot de la cita está disponible
4. IF el paciente no existe, THEN THE sistema SHALL retornar error 404 Patient Not Found sin intentar crear factura
5. IF el doctor no existe, THEN THE sistema SHALL retornar error 404 Doctor Not Found sin intentar crear factura
6. IF el slot está ocupado, THEN THE sistema SHALL retornar error 409 Slot Occupied sin intentar crear factura
7. THE sistema SHALL validar datos ANTES de llamar a Billing_Service_Client para evitar facturas huérfanas

### Requirement 7: Endpoint de Billing Service para Crear Facturas

**User Story:** Como Billing Service, quiero exponer un endpoint REST para que Clinical Service pueda crear facturas automáticamente.

#### Acceptance Criteria

1. THE Billing_Service SHALL exponer endpoint POST /api/billing/invoices
2. WHEN se recibe una petición válida, THE Billing_Service SHALL generar un invoiceNumber único (formato: INV-YYYYMMDD-XXXX)
3. WHEN se recibe una petición válida, THE Billing_Service SHALL calcular el subtotal sumando todos los cargos
4. WHEN se recibe una petición válida, THE Billing_Service SHALL crear la factura con estado PENDING y discountAmount = 0
5. WHEN se crea la factura exitosamente, THE Billing_Service SHALL retornar HTTP 201 Created con InvoiceResponse
6. THE InvoiceResponse SHALL incluir: id, invoiceNumber, patientId, appointmentId, charges, subtotal, total, status, createdAt
7. IF la petición es inválida, THEN THE Billing_Service SHALL retornar HTTP 400 Bad Request con mensaje de error en español
8. THE Billing_Service SHALL registrar en logs el usuario que creó la factura (header X-User-Id)

### Requirement 8: Formato de Petición y Respuesta de Integración

**User Story:** Como desarrollador, quiero que el contrato de integración entre Clinical Service y Billing Service esté claramente definido, para facilitar el desarrollo y testing.

#### Acceptance Criteria

1. THE CreateInvoiceRequest SHALL incluir campos: patientId (String), appointmentId (String), charges (List<ChargeRequest>)
2. THE ChargeRequest SHALL incluir campos: type (String), description (String), quantity (Integer), unitPrice (BigDecimal)
3. THE InvoiceResponse SHALL incluir campos: id, invoiceNumber, patientId, appointmentId, charges, subtotal, discountAmount, total, status, createdAt, createdBy
4. THE ChargeResponse SHALL incluir campos: id, type, description, quantity, unitPrice, subtotal
5. THE type en ChargeRequest SHALL ser uno de: CONSULTATION, LABORATORY, MEDICATION, OTHER
6. THE status en InvoiceResponse SHALL ser uno de: PENDING, PAID, CANCELLED
7. THE Billing_Service_Client SHALL incluir header X-User-Id en todas las peticiones para auditoría
8. THE Billing_Service_Client SHALL incluir header Content-Type: application/json

### Requirement 9: Monitoreo y Observabilidad de la Integración

**User Story:** Como ingeniero de operaciones, quiero monitorear la salud de la integración entre Clinical Service y Billing Service, para detectar y resolver problemas rápidamente.

#### Acceptance Criteria

1. THE sistema SHALL exponer métrica billing_service_calls_total con labels: status (success, failure, timeout)
2. THE sistema SHALL exponer métrica resilience4j_circuitbreaker_state con label name=billingService
3. THE sistema SHALL exponer métrica appointments_without_invoice_total contando citas creadas sin factura
4. THE sistema SHALL exponer métrica billing_service_call_duration_seconds con percentiles p50, p95, p99
5. WHEN el circuit breaker está abierto por más de 5 minutos, THE sistema SHALL generar alerta crítica
6. WHEN la tasa de fallos supera 10% en 1 hora, THE sistema SHALL generar alerta de advertencia
7. WHEN se crean más de 10 citas sin factura en 1 hora, THE sistema SHALL generar alerta de advertencia
8. THE sistema SHALL registrar logs estructurados con campos: timestamp, level, service, appointmentId, invoiceId, duration, error

### Requirement 10: Migraciones de Base de Datos

**User Story:** Como DBA, quiero aplicar migraciones de base de datos de forma segura y sin downtime, para agregar las columnas necesarias para la integración.

#### Acceptance Criteria

1. THE migración SHALL agregar columna invoice_id VARCHAR(36) NULL a tabla appointments en clinical_schema
2. THE migración SHALL agregar columna appointment_id VARCHAR(36) NULL a tabla invoices en billing_schema
3. THE migración SHALL crear índice idx_appointments_invoice_id en appointments(invoice_id)
4. THE migración SHALL crear índice idx_invoices_appointment_id en invoices(appointment_id)
5. THE migración SHALL agregar comentarios SQL documentando que son referencias lógicas, no foreign keys
6. THE migración SHALL ser reversible (incluir script de rollback)
7. THE migración SHALL ejecutarse sin bloquear tablas (usar CONCURRENTLY para índices en PostgreSQL)
8. THE migración SHALL validar que las columnas no existen antes de intentar crearlas (idempotencia)

### Requirement 11: Estrategia de Despliegue sin Downtime

**User Story:** Como ingeniero de despliegue, quiero desplegar la integración sin causar downtime en el sistema, para mantener la disponibilidad del servicio.

#### Acceptance Criteria

1. THE despliegue SHALL seguir el orden: migraciones DB → Billing Service → Clinical Service
2. WHEN se despliega Billing Service actualizado, THE versión anterior SHALL seguir funcionando (backward compatibility)
3. WHEN se despliega Clinical Service actualizado, THE sistema SHALL funcionar con o sin la integración activa
4. THE sistema SHALL soportar feature flag billing.service.enabled para activar/desactivar la integración
5. WHEN billing.service.enabled=false, THE sistema SHALL usar el comportamiento anterior (factura temporal)
6. THE despliegue SHALL incluir smoke tests verificando: crear cita con billing activo, crear cita con billing inactivo
7. THE despliegue SHALL incluir plan de rollback documentado con pasos específicos
8. THE despliegue SHALL monitorear métricas durante 1 hora post-despliegue antes de considerar exitoso

### Requirement 12: Reconciliación Manual de Citas sin Factura

**User Story:** Como cajero, quiero poder crear manualmente facturas para citas que no tienen invoice_id, para reconciliar casos donde Billing Service estuvo no disponible.

#### Acceptance Criteria

1. THE sistema SHALL permitir consultar citas con invoice_id = NULL mediante endpoint GET /api/clinical/appointments?missingInvoice=true
2. WHEN el cajero crea manualmente una factura, THE sistema SHALL permitir actualizar appointments.invoice_id
3. WHEN se actualiza appointments.invoice_id manualmente, THE sistema SHALL validar que la factura existe en Billing Service
4. WHEN se actualiza appointments.invoice_id manualmente, THE sistema SHALL actualizar invoices.appointment_id en Billing Service
5. THE sistema SHALL registrar en logs la reconciliación manual con: appointmentId, invoiceId, usuario, timestamp
6. THE sistema SHALL exponer métrica reconciliations_total contando reconciliaciones manuales
7. THE sistema SHALL validar que la factura corresponde al mismo patientId de la cita
8. IF la factura no existe o no corresponde al paciente, THEN THE sistema SHALL retornar error 400 Bad Request

## Non-Functional Requirements

### Performance

- **NFR-1**: El tiempo de respuesta para crear una cita (incluyendo llamada a Billing Service) SHALL ser menor a 2 segundos en el percentil 95
- **NFR-2**: El timeout de llamada HTTP a Billing Service SHALL ser 5 segundos máximo
- **NFR-3**: El sistema SHALL soportar al menos 100 creaciones de citas concurrentes sin degradación

### Availability

- **NFR-4**: La disponibilidad de Clinical Service SHALL ser >= 99.9% independientemente del estado de Billing Service
- **NFR-5**: El circuit breaker SHALL prevenir que fallos en Billing Service afecten la disponibilidad de Clinical Service
- **NFR-6**: El sistema SHALL recuperarse automáticamente cuando Billing Service vuelva a estar disponible

### Reliability

- **NFR-7**: La tasa de éxito de creación de facturas automáticas SHALL ser >= 95% cuando Billing Service está disponible
- **NFR-8**: El sistema SHALL garantizar que NUNCA se crea una factura sin su cita correspondiente
- **NFR-9**: El sistema SHALL garantizar que una cita puede existir sin factura (compensación), pero una factura automática SIEMPRE tiene su cita

### Security

- **NFR-10**: Todas las llamadas entre Clinical Service y Billing Service SHALL incluir el userId para auditoría
- **NFR-11**: El sistema SHALL validar que el usuario tiene rol ADMISSION o ADMIN antes de crear citas
- **NFR-12**: El sistema SHALL NO exponer detalles internos de errores de Billing Service al frontend

### Maintainability

- **NFR-13**: El código de integración SHALL estar aislado en el componente BillingServiceClient
- **NFR-14**: La configuración de precios SHALL ser externalizada en application.yml
- **NFR-15**: El sistema SHALL incluir logs estructurados para facilitar debugging de problemas de integración

### Testability

- **NFR-16**: El sistema SHALL incluir al menos 5 property-based tests validando las propiedades de correctitud del diseño
- **NFR-17**: El sistema SHALL incluir integration tests con Billing Service mockeado simulando: éxito, timeout, error 500
- **NFR-18**: El sistema SHALL incluir tests del circuit breaker verificando transiciones de estado

## Business Rules

- **BR-1**: Una cita puede existir sin factura (invoice_id = NULL) cuando Billing Service falla
- **BR-2**: Una factura automática SIEMPRE debe tener su cita correspondiente (appointment_id NOT NULL)
- **BR-3**: El precio de consulta por defecto es 150.00 GTQ pero es configurable
- **BR-4**: Las facturas creadas automáticamente SIEMPRE tienen estado PENDING
- **BR-5**: Las facturas creadas automáticamente SIEMPRE tienen discountAmount = 0 inicialmente
- **BR-6**: El número de factura temporal (INV-yyyyMMddHHmmss) solo se usa cuando Billing Service falla
- **BR-7**: NO se permiten foreign keys entre clinical_schema y billing_schema (principio CERO JOINs)
- **BR-8**: Los mensajes de error SIEMPRE deben estar en español

## Dependencies

- **Clinical Service (8083)**: Servicio que inicia la integración al crear citas
- **Billing Service (8086)**: Servicio que crea facturas automáticamente
- **Patient Service (8082)**: Para validar que el paciente existe
- **API Gateway (8080)**: Para enrutamiento de peticiones HTTP
- **PostgreSQL**: Base de datos con esquemas clinical_schema y billing_schema
- **Redis**: Para gestión de slots de citas (no afectado por esta integración)
- **Resilience4j**: Librería para circuit breaker y retry patterns

---

**Created**: April 24, 2026  
**Author**: MedFlow Team  
**Status**: ✅ Ready for Review  
**Version**: 1.0.0
