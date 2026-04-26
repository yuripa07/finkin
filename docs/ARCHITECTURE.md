# Arquitetura do Finkin

## Padrão: Hexagonal (Ports & Adapters)

A arquitetura hexagonal (também chamada de "Ports & Adapters") foi escolhida porque:

1. **Testabilidade**: o domínio de negócio pode ser testado sem nenhuma dependência de banco, Redis ou HTTP.
2. **Substituibilidade**: o banco de dados poderia ser trocado de Postgres para MongoDB sem tocar no domínio.
3. **Alinhamento com Open Finance**: o OCF (Open Finance) exige que as instituições separem dados de processamento — hexagonal facilita esse isolamento.
4. **PDI**: é a arquitetura dominante em bancos modernos brasileiros (Nubank, Inter, C6 Bank usam variações dela).

## Diagrama de dependências

```
┌─────────────────────────────────────────────────────────────┐
│                      Infrastructure                         │
│                                                             │
│  ┌──────────────┐              ┌─────────────────────────┐  │
│  │  Web (REST)  │              │  Persistence (JPA)      │  │
│  │  Controllers │              │  Redis, Mock SPI        │  │
│  └──────┬───────┘              └────────────┬────────────┘  │
│         │ (Driving Port)         (Driven Port) │            │
└─────────┼──────────────────────────────────────┼────────────┘
          │                                      │
          ▼                                      ▼
┌─────────────────────────────────────────────────────────────┐
│                       Application                           │
│                                                             │
│         Services (orquestração dos use cases)               │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         Domain                              │
│                                                             │
│  Model (entidades, VOs)    Ports     Exceptions             │
│                                                             │
│  Regras de negócio puras — ZERO dependências externas       │
└─────────────────────────────────────────────────────────────┘
```

## Design Patterns Aplicados

### Factory — `AccountFactory`
**Localização**: `application/service/account/AccountFactory.java`
**Por que**: A criação de uma `Account` envolve múltiplas validações (KYC aprovado, geração de número com DV) e regras de negócio. Centralizar no Factory evita que o domínio "saiba" como se criar, e garante invariantes desde a criação.

### Builder — Lombok `@Builder`
**Localização**: `Customer`, `Account`, `Transaction` no domain.
**Por que**: Entidades com muitos campos opcionais ficam ilegíveis com construtores posicionais. `@Builder` gera código fluente legível, especialmente nos testes.

### Strategy — `LimitPolicy`
**Localização**: `domain/model/account/LimitPolicy.java`, `DaytimeLimit.java`, `NighttimeLimit.java`
**Por que**: O limite de transferência muda com base no horário (diurno vs noturno — Resolução BCB nº 1/2020). Strategy permite adicionar novas políticas (ex: limite diferenciado para PJ) sem alterar o código existente.

### Chain of Responsibility — `TransactionValidator`
**Localização**: `application/service/transfer/validator/`
**Por que**: A validação de uma transação envolve múltiplas checagens independentes (KYC → status da conta → saldo → limite diário). Chain of Responsibility permite compor validadores sem `if/else` em cascata, e adicionar/remover validações sem alterar outros.

### Observer/Event — `DomainEvents`
**Localização**: `infrastructure/adapter/out/messaging/DomainEventPublisher.java`
**Por que**: Após concluir uma transação, outros contextos precisam reagir (ex: notificação futura, auditoria). Publicar um evento desacopla o `TransactionService` dos consumidores — `TransactionService` não "sabe" que existe um `NotificationService`.

### Facade — `BankingFacade`
**Localização**: `application/service/BankingFacade.java`
**Por que**: Os controllers precisam coordenar múltiplos services (ex: verificar KYC + abrir conta). O Facade simplifica a interface para os controllers sem que eles precisem conhecer a composição interna dos services.

### Adapter — `SpiClientMockAdapter`
**Localização**: `infrastructure/adapter/out/pix/SpiClientMockAdapter.java`
**Por que**: O domínio fala com uma interface `ExternalPixGateway` (port out). Em fase 1, o adapter mocka o SPI. Em fase 2, um novo adapter implementa a chamada HTTP real — o domínio não muda.

## Fluxo de uma transferência interna

```
POST /transfers
      │
      ▼
TransferController (valida DTO, lê Idempotency-Key do header)
      │
      ▼
ExecuteInternalTransferUseCase (port in)
      │
      ▼
ExecuteInternalTransferService
  ├── IdempotencyStore.check(key)  → se já existe, retorna resultado anterior
  ├── Chain of Responsibility:
  │     KycValidator → AccountStatusValidator → BalanceValidator → LimitValidator
  ├── account.debit(amount)        → regra de negócio no domínio
  ├── account.credit(amount)       → regra de negócio no domínio
  ├── TransactionRepository.save() → persiste via JPA
  ├── IdempotencyStore.store(key)  → registra resultado no Redis (TTL 24h)
  └── DomainEventPublisher.publish(TransactionCompletedEvent)
      │
      ▼
TransactionController retorna TransactionResponse (201 Created)
```

## Decisões de banco de dados

### PostgreSQL como banco principal
Escolhido sobre MySQL por: suporte nativo a UUID, CITEXT (CPF case-insensitive), tipos de data com timezone, e uso amplo no ecossistema bancário brasileiro.

### Redis para idempotência e rate limit
A idempotência de transações exige armazenamento rápido com TTL. Redis com TTL nativo é ideal — uma query ao banco seria mais lenta e complexa. O rate limit também usa Redis para sobreviver a restarts e escalar horizontalmente.

### Flyway para migrations
Evolução controlada do schema com versionamento explícito. Essencial para ambiente bancário onde cada mudança de schema precisa ser rastreável e reversível.

### Soft delete
`customers` e `accounts` usam `deleted_at` em vez de DELETE físico porque:
1. Auditoria: regulações bancárias exigem histórico de dados.
2. Referências: transações referenciam contas; deletar fisicamente quebraria integridade referencial.

## Segurança

### JWT stateless
Sem sessão no servidor. Cada request carrega o JWT com as claims do customer (id, KYC status, IDs de contas). Em fase 2, adicionar refresh token e revogação via lista negra no Redis.

### Rate limiting com Bucket4j
Token bucket por IP (endpoints públicos) e por customerId (endpoints de transação). Bucket persistido no Redis — não perde estado com restart da aplicação.

### Mascaramento de CPF em logs
`MaskingConverter` (Logback) substitui CPFs em todas as mensagens de log. Exigência da LGPD e boas práticas do BCB para segurança da informação.
