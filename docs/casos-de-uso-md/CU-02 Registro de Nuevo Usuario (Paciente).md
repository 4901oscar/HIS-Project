Caso de Uso
Sistema de Información Hospitalaria (HIS)
CU-02 Registro de Nuevo Usuario (Paciente)
Versión 1.0
Elaborado por Equipo HIS
11/04/2026
Historial Revisiones

1. Introducción
1.1 Objetivo.
Permitir que un usuario no registrado (paciente) cree una cuenta en el portal web de la clínica para poder agendar citas y acceder a su información.

2. Definición Caso de Uso
2.1 Actores.
• Usuario no registrado (Paciente).
• Sistema.

2.2 Precondiciones.
El usuario debe tener acceso al portal web de la clínica.
El usuario no debe tener una cuenta previa registrada en el sistema.

Flujo Normal Básico
El usuario selecciona "Registrarse" en la barra de navegación o el enlace "Regístrate aquí" en la pantalla de inicio de sesión.
El sistema muestra el formulario "Crear cuenta de paciente" con los campos requeridos y opcionales, indicando con asterisco rojo los obligatorios.
El usuario completa los campos obligatorios:
DPI (13 dígitos)
NIT
Primer nombre y apellido
Correo electrónico
Teléfono (8 dígitos)
Fecha de nacimiento
Género (Masculino / Femenino)
Contraseña (mínimo 8 caracteres)
Confirmar contraseña
El usuario completa los campos opcionales si lo desea: Segundo nombre, Segundo apellido, Departamento, Municipio, Zona y Dirección.
El usuario selecciona "Crear cuenta"
El sistema valida el formulario: campos requeridos, formatos y que las contraseñas coincidan. Si hay errores los muestra en línea junto a cada campo sin enviar los datos. [RN02][RN05]
El sistema envía los datos al servidor, el cual verifica que el DPI y el correo no estén registrados previamente y crea la cuenta. [FA01][FA02][FA03]
El sistema crea la cuenta y muestra la pantalla de confirmación con el mensaje: ¡Cuenta creada! Hemos enviado un correo de verificación a {correo ingresado}. Revisa tu bandeja y activa tu cuenta antes de iniciar sesión." y el botón "Iniciar sesión".
Fin del caso de uso.

2.4 Flujos Alternos.
FA01 — Correo electrónico ya registrado.
El servidor detecta que el correo electrónico ya existe en el sistema.
El sistema muestra el mensaje: "El correo electrónico ya está registrado: {correo ingresado}."
El formulario permanece visible con los datos ingresados.
El flujo retoma en el paso 2.3.3 del Flujo Normal.
FA02 — DPI ya registrado.
El servidor detecta que el DPI ya existe en el sistema.
El sistema muestra el mensaje: "El usuario ya está registrado: {DPI ingresado}."
El formulario permanece visible con los datos ingresados.
El flujo retoma en el paso 2.3.3 del Flujo Normal.
FA03 — Error de conexión.
El sistema no puede establecer comunicación con el servidor.
El sistema muestra el mensaje: "Error al conectar con el servidor. Intenta más tarde."
Fin del flujo alterno.


2.5 Postcondiciones.
Se crea un nuevo usuario en el sistema con estado "Pendiente de Activación".
Se envía un correo electrónico de confirmación al usuario con el enlace de activación.
Al confirmar el correo electrónico, el usuario queda en estado "Activo" y puede iniciar sesión en el portal web (ver Caso de Uso CU-01).

3. Requerimientos Suplementarios o no Funcionales
El enlace de activación enviado por correo electrónico debe tener una vigencia máxima de 24 horas. Si el usuario no activa su cuenta dentro de ese plazo, deberá solicitar un nuevo enlace.
4. Firmas Necesarias
En este punto se debe incluir la firma de aquellas personas que deban aprobar la presente especificación.



| Nombre | Fecha | Descripción del Cambio | Versión |
|---|---|---|---|
| Equipo HIS | 11/04/2026 | Definición inicial del registro de paciente en portal web. | 1.0 |
|  |  |  |  |
|  |  |  |  |
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
