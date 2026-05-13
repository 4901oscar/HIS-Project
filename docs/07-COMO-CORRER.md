# Cómo Correr el Proyecto — MedFlow HIS

## Requisitos

- **Docker** y **Docker Compose** (recomendado)
- O alternativamente: Java 17+, Maven 3.8+, Node.js 18+, PostgreSQL 15, Redis 7

---

## Opción 1: Docker Compose (recomendado)

Levanta todos los servicios con un solo comando:

```bash
cd /ruta/al/proyecto
docker-compose up -d
```

Esto inicia 9 contenedores:
- PostgreSQL (con los 6 esquemas creados automáticamente)
- Redis
- Eureka Server
- API Gateway
- Auth Service
- Patient Service
- Clinical Service
- Lab Service
- Pharmacy Service
- Billing Service
- Frontend

### Verificar que todo esté corriendo

```bash
docker-compose ps
```

Todos los servicios deben mostrar estado `Up (healthy)`.

### Ver logs

```bash
# Todos los servicios
docker-compose logs -f

# Un servicio específico
docker-compose logs -f clinical-service
docker-compose logs -f api-gateway
```

### Detener todo

```bash
docker-compose down
```

Para también borrar los volúmenes (base de datos):
```bash
docker-compose down -v
```

---

## Opción 2: Desarrollo local (sin Docker)

Útil cuando quieres hacer cambios en el código y ver los resultados sin reconstruir imágenes.

### 1. Iniciar PostgreSQL y Redis

Necesitas PostgreSQL corriendo en `localhost:5432` con:
- Base de datos: `medflow_db`
- Usuario: `medflow_user`
- Contraseña: `medflow_pass`

Y Redis en `localhost:6379`.

Puedes usar Docker solo para la base de datos:
```bash
docker-compose up -d postgres redis
```

### 2. Crear los esquemas

```bash
psql -U medflow_user -d medflow_db -f init-db.sql
```

### 3. Iniciar los servicios de infraestructura

```bash
# Terminal 1: Eureka Server
cd backend-cloud/eureka-server
mvn spring-boot:run

# Terminal 2: API Gateway
cd backend-cloud/api-gateway
mvn spring-boot:run
```

Espera a que Eureka esté disponible en `http://localhost:8761` antes de continuar.

### 4. Iniciar los microservicios de negocio

Cada uno en una terminal separada:

```bash
# Auth Service
cd backend-services/auth-service
mvn spring-boot:run

# Patient Service
cd backend-services/patient-service
mvn spring-boot:run

# Clinical Service
cd backend-services/clinical-service
mvn spring-boot:run

# Lab Service
cd backend-services/lab-service
mvn spring-boot:run

# Pharmacy Service
cd backend-services/pharmacy-service
mvn spring-boot:run

# Billing Service
cd backend-services/billing-service
mvn spring-boot:run
```

### 5. Iniciar el frontend

```bash
cd frontend-medflow
npm install
npm run dev
```

El frontend estará disponible en `http://localhost:5173`.

---

## URLs de acceso

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:3000 (Docker) o http://localhost:5173 (dev) |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| Auth Service | http://localhost:8081 |
| Patient Service | http://localhost:8082 |
| Clinical Service | http://localhost:8083 |
| Lab Service | http://localhost:8084 |
| Pharmacy Service | http://localhost:8085 |
| Billing Service | http://localhost:8086 |

---

## Verificar que los servicios funcionan

### Health checks

```bash
curl http://localhost:8080/actuator/health   # API Gateway
curl http://localhost:8081/actuator/health   # Auth Service
curl http://localhost:8082/actuator/health   # Patient Service
curl http://localhost:8083/actuator/health   # Clinical Service
```

Todos deben responder `{"status":"UP"}`.

### Probar el login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin1234"}'
```

Debe devolver un JSON con `token` y `user`.

### Probar un endpoint protegido

```bash
# Primero obtén el token del login
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Buscar pacientes
curl http://localhost:8080/api/patients/search?query=Juan \
  -H "Authorization: Bearer $TOKEN"
```

---

## Credenciales de demo

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| admin | Admin1234 | ADMIN |
| doctor | Doctor1234 | DOCTOR |
| admision | Admision1234 | ADMISSION |

Para crear usuarios con otros roles (VITAL_SIGNS, LABORATORY, PHARMACY, CASHIER), usa el panel de administrador con la cuenta `admin`.

---

## Variables de entorno

El archivo `.env.example` en la raíz muestra todas las variables disponibles. Para desarrollo local, copia y ajusta:

```bash
cp .env.example .env
```

Variables importantes:

```env
JWT_SECRET=tu-clave-secreta-de-al-menos-256-bits
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password
APP_FRONTEND_URL=http://localhost:5173
VITE_API_GATEWAY_URL=http://localhost:8080
```

El `JWT_SECRET` debe ser el mismo en el API Gateway y en el Auth Service.

---

## Reconstruir imágenes Docker

Si haces cambios en el código backend:

```bash
# Reconstruir un servicio específico
docker-compose up -d --build clinical-service

# Reconstruir todo
docker-compose up -d --build
```

---

## Solución de problemas comunes

### Los servicios no arrancan

```bash
# Ver qué está fallando
docker-compose logs -f auth-service

# Reiniciar un servicio
docker-compose restart auth-service
```

### Error CORS en el frontend

Verifica que el frontend esté corriendo en `http://localhost:3000` (Docker) o `http://localhost:5173` (dev). El API Gateway tiene CORS configurado para ambos orígenes.

### Error 401 en todas las peticiones

El token JWT expiró. Haz login nuevamente. Los tokens tienen una duración de 24 horas.

### Error 429 Too Many Requests

El rate limiter bloqueó la IP por exceder 100 peticiones por minuto. Espera 1 minuto.

### Clinical Service no conecta con otros servicios

Verifica que todos los servicios estén registrados en Eureka (`http://localhost:8761`). Clinical Service necesita que patient-service, billing-service, pharmacy-service y lab-service estén disponibles.

### Base de datos vacía después de reiniciar

Los datos persisten en el volumen Docker `postgres_data`. Si hiciste `docker-compose down -v`, los datos se borraron. Los datos semilla (usuarios admin/doctor/admision y catálogo de servicios) se recrean automáticamente al iniciar.

---

## Correr los tests

### Backend (cada servicio)

```bash
cd backend-services/clinical-service
mvn test
```

Clinical Service tiene 105 tests incluyendo 17 property-based tests con jqwik.

### Frontend

```bash
cd frontend-medflow
npm run test
```

---

## Git Flow

El proyecto usa Git Flow:

- `main` — código en producción
- `develop` — integración de features
- `feature/*` — nuevas funcionalidades
- `hotfix/*` — fixes urgentes

```bash
# Crear un feature branch
git checkout develop
git checkout -b feature/mi-nueva-funcionalidad

# Hacer commit
git add .
git commit -m "feat: descripción de la funcionalidad"

# Push
git push origin feature/mi-nueva-funcionalidad
```

Convenciones de commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
