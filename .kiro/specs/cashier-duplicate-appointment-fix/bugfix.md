# Bugfix Requirements Document

## Introduction

Este documento describe el bug de duplicación de citas en el módulo de Cajero (Cashier) del sistema MedFlow. El problema ocurre cuando una cita que ya fue pagada vuelve a aparecer incorrectamente en la lista de citas pendientes de pago cuando el paciente regresa para pagar servicios adicionales (laboratorios o farmacia).

**Impacto:** Este bug causa confusión en el personal de caja, puede llevar a intentos de cobro duplicado, y afecta la integridad de los datos mostrados en la interfaz de usuario del módulo de Cajero.

**Contexto del flujo:**
1. Admisión agenda una cita → llega a Cajero
2. Cajero procesa el pago de la cita → pago exitoso (estado cambia a SCHEDULED o VITAL_SIGNS)
3. Doctor atiende al paciente y ordena laboratorios (estado cambia a PENDING_LAB_PAYMENT)
4. Cuando los laboratorios llegan a Cajero para pago, la cita original aparece DUPLICADA:
   - Aparece correctamente en la lista de "Laboratorios" (PENDING_LAB_PAYMENT)
   - Aparece INCORRECTAMENTE en la lista de "Citas" (como si estuviera en PENDING_PAYMENT)

## Bug Analysis

### Current Behavior (Defect)

1.1 WHEN una cita tiene estado PENDING_LAB_PAYMENT THEN el sistema muestra la cita tanto en la lista de "Laboratorios" como en la lista de "Citas" pendientes de pago

1.2 WHEN una cita tiene estado PENDING_PHARMACY_PAYMENT THEN el sistema muestra la cita tanto en la lista de "Farmacia" como en la lista de "Citas" pendientes de pago

1.3 WHEN el filtro `queue=payment` es aplicado THEN el sistema incluye citas en estados PENDING_LAB_PAYMENT y PENDING_PHARMACY_PAYMENT en los resultados

1.4 WHEN el método `matchesQueueFilter` evalúa el filtro "payment" THEN el sistema retorna true para citas con estado PENDING_LAB_PAYMENT o PENDING_PHARMACY_PAYMENT

### Expected Behavior (Correct)

2.1 WHEN una cita tiene estado PENDING_LAB_PAYMENT THEN el sistema SHALL mostrar la cita ÚNICAMENTE en la lista de "Laboratorios" y NO en la lista de "Citas" pendientes de pago

2.2 WHEN una cita tiene estado PENDING_PHARMACY_PAYMENT THEN el sistema SHALL mostrar la cita ÚNICAMENTE en la lista de "Farmacia" y NO en la lista de "Citas" pendientes de pago

2.3 WHEN el filtro `queue=payment` es aplicado THEN el sistema SHALL incluir ÚNICAMENTE citas en estados PENDING_PAYMENT y SCHEDULED, excluyendo PENDING_LAB_PAYMENT y PENDING_PHARMACY_PAYMENT

2.4 WHEN el método `matchesQueueFilter` evalúa el filtro "payment" THEN el sistema SHALL retornar true ÚNICAMENTE para citas con estado PENDING_PAYMENT o SCHEDULED

### Unchanged Behavior (Regression Prevention)

3.1 WHEN una cita tiene estado PENDING_PAYMENT THEN el sistema SHALL CONTINUE TO mostrar la cita en la lista de "Citas" pendientes de pago

3.2 WHEN una cita tiene estado SCHEDULED THEN el sistema SHALL CONTINUE TO mostrar la cita en la lista de "Citas" pendientes de pago

3.3 WHEN el filtro `queue=lab` es aplicado THEN el sistema SHALL CONTINUE TO incluir únicamente citas en estado LABORATORY

3.4 WHEN el filtro `queue=pharmacy` es aplicado THEN el sistema SHALL CONTINUE TO incluir únicamente citas en estado PHARMACY

3.5 WHEN el filtro `queue=triage` es aplicado THEN el sistema SHALL CONTINUE TO incluir únicamente citas en estado VITAL_SIGNS

3.6 WHEN el filtro `queue=admission` es aplicado THEN el sistema SHALL CONTINUE TO incluir citas en estados PENDING_PAYMENT y SCHEDULED

3.7 WHEN una cita tiene estado PENDING_LAB_PAYMENT y el filtro `queue=lab` es aplicado THEN el sistema SHALL CONTINUE TO NO incluir la cita en los resultados (solo LABORATORY debe aparecer en cola de laboratorio)

3.8 WHEN una cita tiene estado PENDING_PHARMACY_PAYMENT y el filtro `queue=pharmacy` es aplicado THEN el sistema SHALL CONTINUE TO NO incluir la cita en los resultados (solo PHARMACY debe aparecer en cola de farmacia)

---

## Bug Condition Derivation

### Bug Condition Function

```pascal
FUNCTION isBugCondition(X)
  INPUT: X of type AppointmentQueueFilter
  OUTPUT: boolean
  
  // Returns true when the bug condition is met
  // X.appointment.status is the appointment status
  // X.queueFilter is the queue filter being applied
  
  RETURN (X.queueFilter = "payment") AND 
         (X.appointment.status = PENDING_LAB_PAYMENT OR 
          X.appointment.status = PENDING_PHARMACY_PAYMENT)
END FUNCTION
```

**Explicación:** El bug ocurre cuando se aplica el filtro de cola "payment" y la cita tiene un estado de pago pendiente para servicios adicionales (laboratorios o farmacia), no para la consulta original.

### Property Specification - Fix Checking

```pascal
// Property: Fix Checking - Payment Queue Filter Correctness
FOR ALL X WHERE isBugCondition(X) DO
  result ← matchesQueueFilter'(X.appointment, X.queueFilter)
  ASSERT result = false
END FOR
```

**Explicación:** Para todas las citas que cumplen la condición de bug (están en PENDING_LAB_PAYMENT o PENDING_PHARMACY_PAYMENT y se aplica el filtro "payment"), el método corregido debe retornar `false`, excluyéndolas de la lista de citas pendientes de pago.

### Property Specification - Preservation Checking

```pascal
// Property: Preservation Checking - Other Queue Filters Unchanged
FOR ALL X WHERE NOT isBugCondition(X) DO
  ASSERT matchesQueueFilter(X.appointment, X.queueFilter) = 
         matchesQueueFilter'(X.appointment, X.queueFilter)
END FOR
```

**Explicación:** Para todas las citas que NO cumplen la condición de bug (otros estados o filtros diferentes), el comportamiento del método debe permanecer idéntico antes y después del fix.

### Counterexample

**Entrada concreta que demuestra el bug:**

```java
Appointment appointment = new Appointment();
appointment.setStatus(AppointmentStatus.PENDING_LAB_PAYMENT);

String queueFilter = "payment";

// Comportamiento actual (incorrecto):
boolean result = matchesQueueFilter(appointment, queueFilter);
// result = true (la cita aparece en la cola de pago)

// Comportamiento esperado (correcto):
boolean expectedResult = false;
// La cita NO debería aparecer en la cola de pago, solo en la cola de laboratorios
```

---

## Key Definitions

| Concepto | Definición | Ejemplo |
|----------|------------|---------|
| **C(X)** | Bug Condition - identifica entradas que activan el bug | `(queueFilter = "payment") AND (status = PENDING_LAB_PAYMENT OR status = PENDING_PHARMACY_PAYMENT)` |
| **P(result)** | Property - comportamiento deseado para C(X) | `result = false` (no incluir en cola de pago) |
| **¬C(X)** | Entradas no-buggy - deben preservarse | Otros estados o filtros diferentes |
| **F** | Función original (sin fix) | `matchesQueueFilter` actual en AppointmentController |
| **F'** | Función corregida | `matchesQueueFilter` después del fix |
| **Counterexample** | Ejemplo concreto que demuestra el bug | Cita con PENDING_LAB_PAYMENT aparece en queue=payment |
