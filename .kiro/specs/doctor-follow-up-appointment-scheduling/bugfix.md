# Bugfix Requirements Document

## Introduction

El formulario de consulta del doctor incluye una sección para "Agendar cita de seguimiento" que actualmente utiliza un flujo básico con campos simples (fecha y notas). Este enfoque no valida la disponibilidad real de los doctores, lo que puede resultar en conflictos de horarios, doble reserva de slots, y una experiencia deficiente tanto para el personal médico como para los pacientes. Este bug afecta la integridad del sistema de agendamiento y puede causar problemas operacionales significativos.

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN un doctor intenta agendar una cita de seguimiento desde el formulario de consulta THEN el sistema solo muestra campos simples de fecha y notas sin validación de disponibilidad

1.2 WHEN un doctor selecciona una fecha para la cita de seguimiento THEN el sistema no verifica si existen slots de tiempo disponibles en el turno del doctor

1.3 WHEN un doctor agenda una cita de seguimiento en un horario ya ocupado THEN el sistema permite la creación causando conflictos de horarios

1.4 WHEN un doctor intenta agendar una cita de seguimiento en una fecha pasada THEN el sistema no bloquea la selección de fechas pasadas

1.5 WHEN un doctor intenta agendar una cita de seguimiento en un día libre del doctor THEN el sistema no valida ni bloquea días sin turnos asignados

1.6 WHEN un doctor agenda una cita de seguimiento para el día actual THEN el sistema no filtra los slots de tiempo que ya han pasado

1.7 WHEN un doctor agenda una cita de seguimiento THEN el sistema permite seleccionar manualmente el paciente en lugar de usar automáticamente el paciente de la consulta actual

### Expected Behavior (Correct)

2.1 WHEN un doctor accede a la sección "Agendar cita de seguimiento" en el formulario de consulta THEN el sistema SHALL mostrar un calendario visual interactivo para seleccionar la fecha

2.2 WHEN un doctor selecciona una fecha en el calendario THEN el sistema SHALL mostrar los slots de tiempo disponibles de 30 minutos basados en los turnos del doctor

2.3 WHEN un doctor intenta agendar una cita de seguimiento THEN el sistema SHALL validar la disponibilidad del slot en tiempo real antes de permitir la creación

2.4 WHEN un doctor intenta seleccionar una fecha pasada en el calendario THEN el sistema SHALL bloquear y deshabilitar visualmente las fechas pasadas

2.5 WHEN un doctor visualiza el calendario THEN el sistema SHALL bloquear y deshabilitar los días en los que el doctor no tiene turnos asignados

2.6 WHEN un doctor selecciona la fecha actual en el calendario THEN el sistema SHALL filtrar y ocultar los slots de tiempo que ya han pasado

2.7 WHEN un doctor agenda una cita de seguimiento desde el formulario de consulta THEN el sistema SHALL asignar automáticamente el paciente de la consulta actual sin permitir selección manual

### Unchanged Behavior (Regression Prevention)

3.1 WHEN un doctor agenda una cita de seguimiento con disponibilidad válida THEN el sistema SHALL CONTINUE TO crear la cita correctamente en la base de datos

3.2 WHEN un doctor completa el agendamiento de una cita de seguimiento THEN el sistema SHALL CONTINUE TO permitir agregar notas adicionales sobre la cita

3.3 WHEN un doctor agenda una cita de seguimiento THEN el sistema SHALL CONTINUE TO asociar la cita con el doctor que la creó

3.4 WHEN un doctor guarda el formulario de consulta con una cita de seguimiento agendada THEN el sistema SHALL CONTINUE TO persistir toda la información de la consulta y la cita

3.5 WHEN un doctor cancela o cierra el formulario de consulta sin guardar THEN el sistema SHALL CONTINUE TO descartar los cambios sin crear la cita de seguimiento

3.6 WHEN un doctor visualiza el historial de citas del paciente THEN el sistema SHALL CONTINUE TO mostrar las citas de seguimiento agendadas previamente
