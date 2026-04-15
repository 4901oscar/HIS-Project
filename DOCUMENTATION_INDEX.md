# 📚 MedFlow HIS - Documentation Index

Complete index of all project documentation for easy navigation.

**Última actualización**: Abril 13, 2026

---

## ⭐ START HERE

### Documento Maestro
**[MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)** - Especificaciones Core del MVP

Este es el documento más importante del proyecto. Contiene:
- Stack tecnológico real
- Arquitectura de software (Hexagonal vs MVC)
- Estrategia de base de datos (Schema-per-Service)
- Roles y permisos (RBAC - 8 roles)
- Catálogo completo de microservicios
- Características descartadas del MVP
- Flujo de trabajo con IA (Kiro)

### Para Nuevos Desarrolladores
1. **[MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)** - Leer primero
2. **[README.md](README.md)** - Punto de entrada del proyecto
3. **[CURRENT_STATUS.md](CURRENT_STATUS.md)** - Estado actual y progreso
4. **[GITFLOW.md](GITFLOW.md)** - Workflow de Git

### Para Retomar el Proyecto
1. **[CURRENT_STATUS.md](CURRENT_STATUS.md)** - Ver progreso actual
2. **[MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)** - Refrescar especificaciones
3. Revisar últimos commits en Git

---

## 📖 Documentación Principal

### Nivel Raíz
| Documento | Descripción | Cuándo Leer |
|-----------|-------------|-------------|
| **[MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)** | ⭐ Especificaciones Core del MVP | PRIMERO |
| [README.md](README.md) | Punto de entrada del proyecto | Setup inicial |
| [CURRENT_STATUS.md](CURRENT_STATUS.md) | Estado actual y roadmap | Diario/Semanal |
| [GITFLOW.md](GITFLOW.md) | Workflow de Git | Antes de commits |
| [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md) | Arquitectura DDD detallada | Entender arquitectura |

---

## 🏗️ Documentación por Componente

### Infraestructura (Backend Cloud)

#### Eureka Server ✅
| Documento | Descripción |
|-----------|-------------|
| [backend-cloud/eureka-server/README.md](backend-cloud/eureka-server/README.md) | Documentación técnica completa |
| [backend-cloud/eureka-server/QUICKSTART.md](backend-cloud/eureka-server/QUICKSTART.md) | Guía de inicio rápido (5 min) |
| [backend-cloud/eureka-server/IMPLEMENTATION_SUMMARY.md](backend-cloud/eureka-server/IMPLEMENTATION_SUMMARY.md) | Resumen de implementación |

#### API Gateway ✅
| Documento | Descripción |
|-----------|-------------|
| [backend-cloud/api-gateway/README.md](backend-cloud/api-gateway/README.md) | Documentación técnica completa |
| [backend-cloud/api-gateway/QUICKSTART.md](backend-cloud/api-gateway/QUICKSTART.md) | Guía de inicio rápido (5 min) |
| [backend-cloud/api-gateway/API_DOCUMENTATION.md](backend-cloud/api-gateway/API_DOCUMENTATION.md) | Documentación de API |
| [backend-cloud/api-gateway/DOCKER_DEPLOYMENT_TEST.md](backend-cloud/api-gateway/DOCKER_DEPLOYMENT_TEST.md) | Pruebas de Docker |

### Servicios de Negocio (Backend Services)

| Servicio | Estado | Documentación |
|----------|--------|---------------|
| Auth Service | ⏳ Siguiente | Pendiente |
| Patient Service | ⏳ Pendiente | Pendiente |
| Clinical Service | ⏳ Pendiente | Pendiente |
| Lab Service | ⏳ Pendiente | Pendiente |
| Pharmacy Service | ⏳ Pendiente | Pendiente |
| Billing Service | ⏳ Pendiente | Pendiente |

### Frontend

| Documento | Descripción |
|-----------|-------------|
| [frontend-medflow/README.md](frontend-medflow/README.md) | Documentación del frontend |
| [frontend-medflow/AUTHENTICATION.md](frontend-medflow/AUTHENTICATION.md) | Sistema de autenticación |
| [frontend-medflow/ESTRUCTURA_PERFILES.md](frontend-medflow/ESTRUCTURA_PERFILES.md) | Estructura de perfiles por rol |

### Casos de Uso (Para Presentación Final)

| Documento | Descripción |
|-----------|-------------|
| [Documentación/casos-de-uso/](Documentación/casos-de-uso/) | Casos de uso del sistema (formato .docx) |
| - 00__CU_Portal_Web_CLIENTE.docx | Portal web del cliente |
| - 00_1__CU_Inicio_Sesion_EMPLEADO.docx | Inicio de sesión de empleados |
| - 01__CU_Registro_Paciente_CLIENTE.docx | Registro de pacientes |
| - 15__Reglas_de_Negocio_CLIENTE.docx | Reglas de negocio |

**Nota**: Estos documentos serán comparados con la funcionalidad implementada en la presentación final.

---

## 📋 Especificaciones y Metodologías

### Specs de Features (.kiro/specs/)
| Feature | Estado | Ubicación |
|---------|--------|-----------|
| API Gateway | ✅ Implementado | `.kiro/specs/api-gateway/` |
| Patient Appointment Scheduling | ⏳ En progreso | `.kiro/specs/patient-appointment-scheduling/` |

### Metodologías (docs/development/)
| Documento | Descripción |
|-----------|-------------|
| [docs/development/SPEC_DRIVEN_DESIGN.md](docs/development/SPEC_DRIVEN_DESIGN.md) | Metodología Spec-Driven Design |
| [docs/development/TDD_GUIDE.md](docs/development/TDD_GUIDE.md) | Guía de Test-Driven Development |

### Templates (docs/specs/)
| Documento | Descripción |
|-----------|-------------|
| [docs/specs/SPEC_TEMPLATE.md](docs/specs/SPEC_TEMPLATE.md) | Template para crear specs |

---

## 🗂️ Estructura de Documentación

```
medflow-his/
├── MVP_CORE_SPECS.md              ⭐ DOCUMENTO MAESTRO
├── README.md                       Punto de entrada
├── CURRENT_STATUS.md               Estado y progreso
├── GITFLOW.md                      Workflow de Git
├── ARCHITECTURE_DDD.md             Arquitectura DDD
├── DOCUMENTATION_INDEX.md          Este archivo
│
├── backend-cloud/                  Infraestructura
│   ├── README.md
│   ├── eureka-server/
│   │   ├── README.md
│   │   ├── QUICKSTART.md
│   │   └── IMPLEMENTATION_SUMMARY.md
│   └── api-gateway/
│       ├── README.md
│       ├── QUICKSTART.md
│       ├── API_DOCUMENTATION.md
│       └── DOCKER_DEPLOYMENT_TEST.md
│
├── backend-services/               Servicios de negocio
│   ├── README.md
│   └── {service-name}/
│       ├── README.md
│       └── QUICKSTART.md
│
├── frontend-medflow/               Frontend
│   ├── README.md
│   ├── AUTHENTICATION.md
│   └── ESTRUCTURA_PERFILES.md
│
├── .kiro/                          Kiro AI
│   ├── specs/                     Especificaciones
│   │   ├── api-gateway/
│   │   └── {feature-name}/
│   └── steering/                  Reglas
│       └── project-standards.md
│
└── docs/                           Documentación general
    ├── development/               Guías de desarrollo
    │   ├── SPEC_DRIVEN_DESIGN.md
    │   └── TDD_GUIDE.md
    └── specs/                     Templates
        └── SPEC_TEMPLATE.md
```

---

## 🔍 Búsqueda Rápida

### Por Tarea

| Tarea | Documentos a Leer |
|-------|-------------------|
| Entender el proyecto | MVP_CORE_SPECS.md → README.md |
| Ver progreso | CURRENT_STATUS.md |
| Crear nueva feature | SPEC_DRIVEN_DESIGN.md → SPEC_TEMPLATE.md |
| Escribir tests | TDD_GUIDE.md |
| Hacer commits | GITFLOW.md |
| Levantar Eureka | backend-cloud/eureka-server/QUICKSTART.md |
| Levantar Gateway | backend-cloud/api-gateway/QUICKSTART.md |
| Entender arquitectura | MVP_CORE_SPECS.md → ARCHITECTURE_DDD.md |

### Por Rol

#### Nuevo Desarrollador
1. [MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)
2. [README.md](README.md)
3. [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md)
4. [SPEC_DRIVEN_DESIGN.md](docs/development/SPEC_DRIVEN_DESIGN.md)
5. [TDD_GUIDE.md](docs/development/TDD_GUIDE.md)

#### Desarrollador Retornando
1. [CURRENT_STATUS.md](CURRENT_STATUS.md)
2. [MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)
3. Revisar últimos commits

#### Project Manager
1. [CURRENT_STATUS.md](CURRENT_STATUS.md)
2. [MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)
3. [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md)

#### DevOps Engineer
1. [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md)
2. [docker-compose.yml](docker-compose.yml)
3. READMEs de cada servicio

---

## 📊 Estado de Documentación

| Categoría | Estado | Cobertura |
|-----------|--------|-----------|
| Especificaciones Core | ✅ Complete | 100% |
| Arquitectura | ✅ Complete | 100% |
| Infraestructura | ✅ Complete | 100% |
| Servicios de Negocio | ⏳ Pendiente | 0% |
| Frontend | ✅ Complete | 100% |
| Metodologías | ✅ Complete | 100% |
| Templates | ✅ Complete | 100% |

---

## 🎯 Principios de Documentación

### 1. Documento Maestro
- **MVP_CORE_SPECS.md** es la fuente de verdad
- Todos los demás documentos referencian a este

### 2. Siempre Actualizada
- Actualizar docs cuando cambia el código
- Revisar docs en code reviews
- Marcar secciones desactualizadas

### 3. Fácil de Encontrar
- Usar este índice
- Nombres de archivo claros
- Estructura consistente

### 4. Completa pero Concisa
- Cubrir todos los temas
- Sin detalles innecesarios
- Enlaces a más información

---

## 🔗 Enlaces Externos

### Spring Boot
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)

### Testing
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### DDD
- "Domain-Driven Design" by Eric Evans
- "Implementing Domain-Driven Design" by Vaughn Vernon

### TDD
- "Test Driven Development: By Example" by Kent Beck

---

## 📝 Contribuir a la Documentación

### Agregar Nueva Documentación

1. **Determinar ubicación**:
   - Nivel proyecto: Raíz
   - Guías desarrollo: `docs/development/`
   - Arquitectura: `docs/architecture/`
   - Specs: `.kiro/specs/{feature-name}/`

2. **Seguir templates**:
   - Usar docs existentes como ejemplo
   - Seguir estándares de markdown
   - Incluir tabla de contenidos

3. **Actualizar este índice**:
   - Agregar entrada a sección apropiada
   - Actualizar estado de documentación
   - Commit con cambios de docs

4. **Revisar**:
   - Verificar claridad
   - Probar enlaces
   - Validar ejemplos

---

## 🆘 Ayuda y Soporte

### ¿No Encuentras lo que Necesitas?

1. **Buscar en este índice** - Usa Ctrl+F
2. **Revisar MVP_CORE_SPECS.md** - Documento maestro
3. **Revisar CURRENT_STATUS.md** - Últimas actualizaciones
4. **Preguntar al equipo** - Oscar (Team Lead)

### ¿Problemas con la Documentación?

- ¿Info desactualizada? ¡Actualízala!
- ¿Info faltante? ¡Agrégala!
- ¿Info confusa? ¡Claríficala!
- ¿Enlaces rotos? ¡Arréglalos!

---

## 📞 Contacto

- **Repositorio**: https://github.com/4901oscar/HIS-Project
- **Branch**: develop
- **Team Lead**: Oscar

---

**Última actualización**: Abril 13, 2026  
**Versión**: 2.0.0  
**Mantenido por**: MedFlow Team

---

## 🎉 ¡Todo Listo!

Tienes acceso a documentación completa y consolidada. Comienza con **[MVP_CORE_SPECS.md](MVP_CORE_SPECS.md)** para entender el proyecto.

**Happy coding! 🚀**

---

## 📖 Core Documentation

### Project Overview
| Document | Description | When to Read |
|----------|-------------|--------------|
| [README.md](README.md) | Main project README | First time setup |
| [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) | Complete project context | Starting or resuming |
| [CURRENT_STATUS.md](CURRENT_STATUS.md) | Current progress & roadmap | Daily/Weekly |
| [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | This file | Finding docs |

### Architecture
| Document | Description | When to Read |
|----------|-------------|--------------|
| [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md) | Complete DDD architecture | Understanding system |
| [backend-cloud/README.md](backend-cloud/README.md) | Infrastructure services | Working on infra |
| [backend-services/README.md](backend-services/README.md) | Business services | Working on services |

### Development Process
| Document | Description | When to Read |
|----------|-------------|--------------|
| [GITFLOW.md](GITFLOW.md) | Git workflow | Before committing |
| [docs/development/SPEC_DRIVEN_DESIGN.md](docs/development/SPEC_DRIVEN_DESIGN.md) | Spec methodology | Before new feature |
| [docs/development/TDD_GUIDE.md](docs/development/TDD_GUIDE.md) | TDD guide | Writing tests |

---

## 🏗️ Implementation Guides

### Eureka Server (✅ Implemented)
| Document | Description |
|----------|-------------|
| [backend-cloud/eureka-server/README.md](backend-cloud/eureka-server/README.md) | Complete documentation |
| [backend-cloud/eureka-server/QUICKSTART.md](backend-cloud/eureka-server/QUICKSTART.md) | 5-minute quick start |
| [backend-cloud/eureka-server/VISUAL_GUIDE.md](backend-cloud/eureka-server/VISUAL_GUIDE.md) | Visual diagrams |
| [backend-cloud/eureka-server/IMPLEMENTATION_SUMMARY.md](backend-cloud/eureka-server/IMPLEMENTATION_SUMMARY.md) | Implementation summary |

### API Gateway (⏳ Next)
- To be created following Spec-Driven Design

---

## 📋 Templates & Standards

### Spec Templates
| Document | Description | When to Use |
|----------|-------------|-------------|
| [docs/specs/SPEC_TEMPLATE.md](docs/specs/SPEC_TEMPLATE.md) | Complete spec template | Creating new spec |

### Kiro AI Guidelines
| Document | Description | Purpose |
|----------|-------------|---------|
| [.kiro/steering/project-standards.md](.kiro/steering/project-standards.md) | Project standards for Kiro | AI assistant guidelines |

---

## 🎓 Learning Resources

### Methodologies
| Topic | Document | Description |
|-------|----------|-------------|
| Spec-Driven Design | [SPEC_DRIVEN_DESIGN.md](docs/development/SPEC_DRIVEN_DESIGN.md) | How to write specs |
| Test-Driven Development | [TDD_GUIDE.md](docs/development/TDD_GUIDE.md) | How to do TDD |
| Domain-Driven Design | [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md) | DDD principles |
| Git Flow | [GITFLOW.md](GITFLOW.md) | Git workflow |

### Examples
| Example | Location | What it Shows |
|---------|----------|---------------|
| Eureka Server | `backend-cloud/eureka-server/` | Complete microservice |
| Spec Template | `docs/specs/SPEC_TEMPLATE.md` | How to write specs |
| TDD Examples | `docs/development/TDD_GUIDE.md` | TDD cycle examples |

---

## 🗂️ Documentation Structure

```
medflow-his/
├── README.md                           # Main README
├── PROJECT_CONTEXT.md                  # Complete context
├── CURRENT_STATUS.md                   # Current progress
├── ARCHITECTURE_DDD.md                 # Architecture
├── GITFLOW.md                          # Git workflow
├── DOCUMENTATION_INDEX.md              # This file
│
├── docs/                               # Documentation
│   ├── ONBOARDING.md                  # Onboarding guide
│   ├── specs/                         # Spec templates
│   │   └── SPEC_TEMPLATE.md
│   ├── development/                   # Development guides
│   │   ├── SPEC_DRIVEN_DESIGN.md
│   │   └── TDD_GUIDE.md
│   ├── architecture/                  # Architecture docs
│   └── testing/                       # Testing docs
│
├── .kiro/                             # Kiro AI config
│   ├── specs/                         # Feature specs
│   └── steering/                      # AI guidelines
│       └── project-standards.md
│
├── backend-cloud/                     # Infrastructure
│   ├── eureka-server/
│   │   ├── README.md
│   │   ├── QUICKSTART.md
│   │   ├── VISUAL_GUIDE.md
│   │   └── IMPLEMENTATION_SUMMARY.md
│   └── api-gateway/
│
└── backend-services/                  # Business services
    ├── README.md
    ├── auth-service/
    ├── patient-service/
    ├── clinical-service/
    ├── lab-service/
    ├── pharmacy-service/
    └── billing-service/
```

---

## 🔍 Quick Reference

### Common Tasks

| Task | Documents to Read |
|------|-------------------|
| Starting new feature | SPEC_DRIVEN_DESIGN.md → SPEC_TEMPLATE.md |
| Writing tests | TDD_GUIDE.md |
| Understanding architecture | ARCHITECTURE_DDD.md |
| Git workflow | GITFLOW.md |
| Onboarding new dev | ONBOARDING.md |
| Checking progress | CURRENT_STATUS.md |
| Finding documentation | DOCUMENTATION_INDEX.md (this file) |

### By Role

#### New Developer
1. [ONBOARDING.md](docs/ONBOARDING.md)
2. [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md)
3. [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md)
4. [SPEC_DRIVEN_DESIGN.md](docs/development/SPEC_DRIVEN_DESIGN.md)
5. [TDD_GUIDE.md](docs/development/TDD_GUIDE.md)

#### Returning Developer
1. [CURRENT_STATUS.md](CURRENT_STATUS.md)
2. [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md)
3. Review last commits

#### Project Manager
1. [CURRENT_STATUS.md](CURRENT_STATUS.md)
2. [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md)
3. [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md)

#### DevOps Engineer
1. [ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md)
2. [docker-compose.yml](docker-compose.yml)
3. Service READMEs

---

## 📊 Documentation Status

| Category | Status | Coverage |
|----------|--------|----------|
| Project Overview | ✅ Complete | 100% |
| Architecture | ✅ Complete | 100% |
| Development Process | ✅ Complete | 100% |
| Templates | ✅ Complete | 100% |
| Onboarding | ✅ Complete | 100% |
| Eureka Server | ✅ Complete | 100% |
| API Gateway | ⏳ Pending | 0% |
| Auth Service | ⏳ Pending | 0% |
| Other Services | ⏳ Pending | 0% |

---

## 🎯 Documentation Principles

### 1. Always Up-to-Date
- Update docs when code changes
- Review docs in code reviews
- Mark outdated sections

### 2. Easy to Find
- Use this index
- Clear file names
- Consistent structure

### 3. Easy to Understand
- Clear language
- Visual diagrams
- Examples included

### 4. Complete but Concise
- Cover all topics
- No unnecessary details
- Link to more info

---

## 🔗 External Resources

### Spring Boot
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)

### Testing
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)

### DDD
- "Domain-Driven Design" by Eric Evans
- "Implementing Domain-Driven Design" by Vaughn Vernon

### TDD
- "Test Driven Development: By Example" by Kent Beck

---

## 📝 Contributing to Documentation

### Adding New Documentation

1. **Determine location**:
   - Project-level: Root directory
   - Development guides: `docs/development/`
   - Architecture: `docs/architecture/`
   - Specs: `.kiro/specs/{feature-name}/`

2. **Follow templates**:
   - Use existing docs as examples
   - Follow markdown standards
   - Include table of contents for long docs

3. **Update this index**:
   - Add entry to appropriate section
   - Update documentation status
   - Commit with docs changes

4. **Review**:
   - Check for clarity
   - Verify links work
   - Test examples

---

## 🆘 Help & Support

### Can't Find What You Need?

1. **Search this index** - Use Ctrl+F
2. **Check PROJECT_CONTEXT.md** - Comprehensive overview
3. **Review CURRENT_STATUS.md** - Latest updates
4. **Ask team lead** - Oscar

### Documentation Issues?

- Outdated info? Update it!
- Missing info? Add it!
- Unclear? Clarify it!
- Broken links? Fix them!

---

## 📞 Contact

- **Repository**: https://github.com/4901oscar/HIS-Project
- **Branch**: develop
- **Team Lead**: Oscar

---

**Last Updated**: April 10, 2026  
**Version**: 1.0.0  
**Maintained by**: MedFlow Team

---

## 🎉 You're All Set!

You now have access to complete, professional documentation for the entire project. Start with [ONBOARDING.md](docs/ONBOARDING.md) if you're new, or [CURRENT_STATUS.md](CURRENT_STATUS.md) if you're returning.

**Happy coding! 🚀**
