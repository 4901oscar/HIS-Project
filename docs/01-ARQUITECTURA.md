# Arquitectura del Sistema — MedFlow HIS

## Visión general

MedFlow HIS es un sistema de microservicios. El frontend React habla únicamente con el API Gateway, que valida el JWT y enruta cada petición al microservicio correcto. Cada microservicio tiene su propio esquema en PostgreSQL y no puede hacer JOINs con otros esquemas — si necesita datos de otro dominio, hace una llamada HTTP.

```
Frontend (React)  :3000
        │
        ▼  HTTP + JWT
API Gateway       :8080   ← valida JWT, rate limiting, CORS
        │
        ├──► Auth Service      :8081   (auth_schema)
        ├──► Patient Service   :8082   (patient_schema)
        ├──► Clinical Service  :8083   (clinical_schema + Redis)
        ├──► Lab Service       :8084   (lab_schema)
        ├──► Pharmacy Service  :8085   (pharmacy_schema)
        └──► Billing Service   :8086   (billing_schema)

Eureka Server     :8761   ← registro de servicios (los servicios se descubren entre sí)
PostgreSQL        :5432   ← una instancia, 6 esquemas aislados
Redis             :6379   ← usado solo por clinical-service para reserva de slots
```

---

## Microservicios de infraestructura

### Eureka Server (puerto 8761)
Registro de servicios. Cuando un microservicio arranca, se registra aquí. El API Gateway consulta Eureka para saber a qué IP/puerto enrutar cada petición. Tiene un dashboard web en `http://localhost:8761`.

### API Gateway (puerto 8080)
Punto de entrada único para el frontend. Hace tres cosas antes de enrutar:
1. **Valida el JWT** — si el token es inválido o falta, devuelve 401 sin llegar al servicio
2. **Rate limiting** — máximo 100 peticiones por minuto por IP
3. **CORS** — permite peticiones desde `localhost:3000`

Tabla de rutas:
```
/api/auth/**        → auth-service:8081
/api/patients/**    → patient-service:8082
/api/clinical/**    → clinical-service:8083
/api/lab/**         → lab-service:8084
/api/pharmacy/**    → pharmacy-service:8085
/api/billing/**     → billing-service:8086
```

---

## Microservicios de negocio

### Auth Service (puerto 8081)
**Arquitectura**: MVC  
**Esquema**: `auth_schema`

Gestiona usuarios, roles y tokens JWT. Es el único servicio que emite tokens. Todos los demás servicios confían en el gateway para la validación — no validan el JWT ellos mismos.

Responsabilidades:
- Login de empleados y pacientes
- Emisión y revocación de JWT
- Gestión de roles (RBAC)
- Creación de cuentas de empleados (solo ADMIN)
- Creación de cuentas de pacientes (llamado internamente por patient-service)
- Activación de cuentas por email (pacientes nuevos)

Llama a: `patient-service` (para crear el registro de paciente cuando se crea una cuenta)

---

### Patient Service (puerto 8082)
**Arquitectura**: MVC  
**Esquema**: `patient_schema`

Almacena los datos demográficos de los pacientes. No maneja historial clínico — eso es responsabilidad de clinical-service.

Responsabilidades:
- Registro de pacientes (nombre, DPI, fecha de nacimiento, género, contacto, dirección)
- Búsqueda por DPI, nombre o email
- Actualización de datos personales

Llama a: `auth-service` (para crear la cuenta de usuario del paciente)

---

### Clinical Service (puerto 8083)
**Arquitectura**: Hexagonal (Puertos y Adaptadores)  
**Esquema**: `clinical_schema` + Redis

El servicio más complejo del sistema. Contiene toda la lógica médica: el motor de triaje Manchester, la gestión de citas con reserva atómica en Redis, el registro de consultas, recetas y órdenes de laboratorio.

Usa arquitectura hexagonal porque tiene lógica de negocio pesada que necesita estar aislada de la infraestructura (base de datos, Redis, HTTP clients).

Responsabilidades:
- Gestión de citas (crear, activar, cancelar, detectar ausencias automáticamente)
- Registro de signos vitales
- Triaje Manchester (algoritmo de 5 niveles de prioridad)
- Consultas médicas (diagnóstico, plan de tratamiento)
- Recetas médicas
- Órdenes de laboratorio
- Historial clínico del paciente
- Gestión de doctores y clínicas
- Catálogo de motivos y discriminadores Manchester

Llama a:
- `patient-service` — para obtener datos del paciente
- `billing-service` — para validar que el pago esté hecho antes de despachar
- `pharmacy-service` — notificación fire-and-forget cuando se genera una receta
- `lab-service` — notificación fire-and-forget cuando se genera una orden de laboratorio
- `auth-service` — para obtener el nombre completo de un usuario por ID

---

### Lab Service (puerto 8084)
**Arquitectura**: MVC  
**Esquema**: `lab_schema`

Gestiona el flujo de muestras de laboratorio desde que se recibe la orden hasta que los resultados están listos.

Responsabilidades:
- Recepción de órdenes de laboratorio (notificadas por clinical-service)
- Trazabilidad de muestras (recolección → validación → procesamiento)
- Subida de resultados en PDF/imagen
- Catálogo de tipos de exámenes

---

### Pharmacy Service (puerto 8085)
**Arquitectura**: MVC  
**Esquema**: `pharmacy_schema`

Gestiona el inventario de medicamentos y el despacho de recetas.

Responsabilidades:
- Catálogo de medicamentos con control de stock
- Recepción de recetas (notificadas por clinical-service)
- Validación de stock antes de despachar
- Registro de dispensaciones

---

### Billing Service (puerto 8086)
**Arquitectura**: MVC  
**Esquema**: `billing_schema`

Gestiona la facturación interna del hospital.

Responsabilidades:
- Catálogo de servicios con precios (consultas, exámenes, medicamentos)
- Creación de facturas con cargos detallados
- Procesamiento de pagos (efectivo, tarjeta, transferencia)
- Validación de pagos (consultada por clinical-service antes de despachar)

---

## Comunicación entre servicios

### Síncrona (HTTP / Feign Clients)
Se usa cuando el servicio necesita la respuesta para continuar:
- `clinical-service` → `patient-service`: obtener datos del paciente para incluir en la respuesta
- `clinical-service` → `billing-service`: validar que la factura esté pagada
- `patient-service` → `auth-service`: crear cuenta de usuario al registrar paciente

### Fire-and-forget (HTTP asíncrono)
Se usa cuando el servicio solo necesita notificar, no esperar respuesta:
- `clinical-service` → `pharmacy-service`: notificar nueva receta
- `clinical-service` → `lab-service`: notificar nueva orden de laboratorio

Si el servicio destino falla, un circuit breaker (Resilience4j) absorbe el error sin afectar el flujo principal.

---

## Arquitectura del código

### Hexagonal (solo clinical-service)
```
clinical-service/
├── domain/
│   ├── model/          # Entidades puras (sin anotaciones JPA)
│   ├── port/in/        # Interfaces de casos de uso (lo que el mundo exterior puede pedir)
│   ├── port/out/       # Interfaces de repositorios y clientes (lo que el dominio necesita)
│   └── service/        # Lógica de negocio pura
├── application/
│   └── usecase/        # Implementaciones de los casos de uso
└── infrastructure/
    ├── persistence/    # Adaptadores JPA (implementan los puertos out)
    ├── rest/           # Controladores REST (implementan los puertos in)
    ├── client/         # Feign clients hacia otros servicios
    └── cache/          # Adaptador Redis
```

### MVC (todos los demás servicios)
```
service-name/
├── controller/         # Endpoints REST
├── service/            # Lógica de negocio
├── repository/         # Acceso a datos (Spring Data JPA)
├── model/              # Entidades JPA
├── dto/                # Objetos de transferencia de datos
└── config/             # Configuración (seguridad, beans)
```

---

## Seguridad

- **Mecanismo**: JWT stateless
- **Emisión**: Solo auth-service emite tokens
- **Validación**: El API Gateway valida en cada petición
- **Almacenamiento en frontend**: localStorage
- **Revocación**: auth-service mantiene una blacklist de tokens revocados (logout)
- **Contraseñas**: BCrypt
- **Rate limiting en login**: 5 intentos por minuto por IP

### Estructura del JWT
```json
{
  "sub": "user-uuid",
  "username": "doctor",
  "roles": ["DOCTOR"],
  "iat": 1713024000,
  "exp": 1713110400
}
```

### Headers requeridos en cada petición
```
Authorization: Bearer {jwt_token}     ← obligatorio en todos los endpoints protegidos
Content-Type: application/json        ← en POST/PUT
X-User-Id: {user_uuid}               ← requerido por clinical-service para auditoría
```
