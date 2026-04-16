# 📋 Casos de Uso - MedFlow HIS

**Versión**: 1.0  
**Última actualización**: Abril 14, 2026  
**Propósito**: Referencia rápida de casos de uso del sistema

---

## 📚 Índice de Casos de Uso

### Módulo de Autenticación y Acceso
- [CU-00.1: Inicio de Sesión (Empleado)](#cu-001-inicio-de-sesión-empleado)
- [CU-00.2: Registro de Paciente (Usuario)](#cu-002-registro-de-paciente-usuario)

### Módulo de Pacientes
- [CU-01: Registro de Paciente (Admisión)](#cu-01-registro-de-paciente-admisión)
- [CU-12: Historial Clínico](#cu-12-historial-clínico)

### Módulo Clínico
- [CU-03A: Triaje Manchester](#cu-03a-triaje-manchester)
- [CU-03B: Consulta Médica](#cu-03b-consulta-médica)

### Módulo de Laboratorio
- [CU-04: Laboratorios](#cu-04-laboratorios)
- [CU-08: Catálogo de Exámenes](#cu-08-catálogo-de-exámenes)

### Módulo de Farmacia
- [CU-06: Despacho de Farmacia](#cu-06-despacho-de-farmacia)
- [CU-09: Catálogo de Medicamentos](#cu-09-catálogo-de-medicamentos)

### Módulo de Facturación
- [CU-05: Cobros y Facturación](#cu-05-cobros-y-facturación)
- [CU-10: Servicios y Precios](#cu-10-servicios-y-precios)

### Módulo de Administración
- [CU-02: Administración de Personal](#cu-02-administración-de-personal)
- [CU-07: Asistencia de Personal](#cu-07-asistencia-de-personal)
- [CU-11: Reportes de Asistencia](#cu-11-reportes-de-asistencia)

### Catálogos y Configuración
- [CU-13: Catálogo de Motivos y Discriminadores](#cu-13-catálogo-de-motivos-y-discriminadores)

### Reglas de Negocio
- [RN: Reglas de Negocio del Sistema](#rn-reglas-de-negocio-del-sistema)

---

## 🔐 CU-00.1: Inicio de Sesión (Empleado)

**Archivo**: `00_1__CU_Inicio_Sesion_PLANTILLA.docx`

### Descripción
Permite a los empleados del hospital (staff) autenticarse en el sistema.

### Actores
- Empleado (ADMIN, ADMISSION, VITAL_SIGNS, DOCTOR, LABORATORY, PHARMACY, CASHIER)

### Precondiciones
- El empleado debe tener una cuenta creada por el administrador
- El sistema debe estar disponible

### Flujo Principal
1. El empleado accede al sistema
2. Ingresa su email y contraseña
3. El sistema valida las credenciales
4. El sistema genera un JWT con el rol del empleado
5. El empleado es redirigido a su dashboard según su rol

### Flujos Alternativos
- **FA1**: Credenciales inválidas → Mostrar error
- **FA2**: Cuenta desactivada → Mostrar mensaje de cuenta inactiva
- **FA3**: Más de 5 intentos fallidos → Bloquear temporalmente (1 minuto)

### Postcondiciones
- El empleado tiene una sesión activa con JWT válido
- El sistema registra el inicio de sesión

### Servicios Involucrados
- ✅ **Auth Service** (Puerto 8081)

### Estado de Implementación
- ✅ **IMPLEMENTADO** en Auth Service

---

## 👤 CU-00.2: Registro de Paciente (Usuario)

**Archivo**: `00_2__CU_Registro_Paciente_USUARIO.docx`

### Descripción
Permite a un paciente registrarse en el portal web para acceder a su historial clínico.

### Actores
- Paciente (Usuario externo)

### Precondiciones
- El paciente debe haber sido registrado previamente en Admisión
- El paciente debe tener un DPI válido

### Flujo Principal
1. El paciente accede al portal web
2. Selecciona "Registrarse como paciente"
3. Ingresa su DPI y correo electrónico
4. El sistema valida que el DPI existe en la base de datos
5. El sistema envía una contraseña temporal al correo
6. El paciente inicia sesión con la contraseña temporal
7. El sistema solicita cambio de contraseña obligatorio

### Flujos Alternativos
- **FA1**: DPI no encontrado → Mostrar mensaje de contactar admisión
- **FA2**: Correo no coincide → Mostrar error de validación
- **FA3**: Correo ya registrado → Ofrecer recuperación de contraseña

### Postcondiciones
- El paciente tiene acceso al portal con rol PATIENT
- El paciente puede ver su historial clínico

### Servicios Involucrados
- ⏳ **Patient Service** (Puerto 8082) - Para validar DPI y correo
- ✅ **Auth Service** (Puerto 8081) - Para crear credenciales

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere Patient Service

---

## 🏥 CU-01: Registro de Paciente (Admisión)

**Archivo**: `01__CU_Registro_Paciente_PLANTILLA.docx`

### Descripción
Permite al personal de admisión registrar un nuevo paciente en el sistema.

### Actores
- Personal de Admisión (Rol: ADMISSION)

### Precondiciones
- El usuario debe estar autenticado con rol ADMISSION
- El paciente debe proporcionar su DPI

### Flujo Principal
1. El personal de admisión accede al módulo de pacientes
2. Selecciona "Registrar nuevo paciente"
3. Ingresa los datos demográficos:
   - **DPI** (13 dígitos, obligatorio)
   - **NIT** ("C/F" o 1-8 dígitos, obligatorio)
   - **Primer nombre** (obligatorio)
   - **Segundo nombre** (opcional)
   - **Primer apellido** (obligatorio)
   - **Segundo apellido** (opcional)
   - **Fecha de nacimiento** (obligatorio)
   - **Género** (obligatorio)
   - **Teléfono** (8 dígitos, obligatorio)
   - **Correo electrónico** (obligatorio)
   - **Dirección completa** (obligatorio)
4. El sistema valida los datos
5. El sistema genera un código QR de identidad del paciente
6. El sistema guarda el paciente
7. El sistema muestra el QR generado para imprimir

### Flujos Alternativos
- **FA1**: DPI ya existe → Mostrar mensaje de paciente duplicado
- **FA2**: Datos inválidos → Mostrar errores de validación específicos
- **FA3**: Error al generar QR → Permitir regenerar después

### Postcondiciones
- El paciente queda registrado en el sistema
- El paciente tiene un QR de identidad único
- El paciente puede ser buscado por DPI, nombre o teléfono

### Validaciones Específicas
- **DPI**: Exactamente 13 dígitos numéricos
- **NIT**: "C/F" o 1-8 dígitos numéricos
- **Teléfono**: Exactamente 8 dígitos (formato Guatemala)
- **Correo**: Formato válido de email

### Servicios Involucrados
- ⏳ **Patient Service** (Puerto 8082)

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere Patient Service completo

---

## 🩺 CU-03A: Triaje Manchester

**Archivo**: `03A__CU_Triaje_CLIENTE_v1.2.docx`

### Descripción
Permite al médico realizar el triaje Manchester para clasificar la urgencia del paciente.

### Actores
- Doctor (Rol: DOCTOR)

### Precondiciones
- El paciente debe estar registrado
- El paciente debe tener signos vitales capturados
- El médico debe estar autenticado

### Flujo Principal
1. El médico selecciona al paciente de la lista de espera
2. Revisa los signos vitales capturados
3. Evalúa el motivo de consulta
4. Selecciona discriminadores del catálogo
5. El sistema calcula el nivel de prioridad según algoritmo Manchester:
   - **Rojo (Inmediato)**: 0 minutos
   - **Naranja (Muy urgente)**: 10 minutos
   - **Amarillo (Urgente)**: 60 minutos
   - **Verde (Poco urgente)**: 120 minutos
   - **Azul (No urgente)**: 240 minutos
6. El sistema asigna el paciente a la cola correspondiente

### Flujos Alternativos
- **FA1**: Paciente sin signos vitales → Redirigir a captura de signos vitales
- **FA2**: Emergencia crítica → Activar alerta de código rojo

### Postcondiciones
- El paciente tiene un nivel de prioridad asignado
- El paciente está en la cola de atención correspondiente

### Servicios Involucrados
- ⏳ **Clinical Service** (Puerto 8083) - Arquitectura Hexagonal

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere Clinical Service

---

## 💊 CU-03B: Consulta Médica

**Archivo**: `03B__CU_Consulta_Medica_CLIENTE_v1.0.docx`

### Descripción
Permite al médico realizar la consulta médica y generar recetas y órdenes de laboratorio.

### Actores
- Doctor (Rol: DOCTOR)

### Precondiciones
- El paciente debe haber pasado por triaje
- El médico debe estar autenticado

### Flujo Principal
1. El médico selecciona al paciente según prioridad de triaje
2. Revisa el historial clínico del paciente
3. Realiza la consulta médica
4. Registra diagnóstico y notas médicas
5. Genera receta médica (si aplica)
6. Genera órdenes de laboratorio (si aplica)
7. El sistema guarda la consulta en el historial

### Flujos Alternativos
- **FA1**: Paciente requiere hospitalización → Generar orden de ingreso
- **FA2**: Paciente requiere referencia → Generar orden de referencia

### Postcondiciones
- La consulta queda registrada en el historial clínico
- Las recetas están disponibles para farmacia
- Las órdenes de laboratorio están disponibles para el laboratorio

### Servicios Involucrados
- ⏳ **Clinical Service** (Puerto 8083)
- ⏳ **Lab Service** (Puerto 8084) - Para órdenes
- ⏳ **Pharmacy Service** (Puerto 8085) - Para recetas

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere múltiples servicios

---

## 🔬 CU-04: Laboratorios

**Archivo**: `04__CU_Laboratorios_CLIENTE_v1.2.docx`

### Descripción
Permite al personal de laboratorio gestionar muestras y subir resultados.

### Actores
- Técnico de Laboratorio (Rol: LABORATORY)

### Precondiciones
- Debe existir una orden de laboratorio generada por un médico
- El técnico debe estar autenticado

### Flujo Principal
1. El técnico visualiza las órdenes de laboratorio pendientes
2. Selecciona una orden
3. Registra la toma de muestra
4. Procesa la muestra
5. Sube el resultado en formato PDF
6. El sistema notifica al médico que el resultado está disponible

### Flujos Alternativos
- **FA1**: Muestra insuficiente → Solicitar nueva toma
- **FA2**: Resultado anormal → Marcar como urgente

### Postcondiciones
- El resultado está disponible en el historial del paciente
- El médico puede ver el resultado
- El paciente puede ver el resultado en su portal

### Servicios Involucrados
- ⏳ **Lab Service** (Puerto 8084)

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere Lab Service

---

## 💰 CU-05: Cobros y Facturación

**Archivo**: `05__CU_Cobros_Facturacion_CLIENTE_v1.2.docx`

### Descripción
Permite al cajero procesar pagos y generar facturas internas.

### Actores
- Cajero (Rol: CASHIER)

### Precondiciones
- Debe existir un servicio prestado al paciente
- El cajero debe estar autenticado

### Flujo Principal
1. El cajero busca al paciente
2. Visualiza los servicios pendientes de pago
3. Selecciona los servicios a cobrar
4. El sistema calcula el total
5. Registra el método de pago
6. Genera la factura interna
7. Imprime el recibo

### Flujos Alternativos
- **FA1**: Pago parcial → Registrar abono
- **FA2**: Descuento aplicable → Aplicar descuento autorizado

### Postcondiciones
- El pago queda registrado
- La factura está disponible para el paciente
- Los servicios quedan marcados como pagados

### Servicios Involucrados
- ⏳ **Billing Service** (Puerto 8086)

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere Billing Service

---

## 💊 CU-06: Despacho de Farmacia

**Archivo**: `06__CU_Despacho_Farmacia_CLIENTE_v1.2.docx`

### Descripción
Permite al farmacéutico despachar medicamentos recetados.

### Actores
- Farmacéutico (Rol: PHARMACY)

### Precondiciones
- Debe existir una receta médica válida
- Los medicamentos deben estar en inventario
- El farmacéutico debe estar autenticado

### Flujo Principal
1. El farmacéutico busca la receta por código o paciente
2. Verifica la disponibilidad de medicamentos
3. Prepara los medicamentos
4. Registra el despacho
5. Actualiza el inventario
6. Entrega los medicamentos al paciente

### Flujos Alternativos
- **FA1**: Medicamento no disponible → Notificar al médico
- **FA2**: Medicamento controlado → Solicitar identificación adicional

### Postcondiciones
- Los medicamentos quedan despachados
- El inventario se actualiza
- La receta queda marcada como despachada

### Servicios Involucrados
- ⏳ **Pharmacy Service** (Puerto 8085)

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere Pharmacy Service

---

## 👥 CU-02: Administración de Personal

**Archivo**: `02__CU_Administracion_Personal_PLANTILLA.docx`

### Descripción
Permite al administrador gestionar cuentas de empleados.

### Actores
- Administrador (Rol: ADMIN)

### Precondiciones
- El usuario debe estar autenticado con rol ADMIN

### Flujo Principal
1. El administrador accede al módulo de personal
2. Crea una nueva cuenta de empleado
3. Asigna un rol (ADMISSION, VITAL_SIGNS, DOCTOR, LABORATORY, PHARMACY, CASHIER)
4. Genera credenciales temporales
5. El sistema envía las credenciales al correo del empleado

### Flujos Alternativos
- **FA1**: Correo ya existe → Mostrar error
- **FA2**: Desactivar empleado → Cambiar estado a inactivo

### Postcondiciones
- El empleado tiene una cuenta activa
- El empleado puede iniciar sesión

### Servicios Involucrados
- ✅ **Auth Service** (Puerto 8081)

### Estado de Implementación
- ✅ **IMPLEMENTADO** en Auth Service

---

## 📊 CU-12: Historial Clínico

**Archivo**: `12__CU_Historial_Clinico_CLIENTE_v1.0.docx`

### Descripción
Permite visualizar el historial clínico completo de un paciente.

### Actores
- Doctor (Rol: DOCTOR)
- Paciente (Rol: PATIENT) - Solo su propio historial

### Precondiciones
- El paciente debe estar registrado
- El usuario debe estar autenticado

### Flujo Principal
1. El usuario busca al paciente (o accede a su propio historial)
2. El sistema muestra:
   - Datos demográficos
   - Consultas médicas
   - Diagnósticos
   - Recetas
   - Resultados de laboratorio
   - Signos vitales históricos
3. El usuario puede filtrar por fecha o tipo de registro

### Flujos Alternativos
- **FA1**: Paciente sin historial → Mostrar mensaje informativo

### Postcondiciones
- El historial es visualizado
- El acceso queda registrado en auditoría

### Servicios Involucrados
- ⏳ **Patient Service** (Puerto 8082) - Datos demográficos
- ⏳ **Clinical Service** (Puerto 8083) - Consultas y diagnósticos
- ⏳ **Lab Service** (Puerto 8084) - Resultados de laboratorio
- ⏳ **Pharmacy Service** (Puerto 8085) - Recetas

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere integración de múltiples servicios

---

## 📋 CU-13: Catálogo de Motivos y Discriminadores

**Archivo**: `13__CU_Catalogo_Motivos_Discriminadores_v1.0.docx`

### Descripción
Gestión del catálogo de motivos de consulta y discriminadores para el Triaje Manchester.

### Actores
- Administrador (Rol: ADMIN)

### Precondiciones
- El usuario debe estar autenticado con rol ADMIN

### Flujo Principal
1. El administrador accede al catálogo
2. Puede agregar, editar o desactivar motivos
3. Puede agregar, editar o desactivar discriminadores
4. Asocia discriminadores con niveles de prioridad

### Postcondiciones
- El catálogo está actualizado
- Los cambios están disponibles para el triaje

### Servicios Involucrados
- ⏳ **Clinical Service** (Puerto 8083)

### Estado de Implementación
- ⏳ **PENDIENTE** - Requiere Clinical Service

---

## 📜 RN: Reglas de Negocio del Sistema

**Archivo**: `15__Reglas_de_Negocio_CLIENTE_v1.2.docx`

### Reglas Generales

#### RN-01: Validación de DPI
- El DPI debe ser exactamente 13 dígitos numéricos
- No se permiten letras ni caracteres especiales
- El DPI debe ser único en el sistema

#### RN-02: Validación de NIT
- El NIT puede ser "C/F" (consumidor final)
- O puede ser de 1 a 8 dígitos numéricos
- No se permiten otros formatos

#### RN-03: Validación de Teléfono
- El teléfono debe ser exactamente 8 dígitos
- Formato de Guatemala
- No se permiten guiones ni espacios

#### RN-04: Validación de Correo
- Debe tener formato válido de email
- Debe contener @ y dominio válido
- Es obligatorio para acceso al portal

#### RN-05: Mensajes de Error
- Los mensajes deben ser en español
- Deben ser específicos sobre el campo con error
- Ejemplo: "Por favor complete todos los campos obligatorios"

#### RN-06: Tiempos de Respuesta
- Las páginas deben cargar en ≤2 segundos
- Las búsquedas deben responder en ≤1 segundo

#### RN-07: Seguridad de Contraseñas
- Mínimo 8 caracteres
- Debe contener mayúsculas, minúsculas y números
- Cambio obligatorio en primer inicio de sesión

#### RN-08: Rate Limiting
- Máximo 5 intentos de login fallidos
- Bloqueo temporal de 1 minuto después de 5 intentos

#### RN-09: Expiración de JWT
- Los tokens JWT expiran en 24 horas
- Se puede refrescar el token antes de expirar

#### RN-10: Roles y Permisos
- Cada usuario tiene un único rol principal
- Los permisos se heredan del rol
- Solo ADMIN puede crear usuarios

---

## 🔄 Flujos de Integración entre Servicios

### Flujo 1: Registro Completo de Paciente
```
1. ADMISSION → Patient Service: Registrar paciente
2. Patient Service → DB: Guardar datos demográficos
3. Patient Service → QR Service: Generar QR de identidad
4. Patient Service → ADMISSION: Retornar paciente con QR
```

### Flujo 2: Acceso al Portal del Paciente
```
1. PATIENT → Patient Service: Solicitar acceso con DPI
2. Patient Service → DB: Validar DPI y correo
3. Patient Service → Auth Service: Crear credenciales
4. Auth Service → Email Service: Enviar contraseña temporal
5. PATIENT → Auth Service: Login con contraseña temporal
6. Auth Service → PATIENT: JWT con rol PATIENT
```

### Flujo 3: Consulta Médica Completa
```
1. DOCTOR → Clinical Service: Iniciar consulta
2. Clinical Service → Patient Service: Obtener datos del paciente
3. DOCTOR → Clinical Service: Registrar diagnóstico
4. DOCTOR → Clinical Service: Generar receta
5. Clinical Service → Pharmacy Service: Notificar receta
6. DOCTOR → Clinical Service: Generar orden de laboratorio
7. Clinical Service → Lab Service: Notificar orden
```

### Flujo 4: Visualización de Historial
```
1. USER → Patient Service: Solicitar historial
2. Patient Service → Clinical Service: Obtener consultas
3. Patient Service → Lab Service: Obtener resultados
4. Patient Service → Pharmacy Service: Obtener recetas
5. Patient Service → USER: Retornar historial completo
```

---

## 📝 Notas para Implementación

### Patient Service - Prioridades
1. ✅ Validación de datos (DPI, NIT, Email, Phone) - **IMPLEMENTADO**
2. ⏳ CRUD de pacientes
3. ⏳ Generación de QR de identidad
4. ⏳ Búsqueda de pacientes (por DPI, nombre, teléfono)
5. ⏳ Generación de acceso al portal
6. ⏳ Integración con Auth Service

### Dependencias Críticas
- **Patient Service** depende de **Auth Service** (ya implementado)
- **Clinical Service** depende de **Patient Service**
- **Lab Service** depende de **Clinical Service**
- **Pharmacy Service** depende de **Clinical Service**
- **Billing Service** depende de todos los servicios

---

## 🎯 Próximos Pasos

1. **Completar Patient Service**
   - Crear design.md
   - Crear tasks.md
   - Implementar CRUD de pacientes
   - Implementar generación de QR
   - Implementar búsqueda

2. **Validar con Casos de Uso**
   - Revisar cada .docx en detalle
   - Asegurar que todos los flujos estén cubiertos
   - Actualizar este documento con detalles faltantes

3. **Continuar con Clinical Service**
   - Implementar Triaje Manchester
   - Implementar gestión de citas
   - Implementar consultas médicas

---

**Última actualización**: Abril 14, 2026  
**Mantenido por**: MedFlow Team  
**Fuente**: Documentación/casos-de-uso/*.docx
