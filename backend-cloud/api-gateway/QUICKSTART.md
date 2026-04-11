# API Gateway - Guía de Inicio Rápido

## 🚀 Inicio Rápido (5 minutos)

### Prerequisitos

- Java 17 o superior
- Maven 3.8+
- Eureka Server corriendo en puerto 8761

Verifica tu instalación:
```bash
java -version
mvn -version
```

### Paso 1: Iniciar Eureka Server

El API Gateway necesita que Eureka Server esté corriendo primero.

```bash
# En otra terminal
cd backend-cloud/eureka-server
mvn spring-boot:run
```

Verifica que Eureka esté corriendo: http://localhost:8761

### Paso 2: Navegar al directorio

```bash
cd backend-cloud/api-gateway
```

### Paso 3: Compilar el proyecto

```bash
mvn clean install
```

Esto descargará todas las dependencias y compilará el proyecto.

### Paso 4: Ejecutar API Gateway

```bash
mvn spring-boot:run
```

### Paso 5: Verificar que está corriendo

Abre tu navegador en: **http://localhost:8080/actuator/health**

Respuesta esperada:
```json
{
  "status": "UP",
  "components": {
    "discoveryComposite": {
      "status": "UP"
    }
  }
}
```

### Paso 6: Verificar registro en Eureka

Abre: **http://localhost:8761**

Deberías ver:
```
Instances currently registered with Eureka:
- API-GATEWAY
  Status: UP
  Instance: api-gateway:8080
```

## 🎉 ¡Listo!

API Gateway está corriendo y registrado en Eureka, listo para enrutar peticiones.

## 🧪 Probar el Gateway

### Test 1: Health Check (Ruta Pública)

```bash
curl http://localhost:8080/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### Test 2: Ruta Protegida sin JWT (Debe fallar)

```bash
curl http://localhost:8080/api/patients/123
```

Respuesta esperada:
```json
{
  "error": "Unauthorized",
  "message": "Token not provided"
}
```

### Test 3: Ruta Protegida con JWT (Requiere auth-service)

```bash
# Primero obtener token (requiere auth-service corriendo)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"doctor1","password":"password123"}'

# Usar el token
curl http://localhost:8080/api/patients/123 \
  -H "Authorization: Bearer <token-aqui>"
```

### Test 4: Rate Limiting

```bash
# Hacer 101 peticiones rápidas
for i in {1..101}; do
  curl http://localhost:8080/actuator/health
done

# La petición 101 debería retornar 429
```

### Test 5: CORS (Desde navegador)

```javascript
// En consola del navegador (http://localhost:3000)
fetch('http://localhost:8080/actuator/health')
  .then(r => r.json())
  .then(console.log)
```

## 📊 Comandos Comunes

### Compilar

```bash
mvn clean install
```

### Ejecutar

```bash
mvn spring-boot:run
```

### Ejecutar con perfil Docker

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### Ejecutar tests

```bash
mvn test
```

### Ver cobertura de tests

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### Construir JAR

```bash
mvn clean package
java -jar target/api-gateway-1.0.0.jar
```

### Construir Docker image

```bash
docker build -t medflow/api-gateway:1.0.0 .
```

### Ejecutar con Docker

```bash
docker run -p 8080:8080 \
  -e EUREKA_SERVER_URL=http://host.docker.internal:8761/eureka/ \
  -e JWT_SECRET=your-secret-key \
  medflow/api-gateway:1.0.0
```

## 🛑 Detener el Gateway

Presiona `Ctrl + C` en la terminal donde está corriendo.

## 🐛 Problemas Comunes

### Error: "Port 8080 is already in use"

**Solución**: Otro proceso está usando el puerto 8080.

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>
```

### Error: "Connection refused to Eureka"

**Solución**: Eureka Server no está corriendo.

```bash
# Iniciar Eureka primero
cd backend-cloud/eureka-server
mvn spring-boot:run
```

### Error: "JAVA_HOME not set"

**Solución**: Configura JAVA_HOME

```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-17

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17
```

### Error: Maven no encontrado

**Solución**: Instala Maven o usa el wrapper incluido

```bash
# Si tienes mvnw (Maven Wrapper)
./mvnw spring-boot:run  # Linux/Mac
mvnw.cmd spring-boot:run  # Windows
```

### Error: "JWT validation failed"

**Solución**: Verifica que el JWT secret sea el mismo en todos los servicios.

```bash
# Verificar en application.yml
jwt:
  secret: same-secret-in-all-services
```

## 📝 Logs

Los logs aparecerán en la consola:

```
2026-04-10 20:00:00 - Starting GatewayApplication
2026-04-10 20:00:05 - Tomcat started on port(s): 8080
2026-04-10 20:00:05 - Started GatewayApplication in 8.5 seconds
2026-04-10 20:00:06 - DiscoveryClient_API-GATEWAY registered
```

### Logs de Peticiones

```
2026-04-10 20:01:00 - [DEBUG] Request: GET /api/patients/123
2026-04-10 20:01:00 - [DEBUG] JWT validated for user: 123
2026-04-10 20:01:00 - [DEBUG] Routing to: patient-service
2026-04-10 20:01:00 - [DEBUG] Response: 200 OK (45ms)
```

## 🔍 Verificar Configuración

### Ver servicios registrados en Eureka

```bash
curl http://localhost:8761/eureka/apps
```

### Ver métricas del gateway

```bash
curl http://localhost:8080/actuator/metrics
```

### Ver métricas de peticiones

```bash
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## 📊 Dashboard de Eureka

Abre http://localhost:8761 para ver:

```
┌─────────────────────────────────────────┐
│         Eureka Dashboard                │
├─────────────────────────────────────────┤
│ System Status: UP                       │
│                                         │
│ Instances currently registered:         │
│                                         │
│ API-GATEWAY                             │
│   Status: UP                            │
│   Instance: api-gateway:8080            │
│                                         │
│ (Otros servicios aparecerán aquí)      │
└─────────────────────────────────────────┘
```

## ⏭️ Próximos Pasos

1. ✅ API Gateway corriendo
2. ✅ Registrado en Eureka
3. ⏳ Implementar Auth Service para generar JWTs
4. ⏳ Implementar Patient Service para probar enrutamiento
5. ⏳ Probar flujo completo: Login → JWT → Petición protegida

## 🔗 URLs Útiles

- **Gateway**: http://localhost:8080
- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Eureka Dashboard**: http://localhost:8761

## 💡 Tips

1. **Inicia Eureka primero**: El gateway necesita registrarse en Eureka
2. **Revisa los logs**: Son muy útiles para debugging
3. **Usa health checks**: Para verificar que todo está funcionando
4. **Prueba con curl**: Antes de integrar con frontend
5. **Verifica Eureka**: Para confirmar que el gateway está registrado

## 🎓 Conceptos Clave

- **API Gateway**: Punto de entrada único para todos los servicios
- **Service Discovery**: El gateway descubre servicios vía Eureka
- **JWT Validation**: Valida tokens antes de enrutar
- **Rate Limiting**: Protege contra abuso (100 req/min)
- **CORS**: Permite peticiones desde localhost:3000
- **Load Balancing**: Distribuye peticiones entre instancias

## 🔐 Seguridad

### Rutas Públicas (No requieren JWT)

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /actuator/health`
- `GET /actuator/info`

### Rutas Protegidas (Requieren JWT)

Todas las demás rutas requieren:
```
Authorization: Bearer <jwt-token>
```

## 📈 Enrutamiento

| Path | Servicio | Puerto |
|------|----------|--------|
| `/api/auth/**` | auth-service | 8081 |
| `/api/patients/**` | patient-service | 8082 |
| `/api/clinical/**` | clinical-service | 8083 |
| `/api/lab/**` | lab-service | 8084 |
| `/api/pharmacy/**` | pharmacy-service | 8085 |
| `/api/billing/**` | billing-service | 8086 |

## 🧪 Testing Rápido

```bash
# 1. Health check
curl http://localhost:8080/actuator/health

# 2. Ruta protegida sin JWT (debe fallar)
curl http://localhost:8080/api/patients/123

# 3. Verificar registro en Eureka
curl http://localhost:8761/eureka/apps | grep API-GATEWAY

# 4. Ver métricas
curl http://localhost:8080/actuator/metrics/http.server.requests
```

## 🎯 Checklist de Verificación

- [ ] Java 17 instalado
- [ ] Maven instalado
- [ ] Eureka Server corriendo en 8761
- [ ] API Gateway compilado sin errores
- [ ] API Gateway corriendo en 8080
- [ ] Health check retorna UP
- [ ] Gateway registrado en Eureka
- [ ] Rutas públicas funcionan
- [ ] Rutas protegidas requieren JWT

## 📚 Más Información

- [README.md](./README.md) - Documentación completa
- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Documentación de la API
- [Spec Requirements](./.kiro/specs/api-gateway/requirements.md) - Requerimientos
- [Spec Design](./.kiro/specs/api-gateway/design.md) - Diseño técnico

---

**¿Problemas?** Revisa la sección de Troubleshooting en README.md
