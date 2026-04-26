# Finkin Bank — Backend Fase 1

> Banco digital simulado para PDI em Open Finance Brasil.
> Todos os dados e transações são fictícios — sem dinheiro real.

## O que é

Finkin Bank é uma API REST que simula um banco digital brasileiro, implementando
regras de negócio reais conforme as normas do Banco Central do Brasil (BCB).
Projeto de PDI para aprendizado de Java moderno, Spring Boot 4 e Open Finance.

Nome: **Fin** (finance) + **kin** (金 — ouro/dinheiro em japonês)

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
# 1. Instalar Java 25 via SDKMAN
sdk install java 25-open

# 2. Verificar
java --version  # deve mostrar 25.x

# 3. Docker (Postgres + Redis)
docker --version  # 24+ recomendado
```

## Como rodar

```bash
# 1. Subir dependências
docker compose up -d

# 2. Rodar a aplicação (profile dev — migrations automáticas + seed)
./mvnw spring-boot:run

# A API sobe em: http://localhost:8080
# Swagger UI:    http://localhost:8080/swagger-ui.html
# Health:        http://localhost:8080/actuator/health
```

## Testes

```bash
# Testes unitários (rápidos, sem Docker)
./mvnw test

# Build completo (unit + integração — requer Docker)
./mvnw verify
```

## Endpoints (fase 1)

### Autenticação (público)

```bash
# Registrar novo customer
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

# Login — retorna JWT
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "joao@email.com", "password": "Senha123"}'
```

### Conta (autenticado)

```bash
TOKEN="<jwt do login>"

# Abrir conta corrente
curl -X POST http://localhost:8080/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type": "CORRENTE"}'

# Consultar saldo
curl http://localhost:8080/accounts/{id}/balance \
  -H "Authorization: Bearer $TOKEN"

# Extrato paginado (padrão: 20 itens, mais recente primeiro)
curl "http://localhost:8080/accounts/{id}/statement?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Registrar chave Pix (CPF, EMAIL, PHONE, RANDOM)
curl -X POST http://localhost:8080/accounts/{id}/pix-keys \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"keyType": "EMAIL", "keyValue": "joao@email.com"}'
```

### Transferência (com Idempotency-Key)

```bash
# Gerar uma chave UUID v4 para idempotência
IDEM=$(uuidgen)

# Transferência interna
curl -X POST http://localhost:8080/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $IDEM" \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": "<uuid da conta de origem>",
    "targetAccountId": "<uuid da conta de destino>",
    "amount": 100.00,
    "description": "Pagamento"
  }'

# Reenviar com o mesmo IDEM → retorna a mesma resposta (idempotência)
curl -X POST http://localhost:8080/transfers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: $IDEM" \
  -H "Content-Type: application/json" \
  -d '{...mesmo body...}'
```

### Pix

```bash
# Envio de Pix por chave
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

| Customer | CPF | Email | Senha | Saldo |
|---|---|---|---|---|
| Alice Financeira | 529.982.247-25 | alice@finkin.dev | Alice123 | R$ 10.000 |
| Bob Desenvolvedor | 111.444.777-35 | bob@finkin.dev | Bob12345 | R$ 5.000 |

## Estrutura de pacotes

```
com.finkin
├── domain/          ← Regras de negócio puras (sem Spring, sem JPA)
│   ├── model/       ← Entidades (Customer, Account, Transaction) + VOs (Cpf, Money, AccountNumber...)
│   ├── port/in/     ← Use case interfaces (RegisterCustomer, OpenAccount, SendPix...)
│   ├── port/out/    ← Repository + external interfaces (CustomerRepository, IdempotencyStore...)
│   └── exception/   ← DomainException e subclasses
├── application/     ← Orquestra casos de uso
│   └── service/     ← RegisterCustomerService, OpenAccountService, SendPixService...
├── infrastructure/  ← Adapters que conectam ao mundo externo
│   ├── adapter/in/web/   ← Controllers REST + DTOs + filtros HTTP
│   ├── adapter/out/      ← JPA, Redis, mock SPI
│   └── config/           ← SecurityConfig, OpenApiConfig, RedisConfig...
├── shared/          ← BankConstants (ISPB, horários BCB)
└── stubs/           ← Pacotes vazios para fase 2 (card, boleto, investment...)
```

## Mapa: Pattern → Código

| Pattern | Onde | Por quê |
|---|---|---|
| Factory | `OpenAccountService` | Encapsula geração de número de conta e defaults |
| Builder | `@Builder` em Customer/Account/Transaction | Construção fluente com muitos campos |
| Strategy | `LimitPolicy`, `DaytimeLimit`, `NighttimeLimit` | Limite diário muda por horário (BCB Art. 20) |
| Chain of Responsibility | `TransactionValidator` e subclasses | Validações independentes e compostas antes de debitar |
| Observer/Event | `TransactionCompletedEvent` + `SpringDomainEventPublisher` | Desacopla transferência de notificações futuras |
| Adapter | `SpiClientMockAdapter` → `ExternalPixGateway` | Fase 1: mock; Fase 2: troca por HTTP real sem mudar domínio |
| Facade | `BankingFacade` (futuro) | Simplificar chamadas complexas nos controllers |

## Mapa: Regulação BCB → Implementação

| Norma BCB | Implementação |
|---|---|
| Resolução nº 1/2020, Art. 20 — limites Pix noturno | `LimitsProperties.transferNightBrl`, `NighttimeLimit`, `DailyLimitValidator` |
| Resolução nº 1/2020 — horário noturno 20h–6h | `LimitPolicySelector`, `BankConstants.NIGHT_HOUR_START/END` |
| Resolução nº 6/2020 — ISPB de 8 dígitos | `BankConstants.ISPB = "99999999"` (fictício) |
| Manual de Tempos do Pix — formato endToEndId | `EndToEndId.generate()` — 32 chars, prefixo E+ISPB+datetime |
| LGPD Art. 46 — segurança dos dados pessoais | `MaskingConverter` — CPF mascarado em todos os logs |

## Roadmap

- Fase 2: Pix externo real (SPI), cartão, boleto, Open Finance/consentimento
- Ver: [docs/BACKLOG.md](docs/BACKLOG.md)
- Decisões arquiteturais: [docs/DECISIONS.md](docs/DECISIONS.md)
- Histórico de mudanças: [docs/CHANGELOG.md](docs/CHANGELOG.md)
