#!/bin/bash

# MedFlow HIS - Script de Inicio de Servicios
# Este script inicia todos los servicios del backend y verifica su salud

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}   MedFlow HIS - Inicio de Servicios${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Función para verificar si Docker está corriendo
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Error: Docker no está corriendo${NC}"
        echo "Por favor inicia Docker Desktop y vuelve a intentar"
        exit 1
    fi
    echo -e "${GREEN}✅ Docker está corriendo${NC}"
}

# Función para verificar salud de un servicio
check_health() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=0
    
    echo -e "${YELLOW}⏳ Esperando a que $service_name esté listo (puerto $port)...${NC}"
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s -f "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name está listo${NC}"
            return 0
        fi
        
        attempt=$((attempt + 1))
        echo -n "."
        sleep 2
    done
    
    echo ""
    echo -e "${RED}❌ $service_name no respondió después de $max_attempts intentos${NC}"
    return 1
}

# Verificar Docker
check_docker

echo ""
echo -e "${BLUE}📦 Iniciando servicios con Docker Compose...${NC}"
docker compose up -d

echo ""
echo -e "${BLUE}⏳ Esperando a que los servicios estén listos...${NC}"
echo ""

# Esperar a que PostgreSQL esté listo
echo -e "${YELLOW}⏳ Esperando a PostgreSQL...${NC}"
sleep 10
echo -e "${GREEN}✅ PostgreSQL está listo${NC}"

# Esperar a que Redis esté listo
echo -e "${YELLOW}⏳ Esperando a Redis...${NC}"
sleep 5
echo -e "${GREEN}✅ Redis está listo${NC}"

echo ""

# Verificar servicios en orden de dependencia
check_health "Eureka Server" 8761
echo ""

check_health "API Gateway" 8080
echo ""

check_health "Auth Service" 8081
echo ""

check_health "Patient Service" 8082
echo ""

check_health "Clinical Service" 8083
echo ""

# Verificar servicios opcionales (pueden fallar)
echo -e "${YELLOW}⏳ Verificando servicios opcionales...${NC}"

if check_health "Lab Service" 8084 2>/dev/null; then
    echo ""
else
    echo -e "${YELLOW}⚠️  Lab Service no está disponible (opcional)${NC}"
fi

if check_health "Pharmacy Service" 8085 2>/dev/null; then
    echo ""
else
    echo -e "${YELLOW}⚠️  Pharmacy Service no está disponible (opcional)${NC}"
fi

if check_health "Billing Service" 8086 2>/dev/null; then
    echo ""
else
    echo -e "${YELLOW}⚠️  Billing Service no está disponible (opcional)${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}   ✅ Servicios iniciados exitosamente${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

echo -e "${BLUE}📊 URLs de los servicios:${NC}"
echo ""
echo -e "  ${YELLOW}Eureka Dashboard:${NC}    http://localhost:8761"
echo -e "  ${YELLOW}API Gateway:${NC}         http://localhost:8080"
echo -e "  ${YELLOW}Auth Service:${NC}        http://localhost:8081"
echo -e "  ${YELLOW}Patient Service:${NC}     http://localhost:8082"
echo -e "  ${YELLOW}Clinical Service:${NC}    http://localhost:8083"
echo ""

echo -e "${BLUE}🧪 Prueba rápida:${NC}"
echo ""
echo -e "  # Login"
echo -e "  curl -X POST http://localhost:8080/api/auth/login \\"
echo -e "    -H \"Content-Type: application/json\" \\"
echo -e "    -d '{\"username\":\"admin\",\"password\":\"admin123\"}'"
echo ""

echo -e "${BLUE}📚 Documentación:${NC}"
echo ""
echo -e "  Ver ${YELLOW}FRONTEND_INTEGRATION_GUIDE.md${NC} para más detalles"
echo ""

echo -e "${BLUE}🛑 Para detener los servicios:${NC}"
echo ""
echo -e "  docker-compose down"
echo ""

echo -e "${BLUE}📋 Ver logs:${NC}"
echo ""
echo -e "  docker-compose logs -f                    # Todos los servicios"
echo -e "  docker-compose logs -f clinical-service   # Servicio específico"
echo ""

echo -e "${GREEN}¡Listo para integración frontend! 🚀${NC}"
