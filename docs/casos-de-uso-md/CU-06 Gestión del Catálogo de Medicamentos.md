Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-06: Gestión del Catálogo de Medicamentos
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al administrador registrar, editar y controlar el estado de los medicamentos disponibles en el sistema, garantizando que el inventario esté actualizado y que se alerte cuando el stock de un medicamento esté por debajo del mínimo establecido.
Definición Caso de Uso
Actores.
Administrador del sistema
Sistema.

Precondiciones.
El administrador debe haber iniciado sesión en el sistema con rol ADMIN.
Flujo Normal Básico
El administrador selecciona "Catálogo de Medicamentos" desde el Panel de Administrador.
El sistema muestra la lista de medicamentos registrados con las columnas: Nombre, Descripción, Unidad, Stock Actual, Stock Mínimo, Estado y Acciones. Incluye un buscador por nombre y un filtro "Stock bajo" para mostrar únicamente los medicamentos con stock por debajo del mínimo establecido, y el botón "+ Agregar medicamento".
El administrador selecciona "+ Crear medicamento". [FA1]
El sistema muestra el formulario "Agregar Medicamento" con los campos requeridos.
El administrador completa los campos obligatorios:
Nombre del medicamento (obligatorio)
Descripción (opcional)
Unidad de medida (obligatorio — ej. tabletas, mg, ml, frascos)
Stock inicial (obligatorio — solo números enteros mayores o iguales a cero)
Stock mínimo (obligatorio — solo números enteros mayores o iguales a cero y que no sean mayores a stock inicial) [RN-02]
El administrador selecciona "Guardar". [FA02]
El sistema guarda el medicamento con estado "Activo" y regresa a la lista de medicamentos actualizada.
Fin del caso de uso.
Flujos Alternos.
FA01 — El administrador edita un medicamento existente
En el paso 2.3.3, el administrador selecciona "Editar" en la columna Acciones del medicamento que desea modificar.
El sistema muestra el formulario "Editar Medicamento" con los datos actuales precargados.
El administrador modifica los campos que desea actualizar: nombre, descripción, unidad de medida, stock mínimo o estado.
El administrador selecciona "Guardar". [FA03]
El sistema actualiza los datos del medicamento y regresa a la lista actualizada.

FA02 — El administrador actualiza el stock de un medicamento
En el paso 2.3.3, el administrador selecciona "Stock" en la columna Acciones del medicamento que desea actualizar.
El sistema muestra el formulario de actualización con el stock actual del medicamento.
El administrador ingresa el nuevo valor de stock.
El administrador confirma la actualización. [FA03]
El sistema actualiza el stock. Si el nuevo valor es menor al mínimo establecido, el medicamento se resalta en rojo en la lista. [RN-02]
Fin del flujo alterno.

FA04 — Cancelar registro
El administrador selecciona "Cancelar".
El sistema descarta los datos ingresados y regresa a la lista de clínicas.
Fin del flujo alterno.


Postcondiciones.
Creación:
El medicamento queda registrado en el sistema con estado ACTIVE y disponible para ser recetado.
Edición:
Los cambios realizados al medicamento quedan guardados de forma inmediata.
Si el estado fue cambiado a Inactivo o Eliminado, el medicamento deja de estar disponible para nuevas recetas.
Actualización de stock:
El stock actual del medicamento queda actualizado en el sistema.
Si el nuevo stock es menor al mínimo establecido, el sistema activa la alerta visual de stock bajo. [RN-02]

Requerimientos Suplementarios o no Funcionales
Solo el administrador del sistema tiene acceso al módulo de gestión del catálogo de medicamentos.
El sistema debe alertar visualmente de forma inmediata cuando el stock actual de un medicamento sea menor al stock mínimo establecido. [RN-02]
Los valores de stock no pueden ser negativos; el sistema lo valida antes de guardar.
Los medicamentos no se eliminan físicamente; el sistema maneja eliminación lógica mediante el estado

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
