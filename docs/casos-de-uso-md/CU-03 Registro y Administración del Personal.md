Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-02 Registro y Administración del Personal
Versión 1.2
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Permitir al administrador del sistema registrar, editar y gestionar las cuentas del personal de la clínica, asignando correctamente su rol para que puedan acceder al módulo correspondiente dentro del sistema.
Definición Caso de Uso
Actores.
Administrador del sistema
Sistema.

Precondiciones.
El administrador debe haber iniciado sesión en el sistema con rol ADMIN.
El empleado a registrar no debe tener una cuenta previa en el sistema.
Flujo Normal Básico
El administrador inicia sesión y el sistema muestra el Panel de Administrador con las opciones: Gestión de Personal, Gestión de Doctores, Gestión de Clínicas, Catálogo de Medicamentos, Catálogo de Exámenes, Servicios y Precios, Triaje Manchester y Reportes.
El administrador selecciona "Gestión de Personal".
El sistema muestra la lista del personal registrado con filtros de Rol y Estado (activo/inactivo) y el botón "+ Nuevo Empleado". [FA01][FA02]
El administrador selecciona "+ Nuevo Empleado".
El sistema muestra el formulario "Nuevo Empleado".
El administrador completa los campos obligatorios: Primer nombre, Primer apellido, Correo electrónico y Rol. [RN02][RN05]
El administrador completa los campos opcionales si lo desea: Segundo nombre, Segundo apellido y Teléfono.
El administrador selecciona "Registrar Empleado". [FA03][FA04]
El sistema crea la cuenta, genera una contraseña temporal y muestra: "Empleado registrado. Entregue esta contraseña temporal al empleado." con la contraseña visible. [RN-02]
El administrador anota la contraseña y la entrega al empleado. Puede seleccionar "Registrar otro" o "Ver lista".
Fin del caso de uso.
Flujos Alternos.
FA01 — Editar empleado
El administrador selecciona "Editar" en un empleado de la lista.
El sistema muestra el formulario "Editar Empleado" con los datos actuales precargados.
El administrador modifica los campos que desea actualizar.
El administrador selecciona "Guardar Cambios". [FA04]
El sistema guarda los cambios y regresa a la lista de personal.
Fin del flujo alterno.
FA02 — Desactivar o activar empleado
El administrador selecciona "Desactivar" o "Activar" en un empleado de la lista.
El sistema cambia el estado del empleado y actualiza la lista reflejando el nuevo estado.
Fin del flujo alterno.
FA03 — Empleado ya registrado
El sistema detecta que el correo electrónico ya existe en el sistema.
El sistema muestra el mensaje del servidor con el error “El correo electrónico ya está registrado: {correo registrado}”.
El formulario permanece visible con los datos ingresados.
El flujo retoma en el paso 2.3.6 del Flujo Normal.


FA04 — Cancelar registro
El administrador selecciona "Cancelar".
El sistema descarta los datos ingresados y regresa a la lista de personal.
Fin del flujo alterno.


Postcondiciones.
El nuevo empleado queda registrado en el sistema con su rol asignado y estado "Activo".
El sistema genera y muestra una contraseña temporal que el administrador debe entregar al empleado en mano — solo se muestra una vez. Adicionalmente, el sistema envía un correo al empleado con su usuario y contraseña temporal.
El empleado queda habilitado para iniciar sesión en el sistema (ver CU-00.1).

Requerimientos Suplementarios o no Funcionales
La contraseña temporal generada por el sistema solo se muestra una vez en pantalla — una vez que el administrador sale de esa vista no puede recuperarla.
El correo con las credenciales debe enviarse de forma inmediata al confirmar el registro.
El empleado debe cambiar su contraseña en el primer inicio de sesión.

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
