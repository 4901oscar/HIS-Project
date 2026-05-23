Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-05: Gestión de Doctores
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al administrador vincular empleados con rol Doctor al sistema clínico, asignándoles una clínica y turno de atención, y gestionar su disponibilidad mediante el registro de días libres o vacaciones.
Definición Caso de Uso
Actores.
Administrador del sistema
Sistema.

Precondiciones.
El administrador debe haber iniciado sesión en el sistema con rol ADMIN.
Deben existir empleados registrados con rol Doctor en el sistema. (CU-02)
Debe existir al menos una clínica activa registrada en el sistema. (CU-04)

Flujo Normal Básico
El administrador selecciona "Gestión de Doctores" desde el Panel de Administrador.
El sistema muestra las tarjetas de los doctores vinculados con su nombre, clínica asignada, turno de atención y estado. Incluye un buscador por nombre, el botón "+ Vincular Doctor" y en cada tarjeta los botones Editar, Días Libres y Desactivar. [FA01] [FA02] [FA03].
El administrador selecciona "+ Vincular Doctor". [FA04]
El sistema muestra el formulario "Vincular Doctor" con los campos requeridos.
El administrador completa los campos:
Doctor (obligatorio) — selecciona de la lista desplegable de empleados con rol Doctor disponibles.
Clínica (obligatorio) — selecciona de la lista desplegable de clínicas activas registradas en el sistema.
Hora de inicio del turno (obligatorio — formato HH:mm, ej. 08:00).
Hora de fin del turno (obligatorio — formato HH:mm, ej. 16:00). [RN-03]
El administrador selecciona "VINCULAR". [FA04]
El sistema valida que el turno ingresado sea de exactamente 8 horas. [RN-03]
El sistema registra la vinculación del doctor con estado ACTIVO y lo muestra en el listado actualizado.
Fin del caso de uso.
Flujos Alternos.
FA01 — El administrador edita un doctor vinculado
En el paso 2.3.3, el administrador selecciona "Editar" en la tarjeta del doctor que desea modificar.
El sistema muestra el formulario "Editar Doctor" con los datos actuales precargados. El campo nombre es de solo lectura.
El administrador modifica los campos que desea actualizar: clínica asignada, hora de inicio o hora de fin del turno.
El administrador selecciona "ACTUALIZAR". [FA04] [RN-03]
El sistema valida que el turno modificado sea de exactamente 8 horas. [RN-03]
El sistema guarda los cambios y regresa al listado actualizado.

FA02 — El administrador gestiona los días libres de un doctor
En el paso 2.3.3, el administrador selecciona "Días Libres" en la tarjeta del doctor.
El sistema muestra la pantalla "Administra la disponibilidad del Dr. {nombre}" con dos pestañas: Calendario y Marcar Días Libres, y el botón "← Volver" para regresar al listado.
La pestaña de calendario únicamente muestra en el calendario los días que el doctor ya tiene asignados como libres en color rojo.
FA02a — Marcar un período de días libres
El administrador selecciona la sección "Marcar Días Libres".
El sistema muestra el formulario con los campos:
Fecha de Inicio (obligatoria)
Fecha de Fin (obligatoria)
Motivo (obligatorio — ej. Vacaciones, Capacitación)
El administrador completa los campos y selecciona "MARCAR DÍAS LIBRES". [FA02]
El sistema registra el rango de fechas como días libres. El doctor no podrá ser asignado a citas durante ese período
Los días quedan reflejados en el calendario con color rojo.

FA02b — Eliminar un día libre desde el calendario
El administrador selecciona la pestaña "Calendario".
El sistema muestra el calendario con los días disponibles en verde y los días libres en rojo.
El administrador selecciona un día marcado en rojo.
El sistema elimina ese día libre y lo refleja de inmediato en el calendario como disponible.

FA03 — El administrador desactiva un doctor
En el paso 2.3.3, el administrador selecciona "Desactivar" en la tarjeta del doctor.
El sistema solicita confirmación mediante un mensaje: "¿Está seguro de que desea desactivar a este doctor?"
El administrador confirma la acción.
El sistema cambia el estado del doctor a INACTIVO y lo retira del listado activo.
FA04 — Cancelar
El administrador selecciona "Cancelar" en cualquier punto del formulario.
El sistema descarta los cambios y regresa al listado de doctores.


Postcondiciones.
2.5. Postcondiciones
Vinculación:
El doctor queda vinculado al sistema clínico con su clínica y turno asignados, con estado ACTIVE.
El doctor queda disponible para recibir citas en su clínica y turno registrados.
Edición:
Los cambios de clínica o turno del doctor quedan guardados de forma inmediata.
Días libres:
El rango de fechas marcado queda registrado y el doctor no podrá ser asignado a citas durante ese período. [RN-04]
Si se eliminó un día libre, el doctor recupera su disponibilidad en esa fecha de forma inmediata.
Desactivación:
El doctor pasa a estado INACTIVE y deja de aparecer en el listado activo.
El doctor no podrá ser asignado a nuevas citas mientras esté inactivo.

Requerimientos Suplementarios o no Funcionales
Solo el administrador del sistema tiene acceso al módulo de gestión de doctores.
El turno asignado al doctor debe ser de exactamente 8 horas; el sistema lo valida antes de guardar. [RN-03]
Un doctor no puede ser asignado a citas en fechas registradas como días libres. [RN-04]
Los doctores no se eliminan físicamente del sistema; la desactivación es lógica mediante el estado INACTIVO

Firmas Necesarias
En este punto se debe incluir la firma de aquellas personas que deban aprobar la presente especificación.


| Nombre | Fecha | Descripción del Cambio | Versión |
|---|---|---|---|
| Oscar Rivera | 19/02/2026 | Definición inicial. | 1.0 |
| Jose Avila | 20/02/2026 | Definición inicial. | 1.1 |
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
