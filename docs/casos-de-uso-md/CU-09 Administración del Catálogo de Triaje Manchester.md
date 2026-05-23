Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-09 Administración del Catálogo de Triaje Manchester
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al administrador gestionar el catálogo de motivos y discriminadores del sistema de Triaje Manchester, incluyendo su creación, edición y cambio de estado.
Definición Caso de Uso
Actores.
Administrador del sistema
Sistema.

Precondiciones.
Sesión iniciada con rol ADMIN. El administrador se encuentra en el módulo de Administración (/administrator)..
Flujo Normal Básico
El administrador selecciona "Catálogo de Triaje Manchester" desde el Panel de Administrador.
El sistema muestra la página con dos pestañas: Motivos (N) activa por defecto y Discriminadores (N). En la pestaña Motivos presenta la lista de motivos registrados con columnas: Código, Descripción, Categoría, Estado y Acciones. Incluye un buscador por código o descripción y el botón "+ Agregar motivo".
El administrador opera sobre la pestaña Motivos. [FA01] [FA02]
El administrador hace clic en la pestaña Discriminadores (N).
El sistema muestra la lista de discriminadores registrados con columnas: Código, Descripción, Prioridad, Estado y Acciones. Incluye un buscador por código o descripción y el botón "+ Agregar discriminador". Cada fila muestra el badge de nivel de prioridad con color Manchester (Rojo, Naranja, Amarillo, Verde, Azul).
El administrador opera sobre la pestaña Discriminadores. [FA03] [FA04]
El caso de uso termina cuando el administrador navega fuera del módulo.
Flujos Alternos.
FA01 — El administrador agrega un motivo
El administrador hace clic en "+ Agregar motivo" desde la pestaña Motivos.
El sistema muestra el modal "Agregar motivo" con los siguientes campos:
Código * — texto libre, se convierte a mayúsculas automáticamente
Categoría — texto libre, opcional (ej. Cardiovascular, Neurológico)
Descripción * — texto libre (ej. Dolor torácico)
Estado * — select: Activo / Inactivo (por defecto: Activo)
Botones: Cancelar / Guardar
El administrador completa los campos y hace clic en "Guardar". [FA05]
El sistema valida que Código y Descripción no estén vacíos. [RN-01]
El sistema registra el nuevo motivo y cierra el modal. La tabla de motivos se actualiza.
Se retorna al paso 2.3.3 del Flujo Normal Básico.
FA02 — El administrador edita un motivo
El administrador hace clic en "Editar" en la fila del motivo.
El sistema muestra el modal "Editar motivo" con los campos prellenados con los datos actuales del motivo (mismos campos que FA01 Paso 2).
El administrador modifica los campos deseados y hace clic en "Guardar". [FA05]
El sistema valida que Código y Descripción no estén vacíos. [RN-01]
El sistema actualiza el motivo y cierra el modal. La tabla se actualiza.
Se retorna al paso 2.3.3 del Flujo Normal Básico.
FA03 — El administrador agrega un discriminador
El administrador hace clic en "+ Agregar discriminador" desde la pestaña Discriminadores.
El sistema muestra el modal "Agregar discriminador" con los siguientes campos:
Código * — texto libre, se convierte a mayúsculas automáticamente
Prioridad * — select: Rojo (0 min), Naranja (10 min), Amarillo (60 min), Verde (120 min), Azul (240 min) — por defecto: Verde
Descripción * — texto libre (ej. Dolor precordial irradiado)
Motivo asociado — select con los motivos activos del catálogo, opcional
Estado * — select: Activo / Inactivo (por defecto: Activo)
Botones: Cancelar / Guardar
El administrador completa los campos y hace clic en "Guardar". [FA05]
El sistema valida que Código y Descripción no estén vacíos. [RN-02]
El sistema registra el nuevo discriminador y cierra el modal. La tabla de discriminadores se actualiza.
Se retorna al paso 3.2.6 del Flujo Normal Básico.

FA04 — El administrador edita un discriminador
El administrador hace clic en "Editar" en la fila del discriminador.
El sistema muestra el modal "Editar discriminador" con los campos prellenados con los datos actuales (mismos campos que FA03.2).
El administrador modifica los campos deseados y hace clic en "Guardar". [FA05]
El sistema valida que Código y Descripción no estén vacíos. [RN-02]
El sistema actualiza el discriminador y cierra el modal. La tabla se actualiza.
Se retorna al paso 3.2.6 del Flujo Normal Básico.

FA05 — Cancelar registro
El administrador selecciona "Cancelar".
El sistema descarta los datos ingresados y regresa a la lista de exámenes.
Fin del flujo alterno.

Postcondiciones.
El catálogo de motivos y/o discriminadores refleja los cambios realizados (creación, edición o cambio de estado) de forma inmediata en la tabla correspondiente.
Un motivo desactivado deja de aparecer en el select "Motivo asociado" dentro del formulario de discriminadores.
Un discriminador desactivado no es presentado al personal de triaje durante la clasificación Manchester de un paciente.

Requerimientos Suplementarios o no Funcionales
Solo usuarios autenticados con rol ADMIN pueden acceder al módulo de administración del catálogo de Triaje.
El filtrado por código o descripción se aplica de forma instantánea sobre la lista activa sin recargar la página.
Cada nivel de prioridad Manchester se distingue mediante un badge con color propio (Rojo, Naranja, Amarillo, Verde, Azul) tanto en la tabla como en el formulario.

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
