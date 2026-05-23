Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-00 Visualización del Portal Web y Agendamiento en Línea
Versión 1.4
Elaborado por Equipo HIS
08/05/2026
Historial Revisiones

Introducción.
Objetivo.
Ofrecer al paciente un canal digital sencillo para agendar y pagar su cita desde cualquier dispositivo, reduciendo el tiempo de espera en ventanilla el día de su visita.
Definición Caso de Uso
Actores.
Usuario externo / Paciente (accede desde su computadora o teléfono).
Sistema (portal web y plataforma de pago en línea).

Precondiciones.
El paciente debe contar con acceso a internet.
Deben existir citas disponibles en el sistema.

Flujo Normal Básico
El usuario externo ingresa a la URL del portal web de MedFlow.
El sistema muestra la página de inicio con el encabezado de navegación con las opciones: Inicio, Nosotros, Servicios, Iniciar Sesión y Registrarse; y la sección principal con el titular "Marcando el camino en Excelencia Médica" y los botones "Agendar Cita". [RN01][FA1 al FA3]
El usuario externo selecciona "Agendar Cita".
El sistema muestra la página de agendamiento con el calendario de fechas disponibles.
El usuario externo selecciona la fecha de su preferencia. [FA4]
El sistema muestra los horarios disponibles para la fecha seleccionada. [RN23]
El usuario externo selecciona el horario de su preferencia.
El sistema aparta el horario seleccionado temporalmente por 10 minutos y muestra un contador de tiempo al usuario. [RN Redis]
El usuario externo ingresa el motivo de la consulta.
El usuario externo selecciona "AGENDAR CITA".
El sistema verifica si el usuario externo tiene sesión activa. [FA05]
El sistema redirige al usuario a la pasarela de pago mostrando el resumen de la cita (fecha y hora) y el monto a pagar.
El usuario externo ingresa los datos de su tarjeta (número, titular, fecha de vencimiento y CVV) y selecciona "Pagar". [FA06]
El sistema procesa el pago, registra la cita, genera la factura interna y un código QR único asociado a la cita.
El sistema libera el apartado temporal del horario.
El sistema muestra la pantalla de confirmación con: resumen de la cita, número de factura, monto pagado, código QR con su ventana de validez y botón para descargarlo.
Fin del flujo normal básico.

Flujos Alternos.
FA01 — Usuario navega a "Nosotros"
El usuario selecciona "Nosotros" en la barra de navegación.
El sistema muestra la página con la siguiente información:
Encabezado: "Sobre MedFlow — Transformando la atención hospitalaria con tecnología moderna y accesible para todos."
¿Quiénes Somos? — descripción de MedFlow como sistema integral de gestión hospitalaria, con énfasis en accesibilidad, seguridad y eficiencia.
Nuestra Propuesta: Misión, Visión y Valores.
El usuario puede continuar navegando, usando la barra de navegación.
Fin del flujo Alterno.
FA02 — Usuario navega a "Servicios"
El usuario selecciona "Servicios" en la barra de navegación.
El sistema muestra la página con los servicios disponibles:
Consulta Médica General
Laboratorio Clínico
Farmacia
Triaje y Urgencias
Facturación y Cobros
El sistema muestra además la sección "Comprometidos con tu Bienestar" con: Atención 24/7, Tecnología Avanzada y Precios Accesibles.
El usuario puede continuar navegando, usando la barra de navegación.
Fin del flujo alterno.

FA03 — Usuario navega a "Iniciar Sesión" o "Registrarse"
El usuario selecciona "Iniciar Sesión" o "Registrarse" en la barra de navegación.
El sistema redirige al usuario al flujo correspondiente (ver CU-00.1, ver CU-00.2).
Fin del flujo alterno.


FA04 — No hay horarios disponibles
El sistema consulta los horarios disponibles para la fecha seleccionada.
No existen horarios disponibles (todos ocupados o el médico tiene día libre).
El sistema muestra el mensaje: "No hay horarios para esta fecha."
El usuario selecciona una fecha diferente.
El flujo retoma en el paso 2.3.5 del Flujo Normal.

FA05 — Usuario no autenticado al confirmar agendamiento
El sistema detecta que el usuario externo no tiene sesión activa.
El sistema guarda la fecha, horario y motivo.
El sistema redirige al usuario a la pantalla de inicio de sesión.
El usuario externo inicia sesión o se registra (ver CU-00.1), (ver CU-00.2).
El sistema regresa al usuario a la página de agendamiento con la fecha, horario y motivo previamente guardados restaurados y el apartado del horario reactivado.
El flujo retoma en el paso 2.3.10 del Flujo Normal.

FA06 — Error al procesar el pago
En el paso 2.3.13, el sistema detecta que el pago no pudo procesarse.
El sistema muestra el mensaje: "No se pudo procesar el pago. Verifica los datos de tu tarjeta e intenta de nuevo."
El formulario de pago permanece visible con los datos ingresados.
El usuario externo puede corregir los datos de su tarjeta e intentar nuevamente desde el paso 2.3.13, o cerrar la página para cancelar el proceso.

Postcondiciones.
El paciente recibe la confirmación de su cita y el código QR en su correo electrónico.
El estado de la cita queda registrado en el sistema como pagada y pendiente de atención.

Requerimientos Suplementarios o no Funcionales
El sistema debe garantizar la disponibilidad del portal web en todo momento para que los pacientes puedan agendar sus citas sin interrupciones.
El proceso de pago en línea debe cumplir con los estándares de seguridad correspondientes para proteger los datos de la tarjeta del paciente.

Firmas Necesarias
En este punto se debe incluir la firma de aquellas personas que deban aprobar la presente especificación.


| Nombre | Fecha | Descripción del Cambio | Versión |
|---|---|---|---|
| Oscar Rivera | 19/02/2026 | Definición inicial. | 1.0 |
| José Avila | 20/02/2026 | Definición inicial. | 1.1 |
| Equipo HIS | 08/04/2026 | Modificación de flujo básico, flujos alternos y se hace referencia a RN. | 1.2 |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |


| Persona | Sector | Firma |
|---|---|---|
|  |  |  |
|  |  |  |
