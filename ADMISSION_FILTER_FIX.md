# Fix: Filtro de Citas en Portal de Admisión

## Problema Identificado

El usuario reportó dos problemas en el portal de admisión:

1. **Cita nueva (SCHEDULED) no aparece**: La cita creada para mañana (28/04 a las 16:00) no se muestra en la lista de admisión
2. **Cita en VITAL_SIGNS aparece incorrectamente**: Las citas que ya están en proceso de triaje (VITAL_SIGNS) aparecen en admisión cuando solo deberían estar en el portal de triaje

## Análisis

### Problema 1: Cita de mañana no aparece
- **Causa**: El endpoint `/api/clinical/appointments/today` solo retorna citas del **día actual**
- **Comportamiento esperado**: Correcto - solo debe mostrar citas de hoy
- **Solución**: No requiere cambio - es el comportamiento correcto

### Problema 2: Citas en VITAL_SIGNS aparecen en admisión
- **Causa**: El frontend mostraba **TODAS** las citas del día sin filtrar por estado
- **Comportamiento esperado**: Solo mostrar citas en estado **SCHEDULED** (listas para activar)
- **Impacto**: Confusión en el personal de admisión al ver citas que ya están siendo atendidas en triaje

## Solución Implementada

### Cambio en Frontend

**Archivo**: `frontend-medflow/src/pages/admission/ActivateAppointments.tsx`

**Antes**:
```typescript
{allAppointments.length === 0 ? (
  <p className="text-gray-500 text-sm">No hay citas para hoy.</p>
) : (
  // Mostraba TODAS las citas
  {allAppointments
    .slice()
    .sort((a, b) => a.appointmentTime.localeCompare(b.appointmentTime))
    .map((appt) => {
```

**Después**:
```typescript
{allAppointments.filter(appt => appt.status === 'SCHEDULED').length === 0 ? (
  <p className="text-gray-500 text-sm">No hay citas pendientes de activación para hoy.</p>
) : (
  // Filtra solo citas SCHEDULED
  {allAppointments
    .filter(appt => appt.status === 'SCHEDULED')
    .slice()
    .sort((a, b) => a.appointmentTime.localeCompare(b.appointmentTime))
    .map((appt) => {
```

## Comportamiento Correcto Ahora

### Portal de Admisión (Tab "Ver Citas")
✅ Muestra solo citas en estado **SCHEDULED**
✅ Muestra solo citas del **día actual**
✅ Permite activar citas con pago confirmado
❌ NO muestra citas en VITAL_SIGNS (están en triaje)
❌ NO muestra citas de otros días

### Portal de Triaje (Triaje Pendiente)
✅ Muestra solo citas en estado **VITAL_SIGNS**
✅ Todas las citas son editables (sin bloqueo)
✅ Permite capturar signos vitales

## Estados de Citas en el Sistema

Según la base de datos actual:

| ID | Fecha | Hora | Estado | Ubicación Correcta |
|----|-------|------|--------|-------------------|
| `2c2c7759...` | 28/04 | 16:00 | SCHEDULED | No aparece (es de mañana) |
| `0250596b...` | 27/04 | 21:30 | VITAL_SIGNS | Portal de Triaje ✅ |
| `928ddadb...` | 27/04 | 17:00 | ACTIVE | Ninguno (estado legacy) |

## Flujo Correcto

1. **Crear cita** → Estado: `PENDING_PAYMENT` o `SCHEDULED`
2. **Portal de Admisión** → Muestra solo `SCHEDULED` del día actual
3. **Activar cita** → Transición: `SCHEDULED` → `VITAL_SIGNS`
4. **Portal de Triaje** → Muestra solo `VITAL_SIGNS`
5. **Capturar signos vitales** → Transición: `VITAL_SIGNS` → `CONSULTATION`

## Testing

Para verificar el fix:

1. **Verificar que citas SCHEDULED aparecen en admisión**:
   - Crear una cita para hoy
   - Confirmar pago en caja
   - Verificar que aparece en "Ver Citas" de admisión

2. **Verificar que citas VITAL_SIGNS NO aparecen en admisión**:
   - Activar una cita desde admisión
   - Verificar que desaparece de la lista de admisión
   - Verificar que aparece en el portal de triaje

3. **Verificar que citas de otros días NO aparecen**:
   - Crear una cita para mañana
   - Verificar que NO aparece en "Ver Citas" de admisión hoy

## Archivos Modificados

- `frontend-medflow/src/pages/admission/ActivateAppointments.tsx`

## Deployment

✅ Frontend reconstruido
✅ Contenedor reiniciado
✅ Cambios aplicados

---

**Fecha**: 27 de abril de 2026
**Estado**: Completado
**Servicios Afectados**: frontend
