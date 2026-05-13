# Base de Datos — MedFlow HIS

## Estrategia

Una sola instancia de PostgreSQL (`medflow_db`) con 6 esquemas aislados, uno por microservicio. Esta estrategia se llama "Monolito Lógico" y permite ahorrar costos de infraestructura en el MVP mientras se mantiene el aislamiento lógico entre dominios.

**Regla estricta: cero JOINs entre esquemas.** Si un servicio necesita datos de otro dominio, hace una llamada HTTP al servicio correspondiente.

```sql
CREATE SCHEMA auth_schema;
CREATE SCHEMA patient_schema;
CREATE SCHEMA clinical_schema;
CREATE SCHEMA lab_schema;
CREATE SCHEMA pharmacy_schema;
CREATE SCHEMA billing_schema;
```

Redis se usa exclusivamente para la reserva atómica de slots de citas en clinical-service.

---

## auth_schema

Gestiona usuarios, roles y la relación entre ellos.

```sql
-- Usuarios del sistema (empleados y pacientes)
auth_schema.users
  id            UUID PRIMARY KEY
  username      VARCHAR(50) UNIQUE NOT NULL
  password      VARCHAR(255) NOT NULL          -- BCrypt hash
  email         VARCHAR(100) UNIQUE NOT NULL
  first_name    VARCHAR(50) NOT NULL
  second_name   VARCHAR(50)
  first_last_name  VARCHAR(50) NOT NULL
  second_last_name VARCHAR(50)
  phone         VARCHAR(15)
  active        BOOLEAN DEFAULT true
  created_at    TIMESTAMP
  updated_at    TIMESTAMP

-- Roles disponibles
auth_schema.roles
  id            BIGSERIAL PRIMARY KEY
  name          VARCHAR(50) UNIQUE NOT NULL    -- ADMIN, ADMISSION, VITAL_SIGNS, DOCTOR, LABORATORY, PHARMACY, CASHIER, PATIENT
  description   VARCHAR(255)

-- Relación usuario-rol (muchos a muchos)
auth_schema.user_roles
  user_id       UUID → users.id
  role_id       BIGINT → roles.id
  PRIMARY KEY (user_id, role_id)
```

**Datos semilla (desarrollo):**

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| admin | Admin1234 | ADMIN |
| doctor | Doctor1234 | DOCTOR |
| admision | Admision1234 | ADMISSION |

---

## patient_schema

Almacena los datos demográficos de los pacientes.

```sql
patient_schema.patients
  id              UUID PRIMARY KEY
  dpi             VARCHAR(13) UNIQUE NOT NULL   -- Documento Personal de Identificación
  nit             VARCHAR(15)                   -- Número de Identificación Tributaria
  first_name      VARCHAR(50) NOT NULL
  second_name     VARCHAR(50)
  first_last_name VARCHAR(50) NOT NULL
  second_last_name VARCHAR(50)
  email           VARCHAR(100) UNIQUE NOT NULL
  phone           VARCHAR(15)
  birth_date      DATE NOT NULL
  gender          VARCHAR(1) NOT NULL           -- M o F
  department      VARCHAR(50)
  municipality    VARCHAR(50)
  zone            VARCHAR(10)
  address         VARCHAR(200)
  created_at      TIMESTAMP
  updated_at      TIMESTAMP
```

**Nota:** El `id` del paciente en esta tabla es diferente al `id` del usuario en `auth_schema.users`. La relación entre ambos se mantiene lógicamente (el email es el mismo), pero no hay FK entre esquemas.

---

## clinical_schema

El esquema más grande. Contiene todo el flujo clínico.

```sql
-- Clínicas del hospital
clinical_schema.clinics
  id          UUID PRIMARY KEY
  name        VARCHAR(100) NOT NULL
  description VARCHAR(300)
  status      VARCHAR(20)                       -- ACTIVE, INACTIVE

-- Doctores
clinical_schema.doctors
  id          UUID PRIMARY KEY
  user_id     UUID NOT NULL                     -- Referencia lógica a auth_schema.users.id
  first_name  VARCHAR(50) NOT NULL
  last_name   VARCHAR(50) NOT NULL
  specialty   VARCHAR(100)
  clinic_id   UUID → clinics.id
  active      BOOLEAN DEFAULT true

-- Citas médicas
clinical_schema.appointments
  id               UUID PRIMARY KEY
  patient_id       UUID NOT NULL                -- Referencia lógica a patient_schema.patients.id
  doctor_id        UUID NOT NULL → doctors.id
  appointment_date DATE NOT NULL
  appointment_time TIME NOT NULL
  status           VARCHAR(30) NOT NULL         -- SCHEDULED, VITAL_SIGNS, CONSULTATION, COMPLETED, CANCELLED, MISSED
                                                -- + estados de lab: LAB_SAMPLE_COLLECTION, LAB_SAMPLE_PENDING, LAB_PROCESSING, LAB_RESULTS_READY
  notes            VARCHAR(500)
  qr_code          TEXT                         -- QR en base64
  created_at       TIMESTAMP
  created_by       UUID                         -- ID del usuario que creó la cita

-- Historial de transiciones de estado de citas (auditoría)
clinical_schema.appointment_state_transitions
  id              UUID PRIMARY KEY
  appointment_id  UUID → appointments.id
  from_state      VARCHAR(30)
  to_state        VARCHAR(30)
  transitioned_at TIMESTAMP
  transitioned_by UUID
  notes           VARCHAR(500)

-- Signos vitales
clinical_schema.vital_signs
  id                  UUID PRIMARY KEY
  appointment_id      UUID → appointments.id
  patient_id          UUID NOT NULL
  systolic_pressure   INTEGER
  diastolic_pressure  INTEGER
  heart_rate          INTEGER
  respiratory_rate    INTEGER
  temperature         DECIMAL(4,1)
  oxygen_saturation   INTEGER
  weight              DECIMAL(5,2)
  height              DECIMAL(4,2)
  bmi                 DECIMAL(5,2)              -- Calculado automáticamente
  recorded_at         TIMESTAMP
  recorded_by         UUID

-- Triajes Manchester
clinical_schema.triages
  id                    UUID PRIMARY KEY
  appointment_id        UUID → appointments.id
  patient_id            UUID NOT NULL
  motif_id              UUID → manchester_motifs.id
  priority_level        VARCHAR(20)             -- ROJO, NARANJA, AMARILLO, VERDE, AZUL
  priority_description  VARCHAR(100)
  max_wait_time_minutes INTEGER
  performed_at          TIMESTAMP
  performed_by          UUID

-- Relación triaje-discriminadores
clinical_schema.triage_discriminators
  triage_id         UUID → triages.id
  discriminator_id  UUID → manchester_discriminators.id

-- Catálogo de motivos Manchester
clinical_schema.manchester_motifs
  id          UUID PRIMARY KEY
  code        VARCHAR(50) UNIQUE
  name        VARCHAR(200) NOT NULL
  description VARCHAR(500)
  active      BOOLEAN DEFAULT true

-- Catálogo de discriminadores Manchester
clinical_schema.manchester_discriminators
  id          UUID PRIMARY KEY
  motif_id    UUID → manchester_motifs.id
  code        VARCHAR(50)
  name        VARCHAR(200) NOT NULL
  priority    VARCHAR(20)                       -- El nivel de prioridad que activa este discriminador
  active      BOOLEAN DEFAULT true

-- Consultas médicas
clinical_schema.consultations
  id                   UUID PRIMARY KEY
  appointment_id       UUID → appointments.id
  patient_id           UUID NOT NULL
  doctor_id            UUID → doctors.id
  chief_complaint      VARCHAR(500) NOT NULL
  symptoms             TEXT
  primary_diagnosis    VARCHAR(300) NOT NULL    -- Incluye código CIE-10
  secondary_diagnoses  TEXT                     -- JSON array
  medical_notes        TEXT
  treatment_plan       TEXT
  has_lab_orders       BOOLEAN DEFAULT false
  has_prescription     BOOLEAN DEFAULT false
  follow_up_date       DATE
  follow_up_time       TIME
  consultation_date    TIMESTAMP

-- Recetas médicas
clinical_schema.prescriptions
  id                UUID PRIMARY KEY
  prescription_code VARCHAR(30) UNIQUE          -- Formato: RX-YYYYMMDD-NNN
  consultation_id   UUID → consultations.id
  patient_id        UUID NOT NULL
  doctor_id         UUID → doctors.id
  status            VARCHAR(20)                 -- PENDING, DISPENSED, CANCELLED
  issued_at         TIMESTAMP

-- Medicamentos en una receta
clinical_schema.prescription_medications
  id                   UUID PRIMARY KEY
  prescription_id      UUID → prescriptions.id
  name                 VARCHAR(200) NOT NULL
  dosage               VARCHAR(100)
  frequency            VARCHAR(100)
  duration_days        INTEGER
  route                VARCHAR(50)
  special_instructions VARCHAR(500)
  dosage_amount        DECIMAL(6,2)
  dosage_unit          VARCHAR(50)
  frequency_hours      INTEGER
  total_quantity       INTEGER

-- Órdenes de laboratorio
clinical_schema.lab_orders
  id           UUID PRIMARY KEY
  order_code   VARCHAR(30) UNIQUE               -- Formato: LAB-YYYYMMDD-NNN
  consultation_id UUID → consultations.id
  patient_id   UUID NOT NULL
  doctor_id    UUID → doctors.id
  test_names   TEXT                             -- JSON array de nombres de exámenes
  status       VARCHAR(20)                      -- PENDING, IN_PROGRESS, COMPLETED
  ordered_at   TIMESTAMP
```

---

## lab_schema

```sql
-- Órdenes de laboratorio (recibidas desde clinical-service)
lab_schema.lab_orders
  id           UUID PRIMARY KEY
  order_code   VARCHAR(30) UNIQUE
  patient_id   UUID NOT NULL
  doctor_id    UUID NOT NULL
  status       VARCHAR(30)                      -- PENDING, SAMPLE_COLLECTED, SAMPLE_VALIDATED, PROCESSING, RESULTS_READY
  ordered_at   TIMESTAMP
  updated_at   TIMESTAMP

-- Exámenes dentro de una orden
lab_schema.order_tests
  id           UUID PRIMARY KEY
  order_id     UUID → lab_orders.id
  exam_type_id UUID → exam_types.id
  test_name    VARCHAR(200)
  status       VARCHAR(30)

-- Muestras
lab_schema.samples
  id             UUID PRIMARY KEY
  order_id       UUID → lab_orders.id
  collected_by   UUID
  collected_at   TIMESTAMP
  validated_by   UUID
  validated_at   TIMESTAMP
  is_valid       BOOLEAN
  notes          VARCHAR(500)

-- Resultados
lab_schema.lab_results
  id           UUID PRIMARY KEY
  order_id     UUID → lab_orders.id
  file_path    VARCHAR(500)                     -- Ruta al PDF/imagen en el volumen /app/lab-results
  file_name    VARCHAR(200)
  uploaded_by  UUID
  uploaded_at  TIMESTAMP

-- Catálogo de tipos de exámenes
lab_schema.exam_types
  id          UUID PRIMARY KEY
  code        VARCHAR(50) UNIQUE
  name        VARCHAR(200) NOT NULL
  description VARCHAR(500)
  status      VARCHAR(20)                       -- ACTIVE, INACTIVE
```

---

## pharmacy_schema

```sql
-- Catálogo de medicamentos
pharmacy_schema.medications
  id          UUID PRIMARY KEY
  code        VARCHAR(50) UNIQUE
  name        VARCHAR(200) NOT NULL
  description VARCHAR(500)
  unit        VARCHAR(50)                       -- pastilla, ml, ampolla, etc.
  stock       INTEGER DEFAULT 0
  min_stock   INTEGER DEFAULT 10               -- Alerta de stock bajo
  price       DECIMAL(10,2)
  status      VARCHAR(20)                       -- ACTIVE, INACTIVE

-- Recetas recibidas desde clinical-service
pharmacy_schema.prescriptions
  id                UUID PRIMARY KEY
  prescription_code VARCHAR(30) UNIQUE
  patient_id        UUID NOT NULL
  doctor_id         UUID NOT NULL
  status            VARCHAR(20)                 -- PENDING, DISPENSED, CANCELLED
  received_at       TIMESTAMP

-- Medicamentos en la receta
pharmacy_schema.prescription_items
  id              UUID PRIMARY KEY
  prescription_id UUID → prescriptions.id
  medication_name VARCHAR(200)
  dosage          VARCHAR(100)
  quantity        INTEGER
  dispensed       BOOLEAN DEFAULT false

-- Registro de dispensaciones
pharmacy_schema.dispensations
  id              UUID PRIMARY KEY
  prescription_id UUID → prescriptions.id
  dispensed_by    UUID
  dispensed_at    TIMESTAMP
  notes           VARCHAR(500)
```

---

## billing_schema

```sql
-- Catálogo de servicios con precios
billing_schema.service_items
  id          UUID PRIMARY KEY
  code        VARCHAR(20) UNIQUE
  name        VARCHAR(200) NOT NULL
  description VARCHAR(500)
  category    VARCHAR(50)                       -- CONSULTATION, LABORATORY, MEDICATION, OTHER
  price       DECIMAL(10,2) NOT NULL
  status      VARCHAR(20) DEFAULT 'ACTIVE'

-- Facturas
billing_schema.invoices
  id              UUID PRIMARY KEY
  invoice_number  VARCHAR(20) UNIQUE            -- Formato: FAC-YYYYMMDD-NNN
  patient_id      UUID NOT NULL
  appointment_id  UUID                          -- Referencia lógica a clinical_schema.appointments.id
  subtotal        DECIMAL(10,2)
  discount_amount DECIMAL(10,2) DEFAULT 0
  total           DECIMAL(10,2)
  status          VARCHAR(20)                   -- PENDING, PAID, CANCELLED
  customer_nit    VARCHAR(20)
  customer_name   VARCHAR(200)
  created_at      TIMESTAMP
  created_by      UUID
  updated_at      TIMESTAMP

-- Cargos de una factura
billing_schema.charges
  id          UUID PRIMARY KEY
  invoice_id  UUID → invoices.id
  type        VARCHAR(20)                       -- CONSULTATION, LABORATORY, MEDICATION, OTHER
  description VARCHAR(300) NOT NULL
  quantity    INTEGER DEFAULT 1
  unit_price  DECIMAL(10,2)
  subtotal    DECIMAL(10,2)

-- Pagos
billing_schema.payments
  id            UUID PRIMARY KEY
  invoice_id    UUID → invoices.id
  amount        DECIMAL(10,2)
  change_amount DECIMAL(10,2) DEFAULT 0
  method        VARCHAR(20)                     -- CASH, CARD, TRANSFER
  paid_at       TIMESTAMP
  received_by   UUID
```

**Datos semilla del catálogo de servicios:**

| Código | Nombre | Categoría | Precio |
|--------|--------|-----------|--------|
| CONS-GEN | Consulta General | CONSULTATION | Q150.00 |
| CONS-ESP | Consulta Especialista | CONSULTATION | Q250.00 |
| LAB-HEM | Hemograma Completo | LABORATORY | Q75.00 |
| LAB-GLU | Glucosa en Ayunas | LABORATORY | Q40.00 |
| MED-GEN | Medicamento Genérico | MEDICATION | Q25.00 |

---

## Redis

Usado exclusivamente por clinical-service para la reserva atómica de slots de citas.

**Estructura de keys:**
```
appointment:slot:{doctorId}:{date}:{time}  →  "RESERVED" o "AVAILABLE"
```

Cuando se crea una cita, el slot se marca como `RESERVED` en Redis de forma atómica (usando transacciones Redis) para evitar que dos usuarios reserven el mismo slot simultáneamente. Si la cita se cancela, el slot vuelve a `AVAILABLE`.

---

## Migraciones

Cada servicio usa Flyway para gestionar las migraciones de su esquema. Los archivos están en:
```
backend-services/{service}/src/main/resources/
├── schema.sql          ← DDL completo (tablas, índices, datos semilla)
└── db/migration/       ← Migraciones incrementales (si las hay)
```

El archivo `init-db.sql` en la raíz del proyecto crea los 6 esquemas vacíos al iniciar PostgreSQL por primera vez.
