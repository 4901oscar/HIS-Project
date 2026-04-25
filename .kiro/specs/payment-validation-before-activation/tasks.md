# Implementation Plan: Payment Validation Before Activation

## Overview

Este plan implementa la validación de pago antes de activar citas médicas, garantizando que solo pacientes con facturas pagadas (status = PAID) puedan ser activados en el sistema de triaje. La implementación incluye:

- Nuevo componente PaymentValidator para validación de pago
- Extensión de BillingServiceClient con método getInvoice()
- Modificación de AppointmentController para integrar validación
- Nuevo endpoint GET en Billing Service para consultar facturas
- Manejo de casos de compensación (invoice_id = NULL)
- Resiliencia con circuit breaker, retry y timeouts
- Property-based tests para validar propiedades de correctitud

**Lenguaje**: Java 17 con Spring Boot 3.x  
**Arquitectura**: Microservicios con DDD  
**Testing**: jqwik para property-based testing

---

## Tasks

### 1. Configurar dependencias y estructura base

- [x] 1.1 Agregar dependencia jqwik para property-based testing
  - Agregar en `clinical-service/pom.xml` y `billing-service/pom.xml`:
    ```xml
    <dependency>
        <groupId>net.jqwik</groupId>
        <artifactId>jqwik</artifactId>
        <version>1.7.4</version>
        <scope>test</scope>
    </dependency>
    ```
  - _Requirements: 10.1-10.8, NFR-16_

- [x] 1.2 Crear clases de excepción en Clinical Service
  - Crear `BillingServiceException.java` en `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/`
  - Crear `BillingServiceTimeoutException.java` en mismo paquete
  - Crear `PaymentValidationException.java` en `clinical-service/src/main/java/com/medframe/clinical/domain/exception/`
  - Crear `ServiceUnavailableException.java` en mismo paquete
  - Crear `InvalidAppointmentStatusException.java` en mismo paquete
  - _Requirements: 3.1-3.3, 5.3, 5.6_

- [x] 1.3 Crear enum PaymentValidationError
  - Crear `PaymentValidationError.java` en `clinical-service/src/main/java/com/medframe/clinical/domain/service/`
  - Definir valores: PAYMENT_PENDING, INVOICE_CANCELLED, INVOICE_NOT_FOUND, SERVICE_TIMEOUT, SERVICE_ERROR, UNKNOWN_STATUS
  - _Requirements: 6.3, 6.4, 8.1-8.5_

### 2. Implementar endpoint GET en Billing Service

- [x] 2.1 Crear método getInvoiceById() en InvoiceService
  - Ubicación: `billing-service/src/main/java/com/medframe/billing/service/InvoiceService.java`
  - Implementar búsqueda por ID con manejo de InvoiceNotFoundException
  - Retornar InvoiceResponse con todos los campos requeridos
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 2.2 Crear endpoint GET /api/billing/invoices/{id} en InvoiceController
  - Ubicación: `billing-service/src/main/java/com/medframe/billing/controller/InvoiceController.java`
  - Mapear a método getInvoice() que llama a InvoiceService.getInvoiceById()
  - Retornar 200 OK con InvoiceResponse o 404 Not Found
  - Registrar en logs el servicio que consultó (header X-Service-Name)
  - _Requirements: 4.1, 4.2, 4.3, 4.6_

- [x] 2.3 Crear excepción InvoiceNotFoundException
  - Ubicación: `billing-service/src/main/java/com/medframe/billing/exception/InvoiceNotFoundException.java`
  - Anotar con @ResponseStatus(HttpStatus.NOT_FOUND)
  - _Requirements: 4.3_

- [ ]* 2.4 Escribir unit tests para InvoiceService.getInvoiceById()
  - Ubicación: `billing-service/src/test/java/com/medframe/billing/service/InvoiceServiceTest.java`
  - Test: invoice existente retorna InvoiceResponse correcto
  - Test: invoice inexistente lanza InvoiceNotFoundException
  - Test: InvoiceResponse contiene todos los campos requeridos
  - _Requirements: 4.4, 6.8_

- [ ]* 2.5 Escribir integration tests para GET /api/billing/invoices/{id}
  - Ubicación: `billing-service/src/test/java/com/medframe/billing/controller/InvoiceControllerIntegrationTest.java`
  - Test: GET con ID válido retorna 200 OK
  - Test: GET con ID inválido retorna 404 Not Found
  - Test: Response contiene header X-Service-Name en logs
  - _Requirements: 10.1, 4.2, 4.3, 4.6_

### 3. Extender BillingServiceClient en Clinical Service

- [x] 3.1 Agregar método getInvoice() en BillingServiceClient
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/BillingServiceClient.java`
  - Implementar llamada GET a /api/billing/invoices/{id}
  - Agregar anotaciones @CircuitBreaker y @Retry
  - Manejar HttpClientErrorException.NotFound retornando null
  - Manejar ResourceAccessException lanzando BillingServiceTimeoutException
  - Manejar otras excepciones lanzando BillingServiceException
  - _Requirements: 3.1, 3.2, 3.3, 3.7_

- [x] 3.2 Implementar método fallback getInvoiceFallback()
  - Ubicación: mismo archivo BillingServiceClient.java
  - Registrar en logs que circuit breaker está abierto o retries agotados
  - Lanzar BillingServiceException
  - _Requirements: 3.1, 3.3_

- [x] 3.3 Configurar resilience4j para GET requests
  - Ubicación: `clinical-service/src/main/resources/application.yml`
  - Agregar instancia billingServiceGet con maxAttempts=2, waitDuration=500ms, exponentialBackoff
  - Configurar timeout de 3 segundos
  - _Requirements: 3.6, 3.7, NFR-2_

- [ ]* 3.4 Escribir unit tests para BillingServiceClient.getInvoice()
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/infrastructure/client/BillingServiceClientTest.java`
  - Test: llamada exitosa retorna InvoiceResponse
  - Test: 404 retorna null
  - Test: timeout lanza BillingServiceTimeoutException
  - Test: error 500 lanza BillingServiceException
  - Test: fallback se ejecuta cuando circuit breaker abre
  - _Requirements: 3.1, 3.2, 3.3_

### 4. Implementar PaymentValidator

- [x] 4.1 Crear clase PaymentValidationResult
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/domain/service/PaymentValidationResult.java`
  - Definir campos: allowed, error, errorMessage, invoice, hasWarning, warningMessage
  - Implementar métodos estáticos: success(), allowedWithoutInvoice(), allowedWithoutValidation(), failed()
  - _Requirements: 1.2-1.4, 2.1-2.4_

- [x] 4.2 Crear clase PaymentValidationConfig
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/domain/service/PaymentValidationConfig.java`
  - Anotar con @ConfigurationProperties(prefix = "payment.validation")
  - Definir campo enabled con valor por defecto true
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 4.3 Crear clase PaymentValidator
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/domain/service/PaymentValidator.java`
  - Inyectar BillingServiceClient y PaymentValidationConfig
  - Implementar método validatePayment(invoiceId, appointmentId)
  - Manejar caso invoice_id = NULL (retornar allowedWithoutInvoice)
  - Manejar caso validación deshabilitada (retornar allowedWithoutValidation)
  - Validar status de factura: PAID → success, PENDING → failed, CANCELLED → failed
  - Manejar excepciones de BillingServiceClient
  - Registrar logs en cada caso
  - _Requirements: 1.1-1.8, 2.1-2.3, 3.1-3.5, 6.1-6.6_

- [ ]* 4.4 Escribir unit tests para PaymentValidator
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/domain/service/PaymentValidatorTest.java`
  - Test: invoice_id NULL permite activación con warning
  - Test: validación deshabilitada permite activación con warning
  - Test: invoice PAID retorna success
  - Test: invoice PENDING retorna failed con error PAYMENT_PENDING
  - Test: invoice CANCELLED retorna failed con error INVOICE_CANCELLED
  - Test: invoice no encontrada retorna failed con error INVOICE_NOT_FOUND
  - Test: timeout retorna failed con error SERVICE_TIMEOUT
  - Test: error de servidor retorna failed con error SERVICE_ERROR
  - Test: mensajes de error están en español
  - _Requirements: 1.2-1.6, 2.1-2.4, 3.1-3.5, 8.1-8.5, NFR-18_

### 5. Modificar AppointmentController para integrar validación

- [x] 5.1 Inyectar PaymentValidator en AppointmentController
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/controller/AppointmentController.java`
  - Agregar campo privado final PaymentValidator paymentValidator
  - _Requirements: 5.1, 5.4_

- [x] 5.2 Modificar método activateAppointment() para incluir validación
  - Ubicación: mismo archivo AppointmentController.java
  - Después de validar que appointment existe y está en SCHEDULED
  - Llamar a paymentValidator.validatePayment(appointment.getInvoiceId(), appointment.getId())
  - Si validación falla, lanzar PaymentValidationException o ServiceUnavailableException según error
  - Si validación es exitosa, actualizar status a ACTIVE
  - Si hay warning, incluirlo en AppointmentResponse
  - _Requirements: 1.1-1.8, 5.2-5.8_

- [x] 5.3 Actualizar GlobalExceptionHandler para manejar nuevas excepciones
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/exception/GlobalExceptionHandler.java`
  - Agregar handler para PaymentValidationException (retornar 400 con errorCode)
  - Agregar handler para ServiceUnavailableException (retornar 503)
  - Agregar handler para InvalidAppointmentStatusException (retornar 400)
  - Incluir en ErrorResponse: timestamp, status, error, errorCode, message, path, requestId
  - _Requirements: 5.6, 8.1-8.8_

- [ ]* 5.4 Escribir unit tests para AppointmentController.activateAppointment()
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/controller/AppointmentControllerTest.java`
  - Test: activación exitosa con invoice PAID retorna 200 OK
  - Test: activación con invoice PENDING retorna 400 Bad Request
  - Test: activación con invoice CANCELLED retorna 400 Bad Request
  - Test: activación con invoice_id NULL retorna 200 OK con warning
  - Test: activación con Billing Service timeout retorna 503
  - Test: activación de cita no SCHEDULED retorna 400
  - Test: mensajes de error están en español
  - _Requirements: 5.2-5.8, 8.1-8.5_

### 6. Checkpoint - Validar implementación básica

- [x] 6. Checkpoint - Ensure all tests pass, ask the user if questions arise.

### 7. Implementar property-based tests

- [ ]* 7.1 Escribir Property 1: PAID Invoice Allows Activation
  - **Property 1: PAID Invoice Allows Activation**
  - **Validates: Requirements 1.2, 1.8, 5.5, 6.2**
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/properties/PaymentValidationPropertiesTest.java`
  - Generar appointments aleatorios con invoice PAID
  - Verificar que todos pueden activarse exitosamente
  - Verificar que status cambia a ACTIVE
  - Usar @Property con tries=100
  - _Requirements: 1.2, 1.8, 5.5, 6.2, NFR-16_

- [ ]* 7.2 Escribir Property 2: PENDING Invoice Blocks Activation
  - **Property 2: PENDING Invoice Blocks Activation**
  - **Validates: Requirements 1.3, 5.6, 6.3**
  - Ubicación: mismo archivo PaymentValidationPropertiesTest.java
  - Generar appointments aleatorios con invoice PENDING
  - Verificar que todos son rechazados con 400 y error PAYMENT_PENDING
  - Verificar que status permanece SCHEDULED
  - _Requirements: 1.3, 5.6, 6.3, NFR-16_

- [ ]* 7.3 Escribir Property 3: CANCELLED Invoice Blocks Activation
  - **Property 3: CANCELLED Invoice Blocks Activation**
  - **Validates: Requirements 1.4, 5.6, 6.4**
  - Ubicación: mismo archivo PaymentValidationPropertiesTest.java
  - Generar appointments aleatorios con invoice CANCELLED
  - Verificar que todos son rechazados con 400 y error INVOICE_CANCELLED
  - Verificar que status permanece SCHEDULED
  - _Requirements: 1.4, 5.6, 6.4, NFR-16_

- [ ]* 7.4 Escribir Property 4: NULL Invoice Allows Activation
  - **Property 4: NULL Invoice Allows Activation (Compensation Case)**
  - **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
  - Ubicación: mismo archivo PaymentValidationPropertiesTest.java
  - Generar appointments aleatorios con invoice_id = NULL
  - Verificar que todos pueden activarse sin llamar a BillingServiceClient
  - Verificar que response incluye warning
  - _Requirements: 2.1, 2.2, 2.3, 2.4, NFR-16_

- [ ]* 7.5 Escribir Property 5: Service Errors Return 503
  - **Property 5: Service Errors Return 503**
  - **Validates: Requirements 3.1, 3.2, 3.3, 5.6**
  - Ubicación: mismo archivo PaymentValidationPropertiesTest.java
  - Generar appointments aleatorios y mockear errores de servicio (timeout, 500)
  - Verificar que todos retornan 503 Service Unavailable
  - Verificar que status permanece SCHEDULED
  - _Requirements: 3.1, 3.2, 3.3, 5.6, NFR-16_

- [ ]* 7.6 Escribir Property 6: Non-SCHEDULED Appointments Cannot Be Activated
  - **Property 6: Non-SCHEDULED Appointments Cannot Be Activated**
  - **Validates: Requirements 5.2, 5.3**
  - Ubicación: mismo archivo PaymentValidationPropertiesTest.java
  - Generar appointments aleatorios en estados no SCHEDULED (ACTIVE, COMPLETED, CANCELLED)
  - Verificar que todos son rechazados con 400 Bad Request
  - _Requirements: 5.2, 5.3, NFR-16_

- [ ]* 7.7 Escribir Property 7: All Activations Are Logged
  - **Property 7: All Activations Are Logged**
  - **Validates: Requirements 1.7, 2.2, 6.6, 7.1, 7.2, 7.3**
  - Ubicación: mismo archivo PaymentValidationPropertiesTest.java
  - Generar intentos de activación aleatorios (exitosos y fallidos)
  - Verificar que todos generan log con campos requeridos: appointmentId, invoiceId, userId, resultado
  - Usar TestAppender para capturar logs
  - _Requirements: 1.7, 2.2, 6.6, 7.1, 7.2, 7.3, NFR-16_

- [ ]* 7.8 Escribir Property 9: Invoice Response Contains Required Fields
  - **Property 9: Invoice Response Contains Required Fields**
  - **Validates: Requirements 4.4, 6.8**
  - Ubicación: `billing-service/src/test/java/com/medframe/billing/properties/InvoicePropertiesTest.java`
  - Generar IDs de invoice aleatorios válidos
  - Verificar que response contiene todos los campos: id, invoiceNumber, patientId, appointmentId, status, total, createdAt
  - Verificar que status no es NULL
  - _Requirements: 4.4, 6.8, NFR-16_

- [ ]* 7.9 Escribir Property 10: Retry Mechanism Executes on Failures
  - **Property 10: Retry Mechanism Executes on Failures**
  - **Validates: Requirements 3.6**
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/properties/ResiliencePropertiesTest.java`
  - Generar requests aleatorios que fallan con excepciones retryables
  - Verificar que se ejecutan 2 intentos (inicial + 1 retry)
  - Usar AtomicInteger para contar intentos
  - _Requirements: 3.6, NFR-16_

### 8. Implementar integration tests con WireMock

- [ ]* 8.1 Configurar WireMock para simular Billing Service
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/integration/PaymentValidationIntegrationTest.java`
  - Configurar WireMock server en puerto aleatorio
  - Configurar application-test.yml para apuntar a WireMock
  - _Requirements: 10.8_

- [ ]* 8.2 Escribir integration test: activación exitosa con factura PAID
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Mockear GET /api/billing/invoices/{id} retornando invoice con status PAID
  - Llamar a PUT /api/clinical/appointments/{id}/activate
  - Verificar respuesta 200 OK y status ACTIVE
  - _Requirements: 10.1_

- [ ]* 8.3 Escribir integration test: rechazo con factura PENDING
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Mockear GET retornando invoice con status PENDING
  - Verificar respuesta 400 Bad Request con mensaje correcto
  - _Requirements: 10.2_

- [ ]* 8.4 Escribir integration test: rechazo con factura CANCELLED
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Mockear GET retornando invoice con status CANCELLED
  - Verificar respuesta 400 Bad Request con mensaje correcto
  - _Requirements: 10.3_

- [ ]* 8.5 Escribir integration test: activación exitosa con invoice_id NULL
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Crear appointment con invoice_id = NULL
  - Verificar respuesta 200 OK con warning
  - Verificar que WireMock NO recibió llamada
  - _Requirements: 10.4_

- [ ]* 8.6 Escribir integration test: manejo de timeout
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Mockear GET con delay > 3 segundos
  - Verificar respuesta 503 Service Unavailable
  - _Requirements: 10.5_

- [ ]* 8.7 Escribir integration test: manejo de 404 Not Found
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Mockear GET retornando 404
  - Verificar respuesta 400 Bad Request con mensaje "factura no existe"
  - _Requirements: 10.6_

- [ ]* 8.8 Escribir integration test: manejo de 500 Server Error
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Mockear GET retornando 500
  - Verificar respuesta 503 Service Unavailable
  - _Requirements: 10.7_

- [ ]* 8.9 Escribir integration test: retry mechanism
  - Ubicación: mismo archivo PaymentValidationIntegrationTest.java
  - Mockear GET fallando en primer intento, exitoso en segundo
  - Verificar que se ejecutan 2 llamadas a WireMock
  - Verificar respuesta 200 OK
  - _Requirements: 10.8_

### 9. Configuración y deployment

- [x] 9.1 Agregar configuración de payment validation en application.yml
  - Ubicación: `clinical-service/src/main/resources/application.yml`
  - Agregar sección payment.validation.enabled con valor por defecto true
  - Agregar variable de entorno PAYMENT_VALIDATION_ENABLED
  - _Requirements: 9.1, 9.2, 9.3, 9.4_

- [x] 9.2 Agregar configuración de resilience4j para billingServiceGet
  - Ubicación: mismo archivo application.yml
  - Configurar retry: maxAttempts=2, waitDuration=500ms, exponentialBackoff
  - Configurar timeout: 3 segundos
  - Configurar circuit breaker: failureRateThreshold=50%, waitDurationInOpenState=30s
  - _Requirements: 3.6, 3.7, NFR-2_

- [x] 9.3 Agregar logging estructurado en PaymentValidator
  - Ubicación: `clinical-service/src/main/resources/logback-spring.xml`
  - Configurar formato JSON para logs de PaymentValidator
  - Incluir campos: timestamp, level, logger, message, appointmentId, invoiceId, userId, duration_ms
  - _Requirements: 7.1-7.8, NFR-15_

- [x] 9.4 Verificar índice en tabla invoices
  - Ubicación: `billing-service/src/main/resources/db/migration/`
  - Verificar que existe índice en invoices.id (debería existir por ser PK)
  - Si no existe, crear migration con CREATE INDEX
  - _Requirements: 4.8_

- [x] 9.5 Agregar métricas con Micrometer
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/domain/service/PaymentValidator.java`
  - Agregar contador payment_validation_results_total con label status
  - Agregar contador payment_validation_failures_total con label reason
  - Agregar contador appointments_activated_without_invoice_total
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/BillingServiceClient.java`
  - Agregar timer billing_service_get_invoice_duration_seconds
  - _Requirements: 2.6, 3.8, 6.7, NFR-16_

### 12. Implementar QR Status Endpoint

- [x] 12.1 Crear QRStatusResponse DTO
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/application/dto/QRStatusResponse.java`
  - Definir campos: appointmentId, patientName, doctorName, appointmentDate, appointmentTime, appointmentStatus, paymentStatus, message, canActivate, invoiceNumber, warning
  - Usar anotaciones @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
  - _Requirements: 10.7_

- [x] 12.2 Implementar endpoint GET /api/clinical/appointments/{id}/qr-status
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/application/controller/AppointmentController.java`
  - Validar que appointment existe
  - Llamar a PaymentValidator para obtener estado de pago
  - Construir QRStatusResponse con mensaje apropiado según estado
  - Manejar casos: PAID, PENDING, CANCELLED, NO_INVOICE, ERROR
  - Mensajes en español: "Cita pagada - Puede activarse", "Cita no pagada - Debe pagar en caja primero", etc.
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8_

- [ ]* 12.3 Escribir unit tests para QR Status endpoint
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/controller/AppointmentControllerTest.java`
  - Test: QR con factura PAID retorna canActivate=true y mensaje correcto
  - Test: QR con factura PENDING retorna canActivate=false y mensaje correcto
  - Test: QR con factura CANCELLED retorna canActivate=false y mensaje correcto
  - Test: QR con invoice_id NULL retorna canActivate=true con warning
  - Test: QR con appointment inexistente retorna 404
  - Test: mensajes están en español
  - _Requirements: 10.1-10.8_

### 13. Implementar Reception UI con estado de pago

- [ ] 13.1 Crear AppointmentWithPaymentStatusResponse DTO
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/application/dto/AppointmentWithPaymentStatusResponse.java`
  - Definir campos básicos: id, patientName, doctorName, appointmentDate, appointmentTime, status
  - Definir campos de pago: paymentStatus, paymentStatusLabel, paymentStatusColor, canActivate, activateButtonTooltip, invoiceNumber
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [x] 13.2 Modificar endpoint GET /api/clinical/appointments/today
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/application/controller/AppointmentController.java`
  - Cambiar retorno a List<AppointmentWithPaymentStatusResponse>
  - Implementar método mapToAppointmentWithPaymentStatus()
  - Para cada appointment, validar estado de pago y asignar:
    - PAID: label="PAGADA", color="green", canActivate=true, tooltip="Cita pagada - Puede activarse"
    - PENDING: label="PENDIENTE", color="orange", canActivate=false, tooltip="El paciente debe pagar en caja primero"
    - CANCELLED: label="CANCELADA", color="red", canActivate=false, tooltip="Factura cancelada - Contacte administración"
    - NO_INVOICE: label="SIN FACTURA", color="gray", canActivate=true, tooltip="Cita sin factura - Activar bajo responsabilidad"
  - Manejar excepciones asignando status="ERROR"
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

- [ ]* 13.3 Escribir unit tests para Reception UI endpoint
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/controller/AppointmentControllerTest.java`
  - Test: appointment con factura PAID retorna indicadores correctos (green, enabled)
  - Test: appointment con factura PENDING retorna indicadores correctos (orange, disabled)
  - Test: appointment con factura CANCELLED retorna indicadores correctos (red, disabled)
  - Test: appointment con invoice_id NULL retorna indicadores correctos (gray, enabled)
  - Test: appointment con error de validación retorna indicadores de error (red, disabled)
  - Test: tooltips están en español
  - _Requirements: 11.1-11.6_

### 14. Implementar property-based tests adicionales

- [ ]* 14.1 Escribir Property 11: QR Status Returns Correct Payment Information
  - **Property 11: QR Status Returns Correct Payment Information**
  - **Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8**
  - Ubicación: `clinical-service/src/test/java/com/medframe/clinical/properties/PaymentValidationPropertiesTest.java`
  - Generar appointments aleatorios con varios estados de pago
  - Verificar que QRStatusResponse contiene campos correctos
  - Verificar que canActivate flag coincide con estado de pago
  - Verificar que mensajes están en español
  - _Requirements: 10.1-10.8, NFR-16_

- [ ]* 14.2 Escribir Property 12: Reception UI Shows Correct Payment Status
  - **Property 12: Reception UI Shows Correct Payment Status**
  - **Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8**
  - Ubicación: mismo archivo PaymentValidationPropertiesTest.java
  - Generar listas de appointments aleatorios con varios estados de pago
  - Verificar que cada appointment tiene indicadores UI correctos
  - Verificar colores: green (PAID), orange (PENDING), red (CANCELLED), gray (NO_INVOICE)
  - Verificar tooltips están en español
  - _Requirements: 11.1-11.8, NFR-16_

### 15. Checkpoint final - Validación completa

- [ ] 15. Checkpoint - Ensure all tests pass, ask the user if questions arise.

### 16. Documentación y deployment

- [ ] 16.1 Actualizar README.md de Clinical Service
  - Ubicación: `clinical-service/README.md`
  - Documentar nueva funcionalidad de validación de pago
  - Documentar configuración payment.validation.enabled
  - Documentar nuevas métricas y logs
  - Documentar nuevo endpoint GET /api/clinical/appointments/{id}/qr-status
  - Documentar endpoint mejorado GET /api/clinical/appointments/today
  - _Requirements: NFR-15_

- [ ] 16.2 Actualizar README.md de Billing Service
  - Ubicación: `billing-service/README.md`
  - Documentar nuevo endpoint GET /api/billing/invoices/{id}
  - Documentar formato de InvoiceResponse
  - _Requirements: 4.1-4.4_

- [ ] 16.3 Crear guía de deployment
  - Ubicación: `.kiro/specs/payment-validation-before-activation/DEPLOYMENT.md`
  - Documentar estrategia de deployment en 4 fases
  - Documentar plan de rollback con feature flag
  - Documentar métricas a monitorear
  - Documentar alertas a configurar
  - Documentar nuevas funcionalidades: QR inteligente y Reception UI
  - _Requirements: NFR-4, NFR-5, NFR-6_

- [ ] 16.4 Crear guía de usuario para QR Scanner
  - Ubicación: `.kiro/specs/payment-validation-before-activation/QR_USER_GUIDE.md`
  - Documentar cómo interpretar mensajes del QR Scanner
  - Documentar qué hacer en cada escenario (pagada, no pagada, cancelada, sin factura)
  - Incluir capturas de pantalla de ejemplo
  - _Requirements: 10.1-10.8_

- [ ] 16.5 Crear guía de usuario para Reception UI
  - Ubicación: `.kiro/specs/payment-validation-before-activation/RECEPTION_UI_GUIDE.md`
  - Documentar cómo interpretar colores y estados en la lista de citas
  - Documentar cuándo usar botón de activación
  - Documentar filtros por estado de pago
  - Incluir capturas de pantalla de ejemplo
  - _Requirements: 11.1-11.8_

---

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties (12 properties total)
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end flows with mocked Billing Service
- Feature flag `payment.validation.enabled` permite deployment gradual y rollback rápido
- Todos los mensajes de error están en español según requirements
- La implementación sigue principios DDD con PaymentValidator como domain service
- Circuit breaker y retry garantizan resiliencia ante fallos de Billing Service
- Casos de compensación (invoice_id = NULL) se manejan sin bloquear activación
- QR inteligente muestra estado de pago al escanear código
- Reception UI muestra indicadores visuales de estado de pago para cada cita
- Nuevas funcionalidades mejoran UX para recepcionistas y pacientes
