# Changelog — Histórico de Evolução do Finkin

Formato de ondas de implementação. Cada onda é um marco funcional testável.

---

## Onda 1 — Bootstrap | 2026-04-25 | ✅ Concluída

**Objetivo**: Estrutura base do projeto. Compilável, mas sem funcionalidade bancária ainda.

### Criado
- `pom.xml` — Spring Boot 4.0.6, Java 25, todas as dependências da fase 1
- `compose.yaml` — Postgres 17 + Redis 7 + pgAdmin (profile tools)
- `FinkinApplication.java` — entry point com `@EnableJpaAuditing`, `@ConfigurationPropertiesScan`, `@EnableAsync`
- `application.yml` / `application-dev.yml` / `application-test.yml`
- `logback-spring.xml` — logs estruturados com correlationId e mascaramento de CPF
- `BankConstants.java` — ISPB, agência, horários noturnos, formato E2E ID
- `*Properties.java` — `BankProperties`, `LimitsProperties`, `JwtProperties`, `PixSpiProperties`, `RateLimitProperties`, `KycProperties`
- `SecurityConfig.java` — Spring Security 7, JWT stateless, headers de segurança
- `JwtService.java` / `JwtAuthenticationFilter.java` — geração e validação de JWT
- `OpenApiConfig.java` — springdoc-openapi com Bearer auth
- `JacksonConfig.java` — ObjectMapper com JavaTimeModule
- `RedisConfig.java` — `RedisTemplate<String, Object>` com serialização JSON
- `AuditConfig.java` — `AuditorAware<String>` lê do SecurityContext
- `CorrelationIdFilter.java` — injeta X-Correlation-Id no MDC
- `MaskingConverter.java` — mascara CPF em logs (Logback custom converter)
- `GlobalExceptionHandler.java` — RFC 7807 `ProblemDetail` para todas as exceções
- Documentação: `AGENTS.md`, `CLAUDE.md`, `docs/ARCHITECTURE.md`, `docs/DECISIONS.md`, `docs/CHANGELOG.md`, `docs/BACKLOG.md`

### Próximo
Onda 2: Domínio puro — Value Objects, entidades, ports, exceções, testes unitários.

---

## Onda 2 — Domain Core | 2026-04-25 | ✅ Concluída

### Criado
- **Value Objects**: `Cpf` (algoritmo módulo 11 oficial da Receita), `Email` (RFC 5322 simplificada), `Phone` (E.164 BR com DDDs válidos Anatel), `Money` (BigDecimal escala 2, HALF_EVEN), `AccountNumber` (módulo 10), `EndToEndId` (formato Bacen 32 chars)
- **Entidades de domínio**: `Customer`, `Account` (com `debit`/`credit`/`assertActive`), `Transaction` (máquina de estados)
- **Pix**: `PixKey` com validação por tipo (CPF, EMAIL, PHONE, RANDOM), `PixKeyType`
- **Enums**: `AccountType`, `AccountStatus`, `TransactionType`, `TransactionStatus`, `KycStatus`
- **Strategy**: `LimitPolicy` + `DaytimeLimit` + `NighttimeLimit` + `LimitPolicySelector`
- **Ports in**: RegisterCustomer, OpenAccount, GetBalance, GetStatement, ExecuteInternalTransfer, SendPix, RegisterPixKey, IssueReceipt, RegisterUser, Authenticate
- **Ports out**: CustomerRepository, AccountRepository, TransactionRepository, PixKeyRepository, IdempotencyStore, ExternalPixGateway, AuthCredentialsRepository, DomainEventPublisher
- **Exceções**: DomainException e 9 subclasses específicas
- **Testes unitários (167 testes, 100% verde)**: CpfTest, MoneyTest, AccountNumberTest, EndToEndIdTest, LimitPolicySelectorTest, PixKeyTest

---

## Onda 3 — Customer + Account + Auth | 2026-04-25 | ✅ Concluída

### Criado
- **Application Services**: RegisterCustomerService, AccountNumberGenerator, OpenAccountService, GetBalanceService, GetStatementService, AuthService
- **JPA Entities**: CustomerJpaEntity, AccountJpaEntity, PixKeyJpaEntity, TransactionJpaEntity, AuthCredentialsJpaEntity (com @SQLRestriction no lugar do @Where removido no Hibernate 7)
- **Mappers MapStruct**: CustomerMapper, AccountMapper, PixKeyMapper, TransactionMapper
- **Repository Adapters**: CustomerRepositoryAdapter, AccountRepositoryAdapter, PixKeyRepositoryAdapter, TransactionRepositoryAdapter, AuthCredentialsRepositoryAdapter
- **Controllers**: AuthController, CustomerController, AccountController (com DTOs records)
- **Migrations**: V1 (customers + CITEXT), V2 (auth_credentials), V3 (accounts), V4 (transactions), V5 (pix_keys)

---

## Onda 4 — Transferência Interna + Idempotência | 2026-04-25 | ✅ Concluída

### Criado
- **Chain of Responsibility**: TransactionValidator (interface), KycValidator, AccountStatusValidator, SufficientBalanceValidator, DailyLimitValidator
- **ExecuteInternalTransferService**: orquestra validators + débito/crédito + idempotência Redis + evento de domínio
- **TransactionCompletedEvent**: evento Observer publicado após transação concluída
- **RedisIdempotencyAdapter**: chave `idem:tx:<uuid>`, TTL 24h, `RedisTemplate<String, String>`
- **SpringDomainEventPublisher**: adapts ApplicationEventPublisher
- **TransferController**: endpoint POST /transfers com header Idempotency-Key obrigatório

---

## Onda 5 — Pix Interno + Comprovante | 2026-04-25 | ✅ Concluída

### Criado
- **SendPixService**: resolve chave Pix → conta destino → executa débito/crédito + gera PIX_ENVIO e PIX_RECEBIMENTO
- **IssueReceiptService**: consulta transação por ID para comprovante
- **SpiClientMockAdapter**: implementa ExternalPixGateway com delay configurável e taxa de falha (fase 2 substituirá por HTTP real)
- **RegisterPixKeyService**: cria chave Pix com validação por tipo
- **PixController**: POST /pix/send e GET /pix/transactions/{id}/receipt

---

## Onda 6 — Polimento | 2026-04-25 | ✅ Concluída

### Criado
- **Migration V6**: seed de dev com Alice (R$10k) e Bob (R$5k), chaves Pix CPF+EMAIL/PHONE
- **Stubs fase 2**: package-info.java em card, boleto, investment, openfinance, notification
- **README.md**: completo com exemplos curl, tabela de seed, mapa pattern↔código, mapa BCB↔implementação, pré-requisitos e roadmap

---

## Onda 7 — Testes de Integração | 2026-04-26 | ✅ Concluída

**Objetivo**: Cobertura end-to-end com Testcontainers (Postgres 17 + Redis 7 reais).
Execução: `./mvnw verify -Pfailsafe` (requer Docker).

### Criado
- **`AbstractIntegrationTest.java`**: base com Singleton container pattern (Postgres + Redis),
  `@DynamicPropertySource`, `RestClient` (Spring 7 — `TestRestTemplate` foi removido no Boot 4),
  helpers tipados: `post`, `postWithIdempotency`, `get`, `doRegister`, `doLogin`,
  `doOpenAccount`, `doRegisterPixKey`
- **`CustomerControllerIT.java`** (5 testes):
  - register → 201 com customerId e kycStatus=APPROVED
  - login com credenciais válidas → 200 com token JWT (3 partes)
  - CPF duplicado → 409 Conflict
  - senha errada → 401 Unauthorized
  - CPF inválido → 400/422
- **`TransferControllerIT.java`** (4 testes, `@TestInstance(PER_CLASS)`):
  - transferência happy path → 201 CONCLUIDA, endToEndId 32 chars
  - idempotência → dois POSTs com mesma chave retornam mesmo transactionId (sem duplo débito)
  - saldo insuficiente → 422
  - extrato paginado → lista transações CONCLUIDAS em ordem decrescente
- **`PixControllerIT.java`** (5 testes, `@TestInstance(PER_CLASS)`):
  - Pix para chave interna → 201 CONCLUIDA, liquidação imediata
  - Pix para chave inexistente → 404
  - Pix idempotente → mesmo transactionId nas duas chamadas
  - Comprovante por ID → 200 com tipo, status, amount, currency, endToEndId
  - Comprovante de ID inexistente → 404
- **`src/test/resources/application-it.yml`**: perfil IT com Flyway enabled (target V5),
  `ddl-auto: validate`, kyc auto-approve, JWT secret fixo, pix delay=0
- **`pom.xml`**: perfil `failsafe` com `maven-failsafe-plugin` rodando `**/*IT.java`

### Notas técnicas
- `TestRestTemplate` foi removido no Spring Boot 4.x → substituído por `RestClient` com `@LocalServerPort`
- `spring.flyway.target: "5"` exclui a migration V6 de seed dev dos testes de integração
- JdbcTemplate usado em `@BeforeAll` para creditar saldo (não existe endpoint de depósito — correto para um banco real)
- CPFs de teste calculados manualmente pelo algoritmo da Receita Federal (módulo 11)
