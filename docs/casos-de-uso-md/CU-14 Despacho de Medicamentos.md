Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-14: Despacho de Medicamentos
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al personal de farmacia visualizar las recetas pendientes de despacho, revisar los medicamentos prescritos por el médico y registrar la entrega de medicamentos al paciente, completando el ciclo de atención de la cita.
Definición Caso de Uso
Actores.
Actor principal: Farmacéutico
Sistema.

Precondiciones.
Existe una cita con estado PHARMACY, generada cuando el cajero confirmó el pago de la receta (CU-15) luego de que el médico emitió la prescripción (CU-12).
2.2.2.	La cita tiene asociada una prescripción con estado PENDING en el clinical-service.
2.2.3.	El farmacéutico tiene sesión activa con rol PHARMACY.
Flujo Normal Básico
El farmacéutico ingresa al Módulo de Farmacia; el sistema carga la cola de despacho mostrando todas las citas en estado de Farmacia, ordenadas por fecha y hora de cita, con columnas de Fecha, Hora, Paciente, DPI y Código de Receta y la columna Acción.
El farmacéutico presiona Atender en la fila del paciente; el sistema reproduce una notificación de audio con el nombre del paciente ("Nombre del paciente, por favor pasar a Farmacia") y carga el detalle de la receta. [FA01]
El sistema muestra la pantalla de detalle con tres secciones: Información del Paciente (nombre completo, DPI, teléfono, correo), Información de la Receta (código de receta, doctor, fecha de emisión) y tabla de Medicamentos.
La tabla de Medicamentos muestra por cada ítem: nombre del medicamento, dosis, frecuencia, duración en días, vía de administración y cantidad total.
El farmacéutico verifica físicamente que los medicamentos listados estén disponibles y los prepara.
El farmacéutico presiona Dispensar Medicamentos; el sistema registra el despacho, actualiza el estado de la receta a DISPENSED y transiciona la cita de Farmacia a Completada.
El sistema muestra una confirmación de éxito y regresa automáticamente a la cola de despacho; la cita despachada ya no aparece en la lista.
Fin del caso de uso.
Flujos Alternos.
FA01 — Paciente no se presenta al llamado (Paso 2)
1.	El farmacéutico llama al paciente mediante el sistema y el paciente no se presenta al área de farmacia.
2.	El farmacéutico presiona Volver a la Cola para dejar la receta pendiente y atender al siguiente paciente.
3.	El flujo retoma desde el paso 2.3.1 con el siguiente paciente en cola.
Postcondiciones.
La receta queda registrada con estado DISPENSED en el sistema.
La cita transiciona al estado COMPLETED, cerrando el ciclo de atención del paciente.
La cita despachada desaparece de la cola de farmacia.
El despacho queda disponible en el historial clínico del paciente para consulta futura.
Requerimientos Suplementarios o no Funcionales
La cola de farmacia no tiene auto-refresco; el farmacéutico debe presionar Actualizar manualmente para ver nuevas recetas.
El sistema reproduce automáticamente el llamado de audio al seleccionar un paciente; no requiere acción adicional del farmacéutico.
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
