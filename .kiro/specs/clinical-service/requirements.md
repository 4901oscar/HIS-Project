# Requirements Document: Clinical Service

## Introduction

El **Clinical Service** es el microservicio central del sistema MedFlow HIS, responsable de la gestión clínica completa del paciente. Implementa el algoritmo de Triaje Manchester para priorización de pacientes, gestiona citas médicas con cálculo en tiempo real de slots disponibles usando Redis, registra consultas médicas, genera prescripciones y órdenes de laboratorio, y mantiene el historial clínico del paciente.

**Arquitectura**: HEXAGONAL (Ports and Adapters) - Lógica de negocio pesada  
**Base de Datos**: clinical_schema (PostgreSQL)  
**Puerto**: 8083  
**Registro en Eureka**: CLINICAL-SERVICE  
**Cache**: Redis (para slots de citas en tiempo real)

## Glossary

- **Clinical_Service**: El microservicio responsable de la gestión clínica completa
- **Triaje_Engine**: Motor de cálculo del algoritmo de Triaje Manchester
- **Appointment_Manager**: Gestor de citas médicas con Redis
- **Consultation_Manager**: Gestor de consultas médicas
- **Prescription_Generator**: Generador de recetas médicas
- **Lab_Order_Generator**: Generador de órdenes de laboratorio
- **Vital_Signs_Recorder**: Registrador de signos vitales
- **Medical_History_Aggregator**: Agregador de historial clínico
- **Patient_Service_Client**: Cliente HTTP para obtener datos demográficos del paciente
- **Auth_Service_Client**: Cliente HTTP para validación de JWT
- **Pharmacy_Service_Client**: Cliente HTTP para notificar recetas
- **Lab_Service_Client**: Cliente HTTP para notificar órdenes de laboratorio
- **Redis_Cache**: Cache distribuido para slots de citas
- **Manchester_Catalog**: Catálogo de motivos y discriminadores del Triaje Manchester

## Requirements

### Requirement 1: Triaje Manchester

**User Story:** Como doctor, quiero realizar el triaje Manchester de un paciente, para que el sistema calcule automáticamente su nivel de prioridad y tiempo máximo de espera.

#### Acceptance Criteria

1. WHEN un doctor selecciona un paciente para triaje, THE Triaje_Engine SHALL recuperar los signos vitales más recientes del paciente
2. WHEN el doctor selecciona un motivo de consulta del Manchester_Catalog, THE Triaje_Engine SHALL mostrar los discriminadores asociados a ese motivo
3. WHEN el doctor selecciona discriminadores, THE Triaje_Engine SHALL calcular el nivel de prioridad según el algoritmo Manchester
4. THE Triaje_Engine SHALL asignar exactamente uno de cinco niveles de prioridad: Rojo (0 minutos), Naranja (10 minutos), Amarillo (60 minutos), Verde (120 minutos), o Azul (240 minutos)
5. WHEN el triaje se completa, THE Clinical_Service SHALL guardar el resultado en clinical_schema con timestamp y usuario que lo realizó
6. WHEN el triaje se completa, THE Clinical_Service SHALL agregar el paciente a la cola de atención correspondiente a su nivel de prioridad
7. IF el paciente no tiene signos vitales capturados, THEN THE Triaje_Engine SHALL retornar error indicando que se requieren signos vitales
8. FOR ALL triajes válidos, el nivel de prioridad calculado SHALL ser determinístico (mismos discriminadores = mismo nivel)

### Requirement 2: Captura de Signos Vitales

**User Story:** Como personal de enfermería, quiero capturar los signos vitales de un paciente, para que estén disponibles durante el triaje y la consulta médica.

#### Acceptance Criteria

1. WHEN personal con rol VITAL_SIGNS captura signos vitales, THE Vital_Signs_Recorder SHALL validar que todos los campos obligatorios estén presentes
2. THE Vital_Signs_Recorder SHALL almacenar: presión arterial sistólica, presión arterial diastólica, frecuencia cardíaca, frecuencia respiratoria, temperatura, saturación de oxígeno, peso y talla
3. WHEN se capturan signos vitales, THE Vital_Signs_Recorder SHALL calcular automáticamente el IMC (Índice de Masa Corporal) usando peso y talla
4. THE Vital_Signs_Recorder SHALL validar que los valores estén dentro de rangos fisiológicos válidos (presión sistólica 50-250 mmHg, diastólica 30-150 mmHg, frecuencia cardíaca 20-250 bpm, temperatura 30-45°C, saturación 0-100%)
5. WHEN se guardan signos vitales, THE Clinical_Service SHALL registrar timestamp y usuario que los capturó
6. WHEN se consultan signos vitales de un paciente, THE Clinical_Service SHALL retornar los más recientes primero
7. IF se intenta capturar signos vitales con valores fuera de rango, THEN THE Vital_Signs_Recorder SHALL retornar error con el campo específico y rango válido

### Requirement 3: Gestión de Citas Médicas

**User Story:** Como personal de admisión, quiero gestionar citas médicas con cálculo en tiempo real de slots disponibles, para que los pacientes puedan agendar consultas con doctores específicos.

#### Acceptance Criteria

1. WHEN se buscan slots disponibles para un doctor y fecha, THE Appointment_Manager SHALL consultar Redis_Cache para obtener disponibilidad en tiempo real
2. THE Appointment_Manager SHALL calcular slots de 30 minutos desde las 08:00 hasta las 17:00 horas
3. WHEN se crea una cita, THE Appointment_Manager SHALL validar que el slot esté disponible y actualizar Redis_Cache atómicamente
4. WHEN se crea una cita, THE Appointment_Manager SHALL validar que el paciente existe llamando a Patient_Service_Client
5. WHEN se crea una cita, THE Appointment_Manager SHALL validar que el doctor existe y tiene rol DOCTOR
6. THE Appointment_Manager SHALL almacenar citas con estado: SCHEDULED, ACTIVE, COMPLETED, CANCELLED
7. WHEN se activa una cita, THE Appointment_Manager SHALL cambiar el estado de SCHEDULED a ACTIVE
8. WHEN se cancela una cita, THE Appointment_Manager SHALL liberar el slot en Redis_Cache y cambiar estado a CANCELLED
9. IF se intenta crear una cita en un slot ocupado, THEN THE Appointment_Manager SHALL retornar error 409 Conflict
10. FOR ALL operaciones de creación y cancelación de citas, la actualización de Redis_Cache y PostgreSQL SHALL ser consistente (ambas exitosas o ambas fallan)

### Requirement 4: Consulta Médica

**User Story:** Como doctor, quiero registrar una consulta médica completa con diagnósticos y notas, para que quede documentada en el historial clínico del paciente.

#### Acceptance Criteria

1. WHEN un doctor inicia una consulta, THE Consultation_Manager SHALL validar que el doctor tiene rol DOCTOR
2. WHEN un doctor inicia una consulta, THE Consultation_Manager SHALL recuperar el historial clínico del paciente llamando a Medical_History_Aggregator
3. THE Consultation_Manager SHALL permitir registrar: motivo de consulta, síntomas, diagnóstico principal, diagnósticos secundarios, notas médicas, plan de tratamiento
4. WHEN se guarda una consulta, THE Clinical_Service SHALL registrar timestamp, doctor que la realizó, y paciente atendido
5. WHEN se completa una consulta, THE Consultation_Manager SHALL cambiar el estado de la cita asociada a COMPLETED
6. THE Consultation_Manager SHALL permitir agregar múltiples diagnósticos usando códigos CIE-10
7. IF el paciente no existe, THEN THE Consultation_Manager SHALL retornar error 404 Not Found
8. IF el doctor no tiene rol DOCTOR, THEN THE Consultation_Manager SHALL retornar error 403 Forbidden

### Requirement 5: Generación de Prescripciones

**User Story:** Como doctor, quiero generar recetas médicas durante la consulta, para que el paciente pueda obtener sus medicamentos en la farmacia del hospital.

#### Acceptance Criteria

1. WHEN un doctor genera una prescripción, THE Prescription_Generator SHALL validar que existe una consulta médica activa
2. THE Prescription_Generator SHALL permitir agregar múltiples medicamentos con: nombre, dosis, frecuencia, duración, vía de administración, indicaciones especiales
3. WHEN se guarda una prescripción, THE Clinical_Service SHALL generar un código único alfanumérico de 8 caracteres
4. WHEN se guarda una prescripción, THE Prescription_Generator SHALL notificar a Pharmacy_Service_Client que hay una nueva receta disponible
5. THE Prescription_Generator SHALL almacenar prescripciones con estado: PENDING, DISPENSED, CANCELLED
6. WHEN se consulta una prescripción, THE Clinical_Service SHALL retornar todos los medicamentos asociados
7. IF la notificación a Pharmacy_Service_Client falla, THEN THE Prescription_Generator SHALL registrar el error pero NO fallar la transacción (eventual consistency)
8. FOR ALL prescripciones válidas, el código generado SHALL ser único en el sistema

### Requirement 6: Generación de Órdenes de Laboratorio

**User Story:** Como doctor, quiero generar órdenes de laboratorio durante la consulta, para que el paciente pueda realizarse los exámenes solicitados.

#### Acceptance Criteria

1. WHEN un doctor genera una orden de laboratorio, THE Lab_Order_Generator SHALL validar que existe una consulta médica activa
2. THE Lab_Order_Generator SHALL permitir agregar múltiples exámenes de laboratorio del catálogo disponible
3. WHEN se guarda una orden de laboratorio, THE Clinical_Service SHALL generar un código único alfanumérico de 8 caracteres
4. WHEN se guarda una orden de laboratorio, THE Lab_Order_Generator SHALL notificar a Lab_Service_Client que hay una nueva orden disponible
5. THE Lab_Order_Generator SHALL almacenar órdenes con estado: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
6. WHEN se consulta una orden de laboratorio, THE Clinical_Service SHALL retornar todos los exámenes solicitados
7. IF la notificación a Lab_Service_Client falla, THEN THE Lab_Order_Generator SHALL registrar el error pero NO fallar la transacción (eventual consistency)
8. FOR ALL órdenes válidas, el código generado SHALL ser único en el sistema

### Requirement 7: Historial Clínico

**User Story:** Como doctor o paciente, quiero consultar el historial clínico completo de un paciente, para revisar consultas anteriores, diagnósticos, recetas y resultados de laboratorio.

#### Acceptance Criteria

1. WHEN se solicita el historial clínico de un paciente, THE Medical_History_Aggregator SHALL recuperar datos demográficos llamando a Patient_Service_Client
2. WHEN se solicita el historial clínico, THE Medical_History_Aggregator SHALL recuperar todas las consultas médicas del paciente ordenadas por fecha descendente
3. WHEN se solicita el historial clínico, THE Medical_History_Aggregator SHALL recuperar todos los signos vitales históricos del paciente
4. WHEN se solicita el historial clínico, THE Medical_History_Aggregator SHALL recuperar todas las prescripciones del paciente
5. WHEN se solicita el historial clínico, THE Medical_History_Aggregator SHALL recuperar todas las órdenes de laboratorio del paciente
6. WHILE un usuario tiene rol PATIENT, THE Medical_History_Aggregator SHALL retornar solo el historial del paciente asociado a ese usuario
7. WHILE un usuario tiene rol DOCTOR, THE Medical_History_Aggregator SHALL retornar el historial de cualquier paciente
8. IF el paciente no existe en Patient_Service_Client, THEN THE Medical_History_Aggregator SHALL retornar error 404 Not Found

### Requirement 8: Catálogo de Motivos y Discriminadores Manchester

**User Story:** Como administrador, quiero gestionar el catálogo de motivos de consulta y discriminadores del Triaje Manchester, para que los doctores puedan realizar triajes precisos.

#### Acceptance Criteria

1. THE Manchester_Catalog SHALL almacenar motivos de consulta con: código, descripción, categoría
2. THE Manchester_Catalog SHALL almacenar discriminadores con: código, descripción, nivel de prioridad asociado
3. THE Manchester_Catalog SHALL permitir asociar múltiples discriminadores a un motivo de consulta
4. WHEN se consulta un motivo, THE Manchester_Catalog SHALL retornar todos los discriminadores asociados
5. WHEN un administrador agrega un motivo, THE Clinical_Service SHALL validar que el código sea único
6. WHEN un administrador agrega un discriminador, THE Clinical_Service SHALL validar que el nivel de prioridad sea uno de los cinco válidos
7. THE Manchester_Catalog SHALL permitir desactivar motivos y discriminadores sin eliminarlos físicamente
8. WHEN se consultan motivos o discriminadores, THE Clinical_Service SHALL retornar solo los activos por defecto

### Requirement 9: Validación de Permisos por Rol

**User Story:** Como sistema, quiero validar que cada operación sea realizada por un usuario con el rol apropiado, para garantizar la seguridad y trazabilidad del sistema.

#### Acceptance Criteria

1. WHEN se recibe una petición, THE Clinical_Service SHALL extraer el rol del usuario desde el header X-User-Roles
2. WHEN se intenta realizar triaje, THE Clinical_Service SHALL validar que el usuario tiene rol DOCTOR
3. WHEN se intenta capturar signos vitales, THE Clinical_Service SHALL validar que el usuario tiene rol VITAL_SIGNS o DOCTOR
4. WHEN se intenta gestionar citas, THE Clinical_Service SHALL validar que el usuario tiene rol ADMISSION o ADMIN
5. WHEN se intenta registrar consulta médica, THE Clinical_Service SHALL validar que el usuario tiene rol DOCTOR
6. WHEN se intenta generar prescripción, THE Clinical_Service SHALL validar que el usuario tiene rol DOCTOR
7. WHEN se intenta generar orden de laboratorio, THE Clinical_Service SHALL validar que el usuario tiene rol DOCTOR
8. IF el usuario no tiene el rol requerido, THEN THE Clinical_Service SHALL retornar error 403 Forbidden con mensaje en español

### Requirement 10: Integración con Patient Service

**User Story:** Como Clinical Service, quiero obtener datos demográficos del paciente desde Patient Service, para cumplir con la regla de CERO JOINs entre esquemas.

#### Acceptance Criteria

1. WHEN se necesitan datos demográficos de un paciente, THE Patient_Service_Client SHALL realizar una llamada HTTP GET a Patient Service
2. THE Patient_Service_Client SHALL incluir el JWT en el header Authorization de la petición
3. WHEN Patient Service retorna datos del paciente, THE Patient_Service_Client SHALL mapear la respuesta a un DTO interno
4. IF Patient Service retorna 404 Not Found, THEN THE Patient_Service_Client SHALL propagar el error al caller
5. IF Patient Service no responde en 5 segundos, THEN THE Patient_Service_Client SHALL retornar timeout error
6. THE Patient_Service_Client SHALL implementar circuit breaker para evitar cascading failures
7. WHEN el circuit breaker está abierto, THE Patient_Service_Client SHALL retornar error indicando que Patient Service no está disponible
8. FOR ALL llamadas a Patient Service, NO SHALL existir JOINs directos con patient_schema en las queries SQL

### Requirement 11: Parseo y Serialización de Datos Clínicos

**User Story:** Como Clinical Service, quiero parsear y serializar datos clínicos en formato JSON, para intercambiar información con otros servicios y el frontend.

#### Acceptance Criteria

1. WHEN se recibe una petición con datos clínicos en JSON, THE Clinical_Service SHALL parsear el JSON a objetos del dominio
2. WHEN se retorna una respuesta, THE Clinical_Service SHALL serializar objetos del dominio a JSON
3. THE Clinical_Service SHALL validar que el JSON recibido cumple con el schema esperado
4. IF el JSON recibido es inválido, THEN THE Clinical_Service SHALL retornar error 400 Bad Request con descripción del campo inválido
5. THE Clinical_Service SHALL incluir un pretty printer para formatear JSON de respuesta de forma legible
6. FOR ALL objetos del dominio válidos, parsear y luego serializar y luego parsear SHALL producir un objeto equivalente al original (round-trip property)

### Requirement 12: Manejo de Errores y Mensajes en Español

**User Story:** Como usuario del sistema, quiero recibir mensajes de error claros en español, para entender qué salió mal y cómo corregirlo.

#### Acceptance Criteria

1. WHEN ocurre un error de validación, THE Clinical_Service SHALL retornar error 400 Bad Request con mensaje en español
2. WHEN un recurso no existe, THE Clinical_Service SHALL retornar error 404 Not Found con mensaje en español
3. WHEN un usuario no tiene permisos, THE Clinical_Service SHALL retornar error 403 Forbidden con mensaje en español
4. WHEN ocurre un conflicto (ej: slot ocupado), THE Clinical_Service SHALL retornar error 409 Conflict con mensaje en español
5. WHEN ocurre un error interno, THE Clinical_Service SHALL retornar error 500 Internal Server Error con mensaje genérico en español
6. THE Clinical_Service SHALL registrar todos los errores en logs con nivel ERROR incluyendo stack trace
7. THE Clinical_Service SHALL NO exponer detalles internos (stack traces, queries SQL) en las respuestas de error
8. FOR ALL respuestas de error, el campo "message" SHALL estar en español

## Special Requirements Guidance

### Parser and Serializer Requirements

El Clinical Service maneja datos clínicos complejos que deben ser parseados y serializados correctamente. Es ESENCIAL implementar:

1. **Parser de JSON a Objetos del Dominio**: Para convertir peticiones HTTP en entidades del dominio
2. **Serializer de Objetos del Dominio a JSON**: Para convertir respuestas del dominio en JSON
3. **Pretty Printer**: Para formatear JSON de forma legible en logs y respuestas
4. **Round-Trip Property**: Garantizar que `parse(serialize(object)) == object`

**Ejemplo de Round-Trip Test**:
```java
// Given: Un objeto de consulta médica válido
Consultation original = new Consultation(/* ... */);

// When: Se serializa a JSON y luego se parsea de vuelta
String json = consultationSerializer.toJson(original);
Consultation parsed = consultationParser.fromJson(json);

// Then: El objeto parseado debe ser equivalente al original
assertEquals(original, parsed);
```

Este tipo de testing es crítico para garantizar que no se pierda información durante la serialización/deserialización.

---

**Created**: April 14, 2026  
**Author**: MedFlow Team  
**Status**: ✅ Ready for Review  
**Version**: 1.0.0
