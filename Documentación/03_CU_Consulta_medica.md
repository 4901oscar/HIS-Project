1.	Introducción
1.1.	Objetivo.
Garantizar que el médico atienda a los pacientes en el orden de prioridad establecido por el sistema Manchester, pudiendo emitir un diagnóstico completo, ordenar exámenes de laboratorio, agendar citas de seguimiento y gestionar la medicación del paciente. Cualquier notificación de emergencia interrumpe el flujo normal y reordena la atención de forma inmediata.

2.	Definición Caso de Uso
2.1.	Actores.
Médico.
Sistema.

2.2.	Precondiciones.
1.	El médico debe haber ingresado al sistema (ver Caso de Uso 00.1).
2.	Debe existir al menos un paciente en la cola de consulta del médico con estado "En espera de consulta", proveniente del proceso de triaje (ver Caso de Uso CU-03A).

2.3.	Flujo Normal Básico
FB1. El médico ingresa a su bandeja de consultas. El sistema muestra la lista de pacientes pendientes de atención, ordenados por nivel de prioridad Manchester y, dentro del mismo nivel, por hora de llegada.  [RN07]
FB2. El médico selecciona al primer paciente de la lista y lo llama a consulta. El sistema actualiza el estado del paciente a "En consulta".
FB3. El sistema muestra el historial clínico del paciente y los signos vitales registrados por la enfermera durante el triaje (CU-03A).  [FA01]
FB4. El médico realiza la evaluación clínica, ingresa sus notas de evolución y registra el diagnóstico seleccionando el código correspondiente del catálogo CIE-10.  [RN11]
FB5. El sistema consulta al médico si el paciente requiere exámenes de laboratorio. El médico indica que NO.  [FA02]
FB6. El sistema consulta al médico si el paciente requiere diagnósticos adicionales o una cita de seguimiento. El médico indica que NO.  [FA03]
FB7. El sistema consulta al médico si el paciente requiere medicamentos. El médico indica si aplica o no.  [RN08] [FA04]
FB8. El médico cierra la consulta. El sistema actualiza el estado del paciente a "Pendiente de pago en farmacia" y lo envía a la bandeja de facturación.
FB9. Fin del caso de uso.

2.4.	Flujos Alternos.
FA01 — Notificación de emergencia (interrupción del flujo normal)
1. En cualquier momento durante el flujo normal, el sistema recibe una notificación de emergencia proveniente del módulo de Triaje (CU-03A FA02), correspondiente a un paciente con nivel Rojo.
2. El sistema genera una alerta visual y sonora en la pantalla del médico, independientemente del paso en que se encuentre la consulta actual.  [RN06] [RN07]
3. El médico revisa la alerta. Si está en medio de una consulta activa, la pausa de forma ordenada: guarda los avances del paciente actual y el sistema actualiza su estado a "Consulta pausada".
4. El sistema coloca al paciente de emergencia en el primer lugar de la cola y el médico lo selecciona para atención inmediata.
5. El médico atiende al paciente de emergencia. El flujo de esta consulta sigue desde el paso FB4 del Flujo Normal Básico.
6. Al finalizar la atención del paciente de emergencia, el sistema restaura la consulta pausada y el médico retoma desde el punto donde la dejó.

FA02 — Paciente requiere exámenes de laboratorio
1. En el paso FB5, el médico indica que el paciente necesita exámenes. Selecciona los exámenes del catálogo disponible y guarda la orden.  [RN12]
2. El sistema cambia el estado del paciente a "Pendiente de pago por laboratorio" y pausa la consulta actual.
3. El paciente es derivado a caja para cancelar el costo de los exámenes (ver Caso de Uso 05). Una vez pagado, pasa al área de laboratorio (ver Caso de Uso 04).
4. Cuando los resultados de laboratorio están listos, el sistema notifica al médico y devuelve al paciente a la cola de consulta con su prioridad original.
5. El médico revisa los resultados y el flujo retoma en el paso FB6 del Flujo Normal Básico.

FA03 — Médico agenda cita de seguimiento
1. En el paso FB6, el médico indica que el paciente necesita una cita de seguimiento o diagnósticos adicionales.
2. El sistema muestra el formulario de agendamiento. El médico selecciona la fecha propuesta y confirma.
3. El sistema registra la cita de seguimiento en el expediente del paciente.
4. El flujo retoma en el paso FB7 del Flujo Normal Básico para continuar con la gestión de medicamentos de la visita actual.

FA04 — Paciente no puede pagar los medicamentos
1. En el paso FB7, el médico determina que el paciente no puede cubrir el costo de los medicamentos recetados.  [RN10]
2. El sistema genera una receta impresa con la firma del médico para que el paciente adquiera los medicamentos en una farmacia externa.
3. El médico cierra la consulta sin receta interna. El flujo continúa en el paso FB8 del Flujo Normal Básico.

2.5.	Poscondiciones.
3.	El expediente clínico del paciente queda actualizado con el diagnóstico CIE-10, notas de evolución, exámenes ordenados (si aplica), cita de seguimiento (si aplica) y receta emitida (si aplica).
4.	El estado del paciente se actualiza a "Pendiente de pago en farmacia" o cierra con receta externa según corresponda.
5.	Todos los registros de la consulta son inmutables una vez guardados.

3.	Requerimientos Suplementarios o no Funcionales
El sistema debe garantizar que la cola de atención del médico se actualice en tiempo real ante cualquier cambio de prioridad o llegada de nuevos pacientes.
La notificación de emergencia debe interrumpir la interfaz del médico de forma inmediata, sin importar el módulo donde se encuentre, con alerta visual y sonora.
El historial clínico y el diagnóstico deben quedar guardados de forma inmutable; no deben poder modificarse una vez cerrada la consulta.
