Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-12 — Consulta Médica
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al personal de triaje registrar los signos vitales del paciente y aplicar la clasificación Manchester para determinar su nivel de urgencia, derivándolo a consulta médica.
Definición Caso de Uso
Actores.
Personal con rol DOCTOR
Sistema.

Precondiciones.
La cita se encuentra en estado CONSULTA (triaje completado). El doctor tiene sesión activa
Flujo Normal Básico
El doctor accede al módulo "Citas Asignadas" desde el menú principal.
El sistema muestra la lista de citas en estado CONSULTA, ordenadas por prioridad Manchester (ROJO → NARANJA → AMARILLO → VERDE → AZUL) y luego por fecha y hora. Columnas: Fecha, Hora, Paciente, DPI, Estado, Prioridad, Triaje, Motivo, Acción. La lista se actualiza automáticamente cada 30 segundos.
El doctor localiza al paciente y hace clic en "Atender".
El sistema anuncia al paciente por voz ("[Nombre], por favor pasar a Clínica [código]") y navega al formulario de Consulta Médica.
El sistema muestra el formulario dividido en tres bloques:
Bloque 1 — Información del paciente: nombre, DPI, fecha y hora de la cita, motivo de la cita, clasificación Manchester con tiempo máximo de espera, y tarjetas de signos vitales con alertas visuales para valores fuera de rango.
Bloque 2 — Evaluación clínica: campos editables.
Bloque 3 — Destino: cuatro opciones de cierre y seguimiento opcional.
El doctor completa el Bloque 2 — Evaluación clínica:
Motivo de consulta *
Síntomas * (separados por coma)
Diagnóstico principal CIE-10 * (buscador por nombre o código)
Diagnósticos secundarios (opcional)
Notas de evolución (opcional)
Plan de tratamiento (opcional)
El doctor selecciona el destino del paciente en el Bloque 3. El sistema presenta cuatro opciones simultáneas:
Laboratorio — el diagnóstico CIE-10 pasa a ser opcional [FA01]
Farmacia interna — el sistema abre un diálogo de confirmación [FA02]
Receta externa — texto libre de medicamentos [FA03]
Alta sin receta no requiere documentos adicionales; continúa en 2.3.8
El doctor selecciona Alta sin receta
El doctor marca opcionalmente "Agendar cita de seguimiento" [FA04]
El doctor hace clic en "Cerrar consulta". (deshabilitado hasta que se seleccione destino). [FA05]
El sistema valida, registra la consulta, genera los documentos según el destino, crea las facturas internas y transiciona la cita al estado correspondiente.
El sistema muestra mensaje de éxito y redirige a la lista de citas.
El caso de uso termina con la consulta registrada y el paciente enrutado al siguiente módulo o finaliza el proceso.
Flujos Alternos.
FA01 Destino: Laboratorio
El doctor selecciona 🔬 Laboratorio.
El sistema muestra el catálogo de exámenes activos con precio. El campo diagnóstico CIE-10 se marca como opcional con la leyenda "Se completará con resultados de lab".
El doctor marca uno o más exámenes del catálogo.
Regresa al paso 2.3.8.
FA02 Destino: Farmacia interna
El doctor selecciona 💊 Farmacia interna.
El sistema abre un diálogo: "¿El paciente puede pagar en farmacia?"
Sí, pasa a farmacia → el sistema muestra filas de medicamentos del catálogo interno con cálculo automático de cantidad total (dosis × frecuencia × días). Regresa al paso 2.3.8.
No, generar receta externa → deriva a FA04.
Cancelar → cierra el diálogo sin cambiar el destino.
FA03 Destino: Receta externa (acceso directo o derivado desde FA03)
El sistema muestra filas de medicamentos en texto libre (nombre, dosis, frecuencia, días, vía).
El sistema informa que la receta se enviará al correo registrado del paciente.
El doctor agrega uno o más medicamentos.
Regresa al paso 2.3.8.
FA04 Agendar cita de seguimiento (combinable con cualquier destino)
El doctor marca "Agendar cita de seguimiento".
El sistema muestra calendario bloqueando fechas donde todos los doctores tienen día libre.
El doctor selecciona fecha. El sistema carga y muestra los horarios disponibles; si la fecha es hoy, filtra los slots con menos de 30 minutos de anticipación.
El doctor selecciona hora y agrega notas opcionales para la siguiente cita.
Al cerrar la consulta, el sistema crea la cita de seguimiento y genera su factura. Regresa al paso 2.3.9.
FA05 Cancelar consulta
El doctor hace clic en "Cancelar" en cualquier momento.
El sistema regresa a la lista de citas. La cita permanece en estado CONSULTA sin modificaciones.
Fin del flujo alterno.

Postcondiciones.
Consulta registrada: La consulta queda persistida en la base de datos con los datos clínicos del doctor (motivo, síntomas, diagnóstico CIE-10, notas, plan de tratamiento).
Documentos generados según destino:
Laboratorio → orden de laboratorio creada y factura de exámenes vinculada a la cita.
Farmacia interna → receta generada y factura de medicamentos vinculada a la cita.
Receta externa → receta generada (sin factura interna).
Alta sin receta → ningún documento adicional.
Cita de seguimiento (si FA05 se ejecutó): Nueva cita creada en estado PENDING_PAYMENT con su factura de consulta vinculada.

Requerimientos Suplementarios o no Funcionales
Orden de atención: Las citas se muestran ordenadas por prioridad Manchester descendente (ROJO primero), luego por fecha y hora ascendente, garantizando que los casos más urgentes se atiendan primero.
Actualización automática: La lista de citas se refresca cada 30 segundos sin intervención del doctor.
Síntesis de voz: El anuncio del paciente se realiza mediante el navegador en español (es-GT) a velocidad 0.9x, incluyendo el código de la clínica asignada al doctor.
Buscador CIE-10: El catálogo es local (sin llamada al backend). Requiere mínimo 2 caracteres para activar la búsqueda y muestra máximo 8 resultados filtrados por nombre de enfermedad o código CIE-10.
Filtrado de slots de seguimiento: Si la fecha de seguimiento seleccionada es el día actual, el sistema excluye los horarios con menos de 30 minutos de anticipación respecto a la hora actual.
Diagnóstico CIE-10 condicional: El campo diagnóstico principal es obligatorio para todos los destinos excepto Laboratorio, donde se marca como opcional con la leyenda "Se completará con resultados de lab".
Firmas Necesarias
En este punto se debe incluir la firma de aquellas personas que deban aprobar la presente especificación.


| Nombre | Fecha | Descripción del Cambio | Versión |
|---|---|---|---|
| Oscar Rivera | 19/02/2026 | Definición inicial. | 1.0 |
| Jose Avila | 20/02/2026 | Definición inicial. | 1.1 |
| Equipo HIS | 08/04/2026 | Modificación de flujo básico, flujos alternos y se hace referencia a RN. | 1.2 |
|  |  |  |  |
|  |  |  |  |


| Persona | Sector | Firma |
|---|---|---|
|  |  |  |
|  |  |  |
