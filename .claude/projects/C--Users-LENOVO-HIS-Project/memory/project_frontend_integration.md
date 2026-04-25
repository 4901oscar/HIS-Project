---
name: Frontend-Backend Integration Status
description: Estado de la integración completa del frontend con el backend (completada en abril 2026)
type: project
---

Integración frontend-backend completada. Build exitoso sin errores TypeScript.

**Why:** El frontend tenía todas las páginas de roles como stubs con mock data. Se integró completamente con los servicios reales.

**How to apply:** Al hacer cambios en los servicios del backend, actualizar los tipos en los archivos de servicio correspondientes del frontend.

## Archivos creados/modificados

### Infraestructura nueva
- `frontend-medflow/src/api/index.ts` — cliente Axios compartido con interceptor JWT y redirect 401 automático

### Servicios nuevos/actualizados
- `services/authService.ts` — usa cliente compartido
- `services/patientService.ts` — reescrito con API real (createPatient, getByDpi, search)
- `services/appointmentService.ts` — reescrito con API real (create, activate, cancel)
- `services/clinicalService.ts` — NUEVO: vital signs, consultations, prescriptions, lab orders, history
- `services/labService.ts` — NUEVO: getOrders, collectSample, uploadResult
- `services/pharmacyService.ts` — NUEVO: getPrescriptions, dispense
- `services/billingService.ts` — NUEVO: create/list invoices, process payment, apply discount
- `services/index.ts` — actualizado para exportar todos los servicios sin conflictos

### Páginas implementadas
- `admission/ActivateAppointments.tsx` — dos tabs: registrar paciente (POST /api/patients) + activar cita por ID
- `vitals/VitalSignsCapture.tsx` — buscar paciente + formulario signos vitales
- `doctor/DoctorConsultation.tsx` — flujo: buscar paciente → consulta → receta → orden lab
- `lab/LabSampleManagement.tsx` — listar órdenes + recolectar muestra + subir PDF resultado
- `pharmacy/PharmacyDispense.tsx` — listar prescripciones por estado + dispensar
- `cashier/CashierBilling.tsx` — listar/crear facturas + procesar pago + cancelar
- `patient/PatientDashboard.tsx` — historial clínico real (busca por DPI → GET /api/clinical/history)

## Limitación importante
El backend NO tiene GET /api/clinical/appointments (listar todas las citas).
Por eso ActivateAppointments usa input de ID de cita para activar, no una tabla.
