# 🎨 Guía Visual de Eureka Server

## 🏗️ Arquitectura Completa

```
┌─────────────────────────────────────────────────────────────┐
│                    MEDFLOW HIS ARCHITECTURE                  │
└─────────────────────────────────────────────────────────────┘

                    ┌──────────────────┐
                    │  Eureka Server   │
                    │   Port: 8761     │
                    │  ┌────────────┐  │
                    │  │  Registry  │  │
                    │  └────────────┘  │
                    └────────┬─────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ API Gateway  │    │ Auth Service │    │Patient Service│
│  Port: 8080  │    │  Port: 8081  │    │  Port: 8082  │
└──────┬───────┘    └──────────────┘    └──────────────┘
       │
       │ Routes requests
       │
       ▼
┌──────────────┐
│   Frontend   │
│  Port: 3000  │
└──────────────┘
```

## 🔄 Flujo de Registro

```
PASO 1: Servicio arranca
┌─────────────────┐
│  Auth Service   │
│   Starting...   │
└────────┬────────┘
         │
         │ 1. Lee config
         │    eureka.client.service-url
         │
         ▼
┌─────────────────┐
│ application.yml │
│ defaultZone:    │
│ localhost:8761  │
└────────┬────────┘
         │
         │ 2. Conecta a Eureka
         │
         ▼
┌─────────────────┐
│ Eureka Server   │
│ Recibe registro │
└────────┬────────┘
         │
         │ 3. Registra servicio
         │
         ▼
┌─────────────────┐
│   Dashboard     │
│ AUTH-SERVICE ✅ │
│ Status: UP      │
│ localhost:8081  │
└─────────────────┘
```

## 💓 Heartbeat Mechanism

```
Tiempo: 0s
┌──────────┐                    ┌──────────┐
│ Service  │ ─── Register ────> │  Eureka  │
└──────────┘                    └──────────┘

Tiempo: 30s
┌──────────┐                    ┌──────────┐
│ Service  │ ─── Heartbeat ───> │  Eureka  │
└──────────┘     "I'm alive"    └──────────┘

Tiempo: 60s
┌──────────┐                    ┌──────────┐
│ Service  │ ─── Heartbeat ───> │  Eureka  │
└──────────┘     "I'm alive"    └──────────┘

Tiempo: 90s
┌──────────┐                    ┌──────────┐
│ Service  │  X  No heartbeat   │  Eureka  │
└──────────┘                    └────┬─────┘
                                     │
                                     ▼
                            Service marked as DOWN
                            Removed from registry
```

## 🔍 Service Discovery Flow

```
ESCENARIO: API Gateway necesita llamar a Auth Service

1. Gateway pregunta a Eureka
┌─────────────┐                    ┌──────────┐
│ API Gateway │ ─── "¿Dónde está  │  Eureka  │
│             │      AUTH-SERVICE?"│          │
└─────────────┘                    └────┬─────┘
                                        │
                                        │ Busca en registry
                                        │
                                        ▼
                                ┌──────────────┐
                                │   Registry   │
                                │ AUTH-SERVICE:│
                                │ localhost:   │
                                │    8081      │
                                └──────┬───────┘
                                       │
2. Eureka responde                     │
┌─────────────┐                    ┌──┴───────┐
│ API Gateway │ <── "localhost:8081"│  Eureka  │
└──────┬──────┘                    └──────────┘
       │
       │ 3. Llama al servicio
       │
       ▼
┌─────────────┐
│Auth Service │
│ localhost:  │
│   8081      │
└─────────────┘
```

## 📊 Dashboard States

### Estado Inicial (Sin servicios)
```
╔════════════════════════════════════════╗
║      Eureka Dashboard                  ║
╠════════════════════════════════════════╣
║ System Status: UP ✅                   ║
║                                        ║
║ Instances registered: 0                ║
║ ┌────────────────────────────────┐    ║
║ │ No instances available         │    ║
║ └────────────────────────────────┘    ║
╚════════════════════════════════════════╝
```

### Con Auth Service Registrado
```
╔════════════════════════════════════════╗
║      Eureka Dashboard                  ║
╠════════════════════════════════════════╣
║ System Status: UP ✅                   ║
║                                        ║
║ Instances registered: 1                ║
║ ┌────────────────────────────────┐    ║
║ │ AUTH-SERVICE                   │    ║
║ │ ├─ Status: UP ✅               │    ║
║ │ ├─ Instance: localhost:8081    │    ║
║ │ └─ Last heartbeat: 2s ago      │    ║
║ └────────────────────────────────┘    ║
╚════════════════════════════════════════╝
```

### Con Todos los Servicios
```
╔════════════════════════════════════════╗
║      Eureka Dashboard                  ║
╠════════════════════════════════════════╣
║ System Status: UP ✅                   ║
║                                        ║
║ Instances registered: 4                ║
║ ┌────────────────────────────────┐    ║
║ │ API-GATEWAY                    │    ║
║ │ ├─ Status: UP ✅               │    ║
║ │ └─ Instance: localhost:8080    │    ║
║ │                                │    ║
║ │ AUTH-SERVICE                   │    ║
║ │ ├─ Status: UP ✅               │    ║
║ │ └─ Instance: localhost:8081    │    ║
║ │                                │    ║
║ │ PATIENT-SERVICE                │    ║
║ │ ├─ Status: UP ✅               │    ║
║ │ └─ Instance: localhost:8082    │    ║
║ │                                │    ║
║ │ CLINICAL-SERVICE               │    ║
║ │ ├─ Status: UP ✅               │    ║
║ │ └─ Instance: localhost:8083    │    ║
║ └────────────────────────────────┘    ║
╚════════════════════════════════════════╝
```

## 🔧 Configuración Visual

### application.yml Explicado

```yaml
# Nombre de la aplicación
spring:
  application:
    name: eureka-server  # ← Aparece en logs y dashboard

# Puerto donde escucha
server:
  port: 8761  # ← Puerto estándar de Eureka

eureka:
  instance:
    hostname: localhost  # ← Hostname del servidor
  
  client:
    # ¿Se registra a sí mismo?
    register-with-eureka: false  # ← NO (es el servidor)
    
    # ¿Busca otros Eureka servers?
    fetch-registry: false  # ← NO (modo standalone)
    
    # URL donde está Eureka
    service-url:
      defaultZone: http://localhost:8761/eureka/
  
  server:
    # Cada cuánto elimina servicios muertos
    eviction-interval-timer-in-ms: 15000  # ← 15 segundos
    
    # Auto-preservación (protección contra fallos de red)
    enable-self-preservation: false  # ← Deshabilitado en dev
```

## 🎯 Endpoints Importantes

```
┌─────────────────────────────────────────────┐
│         Eureka Server Endpoints             │
├─────────────────────────────────────────────┤
│                                             │
│ 🌐 Dashboard (Web UI)                      │
│    http://localhost:8761                    │
│    → Ver servicios registrados              │
│                                             │
│ 💚 Health Check                            │
│    http://localhost:8761/actuator/health    │
│    → Verificar que Eureka está UP          │
│                                             │
│ 📊 Metrics                                 │
│    http://localhost:8761/actuator/metrics   │
│    → Ver métricas del servidor             │
│                                             │
│ ℹ️  Info                                    │
│    http://localhost:8761/actuator/info      │
│    → Información del servidor              │
│                                             │
│ 📋 Apps List (JSON)                        │
│    http://localhost:8761/eureka/apps        │
│    Header: Accept: application/json         │
│    → Lista de apps registradas             │
│                                             │
│ 🔍 Specific App                            │
│    http://localhost:8761/eureka/apps/       │
│           AUTH-SERVICE                      │
│    → Info de una app específica            │
│                                             │
└─────────────────────────────────────────────┘
```

## 🚦 Estados de Servicio

```
┌──────────┐
│   UP     │  ✅ Servicio funcionando correctamente
└──────────┘     Recibiendo heartbeats

┌──────────┐
│  DOWN    │  ❌ Servicio no responde
└──────────┘     No hay heartbeats

┌──────────┐
│ STARTING │  🔄 Servicio iniciando
└──────────┘     Registrándose en Eureka

┌──────────┐
│OUT_OF_   │  ⚠️  Servicio sobrecargado
│ SERVICE  │     No acepta más peticiones
└──────────┘
```

## 📈 Load Balancing Visual

```
Escenario: 3 instancias de Patient Service

┌─────────────┐
│ API Gateway │
└──────┬──────┘
       │
       │ Pregunta a Eureka: "¿Dónde está PATIENT-SERVICE?"
       │
       ▼
┌──────────────┐
│   Eureka     │
│  Responde:   │
│  - Instance1 │
│  - Instance2 │
│  - Instance3 │
└──────┬───────┘
       │
       │ Gateway elige una (Round-robin)
       │
       ├─────────────┬─────────────┐
       │             │             │
       ▼             ▼             ▼
┌──────────┐  ┌──────────┐  ┌──────────┐
│Patient:  │  │Patient:  │  │Patient:  │
│  8082    │  │  8092    │  │  8102    │
└──────────┘  └──────────┘  └──────────┘
   Request 1    Request 2    Request 3
```

## 🎓 Conceptos Clave Visualizados

### Service Registry = Guía Telefónica
```
┌─────────────────────────────┐
│     Service Registry        │
├─────────────────────────────┤
│ AUTH-SERVICE                │
│   → localhost:8081          │
│                             │
│ PATIENT-SERVICE             │
│   → localhost:8082          │
│   → localhost:8092 (backup) │
│                             │
│ CLINICAL-SERVICE            │
│   → localhost:8083          │
└─────────────────────────────┘
```

### Heartbeat = Latido del Corazón
```
💓 ─── 30s ───> 💓 ─── 30s ───> 💓
   "Vivo"         "Vivo"         "Vivo"

💓 ─── 30s ───> ❌ ─── 90s ───> 💀
   "Vivo"      Sin señal      Eliminado
```

### Discovery = GPS para Servicios
```
"¿Dónde está AUTH-SERVICE?"
         │
         ▼
    [Eureka GPS]
         │
         ▼
"Está en localhost:8081"
         │
         ▼
   [Navegar allí]
```

## 🎉 ¡Listo para Usar!

Eureka Server está implementado y documentado. 

**Siguiente paso**: Implementar API Gateway que se registrará en Eureka.
