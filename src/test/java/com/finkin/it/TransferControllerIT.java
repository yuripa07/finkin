package com.finkin.it;

import com.finkin.AbstractIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para transferência interna e extrato paginado.
 *
 * @TestInstance(PER_CLASS): instância única compartilhada entre os métodos,
 * permitindo @BeforeAll não-estático com @Autowired e JdbcTemplate.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TransferControllerIT extends AbstractIntegrationTest {

    // CPFs válidos (algoritmo módulo 11 verificado): 987.654.321-00 e 456.789.012-49
    private static final String ALICE_CPF   = "987.654.321-00";
    private static final String ALICE_EMAIL = "transfer-alice@test.dev";
    private static final String BOB_CPF     = "456.789.012-49";
    private static final String BOB_EMAIL   = "transfer-bob@test.dev";
    private static final String PASS        = "Transfer1";

    @Autowired
    private JdbcTemplate jdbc;

    private String aliceToken;
    private UUID   aliceAccountId;
    private UUID   bobAccountId;

    @BeforeAll
    void setup() {
        doRegister(ALICE_CPF, "Transfer Alice", "1990-05-01", ALICE_EMAIL, "+5511999990010", PASS);
        doRegister(BOB_CPF,   "Transfer Bob",   "1988-11-20", BOB_EMAIL,   "+5521988880020", PASS);

        aliceToken     = doLogin(ALICE_EMAIL, PASS);
        var bobToken   = doLogin(BOB_EMAIL, PASS);
        aliceAccountId = doOpenAccount(aliceToken);
        bobAccountId   = doOpenAccount(bobToken);

        // Creditar saldo diretamente para evitar dependência de um endpoint de depósito
        jdbc.update("UPDATE accounts SET balance = ? WHERE id = ?",
            new BigDecimal("5000.00"), aliceAccountId);
    }

    @Test
    @Order(1)
    void transferencia_feliz_deve_retornar_201_concluida() {
        var body = Map.of(
            "sourceAccountId", aliceAccountId.toString(),
            "targetAccountId", bobAccountId.toString(),
            "amount", 100.00,
            "description", "Teste happy path"
        );

        var resp = postWithIdempotency("/transfers", body, aliceToken,
            UUID.randomUUID().toString(), Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        var tx = resp.getBody();
        assertThat(tx).isNotNull();
        assertThat(tx.get("status")).isEqualTo("CONCLUIDA");
        assertThat(tx.get("amount")).isEqualTo("100.00");
        assertThat(tx.get("transactionId").toString()).hasSize(36);
        assertThat(tx.get("endToEndId").toString()).hasSize(32).startsWith("E");
    }

    @Test
    @Order(2)
    void mesma_idempotency_key_deve_retornar_mesmo_resultado() {
        var idemKey = UUID.randomUUID().toString();
        var body = Map.of(
            "sourceAccountId", aliceAccountId.toString(),
            "targetAccountId", bobAccountId.toString(),
            "amount", 50.00
        );

        var resp1 = postWithIdempotency("/transfers", body, aliceToken, idemKey, Map.class);
        var resp2 = postWithIdempotency("/transfers", body, aliceToken, idemKey, Map.class);

        assertThat(resp1.getStatusCode().value()).isEqualTo(201);
        assertThat(resp2.getStatusCode().value()).isEqualTo(201);
        assertThat(resp1.getBody()).isNotNull();
        assertThat(resp2.getBody()).isNotNull();
        // Segunda chamada com mesma chave retorna exatamente o mesmo resultado (sem segundo débito)
        assertThat(resp1.getBody().get("transactionId"))
            .isEqualTo(resp2.getBody().get("transactionId"));
        assertThat(resp1.getBody().get("endToEndId"))
            .isEqualTo(resp2.getBody().get("endToEndId"));
    }

    @Test
    @Order(3)
    void transferencia_com_valor_acima_do_saldo_deve_retornar_422() {
        var body = Map.of(
            "sourceAccountId", aliceAccountId.toString(),
            "targetAccountId", bobAccountId.toString(),
            "amount", 999999.00 // excede saldo e limite diário
        );

        var resp = postWithIdempotency("/transfers", body, aliceToken,
            UUID.randomUUID().toString(), Map.class);

        // InsufficientBalance ou DailyLimitExceeded — ambos mapeados para 422 no GlobalExceptionHandler
        assertThat(resp.getStatusCode().value()).isEqualTo(422);
    }

    @Test
    @Order(4)
    @SuppressWarnings("unchecked")
    void extrato_deve_listar_transacoes_concluidas_da_conta() {
        var resp = get("/accounts/" + aliceAccountId + "/statement?size=10", aliceToken, Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        var body = resp.getBody();
        assertThat(body).isNotNull();

        // Spring Page serializado como Map com campo "content"
        var content = (List<Map<String, Object>>) body.get("content");
        assertThat(content).isNotNull().hasSizeGreaterThanOrEqualTo(2);

        // Todos os itens do extrato de Alice são transferências internas concluídas
        content.forEach(item -> {
            assertThat(item.get("type")).isEqualTo("TRANSFERENCIA_INTERNA");
            assertThat(item.get("status")).isEqualTo("CONCLUIDA");
        });
    }
}
