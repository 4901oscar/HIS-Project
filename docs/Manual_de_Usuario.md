# Manual de Usuario — MedFlow HIS

**Versión**: 1.0  
**Fecha de Actualización**: Abril 2026  
**Propósito**: Guía completa para usuarios del Sistema de Información Hospitalaria MedFlow

---

## 1. Descripción del Sistema

### 1.1 Objeto del Sistema

MedFlow HIS (Hospital Information System) es un sistema integral de gestión hospitalaria diseñado para optimizar el flujo de atención al paciente desde su ingreso hasta el alta. El sistema integra módulos de admisión, triaje, consulta médica, laboratorio, farmacia y facturación en una plataforma unificada.

### 1.2 Alcance del Sistema

El sistema cubre los siguientes procesos hospitalarios:

- **Gestión de Pacientes**: Registro, búsqueda y actualización de datos demográficos
- **Gestión de Citas**: Agendamiento, activación y cancelación de citas médicas
- **Triaje Clínico**: Clasificación de urgencia mediante el algoritmo de Triaje Manchester
- **Consulta Médica**: Registro de diagnósticos, recetas y órdenes de laboratorio
- **Laboratorio**: Procesamiento de muestras y carga de resultados
- **Farmacia**: Despacho de medicamentos y control de inventario
- **Facturación**: Generación de facturas y procesamiento de pagos
- **Portal del Paciente**: Acceso a historial clínico, recetas y resultados

### 1.3 Funcionalidad Principal

El sistema permite a diferentes roles de usuarios ejecutar tareas específicas:

- **Administrador**: Gestión completa del sistema, catálogos y personal
- **Personal de Admisión**: Registro de pacientes y creación de citas
- **Personal de Signos Vitales**: Captura de datos vitales y clasificación inicial
- **Médicos**: Realización de triaje, consultas y generación de recetas/órdenes
- **Personal de Laboratorio**: Procesamiento de muestras y carga de resultados
- **Farmacéuticos**: Despacho de medicamentos
- **Cajeros**: Creación de facturas y procesamiento de pagos
- **Pacientes**: Acceso a su historial clínico y resultados

---

## 2. Mapa del Sistema

### 2.1 Modelo Lógico

El sistema está organizado en 6 módulos principales que se comunican a través de un API Gateway:

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React)                         │
│                    Puerto 3000                              │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP + JWT
                         ▼
┌─────────────────────────────────────────────────────────────┐
│              API GATEWAY (Spring Cloud)                     │
│              Puerto 8080 - Validación JWT                   │
└────────────────────────┬────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┬──────────────┐
        ▼                ▼                ▼              ▼
   ┌─────────┐    ┌──────────┐    ┌──────────┐    ┌─────────┐
   │  Auth   │    │ Patient  │    │Clinical  │    │   Lab   │
   │Service  │    │ Service  │    │ Service  │    │ Service │
   │ :8081   │    │  :8082   │    │  :8083   │    │  :8084  │
   └─────────┘    └──────────┘    └──────────┘    └─────────┘
        │              │                │              │
        └──────────────┼────────────────┼──────────────┘
                       ▼
            ┌──────────────────────┐
            │   PostgreSQL (6      │
            │   esquemas)          │
            │   Puerto 5432        │
            └──────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
   ┌─────────┐  ┌──────────┐  ┌──────────┐
   │Pharmacy │  │ Billing  │  │  Redis   │
   │Service  │  │ Service  │  │ (Cache)  │
   │ :8085   │  │  :8086   │  │ :6379    │
   └─────────┘  └──────────┘  └──────────┘
```

### 2.2 Navegación del Sistema

#### 2.2.1 Acceso Inicial

Todos los usuarios acceden a través de la pantalla de login:

1. **URL**: `http://localhost:3000` (o dominio configurado)
2. **Ingreso de Credenciales**: Email y contraseña
3. **Validación**: El sistema valida contra Auth Service
4. **Redirección**: Según el rol, se redirige al dashboard correspondiente

#### 2.2.2 Estructura de Menú por Rol

**ADMIN (Administrador)**
```
├── Dashboard
├── Administración
│   ├── Gestión de Personal
│   ├── Gestión de Doctores
│   ├── Gestión de Clínicas
│   └── Catálogos
│       ├── Medicamentos
│       ├── Exámenes de Laboratorio
│       ├── Servicios y Precios
│       └── Triaje Manchester
└── Reportes
```

**ADMISSION (Admisión)**
```
├── Dashboard
├── Pacientes
│   ├── Registrar Nuevo Paciente
│   ├── Buscar Paciente
│   └── Crear Cita
└── Citas
    ├── Citas Programadas
    └── Activar Cita
```

**VITAL_SIGNS (Signos Vitales)**
```
├── Dashboard
├── Cola de Triaje
│   ├── Pacientes Esperando
│   └── Registrar Signos Vitales
└── Historial
```

**DOCTOR (Médico)**
```
├── Dashboard
├── Consultas
│   ├── Cola de Consultas
│   ├── Realizar Triaje
│   ├── Consulta Médica
│   ├── Generar Receta
│   └── Generar Orden de Laboratorio
└── Historial Clínico
```

**LABORATORY (Laboratorio)**
```
├── Dashboard
├── Órdenes de Laboratorio
│   ├── Órdenes Pendientes
│   ├── Recolectar Muestra
│   ├── Validar Muestra
│   ├── Procesar Muestra
│   └── Cargar Resultados
└── Catálogo de Exámenes
```

**PHARMACY (Farmacia)**
```
├── Dashboard
├── Recetas
│   ├── Recetas Pendientes
│   └── Despachar Medicamentos
├── Inventario
│   ├── Medicamentos
│   └── Actualizar Stock
└── Historial de Despachos
```

**CASHIER (Caja)**
```
├── Dashboard
├── Facturación
│   ├── Crear Factura
│   ├── Procesar Pago
│   └── Generar Recibo
└── Historial de Pagos
```

**PATIENT (Paciente)**
```
├── Mi Perfil
├── Historial Clínico
├── Mis Recetas
├── Resultados de Laboratorio
├── Mis Facturas
└── Agendar Cita
```

---

## 3. Descripción de Subsistemas

### 3.1 Módulo de Autenticación

#### Pantalla de Login
- **Campos**: Email y Contraseña
- **Validaciones**:
  - Email debe tener formato válido
  - Contraseña debe tener mínimo 8 caracteres
  - Máximo 5 intentos fallidos (bloqueo de 1 minuto)
- **Mensajes de Error**:
  - "Credenciales inválidas" — Email o contraseña incorrectos
  - "Cuenta desactivada" — El usuario no está activo
  - "Demasiados intentos fallidos. Intente más tarde" — Bloqueo temporal

#### Cambio de Contraseña
- Disponible en el perfil del usuario
- Requiere contraseña actual
- Nueva contraseña debe cumplir requisitos de seguridad

### 3.2 Módulo de Admisión

#### Registro de Paciente
**Pantalla**: Registrar Nuevo Paciente

**Campos Obligatorios**:
- DPI (13 dígitos)
- Primer Nombre
- Primer Apellido
- Fecha de Nacimiento
- Género (Masculino/Femenino)
- Teléfono (8 dígitos)
- Correo Electrónico
- Dirección

**Campos Opcionales**:
- Segundo Nombre
- Segundo Apellido
- NIT

**Validaciones**:
- DPI: Exactamente 13 dígitos numéricos
- Teléfono: Exactamente 8 dígitos
- Correo: Formato válido de email
- DPI no debe estar duplicado

**Mensajes de Error**:
- "El DPI ya está registrado en el sistema"
- "Por favor complete todos los campos obligatorios"
- "El formato del DPI debe ser 13 dígitos"
- "El teléfono debe tener 8 dígitos"
- "El correo electrónico no es válido"

**Resultado**: Se genera un QR de identidad del paciente que puede imprimirse

#### Búsqueda de Paciente
**Pantalla**: Buscar Paciente

**Opciones de Búsqueda**:
- Por DPI (búsqueda exacta)
- Por Nombre (búsqueda parcial)
- Por Teléfono

**Resultado**: Lista de pacientes coincidentes con opción de seleccionar

#### Creación de Cita
**Pantalla**: Crear Cita

**Pasos**:
1. Seleccionar paciente (búsqueda previa)
2. Seleccionar doctor disponible
3. Seleccionar fecha
4. Seleccionar hora disponible
5. Agregar notas (opcional)
6. Confirmar

**Validaciones**:
- No se pueden crear citas en fechas pasadas
- No se pueden crear citas en horarios ya ocupados
- El doctor debe estar activo

**Mensajes de Error**:
- "No hay slots disponibles para esta fecha"
- "El doctor no está disponible"
- "La fecha no puede ser en el pasado"

**Resultado**: Cita creada en estado SCHEDULED. Se genera un QR de cita

#### Activación de Cita
**Pantalla**: Citas Programadas

**Acción**: Cuando el paciente llega físicamente, se activa la cita

**Resultado**: La cita pasa a estado VITAL_SIGNS y aparece en la cola de signos vitales

### 3.3 Módulo de Signos Vitales

#### Captura de Signos Vitales
**Pantalla**: Registrar Signos Vitales

**Campos a Capturar**:
- Presión Sistólica (mmHg)
- Presión Diastólica (mmHg)
- Frecuencia Cardíaca (latidos/min)
- Frecuencia Respiratoria (respiraciones/min)
- Temperatura (°C)
- Saturación de Oxígeno (%)
- Peso (kg)
- Altura (m)

**Validaciones**:
- Todos los campos son obligatorios
- Los valores deben estar dentro de rangos fisiológicos razonables
- Presión sistólica > presión diastólica

**Cálculos Automáticos**:
- BMI = Peso (kg) / (Altura (m))²

**Mensajes de Error**:
- "Por favor complete todos los campos"
- "La presión sistólica debe ser mayor que la diastólica"
- "Los valores están fuera de rangos normales. Verifique"

**Resultado**: Los signos vitales se guardan y la cita pasa a estado CONSULTATION

### 3.4 Módulo Clínico

#### Triaje Manchester
**Pantalla**: Realizar Triaje

**Pasos**:
1. Seleccionar motivo de consulta (ej: Dolor de cabeza, Fiebre, etc.)
2. Seleccionar discriminadores aplicables (síntomas específicos)
3. El sistema calcula automáticamente el nivel de prioridad

**Niveles de Prioridad**:
- 🔴 **ROJO** (Emergencia): 0 minutos — Atención inmediata
- 🟠 **NARANJA** (Muy urgente): 10 minutos
- 🟡 **AMARILLO** (Urgente): 60 minutos
- 🟢 **VERDE** (Poco urgente): 120 minutos
- 🔵 **AZUL** (No urgente): 240 minutos

**Resultado**: El paciente se asigna a la cola correspondiente

#### Consulta Médica
**Pantalla**: Realizar Consulta

**Campos**:
- Motivo de Consulta (pre-llenado del triaje)
- Síntomas Reportados
- Diagnóstico Principal (código CIE-10)
- Diagnósticos Secundarios (opcional)
- Notas Médicas
- Plan de Tratamiento
- ¿Requiere Laboratorio? (Sí/No)
- ¿Requiere Receta? (Sí/No)
- Fecha de Seguimiento (opcional)

**Validaciones**:
- Diagnóstico principal es obligatorio
- Si requiere laboratorio, debe especificar exámenes
- Si requiere receta, debe especificar medicamentos

**Mensajes de Error**:
- "Por favor ingrese un diagnóstico"
- "Debe especificar al menos un examen de laboratorio"
- "Debe especificar al menos un medicamento"

**Resultado**: La consulta se registra en el historial clínico

#### Generación de Receta
**Pantalla**: Generar Receta (dentro de Consulta Médica)

**Campos por Medicamento**:
- Nombre del Medicamento
- Dosis (ej: 400mg)
- Frecuencia (ej: Cada 8 horas)
- Duración (días)
- Vía de Administración (Oral, Inyectable, etc.)
- Instrucciones Especiales (opcional)
- Cantidad Total

**Validaciones**:
- Todos los campos son obligatorios
- La cantidad debe ser coherente con la frecuencia y duración

**Resultado**: Se genera un código de receta (RX-YYYYMMDD-NNN) que se envía a Farmacia

#### Generación de Orden de Laboratorio
**Pantalla**: Generar Orden de Laboratorio (dentro de Consulta Médica)

**Campos**:
- Seleccionar Exámenes (checkbox múltiple)
- Notas Especiales (opcional)

**Validaciones**:
- Debe seleccionar al menos un examen

**Resultado**: Se genera un código de orden (LAB-YYYYMMDD-NNN) que se envía a Laboratorio

### 3.5 Módulo de Laboratorio

#### Visualización de Órdenes
**Pantalla**: Órdenes de Laboratorio Pendientes

**Información Mostrada**:
- Código de Orden
- Nombre del Paciente
- DPI del Paciente
- Exámenes Solicitados
- Fecha de Orden
- Estado

#### Recolección de Muestra
**Pantalla**: Recolectar Muestra

**Campos**:
- Código de Orden (pre-llenado)
- Técnico que Recolecta (pre-llenado)
- Notas (ej: "Muestra en buen estado")

**Resultado**: La orden pasa a estado LAB_SAMPLE_PENDING

#### Validación de Muestra
**Pantalla**: Validar Muestra

**Campos**:
- ¿Muestra Válida? (Sí/No)
- Notas (si no es válida)

**Mensajes de Error**:
- "Muestra insuficiente — Solicitar nueva toma"
- "Muestra contaminada — Solicitar nueva toma"

#### Procesamiento de Muestra
**Acción**: Marcar como en procesamiento

**Resultado**: La orden pasa a estado LAB_PROCESSING

#### Carga de Resultados
**Pantalla**: Cargar Resultados

**Campos**:
- Código de Orden (pre-llenado)
- Archivo PDF o Imagen del Resultado
- Notas (opcional)

**Validaciones**:
- El archivo debe ser PDF o imagen (JPG, PNG)
- El tamaño máximo es 10MB

**Mensajes de Error**:
- "El archivo debe ser PDF o imagen"
- "El tamaño del archivo excede 10MB"

**Resultado**: La orden pasa a estado LAB_RESULTS_READY y el resultado está disponible en el historial del paciente

### 3.6 Módulo de Farmacia

#### Visualización de Recetas
**Pantalla**: Recetas Pendientes

**Información Mostrada**:
- Código de Receta
- Nombre del Paciente
- Medicamentos
- Dosis y Frecuencia
- Fecha de Emisión
- Estado

#### Despacho de Medicamentos
**Pantalla**: Despachar Medicamentos

**Pasos**:
1. Seleccionar receta
2. Verificar disponibilidad de medicamentos
3. Preparar medicamentos
4. Registrar despacho
5. Entregar al paciente

**Validaciones**:
- Todos los medicamentos deben estar en stock
- La cantidad disponible debe ser suficiente

**Mensajes de Error**:
- "Medicamento no disponible en stock"
- "Stock insuficiente para [medicamento]"

**Resultado**: La receta pasa a estado DISPENSED y el inventario se actualiza

### 3.7 Módulo de Facturación

#### Creación de Factura
**Pantalla**: Crear Factura

**Pasos**:
1. Buscar paciente
2. Seleccionar servicios a cobrar:
   - Consulta Médica
   - Exámenes de Laboratorio
   - Medicamentos
3. El sistema calcula el total
4. Confirmar

**Campos**:
- Nombre del Cliente
- NIT (o "C/F" para consumidor final)
- Detalle de Cargos
- Subtotal (calculado)
- Descuento (opcional)
- Total (calculado)

**Validaciones**:
- Debe seleccionar al menos un servicio
- NIT debe tener formato válido

**Resultado**: Se genera un número de factura (FAC-YYYYMMDD-NNN)

#### Procesamiento de Pago
**Pantalla**: Procesar Pago

**Campos**:
- Monto a Pagar
- Método de Pago (Efectivo, Tarjeta, Transferencia)
- Referencia de Pago (si aplica)

**Validaciones**:
- El monto debe ser mayor a 0
- El monto no puede exceder el total de la factura

**Cálculos**:
- Si es efectivo: Calcular cambio automáticamente

**Mensajes de Error**:
- "El monto debe ser mayor a 0"
- "El monto no puede exceder el total"

**Resultado**: La factura pasa a estado PAID y se genera recibo

### 3.8 Portal del Paciente

#### Acceso al Portal
**URL**: `http://localhost:3000/patient`

**Autenticación**:
- Email y contraseña temporal (recibida al registrarse)
- Cambio obligatorio de contraseña en primer acceso

#### Visualización de Historial Clínico
**Pantalla**: Mi Historial

**Información Mostrada**:
- Datos Personales
- Consultas Médicas (fecha, diagnóstico, doctor)
- Signos Vitales (histórico)
- Recetas (código, medicamentos, estado)
- Resultados de Laboratorio (con opción de descargar PDF)
- Facturas (con opción de descargar)

#### Agendamiento de Cita
**Pantalla**: Agendar Cita

**Pasos**:
1. Seleccionar doctor
2. Seleccionar fecha
3. Seleccionar hora disponible
4. Confirmar

**Validaciones**:
- No se pueden agendar citas en fechas pasadas
- No se pueden agendar citas en horarios ocupados

**Resultado**: Cita creada en estado SCHEDULED

---

## 4. Preguntas Frecuentes (FAQ)

### 4.1 Autenticación y Acceso

**P: ¿Qué debo hacer si olvido mi contraseña?**  
R: Haga clic en "¿Olvidó su contraseña?" en la pantalla de login. Ingrese su email y recibirá un link para resetear su contraseña.

**P: ¿Cuánto tiempo dura mi sesión?**  
R: Su sesión dura 24 horas. Después de ese tiempo, deberá volver a iniciar sesión.

**P: ¿Qué significa "Cuenta desactivada"?**  
R: Su cuenta ha sido desactivada por el administrador. Contacte al administrador del sistema para reactivarla.

**P: ¿Por qué me bloquea después de 5 intentos fallidos?**  
R: Por seguridad, el sistema bloquea temporalmente la cuenta por 1 minuto después de 5 intentos fallidos de login.

### 4.2 Gestión de Pacientes

**P: ¿Cómo registro un nuevo paciente?**  
R: Acceda al módulo de Admisión, seleccione "Registrar Nuevo Paciente" e ingrese los datos demográficos. El sistema generará un QR de identidad.

**P: ¿Qué pasa si el DPI ya está registrado?**  
R: El sistema mostrará un mensaje de error. Busque al paciente existente en lugar de crear uno nuevo.

**P: ¿Puedo editar los datos de un paciente después de registrarlo?**  
R: Sí, busque al paciente y seleccione "Editar". Puede actualizar la mayoría de campos excepto el DPI.

**P: ¿Cómo accede un paciente al portal?**  
R: Cuando se registra en Admisión, el sistema envía una contraseña temporal al email del paciente. El paciente accede con su email y esa contraseña, y debe cambiarla en el primer acceso.

### 4.3 Gestión de Citas

**P: ¿Cómo creo una cita?**  
R: En Admisión, seleccione "Crear Cita", busque al paciente, seleccione el doctor, fecha y hora disponible.

**P: ¿Qué significa "Activar Cita"?**  
R: Cuando el paciente llega físicamente al hospital, Admisión activa la cita. Esto cambia el estado a VITAL_SIGNS y la cita aparece en la cola de signos vitales.

**P: ¿Puedo cancelar una cita?**  
R: Sí, en el módulo de Admisión, busque la cita y seleccione "Cancelar".

**P: ¿Qué pasa si el paciente no llega a su cita?**  
R: El sistema marca automáticamente la cita como MISSED después de 30 minutos de la hora programada.

### 4.4 Triaje y Consulta Médica

**P: ¿Qué es el Triaje Manchester?**  
R: Es un sistema de clasificación de urgencia que asigna un nivel de prioridad (Rojo, Naranja, Amarillo, Verde, Azul) según los síntomas del paciente.

**P: ¿Quién realiza el triaje?**  
R: El médico realiza el triaje después de revisar los signos vitales capturados por el personal de enfermería.

**P: ¿Cómo genero una receta?**  
R: Durante la consulta médica, seleccione "Generar Receta", agregue los medicamentos con dosis y frecuencia, y confirme.

**P: ¿Cómo genero una orden de laboratorio?**  
R: Durante la consulta médica, seleccione "Generar Orden de Laboratorio", seleccione los exámenes y confirme.

### 4.5 Laboratorio

**P: ¿Cuál es el flujo de procesamiento de muestras?**  
R: Recolección → Validación → Procesamiento → Carga de Resultados. Cada paso debe completarse en orden.

**P: ¿Qué formatos acepta para los resultados?**  
R: PDF, JPG o PNG. El tamaño máximo es 10MB.

**P: ¿Cómo notifica el sistema al médico que los resultados están listos?**  
R: El médico puede ver los resultados en el historial clínico del paciente. El sistema no envía notificaciones automáticas.

### 4.6 Farmacia

**P: ¿Qué pasa si un medicamento no está en stock?**  
R: El sistema muestra un mensaje de error. Debe contactar al proveedor o esperar a que se reponga el stock.

**P: ¿Cómo actualizo el inventario de medicamentos?**  
R: En el módulo de Farmacia, seleccione "Inventario" y actualice el stock de cada medicamento.

**P: ¿Puedo despachar medicamentos parcialmente?**  
R: No, debe despachar la cantidad completa especificada en la receta.

### 4.7 Facturación

**P: ¿Cómo creo una factura?**  
R: En Caja, seleccione "Crear Factura", busque al paciente, seleccione los servicios a cobrar y confirme.

**P: ¿Qué métodos de pago acepta el sistema?**  
R: Efectivo, Tarjeta de Crédito/Débito y Transferencia Bancaria.

**P: ¿Puedo aplicar descuentos?**  
R: Sí, durante la creación de la factura puede ingresar un descuento (monto o porcentaje).

**P: ¿Cómo calcula el cambio?**  
R: Si el pago es en efectivo, el sistema calcula automáticamente: Cambio = Monto Pagado - Total.

### 4.8 Historial Clínico

**P: ¿Quién puede ver el historial clínico de un paciente?**  
R: El médico, el personal de laboratorio, farmacia y caja pueden ver el historial completo. El paciente solo ve su propio historial.

**P: ¿Cómo accedo al historial de un paciente?**  
R: Busque al paciente y seleccione "Ver Historial Clínico".

**P: ¿Puedo filtrar el historial por fecha?**  
R: Sí, use los filtros de fecha disponibles en la pantalla de historial.

### 4.9 Errores Comunes

**P: ¿Qué significa "Error 401 - No autorizado"?**  
R: Su token JWT ha expirado o es inválido. Cierre sesión e inicie sesión nuevamente.

**P: ¿Qué significa "Error 403 - Acceso denegado"?**  
R: Su rol no tiene permisos para acceder a esta funcionalidad. Contacte al administrador.

**P: ¿Qué significa "Error 404 - No encontrado"?**  
R: El recurso que busca no existe. Verifique que el ID o código sea correcto.

**P: ¿Qué significa "Error 429 - Demasiadas solicitudes"?**  
R: Ha excedido el límite de 100 solicitudes por minuto. Espere un momento e intente nuevamente.

### 4.10 Soporte Técnico

**P: ¿A quién contacto si tengo problemas?**  
R: Contacte al administrador del sistema o al equipo de soporte técnico de MedFlow.

**P: ¿Dónde puedo reportar un error?**  
R: Reporte los errores al administrador del sistema con detalles de qué estaba haciendo cuando ocurrió el error.

**P: ¿Hay un manual técnico disponible?**  
R: Sí, consulte el archivo "Manual_Tecnico.md" para información técnica sobre la arquitectura y configuración del sistema.

---

## Apéndice: Validaciones de Datos

### Formato de DPI
- Exactamente 13 dígitos numéricos
- Ejemplo: `1234567890101`

### Formato de NIT
- "C/F" (Consumidor Final) o
- 1 a 8 dígitos numéricos
- Ejemplo: `C/F` o `12345678`

### Formato de Teléfono
- Exactamente 8 dígitos
- Ejemplo: `55551234`

### Formato de Correo
- Debe contener @ y un dominio válido
- Ejemplo: `usuario@example.com`

### Formato de Contraseña
- Mínimo 8 caracteres
- Debe contener mayúsculas, minúsculas y números
- Ejemplo: `MiPassword123`

### Códigos Generados Automáticamente
- **Receta**: `RX-YYYYMMDD-NNN` (ej: `RX-20260515-001`)
- **Orden de Laboratorio**: `LAB-YYYYMMDD-NNN` (ej: `LAB-20260515-001`)
- **Factura**: `FAC-YYYYMMDD-NNN` (ej: `FAC-20260515-001`)

---

**Versión**: 1.0  
**Última Actualización**: Abril 2026  
**Mantenido por**: MedFlow Team
