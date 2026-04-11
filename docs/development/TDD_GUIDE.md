# 🧪 Test-Driven Development (TDD) - Guía Completa

## 🎯 ¿Qué es TDD?

**Test-Driven Development** es una práctica donde escribimos tests ANTES de escribir el código de producción.

### El Mantra de TDD

```
❌ RED → ✅ GREEN → ♻️ REFACTOR
```

1. **RED**: Escribir un test que falla
2. **GREEN**: Escribir el código mínimo para que pase
3. **REFACTOR**: Mejorar el código manteniendo los tests verdes

---

## 🔄 El Ciclo TDD Completo

```
┌─────────────────────────────────────────┐
│ 1. Escribir test que falla (RED)       │
│    - Test describe comportamiento       │
│    - Test falla porque no hay código    │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 2. Escribir código mínimo (GREEN)      │
│    - Solo lo necesario para pasar       │
│    - No optimizar todavía               │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│ 3. Refactorizar (REFACTOR)             │
│    - Mejorar diseño                     │
│    - Eliminar duplicación               │
│    - Tests siguen pasando               │
└──────────────┬──────────────────────────┘
               │
               ▼
         ¿Más funcionalidad?
               │
               └──> Volver al paso 1
```

---

## 📚 Tipos de Tests

### 1. Unit Tests (Pruebas Unitarias)

**¿Qué prueban?**
- Una clase o método individual
- Sin dependencias externas (se mockean)

**Ejemplo:**
```java
@Test
void shouldValidateJwtToken() {
    // Arrange
    String validToken = "eyJhbGc...";
    JwtValidator validator = new JwtValidator();
    
    // Act
    boolean isValid = validator.validate(validToken);
    
    // Assert
    assertTrue(isValid);
}
```

**Características:**
- ⚡ Rápidos (milisegundos)
- 🔒 Aislados
- 🎯 Específicos

### 2. Integration Tests (Pruebas de Integración)

**¿Qué prueban?**
- Integración entre componentes
- Con dependencias reales (DB, APIs)

**Ejemplo:**
```java
@SpringBootTest
@Testcontainers
class PatientServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15");
    
    @Autowired
    PatientService patientService;
    
    @Test
    void shouldSavePatient() {
        // Arrange
        Patient patient = new Patient("John", "Doe");
        
        // Act
        Patient saved = patientService.save(patient);
        
        // Assert
        assertNotNull(saved.getId());
    }
}
```

**Características:**
- 🐢 Más lentos (segundos)
- 🔗 Con dependencias
- 🌐 Más realistas

### 3. E2E Tests (Pruebas End-to-End)

**¿Qué prueban?**
- Flujo completo del usuario
- Desde frontend hasta base de datos

**Ejemplo:**
```java
@Test
void shouldCompletePatientRegistrationFlow() {
    // 1. Usuario abre formulario
    // 2. Llena datos del paciente
    // 3. Envía formulario
    // 4. Sistema guarda en DB
    // 5. Usuario ve confirmación
}
```

**Características:**
- 🐌 Lentos (minutos)
- 🎭 Simulan usuario real
- 🔍 Detectan problemas de integración

---

## 🏗️ Estructura de un Test

### Patrón AAA (Arrange-Act-Assert)

```java
@Test
void testName() {
    // ARRANGE (Preparar)
    // - Crear objetos
    // - Configurar mocks
    // - Preparar datos
    
    // ACT (Actuar)
    // - Ejecutar el método a probar
    
    // ASSERT (Verificar)
    // - Verificar el resultado
}
```

### Ejemplo Completo

```java
@Test
void shouldCalculateTotalPrice() {
    // ARRANGE
    Product product = new Product("Laptop", 1000.0);
    int quantity = 2;
    double taxRate = 0.16;
    PriceCalculator calculator = new PriceCalculator();
    
    // ACT
    double total = calculator.calculateTotal(product, quantity, taxRate);
    
    // ASSERT
    assertEquals(2320.0, total, 0.01);
}
```

---

## 🎯 TDD en MedFlow HIS

### Ejemplo: Implementar JWT Validation

#### Paso 1: RED - Escribir test que falla

```java
package com.medflow.gateway.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtValidatorTest {
    
    @Test
    void shouldReturnTrueForValidToken() {
        // Arrange
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        JwtValidator validator = new JwtValidator("secret-key");
        
        // Act
        boolean isValid = validator.validate(validToken);
        
        // Assert
        assertTrue(isValid);
    }
}
```

**Ejecutar test**: ❌ FALLA (JwtValidator no existe)

#### Paso 2: GREEN - Código mínimo

```java
package com.medflow.gateway.security;

public class JwtValidator {
    private String secretKey;
    
    public JwtValidator(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public boolean validate(String token) {
        // Implementación mínima
        return true; // Hardcoded para pasar el test
    }
}
```

**Ejecutar test**: ✅ PASA

#### Paso 3: Agregar más tests (RED)

```java
@Test
void shouldReturnFalseForInvalidToken() {
    // Arrange
    String invalidToken = "invalid-token";
    JwtValidator validator = new JwtValidator("secret-key");
    
    // Act
    boolean isValid = validator.validate(invalidToken);
    
    // Assert
    assertFalse(isValid);
}

@Test
void shouldReturnFalseForExpiredToken() {
    // Arrange
    String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."; // Token expirado
    JwtValidator validator = new JwtValidator("secret-key");
    
    // Act
    boolean isValid = validator.validate(expiredToken);
    
    // Assert
    assertFalse(isValid);
}
```

**Ejecutar tests**: ❌ FALLAN (implementación hardcoded)

#### Paso 4: GREEN - Implementación real

```java
package com.medflow.gateway.security;

import io.jsonwebtoken.*;
import java.util.Date;

public class JwtValidator {
    private String secretKey;
    
    public JwtValidator(String secretKey) {
        this.secretKey = secretKey;
    }
    
    public boolean validate(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token);
            
            // Verificar expiración
            Date expiration = claims.getBody().getExpiration();
            return expiration.after(new Date());
            
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

**Ejecutar tests**: ✅ TODOS PASAN

#### Paso 5: REFACTOR - Mejorar código

```java
package com.medflow.gateway.security;

import io.jsonwebtoken.*;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtValidator {
    private static final Logger logger = LoggerFactory.getLogger(JwtValidator.class);
    private final String secretKey;
    
    public JwtValidator(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        this.secretKey = secretKey;
    }
    
    public boolean validate(String token) {
        if (token == null || token.isEmpty()) {
            logger.warn("Token is null or empty");
            return false;
        }
        
        try {
            Jws<Claims> claims = parseToken(token);
            return isNotExpired(claims);
        } catch (JwtException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    private Jws<Claims> parseToken(String token) {
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token);
    }
    
    private boolean isNotExpired(Jws<Claims> claims) {
        Date expiration = claims.getBody().getExpiration();
        return expiration.after(new Date());
    }
}
```

**Ejecutar tests**: ✅ TODOS SIGUEN PASANDO

---

## 🛠️ Herramientas de Testing

### JUnit 5

**Anotaciones principales:**
```java
@Test                    // Marca un método como test
@BeforeEach             // Se ejecuta antes de cada test
@AfterEach              // Se ejecuta después de cada test
@BeforeAll              // Se ejecuta una vez antes de todos los tests
@AfterAll               // Se ejecuta una vez después de todos los tests
@Disabled               // Deshabilita un test
@DisplayName("...")     // Nombre descriptivo del test
```

**Assertions:**
```java
assertEquals(expected, actual);
assertNotEquals(expected, actual);
assertTrue(condition);
assertFalse(condition);
assertNull(object);
assertNotNull(object);
assertThrows(Exception.class, () -> {...});
assertTimeout(Duration.ofSeconds(1), () -> {...});
```

### Mockito

**Crear mocks:**
```java
@Mock
private UserRepository userRepository;

@InjectMocks
private UserService userService;
```

**Configurar comportamiento:**
```java
when(userRepository.findById(1L))
    .thenReturn(Optional.of(new User("John")));
```

**Verificar llamadas:**
```java
verify(userRepository).save(any(User.class));
verify(userRepository, times(2)).findAll();
verify(userRepository, never()).delete(any());
```

### TestContainers

**Base de datos en tests:**
```java
@Testcontainers
class PatientServiceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

---

## 📊 Cobertura de Tests

### ¿Qué es cobertura?

Porcentaje de código ejecutado por los tests.

### Objetivo

- **Mínimo**: 80% de cobertura
- **Ideal**: 90%+ de cobertura
- **Crítico**: 100% en lógica de negocio

### Herramienta: JaCoCo

**Configuración en pom.xml:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Generar reporte:**
```bash
mvn clean test
# Reporte en: target/site/jacoco/index.html
```

---

## 🎯 Best Practices

### Nombres de Tests

✅ **BUENO**: `shouldReturnUserWhenIdExists()`  
❌ **MALO**: `test1()`

✅ **BUENO**: `shouldThrowExceptionWhenEmailIsInvalid()`  
❌ **MALO**: `testEmail()`

### Un Test, Una Cosa

✅ **BUENO**: Test específico
```java
@Test
void shouldReturnTrueForValidEmail() {
    assertTrue(validator.isValid("user@example.com"));
}

@Test
void shouldReturnFalseForInvalidEmail() {
    assertFalse(validator.isValid("invalid-email"));
}
```

❌ **MALO**: Test que prueba múltiples cosas
```java
@Test
void testEmail() {
    assertTrue(validator.isValid("user@example.com"));
    assertFalse(validator.isValid("invalid-email"));
    assertFalse(validator.isValid(null));
}
```

### Tests Independientes

✅ Cada test debe poder ejecutarse solo  
✅ No depender del orden de ejecución  
✅ Limpiar estado después de cada test  

### Tests Rápidos

✅ Unit tests < 100ms  
✅ Integration tests < 5s  
✅ E2E tests < 30s  

---

## 📋 Checklist de TDD

Antes de escribir código de producción:

- [ ] ¿Escribí el test primero?
- [ ] ¿El test falla por la razón correcta?
- [ ] ¿Escribí el código mínimo para pasar?
- [ ] ¿Todos los tests pasan?
- [ ] ¿Refactoricé el código?
- [ ] ¿Los tests siguen pasando después del refactor?
- [ ] ¿El código es legible?
- [ ] ¿Hay duplicación que pueda eliminar?

---

## 🔗 Integración con Spec-Driven Design

### Workflow Completo

```
1. Escribir Spec (requirements.md, design.md, tasks.md)
   ↓
2. Para cada task:
   ↓
   2.1 Escribir test (RED)
   ↓
   2.2 Implementar código (GREEN)
   ↓
   2.3 Refactorizar (REFACTOR)
   ↓
   2.4 Marcar task como completa
   ↓
3. Validar contra acceptance criteria del spec
```

---

## 📚 Recursos

### Libros
- "Test Driven Development: By Example" - Kent Beck
- "Growing Object-Oriented Software, Guided by Tests" - Freeman & Pryce

### Documentos del Proyecto
- [SPEC_DRIVEN_DESIGN.md](./SPEC_DRIVEN_DESIGN.md)
- [PROJECT_CONTEXT.md](../../PROJECT_CONTEXT.md)
- [ARCHITECTURE_DDD.md](../../ARCHITECTURE_DDD.md)

---

**Versión**: 1.0.0  
**Última actualización**: Abril 10, 2026
