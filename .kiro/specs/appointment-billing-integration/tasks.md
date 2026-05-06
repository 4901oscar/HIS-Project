# Implementation Plan: Appointment-Billing Integration

## Overview

Este plan implementa la integración automática entre Clinical Service y Billing Service para crear facturas PENDING al agendar citas médicas. La implementación sigue una arquitectura de microservicios con DDD, comunicación REST síncrona, y patrones de resiliencia (circuit breaker, retry, compensación).

**Lenguaje**: Java 17 con Spring Boot 3.x  
**Arquitectura**: Microservicios con DDD  
**Principio**: CERO JOINs entre esquemas

## Tasks

- [x] 1. Preparar migraciones de base de datos
  - Crear script SQL para agregar columna `invoice_id` a tabla `appointments` en `clinical_schema`
  - Crear script SQL para agregar columna `appointment_id` a tabla `invoices` en `billing_schema`
  - Crear índices: `idx_appointments_invoice_id` e `idx_invoices_appointment_id`
  - Agregar comentarios SQL documentando referencias lógicas (no foreign keys)
  - Validar idempotencia de migraciones (verificar que columnas no existan antes de crearlas)
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 10.1, 10.2, 10.3, 10.4, 10.5, 10.8_

- [x] 2. Actualizar entidades JPA en ambos servicios
  - [x] 2.1 Actualizar entidad Appointment en Clinical Service
    - Agregar campo `private String invoiceId` con anotación `@Column(name = "invoice_id")`
    - Agregar getters y setters para `invoiceId`
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/domain/model/Appointment.java`
    - _Requirements: 5.1, 5.3_
  
  - [x] 2.2 Actualizar entidad Invoice en Billing Service
    - Agregar campo `private String appointmentId` con anotación `@Column(name = "appointment_id")`
    - Agregar getters y setters para `appointmentId`
    - Ubicación: `billing-service/src/main/java/com/medframe/billing/domain/model/Invoice.java`
    - _Requirements: 5.2, 5.4_

- [x] 3. Crear DTOs para integración en Clinical Service
  - [x] 3.1 Crear CreateInvoiceRequest DTO
    - Campos: `patientId` (String), `appointmentId` (String), `charges` (List<ChargeRequest>)
    - Agregar anotaciones de validación: `@NotNull`, `@NotEmpty`
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/dto/CreateInvoiceRequest.java`
    - _Requirements: 8.1, 8.5_
  
  - [x] 3.2 Crear ChargeRequest DTO
    - Campos: `type` (String), `description` (String), `quantity` (Integer), `unitPrice` (BigDecimal)
    - Validar que `type` sea uno de: CONSULTATION, LABORATORY, MEDICATION, OTHER
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/dto/ChargeRequest.java`
    - _Requirements: 8.2, 8.5_
  
  - [x] 3.3 Crear InvoiceResponse DTO
    - Campos: `id`, `invoiceNumber`, `patientId`, `appointmentId`, `charges`, `subtotal`, `discountAmount`, `total`, `status`, `createdAt`, `createdBy`
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/dto/InvoiceResponse.java`
    - _Requirements: 8.3, 8.6_
  
  - [x] 3.4 Crear ChargeResponse DTO
    - Campos: `id`, `type`, `description`, `quantity`, `unitPrice`, `subtotal`
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/dto/ChargeResponse.java`
    - _Requirements: 8.4_

- [x] 4. Implementar configuración de precios de consulta
  - [x] 4.1 Crear ConsultationPriceConfig bean
    - Usar `@ConfigurationProperties(prefix = "consultation.price")`
    - Campos: `defaultPrice`, `emergency`, `followup` (BigDecimal)
    - Método `getPrice()` que retorna `defaultPrice`
    - Valor por defecto: 150.00 GTQ
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/config/ConsultationPriceConfig.java`
    - _Requirements: 4.1, 4.2, 4.3, 4.4_
  
  - [x] 4.2 Agregar configuración en application.yml
    - Sección `consultation.price` con valores: default, emergency, followup
    - Validar que precio sea mayor a cero
    - _Requirements: 4.1, 4.3, 4.6_

- [x] 5. Configurar RestTemplate con timeouts
  - Crear RestTemplateConfig bean con `RestTemplateBuilder`
  - Configurar `connectTimeout` y `readTimeout` de 5 segundos
  - Leer timeouts desde `application.yml` (propiedad `rest.template.connection.timeout`)
  - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/config/RestTemplateConfig.java`
  - _Requirements: 3.7, NFR-2_

- [x] 6. Implementar BillingServiceClient con resiliencia
  - [x] 6.1 Crear BillingServiceClient component
    - Inyectar `RestTemplate` y `billingServiceUrl` desde configuración
    - Método `createInvoice(CreateInvoiceRequest, String userId)` que retorna `InvoiceResponse`
    - Agregar anotaciones `@CircuitBreaker` y `@Retry` de Resilience4j
    - Incluir headers: `X-User-Id`, `Content-Type: application/json`
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/BillingServiceClient.java`
    - _Requirements: 1.1, 1.2, 3.1, 8.7, 8.8_
  
  - [x] 6.2 Implementar método fallback createInvoiceFallback
    - Retornar `null` cuando circuit breaker está abierto o retries se agotan
    - Registrar log de nivel ERROR con detalles del fallo
    - _Requirements: 2.1, 2.2, 3.3_
  
  - [x] 6.3 Crear BillingServiceException
    - Extender `RuntimeException`
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/infrastructure/client/BillingServiceException.java`
    - _Requirements: 3.1_

- [x] 7. Configurar Resilience4j en Clinical Service
  - [x] 7.1 Agregar configuración de circuit breaker en application.yml
    - Instancia `billingService` con: `slidingWindowSize=10`, `minimumNumberOfCalls=5`, `failureRateThreshold=50`, `waitDurationInOpenState=30s`
    - Configurar excepciones a registrar: `HttpServerErrorException`, `ResourceAccessException`, `BillingServiceException`
    - _Requirements: 3.1, 3.2, 3.4, 3.5_
  
  - [x] 7.2 Agregar configuración de retry en application.yml
    - Instancia `billingService` con: `maxAttempts=3`, `waitDuration=1s`, `exponentialBackoffMultiplier=2`
    - Configurar excepciones a reintentar
    - _Requirements: 3.6_
  
  - [x] 7.3 Agregar configuración de Billing Service URL
    - Propiedad `billing.service.url` con valor por defecto `http://localhost:8086`
    - _Requirements: 1.1_

- [x] 8. Checkpoint - Validar configuración y componentes base
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Modificar AppointmentController.createAppointment()
  - [x] 9.1 Agregar lógica de creación de factura después de crear cita
    - Construir `CreateInvoiceRequest` con `patientId`, `appointmentId`, y cargo CONSULTATION
    - Llamar a `billingServiceClient.createInvoice(request, userId)`
    - Si retorna `InvoiceResponse` exitosa: guardar `invoiceId` en appointment y usar `invoiceNumber` real
    - Si retorna `null` (fallback): dejar `invoiceId` como NULL y usar número temporal
    - Registrar logs apropiados (INFO para éxito, WARN para fallback)
    - Ubicación: `clinical-service/src/main/java/com/medframe/clinical/application/controller/AppointmentController.java`
    - _Requirements: 1.1, 1.2, 1.3, 2.1, 2.3_
  
  - [x] 9.2 Implementar método generateTemporaryInvoiceNumber()
    - Formato: `INV-yyyyMMddHHmmss`
    - Usar `LocalDateTime.now().format(DateTimeFormatter.ofPattern(...))`
    - _Requirements: 2.3, BR-6_
  
  - [x] 9.3 Actualizar llamada a attachQRAndNotify con invoiceNumber correcto
    - Pasar `invoiceNumber` real si billing exitoso, temporal si falló
    - _Requirements: 1.4, 1.5, 2.4_

- [ ]* 9.4 Escribir unit tests para AppointmentController
    - Test: crear cita con Billing Service exitoso → verificar `invoiceId` guardado
    - Test: crear cita con Billing Service fallido → verificar `invoiceId` NULL
    - Test: validar que se llama a `billingServiceClient.createInvoice()` con parámetros correctos
    - _Requirements: 1.1, 2.1, NFR-17_

- [x] 10. Actualizar Billing Service para recibir appointmentId
  - [x] 10.1 Modificar CreateInvoiceRequest en Billing Service
    - Agregar campo `appointmentId` (String, nullable)
    - Ubicación: `billing-service/src/main/java/com/medframe/billing/application/dto/CreateInvoiceRequest.java`
    - _Requirements: 7.1, 8.1_
  
  - [x] 10.2 Modificar InvoiceService.createInvoice()
    - Guardar `appointmentId` en entidad Invoice: `invoice.setAppointmentId(request.getAppointmentId())`
    - Ubicación: `billing-service/src/main/java/com/medframe/billing/domain/service/InvoiceService.java`
    - _Requirements: 1.8, 5.4, 7.3_
  
  - [x] 10.3 Modificar InvoiceResponse en Billing Service
    - Agregar campo `appointmentId` en respuesta
    - Ubicación: `billing-service/src/main/java/com/medframe/billing/application/dto/InvoiceResponse.java`
    - _Requirements: 7.6, 8.3_

- [ ]* 10.4 Escribir unit tests para InvoiceService
    - Test: crear factura con `appointmentId` → verificar que se guarda correctamente
    - Test: crear factura sin `appointmentId` → verificar que acepta NULL
    - _Requirements: 1.8, 5.4_

- [x] 11. Checkpoint - Validar integración básica
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 12. Implementar property-based tests
  - [ ]* 12.1 Property test: Bidirectional Reference Consistency
    - **Property 1: Bidirectional Reference Consistency**
    - **Validates: Requirements 5.3, 5.4, 5.5, 5.6**
    - Generar appointments aleatorios con `invoiceId` no nulo
    - Verificar que existe invoice con `id = appointment.invoiceId` y `appointmentId = appointment.id`
    - Usar librería jqwik o QuickTheories
    - _Requirements: 5.3, 5.4, 5.5, 5.6_
  
  - [ ]* 12.2 Property test: Invoice Created for Every Successful Appointment
    - **Property 2: Invoice Created for Every Successful Appointment**
    - **Validates: Requirements 1.1, 1.2, 1.3, 1.6, 1.8**
    - Mockear `billingServiceClient` retornando éxito
    - Crear appointment y verificar que `invoiceId` no es NULL
    - Verificar que invoice existe con estado PENDING y `appointmentId` correcto
    - _Requirements: 1.1, 1.2, 1.3, 1.6, 1.8_
  
  - [ ]* 12.3 Property test: Appointment Created Even When Billing Fails
    - **Property 3: Appointment Created Even When Billing Fails (Compensation)**
    - **Validates: Requirements 2.1, 2.2, 2.6, 2.7**
    - Mockear `billingServiceClient` lanzando excepción
    - Crear appointment y verificar que se crea con `invoiceId = NULL`
    - Verificar que estado es SCHEDULED
    - _Requirements: 2.1, 2.2, 2.6, 2.7_
  
  - [ ]* 12.4 Property test: Invoice Total Equals Consultation Price
    - **Property 4: Invoice Total Equals Consultation Price**
    - **Validates: Requirements 4.3, 4.4, 4.5**
    - Generar appointments aleatorios con billing exitoso
    - Verificar que `invoice.total` equals `consultationPriceConfig.getPrice()`
    - Verificar que `invoice.discountAmount` equals 0
    - _Requirements: 4.3, 4.4, 4.5_
  
  - [ ]* 12.5 Property test: Circuit Breaker Opens After Threshold
    - **Property 5: Circuit Breaker Opens After Threshold**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.7**
    - Simular 5 fallos consecutivos de `billingServiceClient`
    - Verificar que circuit breaker transiciona a estado OPEN
    - Verificar que siguiente llamada falla rápido (< 100ms)
    - _Requirements: 3.1, 3.2, 3.3, 3.7_

- [ ]* 13. Implementar integration tests
  - [ ]* 13.1 Integration test: End-to-end flow con Billing Service exitoso
    - Usar `@SpringBootTest` con `@MockBean` para `billingServiceClient`
    - Crear appointment completo y verificar invoice creado
    - Verificar QR code contiene número de factura real
    - _Requirements: 1.1, 1.3, 1.4_
  
  - [ ]* 13.2 Integration test: End-to-end flow con Billing Service fallido
    - Mockear `billingServiceClient` retornando NULL
    - Crear appointment y verificar que se crea sin invoice
    - Verificar QR code contiene número temporal
    - _Requirements: 2.1, 2.3, 2.4_
  
  - [ ]* 13.3 Integration test: Circuit breaker behavior
    - Simular múltiples fallos consecutivos
    - Verificar transiciones: CLOSED → OPEN → HALF_OPEN → CLOSED
    - _Requirements: 3.1, 3.2, 3.4, 3.5_

- [ ] 14. Implementar métricas y observabilidad
  - [x] 14.1 Agregar métricas de Micrometer
    - Métrica: `billing_service_calls_total` con labels `status` (success, failure, timeout)
    - Métrica: `appointments_without_invoice_total` (contador)
    - Métrica: `billing_service_call_duration_seconds` (timer con percentiles)
    - Ubicación: Dentro de `BillingServiceClient` usando `MeterRegistry`
    - _Requirements: 9.1, 9.3, 9.4_
  
  - [x] 14.2 Configurar logs estructurados
    - Log INFO: "Invoice created successfully for appointment {id}: {invoiceNumber}"
    - Log WARN: "Billing Service unavailable. Appointment {id} created without invoice."
    - Log ERROR: "Circuit breaker OPEN for Billing Service"
    - _Requirements: 9.8_

- [x] 15. Implementar endpoint de reconciliación manual
  - [x] 15.1 Agregar endpoint GET /api/clinical/appointments?missingInvoice=true
    - Filtrar appointments con `invoiceId = NULL`
    - Retornar lista de appointments sin factura
    - Ubicación: `AppointmentController`
    - _Requirements: 12.1_
  
  - [x] 15.2 Agregar endpoint PATCH /api/clinical/appointments/{id}/invoice
    - Recibir `invoiceId` en request body
    - Validar que invoice existe en Billing Service
    - Validar que invoice corresponde al mismo `patientId`
    - Actualizar `appointments.invoice_id`
    - Llamar a Billing Service para actualizar `invoices.appointment_id`
    - Registrar log de reconciliación manual
    - _Requirements: 12.2, 12.3, 12.4, 12.5, 12.7_

- [ ]* 15.3 Escribir tests para endpoints de reconciliación
    - Test: consultar appointments sin invoice
    - Test: actualizar invoice_id con validación exitosa
    - Test: rechazar actualización si invoice no existe
    - Test: rechazar actualización si patientId no coincide
    - _Requirements: 12.1, 12.2, 12.7, 12.8_

- [x] 16. Checkpoint - Validar funcionalidad completa
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 17. Preparar documentación de despliegue
  - [x] 17.1 Crear script de aplicación de migraciones
    - Script SQL para clinical_schema (agregar invoice_id)
    - Script SQL para billing_schema (agregar appointment_id)
    - Instrucciones de rollback
    - _Requirements: 10.6, 10.7_
  
  - [x] 17.2 Documentar orden de despliegue
    - Fase 1: Aplicar migraciones de base de datos
    - Fase 2: Desplegar Billing Service actualizado
    - Fase 3: Desplegar Clinical Service actualizado
    - Fase 4: Monitorear métricas por 1 hora
    - _Requirements: 11.1, 11.2, 11.3, 11.8_
  
  - [x] 17.3 Documentar plan de rollback
    - Pasos para revertir cambios en caso de problemas
    - Feature flag `billing.service.enabled` para desactivar integración
    - _Requirements: 11.7_

- [x] 18. Implementar feature flag para activar/desactivar integración
  - Agregar propiedad `billing.service.enabled` en application.yml (default: true)
  - Modificar `AppointmentController` para verificar flag antes de llamar a Billing Service
  - Si flag es false, usar comportamiento anterior (número temporal)
  - _Requirements: 11.4, 11.5_

- [x] 19. Final checkpoint - Validación completa
  - Ensure all tests pass, ask the user if questions arise.
  - Verificar que todas las migraciones están listas
  - Verificar que todos los tests pasan (unit, integration, property-based)
  - Verificar que métricas están configuradas
  - Verificar que documentación de despliegue está completa

## Notes

- Tasks marcadas con `*` son opcionales (tests) y pueden omitirse para MVP más rápido
- Cada task referencia requirements específicos para trazabilidad
- Checkpoints aseguran validación incremental
- Property tests validan propiedades universales de correctitud
- Unit tests validan ejemplos específicos y casos edge
- Integration tests validan flujo end-to-end
- La implementación sigue principios DDD y arquitectura de microservicios
- CERO foreign keys entre esquemas (solo referencias lógicas)
- Circuit breaker y retry garantizan resiliencia
- Estrategia de compensación garantiza disponibilidad de Clinical Service
