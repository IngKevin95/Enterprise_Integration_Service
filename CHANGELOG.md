# Changelog

Todos los cambios relevantes de este proyecto se documentan aquí.
Formato basado en [Keep a Changelog](https://keepachangelog.com/es/1.0.0/).

---

## [1.0.0] - 2026-06-28

### Added

#### Infraestructura base (PR #1)
- Estructura de proyecto Spring Boot 3.4.1 con Java 21 y arquitectura hexagonal
- Pipeline CI con GitHub Actions (`ci.yml`): compile, test, Checkstyle, JaCoCo 80%
- Pipeline CD (`release.yml`): build Docker image y push a GHCR en tag `v*`
- Dockerfile multi-stage (build Maven + runtime JRE 21 Alpine)
- Docker Compose con PostgreSQL 16
- Checkstyle con Google Java Style Guide
- Makefile con targets `build`, `test`, `verify`, `run`, `docker-up`, `docker-down`
- `docs/architecture.md` — arquitectura hexagonal, stack, decisiones técnicas (ADRs)

#### Dominio y seguridad (PR #2)
- Modelos de dominio: `Transaction`, `Client`, `TransactionStatus`
- Excepciones de dominio: `NotFoundException`, `BusinessRuleException`, `InvalidStatusTransitionException`
- Puertos hexagonales: `TransactionUseCase`, `ClientQueryUseCase`, `TransactionRepository`, `ClientRepository`, `LegacySystemPort`
- Autenticación JWT con JJWT 0.12.6: `JwtTokenProvider`, `JwtAuthenticationFilter`, `JwtUserDetailsService`
- `SecurityConfig`: rutas públicas (`/api/v1/auth/**`, Swagger, Actuator) y protegidas con Bearer token
- `AuthController` con `POST /api/v1/auth/login` y `POST /api/v1/auth/refresh`
- `GlobalExceptionHandler` con Problem Details RFC 7807
- `OpenApiConfig` con SecurityScheme Bearer para Swagger UI
- Flyway V3: usuario admin de demo con hash BCrypt

#### Capa de persistencia (PR #3)
- Entidades JPA: `TransactionEntity`, `ClientEntity`, `UserEntity`
- Flyway V1 y V2: tablas `clients` y `transactions` con índices
- Repositorios Spring Data: `TransactionJpaRepository`, `ClientJpaRepository`
- Adaptadores de persistencia: `TransactionJpaAdapter`, `ClientJpaAdapter`
- Mappers MapStruct: `TransactionPersistenceMapper`, `ClientPersistenceMapper`

#### CRUD de transacciones (PR #4)
- `TransactionService` con `@Transactional` — lógica de negocio y validación de transiciones de estado
- `TransactionController`: `GET /transactions`, `GET /transactions/{id}`, `POST /transactions`, `PATCH /transactions/{id}/status`
- DTOs: `CreateTransactionRequest`, `UpdateStatusRequest`, `TransactionResponse`, `PagedResponse<T>`
- `TransactionWebMapper` (MapStruct)
- Tests: `TransactionServiceTest`, `TransactionControllerTest`, `TransactionJpaAdapterTest`, `JwtAuthenticationFilterTest`, `JwtUserDetailsServiceTest`, `ClientJpaAdapterTest`, `GlobalExceptionHandlerTest`
- JaCoCo configurado al 80% con exclusiones de entidades JPA y mappers generados

#### Integración con sistema legacy (PR #5)
- `ClientService` — consulta de clientes con `NotFoundException` si no existe
- `LegacySystemStubAdapter` — implementa `LegacySystemPort` leyendo `legacy-data.json` del classpath (reemplazable por SOAP/JDBC/REST sin cambiar la interfaz)
- `ClientController`: `GET /api/v1/clients/{id}` y `GET /api/v1/clients/{id}/transactions`
- `ClientResponse` DTO
- Tests: `ClientServiceTest`, `LegacySystemStubAdapterTest`, `ClientControllerTest`

### Technical

- Java 21 + Spring Boot 3.4.1 + PostgreSQL 16 + Flyway 10
- JWT stateless (HS256, JJWT 0.12.6)
- MapStruct 1.6.3 para mapeo en tiempo de compilación
- Testcontainers 1.20.4 (disponible en classpath para futuros tests de integración)
- ArchUnit 1.3.0: validación de reglas hexagonales en tests
- Cobertura de instrucciones JaCoCo ≥ 80%

---

[1.0.0]: https://github.com/IngKevin95/Enterprise_Integration_Service/releases/tag/v1.0.0
