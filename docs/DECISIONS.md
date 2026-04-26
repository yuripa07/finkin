# ADR — Architecture Decision Records

Registro cronológico das decisões arquiteturais do Finkin.
Formato: **ADR-NNN | Data | Status | Título**

---

## ADR-001 | 2026-04-25 | Aceito | Arquitetura Hexagonal

**Contexto**: Projeto de PDI para Open Finance Brasil. Precisamos de uma arquitetura que seja testável, educativa e próxima da realidade dos bancos modernos.

**Decisão**: Adotar Ports & Adapters (Hexagonal Architecture).

**Consequências**:
- (+) Domínio testável sem infraestrutura.
- (+) Fácil substituição de adapters (ex: trocar PostgreSQL por outro banco).
- (+) Separação clara de responsabilidades.
- (-) Mais arquivos/camadas que uma arquitetura em 3 camadas tradicional.
- (-) Curva de aprendizado inicial maior.

---

## ADR-002 | 2026-04-25 | Aceito | Java 25 + Spring Boot 4.0.6

**Contexto**: Usuário quer trabalhar com a stack mais atual possível para PDI.

**Decisão**: Java 25 (LTS setembro 2025) + Spring Boot 4.0.6 (GA).

**Alternativas consideradas**: Java 21 (LTS anterior, já instalado localmente).

**Consequências**:
- (+) Aprende com a versão LTS mais recente.
- (+) Virtual threads habilitadas por padrão (Spring Boot 4 + `spring.threads.virtual.enabled=true`).
- (-) Requer instalação manual de JDK 25 via SDKMAN.

---

## ADR-003 | 2026-04-25 | Aceito | Lombok em todo o código

**Contexto**: Usuário escolheu Lombok em todo o código (incluindo domínio).

**Decisão**: Usar `@Builder`, `@Value`, `@Data`, `@RequiredArgsConstructor`, `@Slf4j` em todos os pacotes.

**Trade-off**: domínio com Lombok é tecnicamente "impuro" (leve contaminação de framework), mas o benefício de legibilidade supera para o objetivo de PDI. Lombok é processado em compile-time (não há dependência em runtime).

---

## ADR-004 | 2026-04-25 | Aceito | JWT com HMAC-SHA256 (HS256)

**Contexto**: API REST stateless, fase 1 com serviço único.

**Decisão**: JWT assinado com HS256 (chave simétrica).

**Alternativas consideradas**: RS256 (assimétrico).

**Por que não RS256 agora**: RS256 é necessário quando múltiplos serviços precisam verificar o token sem a chave privada (ex: gateway, resource servers). Em fase 1 com serviço único, HS256 é suficiente e mais simples. **Em fase 2, migrar para RS256 com JWK Set público.**

---

## ADR-005 | 2026-04-25 | Aceito | ProblemDetail nativo (RFC 7807)

**Contexto**: Erros da API precisam de formato padronizado.

**Decisão**: Usar `ProblemDetail` do Spring Framework 6+ nativo.

**Alternativas consideradas**: `org.zalando:problem-spring-web` (biblioteca externa mais rica).

**Por que nativo**: Spring 6+ implementa RFC 7807 nativamente; não justifica dependência extra em fase 1. Se o projeto crescer para um API Gateway precisar de erros mais ricos (Problem extensions padronizadas por tipo), avaliar Zalando.

---

## ADR-006 | 2026-04-25 | Aceito | Soft Delete com @SQLRestriction (Hibernate 7)

**Contexto**: Entidades `Customer` e `Account` não podem ser deletadas fisicamente (auditoria bancária).

**Decisão**: Coluna `deleted_at TIMESTAMPTZ NULL` + `@SQLRestriction("deleted_at IS NULL")`.

**Por que não @SoftDelete do Hibernate 7**: `@SoftDelete` com estratégia booleana (`deleted = true/false`) é mais simples, mas perde o timestamp de quando ocorreu o soft delete — informação relevante para auditoria.

**Por que não @Where (Hibernate 6)**: `@Where` foi removido no Hibernate 7. Código legado que usar `@Where` não compilará com Hibernate 7.

---

## ADR-007 | 2026-04-25 | Aceito | Idempotência via Redis com TTL 24h

**Contexto**: Transações financeiras exigem idempotência (BCB exige para endpoints Pix).

**Decisão**: Redis com chave `idem:tx:<uuid>` e TTL de 24 horas.

**Consequências**:
- (+) Resposta rápida para requisições duplicadas (sem consulta ao Postgres).
- (+) TTL automático — sem necessidade de job de limpeza.
- (-) Se o Redis cair, a proteção de idempotência fica temporariamente indisponível. Em produção, usar Redis Sentinel ou Cluster.

---

## ADR-008 | 2026-04-25 | Aceito | MapStruct para mapeamento domain ↔ JPA ↔ DTO

**Contexto**: Precisamos converter entre 3 representações do mesmo dado: domain entity, JPA entity, DTO REST.

**Decisão**: MapStruct com geração de código em compile-time.

**Alternativas**: ModelMapper (reflexão em runtime), mapeamento manual.

**Por que MapStruct**: zero overhead em runtime (código gerado), type-safe, erros detectados em compile-time. Alinha com o princípio de "sem magia em runtime".

---

## ADR-009 | 2026-04-25 | Aceito | Documentação em AGENTS.md + docs/ para agentes IA

**Contexto**: Projeto desenvolvido com auxílio de múltiplos agentes IA (Claude Code, OpenAI Codex, Google Gemini CLI).

**Decisão**: Manter `AGENTS.md` na raiz (ponto de entrada agnóstico), `CLAUDE.md` para Claude Code, e `docs/` com ARCHITECTURE, DECISIONS, CHANGELOG e BACKLOG.

**Consequências**: Qualquer agente pode entender o projeto lendo apenas `AGENTS.md`. Histórico de evolução em `CHANGELOG.md` evita que agentes repitam trabalho ou revertam decisões já tomadas.

---

## ADR-010 | 2026-04-25 | Aceito | Mock SPI para Pix externo (fase 1)

**Contexto**: Integrar com o SPI real do BCB exige credenciais e certificados que não fazem sentido em um projeto de PDI.

**Decisão**: `SpiClientMockAdapter` que implementa `ExternalPixGateway` com delay configurável e taxa de falha simulada.

**Fase 2**: Novo adapter `SpiHttpClientAdapter` que faz chamada HTTP real ao SPI. O domínio não muda — apenas o adapter.
