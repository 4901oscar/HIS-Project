# Cashier Duplicate Appointment Fix - Bugfix Design

## Overview

Este documento describe el diseño técnico para corregir el bug de duplicación de citas en el módulo de Cajero. El problema ocurre en el método `matchesQueueFilter` del `AppointmentController`, que incorrectamente incluye citas con estados `PENDING_LAB_PAYMENT` y `PENDING_PHARMACY_PAYMENT` en la cola de pago de consultas (`queue=payment`).

**Estrategia de Fix:** Modificar la lógica del filtro "payment" en el método `matchesQueueFilter` para excluir explícitamente los estados `PENDING_LAB_PAYMENT` y `PENDING_PHARMACY_PAYMENT`, manteniendo únicamente `PENDING_PAYMENT` y `SCHEDULED`.

**Impacto:** Este fix es quirúrgico y de bajo riesgo. Solo afecta una condición específica en un método de filtrado, sin cambios en la estructura de datos, API contracts, o flujos de negocio.

## Glossary

- **Bug_Condition (C)**: La condición que activa el bug - cuando el filtro "payment" incluye incorrectamente citas en estados PENDING_LAB_PAYMENT o PENDING_PHARMACY_PAYMENT
- **Property (P)**: El comportamiento deseado cuando se aplica el filtro "payment" - solo incluir citas en estados PENDING_PAYMENT y SCHEDULED
- **Preservation**: Los comportamientos existentes de otros filtros de cola (lab, pharmacy, triage, admission) que deben permanecer sin cambios
- **matchesQueueFilter**: El método en `AppointmentController.java` (línea 231) que determina si una cita coincide con un filtro de cola específico
- **queue parameter**: El parámetro de query string que especifica el tipo de cola a filtrar (payment, lab, pharmacy, triage, admission)
- **AppointmentStatus**: El enum que define los estados posibles de una cita (PENDING_PAYMENT, SCHEDULED, PENDING_LAB_PAYMENT, PENDING_PHARMACY_PAYMENT, LABORATORY, PHARMACY, VITAL_SIGNS, etc.)

## Bug Details

### Bug Condition

El bug se manifiesta cuando el personal de Cajero aplica el filtro `queue=payment` para ver las citas pendientes de pago de consulta. El método `matchesQueueFilter` incluye incorrectamente citas que están esperando pago de servicios adicionales (laboratorios o farmacia), causando que aparezcan duplicadas en múltiples listas.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type AppointmentQueueFilterInput
         where input.appointment is an Appointment object
         and input.queueFilter is a String
  OUTPUT: boolean
  
  RETURN input.queueFilter.toLowerCase() == "payment"
         AND (input.appointment.status == PENDING_LAB_PAYMENT
              OR input.appointment.status == PENDING_PHARMACY_PAYMENT)
END FUNCTION
```

**Explicación:** El bug ocurre cuando:
1. Se aplica el filtro de cola "payment" (case-insensitive)
2. Y la cita tiene estado PENDING_LAB_PAYMENT o PENDING_PHARMACY_PAYMENT

En este escenario, el método actual retorna `true`, incluyendo la cita en la lista de pagos de consulta, cuando debería retornar `false`.

### Examples

**Ejemplo 1: Cita con laboratorios pendientes de pago**
- **Input:** `appointment.status = PENDING_LAB_PAYMENT`, `queueFilter = "payment"`
- **Comportamiento actual (incorrecto):** `matchesQueueFilter` retorna `true` → La cita aparece en la lista de "Citas" pendientes de pago
- **Comportamiento esperado (correcto):** `matchesQueueFilter` debe retornar `false` → La cita NO debe aparecer en la lista de "Citas", solo en "Laboratorios"

**Ejemplo 2: Cita con farmacia pendiente de pago**
- **Input:** `appointment.status = PENDING_PHARMACY_PAYMENT`, `queueFilter = "payment"`
- **Comportamiento actual (incorrecto):** `matchesQueueFilter` retorna `true` → La cita aparece en la lista de "Citas" pendientes de pago
- **Comportamiento esperado (correcto):** `matchesQueueFilter` debe retornar `false` → La cita NO debe aparecer en la lista de "Citas", solo en "Farmacia"

**Ejemplo 3: Cita pendiente de pago de consulta (caso válido)**
- **Input:** `appointment.status = PENDING_PAYMENT`, `queueFilter = "payment"`
- **Comportamiento actual (correcto):** `matchesQueueFilter` retorna `true` → La cita aparece correctamente en la lista de "Citas" pendientes de pago
- **Comportamiento esperado (correcto):** Debe continuar retornando `true` (sin cambios)

**Ejemplo 4: Cita agendada esperando activación (caso válido)**
- **Input:** `appointment.status = SCHEDULED`, `queueFilter = "payment"`
- **Comportamiento actual (correcto):** `matchesQueueFilter` retorna `true` → La cita aparece correctamente en la lista de "Citas" (puede requerir pago antes de activación)
- **Comportamiento esperado (correcto):** Debe continuar retornando `true` (sin cambios)

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Citas con estado `PENDING_PAYMENT` deben continuar apareciendo en la cola de pago (`queue=payment`)
- Citas con estado `SCHEDULED` deben continuar apareciendo en la cola de pago (`queue=payment`)
- El filtro `queue=lab` debe continuar incluyendo únicamente citas en estado `LABORATORY`
- El filtro `queue=pharmacy` debe continuar incluyendo únicamente citas en estado `PHARMACY`
- El filtro `queue=triage` debe continuar incluyendo únicamente citas en estado `VITAL_SIGNS`
- El filtro `queue=admission` debe continuar incluyendo citas en estados `PENDING_PAYMENT` y `SCHEDULED`
- Citas con estado `PENDING_LAB_PAYMENT` NO deben aparecer en el filtro `queue=lab` (solo `LABORATORY` debe aparecer)
- Citas con estado `PENDING_PHARMACY_PAYMENT` NO deben aparecer en el filtro `queue=pharmacy` (solo `PHARMACY` debe aparecer)

**Scope:**
Todas las combinaciones de (appointment.status, queueFilter) que NO involucran el filtro "payment" con estados PENDING_LAB_PAYMENT o PENDING_PHARMACY_PAYMENT deben comportarse exactamente igual antes y después del fix. Esto incluye:
- Todos los demás filtros de cola (lab, pharmacy, triage, admission)
- Todos los demás estados de cita cuando se aplica el filtro "payment"
- El comportamiento cuando queueFilter es null o vacío (debe retornar true para todas las citas)

## Hypothesized Root Cause

Basado en el análisis del código actual en `AppointmentController.java` (líneas 231-253), el root cause es:

**1. Lógica de Filtro Demasiado Inclusiva**: El case "payment" en el switch statement incluye explícitamente los estados `PENDING_LAB_PAYMENT` y `PENDING_PHARMACY_PAYMENT`:

```java
case "payment":
    return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
           appointment.getStatus() == AppointmentStatus.PENDING_LAB_PAYMENT ||  // ← PROBLEMA
           appointment.getStatus() == AppointmentStatus.PENDING_PHARMACY_PAYMENT || // ← PROBLEMA
           appointment.getStatus() == AppointmentStatus.SCHEDULED;
```

**Análisis:** Esta lógica fue probablemente diseñada con la intención de mostrar "todos los pagos pendientes" en una sola cola, pero no considera que:
- Los pagos de laboratorio y farmacia son transacciones separadas de la consulta original
- Estos pagos ya tienen sus propias colas dedicadas (aunque actualmente no se usan porque los estados PENDING_LAB_PAYMENT y PENDING_PHARMACY_PAYMENT no coinciden con los filtros "lab" y "pharmacy")
- Incluir estos estados causa duplicación visual en la UI del Cajero

**2. Confusión Semántica de Estados**: Los nombres de los estados `PENDING_LAB_PAYMENT` y `PENDING_PHARMACY_PAYMENT` sugieren que son "pagos pendientes", lo que puede haber llevado al desarrollador original a incluirlos en el filtro "payment". Sin embargo, semánticamente representan:
- `PENDING_PAYMENT`: Pago de consulta pendiente (primera transacción)
- `PENDING_LAB_PAYMENT`: Pago de laboratorios pendiente (transacción adicional después de la consulta)
- `PENDING_PHARMACY_PAYMENT`: Pago de farmacia pendiente (transacción adicional después de la consulta)

**3. Falta de Separación de Contextos**: El filtro "payment" debería representar únicamente el contexto de "pago de consulta", no "cualquier tipo de pago". La inclusión de pagos adicionales rompe esta separación de contextos.

## Correctness Properties

Property 1: Bug Condition - Payment Queue Excludes Additional Service Payments

_For any_ appointment where the status is PENDING_LAB_PAYMENT or PENDING_PHARMACY_PAYMENT and the queue filter is "payment", the fixed matchesQueueFilter function SHALL return false, excluding the appointment from the consultation payment queue.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

Property 2: Preservation - Non-Buggy Filter Behavior Unchanged

_For any_ appointment and queue filter combination where the bug condition does NOT hold (i.e., queue filter is not "payment", or status is not PENDING_LAB_PAYMENT/PENDING_PHARMACY_PAYMENT), the fixed matchesQueueFilter function SHALL produce exactly the same result as the original function, preserving all existing filter behaviors for other queues and statuses.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8**

## Fix Implementation

### Changes Required

Asumiendo que nuestro análisis de root cause es correcto:

**File**: `backend-services/clinical-service/src/main/java/com/medframe/clinical/infrastructure/rest/controller/AppointmentController.java`

**Method**: `matchesQueueFilter` (línea 231)

**Specific Changes**:

1. **Remover Estados Incorrectos del Filtro "payment"**: Eliminar las condiciones que incluyen `PENDING_LAB_PAYMENT` y `PENDING_PHARMACY_PAYMENT` del case "payment"
   - **Línea actual (~239-242):**
     ```java
     case "payment":
         return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
                appointment.getStatus() == AppointmentStatus.PENDING_LAB_PAYMENT ||
                appointment.getStatus() == AppointmentStatus.PENDING_PHARMACY_PAYMENT ||
                appointment.getStatus() == AppointmentStatus.SCHEDULED;
     ```
   - **Línea corregida:**
     ```java
     case "payment":
         return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
                appointment.getStatus() == AppointmentStatus.SCHEDULED;
     ```

2. **Agregar Comentario Explicativo**: Documentar por qué estos estados están excluidos para prevenir regresiones futuras
   - **Agregar antes del case "payment":**
     ```java
     // "payment" queue: Only consultation payments (PENDING_PAYMENT, SCHEDULED)
     // Excludes PENDING_LAB_PAYMENT and PENDING_PHARMACY_PAYMENT to prevent
     // duplicate display (those belong to their respective service queues)
     case "payment":
     ```

3. **Verificar Consistencia con Otros Filtros**: Confirmar que los filtros "lab" y "pharmacy" NO incluyen los estados PENDING_LAB_PAYMENT y PENDING_PHARMACY_PAYMENT (actualmente correcto)
   - **Línea actual (~243-247):** ✓ Correcto - solo incluye LABORATORY
     ```java
     case "lab":
     case "laboratory":
         return appointment.getStatus() == AppointmentStatus.LABORATORY;
     ```
   - **Línea actual (~248-249):** ✓ Correcto - solo incluye PHARMACY
     ```java
     case "pharmacy":
         return appointment.getStatus() == AppointmentStatus.PHARMACY;
     ```

4. **Verificar Filtro "admission"**: Confirmar que el filtro "admission" NO incluye los estados problemáticos (actualmente correcto)
   - **Línea actual (~252-254):** ✓ Correcto - solo incluye PENDING_PAYMENT y SCHEDULED
     ```java
     case "admission":
         return appointment.getStatus() == AppointmentStatus.PENDING_PAYMENT ||
                appointment.getStatus() == AppointmentStatus.SCHEDULED;
     ```

**Resumen del Cambio:**
- **Tipo:** Modificación de lógica condicional (eliminación de 2 condiciones OR)
- **Alcance:** 2 líneas de código en un solo método
- **Riesgo:** Muy bajo - cambio quirúrgico sin efectos secundarios
- **Reversibilidad:** Alta - fácil de revertir si es necesario

## Testing Strategy

### Validation Approach

La estrategia de testing sigue un enfoque de dos fases: primero, ejecutar tests exploratorios en el código SIN FIX para confirmar el bug y validar nuestra hipótesis de root cause; luego, aplicar el fix y ejecutar tests de verificación (fix checking y preservation checking) para confirmar que el bug está resuelto y que no se introdujeron regresiones.

### Exploratory Bug Condition Checking

**Goal**: Demostrar el bug en el código UNFIXED y confirmar nuestra hipótesis de root cause. Si los tests pasan en el código unfixed, nuestra hipótesis es incorrecta y debemos re-analizar.

**Test Plan**: Escribir unit tests que invoquen directamente el método `matchesQueueFilter` con las combinaciones de (appointment.status, queueFilter) que deberían activar el bug. Ejecutar estos tests en el código UNFIXED y observar que fallan (el método retorna `true` cuando debería retornar `false`).

**Test Cases**:

1. **Exploratory Test 1: PENDING_LAB_PAYMENT with payment filter**
   - **Input:** `appointment.status = PENDING_LAB_PAYMENT`, `queueFilter = "payment"`
   - **Expected on UNFIXED code:** `matchesQueueFilter` retorna `true` (bug manifestado)
   - **Expected on FIXED code:** `matchesQueueFilter` retorna `false` (bug corregido)
   - **Purpose:** Confirmar que el bug existe para laboratorios

2. **Exploratory Test 2: PENDING_PHARMACY_PAYMENT with payment filter**
   - **Input:** `appointment.status = PENDING_PHARMACY_PAYMENT`, `queueFilter = "payment"`
   - **Expected on UNFIXED code:** `matchesQueueFilter` retorna `true` (bug manifestado)
   - **Expected on FIXED code:** `matchesQueueFilter` retorna `false` (bug corregido)
   - **Purpose:** Confirmar que el bug existe para farmacia

3. **Exploratory Test 3: PENDING_LAB_PAYMENT with lab filter**
   - **Input:** `appointment.status = PENDING_LAB_PAYMENT`, `queueFilter = "lab"`
   - **Expected on UNFIXED code:** `matchesQueueFilter` retorna `false` (comportamiento correcto)
   - **Expected on FIXED code:** `matchesQueueFilter` retorna `false` (sin cambios)
   - **Purpose:** Confirmar que los estados PENDING_LAB_PAYMENT no aparecen en la cola de laboratorio (comportamiento esperado)

4. **Exploratory Test 4: PENDING_PHARMACY_PAYMENT with pharmacy filter**
   - **Input:** `appointment.status = PENDING_PHARMACY_PAYMENT`, `queueFilter = "pharmacy"`
   - **Expected on UNFIXED code:** `matchesQueueFilter` retorna `false` (comportamiento correcto)
   - **Expected on FIXED code:** `matchesQueueFilter` retorna `false` (sin cambios)
   - **Purpose:** Confirmar que los estados PENDING_PHARMACY_PAYMENT no aparecen en la cola de farmacia (comportamiento esperado)

**Expected Counterexamples**:
- Tests 1 y 2 deben FALLAR en código unfixed (retornan `true` cuando esperamos `false`)
- Tests 3 y 4 deben PASAR en código unfixed (retornan `false` como esperamos)
- Si los tests 1 y 2 PASAN en código unfixed, nuestra hipótesis de root cause es incorrecta

**Acción si la hipótesis es refutada:**
Si los tests exploratorios no demuestran el bug como esperamos, debemos:
1. Re-examinar el código para identificar el verdadero root cause
2. Verificar si el bug está en otro método o capa (ej: frontend, mapeo de DTOs)
3. Actualizar la hipótesis y el plan de fix
4. Re-ejecutar exploratory tests con la nueva hipótesis

### Fix Checking

**Goal**: Verificar que para todas las entradas donde la condición de bug se cumple, la función corregida produce el comportamiento esperado.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := matchesQueueFilter_fixed(input.appointment, input.queueFilter)
  ASSERT result = false
END FOR
```

**Test Plan**: Después de aplicar el fix, ejecutar unit tests que cubran todas las combinaciones de estados problemáticos (PENDING_LAB_PAYMENT, PENDING_PHARMACY_PAYMENT) con el filtro "payment". Todos deben retornar `false`.

**Test Cases**:

1. **Fix Check 1: PENDING_LAB_PAYMENT excluded from payment queue**
   - **Input:** `appointment.status = PENDING_LAB_PAYMENT`, `queueFilter = "payment"`
   - **Expected:** `matchesQueueFilter` retorna `false`
   - **Validates:** Property 1, Requirements 2.1, 2.3

2. **Fix Check 2: PENDING_PHARMACY_PAYMENT excluded from payment queue**
   - **Input:** `appointment.status = PENDING_PHARMACY_PAYMENT`, `queueFilter = "payment"`
   - **Expected:** `matchesQueueFilter` retorna `false`
   - **Validates:** Property 1, Requirements 2.2, 2.3

3. **Fix Check 3: Case-insensitive filter handling**
   - **Input:** `appointment.status = PENDING_LAB_PAYMENT`, `queueFilter = "PAYMENT"` (uppercase)
   - **Expected:** `matchesQueueFilter` retorna `false`
   - **Validates:** Property 1, edge case handling

4. **Fix Check 4: Mixed case filter handling**
   - **Input:** `appointment.status = PENDING_PHARMACY_PAYMENT`, `queueFilter = "Payment"` (mixed case)
   - **Expected:** `matchesQueueFilter` retorna `false`
   - **Validates:** Property 1, edge case handling

### Preservation Checking

**Goal**: Verificar que para todas las entradas donde la condición de bug NO se cumple, la función corregida produce exactamente el mismo resultado que la función original.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT matchesQueueFilter_original(input.appointment, input.queueFilter) = 
         matchesQueueFilter_fixed(input.appointment, input.queueFilter)
END FOR
```

**Testing Approach**: Property-based testing es recomendado para preservation checking porque:
- Genera automáticamente muchos casos de prueba a través del dominio de entrada
- Detecta edge cases que los unit tests manuales podrían omitir
- Proporciona garantías sólidas de que el comportamiento no cambió para todas las entradas no-buggy

**Test Plan**: Ejecutar property-based tests que generen combinaciones aleatorias de (appointment.status, queueFilter) y verifiquen que el comportamiento es idéntico antes y después del fix para todas las combinaciones que NO activan el bug.

**Test Cases**:

1. **Preservation Test 1: PENDING_PAYMENT still included in payment queue**
   - **Input:** `appointment.status = PENDING_PAYMENT`, `queueFilter = "payment"`
   - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
   - **Validates:** Property 2, Requirements 3.1

2. **Preservation Test 2: SCHEDULED still included in payment queue**
   - **Input:** `appointment.status = SCHEDULED`, `queueFilter = "payment"`
   - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
   - **Validates:** Property 2, Requirements 3.2

3. **Preservation Test 3: Lab queue filter unchanged**
   - **Input:** `appointment.status = LABORATORY`, `queueFilter = "lab"`
   - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
   - **Validates:** Property 2, Requirements 3.3

4. **Preservation Test 4: Pharmacy queue filter unchanged**
   - **Input:** `appointment.status = PHARMACY`, `queueFilter = "pharmacy"`
   - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
   - **Validates:** Property 2, Requirements 3.4

5. **Preservation Test 5: Triage queue filter unchanged**
   - **Input:** `appointment.status = VITAL_SIGNS`, `queueFilter = "triage"`
   - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
   - **Validates:** Property 2, Requirements 3.5

6. **Preservation Test 6: Admission queue filter unchanged**
   - **Input:** `appointment.status = PENDING_PAYMENT`, `queueFilter = "admission"`
   - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
   - **Validates:** Property 2, Requirements 3.6

7. **Preservation Test 7: PENDING_LAB_PAYMENT not in lab queue**
   - **Input:** `appointment.status = PENDING_LAB_PAYMENT`, `queueFilter = "lab"`
   - **Expected:** `matchesQueueFilter` retorna `false` (sin cambios)
   - **Validates:** Property 2, Requirements 3.7

8. **Preservation Test 8: PENDING_PHARMACY_PAYMENT not in pharmacy queue**
   - **Input:** `appointment.status = PENDING_PHARMACY_PAYMENT`, `queueFilter = "pharmacy"`
   - **Expected:** `matchesQueueFilter` retorna `false` (sin cambios)
   - **Validates:** Property 2, Requirements 3.8

9. **Preservation Test 9: Null filter returns true for all statuses**
   - **Input:** `appointment.status = [any status]`, `queueFilter = null`
   - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
   - **Validates:** Property 2, edge case handling

10. **Preservation Test 10: Empty filter returns true for all statuses**
    - **Input:** `appointment.status = [any status]`, `queueFilter = ""`
    - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
    - **Validates:** Property 2, edge case handling

11. **Preservation Test 11: Unknown filter returns true (default behavior)**
    - **Input:** `appointment.status = [any status]`, `queueFilter = "unknown"`
    - **Expected:** `matchesQueueFilter` retorna `true` (sin cambios)
    - **Validates:** Property 2, default case handling

### Unit Tests

- Test `matchesQueueFilter` directamente con todas las combinaciones de estados y filtros
- Test edge cases: null filter, empty filter, unknown filter, case variations
- Test boundary conditions: estados no relacionados con pagos (COMPLETED, CANCELLED, etc.)
- Test que el método `listAppointments` con `queue=payment` no incluye citas duplicadas

### Property-Based Tests

- Generar combinaciones aleatorias de (AppointmentStatus, queueFilter) y verificar que:
  - Para estados PENDING_LAB_PAYMENT y PENDING_PHARMACY_PAYMENT con filtro "payment": siempre retorna `false`
  - Para todas las demás combinaciones: el comportamiento es idéntico antes y después del fix
- Generar variaciones de case para queueFilter (lowercase, uppercase, mixed) y verificar consistencia
- Generar estados de cita aleatorios y verificar que el filtro null/empty siempre retorna `true`

### Integration Tests

- Test del endpoint `GET /api/clinical/appointments?queue=payment` y verificar que:
  - No incluye citas con estado PENDING_LAB_PAYMENT
  - No incluye citas con estado PENDING_PHARMACY_PAYMENT
  - Incluye citas con estado PENDING_PAYMENT
  - Incluye citas con estado SCHEDULED
- Test del endpoint `GET /api/clinical/appointments?queue=lab` y verificar que:
  - Incluye citas con estado LABORATORY
  - No incluye citas con estado PENDING_LAB_PAYMENT
- Test del endpoint `GET /api/clinical/appointments?queue=pharmacy` y verificar que:
  - Incluye citas con estado PHARMACY
  - No incluye citas con estado PENDING_PHARMACY_PAYMENT
- Test de flujo completo: crear cita → pagar consulta → ordenar laboratorios → verificar que la cita aparece solo en la cola de laboratorios, no en la cola de pagos de consulta
