Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-10 — Gestión de Admisión
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Objetivo: Permitir al personal de admisión registrar citas presenciales (con registro de paciente nuevo si aplica), gestionar la cola de citas pendientes de activación y validar citas mediante código QR.
Definición Caso de Uso
Actores.
Admisionista (rol ADMISSION)
Sistema.

Precondiciones.
Sesión iniciada con rol ADMISSION. El usuario se encuentra en el módulo de Admisión (/admission).
Flujo Normal Básico
El admisionista accede al módulo de Admisión desde el menú principal.
El sistema muestra la página con tres pestañas: Agendar Cita Presencial (activa por defecto), Ver Citas y Escanear QR.
El admisionista ingresa el DPI del paciente y sale del campo.
El sistema no encuentra el DPI en el registro de pacientes, muestra el mensaje "DPI no registrado" y habilita todos los campos del formulario para el registro del nuevo paciente. [FA01]
El admisionista completa los datos del paciente:
DPI * — 13 dígitos
Primer nombre * y segundo nombre
Primer apellido * y segundo apellido
Fecha de nacimiento *
Género * — select: Masculino / Femenino / Otro
Correo electrónico *
Teléfono * — 8 dígitos
NIT, departamento, municipio, zona y dirección — opcionales
El admisionista selecciona la fecha de la cita en el calendario. El sistema bloquea fechas pasadas y días en que todos los doctores tienen días libres registrados.
El sistema carga los slots de horario disponibles para la fecha seleccionada.
El admisionista selecciona un horario — el sistema reserva ese slot por 10 minutos para la sesión actual.
El admisionista ingresa el motivo de consulta * y hace clic en "Agendar cita".
El sistema crea la cuenta del paciente, registra la cita, genera la factura vinculada y envía correo de confirmación. Muestra: "Cita agendada exitosamente. El paciente debe pasar a caja para realizar el pago."
El paciente realiza el pago en caja (proceso externo al caso de uso).
El admisionista va a la pestaña "Escanear QR", inicia la cámara y escanea el código QR que presenta el paciente. [FA02]
El sistema valida la cita y la activa automáticamente mostrando el resultado en verde.
El caso de uso termina cuando el paciente ha sido activado y derivado al área de signos vitales.
Flujos Alternos.
FA01 — El paciente ya está registrado
En el paso 2.3.4, el sistema encuentra el DPI en el registro de pacientes y autocompleta todos los campos de datos personales, los cuales quedan deshabilitados para edición.
El admisionista continúa en el paso 2.3.6.

FA02 — El paciente no presenta QR (activación manual)
En el paso 2.3.12, el paciente no dispone del código QR.
El admisionista cambia a la pestaña "Ver Citas". El sistema muestra la cola de admisión con columnas: Fecha, Hora, Paciente, DPI, Doctor, Estado, Pago y Acciones.
Las citas con pago confirmado muestran el botón "Activar" habilitado. Las citas con pago pendiente lo muestran deshabilitado con el mensaje "El Paciente debe pagar en caja primero."
El admisionista localiza la cita y hace clic en "Activar".
El sistema activa la cita, transiciona su estado a VITAL_SIGNS y actualiza la lista.
El flujo regresa al paso 2.3.14.
Postcondiciones.
El paciente recibe un correo de confirmación con los detalles de la cita.
Si el paciente era nuevo, su cuenta queda creada en el sistema con rol PATIENT.
Tras la activación (por QR o manual), la cita transiciona a estado VITAL_SIGNS y queda disponible en la cola del área de signos vitales.

Requerimientos Suplementarios o no Funcionales
Solo usuarios autenticados con rol (ADMISSION y ADMIN) pueden acceder al módulo de admisión.
El slot de horario seleccionado queda reservado por 10 minutos para la sesión activa. Si el formulario no se completa en ese tiempo, el slot es liberado automáticamente.
El calendario bloquea en tiempo real los días en que todos los doctores tienen días libres registrados, impidiendo agendar citas en esas fechas.
La activación por QR requiere acceso a la cámara del dispositivo. Si el acceso es denegado, el sistema notifica al admisionista para usar la activación manual.

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
