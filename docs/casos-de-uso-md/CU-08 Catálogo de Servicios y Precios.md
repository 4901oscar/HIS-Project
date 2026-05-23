Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-08: Catálogo de Servicios y Precios
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al administrador registrar y mantener el catálogo de servicios que ofrece la clínica con sus respectivos precios, clasificados por categoría, para su uso en la facturación de atenciones médicas.
Definición Caso de Uso
Actores.
Administrador del sistema
Sistema.

Precondiciones.
El administrador debe haber iniciado sesión en el sistema con rol ADMIN.
Flujo Normal Básico
El administrador selecciona "Servicios y Precios" desde el Panel de Administrador.
El sistema muestra la lista de servicios registrados con las columnas: Código, Nombre, Categoría, Precio, Estado y Acciones. Incluye un buscador por código o nombre, un filtro por categoría (Consulta, Laboratorio, Medicamento, Procedimiento, Otro) y el botón "+ Agregar servicio".
El administrador selecciona "+ Agregar servicio". [FA01]
El sistema muestra el formulario "Agregar servicio" con los campos requeridos.
El administrador completa los campos:
Categoría (obligatoria — Consulta, Laboratorio, Medicamento, Procedimiento u Otro). [FA02] [FA03]
Código (obligatorio — máx. 20 caracteres, el sistema lo convierte automáticamente a mayúsculas).
Nombre (obligatorio — máx. 200 caracteres).
Descripción (opcional — máx. 500 caracteres).
Precio (obligatorio — valor mayor a Q 0.00).
El administrador selecciona "Guardar". [FA04]
El sistema valida que los campos obligatorios estén completos, que el precio sea mayor a cero y que el código no esté duplicado.
El sistema registra el servicio con estado ACTIVO y lo muestra en la lista actualizada.
Fin del caso de uso.
Flujos Alternos.
FA01 — El administrador edita un servicio existente
En el paso 2.3.3, el administrador selecciona "Editar" en la columna Acciones del servicio que desea modificar.
El sistema muestra el formulario "Editar servicio" con los datos actuales precargados, incluyendo el campo Estado.
El administrador modifica los campos que desea actualizar: categoría, código, nombre, descripción, precio o estado.
El administrador selecciona "Guardar". [FA04]
El sistema actualiza los datos del servicio y regresa a la lista actualizada.
FA02 — El administrador selecciona la categoría Laboratorio
En el paso 2.3.5.1, el administrador selecciona la categoría "Laboratorio".
El sistema carga el catálogo de exámenes de laboratorio disponibles y muestra un selector adicional.
El administrador selecciona el examen del listado.
El sistema autocompleta automáticamente el código con el patrón LAB-{código del examen} y el nombre con el nombre del examen seleccionado.
El administrador completa el precio y continúa desde el paso 2.3.6.
FA03 — El administrador selecciona la categoría Medicamento
En el paso 2.3.5.1, el administrador selecciona la categoría "Medicamento".
El sistema carga el catálogo de medicamentos disponibles y muestra un selector adicional.
El administrador selecciona el medicamento del listado.
El sistema autocompleta automáticamente el código con el patrón MED-{nombre} y el nombre con el nombre del medicamento seleccionado.
El administrador completa el precio y continúa desde el paso 2.3.6.

FA04 — Cancelar registro
El administrador selecciona "Cancelar".
El sistema descarta los datos ingresados y regresa a la lista de exámenes.
Fin del flujo alterno.


Postcondiciones.
Creación:
El servicio queda registrado en el sistema con estado ACTIVO  y disponible para ser utilizado en la facturación de atenciones.
El código del servicio queda almacenado en mayúsculas.
Edición:
Los cambios realizados al servicio quedan guardados de forma inmediata.
Si el estado fue cambiado a INACTIVO o ELIMINADO, el servicio deja de estar disponible para nuevas facturaciones.

Requerimientos Suplementarios o no Funcionales
Solo el administrador del sistema tiene acceso al módulo de catálogo de servicios y precios.
El código de cada servicio debe ser único en el sistema; el sistema lo verifica al momento de guardar y notifica si ya existe.
El sistema convierte automáticamente el código ingresado a mayúsculas antes de almacenarlo.
El precio debe ser un valor numérico mayor a Q 0.00 con precisión de dos decimales.
Los servicios no se eliminan físicamente; el sistema maneja eliminación lógica mediante el estado ELIMINADO

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
