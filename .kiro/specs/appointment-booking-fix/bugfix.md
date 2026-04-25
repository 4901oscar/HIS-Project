# Bugfix Requirements Document

## Introduction

El formulario de "Agendar Cita" en la aplicación MedFlow no funciona correctamente al intentar crear una nueva cita médica. El problema impide que los pacientes puedan agendar citas a través de la interfaz web, afectando la funcionalidad principal del sistema de gestión de citas.

Los problemas identificados incluyen:
- Envío de `doctorId` vacío que falla la validación `@NotBlank` del backend
- Formato de fecha incorrecto (ISO completo con hora vs `LocalDate` esperado)
- Redundancia en el envío de fecha/hora (`appointmentDate` con timestamp + `appointmentTime` separado)

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN el usuario completa el formulario de agendar cita y hace clic en "AGENDAR CITA" THEN el sistema envía `doctorId: ''` (string vacío) al backend y la solicitud falla con error de validación "Doctor ID es requerido"

1.2 WHEN el formulario envía la fecha de la cita THEN el sistema envía `appointmentDate` como string ISO completo (ej: `"2024-01-01T10:00:00"`) pero el backend espera un `LocalDate` (solo fecha sin hora), causando error de parsing o validación

1.3 WHEN el formulario envía los datos de fecha y hora THEN el sistema envía tanto `appointmentDate` (con timestamp completo) como `appointmentTime` (hora separada), creando redundancia y confusión en el procesamiento de datos

### Expected Behavior (Correct)

2.1 WHEN el usuario completa el formulario de agendar cita y hace clic en "AGENDAR CITA" THEN el sistema SHALL enviar un `doctorId` válido (no vacío) que pase la validación `@NotBlank` del backend

2.2 WHEN el formulario envía la fecha de la cita THEN el sistema SHALL enviar `appointmentDate` como string en formato de solo fecha (ej: `"2024-01-01"`) compatible con `LocalDate` del backend

2.3 WHEN el formulario envía los datos de fecha y hora THEN el sistema SHALL enviar `appointmentDate` (solo fecha) y `appointmentTime` (solo hora en formato `HH:mm`) como campos separados y correctamente formateados

### Unchanged Behavior (Regression Prevention)

3.1 WHEN el usuario selecciona una fecha válida (futura) THEN el sistema SHALL CONTINUE TO validar que la fecha sea futura según la restricción `@Future` del backend

3.2 WHEN el usuario ingresa notas/motivo de consulta THEN el sistema SHALL CONTINUE TO enviar el campo `notes` opcional al backend sin modificaciones

3.3 WHEN el usuario completa el formulario con datos válidos THEN el sistema SHALL CONTINUE TO limpiar los campos del formulario después de una creación exitosa

3.4 WHEN el backend responde con éxito THEN el sistema SHALL CONTINUE TO mostrar el mensaje de confirmación "¡Cita agendada exitosamente! Pronto recibirás confirmación."

3.5 WHEN el backend responde con error THEN el sistema SHALL CONTINUE TO mostrar el mensaje de error "No se pudo agendar la cita. Intenta de nuevo más tarde."

3.6 WHEN el usuario no ha seleccionado fecha, hora o motivo THEN el sistema SHALL CONTINUE TO mostrar mensajes de validación del lado del cliente antes de enviar la solicitud
