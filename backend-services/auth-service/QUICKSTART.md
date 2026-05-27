# Auth Service - Quick Start Guide

Get the Auth Service running in 5 minutes.

## 5-Minute Setup

### Prerequisites Check

```bash
# Check Java version (need 17+)
java -version

# Check Maven
mvn -version

# Check Docker (optional)
docker --version
```

### Option 1: Docker Compose (Easiest)

```bash
# From project root
cd /path/to/medflow-his

# Start dependencies and auth-service
docker-compose up -d postgres eureka-server auth-service

# Wait 30 seconds for startup
sleep 30

# Verify service is running
curl http://localhost:8081/actuator/health
```

**Expected Output:**
```json
{"status":"UP"}
```

### Option 2: Local Development

```bash
# 1. Start PostgreSQL
docker run -d --name medflow-postgres \
  -e POSTGRES_DB=medflow_db \
  -e POSTGRES_USER=medflow_user \
  -e POSTGRES_PASSWORD=medflow_pass \
  -p 5432:5432 \
  postgres:15-alpine

# 2. Start Eureka Server (in separate terminal)
cd backend-cloud/eureka-server
mvn spring-boot:run

# 3. Start Auth Service (in separate terminal)
cd backend-services/auth-service
mvn spring-boot:run

# 4. Verify
curl http://localhost:8081/actuator/health
```

## Common Commands

### Build Commands

```bash
# Clean build
mvn clean package

# Build without tests (faster)
mvn clean package -DskipTests

# Run tests only
mvn test

# Run with coverage report
mvn clean test jacoco:report
```

### Run Commands

```bash
# Run with Maven
mvn spring-boot:run

# Run JAR directly
java -jar target/auth-service-1.0.0.jar

# Run with custom profile
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Run with custom JWT secret
JWT_SECRET=my-secret-key mvn spring-boot:run
```

### Docker Commands

```bash
# Build image
docker build -t medflow/auth-service:latest .

# Run container
docker run -d -p 8081:8081 \
  --name auth-service \
  -e SPRING_PROFILES_ACTIVE=docker \
  medflow/auth-service:latest

# View logs
docker logs -f auth-service

# Stop container
docker stop auth-service

# Remove container
docker rm auth-service
```

### Docker Compose Commands

```bash
# Start all services
docker-compose up -d

# Start specific services
docker-compose up -d postgres eureka-server auth-service

# View logs
docker-compose logs -f auth-service

# Restart service
docker-compose restart auth-service

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Testing Endpoints

### 1. Health Check

```bash
curl http://localhost:8081/actuator/health
```

**Expected:**
```json
{"status":"UP"}
```

---

### 2. Login (Staff)

```bash
# Login as doctor
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "doctor1",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400,
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "username": "doctor1",
    "email": "doctor1@medflow.com",
    "fullName": "Dr. Juan Pérez",
    "roles": ["DOCTOR"],
    "active": true
  }
}
```

**Save the token for next steps:**
```bash
# Linux/Mac
export TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Windows PowerShell
$TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 3. Get Current User

```bash
curl http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "username": "doctor1",
  "email": "doctor1@medflow.com",
  "fullName": "Dr. Juan Pérez",
  "roles": ["DOCTOR"],
  "active": true
}
```

---

### 4. Validate Token

```bash
curl "http://localhost:8081/api/auth/validate?token=$TOKEN"
```

**Expected Response:**
```json
{
  "valid": true,
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "username": "doctor1",
  "roles": "DOCTOR"
}
```

---

### 5. Refresh Token

```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 86400
}
```

---

### 6. Logout

```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response:**
```json
{
  "message": "Logged out successfully"
}
```

**Verify token is blacklisted:**
```bash
curl "http://localhost:8081/api/auth/validate?token=$TOKEN"
```

**Expected Response:**
```json
{
  "valid": false,
  "userId": null,
  "username": null,
  "roles": null
}
```

---

### 7. Test Rate Limiting

```bash
# Try 6 login attempts with wrong password
for i in {1..6}; do
  echo "Attempt $i:"
  curl -X POST http://localhost:8081/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"doctor1","password":"wrongpassword"}'
  echo ""
done
```

**Expected:** First 5 attempts return 401, 6th attempt returns 429 (Too Many Requests)

---

## Complete Test Script

Save this as `test-auth-service.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8081"

echo "=== Auth Service Quick Test ==="
echo ""

# 1. Health Check
echo "1. Health Check"
curl -s $BASE_URL/actuator/health | jq
echo ""

# 2. Login
echo "2. Login"
RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"doctor1","password":"password123"}')
echo $RESPONSE | jq
TOKEN=$(echo $RESPONSE | jq -r '.token')
echo ""

# 3. Get Current User
echo "3. Get Current User"
curl -s $BASE_URL/api/auth/me \
  -H "Authorization: Bearer $TOKEN" | jq
echo ""

# 4. Validate Token
echo "4. Validate Token"
curl -s "$BASE_URL/api/auth/validate?token=$TOKEN" | jq
echo ""

# 5. Refresh Token
echo "5. Refresh Token"
REFRESH_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/refresh \
  -H "Authorization: Bearer $TOKEN")
echo $REFRESH_RESPONSE | jq
NEW_TOKEN=$(echo $REFRESH_RESPONSE | jq -r '.token')
echo ""

# 6. Logout
echo "6. Logout"
curl -s -X POST $BASE_URL/api/auth/logout \
  -H "Authorization: Bearer $TOKEN" | jq
echo ""

# 7. Verify token is blacklisted
echo "7. Verify Token Blacklisted"
curl -s "$BASE_URL/api/auth/validate?token=$TOKEN" | jq
echo ""

echo "=== Test Complete ==="
```

**Run the script:**
```bash
chmod +x test-auth-service.sh
./test-auth-service.sh
```

## Troubleshooting

### Service Won't Start

**Problem:** Port 8081 already in use

**Solution:**
```bash
# Find process using port 8081
lsof -i :8081

# Kill the process
kill -9 <PID>

# Or change port in application.yml
server:
  port: 8082
```

---

**Problem:** Cannot connect to PostgreSQL

**Solution:**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Start PostgreSQL
docker-compose up -d postgres

# Test connection
psql -h localhost -U medflow_user -d medflow_db
```

---

**Problem:** Eureka registration fails

**Solution:**
```bash
# Check if Eureka is running
curl http://localhost:8761

# Start Eureka
docker-compose up -d eureka-server

# Wait 30 seconds and check again
```

---

### Login Fails

**Problem:** 401 Unauthorized with correct credentials

**Solution:**
```bash
# Check if database has users
docker exec -it medflow-postgres psql -U medflow_user -d medflow_db

# In psql:
\c medflow_db
SET search_path TO auth_schema;
SELECT * FROM users;

# If no users, insert test user (password: password123)
INSERT INTO users (id, username, password, email, full_name, active)
VALUES (
  gen_random_uuid(),
  'doctor1',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'doctor1@medflow.com',
  'Dr. Juan Pérez',
  true
);
```

---

**Problem:** Rate limiting blocking legitimate requests

**Solution:**
```bash
# Wait 1 minute for rate limit to reset
sleep 60

# Or restart service to clear rate limiter
docker-compose restart auth-service
```

---

### Token Issues

**Problem:** Token validation fails

**Solution:**
```bash
# Check JWT secret matches across services
echo $JWT_SECRET

# Verify token format
echo $TOKEN | cut -d'.' -f1 | base64 -d

# Check token expiration (24 hours)
# Tokens expire after 86400 seconds
```

---

**Problem:** Token blacklisted unexpectedly

**Solution:**
```bash
# Restart service to clear in-memory blacklist
docker-compose restart auth-service

# Get new token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"doctor1","password":"password123"}'
```

---

### Docker Issues

**Problem:** Container exits immediately

**Solution:**
```bash
# Check logs
docker-compose logs auth-service

# Common issues:
# - Database not ready: wait 10 seconds and restart
# - Eureka not ready: wait 30 seconds and restart
# - Missing environment variables: check docker-compose.yml
```

---

**Problem:** Cannot connect to other containers

**Solution:**
```bash
# Verify all containers on same network
docker network inspect medflow-network

# Restart with network recreation
docker-compose down
docker-compose up -d
```

---

## Quick Reference

### Default Credentials

**Test Users** (if seeded):
- Username: `doctor1` / Password: `password123`
- Username: `admin1` / Password: `password123`
- Username: `patient1` / Password: `password123`

### Ports

- Auth Service: `8081`
- Eureka Server: `8761`
- PostgreSQL: `5432`

### URLs

- Health Check: `http://localhost:8081/actuator/health`
- Eureka Dashboard: `http://localhost:8761`
- Login: `POST http://localhost:8081/api/auth/login`

### Environment Variables

```bash
# Required for production
export JWT_SECRET="your-256-bit-secret-key"

# Optional
export DB_USERNAME="medflow_user"
export DB_PASSWORD="medflow_pass"
export EUREKA_SERVER_URL="http://localhost:8761/eureka/"
```

### Common Errors

| Error | Cause | Solution |
|-------|-------|----------|
| 401 Unauthorized | Invalid credentials | Check username/password |
| 429 Too Many Requests | Rate limit exceeded | Wait 1 minute |
| 500 Internal Server Error | Database connection | Check PostgreSQL |
| 503 Service Unavailable | Eureka not found | Start Eureka Server |

---

## Next Steps

1. **Read Full Documentation**: See [README.md](./README.md)
2. **Test All Endpoints**: Use the test script above
3. **Integrate with API Gateway**: Configure JWT validation
4. **Add Test Users**: Insert users in database
5. **Configure Production**: Set JWT secret and database credentials

---

**Version**: 1.0.0  
**Last Updated**: April 13, 2026  
**Service Port**: 8081
