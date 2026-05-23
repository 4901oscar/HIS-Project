# Plan de Pruebas de Software — MedFlow HIS

**Versión**: 1.0  
**Fecha de Actualización**: Abril 2026  
**Propósito**: Plantilla de pruebas para funcionalidades principales del sistema

---

## 1. Introducción

Este documento define el plan de pruebas para MedFlow HIS. Incluye plantillas de pruebas para cada caso de uso principal, con datos de entrada, reglas de negocio, flujos básicos, alternos y de excepción.

### 1.1 Alcance de Pruebas

- Pruebas funcionales de casos de uso principales
- Pruebas de validación de datos
- Pruebas de flujos de excepción
- Pruebas de integración entre servicios

### 1.2 Niveles de Prueba

- **Unitarias**: Funciones individuales
- **Integración**: Comunicación entre servicios
- **Sistema**: Flujos completos de usuario
- **Aceptación**: Validación contra requisitos

---

## 2. Plantilla General de Prueba

### 2.1 Estructura

```
Caso de Uso: [Nombre del CU]
ID: [CU-XX]
Objetivo: [Qué se prueba]

Precondiciones:
- [Condición 1]
- [Condición 2]

Datos de Entrada:
- [Campo 1]: [Valor válido]
- [Campo 2]: [Valor válido]

Reglas de Negocio:
- [RN-01]: [Descripción]
- [RN-02]: [Descripción]

Flujo Básico:
1. [Paso 1]
2. [Paso 2]
3. [Resultado esperado]

Flujos Alternos:
FA1: [Condición alternativa]
  1. [Paso 1]
  2. [Resultado esperado]

Flujos de Excepción:
FE1: [Condición de error]
  1. [Paso 1]
  2. [Mensaje de error esperado]

Postcondiciones:
- [Estado final 1]
- [Estado final 2]
```

---

## 3. Pruebas por Caso de Uso

### 3.1 CU-01: Registro de Paciente (Admisión)

#### Prueba 1.1: Registro exitoso de paciente nuevo

**Objetivo**: Verificar que un paciente nuevo se registra correctamente

**Precondiciones**:
- Usuario autenticado con rol ADMISSION
- El DPI no existe en el sistema

**Datos de Entrada**:
- DPI: `1234567890101`
- Primer Nombre: `Juan`
- Primer Apellido: `Pérez`
- Fecha de Nacimiento: `1990-05-15`
- Género: `Masculino`
- Teléfono: `55551234`
- Correo: `juan@example.com`
- Dirección: `5a Avenida 10-05, Guatemala`

**Reglas de Negocio**:
- RN-01: DPI debe ser exactamente 13 dígitos
- RN-03: Teléfono debe ser exactamente 8 dígitos
- RN-04: Correo debe tener formato válido

**Flujo Básico**:
1. Acceder a módulo de Admisión
2. Seleccionar "Registrar Nuevo Paciente"
3. Ingresar todos los datos
4. Hacer clic en "Guardar"
5. Sistema valida datos
6. Sistema genera QR de identidad
7. Mostrar mensaje: "Paciente registrado exitosamente"
8. Mostrar QR para imprimir

**Resultado Esperado**:
- Paciente aparece en la base de datos
- QR se genera correctamente
- Paciente puede ser buscado por DPI

**Postcondiciones**:
- Paciente registrado en patient_schema
- QR disponible para impresión
- Paciente puede crear citas

---

#### Prueba 1.2: Rechazo de DPI duplicado

**Objetivo**: Verificar que no se permite registrar un DPI duplicado

**Precondiciones**:
- Usuario autenticado con rol ADMISSION
- El DPI `1234567890101` ya existe en el sistema

**Datos de Entrada**:
- DPI: `1234567890101` (duplicado)
- Otros datos: válidos

**Flujo Básico**:
1. Acceder a módulo de Admisión
2. Seleccionar "Registrar Nuevo Paciente"
3. Ingresar DPI duplicado
4. Ingresar otros datos
5. Hacer clic en "Guardar"

**Resultado Esperado**:
- Sistema muestra error: "El DPI ya está registrado en el sistema"
- Paciente NO se crea
- Se sugiere buscar al paciente existente

**Postcondiciones**:
- No se crea registro duplicado
- Usuario puede buscar al paciente existente

---

#### Prueba 1.3: Validación de formato de DPI

**Objetivo**: Verificar que se valida el formato del DPI

**Casos de Prueba**:

| DPI | Esperado | Mensaje |
|-----|----------|---------|
| `123456789010` | Rechazar | "El DPI debe tener 13 dígitos" |
| `12345678901012` | Rechazar | "El DPI debe tener 13 dígitos" |
| `123456789010A` | Rechazar | "El DPI debe contener solo dígitos" |
| `1234567890101` | Aceptar | Registro exitoso |

---

#### Prueba 1.4: Validación de teléfono

**Objetivo**: Verificar que se valida el formato del teléfono

**Casos de Prueba**:

| Teléfono | Esperado | Mensaje |
|----------|----------|---------|
| `5555123` | Rechazar | "El teléfono debe tener 8 dígitos" |
| `555512345` | Rechazar | "El teléfono debe tener 8 dígitos" |
| `5555-1234` | Rechazar | "El teléfono debe contener solo dígitos" |
| `55551234` | Aceptar | Registro exitoso |

---

### 3.2 CU-10: Gestión de Admisión (Crear Cita)

#### Prueba 2.1: Creación exitosa de cita

**Objetivo**: Verificar que se crea una cita correctamente

**Precondiciones**:
- Usuario autenticado con rol ADMISSION
- Paciente registrado
- Doctor disponible
- Fecha es futura

**Datos de Entrada**:
- Paciente: Juan Pérez (DPI: 1234567890101)
- Doctor: Dr. Carlos López
- Fecha: 2026-05-20
- Hora: 09:00:00
- Notas: "Primera consulta"

**Reglas de Negocio**:
- RN-11: No se pueden crear citas en fechas pasadas
- RN-12: No se pueden crear citas en horarios ocupados
- RN-13: El doctor debe estar activo

**Flujo Básico**:
1. Acceder a módulo de Admisión
2. Buscar paciente por DPI
3. Seleccionar "Crear Cita"
4. Seleccionar doctor
5. Seleccionar fecha
6. Consultar slots disponibles
7. Seleccionar hora
8. Agregar notas (opcional)
9. Hacer clic en "Confirmar"
10. Sistema reserva slot en Redis
11. Mostrar mensaje: "Cita creada exitosamente"
12. Mostrar QR de cita

**Resultado Esperado**:
- Cita creada en estado SCHEDULED
- Slot reservado en Redis
- QR generado
- Cita aparece en calendario del doctor

**Postcondiciones**:
- Cita registrada en clinical_schema
- Slot no disponible para otros pacientes
- Paciente puede ver su cita

---

#### Prueba 2.2: Rechazo de cita en fecha pasada

**Objetivo**: Verificar que no se permite crear citas en fechas pasadas

**Precondiciones**:
- Usuario autenticado con rol ADMISSION
- Fecha seleccionada es pasada

**Datos de Entrada**:
- Fecha: 2026-04-01 (pasada)
- Hora: 09:00:00

**Flujo Básico**:
1. Acceder a módulo de Admisión
2. Buscar paciente
3. Seleccionar "Crear Cita"
4. Seleccionar fecha pasada
5. Hacer clic en "Confirmar"

**Resultado Esperado**:
- Sistema muestra error: "La fecha no puede ser en el pasado"
- Cita NO se crea

---

#### Prueba 2.3: Rechazo de slot ocupado

**Objetivo**: Verificar que no se permite reservar un slot ya ocupado

**Precondiciones**:
- Usuario autenticado con rol ADMISSION
- Slot ya está reservado por otro paciente

**Datos de Entrada**:
- Doctor: Dr. Carlos López
- Fecha: 2026-05-20
- Hora: 09:00:00 (ya ocupada)

**Flujo Básico**:
1. Acceder a módulo de Admisión
2. Buscar paciente
3. Seleccionar "Crear Cita"
4. Seleccionar doctor y fecha
5. Intentar seleccionar hora ocupada
6. Hacer clic en "Confirmar"

**Resultado Esperado**:
- Sistema muestra error: "No hay slots disponibles para esta hora"
- Cita NO se crea
- Slot no se modifica

---

### 3.3 CU-11: Registro de Signos Vitales

#### Prueba 3.1: Registro exitoso de signos vitales

**Objetivo**: Verificar que se registran correctamente los signos vitales

**Precondiciones**:
- Usuario autenticado con rol VITAL_SIGNS
- Cita en estado VITAL_SIGNS
- Paciente presente

**Datos de Entrada**:
- Presión Sistólica: 120 mmHg
- Presión Diastólica: 80 mmHg
- Frecuencia Cardíaca: 75 latidos/min
- Frecuencia Respiratoria: 16 respiraciones/min
- Temperatura: 36.5 °C
- Saturación de Oxígeno: 98%
- Peso: 70.5 kg
- Altura: 1.75 m

**Reglas de Negocio**:
- RN-14: Presión sistólica > presión diastólica
- RN-15: Valores deben estar en rangos fisiológicos
- RN-16: BMI se calcula automáticamente

**Flujo Básico**:
1. Acceder a módulo de Signos Vitales
2. Ver lista de pacientes esperando
3. Seleccionar paciente
4. Ingresar todos los signos vitales
5. Sistema calcula BMI: 70.5 / (1.75²) = 23.02
6. Hacer clic en "Guardar"
7. Mostrar mensaje: "Signos vitales registrados"
8. Cita pasa a estado CONSULTATION

**Resultado Esperado**:
- Signos vitales guardados en clinical_schema
- BMI calculado correctamente
- Cita pasa a CONSULTATION
- Paciente aparece en cola de consultas

**Postcondiciones**:
- Signos vitales disponibles para el doctor
- Cita lista para consulta médica

---

#### Prueba 3.2: Validación de presión sistólica > diastólica

**Objetivo**: Verificar que se valida la relación entre presiones

**Datos de Entrada**:
- Presión Sistólica: 80 mmHg
- Presión Diastólica: 120 mmHg (inválido)

**Flujo Básico**:
1. Ingresar datos con presión sistólica < diastólica
2. Hacer clic en "Guardar"

**Resultado Esperado**:
- Sistema muestra error: "La presión sistólica debe ser mayor que la diastólica"
- Signos vitales NO se guardan

---

### 3.4 CU-12: Consulta Médica

#### Prueba 4.1: Registro exitoso de consulta

**Objetivo**: Verificar que se registra correctamente una consulta médica

**Precondiciones**:
- Usuario autenticado con rol DOCTOR
- Cita en estado CONSULTATION
- Signos vitales registrados

**Datos de Entrada**:
- Motivo: Dolor de cabeza
- Síntomas: "Dolor frontal, náuseas, fotofobia"
- Diagnóstico Principal: "Migraña sin aura (G43.0)"
- Diagnósticos Secundarios: "Tensión muscular cervical"
- Notas Médicas: "Episodios recurrentes"
- Plan de Tratamiento: "Analgésicos, reposo"
- ¿Requiere Laboratorio?: No
- ¿Requiere Receta?: Sí

**Reglas de Negocio**:
- RN-17: Diagnóstico principal es obligatorio
- RN-18: Si requiere laboratorio, debe especificar exámenes
- RN-19: Si requiere receta, debe especificar medicamentos

**Flujo Básico**:
1. Acceder a módulo de Consultas
2. Ver lista de pacientes en CONSULTATION
3. Seleccionar paciente
4. Revisar signos vitales
5. Ingresar datos de consulta
6. Hacer clic en "Guardar"
7. Mostrar mensaje: "Consulta registrada"
8. Cita pasa a COMPLETED

**Resultado Esperado**:
- Consulta guardada en clinical_schema
- Diagnóstico registrado
- Cita pasa a COMPLETED
- Receta lista para generar

**Postcondiciones**:
- Consulta disponible en historial del paciente
- Receta puede ser generada

---

#### Prueba 4.2: Validación de diagnóstico obligatorio

**Objetivo**: Verificar que diagnóstico principal es obligatorio

**Datos de Entrada**:
- Diagnóstico Principal: (vacío)
- Otros datos: válidos

**Flujo Básico**:
1. Ingresar datos sin diagnóstico
2. Hacer clic en "Guardar"

**Resultado Esperado**:
- Sistema muestra error: "Por favor ingrese un diagnóstico"
- Consulta NO se guarda

---

### 3.5 CU-13: Procesamiento de Exámenes de Laboratorio

#### Prueba 5.1: Flujo completo de laboratorio

**Objetivo**: Verificar el flujo completo de procesamiento de muestras

**Precondiciones**:
- Usuario autenticado con rol LABORATORY
- Orden de laboratorio generada
- Muestra disponible

**Datos de Entrada**:
- Orden: LAB-20260515-001
- Exámenes: Hemograma Completo, Glucosa en Ayunas
- Técnico: María García

**Flujo Básico**:

**Paso 1: Recolección**
1. Ver órdenes pendientes
2. Seleccionar orden
3. Registrar recolección
4. Ingresar notas: "Muestra en buen estado"
5. Hacer clic en "Confirmar"
6. Orden pasa a LAB_SAMPLE_PENDING

**Paso 2: Validación**
1. Seleccionar orden
2. Validar muestra
3. Hacer clic en "Muestra Válida"
4. Orden permanece en LAB_SAMPLE_PENDING

**Paso 3: Procesamiento**
1. Seleccionar orden
2. Hacer clic en "Procesar"
3. Orden pasa a LAB_PROCESSING

**Paso 4: Carga de Resultados**
1. Seleccionar orden
2. Cargar archivo PDF con resultados
3. Ingresar notas (opcional)
4. Hacer clic en "Cargar"
5. Orden pasa a LAB_RESULTS_READY

**Resultado Esperado**:
- Cada paso se completa exitosamente
- Estados cambian correctamente
- Resultados disponibles en historial del paciente

**Postcondiciones**:
- Resultados visibles para doctor y paciente
- Cita pasa a COMPLETED

---

#### Prueba 5.2: Rechazo de muestra inválida

**Objetivo**: Verificar que se puede rechazar una muestra

**Datos de Entrada**:
- Muestra: Insuficiente

**Flujo Básico**:
1. Seleccionar orden
2. Validar muestra
3. Hacer clic en "Muestra Inválida"
4. Ingresar razón: "Muestra insuficiente"
5. Hacer clic en "Confirmar"

**Resultado Esperado**:
- Orden vuelve a estado PENDING
- Se solicita nueva toma de muestra
- Mensaje: "Muestra rechazada. Solicitar nueva toma"

---

### 3.6 CU-14: Despacho de Medicamentos

#### Prueba 6.1: Despacho exitoso de medicamentos

**Objetivo**: Verificar que se despachan correctamente los medicamentos

**Precondiciones**:
- Usuario autenticado con rol PHARMACY
- Receta generada
- Medicamentos en stock

**Datos de Entrada**:
- Receta: RX-20260515-001
- Medicamentos:
  - Ibuprofeno 400mg: 15 pastillas
  - Paracetamol 500mg: 10 pastillas

**Reglas de Negocio**:
- RN-20: Stock debe ser suficiente
- RN-21: Cantidad debe coincidir con receta

**Flujo Básico**:
1. Acceder a módulo de Farmacia
2. Ver recetas pendientes
3. Seleccionar receta
4. Verificar disponibilidad de medicamentos
5. Preparar medicamentos
6. Hacer clic en "Despachar"
7. Mostrar mensaje: "Medicamentos despachados"
8. Receta pasa a DISPENSED

**Resultado Esperado**:
- Medicamentos despachados
- Inventario actualizado
- Receta marcada como DISPENSED
- Paciente puede recoger medicamentos

**Postcondiciones**:
- Stock reducido
- Receta no aparece en pendientes
- Historial del paciente actualizado

---

#### Prueba 6.2: Rechazo por stock insuficiente

**Objetivo**: Verificar que se rechaza despacho sin stock

**Precondiciones**:
- Stock de Ibuprofeno: 5 pastillas
- Receta requiere: 15 pastillas

**Flujo Básico**:
1. Seleccionar receta
2. Verificar disponibilidad
3. Hacer clic en "Despachar"

**Resultado Esperado**:
- Sistema muestra error: "Stock insuficiente para Ibuprofeno"
- Medicamentos NO se despachan
- Receta permanece en PENDING

---

### 3.7 CU-15: Cobros y Facturación

#### Prueba 7.1: Creación exitosa de factura

**Objetivo**: Verificar que se crea correctamente una factura

**Precondiciones**:
- Usuario autenticado con rol CASHIER
- Servicios prestados al paciente

**Datos de Entrada**:
- Paciente: Juan Pérez
- NIT: C/F
- Servicios:
  - Consulta General: 1 × Q150.00 = Q150.00
  - Hemograma Completo: 1 × Q75.00 = Q75.00
- Subtotal: Q225.00
- Descuento: Q0.00
- Total: Q225.00

**Reglas de Negocio**:
- RN-22: Debe seleccionar al menos un servicio
- RN-23: NIT debe tener formato válido
- RN-24: Total se calcula automáticamente

**Flujo Básico**:
1. Acceder a módulo de Caja
2. Buscar paciente
3. Seleccionar "Crear Factura"
4. Seleccionar servicios
5. Sistema calcula total
6. Hacer clic en "Confirmar"
7. Mostrar mensaje: "Factura creada"
8. Mostrar número: FAC-20260515-001

**Resultado Esperado**:
- Factura creada en estado PENDING
- Número generado correctamente
- Total calculado correctamente

**Postcondiciones**:
- Factura lista para pago
- Servicios marcados como facturados

---

#### Prueba 7.2: Procesamiento de pago en efectivo

**Objetivo**: Verificar que se procesa correctamente el pago

**Datos de Entrada**:
- Total: Q225.00
- Monto Pagado: Q250.00
- Método: Efectivo

**Flujo Básico**:
1. Seleccionar factura
2. Hacer clic en "Procesar Pago"
3. Ingresar monto: Q250.00
4. Seleccionar método: Efectivo
5. Hacer clic en "Confirmar"
6. Sistema calcula cambio: Q250.00 - Q225.00 = Q25.00
7. Mostrar recibo con cambio

**Resultado Esperado**:
- Pago registrado
- Cambio calculado correctamente: Q25.00
- Factura pasa a PAID
- Recibo generado

**Postcondiciones**:
- Factura pagada
- Recibo disponible para imprimir
- Historial del paciente actualizado

---

#### Prueba 7.3: Validación de monto

**Objetivo**: Verificar que se valida el monto pagado

**Casos de Prueba**:

| Monto | Total | Esperado | Mensaje |
|-------|-------|----------|---------|
| Q0.00 | Q225.00 | Rechazar | "El monto debe ser mayor a 0" |
| Q100.00 | Q225.00 | Rechazar | "El monto no puede ser menor al total" |
| Q225.00 | Q225.00 | Aceptar | Pago exacto |
| Q250.00 | Q225.00 | Aceptar | Cambio: Q25.00 |

---

## 4. Pruebas de Integración

### 4.1 Flujo Completo: Registro a Facturación

**Objetivo**: Verificar el flujo completo desde registro hasta pago

**Precondiciones**:
- Todos los servicios disponibles
- Base de datos limpia

**Pasos**:
1. ADMISSION: Registrar paciente
2. ADMISSION: Crear cita
3. ADMISSION: Activar cita
4. VITAL_SIGNS: Registrar signos vitales
5. DOCTOR: Realizar triaje
6. DOCTOR: Realizar consulta
7. DOCTOR: Generar receta
8. DOCTOR: Generar orden de laboratorio
9. LABORATORY: Procesar muestra
10. LABORATORY: Cargar resultados
11. PHARMACY: Despachar medicamentos
12. CASHIER: Crear factura
13. CASHIER: Procesar pago

**Resultado Esperado**:
- Cada paso se completa exitosamente
- Datos se propagan correctamente entre servicios
- Historial del paciente contiene toda la información

---

## 5. Pruebas de Validación de Datos

### 5.1 Tabla de Validaciones

| Campo | Tipo | Validación | Ejemplo Válido | Ejemplo Inválido |
|-------|------|-----------|-----------------|------------------|
| DPI | String | 13 dígitos | 1234567890101 | 123456789010 |
| NIT | String | C/F o 1-8 dígitos | C/F | 123456789 |
| Teléfono | String | 8 dígitos | 55551234 | 5555123 |
| Correo | String | Formato email | juan@example.com | juan@example |
| Fecha | Date | Futura | 2026-05-20 | 2026-04-01 |
| Hora | Time | Disponible | 09:00:00 | 09:00:00 (ocupada) |
| Presión Sistólica | Integer | > Diastólica | 120 | 80 (si Diast=120) |
| Temperatura | Float | 35-40°C | 36.5 | 50.0 |
| Peso | Float | > 0 | 70.5 | -5.0 |
| Altura | Float | > 0 | 1.75 | 0.0 |

---

## 6. Criterios de Aceptación

### 6.1 Pruebas Unitarias
- Cobertura mínima: 80%
- Todos los tests deben pasar
- No debe haber warnings

### 6.2 Pruebas de Integración
- Todos los servicios deben comunicarse correctamente
- Tiempos de respuesta < 2 segundos
- No debe haber errores de conexión

### 6.3 Pruebas de Sistema
- Flujo completo debe funcionar sin errores
- Datos deben persistir correctamente
- Historial debe ser accesible

### 6.4 Pruebas de Aceptación
- Todos los casos de uso deben funcionar
- Mensajes de error deben ser claros
- Validaciones deben ser correctas

---

## 7. Ejecución de Pruebas

### 7.1 Backend

```bash
# Pruebas unitarias
mvn test

# Pruebas con cobertura
mvn test jacoco:report

# Pruebas de integración
mvn verify

# Pruebas específicas
mvn test -Dtest=PatientServiceTest
```

### 7.2 Frontend

```bash
# Pruebas unitarias
npm run test

# Pruebas en modo watch
npm run test:watch

# Pruebas con UI
npm run test:ui

# Pruebas específicas
npm run test -- PatientForm.test.tsx
```

---

## 8. Reporte de Defectos

### 8.1 Plantilla de Defecto

```
ID: [DEF-XXX]
Título: [Descripción breve]
Severidad: [Crítica/Alta/Media/Baja]
Prioridad: [Inmediata/Alta/Normal/Baja]

Descripción:
[Descripción detallada del defecto]

Pasos para Reproducir:
1. [Paso 1]
2. [Paso 2]
3. [Resultado inesperado]

Resultado Esperado:
[Qué debería suceder]

Resultado Actual:
[Qué sucede realmente]

Ambiente:
- Navegador: [Chrome/Firefox/Safari]
- SO: [Windows/Mac/Linux]
- Versión: [v1.0.0]

Adjuntos:
- [Screenshot/Video]
```

---

## 9. Matriz de Trazabilidad

| Caso de Uso | Prueba | Estado | Resultado |
|-------------|--------|--------|-----------|
| CU-01 | 1.1 | ✓ | Pasó |
| CU-01 | 1.2 | ✓ | Pasó |
| CU-01 | 1.3 | ✓ | Pasó |
| CU-01 | 1.4 | ✓ | Pasó |
| CU-10 | 2.1 | ✓ | Pasó |
| CU-10 | 2.2 | ✓ | Pasó |
| CU-10 | 2.3 | ✓ | Pasó |
| CU-11 | 3.1 | ✓ | Pasó |
| CU-11 | 3.2 | ✓ | Pasó |
| CU-12 | 4.1 | ✓ | Pasó |
| CU-12 | 4.2 | ✓ | Pasó |
| CU-13 | 5.1 | ✓ | Pasó |
| CU-13 | 5.2 | ✓ | Pasó |
| CU-14 | 6.1 | ✓ | Pasó |
| CU-14 | 6.2 | ✓ | Pasó |
| CU-15 | 7.1 | ✓ | Pasó |
| CU-15 | 7.2 | ✓ | Pasó |
| CU-15 | 7.3 | ✓ | Pasó |

---

## 10. Recursos Adicionales

- **Casos de Uso**: `Documentación/USE_CASES_REFERENCE.md`
- **Manual de Usuario**: `docs/Manual_de_Usuario.md`
- **API Endpoints**: `docs/04-API-ENDPOINTS.md`
- **Flujo de Atención**: `docs/02-FLUJO-ATENCION.md`

---

**Versión**: 1.0  
**Última Actualización**: Abril 2026  
**Mantenido por**: MedFlow Team
