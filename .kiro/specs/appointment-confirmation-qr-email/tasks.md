# Implementation Plan: Appointment Confirmation QR & Email

## Overview

Este plan implementa la generación de códigos QR únicos y el envío de emails de confirmación para citas médicas en el sistema MedFlow HIS. La implementación sigue una arquitectura hexagonal y se divide en 5 fases principales: infraestructura de QR, integración de email, mejoras al dominio, capa REST, y mejoras al frontend.

**Tecnologías**: Java 17, Spring Boot, ZXing 3.5.1, Jackson, Feign, React/TypeScript

**Arquitectura**: Hexagonal (Ports & Adapters)

**Servicios involucrados**: clinical-service, auth-service, frontend-medflow

## Tasks

- [x] 1. Configurar infraestructura de generación de QR
  - [x] 1.1 Agregar dependencias ZXing al pom.xml de clinical-service
    - Agregar `com.google.zxing:core:3.5.1`
    - Agregar `com.google.zxing:javase:3.5.1`
    - Ubicación: `backend-services/clinical-service/pom.xml`
    - _Requirements: 8.1, 8.2_

  - [x] 1.2 Crear modelo de dominio AppointmentQRData
    - Crear clase `AppointmentQRData` en `domain/model/`
    - Incluir campos: appointmentId, patientId, doctorId, date (ISO 8601), time (ISO 8601), generatedAt (ISO 8601 con timezone)
    - Implementar método estático `fromAppointment(Appointment)`
    - Agregar anotaciones Lombok (@Data, @NoArgsConstructor, @AllArgsConstructor)
    - _Requirements: 1.2, 6.1, 6.2, 6.3, 6.4_

  - [x] 1.3 Crear interfaz QRCodeGenerator en capa de dominio
    - Crear interfaz `QRCodeGenerator` en `domain/port/out/`
    - Definir método `String generateQRCode(AppointmentQRData appointmentData)`
    - Documentar que retorna base64-encoded PNG o null si falla
    - _Requirements: 1.1, 1.3_

  - [x] 1.4 Implementar ZXingQRGenerator en capa de infraestructura
    - Crear clase `ZXingQRGenerator` en `infrastructure/qr/`
    - Implementar interfaz `QRCodeGenerator`
    - Configurar QR con error correction level L (15%), UTF-8, tamaño 300x300
    - Serializar AppointmentQRData a JSON usando ObjectMapper
    - Generar QR usando QRCodeWriter y MatrixToImageWriter
    - Codificar imagen PNG a base64
    - Manejar excepciones gracefully (retornar null, log error)
    - _Requirements: 1.1, 1.3, 1.4, 1.5, 1.6, 6.5, 9.1, 9.2, 9.3_

  - [ ]* 1.5 Escribir unit tests para ZXingQRGenerator
    - Test: QR generado contiene todos los campos requeridos
    - Test: QR es base64 válido que decodifica a PNG válido
    - Test: Manejo de caracteres UTF-8 (acentos, ñ)
    - Test: Manejo de excepciones retorna null
    - _Requirements: 1.2, 1.3, 9.3_

  - [ ]* 1.6 Escribir property test para round-trip de QR
    - **Property 2: QR Generation Round-Trip Preserves Data**
    - **Validates: Requirements 1.4, 9.4, 10.5**
    - Generar AppointmentQRData aleatorio
    - Generar QR, decodificar, deserializar JSON
    - Verificar que datos deserializados son equivalentes a originales
    - Mínimo 100 iteraciones

- [ ] 2. Checkpoint - Verificar generación de QR
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 3. Implementar integración de email entre clinical-service y auth-service
  - [ ] 3.1 Crear DTOs para comunicación de email
    - Crear `AppointmentEmailRequest` en `clinical-service/infrastructure/client/dto/`
    - Incluir campos: toEmail, firstName, appointmentDate, appointmentTime, doctorName, invoiceNumber, qrCodeBase64, notes, validFromTime, validUntilTime
    - Agregar anotaciones Lombok (@Data, @Builder, @NoArgsConstructor, @AllArgsConstructor)
    - Duplicar DTO en `auth-service/dto/` para recepción
    - _Requirements: 5.3, 8.8_

  - [ ] 3.2 Crear interfaz AppointmentEmailSender en capa de dominio
    - Crear interfaz `AppointmentEmailSender` en `clinical-service/domain/port/out/`
    - Definir método `void sendAppointmentConfirmationEmail(AppointmentEmailRequest request)`
    - Documentar que es asíncrono y no lanza excepciones
    - _Requirements: 2.6, 4.2_

  - [ ] 3.3 Crear EmailServiceFeignClient en clinical-service
    - Crear interfaz `EmailServiceFeignClient` en `infrastructure/client/`
    - Anotar con @FeignClient(name="auth-service", url="${services.auth-service.url}")
    - Definir método POST `/api/emails/appointment-confirmation`
    - Configurar timeout: connect 2s, read 5s
    - _Requirements: 5.1, 5.6_

  - [ ] 3.4 Implementar EmailClientAdapter en clinical-service
    - Crear clase `EmailClientAdapter` en `infrastructure/client/`
    - Implementar interfaz `AppointmentEmailSender`
    - Inyectar `EmailServiceFeignClient`
    - Anotar método con @Async
    - Manejar FeignException sin lanzar (log error)
    - Implementar timeout de 5 segundos
    - _Requirements: 2.7, 4.2, 5.6, 7.2, 7.3_

  - [ ] 3.5 Configurar Feign y @Async en clinical-service
    - Agregar configuración en `application.yml`: services.auth-service.url
    - Configurar Feign timeouts (connect: 2000ms, read: 5000ms)
    - Configurar thread pool para @Async (core: 2, max: 5, queue: 100)
    - Agregar @EnableAsync en clase de configuración
    - Agregar variable de entorno AUTH_SERVICE_URL para Docker
    - _Requirements: 8.4, 8.5, 8.6_

  - [ ]* 3.6 Escribir unit tests para EmailClientAdapter
    - Test: Llamada exitosa a Feign client
    - Test: FeignException no se propaga (log error)
    - Test: Timeout no bloquea flujo
    - Mock EmailServiceFeignClient
    - _Requirements: 2.7, 5.6, 7.2_

- [ ] 4. Implementar endpoint de email en auth-service
  - [ ] 4.1 Crear EmailController en auth-service
    - Crear clase `EmailController` en `controller/`
    - Anotar con @RestController, @RequestMapping("/api/emails")
    - Implementar endpoint POST `/appointment-confirmation`
    - Aceptar @RequestBody AppointmentEmailRequest
    - Delegar a EmailService
    - Retornar ResponseEntity.ok() inmediatamente (async)
    - _Requirements: 5.4_

  - [ ] 4.2 Agregar método sendAppointmentConfirmationEmail a EmailService
    - Agregar método en `EmailService` existente
    - Anotar con @Async
    - Parámetros: toEmail, firstName, appointmentDate, appointmentTime, doctorName, invoiceNumber, qrCodeBase64, notes, validFromTime, validUntilTime
    - Crear MimeMessage con MimeMessageHelper(multipart=true, UTF-8)
    - Configurar from, to, subject ("Confirmación de Cita — MedFlow HIS")
    - Llamar a buildAppointmentConfirmationHtml()
    - Enviar email usando JavaMailSender
    - Manejar MessagingException (log error)
    - _Requirements: 2.1, 2.2, 2.6, 8.3_

  - [ ] 4.3 Crear template HTML para email de confirmación
    - Implementar método privado `buildAppointmentConfirmationHtml()` en EmailService
    - Incluir header con branding MedFlow (color #0f4c75, logo text)
    - Incluir saludo con nombre del paciente
    - Incluir tabla con detalles: fecha, hora, doctor, número de factura
    - Incluir sección de notas (condicional si notes != null)
    - Incluir QR embebido: `<img src="data:image/png;base64,{qrCodeBase64}">`
    - Incluir instrucciones de ventana de tiempo: "QR válido desde {validFromTime} hasta {validUntilTime}"
    - Incluir footer con copyright
    - Usar mismo estilo que emails existentes del sistema
    - _Requirements: 2.2, 2.3, 2.4, 2.5, 2.6_

  - [ ]* 4.4 Escribir unit tests para generación de HTML de email
    - Test: HTML contiene todos los campos requeridos
    - Test: QR está embebido correctamente como data URI
    - Test: Ventana de tiempo está presente
    - Test: Sección de notas es condicional
    - Test: Caracteres especiales son escapados correctamente
    - _Requirements: 2.2, 2.3, 2.5_

  - [ ]* 4.5 Escribir property test para contenido de email
    - **Property 9: Email HTML Contains Required Information**
    - **Validates: Requirements 2.2, 2.3, 2.5**
    - Generar datos de cita aleatorios
    - Generar HTML de email
    - Verificar que HTML contiene: nombre paciente, fecha, hora, doctor, factura, QR embebido, ventana de tiempo
    - Mínimo 100 iteraciones

- [ ] 5. Checkpoint - Verificar integración de email
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Mejorar servicios de dominio para incluir QR y notificaciones
  - [x] 6.1 Agregar campo transient qrCodeBase64 a Appointment entity
    - Agregar campo `@Transient private String qrCodeBase64` en clase Appointment
    - Agregar getter y setter
    - Documentar que no se persiste en BD
    - _Requirements: 4.3_

  - [x] 6.2 Agregar estado MISSED a AppointmentStatus enum
    - Agregar valor `MISSED` a enum AppointmentStatus
    - Ubicación: `domain/model/Appointment.java` o archivo separado
    - _Requirements: 13.1_

  - [x] 6.3 Agregar método markAsMissed() a Appointment entity
    - Implementar método `public void markAsMissed()`
    - Validar que estado actual es SCHEDULED
    - Lanzar IllegalStateException si estado no es SCHEDULED
    - Transicionar estado a MISSED
    - Mensaje de error: "Solo se pueden marcar como perdidas las citas programadas. Estado actual: {status}"
    - _Requirements: 13.3, 13.4_

  - [x] 6.4 Agregar método activate() a Appointment entity (si no existe)
    - Implementar método `public void activate()`
    - Validar que estado actual es SCHEDULED
    - Lanzar IllegalStateException si estado no es SCHEDULED
    - Transicionar estado a ACTIVE
    - Mensaje de error: "Solo se pueden activar citas con estado PROGRAMADO. Estado actual: {status}"
    - _Requirements: 13.2, 13.4_

  - [ ] 6.5 Crear modelo de dominio ScanResult
    - Crear clase `ScanResult` en `domain/model/`
    - Crear enum interno `Status { EARLY, ACTIVE, MISSED }`
    - Incluir campos: status, message, appointment, scanTime
    - Agregar anotaciones Lombok (@Data, @AllArgsConstructor)
    - _Requirements: 11.7, 12.3_

  - [x] 6.6 Agregar método createAppointmentWithNotification a AppointmentManager
    - Agregar método en `AppointmentManager` (domain service)
    - Parámetros: patientId, doctorId, date, time, notes, createdBy, skipPatientValidation, patientEmail, patientFirstName, doctorName, invoiceNumber
    - Llamar a createAppointment() existente
    - Generar QR: crear AppointmentQRData, llamar a qrGenerator.generateQRCode()
    - Asignar qrCodeBase64 a appointment (try-catch, log error si falla)
    - Calcular ventana de tiempo: validFrom = time - 15min, validUntil = time + 60min
    - Construir AppointmentEmailRequest con todos los datos
    - Llamar a emailSender.sendAppointmentConfirmationEmail() (try-catch, log error si falla)
    - Retornar appointment con qrCodeBase64 poblado
    - _Requirements: 1.6, 2.1, 4.1, 4.2, 4.4, 7.1, 7.2, 7.5_

  - [x] 6.7 Agregar método validateAndActivateAppointment a AppointmentManager
    - Agregar método en `AppointmentManager`
    - Parámetros: appointmentId, scanTime
    - Recuperar appointment de repositorio (lanzar AppointmentNotFoundException si no existe)
    - Calcular appointmentDateTime combinando date y time
    - Calcular windowStart = appointmentDateTime - 15min
    - Calcular windowEnd = appointmentDateTime + 60min
    - Si scanTime < windowStart: retornar ScanResult(EARLY, "QR disponible a las {windowStart.time}")
    - Si scanTime > windowEnd: llamar appointment.markAsMissed(), guardar, retornar ScanResult(MISSED, "Cita perdida")
    - Si dentro de ventana y estado SCHEDULED: llamar appointment.activate(), guardar, retornar ScanResult(ACTIVE, "Cita activa")
    - Si dentro de ventana y estado ACTIVE: retornar ScanResult(ACTIVE, "Cita activa") sin cambiar estado (idempotente)
    - Si dentro de ventana y estado MISSED: retornar ScanResult(MISSED, "Cita perdida") sin cambiar estado (idempotente)
    - Si estado no es SCHEDULED/ACTIVE/MISSED: lanzar IllegalStateException
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 13.5, 13.6_

  - [ ]* 6.8 Escribir unit tests para transiciones de estado
    - Test: markAsMissed() transiciona SCHEDULED → MISSED
    - Test: markAsMissed() lanza excepción si estado no es SCHEDULED
    - Test: activate() transiciona SCHEDULED → ACTIVE
    - Test: activate() lanza excepción si estado no es SCHEDULED
    - _Requirements: 13.2, 13.3, 13.4_

  - [ ]* 6.9 Escribir property test para validación de ventana de tiempo
    - **Property 4: Time Window Validation Determines Scan Result**
    - **Validates: Requirements 11.1, 11.2, 11.4, 11.5, 11.6**
    - Generar appointmentTime y scanTime aleatorios
    - Calcular windowStart y windowEnd
    - Llamar a validateAndActivateAppointment()
    - Verificar que status es EARLY si scanTime < windowStart
    - Verificar que status es ACTIVE si windowStart ≤ scanTime ≤ windowEnd
    - Verificar que status es MISSED si scanTime > windowEnd
    - Mínimo 100 iteraciones

  - [ ]* 6.10 Escribir property test para transiciones de estado válidas
    - **Property 5: State Transitions Follow Valid Paths**
    - **Validates: Requirements 13.2, 13.3, 13.4**
    - Generar estado actual y estado objetivo aleatorios
    - Intentar transición
    - Verificar que transiciones válidas no lanzan excepción
    - Verificar que transiciones inválidas lanzan IllegalStateException
    - Transiciones válidas: SCHEDULED→ACTIVE, SCHEDULED→MISSED, SCHEDULED→CANCELLED, ACTIVE→COMPLETED, ACTIVE→CANCELLED
    - Mínimo 100 iteraciones

  - [ ]* 6.11 Escribir property test para idempotencia de escaneo
    - **Property 7: Scan Result Idempotence**
    - **Validates: Requirements 13.5, 13.6**
    - Crear appointment en estado ACTIVE o MISSED
    - Escanear QR múltiples veces
    - Verificar que status y mensaje no cambian
    - Verificar que estado de appointment no cambia
    - Mínimo 100 iteraciones

- [ ] 7. Checkpoint - Verificar lógica de dominio
  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Implementar capa REST para escaneo de QR
  - [x] 8.1 Crear DTOs para escaneo de QR
    - Crear `QRScanRequest` en `infrastructure/rest/dto/`
    - Incluir campos: appointmentId, patientId, doctorId, date, time, generatedAt
    - Crear `ScanResultDTO` en `infrastructure/rest/dto/`
    - Incluir campos: status, message, appointmentDetails (AppointmentDTO), timestamp
    - Implementar método estático `fromDomain(ScanResult)` en ScanResultDTO
    - _Requirements: 12.2, 12.3_

  - [x] 8.2 Crear QRScanController
    - Crear clase `QRScanController` en `infrastructure/rest/controller/`
    - Anotar con @RestController, @RequestMapping("/api/clinical/appointments")
    - Inyectar ManageAppointmentUseCase
    - Implementar endpoint POST `/scan-qr`
    - Aceptar @RequestBody QRScanRequest
    - Validar que appointmentId no es null/blank (retornar 400 si inválido)
    - Obtener scanTime = LocalDateTime.now()
    - Llamar a useCase.scanAndActivateAppointment(appointmentId, scanTime)
    - Convertir ScanResult a ScanResultDTO
    - Retornar ResponseEntity.ok(dto)
    - Manejar AppointmentNotFoundException → retornar 404
    - Manejar Exception genérica → retornar 500
    - _Requirements: 12.1, 12.2, 12.7, 12.8, 12.9_

  - [x] 8.3 Agregar método scanAndActivateAppointment a ManageAppointmentUseCase
    - Agregar método en interfaz `ManageAppointmentUseCase` (port in)
    - Firma: `ScanResult scanAndActivateAppointment(String appointmentId, LocalDateTime scanTime)`
    - Implementar en clase de caso de uso delegando a AppointmentManager.validateAndActivateAppointment()
    - _Requirements: 12.1_

  - [ ]* 8.4 Escribir integration tests para endpoint de escaneo
    - Test: Escaneo dentro de ventana retorna 200 con status ACTIVE
    - Test: Escaneo antes de ventana retorna 200 con status EARLY
    - Test: Escaneo después de ventana retorna 200 con status MISSED
    - Test: Escaneo con appointmentId inexistente retorna 404
    - Test: Escaneo con appointmentId null retorna 400
    - Usar @SpringBootTest y MockMvc
    - _Requirements: 12.7, 12.8, 12.9_

  - [ ]* 8.5 Escribir property test para respuesta de escaneo
    - **Property 12: Scan Response Contains Required Fields**
    - **Validates: Requirements 12.3**
    - Generar QRScanRequest aleatorio (válido e inválido)
    - Llamar a endpoint de escaneo
    - Verificar que respuesta contiene: status, message, appointmentDetails (si encontrado), timestamp
    - Mínimo 100 iteraciones

  - [ ]* 8.6 Escribir property test para mensaje de escaneo temprano
    - **Property 13: Early Scan Message Includes Valid-From Time**
    - **Validates: Requirements 11.4, 12.4**
    - Generar appointment con tiempo futuro
    - Escanear antes de ventana (status EARLY)
    - Verificar que mensaje incluye hora exacta de validFrom (appointmentTime - 15min)
    - Mínimo 100 iteraciones

- [x] 9. Actualizar AppointmentController para incluir QR en respuesta
  - [x] 9.1 Actualizar AppointmentResponse DTO
    - Agregar campo `private String qrCodeBase64` a AppointmentResponse
    - Actualizar método `fromDomain(Appointment)` para incluir qrCodeBase64
    - _Requirements: 4.3_

  - [x] 9.2 Modificar método createAppointment en AppointmentController
    - Obtener datos adicionales para email: patientEmail, patientFirstName, doctorName
    - Generar invoiceNumber (obtener de billing service o generar temporal)
    - Reemplazar llamada a appointmentManager.createAppointment() por createAppointmentWithNotification()
    - Pasar todos los parámetros adicionales (email, firstName, doctorName, invoiceNumber)
    - Convertir appointment a AppointmentResponse (incluye qrCodeBase64)
    - Retornar ResponseEntity con status 201 y response
    - _Requirements: 4.1, 4.3, 4.5_

  - [ ]* 9.3 Escribir integration test para creación de cita con QR
    - Test: POST /appointments retorna 201 con qrCodeBase64 en respuesta
    - Test: Appointment se guarda en BD con estado SCHEDULED
    - Test: Email service fue llamado (verificar mock)
    - Test: Si QR falla, appointment se crea sin qrCodeBase64
    - Usar @SpringBootTest y MockMvc
    - _Requirements: 4.1, 4.3, 7.1_

- [x] 10. Checkpoint - Verificar integración backend completa
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Implementar mejoras en frontend para mostrar QR
  - [x] 11.1 Actualizar interfaz AppointmentResponse en appointmentService.ts
    - Agregar campo opcional `qrCodeBase64?: string` a interfaz AppointmentResponse
    - Ubicación: `frontend-medflow/src/services/appointmentService.ts`
    - _Requirements: 3.1_

  - [x] 11.2 Agregar función calculateTimeWindow en PaymentGatewayPage
    - Implementar función que recibe appointmentTime (string HH:mm:ss)
    - Parsear hora y minutos
    - Calcular validFrom = appointmentTime - 15 minutos
    - Calcular validUntil = appointmentTime + 60 minutos
    - Retornar objeto con validFrom y validUntil en formato 12h (AM/PM)
    - Ubicación: `frontend-medflow/src/pages/PaymentGatewayPage.tsx`
    - _Requirements: 3.5_

  - [x] 11.3 Agregar función downloadQR en PaymentGatewayPage
    - Implementar función que crea elemento <a> dinámicamente
    - Configurar href con data URI: `data:image/png;base64,{qrCodeBase64}`
    - Configurar download con nombre: `cita-{appointmentId}.png`
    - Agregar a DOM, hacer click, remover de DOM
    - _Requirements: 3.6_

  - [x] 11.4 Agregar sección de QR en pantalla de éxito de PaymentGatewayPage
    - Renderizar sección condicional si `appointment?.qrCodeBase64` existe
    - Mostrar título: "Tu código QR de confirmación"
    - Mostrar imagen QR: `<img src="data:image/png;base64,{qrCodeBase64}">`
    - Aplicar estilos: width 200px, border 2px medin-cyan, border-radius 8px
    - Mostrar ventana de tiempo: "⏰ QR válido desde {validFrom} hasta {validUntil}"
    - Mostrar instrucciones: "Presenta este código en recepción el día de tu cita"
    - Agregar botón "Descargar QR" que llama a downloadQR()
    - Aplicar estilos consistentes con diseño existente
    - _Requirements: 3.2, 3.3, 3.4, 3.5, 3.6, 3.7_

  - [x] 11.5 Manejar caso donde qrCodeBase64 no está presente
    - Verificar que pantalla de éxito funciona sin QR
    - Mostrar solo detalles de cita si qrCodeBase64 es undefined
    - No mostrar errores ni mensajes de fallo
    - _Requirements: 7.4_

- [x] 12. Checkpoint - Verificar integración frontend
  - Ensure all tests pass, ask the user if questions arise.

- [x] 13. Configuración y deployment
  - [x] 13.1 Configurar variables de entorno para clinical-service
    - Agregar `services.auth-service.url: ${AUTH_SERVICE_URL:http://localhost:8081}` en application.yml
    - Agregar configuración de thread pool para @Async en application.yml
    - Documentar variable AUTH_SERVICE_URL en README o .env.example
    - _Requirements: 8.4, 8.5, 8.6_

  - [x] 13.2 Configurar Docker Compose para comunicación entre servicios
    - Agregar variable de entorno `AUTH_SERVICE_URL=http://auth-service:8081` en clinical-service
    - Verificar que auth-service y clinical-service están en misma red Docker
    - _Requirements: 8.6_

  - [x] 13.3 Verificar configuración de SMTP en auth-service
    - Confirmar que JavaMailSender está configurado correctamente
    - Verificar que MimeMessageHelper soporta multipart=true
    - Verificar que charset UTF-8 está configurado
    - _Requirements: 8.3_

- [ ] 14. Testing end-to-end y validación
  - [ ]* 14.1 Escribir E2E test para flujo completo de creación de cita
    - Test: Paciente agenda cita → recibe QR en respuesta → recibe email
    - Verificar que QR es válido y decodificable
    - Verificar que email contiene QR embebido
    - Usar test email server (ej: GreenMail) para capturar email
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ]* 14.2 Escribir E2E test para flujo de escaneo de QR
    - Test: Crear cita → escanear QR dentro de ventana → verificar estado ACTIVE
    - Test: Crear cita → escanear QR antes de ventana → verificar status EARLY
    - Test: Crear cita → escanear QR después de ventana → verificar estado MISSED
    - _Requirements: 11.1, 11.2, 11.5, 11.6_

  - [ ]* 14.3 Escribir property test para serialización JSON round-trip
    - **Property 11: JSON Serialization Round-Trip Preserves Appointment Data**
    - **Validates: Requirements 9.1, 9.2, 9.3, 9.4**
    - Generar AppointmentQRData aleatorio con caracteres UTF-8
    - Serializar a JSON
    - Deserializar de JSON
    - Verificar que objeto deserializado es equivalente al original
    - Mínimo 100 iteraciones

  - [ ]* 14.4 Escribir property test para cálculo consistente de ventana de tiempo
    - **Property 10: Time Window Calculation is Consistent**
    - **Validates: Requirements 2.5, 3.5, 11.2**
    - Generar appointmentTime aleatorio
    - Calcular ventana en backend (AppointmentManager)
    - Calcular ventana en email (EmailService)
    - Calcular ventana en frontend (PaymentGatewayPage)
    - Verificar que validFrom y validUntil son idénticos en los 3 lugares
    - Mínimo 100 iteraciones

  - [ ]* 14.5 Escribir property test para request de email completo
    - **Property 8: Email Request Contains All Required Fields**
    - **Validates: Requirements 5.3**
    - Generar appointment aleatorio
    - Construir AppointmentEmailRequest
    - Verificar que todos los campos requeridos están presentes y no vacíos
    - Campos: toEmail, firstName, appointmentDate, appointmentTime, doctorName, invoiceNumber, qrCodeBase64, validFromTime, validUntilTime
    - Mínimo 100 iteraciones

- [ ] 15. Checkpoint final - Validación completa del sistema
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Las tareas marcadas con `*` son opcionales y pueden omitirse para un MVP más rápido
- Cada tarea referencia los requisitos específicos para trazabilidad
- Los checkpoints aseguran validación incremental del progreso
- Los property tests validan propiedades universales de corrección
- Los unit tests validan ejemplos específicos y casos borde
- La implementación sigue arquitectura hexagonal estricta
- El manejo de errores es graceful: fallos en QR o email no bloquean creación de cita
- La comunicación entre servicios es asíncrona para email
- El frontend maneja gracefully la ausencia de QR
