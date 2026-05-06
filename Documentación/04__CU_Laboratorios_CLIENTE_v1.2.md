Introducción
Descripción:
Describe el proceso mediante el cual el personal de laboratorio recibe las órdenes del médico, toma las muestras, las procesa y carga los resultados para que el médico pueda continuar con la consulta.

Objetivo:
Garantizar que los resultados lleguen de forma rápida y ordenada al médico, manteniendo el registro completo en el expediente del paciente.

Actores
•	Personal de laboratorio.
•	Sistema.

Condiciones de Inicio
•	El paciente debe tener una orden de examen activa generada por el médico.
•	El paciente debe haber cancelado el costo de los exámenes en caja.
•	El personal de laboratorio debe haber ingresado al sistema (ver Caso de Uso 00.1).

Flujo Normal
1.	El personal ingresa al módulo de laboratorio. El sistema muestra pacientes con exámenes pendientes por hora de llegada.
2.	El personal selecciona al paciente y visualiza el detalle de los exámenes solicitados.
3.	El personal llama al paciente y confirma la toma de muestra. El sistema actualiza el estado a "Muestra en proceso".  [FA01]
4.	El personal procesa la muestra en los equipos del laboratorio.
5.	El personal selecciona "Cargar resultados". El sistema muestra el formulario del tipo de examen.
6.	El personal ingresa los valores del resultado y/o adjunta el documento PDF.  [RN12] [RN13] [FA02]
7.	El personal guarda y envía los resultados.  [RN05]
8.	El sistema verifica si el paciente tiene más exámenes pendientes en la misma visita.  [FA03]
9.	El sistema notifica al médico y regresa al paciente a la cola de consulta.
10.	Fin del caso de uso.

Flujos Alternos
FA01 — Muestra rechazada
11.	El personal determina que la muestra no es adecuada.
12.	El personal selecciona "Rechazar muestra" e indica el motivo desde la lista disponible.  [RN14]
13.	El sistema notifica al paciente que debe regresar para una nueva toma.
14.	El proceso retoma desde el paso 3 con la nueva muestra.

FA02 — Datos incompletos o inválidos
15.	El sistema detecta que no se ingresaron resultados ni se adjuntó documento válido.  [RN12] [RN13]
16.	El sistema muestra: "Debe ingresar los valores del resultado o adjuntar un documento de soporte válido."
17.	El personal corrige y continúa desde el paso 7.

FA03 — Paciente con varios exámenes pendientes
18.	El sistema detecta que hay más exámenes pendientes en la misma visita.
19.	El sistema mantiene al paciente en laboratorio y retoma desde el paso 2 para el siguiente examen.
20.	Al completar todos, el sistema notifica al médico y el paciente regresa a consulta.
