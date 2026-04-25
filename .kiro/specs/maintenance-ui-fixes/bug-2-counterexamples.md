# Bug 2: Contraejemplos Encontrados - Campos Obligatorios Sin Asterisco

## Resumen de Ejecución del Test

**Fecha**: 2025-01-XX  
**Test**: `bug-condition-required-fields.test.tsx`  
**Resultado**: ✅ Test FALLÓ como se esperaba (confirma que el bug existe)  
**Tests ejecutados**: 5  
**Tests fallidos**: 5  

## Contraejemplos Documentados

### Contraejemplo 1: Campo "Seleccionar Doctor"

**Comportamiento Actual (Buggy)**:
- El label muestra: `Seleccionar Doctor *` (asterisco como texto plano)
- NO contiene: `<span className="text-red-500">*</span>`

**Comportamiento Esperado**:
- El label debe mostrar: `Seleccionar Doctor ` seguido de `<span className="text-red-500">*</span>`

**Error del Test**:
```
Error: expect(received).toBeInTheDocument()
received value must be an HTMLElement or an SVGElement.
Received has type:  Null
Received has value: null
```

**Causa Raíz Confirmada**: El código actual en `DoctorForm.tsx` línea ~127 tiene:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Seleccionar Doctor *
</label>
```

Debería ser:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Seleccionar Doctor <span className="text-red-500">*</span>
</label>
```

---

### Contraejemplo 2: Campo "Especialidad"

**Comportamiento Actual (Buggy)**:
- El label muestra: `Especialidad *` (asterisco como texto plano)
- NO contiene: `<span className="text-red-500">*</span>`

**Comportamiento Esperado**:
- El label debe mostrar: `Especialidad ` seguido de `<span className="text-red-500">*</span>`

**Error del Test**:
```
Error: expect(received).toBeInTheDocument()
received value must be an HTMLElement or an SVGElement.
Received has type:  Null
Received has value: null
```

**Causa Raíz Confirmada**: El código actual en `DoctorForm.tsx` línea ~175 tiene:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Especialidad *
</label>
```

Debería ser:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Especialidad <span className="text-red-500">*</span>
</label>
```

---

### Contraejemplo 3: Campo "Hora de Inicio del Turno"

**Comportamiento Actual (Buggy)**:
- El label muestra: `Hora de Inicio del Turno *` (asterisco como texto plano)
- NO contiene: `<span className="text-red-500">*</span>`

**Comportamiento Esperado**:
- El label debe mostrar: `Hora de Inicio del Turno ` seguido de `<span className="text-red-500">*</span>`

**Error del Test**:
```
Error: expect(received).toBeInTheDocument()
received value must be an HTMLElement or an SVGElement.
Received has type:  Null
Received has value: null
```

**Causa Raíz Confirmada**: El código actual en `DoctorForm.tsx` línea ~192 tiene:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Hora de Inicio del Turno *
</label>
```

Debería ser:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Hora de Inicio del Turno <span className="text-red-500">*</span>
</label>
```

---

### Contraejemplo 4: Campo "Hora de Fin del Turno"

**Comportamiento Actual (Buggy)**:
- El label muestra: `Hora de Fin del Turno *` (asterisco como texto plano)
- NO contiene: `<span className="text-red-500">*</span>`

**Comportamiento Esperado**:
- El label debe mostrar: `Hora de Fin del Turno ` seguido de `<span className="text-red-500">*</span>`

**Error del Test**:
```
Error: expect(received).toBeInTheDocument()
received value must be an HTMLElement or an SVGElement.
Received has type:  Null
Received has value: null
```

**Causa Raíz Confirmada**: El código actual en `DoctorForm.tsx` línea ~209 tiene:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Hora de Fin del Turno *
</label>
```

Debería ser:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Hora de Fin del Turno <span className="text-red-500">*</span>
</label>
```

---

## Análisis de Causa Raíz

### Causa Raíz Confirmada

**Omisión en el Desarrollo**: Los labels de los campos obligatorios en `DoctorForm.tsx` no incluyen el elemento `<span className="text-red-500">*</span>` que sí está presente en otros formularios del sistema (como `EmployeeFormPage`).

**Inconsistencia con Otros Formularios**: 
- ✅ `EmployeeFormPage` y otros formularios SÍ muestran asteriscos rojos en campos obligatorios
- ❌ `DoctorForm` NO muestra asteriscos rojos, solo texto plano con "*"

**Patrón Correcto Identificado**:
El patrón correcto usado en otros formularios del sistema es:
```tsx
<label className="block text-sm font-medium text-gray-700 mb-1">
  Nombre del Campo <span className="text-red-500">*</span>
</label>
```

### Impacto del Bug

**Experiencia de Usuario**:
- Los usuarios no pueden identificar visualmente cuáles campos son obligatorios
- El asterisco en texto plano no tiene el mismo impacto visual que el asterisco rojo
- Inconsistencia con otros formularios del sistema causa confusión

**Validación**:
- La validación funciona correctamente (muestra errores cuando faltan campos)
- El problema es SOLO visual: falta el indicador rojo que alerta al usuario ANTES de enviar el formulario

## Próximos Pasos

1. ✅ **Tarea 5 Completada**: Test de exploración escrito y ejecutado, contraejemplos documentados
2. ⏭️ **Tarea 6**: Escribir tests de preservación para funcionalidad de formularios
3. ⏭️ **Tarea 7**: Implementar la corrección agregando `<span className="text-red-500">*</span>` a los 4 campos
4. ⏭️ **Verificación**: Re-ejecutar el test de la Tarea 5 - debe PASAR después de la corrección

## Validación del Test

✅ El test está correctamente escrito:
- Codifica el comportamiento esperado (Property 2 del diseño)
- Falla en código sin corregir (confirma que el bug existe)
- Pasará cuando se implemente la corrección
- Valida Requirements 2.1, 2.2, 5.1, 5.2, 5.3

✅ Los contraejemplos confirman la hipótesis de causa raíz del documento de diseño:
- "Omisión en el Desarrollo" ✓
- "Inconsistencia con Otros Formularios" ✓
