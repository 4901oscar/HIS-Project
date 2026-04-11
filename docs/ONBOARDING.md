# 🚀 MedFlow HIS - Onboarding Guide

Welcome to MedFlow HIS! This guide will help you get started with the project.

## 📋 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Setup](#project-setup)
3. [Understanding the Architecture](#understanding-the-architecture)
4. [Development Workflow](#development-workflow)
5. [Running the Project](#running-the-project)
6. [Your First Contribution](#your-first-contribution)
7. [Resources](#resources)

---

## 1. Prerequisites

### Required Software

Install the following before starting:

```bash
# Java 17
java -version
# Should show: openjdk version "17.x.x"

# Maven 3.8+
mvn -version
# Should show: Apache Maven 3.8.x or higher

# Node.js 18+
node -version
# Should show: v18.x.x or higher

# Docker & Docker Compose
docker -version
docker-compose -version

# Git
git --version
```

### IDE Setup

**Recommended IDEs:**
- **Backend**: IntelliJ IDEA or Eclipse
- **Frontend**: VS Code

**VS Code Extensions:**
- ESLint
- Prettier
- TypeScript
- Tailwind CSS IntelliSense

**IntelliJ IDEA Plugins:**
- Lombok
- Spring Boot
- Docker

---

## 2. Project Setup

### Step 1: Clone Repository

```bash
git clone https://github.com/4901oscar/HIS-Project.git
cd HIS-Project
```

### Step 2: Checkout develop branch

```bash
git checkout develop
git pull origin develop
```

### Step 3: Setup Environment Variables

```bash
# Copy example env file
cp .env.example .env

# Edit .env with your local configuration
# (Database credentials, JWT secret, etc.)
```

### Step 4: Install Dependencies

**Backend (each service):**
```bash
cd backend-cloud/eureka-server
mvn clean install
```

**Frontend:**
```bash
cd frontend-medflow
npm install
```

---

## 3. Understanding the Architecture

### Read These Documents First (30 minutes)

1. **[PROJECT_CONTEXT.md](../PROJECT_CONTEXT.md)** - Complete project overview
2. **[ARCHITECTURE_DDD.md](../ARCHITECTURE_DDD.md)** - Architecture details
3. **[CURRENT_STATUS.md](../CURRENT_STATUS.md)** - Current progress

### Key Concepts

#### Domain-Driven Design (DDD)

Each microservice represents a hospital domain:
- **Auth Service**: Authentication & Authorization
- **Patient Service**: Patient management
- **Clinical Service**: Medical operations
- **Lab Service**: Laboratory
- **Pharmacy Service**: Medication management
- **Billing Service**: Payments & invoicing

#### Service Discovery (Eureka)

All services register with Eureka Server:
```
Service → Eureka: "I'm alive at localhost:8081"
Gateway → Eureka: "Where is auth-service?"
Eureka → Gateway: "At localhost:8081"
```

#### API Gateway

Single entry point for all requests:
```
Frontend → API Gateway → Microservices
```

### Architecture Diagram

```
┌─────────────┐
│   Frontend  │
│  (React)    │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ API Gateway │
│   (8080)    │
└──────┬──────┘
       │
       ├──> Eureka (8761)
       │
       ├──> Auth Service (8081)
       ├──> Patient Service (8082)
       ├──> Clinical Service (8083)
       ├──> Lab Service (8084)
       ├──> Pharmacy Service (8085)
       └──> Billing Service (8086)
```

---

## 4. Development Workflow

### Methodology: Spec-Driven Design + TDD

**Before writing ANY code:**

1. **Create Spec** (`.kiro/specs/{feature-name}/`)
   - `requirements.md` - What to build
   - `design.md` - How to build it
   - `tasks.md` - Implementation steps

2. **Implement with TDD**
   - RED: Write failing test
   - GREEN: Write minimum code to pass
   - REFACTOR: Improve code

3. **Validate**
   - All tests pass
   - Coverage >= 80%
   - Meets acceptance criteria

### Git Flow

**Branches:**
- `main` - Production
- `develop` - Integration
- `feature/*` - New features
- `fix/*` - Bug fixes

**Workflow:**
```bash
# Start new feature
git checkout develop
git pull origin develop
git checkout -b feature/my-feature

# Make changes, commit
git add .
git commit -m "feat(scope): description"

# Push and create PR
git push origin feature/my-feature
# Create Pull Request to develop
```

**Commit Message Format:**
```
<type>(<scope>): <subject>

feat: New feature
fix: Bug fix
docs: Documentation
test: Tests
refactor: Code refactoring
```

---

## 5. Running the Project

### Option 1: Run Individual Services (Development)

**1. Start Eureka Server:**
```bash
cd backend-cloud/eureka-server
mvn spring-boot:run
```

**2. Verify Eureka is running:**
Open http://localhost:8761

**3. Start other services** (when implemented):
```bash
cd backend-cloud/api-gateway
mvn spring-boot:run

cd backend-services/auth-service
mvn spring-boot:run
```

**4. Start Frontend:**
```bash
cd frontend-medflow
npm run dev
```

Open http://localhost:3000

### Option 2: Docker Compose (Full Stack)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Verify Services

| Service | URL | Status |
|---------|-----|--------|
| Eureka Dashboard | http://localhost:8761 | ✅ |
| API Gateway | http://localhost:8080 | ⏳ |
| Frontend | http://localhost:3000 | ⏳ |

---

## 6. Your First Contribution

### Task: Add a Simple Feature

Let's add a health check endpoint to a service.

#### Step 1: Create Spec

```bash
mkdir -p .kiro/specs/health-check
cd .kiro/specs/health-check
```

Create `requirements.md`:
```markdown
# Requirements: Health Check Endpoint

## User Story
As a DevOps engineer,
I want a health check endpoint,
So that I can monitor service health.

## Functional Requirements
- FR1: Endpoint at /actuator/health
- FR2: Returns JSON with status

## Acceptance Criteria
- [ ] Endpoint returns 200 OK
- [ ] Response includes status: UP
```

#### Step 2: Write Test (RED)

```java
@SpringBootTest
class HealthCheckTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }
}
```

Run test: ❌ FAILS

#### Step 3: Implement (GREEN)

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Add to `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
```

Run test: ✅ PASSES

#### Step 4: Commit

```bash
git add .
git commit -m "feat(health): add health check endpoint"
git push origin feature/health-check
```

---

## 7. Resources

### Documentation

| Document | Purpose |
|----------|---------|
| [PROJECT_CONTEXT.md](../PROJECT_CONTEXT.md) | Complete project overview |
| [ARCHITECTURE_DDD.md](../ARCHITECTURE_DDD.md) | Architecture details |
| [CURRENT_STATUS.md](../CURRENT_STATUS.md) | Current progress |
| [SPEC_DRIVEN_DESIGN.md](development/SPEC_DRIVEN_DESIGN.md) | Spec methodology |
| [TDD_GUIDE.md](development/TDD_GUIDE.md) | TDD guide |
| [GITFLOW.md](../GITFLOW.md) | Git workflow |

### Code Examples

**Eureka Server** (✅ Implemented):
- Location: `backend-cloud/eureka-server/`
- README: Complete implementation example
- Tests: Unit and integration tests

### Useful Commands

```bash
# Run tests
mvn test

# Run with coverage
mvn clean test jacoco:report

# Build
mvn clean package

# Run service
mvn spring-boot:run

# Docker build
docker build -t service-name .

# Docker run
docker run -p 8081:8081 service-name
```

### Getting Help

1. **Check Documentation**: Start with PROJECT_CONTEXT.md
2. **Review Examples**: Look at eureka-server implementation
3. **Ask Questions**: Contact team lead
4. **Pair Programming**: Work with experienced team member

---

## 🎯 Checklist for First Week

### Day 1: Setup
- [ ] Install all prerequisites
- [ ] Clone repository
- [ ] Setup IDE
- [ ] Run Eureka Server successfully

### Day 2: Understanding
- [ ] Read PROJECT_CONTEXT.md
- [ ] Read ARCHITECTURE_DDD.md
- [ ] Understand DDD concepts
- [ ] Understand microservices architecture

### Day 3: Methodology
- [ ] Read SPEC_DRIVEN_DESIGN.md
- [ ] Read TDD_GUIDE.md
- [ ] Understand Red-Green-Refactor cycle
- [ ] Review spec template

### Day 4: Code Review
- [ ] Review Eureka Server code
- [ ] Understand project structure
- [ ] Review tests
- [ ] Understand Docker configuration

### Day 5: First Contribution
- [ ] Create simple feature spec
- [ ] Implement with TDD
- [ ] Submit pull request
- [ ] Code review

---

## 🚀 Next Steps

After completing onboarding:

1. **Pick a Task**: Check CURRENT_STATUS.md for next steps
2. **Create Spec**: Follow Spec-Driven Design
3. **Implement with TDD**: Red-Green-Refactor
4. **Submit PR**: Get code review
5. **Iterate**: Learn and improve

---

## 📞 Contact

- **Team Lead**: Oscar
- **Repository**: https://github.com/4901oscar/HIS-Project
- **Branch**: develop

---

**Welcome to the team! 🎉**

**Version**: 1.0.0  
**Last Updated**: April 10, 2026
