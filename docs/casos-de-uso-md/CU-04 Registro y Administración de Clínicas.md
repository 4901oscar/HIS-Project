Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-04 Registro y Administración de Clínicas
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al administrador registrar y gestionar los consultorios y espacios clínicos disponibles en el sistema.
Definición Caso de Uso
Actores.
Administrador del sistema
Sistema.

Precondiciones.
El administrador debe haber iniciado sesión en el sistema con rol ADMIN.
Flujo Normal Básico
El administrador selecciona "Gestión de Clínicas" desde el Panel de Administrador.
El sistema muestra la lista de clínicas registradas con las columnas: Código, Nombre, Descripción, Estado y Acciones. Incluye un buscador por código, nombre o descripción, un filtro por estado (Activa, Inactiva, Eliminada) y el botón "+ Crear Clínica".
El administrador selecciona "+ Crear Clínica". [FA1]
El sistema muestra el formulario "Crear Nueva Clínica" con los campos requeridos.
El administrador completa los campos obligatorios:
Código (solo números — ej: 101 para nivel 1, oficina 01)
Nombre (solo caracteres alfanuméricos)
Descripción (solo caracteres alfanuméricos)
El administrador selecciona "CREAR". [FA02]
El sistema guarda la clínica con estado "Activa" y regresa a la lista de clínicas actualizada.
Fin del caso de uso.
Flujos Alternos.
FA01 — Editar clínica
El administrador selecciona "Editar" en una clínica de la lista.
El sistema muestra el formulario "Editar Clínica" con los datos actuales precargados, incluyendo el campo Estado (Activa, Inactiva, Eliminada).
El administrador modifica los campos deseados y selecciona "ACTUALIZAR".  [FA02]
El sistema guarda los cambios y regresa a la lista de clínicas actualizada.
Fin del flujo alterno.


FA02 — Cancelar registro
El administrador selecciona "Cancelar".
El sistema descarta los datos ingresados y regresa a la lista de clínicas.
Fin del flujo alterno.


Postcondiciones.
Creación:
La clínica queda registrada en el sistema con estado ACTIVE y disponible para ser asignada a citas y servicios.
Edición:
Los datos actualizados de la clínica quedan reflejados de forma inmediata en el sistema.
Si el estado fue cambiado a INACTIVE, la clínica deja de estar disponible para nuevas asignaciones, pero sus registros históricos se conservan.

Requerimientos Suplementarios o no Funcionales
El código de la clínica debe ser único en el sistema; el sistema lo verifica al momento de guardar y notifica si ya existe.
Solo el administrador del sistema tiene acceso al módulo de gestión de clínicas.
Las operaciones de creación y edición deben completarse en menos de 2 segundos bajo condiciones normales de uso.
Los datos de una clínica no se eliminan físicamente; el sistema maneja eliminación lógica mediante el estado DELETED.

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
