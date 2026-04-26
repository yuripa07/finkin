# Finkin Bank — Instruções para Claude Code

> Leia também `AGENTS.md` — este arquivo adiciona apenas instruções específicas do Claude Code.

## Contexto do projeto

Banco digital simulado para PDI em Java / Open Finance Brasil.
Leia `AGENTS.md` para stack, arquitetura, convenções e endpoints.

## Como eu (desenvolvedor) prefiro trabalhar

- **Implementar em ondas**: cada onda é um marco funcional testável antes da próxima.
  Ondas definidas em `docs/CHANGELOG.md`.
- **Documentar o PORQUÊ**: comentários no código devem explicar motivação (norma BCB,
  decisão de design, trade-off), não reescrever o que o código já diz.
- **Lombokizado**: Lombok em todo o código, inclusive domínio.
- **Sem mágica oculta**: se usar um design pattern, comentar qual é e por que foi escolhido.

## Comandos úteis durante desenvolvimento

```bash
# Compilar sem rodar testes
./mvnw compile

# Rodar apenas testes unitários (H2, rápido)
./mvnw test

# Rodar testes de integração (Testcontainers, Docker obrigatório)
./mvnw verify -Pfailsafe

# Ver o log da aplicação (dev profile, console colorido)
./mvnw spring-boot:run

# Gerar o jar para deploy
./mvnw package -DskipTests

# Verificar se as migrations estão corretas (sem rodar a app)
./mvnw flyway:validate
```

## Padrão para novos endpoints

1. Criar DTO de request/response em `infrastructure/adapter/in/web/<bounded>/dto/`
2. Criar ou atualizar o use case interface em `domain/port/in/`
3. Implementar o service em `application/service/<bounded>/`
4. Criar ou atualizar o controller em `infrastructure/adapter/in/web/<bounded>/`
5. Se necessário, criar migration em `src/main/resources/db/migration/V{N}__<desc>.sql`
6. Criar teste unitário em `src/test/java/com/finkin/domain/`
7. Criar teste de integração em `src/test/java/com/finkin/integration/`
8. Atualizar `docs/CHANGELOG.md`

## O que NÃO fazer

- Nunca importar Spring/JPA/Lombok no pacote `com.finkin.domain`
- Nunca editar migrations que já existem (criar nova)
- Nunca logar CPF completo ou número de conta completo
- Nunca usar `@Where` (removido no Hibernate 7) — usar `@SQLRestriction`
- Nunca deixar `@Transactional` no domain ou application sem justificativa
  (transações ficam nos adapters de persistência ou no service de aplicação)
