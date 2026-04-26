# Finkin

> **Fin** (finance) + **kin** (金 — ouro em japonês)

API REST que simula um banco digital brasileiro, com regras de negócio reais baseadas nas normas do Banco Central do Brasil. Projeto de PDI para aprendizado de Java moderno, Spring Boot 4 e Open Finance.

Todos os dados e transações são fictícios — sem dinheiro real.

## O que o app faz

- Cadastro e autenticação de clientes via JWT
- Abertura de contas corrente e poupança
- Transferências internas com idempotência (Idempotency-Key)
- Pix por chave (CPF, e-mail, telefone, aleatória)
- Limites operacionais por horário, conforme Resolução BCB nº 1/2020
- Extrato paginado e comprovante de transação
- Soft delete em clientes e contas
- Auditoria automática de criação/atualização via Spring Data

## Stack

| Componente | Versão |
|---|---|
| Java | 25 (LTS) |
| Spring Boot | 4.0.6 |
| PostgreSQL | 17 |
| Redis | 7 |
| Flyway | 11+ |
| MapStruct | 1.6.3 |
| Lombok | gerenciado pelo Spring Boot BOM |

## Pré-requisitos

```bash
# Java 25 via SDKMAN
sdk install java 25-open

# Docker (Postgres + Redis)
docker --version  # 24+ recomendado
```

## Como rodar

```bash
# 1. Subir dependências
docker compose up -d

# 2. Rodar (profile dev — migrations automáticas + seed)
./mvnw spring-boot:run

# API:        http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Health:     http://localhost:8080/actuator/health
```

## Testes

```bash
# Unitários (sem Docker)
./mvnw test

# Build completo com integração (requer Docker)
./mvnw verify
```

## Endpoints principais

### Autenticação

```bash
# Registrar
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "529.982.247-25",
    "fullName": "João da Silva",
    "birthDate": "1990-01-15",
    "email": "joao@email.com",
    "phone": "+5511999990001",
    "password": "Senha123"
  }'

# Login → retorna JWT
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "joao@email.com", "password": "Senha123"}'
```

### Conta

```bash
TOKEN="<jwt do login>"

# Abrir conta
curl -X POST http://localhost:8080/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type": "CORRENTE"}'

# Saldo
curl http://localhost:8080/accounts/{id}/balance \
  -H "Authorization: Bearer $TOKEN"

# Extrato
curl "http://localhost:8080/accounts/{id}/statement?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Registrar chave Pix
curl -X POST http://localhost:8080/accounts/{id}/pix-keys \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"keyType": "EMAIL", "keyValue": "joao@email.com"}'
```

### Transferência

```bash
IDEM=$(uuidgen)

curl -X POST http://localhost:8080/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $IDEM" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": "<uuid>",
    "targetAccountId": "<uuid>",
    "amount": 100.00,
    "description": "Pagamento"
  }'
```

### Pix

```bash
curl -X POST http://localhost:8080/pix/send \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": "<uuid>",
    "targetKeyType": "EMAIL",
    "targetKeyValue": "alice@finkin.dev",
    "amount": 50.00
  }'

# Comprovante
curl http://localhost:8080/pix/transactions/{id}/receipt \
  -H "Authorization: Bearer $TOKEN"
```

## Usuários de seed (dev)

| Cliente | CPF | Email | Senha | Saldo |
|---|---|---|---|---|
| Alice Financeira | 529.982.247-25 | alice@finkin.dev | Alice123 | R$ 10.000 |
| Bob Desenvolvedor | 111.444.777-35 | bob@finkin.dev | Bob12345 | R$ 5.000 |

## Estrutura de pacotes

```
com.finkin
├── domain/          ← Regras de negócio puras (sem Spring, sem JPA)
│   ├── model/       ← Entidades + Value Objects (Cpf, Money, AccountNumber...)
│   ├── port/in/     ← Use case interfaces
│   ├── port/out/    ← Repository + interfaces externas
│   └── exception/   ← DomainException e subclasses
├── application/     ← Orquestra casos de uso
│   └── service/     ← RegisterCustomerService, OpenAccountService, SendPixService...
├── infrastructure/  ← Adapters
│   ├── adapter/in/web/   ← Controllers REST + DTOs + filtros HTTP
│   ├── adapter/out/      ← JPA, Redis, mock SPI
│   └── config/           ← SecurityConfig, OpenApiConfig, RedisConfig...
└── shared/          ← BankConstants (ISPB, horários BCB)
```

## Decisões arquiteturais

- [docs/DECISIONS.md](docs/DECISIONS.md) — ADRs
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — visão geral da arquitetura
- [docs/CHANGELOG.md](docs/CHANGELOG.md) — histórico de mudanças
