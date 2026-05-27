# Requirements Document

## Introduction

Este documento especifica los requisitos para implementar la generación de códigos QR únicos y el envío de emails de confirmación para citas médicas agendadas, completando los pasos 12-13 del caso de uso CU-00 "Visualización del Portal Web y Agendamiento en Línea".

Actualmente, el sistema permite agendar citas y procesar pagos exitosamente, pero no genera códigos QR ni envía confirmaciones por correo electrónico al paciente. Esta funcionalidad es esencial para cumplir con el flujo completo especificado en el caso de uso.

## Glossary

- **QR_Generator**: Componente responsable de generar códigos QR únicos para cada cita
- **Email_Service**: Servicio existente en auth-service que envía correos electrónicos
- **Appointment_Manager**: Servicio de dominio que gestiona la lógica de negocio de citas
- **Clinical_Service**: Microservicio que maneja las operaciones clínicas incluyendo citas
- **Auth_Service**: Microservicio que maneja autenticación y envío de emails
- **Frontend**: Aplicación React que muestra la interfaz al paciente
- **Payment_Gateway_Page**: Pantalla del frontend que muestra la confirmación de pago
- **QR_Code**: Código de respuesta rápida que contiene información de la cita en formato legible por escáneres estándar
- **Confirmation_Email**: Correo electrónico enviado al paciente con los detalles de la cita y el código QR

## Requirements

### Requirement 1: Generación de Código QR Único

**User Story:** Como paciente, quiero recibir un código QR único para mi cita, para que pueda presentarlo fácilmente el día de mi visita al hospital.

#### Acceptance Criteria

1. WHEN una cita es creada exitosamente en el sistema, THE QR_Generator SHALL generar un código QR único en formato PNG
2. THE QR_Code SHALL contener el ID de la cita, fecha, hora, ID del paciente, y ID del doctor asignado en formato JSON
3. THE QR_Code SHALL ser codificado en base64 para facilitar su transmisión y almacenamiento
4. THE QR_Code SHALL ser legible por cualquier lector de códigos QR estándar que cumpla con ISO/IEC 18004
5. WHEN el QR_Generator recibe datos de cita válidos, THE QR_Generator SHALL completar la generación en menos de 500 milisegundos
6. IF la generación del código QR falla, THEN THE Clinical_Service SHALL registrar el error en los logs y continuar con el flujo sin bloquear la creación de la cita

### Requirement 2: Envío de Email de Confirmación

**User Story:** Como paciente, quiero recibir un email de confirmación con los detalles de mi cita y el código QR, para que tenga un registro permanente y pueda acceder a la información cuando la necesite.

#### Acceptance Criteria

1. WHEN una cita es creada exitosamente y el código QR es generado, THE Clinical_Service SHALL invocar al Email_Service para enviar el email de confirmación
2. THE Confirmation_Email SHALL incluir la fecha de la cita, hora, motivo de consulta, nombre del doctor asignado, y número de factura
3. THE Confirmation_Email SHALL incluir el código QR embebido como imagen en el cuerpo del email
4. THE Confirmation_Email SHALL incluir instrucciones indicando que el código QR estará disponible para escaneo desde 15 minutos antes de la hora de la cita hasta 60 minutos después
5. THE Confirmation_Email SHALL mostrar la ventana de tiempo válida para el escaneo (ej: "QR válido desde 13:45 hasta 15:00" para una cita a las 14:00)
6. THE Confirmation_Email SHALL utilizar el mismo estilo HTML y diseño visual que los emails existentes del sistema
6. THE Email_Service SHALL enviar el email de forma asíncrona sin bloquear la respuesta HTTP al cliente
7. IF el envío del email falla, THEN THE Clinical_Service SHALL registrar el error en los logs sin afectar la confirmación de la cita al usuario

### Requirement 3: Visualización del Código QR en Pantalla

**User Story:** Como paciente, quiero ver el código QR inmediatamente después de confirmar mi pago, para que pueda guardarlo en mi dispositivo sin esperar el email.

#### Acceptance Criteria

1. WHEN el pago es procesado exitosamente, THE Frontend SHALL recibir el código QR en formato base64 desde el Clinical_Service
2. THE Payment_Gateway_Page SHALL mostrar el código QR como imagen en la pantalla de confirmación de pago
3. THE Payment_Gateway_Page SHALL mostrar el código QR junto con los detalles de la cita existentes (fecha, hora, número de factura)
4. THE Payment_Gateway_Page SHALL mostrar instrucciones indicando que el QR estará disponible para escaneo desde 15 minutos antes de la hora de la cita
5. THE Payment_Gateway_Page SHALL calcular y mostrar la hora exacta a partir de la cual el QR será válido (hora_cita - 15 minutos)
6. THE Frontend SHALL permitir al paciente descargar el código QR como archivo PNG mediante un botón de descarga
7. WHILE el paciente está en la pantalla de confirmación, THE Frontend SHALL mantener el código QR visible y accesible

### Requirement 4: Integración con el Flujo de Creación de Citas

**User Story:** Como desarrollador del sistema, quiero que la generación del QR y el envío del email se integren sin problemas en el flujo existente, para que no se interrumpa la experiencia del usuario.

#### Acceptance Criteria

1. WHEN el Appointment_Manager crea una cita exitosamente, THE Clinical_Service SHALL generar el código QR antes de retornar la respuesta al cliente
2. THE Clinical_Service SHALL invocar el envío del email de forma asíncrona después de generar el código QR
3. THE Clinical_Service SHALL incluir el código QR en base64 en la respuesta HTTP al Frontend
4. IF la generación del QR o el envío del email fallan, THEN THE Clinical_Service SHALL completar la creación de la cita y retornar una respuesta exitosa al cliente
5. THE Clinical_Service SHALL registrar en los logs cada paso del proceso (generación de QR, invocación de email) para facilitar el debugging

### Requirement 5: Comunicación entre Microservicios

**User Story:** Como arquitecto del sistema, quiero que la comunicación entre clinical-service y auth-service sea robusta y mantenga la arquitectura hexagonal, para que el sistema sea mantenible y escalable.

#### Acceptance Criteria

1. THE Clinical_Service SHALL invocar al Email_Service del Auth_Service mediante una llamada HTTP REST
2. THE Clinical_Service SHALL incluir el código QR en base64 en el payload de la solicitud al Email_Service
3. THE Clinical_Service SHALL incluir todos los datos necesarios para el email (datos del paciente, datos de la cita, número de factura) en la solicitud
4. THE Email_Service SHALL exponer un nuevo endpoint POST /api/emails/appointment-confirmation para recibir solicitudes de confirmación de citas
5. THE Clinical_Service SHALL implementar un cliente HTTP en la capa de infraestructura sin contaminar el dominio con anotaciones de Spring
6. IF la llamada HTTP al Auth_Service falla o excede el timeout de 5 segundos, THEN THE Clinical_Service SHALL registrar el error y continuar sin reintentar

### Requirement 6: Formato y Contenido del Código QR

**User Story:** Como personal de recepción del hospital, quiero que el código QR contenga toda la información necesaria para identificar la cita, para que pueda procesar rápidamente a los pacientes que llegan.

#### Acceptance Criteria

1. THE QR_Code SHALL contener un objeto JSON con los campos: appointmentId, patientId, doctorId, date, time, y generatedAt
2. THE QR_Code SHALL utilizar el formato de fecha ISO 8601 (YYYY-MM-DD) para el campo date
3. THE QR_Code SHALL utilizar el formato de hora ISO 8601 (HH:mm:ss) para el campo time
4. THE QR_Code SHALL incluir un timestamp de generación en formato ISO 8601 con zona horaria
5. THE QR_Code SHALL tener un nivel de corrección de errores de al menos 15% (nivel L de QR) para garantizar legibilidad
6. THE QR_Generator SHALL validar que todos los campos requeridos estén presentes antes de generar el código

### Requirement 11: Activación de Cita mediante Escaneo de QR

**User Story:** Como paciente, quiero activar mi cita escaneando el código QR en recepción dentro de la ventana de tiempo permitida, para que el sistema registre mi llegada y el doctor sepa que estoy presente.

#### Acceptance Criteria

1. WHEN un código QR es escaneado, THE Clinical_Service SHALL validar la hora actual contra la hora de la cita
2. THE Clinical_Service SHALL permitir el escaneo del QR desde 15 minutos antes de la hora de la cita hasta 60 minutos después de la hora de inicio
3. WHERE la hora de la cita es 14:00, THE Clinical_Service SHALL aceptar escaneos desde las 13:45 hasta las 15:00
4. IF el QR es escaneado antes de la ventana permitida (más de 15 minutos antes), THEN THE Clinical_Service SHALL retornar el mensaje "QR disponible a las [hora_inicio_ventana]" donde hora_inicio_ventana es 15 minutos antes de la cita
5. IF el QR es escaneado dentro de la ventana permitida (desde 15 minutos antes hasta 60 minutos después), THEN THE Clinical_Service SHALL activar la cita, cambiar su estado a "ACTIVE", y retornar el mensaje "Cita activa"
6. IF el QR es escaneado después de la ventana permitida (más de 60 minutos después del inicio), THEN THE Clinical_Service SHALL marcar la cita como "MISSED" y retornar el mensaje "Cita perdida"
7. THE Clinical_Service SHALL registrar en los logs cada intento de escaneo con timestamp, appointmentId, y resultado de la validación

### Requirement 12: Endpoint de Escaneo de QR

**User Story:** Como sistema de recepción, quiero un endpoint REST para escanear códigos QR y activar citas, para que pueda integrar lectores de QR en la aplicación de recepción.

#### Acceptance Criteria

1. THE Clinical_Service SHALL exponer un endpoint POST /api/clinical/appointments/scan-qr que acepta el contenido del código QR
2. THE endpoint SHALL aceptar un payload JSON con el campo "qrContent" que contiene el JSON deserializado del código QR
3. THE endpoint SHALL retornar un objeto JSON con los campos: status (EARLY, ACTIVE, MISSED), message, appointmentDetails, y timestamp
4. WHERE status es "EARLY", THE message SHALL incluir la hora exacta a partir de la cual el QR será válido
5. WHERE status es "ACTIVE", THE Clinical_Service SHALL actualizar el estado de la cita a "ACTIVE" en la base de datos
6. WHERE status es "MISSED", THE Clinical_Service SHALL actualizar el estado de la cita a "MISSED" en la base de datos
7. THE endpoint SHALL retornar HTTP 200 para escaneos exitosos con cualquier status (EARLY, ACTIVE, MISSED)
8. THE endpoint SHALL retornar HTTP 404 si el appointmentId del QR no existe en el sistema
9. THE endpoint SHALL retornar HTTP 400 si el contenido del QR es inválido o está malformado

### Requirement 13: Estados de Cita y Transiciones

**User Story:** Como administrador del sistema, quiero que los estados de las citas reflejen correctamente su ciclo de vida, para que pueda hacer seguimiento y generar reportes precisos.

#### Acceptance Criteria

1. WHEN una cita es creada y pagada, THE Clinical_Service SHALL asignar el estado inicial "SCHEDULED"
2. WHEN un QR es escaneado exitosamente dentro de la ventana permitida, THE Clinical_Service SHALL transicionar el estado de "SCHEDULED" a "ACTIVE"
3. WHEN un QR es escaneado después de la ventana permitida, THE Clinical_Service SHALL transicionar el estado de "SCHEDULED" a "MISSED"
4. THE Clinical_Service SHALL prevenir transiciones de estado inválidas (ej: de "ACTIVE" a "SCHEDULED")
5. IF una cita ya está en estado "ACTIVE", THEN escaneos adicionales del QR SHALL retornar "Cita activa" sin cambiar el estado
6. IF una cita ya está en estado "MISSED", THEN escaneos adicionales del QR SHALL retornar "Cita perdida" sin cambiar el estado
7. THE Clinical_Service SHALL registrar un timestamp de activación cuando una cita transiciona a "ACTIVE"

### Requirement 7: Manejo de Errores y Resiliencia

**User Story:** Como usuario del sistema, quiero que mi cita se confirme exitosamente incluso si hay problemas técnicos con el QR o el email, para que no pierda mi reservación por fallos secundarios.

#### Acceptance Criteria

1. IF el QR_Generator lanza una excepción, THEN THE Clinical_Service SHALL registrar el error, continuar con el flujo, y retornar la cita sin código QR
2. IF el Email_Service no está disponible o retorna un error, THEN THE Clinical_Service SHALL registrar el error y completar la creación de la cita
3. THE Clinical_Service SHALL utilizar un patrón de circuit breaker o timeout para las llamadas al Email_Service
4. THE Frontend SHALL manejar gracefully el caso donde la respuesta no incluye código QR mostrando solo los detalles de la cita
5. THE Clinical_Service SHALL garantizar que la transacción de creación de cita se complete exitosamente independientemente de fallos en QR o email

### Requirement 8: Configuración y Dependencias

**User Story:** Como desarrollador del sistema, quiero que las dependencias necesarias estén correctamente configuradas, para que el sistema funcione sin problemas en todos los entornos.

#### Acceptance Criteria

1. THE Clinical_Service SHALL incluir la dependencia de ZXing (com.google.zxing:core) versión 3.5.1 o superior en su pom.xml
2. THE Clinical_Service SHALL incluir la dependencia de ZXing JavaSE (com.google.zxing:javase) versión 3.5.1 o superior para generación de imágenes PNG
3. THE Email_Service SHALL configurar el soporte para imágenes embebidas en emails HTML mediante MimeMessageHelper con multipart=true
4. THE Clinical_Service SHALL configurar la URL del Auth_Service mediante propiedades externalizadas en application.yml
5. WHERE el entorno es desarrollo local, THE Clinical_Service SHALL utilizar http://localhost:8081 como URL del Auth_Service
6. WHERE el entorno es Docker, THE Clinical_Service SHALL utilizar http://auth-service:8081 como URL del Auth_Service

## Special Notes

### Parser and Serializer Requirements

Este feature requiere serialización y deserialización de datos JSON para el contenido del código QR:

**Requirement 9: Serialización de Datos del QR**

**User Story:** Como desarrollador, quiero serializar los datos de la cita a JSON para incluirlos en el código QR, para que la información sea estructurada y fácil de procesar.

#### Acceptance Criteria

1. THE QR_Generator SHALL serializar el objeto de datos de cita a formato JSON utilizando Jackson ObjectMapper
2. WHEN un objeto de datos de cita válido es proporcionado, THE QR_Generator SHALL producir una cadena JSON válida según RFC 8259
3. THE QR_Generator SHALL manejar caracteres especiales y acentos en los datos mediante codificación UTF-8
4. FOR ALL objetos de datos de cita válidos, serializar a JSON y luego deserializar SHALL producir un objeto equivalente (round-trip property)
5. IF la serialización falla, THEN THE QR_Generator SHALL lanzar una excepción específica QRGenerationException con un mensaje descriptivo

**Requirement 10: Deserialización de Datos del QR**

**User Story:** Como sistema de recepción, quiero deserializar el contenido del código QR escaneado, para que pueda extraer los datos de la cita y procesarlos.

#### Acceptance Criteria

1. WHEN un código QR válido es escaneado, THE Clinical_Service SHALL deserializar el contenido JSON a un objeto de datos de cita
2. THE Clinical_Service SHALL validar que el JSON deserializado contiene todos los campos requeridos (appointmentId, patientId, doctorId, date, time)
3. IF el JSON está malformado o incompleto, THEN THE Clinical_Service SHALL retornar un error descriptivo indicando el problema
4. THE Clinical_Service SHALL validar que las fechas y horas deserializadas son válidas y están en el formato correcto
5. FOR ALL códigos QR generados por el sistema, escanear y deserializar SHALL producir los datos originales de la cita (round-trip property)
