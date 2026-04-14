# Requirements: Auth Service

## 1. Overview

El **Auth Service** es el microservicio responsable de la autenticación y autorización en el sistema MedFlow HIS. Maneja el login de staff y pacientes, emisión de tokens JWT, y gestión de roles (RBAC).

**Arquitectura**: MVC (Capas) - CRUD simple  
**Base de Datos**: auth_schema (PostgreSQL)  
**Puerto**: 8081  
**Registro en Eureka**: AUTH-SERVICE

## 2. User Stories

### US-1: Login de Staff (Empleados)
**Como** empleado del hospital,  
**Quiero** iniciar sesión con mi username y contraseña,  
**Para** acceder al sistema según mi rol.

**Acceptance Criteria:**
- [ ] Puedo ingresar username y contraseña
- [ ] El sistema valida las credenciales
- [ ] Si son correctas, recibo un JWT con mi userId y roles
- [ ] Si son incorrectas, recibo error 401 Unauthorized
- [ ] El JWT tiene duración de 24 horas

### US-2: Login de Paciente
**Como** paciente,  
**Quiero** iniciar sesión con mi Email/DPI y contraseña temporal,  
**Para** acceder a mi portal del paciente.

**Acceptance Criteria:**
- [ ] Puedo usar Email o DPI como username
- [ ] Puedo ingresar la contraseña temporal que me dieron en admisión
- [ ] Si son correctas, recibo un JWT con mi userId y rol PATIENT
- [ ] Si son incorrectas, recibo error 401 Unauthorized
- [ ] El JWT tiene duración de 24 horas

### US-3: Validación de JWT
**Como** API Gateway,  
**Quiero** validar tokens JWT,  
**Para** verificar que el usuario está autenticado.

**Acceptance Criteria:**
- [ ] Puedo enviar un JWT para validación
- [ ] El servicio verifica la firma del token
- [ ] El servicio verifica que no esté expirado
- [ ] Retorna los claims del token (userId, roles)
- [ ] Retorna 401 si el token es inválido o expirado

### US-4: Refresh Token
**Como** usuario autenticado,  
**Quiero** renovar mi token antes de que expire,  
**Para** no tener que hacer login nuevamente.

**Acceptance Criteria:**
- [ ] Puedo enviar mi JWT actual para renovarlo
- [ ] Si es válido, recibo un nuevo JWT con nueva expiración
- [ ] Si está expirado, recibo error 401
- [ ] El nuevo JWT mantiene los mismos claims (userId, roles)

### US-5: Logout
**Como** usuario autenticado,  
**Quiero** cerrar sesión,  
**Para** invalidar mi token.

**Acceptance Criteria:**
- [ ] Puedo enviar mi JWT para hacer logout
- [ ] El token se agrega a una blacklist
- [ ] Intentos posteriores de usar ese token fallan
- [ ] Recibo confirmación de logout exitoso

## 3. Functional Requirements

### FR1: Gestión de Usuarios (Staff)
El servicio DEBE almacenar:

**Datos de Usuario**:
- Username (único, requerido)
- Password (hasheado con BCrypt, requerido)
- Email (único, requerido)
- Nombre completo
- Roles (uno o más de los 8 roles del sistema)
- Estado (Activo, Inactivo)
- Fecha de creación
- Fecha de última actualización

**8 Roles del Sistema (RBAC)**:
1. `ADMIN` - Súper Usuario
2. `ADMISSION` - Admisión
3. `VITAL_SIGNS` - Signos Vitales
4. `DOCTOR` - Doctor
5. `LABORATORY` - Laboratorio
6. `PHARMACY` - Farmacia
7. `CASHIER` - Caja
8. `PATIENT` - Paciente

### FR2: Autenticación
El servicio DEBE:
- Aceptar username/password para staff
- Aceptar Email o DPI + password para pacientes
- Validar credenciales contra la base de datos
- Hashear contraseñas con BCrypt (strength 10)
- Generar JWT con claims: `userId`, `roles`, `exp`
- JWT válido por 24 horas
- Usar secret key compartido con API Gateway

### FR3: Emisión de JWT
El JWT DEBE contener:
```json
{
  "sub": "user123",
  "userId": "123",
  "username": "doctor1",
  "roles": "DOCTOR,ADMIN",
  "iat": 1713024000,
  "exp": 1713110400
}
```

### FR4: Validación de JWT
El servicio DEBE:
- Verificar firma del token
- Verificar que no esté expirado
- Verificar que no esté en blacklist
- Extraer y retornar claims

### FR5: Blacklist de Tokens
El servicio DEBE:
- Mantener lista de tokens invalidados (logout)
- Almacenar en Redis (opcional fase 1: en memoria)
- Limpiar tokens expirados automáticamente

### FR6: Endpoints REST

| Método | Endpoint | Descripción | Autenticación |
|--------|----------|-------------|---------------|
| POST | /api/auth/login | Login de staff o paciente | No |
| POST | /api/auth/logout | Cerrar sesión | Sí (JWT) |
| POST | /api/auth/refresh | Renovar token | Sí (JWT) |
| GET | /api/auth/validate | Validar token | No |
| GET | /api/auth/me | Obtener info del usuario actual | Sí (JWT) |

## 4. Non-Functional Requirements

### NFR1: Performance
- **Tiempo de respuesta**: < 200ms para login
- **Tiempo de respuesta**: < 50ms para validación de JWT
- **Throughput**: Soportar 1000 peticiones/segundo

### NFR2: Security
- Contraseñas hasheadas con BCrypt (strength 10)
- JWT firmado con HS256
- Secret key de 256 bits mínimo
- No loggear contraseñas ni tokens completos
- Rate limiting: máximo 5 intentos de login por minuto por IP

### NFR3: Availability
- **Uptime**: 99.9%
- **Startup time**: < 20 segundos

### NFR4: Scalability
- Stateless (excepto blacklist de tokens)
- Soportar escalamiento horizontal
- Blacklist compartida vía Redis (fase 2)

## 5. Business Rules

### BR1: Autenticación de Staff
- Username es único en el sistema
- Password debe tener mínimo 8 caracteres
- Un usuario puede tener múltiples roles

### BR2: Autenticación de Paciente
- Paciente puede usar Email o DPI como username
- Contraseña temporal generada por patient-service
- Paciente solo tiene rol PATIENT

### BR3: JWT
- Duración: 24 horas
- No renovable después de expirado
- Mismo secret key en todos los servicios

### BR4: Logout
- Token invalidado se agrega a blacklist
- Blacklist se limpia automáticamente de tokens expirados

### BR5: Comunicación entre Servicios
- **CERO JOINs** con otros esquemas
- Para validar paciente, llamar a patient-service vía HTTP

## 6. Constraints

### C1: Tecnología
- DEBE usar Spring Boot 3.2.4
- DEBE usar Java 17
- DEBE usar Spring Security
- DEBE usar JJWT 0.11.5
- DEBE usar PostgreSQL

### C2: Arquitectura
- DEBE usar arquitectura MVC (Capas)
- DEBE registrarse en Eureka como AUTH-SERVICE
- DEBE exponer health check en /actuator/health

### C3: Base de Datos
- DEBE usar esquema `auth_schema` en PostgreSQL
- NO DEBE hacer JOINs con otros esquemas

## 7. Dependencies

### D1: API Gateway
- **Tipo**: Infraestructura
- **Estado**: ✅ Implementado
- **Necesario para**: Enrutamiento

### D2: Eureka Server
- **Tipo**: Infraestructura
- **Estado**: ✅ Implementado
- **Necesario para**: Service Discovery

### D3: PostgreSQL
- **Tipo**: Base de Datos
- **Estado**: ⏳ Pendiente
- **Necesario para**: Almacenamiento

### D4: Patient Service (para testing)
- **Tipo**: Microservicio
- **Estado**: ⏳ Pendiente
- **Necesario para**: Validar login de pacientes

## 8. Out of Scope

### No incluido en esta versión:
- ❌ OAuth2 / OpenID Connect - Solo JWT
- ❌ Autenticación Biométrica - Descartada del MVP
- ❌ Two-Factor Authentication (2FA) - Versión futura
- ❌ Password reset por email - Versión futura
- ❌ Social login (Google, Facebook) - No necesario
- ❌ LDAP/Active Directory integration - No necesario

## 9. Success Metrics

- **Tiempo de login**: < 200ms
- **Tiempo de validación**: < 50ms
- **Error rate**: < 1%
- **Availability**: > 99.9%

---

**Created**: April 13, 2026  
**Author**: MedFlow Team  
**Status**: ✅ Approved  
**Version**: 1.0.0
