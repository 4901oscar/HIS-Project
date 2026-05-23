Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-07 Catálogo de Exámenes de Laboratorio
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al administrador registrar, editar y controlar el estado de los tipos de exámenes de laboratorio disponibles en el sistema, garantizando que el catálogo esté actualizado para su uso en órdenes clínicas.
Definición Caso de Uso
Actores.
Administrador del sistema
Sistema.

Precondiciones.
El administrador debe haber iniciado sesión en el sistema con rol ADMIN.
Flujo Normal Básico
El administrador selecciona "Catálogo de Exámenes de Laboratorio" desde el Panel de Administrador.
El sistema muestra la lista de exámenes registrados con las columnas: Código, Nombre, Descripción, Estado y Acciones. Incluye un buscador por código o nombre y el botón "+ Agregar examen".
El administrador selecciona "+ Agregar examen". [FA01]
El sistema muestra el formulario "Agregar examen" con los campos requeridos y opcionales.
El administrador completa los campos:
Código (obligatorio — máx. 20 caracteres, el sistema lo convierte automáticamente a mayúsculas).
Nombre (obligatorio — máx. 200 caracteres).
Descripción (opcional — máx. 500 caracteres).
Tipo de examen (opcional — ej. Hematología, Química Clínica, Uroanálisis).
Tipo de muestra (opcional — ej. Sangre, Orina, Heces).
El administrador selecciona "Guardar". [FA02]
El sistema valida que los campos obligatorios estén completos y que el código no esté duplicado.
El sistema registra el examen con estado ACTIVO y lo muestra en la lista actualizada.
Fin del caso de uso.
Flujos Alternos.
FA01 — El administrador edita un examen existente
En el paso 2.3.3, el administrador selecciona "Editar" en la columna Acciones del examen que desea modificar.
El sistema muestra el formulario "Editar examen" con los datos actuales precargados, incluyendo el campo Estado.
El administrador modifica los campos que desea actualizar: nombre, descripción, tipo de examen, tipo de muestra o estado.
El administrador selecciona "Guardar". [FA02]
El sistema actualiza los datos del examen y regresa a la lista actualizada.

FA04 — Cancelar registro
El administrador selecciona "Cancelar".
El sistema descarta los datos ingresados y regresa a la lista de exámenes.
Fin del flujo alterno.


Postcondiciones.
Creación:
El examen queda registrado en el sistema con estado ACTIVE y disponible para ser solicitado en órdenes clínicas.
El código del examen queda almacenado en mayúsculas.
Edición:
Los cambios realizados al examen quedan guardados de forma inmediata.
Si el estado fue cambiado a INACTIVO o ELIMINADO, el examen deja de estar disponible para nuevas órdenes clínicas.

Requerimientos Suplementarios o no Funcionales
Solo el administrador del sistema tiene acceso al módulo de catálogo de exámenes de laboratorio.
El código de cada examen debe ser único en el sistema; el sistema lo verifica al momento de guardar y notifica si ya existe.
El sistema convierte automáticamente el código ingresado a mayúsculas antes de almacenarlo.
Los exámenes no se eliminan físicamente; el sistema maneja eliminación lógica mediante el estado ELIMINADO

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
