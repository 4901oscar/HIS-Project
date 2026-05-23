Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-01 Ingreso al Sistema — Usuarios (Empleados y Pacientes)
Versión 1.3
Elaborado por Equipo HIS
08/04/2026
Historial Revisiones

Introducción.
Objetivo.
Proteger el acceso al sistema y los expedientes de los pacientes, asegurando que quien ingresa es físicamente el usuario registrado.

Definición Caso de Uso
Actores.
Personal de la clínica (médicos, enfermeras, cajeros, laboratoristas y farmacéuticos).
Usuarios Pacientes
Sistema.
Precondiciones.
El usuario debe estar registrado en el sistema.
La cuenta del usuario debe haber sido activada mediante el correo de verificación.

Normal Básico
El usuario selecciona "Iniciar Sesión" en la barra de navegación o es redirigido desde otra página del sistema.
El sistema muestra la pantalla de inicio de sesión con el logo de MedFlow, el título "Inicio de sesión", la descripción "Ingresa tus credenciales para acceder al sistema", los campos Correo electrónico y Contraseña (con botón para mostrar/ocultar), el botón "Ingresar" y el enlace "Regístrate aquí (ver CU-00.2)".
El usuario ingresa su correo electrónico y contraseña. [RN03]
El usuario selecciona "Ingresar". El sistema muestra indicador de carga mientras valida las credenciales. [FA01 al FA04]
2.3.5. El sistema recibe y almacena el token de sesión JWT y los datos del usuario incluyendo su rol.
Fin del caso de uso.

Flujos Alternos.
FA01 — Credenciales incorrectas.
El sistema detecta que el correo o contraseña no coinciden con ningún registro.
El sistema muestra el mensaje: "Usuario o contraseña incorrectos."
El sistema limpia el campo contraseña.
El flujo retoma en el paso 2.3.3 del Flujo Normal.
FA02 — Cuenta no verificada.
El sistema detecta que la cuenta existe, pero aún no ha sido activada.
El sistema muestra el mensaje: "Tu cuenta aún no ha sido verificada. Revisa tu correo y activa tu cuenta."
El sistema limpia el campo contraseña.
El flujo retoma en el paso 2.3.3 del Flujo Normal.
FA03 — Demasiados intentos fallidos.
El sistema detecta que el usuario ha superado el límite de intentos permitidos. (5 intentos)
El sistema muestra el mensaje: "Demasiados intentos. Espera un momento e intenta de nuevo."
El sistema limpia el campo contraseña.
El flujo retoma en el paso 2.3.3 del Flujo Normal una vez transcurrido el tiempo de espera (1 a 2 mins de espera.).
FA04 — Error de conexión.
El sistema no puede establecer comunicación con el servidor.
El sistema muestra el mensaje: "Error al conectar con el servidor. Intenta más tarde."
El sistema limpia el campo contraseña.
Fin del flujo alterno.




Postcondiciones.
El empleado queda autenticado en el sistema y puede acceder a su área de trabajo según el cargo asignado.
El intento de ingreso queda registrado en el sistema para fines de auditoría y seguridad.
Requerimientos Suplementarios o no Funcionales
El sistema debe bloquear temporalmente el acceso luego de un número determinado de intentos fallidos de inicio de sesión.
Las credenciales de acceso deben transmitirse de forma segura entre el navegador y el servidor.
Firmas Necesarias
En este punto se debe incluir la firma de aquellas personas que deban aprobar la presente especificación.


| Nombre | Fecha | Descripción del Cambio | Versión |
|---|---|---|---|
| Oscar Rivera | 19/02/2026 | Definición inicial. | 1.0 |
| José Avila | 20/02/2026 | Definición inicial. | 1.1 |
| Equipo HIS | 17/03/2026 | Versión cliente con referencias a Reglas de Negocio. | 1.2 |
| Equipo HIS | 08/04/2026 | Modificación de flujo básico, flujos alternos y se hace referencia a RN. | 1.3 |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |


| Persona | Sector | Firma |
|---|---|---|
|  |  |  |
|  |  |  |
