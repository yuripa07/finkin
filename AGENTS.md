# Finkin Bank — Guia para Agentes IA

> Este arquivo é o ponto de entrada para qualquer agente IA (Claude Code, OpenAI Codex,
> Google Gemini CLI, etc.) que trabalhe neste repositório. Leia-o completamente antes
> de propor qualquer mudança.

## O que é este projeto

**Finkin Bank** é um banco digital simulado para PDI (Plano de Desenvolvimento Individual)
focado em Open Finance Brasil. Todos os dados e transações são fictícios. As regras de
negócio seguem as normas do Banco Central do Brasil (BCB) como se fosse um banco real.

O nome: "Fin" (finance) + "kin" (金, ouro/dinheiro em japonês).

## Stack

| Componente | Versão | Notas |
|---|---|---|
| Java | 25 (LTS) | Instalar via SDKMAN: `sdk install java 25-open` |
| Spring Boot | 4.0.6 | Spring Framework 7, Jakarta EE 11, Hibernate ORM 7 |
| Maven | 3.9.9 | Use `./mvnw` (wrapper incluído) |
| PostgreSQL | 17 | Subir via `docker compose up -d` |
| Redis | 7 | Subir via `docker compose up -d` |
| Flyway | 11+ | Migrations em `src/main/resources/db/migration/` |
| MapStruct | 1.6.3 | Geração de mappers em compile-time |
| Lombok | gerenciado pelo SB BOM | Deve vir antes do MapStruct no maven-compiler-plugin |

## Como rodar

```bash
# 1. Subir dependências (Postgres + Redis)
docker compose up -d

# 2. Rodar a aplicação (profile dev por padrão)
./mvnw spring-boot:run

# 3. Rodar testes unitários
./mvnw test

# 4. Rodar testes de integração (requer Docker)
./mvnw verify

# 5. Build do jar executável
./mvnw package -DskipTests
```

A aplicação sobe em `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Arquitetura

Hexagonal (Ports & Adapters). Veja `docs/ARCHITECTURE.md` para detalhe completo.

```
com.finkin
├── domain/          # Regras de negócio puras — sem Spring, sem JPA
│   ├── model/       # Entidades e Value Objects
│   ├── port/in/     # Use case interfaces (driving ports)
│   ├── port/out/    # Repository/external interfaces (driven ports)
│   └── exception/   # Exceções de domínio
├── application/     # Orquestra casos de uso — depende apenas do domain
│   └── service/
├── infrastructure/  # Adapters que conectam o domínio ao mundo externo
│   ├── adapter/in/web/    # Controllers REST + DTOs + filtros HTTP
│   ├── adapter/out/       # JPA, Redis, mock Pix/SPI
│   └── config/            # Beans Spring (@Configuration)
├── shared/          # Utilitários sem lógica de negócio
└── stubs/           # Pacotes vazios para fase 2 (cartão, boleto, etc.)
```

## Regras de dependência entre camadas (CRÍTICO)

```
domain ← application ← infrastructure
```

- `domain` não importa nada do Spring, JPA, Lombok ou qualquer framework.
- `application` importa apenas `domain.*`.
- `infrastructure` importa `domain.*` e `application.*`.
- Nunca importar `infrastructure.*` no `domain` ou `application`.

Se um agente sugerir ou gerar código que viole esta regra, **rejeite** a mudança.

## Convenções de código

- **Lombok**: usado em todo o código. `@Data` em DTOs, `@Value` em VOs, `@Builder` em entidades, `@RequiredArgsConstructor` em services/controllers.
- **MapStruct**: mappers em `infrastructure.adapter.out.persistence.<bounded>/` com sufixo `Mapper` (ex: `CustomerMapper`).
- **Nenhuma anotação JPA no domain**: entidades de domínio são POJOs. JPA entities ficam em `infrastructure.adapter.out.persistence.<bounded>/` com sufixo `JpaEntity`.
- **Comentários**: explicar o PORQUÊ, não o QUÊ. Incluir referência a norma BCB quando aplicável.
- **Erros**: sempre RFC 7807 (`ProblemDetail` nativo do Spring 6+).
- **Paginação**: usar `Pageable` do Spring Data em todo endpoint de listagem.
- **Soft delete**: `@SQLRestriction("deleted_at IS NULL")` + `@SQLDelete` (Hibernate 7). Não usar `@Where` (removido no Hibernate 7).

## Variáveis de ambiente

| Variável | Padrão (dev) | Descrição |
|---|---|---|
| `DB_HOST` | localhost | Host do PostgreSQL |
| `DB_PORT` | 5432 | Porta do PostgreSQL |
| `DB_USER` | finkin | Usuário do banco |
| `DB_PASS` | finkin | Senha do banco |
| `REDIS_HOST` | localhost | Host do Redis |
| `REDIS_PORT` | 6379 | Porta do Redis |
| `FINKIN_JWT_SECRET` | (veja application.yml) | Segredo HMAC-SHA256 do JWT — **obrigatório em prod** |

## Endpoints principais (fase 1)

| Método | Path | Auth | Descrição |
|---|---|---|---|
| POST | /auth/register | público | Cadastrar novo customer |
| POST | /auth/login | público | Login, retorna JWT |
| POST | /accounts | JWT | Abrir conta corrente |
| GET | /accounts/{id}/balance | JWT | Consultar saldo |
| GET | /accounts/{id}/statement | JWT | Extrato paginado |
| POST | /transfers | JWT + Idempotency-Key | Transferência interna |
| POST | /pix/send | JWT + Idempotency-Key | Envio Pix por chave |
| POST | /pix/keys | JWT | Registrar chave Pix |
| GET | /transactions/{id}/receipt | JWT | Comprovante da transação |

## Migrations Flyway

Sempre que criar ou alterar tabelas, criar uma nova migration em `src/main/resources/db/migration/`.
Nunca editar uma migration existente que já foi aplicada.

| Arquivo | Tabela |
|---|---|
| V1__create_customer.sql | customers |
| V2__create_auth_credentials.sql | auth_credentials |
| V3__create_account.sql | accounts |
| V4__create_transaction.sql | transactions |
| V5__create_pix_key.sql | pix_keys |
| V6__seed_dev.sql | (dados de dev) |

## Normas BCB referenciadas

| Norma | Implementação |
|---|---|
| Resolução BCB nº 1/2020 | `LimitsProperties`, `LimitPolicy`, horário noturno 20h–6h |
| Resolução BCB nº 6/2020 | ISPB `99999999` (fictício) em `BankConstants` |
| Manual de Tempos Pix (ICOM-BCB) | Formato `EndToEndId` — 32 chars, prefixo E+ISPB |
| LGPD (Lei 13.709/2018) | `MaskingConverter` — mascaramento de CPF em logs |

## Arquivos de documentação

| Arquivo | Conteúdo |
|---|---|
| `AGENTS.md` (este) | Guia de entrada para agentes IA |
| `CLAUDE.md` | Instruções específicas para Claude Code |
| `docs/ARCHITECTURE.md` | Arquitetura detalhada, patterns, fluxos |
| `docs/DECISIONS.md` | ADRs — registro de decisões arquiteturais |
| `docs/CHANGELOG.md` | Histórico de evolução por onda de implementação |
| `docs/BACKLOG.md` | Funcionalidades planejadas para fase 2+ |

## Onde buscar contexto adicional

- Decisões passadas: `docs/DECISIONS.md`
- Histórico de mudanças: `docs/CHANGELOG.md`
- Fase 2 e backlog: `docs/BACKLOG.md`
- Exemplos de curl/Postman: `docs/examples/` (criado na onda 6)
