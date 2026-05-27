# Eureka Server - Guía de Inicio Rápido

## 🚀 Inicio Rápido (5 minutos)

### Prerequisitos

- Java 17 o superior
- Maven 3.8+

Verifica tu instalación:
```bash
java -version
mvn -version
```

### Paso 1: Navegar al directorio

```bash
cd backend-cloud/eureka-server
```

### Paso 2: Compilar el proyecto

```bash
mvn clean install
```

Esto descargará todas las dependencias y compilará el proyecto.

### Paso 3: Ejecutar Eureka Server

```bash
mvn spring-boot:run
```

### Paso 4: Verificar que está corriendo

Abre tu navegador en: **http://localhost:8761**

Deberías ver el dashboard de Eureka con:
- ✅ "Instances currently registered with Eureka" (vacío por ahora)
- ✅ System Status: UP

### Paso 5: Verificar Health Check

```bash
curl http://localhost:8761/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

## 🎉 ¡Listo!

Eureka Server está corriendo y listo para recibir registros de microservicios.

## 📊 ¿Qué verás en el Dashboard?

Cuando abras http://localhost:8761 verás:

1. **System Status**: Estado del servidor (UP/DOWN)
2. **DS Replicas**: Réplicas de Eureka (ninguna en modo standalone)
3. **Instances currently registered**: Lista de servicios registrados
4. **General Info**: Información del entorno
5. **Instance Info**: Detalles de cada instancia registrada

## 🔍 Ejemplo de Dashboard

```
┌─────────────────────────────────────────┐
│         Eureka Dashboard                │
├─────────────────────────────────────────┤
│ System Status: UP                       │
│                                         │
│ Instances currently registered:         │
│ (No instances available)                │
│                                         │
│ General Info                            │
│ - Environment: test                     │
│ - Data center: default                  │
└─────────────────────────────────────────┘
```

Cuando registres servicios, aparecerán aquí:

```
┌─────────────────────────────────────────┐
│ Instances currently registered:         │
│                                         │
│ AUTH-SERVICE                            │
│   Status: UP                            │
│   Instance: localhost:8081              │
│                                         │
│ PATIENT-SERVICE                         │
│   Status: UP                            │
│   Instance: localhost:8082              │
└─────────────────────────────────────────┘
```

## 🛑 Detener el Servidor

Presiona `Ctrl + C` en la terminal donde está corriendo.

## 🐛 Troubleshooting

### Error: "Port 8761 is already in use"

**Solución**: Otro proceso está usando el puerto 8761.

```bash
# Windows
netstat -ano | findstr :8761
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8761
kill -9 <PID>
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

## 📝 Logs

Los logs aparecerán en la consola:

```
2026-04-10 19:51:00 - Starting EurekaServerApplication
2026-04-10 19:51:05 - Tomcat started on port(s): 8761
2026-04-10 19:51:05 - Started EurekaServerApplication in 8.5 seconds
```

## ⏭️ Próximos Pasos

1. ✅ Eureka Server corriendo
2. ⏳ Implementar un microservicio que se registre en Eureka
3. ⏳ Ver el servicio aparecer en el dashboard
4. ⏳ Implementar API Gateway para enrutar peticiones

## 🔗 URLs Útiles

- Dashboard: http://localhost:8761
- Health: http://localhost:8761/actuator/health
- Info: http://localhost:8761/actuator/info
- Apps (JSON): http://localhost:8761/eureka/apps (con header Accept: application/json)

## 💡 Tips

1. **Mantén Eureka corriendo**: Los servicios necesitan que Eureka esté activo para registrarse
2. **Revisa el dashboard**: Es la forma más fácil de ver qué servicios están registrados
3. **Logs son tu amigo**: Si algo no funciona, revisa los logs
4. **Health checks**: Usa `/actuator/health` para verificar el estado

## 🎓 Conceptos Clave

- **Service Registry**: Eureka mantiene un registro de todos los servicios
- **Heartbeat**: Cada servicio envía un "latido" cada 30 segundos
- **Self-Preservation**: Protege contra fallos de red (deshabilitado en desarrollo)
- **Discovery**: Los servicios consultan a Eureka para encontrar otros servicios
