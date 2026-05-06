# Guía de Despliegue: Integración Appointment-Billing

Esta guía documenta el proceso de despliegue de la integración automática entre Clinical Service y Billing Service para crear facturas PENDING al agendar citas médicas.

## Tabla de Contenidos

1. [Resumen de Cambios](#resumen-de-cambios)
2. [Pre-requisitos](#pre-requisitos)
3. [Orden de Despliegue](#orden-de-despliegue)
4. [Fase 1: Migraciones de Base de Datos](#fase-1-migraciones-de-base-de-datos)
5. [Fase 2: Despliegue de Billing Service](#fase-2-despliegue-de-billing-service)
6. [Fase 3: Despliegue de Clinical Service](#fase-3-despliegue-de-clinical-service)
7. [Fase 4: Monitoreo Post-Despliegue](#fase-4-monitoreo-post-despliegue)
8. [Plan de Rollback](#plan-de-rollback)
9. [Validación](#validación)

---

## Resumen de Cambios

### Clinical Service
- **Nuevas columnas**: `invoice_id` en tabla `appointments`
- **Nuevos componentes**: `BillingServiceClient`, `ConsultationPriceConfig`, `CircuitBreakerEventListener`
- **Endpoints modificados**: `POST /api/clinical/appointments` (ahora crea factura automáticamente)
- **Nuevos endpoints**: 
  - `GET /api/clinical/appointments?missingInvoice=true` (reconciliación)
  - `PATCH /api/clinical/appointments/{id}/invoice` (reconciliación manual)
- **Configuración**: Resilience4j circuit breaker y retry para Billing Service

### Billing Service
- **Nuevas columnas**: `appointment_id` en tabla `invoices`
- **DTOs modificados**: `CreateInvoiceRequest` y `InvoiceResponse` incluyen `appointmentId`
- **Lógica modificada**: `InvoiceService.createInvoice()` guarda `appointmentId`

---

## Pre-requisitos

### Base de Datos
- PostgreSQL 12+ con esquemas `clinical_schema` y `billing_schema`
- Usuario con permisos `ALTER TABLE` y `CREATE INDEX`
- Backup completo de ambos esquemas

### Servicios
- Eureka Server funcionando
- API Gateway funcionando
- Redis funcionando (para Clinical Service)

### Configuración
- Variables de entorno configuradas:
  - `BILLING_SERVICE_URL` (default: `http://localhost:8086`)
  - `BILLING_SERVICE_ENABLED` (default: `true`)

---

## Orden de Despliegue

**IMPORTANTE**: Seguir este orden estrictamente para evitar downtime y errores.

```
1. Migraciones de Base de Datos (Clinical + Billing)
   ↓
2. Desplegar Billing Service actualizado
   ↓
3. Desplegar Clinical Service actualizado
   ↓
4. Monitorear métricas por 1 hora
```

---

## Fase 1: Migraciones de Base de Datos

### 1.1 Aplicar Migración en Clinical Service

**Archivo**: `backend-services/clinical-service/src/main/resources/db/migration/V7__add_invoice_id_to_appointments.sql`

```bash
# Conectar a la base de datos
psql -h <host> -U <user> -d medflow_db

# Verificar esquema actual
\c medflow_db
SET search_path TO clinical_schema;
\d appointments

# Aplicar migración manualmente (si no usas Flyway automático)
\i backend-services/clinical-service/src/main/resources/db/migration/V7__add_invoice_id_to_appointments.sql

# Verificar que la columna fue creada
\d appointments
# Debe mostrar: invoice_id | character varying(36) |

# Verificar índice
\di idx_appointments_invoice_id
```

**Validación**:
```sql
-- Verificar que la columna existe y es nullable
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_schema = 'clinical_schema' 
  AND table_name = 'appointments' 
  AND column_name = 'invoice_id';

-- Resultado esperado:
-- column_name | data_type         | is_nullable
-- invoice_id  | character varying | YES
```

### 1.2 Aplicar Migración en Billing Service

**Archivo**: `backend-services/billing-service/src/main/resources/db/migration/V2__add_appointment_id_to_invoices.sql`

```bash
# Conectar a la base de datos
psql -h <host> -U <user> -d medflow_db

# Verificar esquema actual
SET search_path TO billing_schema;
\d invoices

# Aplicar migración
\i backend-services/billing-service/src/main/resources/db/migration/V2__add_appointment_id_to_invoices.sql

# Verificar que la columna fue creada
\d invoices
# Debe mostrar: appointment_id | character varying(36) |

# Verificar índice
\di idx_invoices_appointment_id
```

**Validación**:
```sql
-- Verificar que la columna existe y es nullable
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_schema = 'billing_schema' 
  AND table_name = 'invoices' 
  AND column_name = 'appointment_id';

-- Resultado esperado:
-- column_name    | data_type         | is_nullable
-- appointment_id | character varying | YES
```

### 1.3 Verificación de Migraciones

```sql
-- Clinical Service: Verificar versión de Flyway
SELECT * FROM clinical_schema.flyway_schema_history 
ORDER BY installed_rank DESC LIMIT 5;

-- Billing Service: Verificar versión de Flyway
SELECT * FROM billing_schema.flyway_schema_history 
ORDER BY installed_rank DESC LIMIT 5;
```

---

## Fase 2: Despliegue de Billing Service

### 2.1 Build del Servicio

```bash
cd backend-services/billing-service
mvn clean package -DskipTests
```

### 2.2 Detener Instancia Actual

```bash
# Si usas systemd
sudo systemctl stop billing-service

# Si usas Docker
docker stop billing-service

# Si usas proceso manual
kill -15 $(cat /var/run/billing-service.pid)
```

### 2.3 Desplegar Nueva Versión

```bash
# Copiar JAR
cp target/billing-service-1.0.0.jar /opt/medflow/billing-service/

# Iniciar servicio
sudo systemctl start billing-service

# O con Docker
docker run -d --name billing-service \
  -p 8086:8086 \
  -e SPRING_PROFILES_ACTIVE=production \
  medflow/billing-service:latest
```

### 2.4 Verificar Health Check

```bash
# Esperar 30 segundos para que el servicio inicie
sleep 30

# Verificar health
curl http://localhost:8086/actuator/health

# Resultado esperado:
# {"status":"UP"}

# Verificar registro en Eureka
curl http://localhost:8761/eureka/apps/BILLING-SERVICE
```

---

## Fase 3: Despliegue de Clinical Service

### 3.1 Build del Servicio

```bash
cd backend-services/clinical-service
mvn clean package -DskipTests
```

### 3.2 Configurar Variables de Entorno

```bash
# Crear archivo de configuración
cat > /opt/medflow/clinical-service/application-production.yml << EOF
billing:
  service:
    url: http://billing-service:8086
    enabled: true

consultation:
  price:
    default: 150.00
    emergency: 300.00
    followup: 100.00

rest:
  template:
    connection:
      timeout: 5000
    read:
      timeout: 5000

resilience4j:
  circuitbreaker:
    instances:
      billingService:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      billingService:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
EOF
```

### 3.3 Detener Instancia Actual

```bash
# Si usas systemd
sudo systemctl stop clinical-service

# Si usas Docker
docker stop clinical-service
```

### 3.4 Desplegar Nueva Versión

```bash
# Copiar JAR
cp target/clinical-service-1.0.0.jar /opt/medflow/clinical-service/

# Iniciar servicio
sudo systemctl start clinical-service

# O con Docker
docker run -d --name clinical-service \
  -p 8083:8083 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e BILLING_SERVICE_URL=http://billing-service:8086 \
  -e BILLING_SERVICE_ENABLED=true \
  medflow/clinical-service:latest
```

### 3.5 Verificar Health Check

```bash
# Esperar 30 segundos para que el servicio inicie
sleep 30

# Verificar health
curl http://localhost:8083/actuator/health

# Resultado esperado:
# {"status":"UP"}

# Verificar registro en Eureka
curl http://localhost:8761/eureka/apps/CLINICAL-SERVICE
```

---

## Fase 4: Monitoreo Post-Despliegue

### 4.1 Métricas a Monitorear (Primera Hora)

```bash
# Métricas de Billing Service calls
curl http://localhost:8083/actuator/metrics/billing_service_calls_total

# Métricas de citas sin factura
curl http://localhost:8083/actuator/metrics/appointments_without_invoice_total

# Métricas de duración de llamadas
curl http://localhost:8083/actuator/metrics/billing_service_call_duration_seconds

# Estado del circuit breaker
curl http://localhost:8083/actuator/circuitbreakers
```

### 4.2 Logs a Revisar

```bash
# Clinical Service logs
tail -f /var/log/medflow/clinical-service.log | grep -E "Factura|Billing|Circuit"

# Buscar logs de éxito
grep "Factura creada exitosamente" /var/log/medflow/clinical-service.log

# Buscar logs de fallback
grep "Billing Service no disponible" /var/log/medflow/clinical-service.log

# Buscar logs de circuit breaker
grep "Circuit breaker" /var/log/medflow/clinical-service.log
```

### 4.3 Pruebas Funcionales

```bash
# 1. Crear una cita y verificar que se crea factura
curl -X POST http://localhost:8083/api/clinical/appointments \
  -H "Content-Type: application/json" \
  -H "X-User-Id: admin-user-id" \
  -d '{
    "patientId": "test-patient-id",
    "doctorId": "test-doctor-id",
    "appointmentDate": "2026-04-25",
    "appointmentTime": "10:00",
    "notes": "Prueba de integración"
  }'

# Verificar respuesta - debe incluir invoiceId no nulo

# 2. Consultar citas sin factura (debe estar vacío si todo funciona)
curl http://localhost:8083/api/clinical/appointments?missingInvoice=true \
  -H "X-User-Id: admin-user-id"
```

### 4.4 Validación de Base de Datos

```sql
-- Verificar que las citas nuevas tienen invoiceId
SELECT id, patient_id, invoice_id, created_at 
FROM clinical_schema.appointments 
WHERE created_at > NOW() - INTERVAL '1 hour'
ORDER BY created_at DESC 
LIMIT 10;

-- Verificar que las facturas tienen appointmentId
SELECT id, invoice_number, patient_id, appointment_id, created_at 
FROM billing_schema.invoices 
WHERE created_at > NOW() - INTERVAL '1 hour'
ORDER BY created_at DESC 
LIMIT 10;

-- Verificar consistencia bidireccional
SELECT 
    a.id as appointment_id,
    a.invoice_id,
    i.id as invoice_id_from_billing,
    i.appointment_id
FROM clinical_schema.appointments a
LEFT JOIN billing_schema.invoices i ON a.invoice_id = i.id
WHERE a.created_at > NOW() - INTERVAL '1 hour'
  AND a.invoice_id IS NOT NULL;
```

---

## Plan de Rollback

### Escenario 1: Rollback Completo (Crítico)

Si hay problemas graves, revertir en orden inverso:

#### 1. Revertir Clinical Service

```bash
# Detener servicio actual
sudo systemctl stop clinical-service

# Restaurar versión anterior
cp /opt/medflow/clinical-service/clinical-service-1.0.0.jar.backup \
   /opt/medflow/clinical-service/clinical-service-1.0.0.jar

# Iniciar versión anterior
sudo systemctl start clinical-service
```

#### 2. Revertir Billing Service

```bash
# Detener servicio actual
sudo systemctl stop billing-service

# Restaurar versión anterior
cp /opt/medflow/billing-service/billing-service-1.0.0.jar.backup \
   /opt/medflow/billing-service/billing-service-1.0.0.jar

# Iniciar versión anterior
sudo systemctl start billing-service
```

#### 3. Revertir Migraciones (Opcional - Solo si es necesario)

```bash
# Clinical Service
psql -h <host> -U <user> -d medflow_db << EOF
SET search_path TO clinical_schema;
\i backend-services/clinical-service/src/main/resources/db/migration/V7__add_invoice_id_to_appointments_ROLLBACK.sql
EOF

# Billing Service
psql -h <host> -U <user> -d medflow_db << EOF
SET search_path TO billing_schema;
\i backend-services/billing-service/src/main/resources/db/migration/V2__add_appointment_id_to_invoices_ROLLBACK.sql
EOF
```

### Escenario 2: Desactivar Integración (Feature Flag)

Si solo quieres desactivar la integración sin hacer rollback completo:

```bash
# Opción 1: Variable de entorno
export BILLING_SERVICE_ENABLED=false
sudo systemctl restart clinical-service

# Opción 2: Actualizar application.yml
sed -i 's/enabled: true/enabled: false/' /opt/medflow/clinical-service/application-production.yml
sudo systemctl restart clinical-service
```

Con el feature flag desactivado, el Clinical Service volverá al comportamiento anterior (números de factura temporales).

---

## Validación

### Checklist de Validación Post-Despliegue

- [ ] Migraciones aplicadas correctamente en ambos esquemas
- [ ] Billing Service health check OK
- [ ] Clinical Service health check OK
- [ ] Ambos servicios registrados en Eureka
- [ ] Crear cita genera factura automáticamente
- [ ] `invoiceId` se guarda en appointment
- [ ] `appointmentId` se guarda en invoice
- [ ] Circuit breaker funciona (simular fallo de Billing Service)
- [ ] Métricas de Micrometer expuestas correctamente
- [ ] Logs estructurados funcionando
- [ ] Endpoint de reconciliación funciona
- [ ] Feature flag funciona (desactivar y verificar comportamiento)

### Comandos de Validación Rápida

```bash
# Script de validación completa
cat > /tmp/validate_deployment.sh << 'EOF'
#!/bin/bash

echo "=== Validación de Despliegue: Appointment-Billing Integration ==="

# 1. Health checks
echo -n "Clinical Service health: "
curl -s http://localhost:8083/actuator/health | jq -r '.status'

echo -n "Billing Service health: "
curl -s http://localhost:8086/actuator/health | jq -r '.status'

# 2. Métricas
echo -n "Billing calls total: "
curl -s http://localhost:8083/actuator/metrics/billing_service_calls_total | jq -r '.measurements[0].value'

echo -n "Appointments without invoice: "
curl -s http://localhost:8083/actuator/metrics/appointments_without_invoice_total | jq -r '.measurements[0].value'

# 3. Circuit breaker
echo -n "Circuit breaker state: "
curl -s http://localhost:8083/actuator/circuitbreakers | jq -r '.circuitBreakers.billingService.state'

echo "=== Validación Completada ==="
EOF

chmod +x /tmp/validate_deployment.sh
/tmp/validate_deployment.sh
```

---

## Contacto y Soporte

Para problemas durante el despliegue:
- **Equipo de DevOps**: devops@medflow.com
- **Equipo de Desarrollo**: dev@medflow.com
- **Documentación adicional**: Ver `design.md` y `requirements.md` en `.kiro/specs/appointment-billing-integration/`

---

**Última actualización**: 2026-04-24  
**Versión**: 1.0.0  
**Autor**: MedFlow Development Team
