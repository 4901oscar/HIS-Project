# Migration Guide: Appointment-Billing Integration

## Overview

Este documento describe cómo aplicar las migraciones de base de datos para la integración automática entre citas y facturación.

**Feature**: appointment-billing-integration  
**Requirements**: 5.1, 5.2, 10.1-10.8, 11.1

## Cambios en Base de Datos

### Clinical Service (clinical_schema)
- **Nueva columna**: `appointments.invoice_id` (VARCHAR 36, nullable)
- **Nuevo índice**: `idx_appointments_invoice_id`
- **Propósito**: Almacenar referencia lógica a la factura creada en Billing Service

### Billing Service (billing_schema)
- **Nueva columna**: `invoices.appointment_id` (VARCHAR 36, nullable)
- **Nuevo índice**: `idx_invoices_appointment_id`
- **Propósito**: Almacenar referencia lógica a la cita que generó la factura

## Orden de Aplicación (Zero Downtime)

### Fase 1: Aplicar Migraciones de Base de Datos

**IMPORTANTE**: Aplicar migraciones ANTES de desplegar código actualizado.

#### 1.1 Clinical Service Migration

```bash
# Opción A: Flyway automático (recomendado)
# La migración se aplicará automáticamente al iniciar el servicio
# Archivo: clinical-service/src/main/resources/db/migration/V7__add_invoice_id_to_appointments.sql

# Opción B: Aplicación manual
psql -h <host> -U <user> -d <database> -f backend-services/clinical-service/src/main/resources/db/migration/V7__add_invoice_id_to_appointments.sql
```

**Verificación**:
```sql
-- Verificar que la columna existe
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns
WHERE table_schema = 'clinical_schema' 
AND table_name = 'appointments' 
AND column_name = 'invoice_id';

-- Verificar que el índice existe
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE schemaname = 'clinical_schema' 
AND tablename = 'appointments' 
AND indexname = 'idx_appointments_invoice_id';
```

#### 1.2 Billing Service Migration

```bash
# Opción A: Schema.sql automático (actual)
# El schema.sql actualizado se aplicará al iniciar el servicio
# Archivo: billing-service/src/main/resources/schema.sql

# Opción B: Aplicación manual (si ya existe la tabla)
ALTER TABLE billing_schema.invoices ADD COLUMN IF NOT EXISTS appointment_id VARCHAR(36) NULL;
CREATE INDEX IF NOT EXISTS idx_invoices_appointment_id ON billing_schema.invoices(appointment_id);
COMMENT ON COLUMN billing_schema.invoices.appointment_id IS 'Logical FK to clinical_schema.appointments.id';
```

**Verificación**:
```sql
-- Verificar que la columna existe
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns
WHERE table_schema = 'billing_schema' 
AND table_name = 'invoices' 
AND column_name = 'appointment_id';

-- Verificar que el índice existe
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE schemaname = 'billing_schema' 
AND tablename = 'invoices' 
AND indexname = 'idx_invoices_appointment_id';
```

### Fase 2: Desplegar Billing Service Actualizado

```bash
# Desplegar versión actualizada de Billing Service
# Cambios: InvoiceService acepta appointmentId, InvoiceResponse incluye appointmentId
cd backend-services/billing-service
mvn clean package
docker build -t billing-service:latest .
docker-compose up -d billing-service
```

**Verificación**:
```bash
# Verificar que el servicio inició correctamente
curl http://localhost:8086/actuator/health

# Verificar backward compatibility (crear factura sin appointmentId)
curl -X POST http://localhost:8086/api/billing/invoices \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user" \
  -d '{
    "patientId": "patient-123",
    "charges": [{
      "type": "CONSULTATION",
      "description": "Test",
      "quantity": 1,
      "unitPrice": 150.00
    }]
  }'
```

### Fase 3: Desplegar Clinical Service Actualizado

```bash
# Desplegar versión actualizada de Clinical Service
# Cambios: BillingServiceClient, AppointmentController integración
cd backend-services/clinical-service
mvn clean package
docker build -t clinical-service:latest .
docker-compose up -d clinical-service
```

**Verificación**:
```bash
# Verificar que el servicio inició correctamente
curl http://localhost:8083/actuator/health

# Verificar métricas de circuit breaker
curl http://localhost:8083/actuator/metrics/resilience4j.circuitbreaker.state

# Crear una cita de prueba y verificar que se crea la factura
curl -X POST http://localhost:8083/api/clinical/appointments \
  -H "Content-Type: application/json" \
  -H "X-User-Id: test-user" \
  -d '{
    "patientId": "patient-123",
    "doctorId": "doctor-456",
    "appointmentDate": "2026-05-01",
    "appointmentTime": "10:00",
    "notes": "Test integration"
  }'
```

### Fase 4: Monitoreo Post-Despliegue

Monitorear durante 1 hora después del despliegue:

```bash
# Métricas clave a monitorear
curl http://localhost:8083/actuator/metrics/billing_service_calls_total
curl http://localhost:8083/actuator/metrics/appointments_without_invoice_total
curl http://localhost:8083/actuator/metrics/resilience4j.circuitbreaker.state

# Logs a revisar
docker logs clinical-service | grep "Invoice created successfully"
docker logs clinical-service | grep "Billing Service unavailable"
docker logs clinical-service | grep "Circuit breaker"
```

## Plan de Rollback

### Si hay problemas con la integración:

#### Opción 1: Desactivar integración con feature flag (RECOMENDADO)

```yaml
# En clinical-service application.yml
billing:
  service:
    enabled: false  # Desactiva la integración, vuelve a comportamiento anterior
```

```bash
# Reiniciar Clinical Service con flag desactivado
docker-compose restart clinical-service
```

#### Opción 2: Rollback de código

```bash
# Revertir a versión anterior de Clinical Service
docker-compose down clinical-service
docker pull clinical-service:previous-version
docker-compose up -d clinical-service
```

#### Opción 3: Rollback de base de datos (ÚLTIMO RECURSO)

**ADVERTENCIA**: Esto eliminará todas las referencias entre citas y facturas.

```bash
# Clinical Service
psql -h <host> -U <user> -d <database> -f backend-services/clinical-service/src/main/resources/db/migration/V7__add_invoice_id_to_appointments_ROLLBACK.sql

# Billing Service
psql -h <host> -U <user> -d <database> -f backend-services/billing-service/src/main/resources/db/migration/V2__add_appointment_id_to_invoices_ROLLBACK.sql
```

## Reconciliación de Datos (Opcional)

Si hay citas creadas antes de la integración que necesitan ser vinculadas a facturas:

```sql
-- Script de reconciliación manual (ejecutar con precaución)
-- Vincular citas a facturas por patientId y fecha cercana

UPDATE clinical_schema.appointments a
SET invoice_id = i.id
FROM billing_schema.invoices i
WHERE a.patient_id = i.patient_id
AND a.invoice_id IS NULL
AND i.appointment_id IS NULL
AND i.status = 'PENDING'
AND DATE(a.created_at) = DATE(i.created_at)
AND ABS(EXTRACT(EPOCH FROM (a.created_at - i.created_at))) < 300; -- Dentro de 5 minutos

-- Actualizar referencias inversas
UPDATE billing_schema.invoices i
SET appointment_id = a.id
FROM clinical_schema.appointments a
WHERE i.id = a.invoice_id
AND i.appointment_id IS NULL;
```

## Checklist de Despliegue

- [ ] Backup de base de datos realizado
- [ ] Migraciones aplicadas en Clinical Service
- [ ] Migraciones aplicadas en Billing Service
- [ ] Verificación de columnas e índices completada
- [ ] Billing Service desplegado y verificado
- [ ] Clinical Service desplegado y verificado
- [ ] Smoke tests ejecutados exitosamente
- [ ] Métricas monitoreadas por 1 hora
- [ ] Sin alertas críticas generadas
- [ ] Documentación actualizada

## Contacto

Para problemas durante el despliegue, contactar al equipo de MedFlow:
- **Slack**: #medflow-deployments
- **Email**: devops@medflow.com

---

**Creado**: Abril 24, 2026  
**Versión**: 1.0.0  
**Feature**: appointment-billing-integration
