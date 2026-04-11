# 📚 MedFlow HIS - Documentation Index

Complete index of all project documentation for easy navigation.

---

## 🎯 Start Here

### For New Developers
1. **[ONBOARDING.md](docs/ONBOARDING.md)** - Start here! Complete onboarding guide
2. **[PROJECT_CONTEXT.md](PROJECT_CONTEXT.md)** - Complete project overview
3. **[CURRENT_STATUS.md](CURRENT_STATUS.md)** - Current progress and next steps

### For Returning to the Project
1. **[CURRENT_STATUS.md](CURRENT_STATUS.md)** - Check current progress
2. **[PROJECT_CONTEXT.md](PROJECT_CONTEXT.md)** - Refresh your memory
3. **[ARCHITECTURE_DDD.md](ARCHITECTURE_DDD.md)** - Review architecture

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
