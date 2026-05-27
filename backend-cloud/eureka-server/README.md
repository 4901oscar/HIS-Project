# Eureka Server - Service Discovery

Servidor de descubrimiento de servicios para MedFlow HIS usando Netflix Eureka.

## 🎯 Propósito

Eureka Server actúa como un registro centralizado donde todos los microservicios se registran y descubren entre sí dinámicamente.

## 🔧 Tecnologías

- **Spring Boot 3.2.4**
- **Spring Cloud Netflix Eureka Server**
- **Java 17**
- **Maven**

## 📋 Configuración

### Perfiles Disponibles

1. **default** (desarrollo local)
   - Puerto: 8761
   - Hostname: localhost
   - URL: http://localhost:8761

2. **docker** (contenedores)
   - Puerto: 8761
   - Hostname: eureka-server
   - URL: http://eureka-server:8761

### Propiedades Importantes

```yaml
eureka:
  client:
    register-with-eureka: false  # No se registra a sí mismo
    fetch-registry: false        # No busca otros Eureka servers
  server:
    enable-self-preservation: false  # Deshabilitado en desarrollo
```

## 🚀 Ejecución

### Opción 1: Maven (Desarrollo Local)

```bash
cd backend-cloud/eureka-server
mvn clean install
mvn spring-boot:run
```

### Opción 2: Docker

```bash
# Desde la raíz del proyecto
docker-compose up eureka-server
```

### Opción 3: JAR

```bash
cd backend-cloud/eureka-server
mvn clean package
java -jar target/eureka-server-1.0.0.jar
```

## 🌐 Acceso

Una vez iniciado, accede al dashboard de Eureka:

**URL**: http://localhost:8761

Verás:
- Servicios registrados
- Estado de cada instancia
- Información de salud
- Métricas

## 📊 Dashboard de Eureka

El dashboard muestra:

1. **System Status**: Estado general del servidor
2. **DS Replicas**: Réplicas de Eureka (ninguna en modo standalone)
3. **Instances currently registered**: Servicios registrados
4. **General Info**: Información del servidor
5. **Instance Info**: Detalles de cada instancia

## 🔍 Endpoints Importantes

| Endpoint | Descripción |
|----------|-------------|
| `/` | Dashboard web de Eureka |
| `/eureka/apps` | Lista de aplicaciones registradas (XML) |
| `/eureka/apps/{appName}` | Información de una aplicación específica |
| `/actuator/health` | Health check del servidor |
| `/actuator/info` | Información del servidor |
| `/actuator/metrics` | Métricas del servidor |

## 🔌 Cómo Registrar un Servicio

Para que un microservicio se registre en Eureka:

### 1. Agregar dependencia en pom.xml

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

### 2. Habilitar Eureka Client

```java
@SpringBootApplication
@EnableDiscoveryClient  // O @EnableEurekaClient
public class MyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyServiceApplication.class, args);
    }
}
```

### 3. Configurar application.yml

```yaml
spring:
  application:
    name: my-service  # Nombre con el que se registra

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```

## 🧪 Testing

### Verificar que Eureka está corriendo

```bash
curl http://localhost:8761/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP"
}
```

### Ver servicios registrados (JSON)

```bash
curl -H "Accept: application/json" http://localhost:8761/eureka/apps
```

## 📝 Logs

Los logs muestran:
- Inicio del servidor
- Servicios que se registran
- Heartbeats recibidos
- Servicios que se desregistran

Ejemplo:
```
2026-04-10 19:51:00 - Eureka Server started on port 8761
2026-04-10 19:52:00 - Registered instance AUTH-SERVICE/localhost:8081
2026-04-10 19:52:30 - Renew threshold updated to 1
```

## 🔧 Troubleshooting

### Problema: Servicios no se registran

**Solución**:
1. Verificar que Eureka Server está corriendo
2. Verificar la URL en `eureka.client.service-url.defaultZone`
3. Revisar logs del servicio cliente

### Problema: Self-preservation mode activado

**Solución**:
En desarrollo, está deshabilitado. En producción, es normal y protege contra fallos de red.

### Problema: Servicios aparecen como DOWN

**Solución**:
1. Verificar health check del servicio
2. Verificar conectividad de red
3. Revisar configuración de heartbeat

## 🏗️ Arquitectura

```
┌─────────────────────────────────┐
│      Eureka Server (8761)       │
│  ┌───────────────────────────┐  │
│  │   Service Registry        │  │
│  │  - AUTH-SERVICE: 8081     │  │
│  │  - PATIENT-SERVICE: 8082  │  │
│  │  - CLINICAL-SERVICE: 8083 │  │
│  │  - API-GATEWAY: 8080      │  │
│  └───────────────────────────┘  │
└─────────────────────────────────┘
         ↑           ↑
         │           │
    Register    Discover
         │           │
    ┌────┴───────────┴────┐
    │   Microservices     │
    └─────────────────────┘
```

## 📚 Próximos Pasos

1. ✅ Eureka Server implementado
2. ⏳ Implementar API Gateway con Eureka Client
3. ⏳ Implementar Auth Service con Eureka Client
4. ⏳ Implementar Patient Service con Eureka Client
5. ⏳ Implementar Clinical Service con Eureka Client

## 🔗 Referencias

- [Spring Cloud Netflix Eureka](https://spring.io/projects/spring-cloud-netflix)
- [Eureka Wiki](https://github.com/Netflix/eureka/wiki)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
