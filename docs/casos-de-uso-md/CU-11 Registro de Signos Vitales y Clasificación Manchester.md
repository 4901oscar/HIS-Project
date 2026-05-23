Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-11 — Registro de Signos Vitales y Clasificación Manchester
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al personal de triaje registrar los signos vitales del paciente y aplicar la clasificación Manchester para determinar su nivel de urgencia, derivándolo a consulta médica.
Definición Caso de Uso
Actores.
Personal de Triaje (rol Signos Vitales)
Sistema.

Precondiciones.
Sesión iniciada con rol Signos Vitales y que exista al menos una cita con estado Signos Vitales en la cola de triaje.
Flujo Normal Básico
El personal de triaje accede al módulo Triaje — Signos Vitales desde el menú principal.
El sistema muestra la lista "Pacientes en Espera (N)" con las citas en estado Signos Vitales, ordenadas por fecha y hora, con columnas: Fecha, Hora, Paciente, DPI, Estado, Progreso, Motivo y Acción. La lista se actualiza automáticamente cada 30 segundos.
El personal localiza al paciente y hace clic en "Atender".
El sistema anuncia el nombre del paciente por voz ("[Nombre], por favor pasar a sala de triaje") y navega a la página "Registrar Signos Vitales".
El sistema muestra la página "Registrar Signos Vitales" con: Subtítulo: Triaje — [Nombre del paciente], Tarjeta de Información del Paciente: Nombre completo, DPI y correo electrónico Botón "Llamar Paciente" para volver a anunciar al paciente por voz en caso de que no haya respondido
El sistema muestra el formulario de Paso 1 — Signos Vitales con los campos:
Presión sistólica * — 50 a 250 mmHg
Presión diastólica * — 30 a 150 mmHg
Frecuencia cardíaca * — 20 a 300 lpm
Frecuencia respiratoria * — 5 a 60 rpm
Temperatura * — 30 a 45 °C
Saturación de oxígeno * — 50 a 100 %
Peso y Altura — opcionales
El personal completa los signos vitales y hace clic en "Guardar Signos Vitales".[FA01]
El sistema valida los rangos clínicos [RN-01], registra los signos vitales y bloquea los campos.
El sistema muestra el mensaje “✓ Signos vitales guardados exitosamente. Ahora puede completar la clasificación Manchester” en color verde y activa el botón Editar Signos Vitales. [FA02]
El sistema muestra la sección de Paso 2 — Clasificación Manchester.
El personal selecciona el motivo de consulta del catálogo activo.
El sistema filtra y muestra únicamente los discriminadores asociados a ese motivo.
El personal marca uno o más discriminadores aplicables al paciente. El sistema calcula en tiempo real el nivel de prioridad más urgente entre los seleccionados y muestra la vista previa del resultado (color + descripción).
El personal hace clic en "Guardar Clasificación Manchester".[FA01]
El sistema registra el triaje, transiciona la cita al estado CONSULTA y redirige automáticamente a la lista de espera.
El caso de uso termina con el paciente clasificado y disponible en la cola del médico.
Flujos Alternos.
FA01 — Cancelar registro
El personal selecciona "Cancelar".
El sistema descarta los datos y/o cambios ingresados.
Fin del flujo alterno.

FA02: Personal edita signos vitales ya guardados
Personal hace clic en "Editar signos vitales".
Sistema desbloquea campos y muestra botones "Cancelar" y "Guardar Signos Vitales". La sección Manchester se oculta.
Personal modifica los valores y hace clic en "Guardar Signos Vitales".
Sistema valida, actualiza el registro y vuelve a bloquear los campos.
Regresa al paso 2.3.9.

Postcondiciones.
Triaje completo: Los signos vitales quedan registrados en la base de datos asociados a la cita y al paciente. La cita transiciona al estado CONSULTA y el paciente queda disponible en la cola del médico.
Triaje parcial: Si el personal guardó signos vitales pero no completó la clasificación Manchester (salió con Cancelar o cerró sesión), la cita permanece en estado SIGNOS VITALES con los signos vitales persistidos. El personal puede retomar el registro en cualquier momento.
Requerimientos Suplementarios o no Funcionales
Actualización automática: La lista de espera se refresca cada 30 segundos sin intervención del usuario.
Síntesis de voz: El anuncio del paciente se realiza en español (es-GT), a una velocidad de 0.9x.
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
