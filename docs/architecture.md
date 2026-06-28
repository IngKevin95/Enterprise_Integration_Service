# Arquitectura — Enterprise Integration Service

## 1. Resumen ejecutivo

Este microservicio moderniza el acceso a sistemas legacy exponiendo REST APIs seguras con autenticación JWT. Actúa como capa de integración entre clientes modernos y datos que viven en sistemas heredados, con persistencia en PostgreSQL para las transacciones gestionadas por el servicio.

```
┌─────────────┐     HTTPS      ┌─────────────────────────────┐
│   Cliente   │ ─────────────► │  Enterprise Integration     │
│  (app web,  │                │  Service (:8080)            │
│   mobile,   │                │                             │
│   postman)  │                │  JWT auth                   │
└─────────────┘                │  REST /api/v1/              │
                               │  Swagger UI                 │
                               └─────────┬───────────────────┘
                                         │
                      ┌──────────────────┼──────────────────┐
                      │                  │                   │
               ┌──────▼──────┐   ┌───────▼──────┐    (futuro)
               │ PostgreSQL  │   │ Sistema      │   ┌──────────┐
               │  (:5432)    │   │ Legacy       │   │ Gateway  │
               │             │   │ (stub/SOAP/  │   │ externo  │
               │ Transacc.   │   │  JDBC/REST)  │   └──────────┘
               │ Clientes    │   └──────────────┘
               └─────────────┘
```

**URLs de acceso local:**
- API: `http://localhost:8080/api/v1/`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`
- Métricas Prometheus: `http://localhost:8080/actuator/prometheus`

---

## 2. Stack tecnológico

| Tecnología | Versión | Razón de elección |
|---|---|---|
| Java | 21 (LTS) | Records para DTOs inmutables, pattern matching, virtual threads (Project Loom) |
| Spring Boot | 3.4.x | Jakarta EE 10, Observability API nativa, actuators mejorados |
| PostgreSQL | 16 | ACID, JSONB, extensible, estándar enterprise probado |
| Flyway | 10 | Migraciones versionadas, auditoría de cambios de esquema, compatible con CI/CD |
| JJWT | 0.12.6 | Biblioteca JWT moderna y mantenida, soporte HS256/RS256, API fluida |
| MapStruct | 1.6.3 | Mapeo en tiempo de compilación (zero reflection), rendimiento óptimo, errores en build |
| Testcontainers | 1.20.x | Tests de integración contra PostgreSQL real, misma imagen que producción |
| ArchUnit | 1.3.0 | Reglas de arquitectura como código, fallan en CI si se rompen las fronteras |
| Springdoc OpenAPI | 2.8.0 | OpenAPI 3.1, Swagger UI integrado, generación automática desde anotaciones |
| JaCoCo | 0.8.12 | Cobertura de instrucciones con umbral mínimo del 80% en CI |
| Checkstyle | 3.6.0 | Google Java Style Guide, consistencia de código sin debate en PRs |
| Docker | multi-stage | Build con Maven 3.9 + JRE 21 Alpine mínimo en runtime (imagen < 200MB) |

---

## 3. Arquitectura Hexagonal (Ports & Adapters)

### Principio fundamental

El dominio no depende de nadie. Las capas externas dependen del dominio, nunca al revés.

```
┌─────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE                        │
│                                                         │
│  ┌─────────────────┐         ┌──────────────────────┐  │
│  │  Driving Side   │         │   Driven Side        │  │
│  │  (in/web)       │         │   (out/persistence)  │  │
│  │                 │         │   (out/legacy)       │  │
│  │  Controllers    │         │                      │  │
│  │  DTOs           │         │   JPA Adapters       │  │
│  │  Security       │         │   Legacy Stub        │  │
│  └────────┬────────┘         └──────────┬───────────┘  │
│           │                             │               │
└───────────┼─────────────────────────────┼───────────────┘
            │ implementa Port IN           │ implementa Port OUT
            ▼                             ▼
┌───────────────────────────────────────────────────────────┐
│                    APPLICATION                             │
│                                                           │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  Ports IN (interfaces)   Ports OUT (interfaces)      │ │
│  │  TransactionUseCase      TransactionRepository       │ │
│  │  ClientQueryUseCase      ClientRepository            │ │
│  │                          LegacySystemPort            │ │
│  └──────────────────────────────────────────────────────┘ │
│                           ▲                               │
│                           │ usa                           │
│  ┌────────────────────────┴──────────────────────────┐   │
│  │  Services                                         │   │
│  │  TransactionService (@Service)                    │   │
│  │  ClientService (@Service)                         │   │
│  └───────────────────────────────────────────────────┘   │
│                           ▲                               │
└───────────────────────────┼───────────────────────────────┘
                            │ usa
┌───────────────────────────┼───────────────────────────────┐
│                    DOMAIN │                               │
│                           │                               │
│  ┌──────────────────────────────────────────────────────┐ │
│  │  Models (puros Java — zero Spring, zero JPA)         │ │
│  │  Transaction, Client, TransactionStatus              │ │
│  │                                                      │ │
│  │  Exceptions                                          │ │
│  │  NotFoundException, BusinessRuleException            │ │
│  │  InvalidStatusTransitionException                    │ │
│  └──────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────┘
```

### Por qué Hexagonal sobre Layered Architecture

| Aspecto | Layered (N-capas) | Hexagonal |
|---|---|---|
| Dependencias | Controller → Service → Repository → DB | Dominio no depende de nadie |
| Tests unitarios | Necesitan mocks de toda la cadena | Domain y application testeables sin Spring |
| Cambiar DB | Afecta Service y Controller | Solo cambia el adapter de persistencia |
| Cambiar sistema legacy | Requiere cambios en business logic | Solo se reemplaza el adapter |
| Legibilidad | Capas implícitas por convención | Contratos explícitos (interfaces Port) |

### Tabla de puertos

| Puerto | Tipo | Interfaz | Implementaciones |
|---|---|---|---|
| TransactionUseCase | Driving (in) | `application/port/in/` | TransactionController llama a esta interfaz |
| ClientQueryUseCase | Driving (in) | `application/port/in/` | ClientController llama a esta interfaz |
| TransactionRepository | Driven (out) | `application/port/out/` | TransactionJpaAdapter |
| ClientRepository | Driven (out) | `application/port/out/` | ClientJpaAdapter |
| LegacySystemPort | Driven (out) | `application/port/out/` | LegacySystemStubAdapter (demo), reemplazable |

---

## 4. Modelo de dominio

### Entidades

```
┌─────────────────────────────────────────┐
│  Client                                  │
│─────────────────────────────────────────│
│  id: UUID                               │
│  name: String                           │
│  documentNumber: String (único)         │
│  email: String                          │
│  legacyId: String (ID en sistema viejo) │
└─────────────────────────────────────────┘
            │ 1
            │ tiene muchas
            ▼ N
┌─────────────────────────────────────────┐
│  Transaction                            │
│─────────────────────────────────────────│
│  id: UUID                               │
│  clientId: UUID (referencia)            │
│  amount: BigDecimal (≥ 0.01)           │
│  description: String                    │
│  status: TransactionStatus              │
│  createdAt: LocalDateTime               │
│  updatedAt: LocalDateTime               │
└─────────────────────────────────────────┘
```

### Ciclo de vida de una transacción

```
            crear
   ─────────────────► PENDING
                         │
              procesar   │    cancelar
                         ├────────────► CANCELLED
                         │
                         ▼
                     PROCESSING
                         │
              completar  │    fallar
                         ├────────────► FAILED
                         │
                         ▼
                     COMPLETED
```

**Transiciones válidas:**
- `PENDING` → `PROCESSING`, `CANCELLED`
- `PROCESSING` → `COMPLETED`, `FAILED`
- `COMPLETED`, `FAILED`, `CANCELLED` → estado terminal (no permite más cambios)

**Validaciones de dominio:**
- `amount` mínimo 0.01 (centavos)
- `clientId` no puede ser null al crear
- `description` máximo 255 caracteres
- Cambio de estado a un estado inválido lanza `InvalidStatusTransitionException`

---

## 5. Diseño de la API REST

### Convenciones

- Versionado en path: `/api/v1/`
- Identificadores en UUID (no IDs secuenciales expuestos)
- Kebab-case en paths: `/api/v1/transactions/{id}/status-history`
- Paginación estándar Spring: `?page=0&size=20&sort=createdAt,desc`
- Errores: Problem Details RFC 7807

### Endpoints

#### Autenticación
| Método | Path | Auth | Request | Response | Códigos HTTP |
|--------|------|------|---------|----------|-------------|
| POST | `/api/v1/auth/login` | Público | `LoginRequest` (username, password) | `TokenResponse` (token, expiresIn) | 200, 401 |
| POST | `/api/v1/auth/refresh` | Bearer | — | `TokenResponse` | 200, 401 |

#### Transacciones
| Método | Path | Auth | Request | Response | Códigos HTTP |
|--------|------|------|---------|----------|-------------|
| GET | `/api/v1/transactions` | Bearer | `?page&size&sort` | `Page<TransactionResponse>` | 200, 401 |
| GET | `/api/v1/transactions/{id}` | Bearer | — | `TransactionResponse` | 200, 401, 404 |
| POST | `/api/v1/transactions` | Bearer | `CreateTransactionRequest` | `TransactionResponse` | 201, 400, 401 |
| PATCH | `/api/v1/transactions/{id}/status` | Bearer | `UpdateStatusRequest` (status) | `TransactionResponse` | 200, 400, 401, 404, 422 |

#### Clientes (proxied desde legacy)
| Método | Path | Auth | Request | Response | Códigos HTTP |
|--------|------|------|---------|----------|-------------|
| GET | `/api/v1/clients/{id}` | Bearer | — | `ClientResponse` | 200, 401, 404 |
| GET | `/api/v1/clients/{id}/transactions` | Bearer | `?page&size` | `Page<TransactionResponse>` | 200, 401, 404 |

### Formato de errores (RFC 7807 Problem Details)

```json
{
  "type": "https://api.empresa.com/errors/not-found",
  "title": "Transaction Not Found",
  "status": 404,
  "detail": "Transaction with id 'abc-123' does not exist",
  "instance": "/api/v1/transactions/abc-123"
}
```

```json
{
  "type": "https://api.empresa.com/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/api/v1/transactions",
  "violations": [
    { "field": "amount", "message": "must be greater than 0.01" }
  ]
}
```

---

## 6. Seguridad JWT

### Flujo de autenticación

```
Cliente                    API                      Base de datos
  │                         │                            │
  │  POST /auth/login       │                            │
  │  {username, password}   │                            │
  │ ───────────────────────►│                            │
  │                         │  SELECT user WHERE         │
  │                         │  username = ?              │
  │                         │ ──────────────────────────►│
  │                         │                            │
  │                         │  UserEntity (hashed pass)  │
  │                         │ ◄──────────────────────────│
  │                         │                            │
  │                         │  BCrypt.verify(password)   │
  │                         │  Hmac.sign(claims, secret) │
  │                         │                            │
  │  200 {token, expiresIn} │                            │
  │ ◄───────────────────────│                            │
  │                         │                            │
  │  GET /transactions      │                            │
  │  Authorization: Bearer  │                            │
  │ ───────────────────────►│                            │
  │                         │  JwtFilter.doFilter()      │
  │                         │  Hmac.verify(token, secret)│
  │                         │  SecurityContext.set(auth) │
  │                         │                            │
  │  200 {transactions}     │                            │
  │ ◄───────────────────────│                            │
```

### Claims del token

```json
{
  "sub": "admin",
  "roles": ["ROLE_USER"],
  "iat": 1735000000,
  "exp": 1735003600
}
```

### Rutas públicas vs protegidas

| Path | Auth |
|---|---|
| `POST /api/v1/auth/login` | Público |
| `GET /actuator/health` | Público |
| `GET /swagger-ui.html` | Público (dev) |
| `GET /api-docs/**` | Público (dev) |
| Todo lo demás | Bearer token requerido |

### Consideraciones de seguridad

- El secret debe ser mínimo 256 bits (32 bytes de entropía aleatoria)
- En producción: usar variable de entorno `JWT_SECRET`, nunca valor hardcoded
- No almacenar el token en `localStorage` (vulnerable a XSS): usar cookie `HttpOnly` en producción
- HTTPS obligatorio en producción — HTTP solo para desarrollo local
- El token no se almacena en servidor (stateless): para revocar sesiones, usar TTL corto o blacklist en Redis
- Contraseñas almacenadas con BCrypt (factor de costo = 10 por defecto en Spring)

---

## 7. Persistencia y migraciones

### Estrategia Flyway

Scripts versionados en `src/main/resources/db/migration/`:

```
V1__create_clients_table.sql     ← tabla clients con índices
V2__create_transactions_table.sql ← tabla transactions con FK a clients
V3__insert_test_users.sql         ← usuario admin para demo (BCrypt hash)
```

Flyway valida que los scripts aplicados coincidan con el checksum en la tabla `flyway_schema_history`. Cualquier modificación a un script ya aplicado falla el arranque — esto protege contra migraciones accidentales en producción.

### Diseño de tablas PostgreSQL

```sql
CREATE TABLE clients (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    document_number VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL,
    legacy_id       VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_clients_document ON clients(document_number);
CREATE INDEX idx_clients_legacy_id ON clients(legacy_id);

CREATE TABLE transactions (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id   UUID         NOT NULL REFERENCES clients(id),
    amount      NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    description VARCHAR(255),
    status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_client    ON transactions(client_id);
CREATE INDEX idx_transactions_status    ON transactions(status);
CREATE INDEX idx_transactions_created   ON transactions(created_at DESC);
```

**Índices y su razón:**
- `idx_clients_document`: búsqueda de cliente por número de documento (operación frecuente)
- `idx_clients_legacy_id`: lookup al sincronizar con sistema legacy
- `idx_transactions_client`: listado de transacciones por cliente (`/clients/{id}/transactions`)
- `idx_transactions_status`: filtrado por estado (reportes operacionales)
- `idx_transactions_created`: ordenamiento descendente en listados paginados

### Por qué `@Transactional(readOnly = true)`

Las queries de solo lectura con `readOnly=true`:
1. Le indican a Hibernate que no rastree cambios en las entidades (sin dirty checking)
2. Permiten al driver PostgreSQL redirigir la query a una réplica de lectura si hay una configurada
3. Reducen el overhead de sesión JPA en un 20-30% en operaciones de solo consulta

---

## 8. Integración con Sistema Legacy

### Patrón Adapter

```
┌──────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                       │
│                                                          │
│  ClientService                                           │
│     │                                                    │
│     │ usa (inyecta por constructor)                     │
│     ▼                                                    │
│  LegacySystemPort  ◄─── interfaz (application/port/out) │
└──────────────────────────────────────────────────────────┘
            ▲
            │ implementa
┌───────────┴──────────────────────────────────────────────┐
│               INFRASTRUCTURE LAYER                        │
│                                                          │
│  LegacySystemStubAdapter                                 │
│    - Lee de legacy-data.json en classpath                │
│    - Simula latencia y respuestas del sistema real       │
│    - Configurable via @Profile("!prod")                  │
│                                                          │
│  (futuro) LegacySystemSoapAdapter                        │
│    - SOAP client via Jakarta XML Web Services            │
│    - Solo cambia este archivo — nada más                 │
│                                                          │
│  (futuro) LegacySystemJdbcAdapter                        │
│    - JDBC directo a la BD legacy                         │
└──────────────────────────────────────────────────────────┘
```

### Cómo reemplazar el stub en producción

1. Crear `LegacySystemRealAdapter implements LegacySystemPort` en `infrastructure/adapter/out/legacy/`
2. Anotar con `@Primary` o usar `@Profile("prod")` para que Spring inyecte la implementación correcta
3. No tocar `ClientService`, `LegacySystemPort`, ni ningún test de aplicación
4. Solo agregar tests de integración para el nuevo adapter

### Demo data (`legacy-data.json`)

```json
{
  "clients": [
    {
      "legacyId": "LEG-001",
      "name": "Empresa Demo S.A.",
      "documentNumber": "900123456-7",
      "email": "contacto@empresademo.com"
    }
  ]
}
```

---

## 9. Estrategia de Testing

### Pirámide de tests

```
          /\
         /E2E\       10% — flujo completo en staging (manual/postman)
        /──────\
       /  Integ \    20% — Testcontainers: JPA adapters + Flyway + DB real
      /──────────\
     /    Unit    \  70% — JUnit 5 + Mockito: services, domain, JWT
    /______________\
```

### Tipos de test y sus herramientas

| Tipo | Herramienta | Qué valida | Velocidad |
|------|-------------|------------|-----------|
| Unit | JUnit 5 + Mockito | Domain model, Services, JwtTokenProvider | < 5s |
| Slice | `@WebMvcTest` + MockMvc | Controllers, serialización JSON, HTTP status | < 15s |
| Integration | Testcontainers (PostgreSQL real) | JPA adapters, Flyway migrations, queries | < 60s |
| Architecture | ArchUnit | Reglas de capas hexagonales | < 10s |

### Configuración Testcontainers

```java
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class TransactionJpaAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withReuse(true); // reutiliza contenedor entre tests del mismo proceso

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

### ArchUnit — reglas de arquitectura como código

```java
@AnalyzeClasses(packages = "com.empresa.integration")
class HexagonalArchitectureTest {

    // El dominio no puede importar nada de infrastructure ni application
    @ArchTest
    ArchRule domainIsIsolated = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infrastructure..")
        .orShould().dependOnClassesThat()
        .resideInAPackage("..application..");

    // Application no puede importar infrastructure
    @ArchTest
    ArchRule applicationDoesNotDependOnInfrastructure = noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat()
        .resideInAPackage("..infrastructure..");

    // Controllers no pueden llamar a repositorios JPA directamente
    @ArchTest
    ArchRule controllersGoThroughPorts = noClasses()
        .that().resideInAPackage("..adapter.in.web..")
        .should().dependOnClassesThat()
        .resideInAPackage("..adapter.out.persistence..");
}
```

**Umbral JaCoCo:** 80% de instrucciones. CI falla si baja de ese nivel. Clases excluidas del umbral: entidades JPA, DTOs (records — código generado por Java), clases de configuración de Spring.

---

## 10. CI/CD y GitFlow

### Estrategia de ramas

```
main ────────────────────────────────────────► producción
  ▲                                                │
  │ release PR + tag v1.0.0                        │
  │                                                │
develop ──────────────────────────────────────────
  ▲
  │ feature PRs (merge cuando CI verde)
  │
feature/nombre-feature
hotfix/nombre-fix ──────────────────────────────► main (hotfixes urgentes)
```

**Reglas de protección de ramas (configurar en GitHub Settings > Branches):**
- `main`: require PR, require CI verde, no force push, no delete
- `develop`: require PR, require CI verde

### GitHub Actions — ci.yml

```
PR abierto a develop/main
  │
  ▼
actions/checkout@v4
  │
  ▼
actions/setup-java@v4 (Java 21 Temurin, cache Maven)
  │
  ▼
mvn verify -P ci --batch-mode
  ├── Checkstyle (Google Java Style)
  ├── mvn compile
  ├── mvn test (JUnit 5 + Testcontainers)
  └── JaCoCo check (≥ 80%)
  │
  ▼
Upload jacoco-report artifact (retención 7 días)
```

### GitHub Actions — release.yml

```
git push tag v1.x.x
  │
  ▼
actions/checkout@v4
  │
  ▼
mvn package -DskipTests (JAR ya testeado en develop)
  │
  ▼
docker/login-action (GHCR con GITHUB_TOKEN)
  │
  ▼
docker/build-push-action (docker/Dockerfile multi-stage)
  │
  ▼
Imagen publicada en ghcr.io/IngKevin95/enterprise-integration-service:1.0.0
```

### Convención de commits (Conventional Commits)

| Prefijo | Cuándo usarlo |
|---|---|
| `feat:` | Nueva funcionalidad visible al usuario |
| `fix:` | Corrección de bug |
| `refactor:` | Refactoring sin cambio de comportamiento |
| `test:` | Agregar o mejorar tests |
| `ci:` | Cambios en pipeline o scripts de CI |
| `docs:` | Solo documentación |
| `chore:` | Configuración, dependencias, tareas de mantenimiento |

---

## 11. Estructura de paquetes

```
src/
├── main/
│   ├── java/com/empresa/integration/
│   │   ├── EnterpriseIntegrationApplication.java   ← punto de entrada Spring Boot
│   │   │
│   │   ├── domain/                                 ← núcleo puro (zero Spring, zero JPA)
│   │   │   ├── model/
│   │   │   │   ├── Transaction.java                ← entidad de dominio (POJO)
│   │   │   │   ├── Client.java                     ← entidad de dominio (POJO)
│   │   │   │   └── TransactionStatus.java          ← enum con transiciones válidas
│   │   │   └── exception/
│   │   │       ├── NotFoundException.java           ← recurso no existe
│   │   │       ├── BusinessRuleException.java       ← regla de negocio violada
│   │   │       └── InvalidStatusTransitionException.java
│   │   │
│   │   ├── application/                            ← orquestación, sin detalles técnicos
│   │   │   ├── port/
│   │   │   │   ├── in/                             ← driving ports (lo que el exterior llama)
│   │   │   │   │   ├── TransactionUseCase.java
│   │   │   │   │   └── ClientQueryUseCase.java
│   │   │   │   └── out/                            ← driven ports (lo que la app necesita del exterior)
│   │   │   │       ├── TransactionRepository.java
│   │   │   │       ├── ClientRepository.java
│   │   │   │       └── LegacySystemPort.java
│   │   │   └── service/                            ← implementaciones de use cases
│   │   │       ├── TransactionService.java         ← @Service, @Transactional
│   │   │       └── ClientService.java              ← @Service
│   │   │
│   │   └── infrastructure/                         ← detalles técnicos (Spring, JPA, HTTP)
│   │       ├── adapter/
│   │       │   ├── in/web/                         ← driving adapters (REST)
│   │       │   │   ├── TransactionController.java  ← @RestController
│   │       │   │   ├── ClientController.java
│   │       │   │   ├── AuthController.java
│   │       │   │   ├── GlobalExceptionHandler.java ← @RestControllerAdvice, Problem Details
│   │       │   │   ├── dto/                        ← Java Records (request/response)
│   │       │   │   │   ├── CreateTransactionRequest.java
│   │       │   │   │   ├── UpdateStatusRequest.java
│   │       │   │   │   ├── TransactionResponse.java
│   │       │   │   │   ├── ClientResponse.java
│   │       │   │   │   ├── LoginRequest.java
│   │       │   │   │   └── TokenResponse.java
│   │       │   │   └── mapper/                     ← MapStruct (compilación)
│   │       │   │       ├── TransactionMapper.java
│   │       │   │       └── ClientMapper.java
│   │       │   └── out/
│   │       │       ├── persistence/                ← driven adapters (JPA)
│   │       │       │   ├── TransactionJpaAdapter.java   ← implements TransactionRepository
│   │       │       │   ├── ClientJpaAdapter.java        ← implements ClientRepository
│   │       │       │   ├── entity/
│   │       │       │   │   ├── TransactionEntity.java   ← @Entity JPA
│   │       │       │   │   ├── ClientEntity.java
│   │       │       │   │   └── UserEntity.java
│   │       │       │   └── repository/
│   │       │       │       ├── TransactionJpaRepository.java  ← extends JpaRepository
│   │       │       │       └── ClientJpaRepository.java
│   │       │       └── legacy/                     ← driven adapter (sistema heredado)
│   │       │           ├── LegacySystemStubAdapter.java  ← implements LegacySystemPort
│   │       │           └── LegacyDataLoader.java         ← carga legacy-data.json
│   │       ├── config/
│   │       │   ├── SecurityConfig.java             ← Spring Security + rutas públicas
│   │       │   ├── OpenApiConfig.java              ← SecurityScheme Bearer en Swagger
│   │       │   └── AppConfig.java                  ← beans auxiliares (ObjectMapper, etc.)
│   │       └── security/
│   │           ├── JwtTokenProvider.java           ← genera y valida tokens
│   │           ├── JwtAuthenticationFilter.java    ← OncePerRequestFilter
│   │           └── JwtUserDetailsService.java      ← UserDetailsService para Spring Security
│   │
│   └── resources/
│       ├── application.yml                         ← configuración base
│       ├── application-dev.yml                     ← show-sql, logs debug
│       ├── application-test.yml                    ← configuración para tests
│       └── db/migration/
│           ├── V1__create_clients_table.sql
│           ├── V2__create_transactions_table.sql
│           └── V3__insert_test_users.sql
│
└── test/
    └── java/com/empresa/integration/
        ├── architecture/
        │   └── HexagonalArchitectureTest.java      ← ArchUnit rules
        ├── domain/
        │   └── model/
        │       └── TransactionTest.java            ← transiciones de estado
        ├── application/service/
        │   ├── TransactionServiceTest.java         ← Mockito
        │   └── ClientServiceTest.java
        ├── infrastructure/adapter/
        │   ├── in/web/
        │   │   ├── TransactionControllerTest.java  ← @WebMvcTest
        │   │   ├── ClientControllerTest.java
        │   │   └── AuthControllerTest.java
        │   └── out/
        │       ├── persistence/
        │       │   └── TransactionJpaAdapterIT.java ← Testcontainers
        │       └── legacy/
        │           └── LegacySystemStubAdapterTest.java
        └── security/
            └── JwtTokenProviderTest.java
```

---

## 12. Decisiones de diseño (ADRs)

### ADR-001: Hexagonal sobre Layered Architecture

**Contexto:** Microservicio con múltiples fuentes externas (PostgreSQL, sistema legacy) y requisito de alta testeabilidad.

**Decisión:** Arquitectura Hexagonal (Ports & Adapters).

**Razón:** La arquitectura hexagonal hace explícitas las dependencias mediante interfaces (puertos). Permite testear el dominio y la lógica de aplicación sin Spring ni base de datos. Facilita reemplazar el sistema legacy por una implementación real sin tocar la lógica de negocio. La inversión de dependencias es estructural, no de convención.

**Consecuencias:** Más archivos que una arquitectura en capas simple. La complejidad adicional se justifica cuando hay múltiples sistemas externos, como es el caso aquí.

---

### ADR-002: MapStruct sobre ModelMapper

**Contexto:** Necesitamos mapear entre entidades de dominio y DTOs.

**Decisión:** MapStruct 1.6.3.

**Razón:** MapStruct genera código de mapeo en tiempo de compilación. Los errores de mapeo se detectan en el build, no en runtime. Zero reflection implica mejor rendimiento y compatibilidad con GraalVM nativo. ModelMapper usa reflection y puede fallar en runtime con errores difíciles de debuggear.

**Consecuencias:** Los mappers se deben declarar como interfaces. Maven necesita el annotation processor configurado.

---

### ADR-003: Flyway sobre JPA ddl-auto

**Contexto:** Base de datos PostgreSQL que evoluciona con el tiempo.

**Decisión:** Flyway con scripts SQL versionados. `ddl-auto=validate` en aplicación.

**Razón:** `ddl-auto=create` o `update` en producción es peligroso: puede borrar datos o crear columnas incorrectas. Flyway proporciona scripts auditables, rollback planificado, y la misma migración se puede ejecutar en local, CI, staging y producción con el mismo resultado. El historial de cambios de esquema es trazable en git.

**Consecuencias:** Cada cambio de esquema requiere un nuevo script Vn. No se puede modificar un script ya aplicado.

---

### ADR-004: Java Records para DTOs en lugar de Lombok

**Contexto:** DTOs inmutables para request/response de la API.

**Decisión:** Java 21 Records.

**Razón:** Los Records son parte del lenguaje — no requieren dependencia externa, son compatibles con GraalVM nativo, y el código generado (equals, hashCode, toString, accessors) es transparente. Lombok requiere plugin de IDE, puede tener problemas con nuevas versiones de Java, y "oculta" código que existe pero no se ve.

**Consecuencias:** Los Records no pueden extender clases (solo implementar interfaces). Para campos con validaciones complejas, se usan constructores compactos.

---

### ADR-005: Stub en memoria para el sistema legacy

**Contexto:** El sistema legacy puede ser un sistema SOAP, base de datos heredada, o servicio REST propietario. No está disponible en entornos de desarrollo ni CI.

**Decisión:** `LegacySystemStubAdapter` que lee de `legacy-data.json` en classpath.

**Razón:** Permite desarrollar y testear sin dependencia del sistema legacy. La interfaz `LegacySystemPort` garantiza que el contrato sea el mismo en stub y en producción. Cuando el equipo tenga acceso al sistema real, solo se implementa un nuevo adapter sin tocar nada más.

**Consecuencias:** Los datos del stub son estáticos. Los tests del stub no verifican el comportamiento real del sistema legacy.

---

## 13. Guía de desarrollo local

### Prerequisitos

- Java 21 instalado (`java -version`)
- Maven 3.9+ o usar `./mvnw`
- Docker Desktop corriendo
- Git configurado

### Setup inicial

```bash
git clone https://github.com/IngKevin95/Enterprise_Integration_Service.git
cd Enterprise_Integration_Service

# Levantar solo PostgreSQL para desarrollo local
docker compose up postgres -d

# Verificar que PostgreSQL está listo
docker compose ps

# Correr la aplicación (perfil dev: logs verbose, SQL visible)
make run
# equivale a: mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### URLs disponibles en local

```
Swagger UI:  http://localhost:8080/swagger-ui.html
API docs:    http://localhost:8080/api-docs
Health:      http://localhost:8080/actuator/health
Prometheus:  http://localhost:8080/actuator/prometheus
```

### Flujo de trabajo diario

```bash
# Tests unitarios (rápido, sin Docker)
make test

# Verificación completa (Checkstyle + JaCoCo + Testcontainers)
make verify

# Levantar stack completo (app + PostgreSQL en Docker)
make docker-up

# Detener todo
make docker-down

# Limpiar build
make clean
```

### Obtener token y llamar a la API

```bash
# Login (usuario demo cargado por Flyway V3)
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' \
  | jq -r '.token')

# Crear una transacción
curl -X POST http://localhost:8080/api/v1/transactions \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "00000000-0000-0000-0000-000000000001",
    "amount": 150.00,
    "description": "Pago de factura #123"
  }'

# Listar transacciones
curl "http://localhost:8080/api/v1/transactions?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 14. Cómo extender el servicio

### Agregar un nuevo endpoint

1. **Domain**: si hay nueva lógica de negocio, agregar al modelo en `domain/model/`
2. **Port in**: agregar método a la interfaz en `application/port/in/`
3. **Service**: implementar el método en `application/service/`
4. **DTO**: crear record en `infrastructure/adapter/in/web/dto/`
5. **Mapper**: agregar método MapStruct en `infrastructure/adapter/in/web/mapper/`
6. **Controller**: agregar endpoint en `infrastructure/adapter/in/web/`
7. **Tests**: `@WebMvcTest` para el controller, unit test para el service

### Agregar soporte para un nuevo sistema legacy

1. Crear `MiSistemaAdapter implements LegacySystemPort` en `infrastructure/adapter/out/legacy/`
2. Usar `@Profile("prod")` o `@Primary` según la estrategia de activación
3. Agregar tests de integración específicos para el adapter
4. No tocar `ClientService`, `LegacySystemPort`, ni tests de aplicación existentes

### Agregar un nuevo rol

1. Agregar el rol al enum o constante en `infrastructure/security/`
2. Configurar en `SecurityConfig.java` qué paths requieren el nuevo rol
3. Cargar el rol en `JwtTokenProvider` al generar el token
4. Actualizar `V3__insert_test_users.sql` con un usuario de prueba para el nuevo rol
5. Agregar tests en `AuthControllerTest` para verificar acceso denegado/permitido

### Agregar métricas personalizadas

Spring Boot Actuator + Micrometer están configurados. Para agregar una métrica:

```java
private final MeterRegistry meterRegistry;

// En el constructor
Counter.builder("transactions.created")
    .tag("status", "success")
    .register(meterRegistry)
    .increment();
```

La métrica aparece automáticamente en `/actuator/prometheus`.
