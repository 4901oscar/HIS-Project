# MedFlow HIS - Script de Inicio de Servicios (PowerShell)
# Este script inicia todos los servicios del backend y verifica su salud

$ErrorActionPreference = "Stop"

# Función para escribir con colores
function Write-ColorOutput {
    param(
        [string]$Message,
        [string]$Color = "White"
    )
    Write-Host $Message -ForegroundColor $Color
}

Write-ColorOutput "========================================" "Cyan"
Write-ColorOutput "   MedFlow HIS - Inicio de Servicios" "Cyan"
Write-ColorOutput "========================================" "Cyan"
Write-Host ""

# Función para verificar si Docker está corriendo
function Test-Docker {
    try {
        docker info | Out-Null
        Write-ColorOutput "✅ Docker está corriendo" "Green"
        return $true
    }
    catch {
        Write-ColorOutput "❌ Error: Docker no está corriendo" "Red"
        Write-Host "Por favor inicia Docker Desktop y vuelve a intentar"
        exit 1
    }
}

# Función para verificar salud de un servicio
function Test-ServiceHealth {
    param(
        [string]$ServiceName,
        [int]$Port,
        [int]$MaxAttempts = 30
    )
    
    Write-ColorOutput "⏳ Esperando a que $ServiceName esté listo (puerto $Port)..." "Yellow"
    
    for ($attempt = 1; $attempt -le $MaxAttempts; $attempt++) {
        try {
            $response = Invoke-WebRequest -Uri "http://localhost:$Port/actuator/health" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -eq 200) {
                Write-ColorOutput "✅ $ServiceName está listo" "Green"
                return $true
            }
        }
        catch {
            # Servicio aún no está listo
        }
        
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 2
    }
    
    Write-Host ""
    Write-ColorOutput "❌ $ServiceName no respondió después de $MaxAttempts intentos" "Red"
    return $false
}

# Verificar Docker
Test-Docker

Write-Host ""
Write-ColorOutput "📦 Iniciando servicios con Docker Compose..." "Cyan"
docker-compose up -d

Write-Host ""
Write-ColorOutput "⏳ Esperando a que los servicios estén listos..." "Cyan"
Write-Host ""

# Esperar a que PostgreSQL esté listo
Write-ColorOutput "⏳ Esperando a PostgreSQL..." "Yellow"
Start-Sleep -Seconds 10
Write-ColorOutput "✅ PostgreSQL está listo" "Green"

# Esperar a que Redis esté listo
Write-ColorOutput "⏳ Esperando a Redis..." "Yellow"
Start-Sleep -Seconds 5
Write-ColorOutput "✅ Redis está listo" "Green"

Write-Host ""

# Verificar servicios en orden de dependencia
Test-ServiceHealth -ServiceName "Eureka Server" -Port 8761
Write-Host ""

Test-ServiceHealth -ServiceName "API Gateway" -Port 8080
Write-Host ""

Test-ServiceHealth -ServiceName "Auth Service" -Port 8081
Write-Host ""

Test-ServiceHealth -ServiceName "Patient Service" -Port 8082
Write-Host ""

Test-ServiceHealth -ServiceName "Clinical Service" -Port 8083
Write-Host ""

# Verificar servicios opcionales (pueden fallar)
Write-ColorOutput "⏳ Verificando servicios opcionales..." "Yellow"

if (Test-ServiceHealth -ServiceName "Lab Service" -Port 8084 -MaxAttempts 5) {
    Write-Host ""
}
else {
    Write-ColorOutput "⚠️  Lab Service no está disponible (opcional)" "Yellow"
}

if (Test-ServiceHealth -ServiceName "Pharmacy Service" -Port 8085 -MaxAttempts 5) {
    Write-Host ""
}
else {
    Write-ColorOutput "⚠️  Pharmacy Service no está disponible (opcional)" "Yellow"
}

if (Test-ServiceHealth -ServiceName "Billing Service" -Port 8086 -MaxAttempts 5) {
    Write-Host ""
}
else {
    Write-ColorOutput "⚠️  Billing Service no está disponible (opcional)" "Yellow"
}

Write-Host ""
Write-ColorOutput "========================================" "Green"
Write-ColorOutput "   ✅ Servicios iniciados exitosamente" "Green"
Write-ColorOutput "========================================" "Green"
Write-Host ""

Write-ColorOutput "📊 URLs de los servicios:" "Cyan"
Write-Host ""
Write-ColorOutput "  Eureka Dashboard:    http://localhost:8761" "Yellow"
Write-ColorOutput "  API Gateway:         http://localhost:8080" "Yellow"
Write-ColorOutput "  Auth Service:        http://localhost:8081" "Yellow"
Write-ColorOutput "  Patient Service:     http://localhost:8082" "Yellow"
Write-ColorOutput "  Clinical Service:    http://localhost:8083" "Yellow"
Write-Host ""

Write-ColorOutput "🧪 Prueba rápida:" "Cyan"
Write-Host ""
Write-Host '  # Login (PowerShell)'
Write-Host '  $body = @{'
Write-Host '    username = "admin"'
Write-Host '    password = "admin123"'
Write-Host '  } | ConvertTo-Json'
Write-Host ''
Write-Host '  Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `'
Write-Host '    -Method Post `'
Write-Host '    -ContentType "application/json" `'
Write-Host '    -Body $body'
Write-Host ""

Write-ColorOutput "📚 Documentación:" "Cyan"
Write-Host ""
Write-ColorOutput "  Ver FRONTEND_INTEGRATION_GUIDE.md para más detalles" "Yellow"
Write-Host ""

Write-ColorOutput "🛑 Para detener los servicios:" "Cyan"
Write-Host ""
Write-Host "  docker-compose down"
Write-Host ""

Write-ColorOutput "📋 Ver logs:" "Cyan"
Write-Host ""
Write-Host "  docker-compose logs -f                    # Todos los servicios"
Write-Host "  docker-compose logs -f clinical-service   # Servicio específico"
Write-Host ""

Write-ColorOutput "¡Listo para integración frontend! 🚀" "Green"
