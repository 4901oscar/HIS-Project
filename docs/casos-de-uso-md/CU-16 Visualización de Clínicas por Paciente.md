Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-16 Consulta de Citas por Paciente
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al paciente autenticado consultar el directorio de clínicas activas del hospital nombre, descripción, doctor asignado y horario de atención — desde su portal personal, para que pueda planificar su visita o identificar la clínica a la que fue referido.
Definición Caso de Uso
Actores.
Actor principal: Paciente
Sistema.

Precondiciones.
El paciente debe haber iniciado sesión en el sistema con rol Paciente.
Flujo Normal Básico
El paciente inicia sesión en el portal y el sistema lo redirige a la página de inicio.
El paciente hace clic sobre su nombre en la barra de navegación superior;
El sistema despliega el menú con las opciones: Mi Perfil, Mis Citas y Salir.
El paciente selecciona "Mis Citas".
El sistema muestra la página "Mis Citas" con el listado de citas ordenadas de más reciente a más antigua. Por cada cita presenta: Fecha, Hora, Motivo (notas) y Estado (badge de color). [FA01][FA02].
El paciente localiza la cita de su interés y selecciona "Ver".
El sistema abre el modal de detalle de cita cargando en paralelo: Signos vitales, Clasificación Manchester, Datos de consulta médica (diagnóstico, síntomas, plan de tratamiento), Exámenes de laboratorio y resultados, y Medicamentos prescritos. [FA03]
El paciente revisa la información clínica de su cita.
El paciente cierra el modal con la X o haciendo clic fuera de él.
.Fin del caso de uso.
Flujos Alternos.
FA01 — Sin citas registradas
En el paso 4, si el servicio retorna una lista vacía, el sistema muestra el mensaje "No tienes citas registradas."
El flujo termina. El paciente puede navegar a otra sección del sistema.

FA02 — Sección clínica sin datos para la cita seleccionada
En el paso 7, si una sección específica no tiene datos registrados (la cita aún no ha pasado por triaje, consulta o laboratorio), el sistema muestra para cada sección vacía su mensaje correspondiente en cursiva:
"No se registraron signos vitales para esta cita."
"No se realizó triaje para esta cita."
"No se registró consulta médica para esta cita."
"No se emitió receta para esta cita."
"No se solicitaron exámenes de laboratorio para esta cita."
Las demás secciones con datos se muestran normalmente. El flujo continúa.

Postcondiciones.
El sistema no modifica ningún dato — la operación es de solo lectura.
El historial de citas del paciente permanece sin cambios en la base de datos.
No se genera ningún registro de auditoría por la consulta
Requerimientos Suplementarios o no Funcionales
a lista de citas se refresca automáticamente cada 30 segundos para reflejar cambios de estado en tiempo cuasi-real, sin requerir acción del usuario.
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
