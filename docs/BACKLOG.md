# Backlog — Funcionalidades Planejadas

Funcionalidades estruturadas como stubs na fase 1 (pacote `com.finkin.stubs`).
Cada item tem uma referência à norma BCB ou regulação pertinente quando aplicável.

---

## Fase 2

### Cartão de Crédito/Débito
- Emissão de cartão virtual e físico
- Autorização de transações (simulação do fluxo ISO 8583)
- Fatura mensal e parcelamento
- Regulação: Resolução BCB nº 96/2021 (arranjos de pagamento)

### Boleto Bancário
- Emissão de boleto (CNAB 240/400 simplificado)
- Registro na CIP (simulado)
- Pagamento de boleto (débito na conta)
- Regulação: Circular BCB nº 3.599/2012 e sucessores

### Pix Externo Real (SPI)
- Substituir `SpiClientMockAdapter` por `SpiHttpClientAdapter`
- Integração com DICT (Diretório de Identificadores de Contas Transacionais) via mTLS
- Webhook para recebimento de Pix externo
- Regulação: Resolução BCB nº 1/2020 e Manual de Integração do Pix

### Devolução Pix (D+90)
- Solicitação de devolução em até 90 dias
- Tipo de transação `PIX_DEVOLUCAO`
- Rastreabilidade pelo endToEndId original
- Regulação: Manual de Integração do Pix, seção Devolução

---

## Fase 3

### Open Finance / Open Banking
- Consentimento (consent management) — LGPD + regulação OFB
- Compartilhamento de dados (dados cadastrais, extrato, saldo)
- Iniciação de pagamento via terceiros (TPP)
- Regulação: Resolução BCB nº 32/2020 (Open Finance)

### Investimentos
- Conta de custódia
- CDB, LCI/LCA simulados com rentabilidade diária
- Liquidez e resgate
- Regulação: Instrução CVM 617 (simplificado)

### Notificações
- Push notification (FCM/APNs simulado)
- E-mail transacional (Postmark/SendGrid simulado)
- Webhook de eventos para parceiros

### Autenticação Avançada
- OAuth2 / OIDC com Authorization Server (Spring Authorization Server)
- Multi-factor authentication (TOTP — Google Authenticator compatible)
- Refresh token com rotação
- Revogação via Redis (lista negra de JTIs)

---

## Melhorias Transversais (qualquer fase)

- Pix externo: certificado ICP-Brasil simulado para mTLS
- Rate limiting distribuído com Redis Cluster
- Observabilidade: traces com OpenTelemetry + Jaeger
- Health checks customizados (Redis, Postgres, SPI mock)
- Circuit breaker (Resilience4j) para chamadas externas
- Testes de carga com Gatling
- Deploy: Kubernetes + Helm chart
- CI/CD: GitHub Actions com quality gates (Sonar, OWASP Dependency Check)
