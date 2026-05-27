# Backend Cloud - Spring Cloud Infrastructure

Esta carpeta contiene la infraestructura de Spring Cloud para MedFlow HIS.

## Servicios

### 1. Eureka Server (Puerto 8761)
- **Propósito**: Service Discovery y Registry
- **Tecnología**: Spring Cloud Netflix Eureka
- **URL**: http://localhost:8761

Todos los microservicios se registran aquí para descubrimiento dinámico.

### 2. API Gateway (Puerto 8080)
- **Propósito**: Punto de entrada único para todos los servicios
- **Tecnología**: Spring Cloud Gateway
- **URL**: http://localhost:8080

Enruta las peticiones a los microservicios correspondientes.

## Estructura

```
backend-cloud/
├── api-gateway/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
└── eureka-server/
    ├── src/
    ├── pom.xml
    └── Dockerfile
```

## Configuración

Cada servicio tiene su propio `application.yml` con perfiles:
- `default`: Para desarrollo local
- `docker`: Para ejecución en contenedores

## Ejecución

### Local (sin Docker)
```bash
# Eureka Server
cd eureka-server
mvn spring-boot:run

# API Gateway
cd api-gateway
mvn spring-boot:run
```

### Docker
```bash
# Desde la raíz del proyecto
docker-compose up eureka-server api-gateway
```

## Próximos Pasos

1. Implementar Eureka Server
2. Implementar API Gateway con rutas
3. Agregar Spring Cloud Config Server (opcional)
4. Agregar Circuit Breaker (Resilience4j)
