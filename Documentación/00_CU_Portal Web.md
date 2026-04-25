Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-00 Visualización del Portal Web y Agendamiento en Línea
Versión 1.3
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones
Nombre	Fecha	Descripción del Cambio	Versión
Oscar Rivera	19/02/2026	Definición inicial.	1.0
José Avila	20/02/2026	Definición inicial.	1.1
Equipo HIS	08/04/2026	Modificación de flujo básico, flujos alternos y se hace referencia a RN.	1.2
			
			
			
			
			
			

1.	Introducción.
1.1.	Objetivo.
Ofrecer al paciente un canal digital sencillo para agendar y pagar su cita desde cualquier dispositivo, reduciendo el tiempo de espera en ventanilla el día de su visita.
2.	Definición Caso de Uso
2.1.	Actores.
Usuario externo / Paciente (accede desde su computadora o teléfono).
Sistema (portal web y plataforma de pago en línea).

2.2.	Precondiciones.
1.	El paciente debe contar con acceso a internet.
2.	Deben existir citas disponibles en el sistema.

2.3.	Flujo Normal Básico
1.	El usuario externo ingresa al sitio web de la clínica. 
2.	El sistema muestra la página de inicio con las opciones disponibles. [RN01] 
3.	El usuario externo selecciona la opción "Agendar cita en línea". 
4.	El sistema muestra las fechas y horarios disponibles. [RN23] 
5.	El usuario externo selecciona la fecha y horario de su preferencia e ingresa el motivo de la cita. 
6.	El sistema valida los datos del formulario. [RN05] 
7.	El sistema muestra el resumen de la cita y consulta al usuario externo si desea cambiar algún dato antes de continuar. [FA01] 
8.	El usuario externo confirma su selección. 
9.	El sistema verifica si el usuario externo está logueado. [FA03] 
10.	El usuario externo ingresa los datos de su tarjeta en la pantalla de pago. [FA02] 
11.	El sistema confirma que el pago fue exitoso. 
12.	El sistema genera un código QR único para la cita y lo envía al correo electrónico del paciente. 
13.	El paciente recibe la confirmación de su cita y el código QR en pantalla y por correo. 
14.	Fin del caso de uso.

2.4.	Flujos Alternos.
FA01 — Paciente desea cambiar su selección
1. El paciente indica que desea cambiar la fecha u horario seleccionado.
2. El sistema regresa al paso 4 del Flujo Normal.

FA02 — Pago rechazado
1. La plataforma de pago informa que la transacción no fue aprobada.
2. El sistema muestra al paciente un mensaje indicando que el pago no pudo procesarse.  [RN23]
3. El paciente puede intentar nuevamente con otro método de pago sin perder los datos de su cita seleccionada, o bien cancelar la solicitud.

FA03 — Usuario externo no está logueado 
1.	El sistema detecta que el usuario externo no ha iniciado sesión. 
2.	El sistema redirige al usuario externo a la página de inicio de sesión. 
3.	El usuario externo ingresa sus credenciales o se registra si no tiene una cuenta. 
4.	Una vez autenticado, el usuario externo regresa al paso 10 para proceder con el pago.
2.5.	Postcondiciones.
1.	El paciente recibe la confirmación de su cita y el código QR en su correo electrónico.
2.	El estado de la cita queda registrado en el sistema como pagada y pendiente de atención.

3.	Requerimientos Suplementarios o no Funcionales
El sistema debe garantizar la disponibilidad del portal web en todo momento para que los pacientes puedan agendar sus citas sin interrupciones.
El proceso de pago en línea debe cumplir con los estándares de seguridad correspondientes para proteger los datos de la tarjeta del paciente.

4.	Firmas Necesarias
En este punto se debe incluir la firma de aquellas personas que deban aprobar la presente especificación.
Persona	Sector	Firma
		
		

