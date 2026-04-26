package com.finkin.it;

import com.finkin.AbstractIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para envio de Pix interno e emissão de comprovante.
 *
 * Fase 1: apenas chaves Pix registradas no próprio Finkin (liquidação imediata).
 * A chave EMAIL do Bob é registrada no @BeforeAll e usada como destino.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PixControllerIT extends AbstractIntegrationTest {

    // CPFs válidos (módulo 11): 321.654.987-91 e 789.012.345-05
    private static final String PIX_ALICE_CPF   = "321.654.987-91";
    private static final String PIX_ALICE_EMAIL = "pix-alice@test.dev";
    private static final String PIX_BOB_CPF     = "789.012.345-05";
    private static final String PIX_BOB_EMAIL   = "pix-bob@test.dev";
    private static final String PASS            = "Pixtest1";

    @Autowired
    private JdbcTemplate jdbc;

    private String aliceToken;
    private UUID   aliceAccountId;
    private UUID   bobAccountId;
    private String lastPixTransactionId; // capturado no teste 1, usado no teste de comprovante

    @BeforeAll
    void setup() {
        doRegister(PIX_ALICE_CPF, "Pix Alice", "1992-07-15", PIX_ALICE_EMAIL, "+5531988880031", PASS);
        doRegister(PIX_BOB_CPF,   "Pix Bob",   "1985-09-22", PIX_BOB_EMAIL,   "+5541977770041", PASS);

        aliceToken      = doLogin(PIX_ALICE_EMAIL, PASS);
        var bobToken    = doLogin(PIX_BOB_EMAIL, PASS);
        aliceAccountId  = doOpenAccount(aliceToken);
        bobAccountId    = doOpenAccount(bobToken);

        // Chave Pix EMAIL do Bob — será o destino dos envios
        doRegisterPixKey(bobToken, bobAccountId, "EMAIL", PIX_BOB_EMAIL);

        // Saldo inicial de Alice para cobrir os Pix enviados
        jdbc.update("UPDATE accounts SET balance = ? WHERE id = ?",
            new BigDecimal("3000.00"), aliceAccountId);
    }

    @Test
    @Order(1)
    void pix_para_chave_interna_deve_liquidar_imediatamente() {
        var body = Map.of(
            "sourceAccountId", aliceAccountId.toString(),
            "targetKeyType", "EMAIL",
            "targetKeyValue", PIX_BOB_EMAIL,
            "amount", 200.00
        );

        var resp = postWithIdempotency("/pix/send", body, aliceToken,
            UUID.randomUUID().toString(), Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        var tx = resp.getBody();
        assertThat(tx).isNotNull();
        assertThat(tx.get("status")).isEqualTo("CONCLUIDA");
        assertThat(tx.get("amount")).isEqualTo("200.00");
        String txId = (String) tx.get("transactionId");
        assertThat(txId).hasSize(36);
        assertThat(tx.get("endToEndId").toString()).hasSize(32).startsWith("E");

        lastPixTransactionId = txId;
    }

    @Test
    @Order(2)
    void pix_para_chave_inexistente_deve_retornar_404() {
        var body = Map.of(
            "sourceAccountId", aliceAccountId.toString(),
            "targetKeyType", "EMAIL",
            "targetKeyValue", "nao-existe@test.dev",
            "amount", 10.00
        );

        var resp = postWithIdempotency("/pix/send", body, aliceToken,
            UUID.randomUUID().toString(), Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @Order(3)
    void pix_idempotente_deve_retornar_mesmo_transaction_id() {
        var idemKey = UUID.randomUUID().toString();
        var body = Map.of(
            "sourceAccountId", aliceAccountId.toString(),
            "targetKeyType", "EMAIL",
            "targetKeyValue", PIX_BOB_EMAIL,
            "amount", 50.00
        );

        var resp1 = postWithIdempotency("/pix/send", body, aliceToken, idemKey, Map.class);
        var resp2 = postWithIdempotency("/pix/send", body, aliceToken, idemKey, Map.class);

        assertThat(resp1.getStatusCode().value()).isEqualTo(201);
        assertThat(resp2.getStatusCode().value()).isEqualTo(201);
        assertThat(resp1.getBody()).isNotNull();
        assertThat(resp2.getBody()).isNotNull();
        assertThat(resp1.getBody().get("transactionId"))
            .isEqualTo(resp2.getBody().get("transactionId"));
    }

    @Test
    @Order(4)
    void comprovante_deve_conter_dados_completos_da_transacao() {
        // Usa o transactionId capturado no teste 1 (Pix enviado com sucesso)
        assertThat(lastPixTransactionId)
            .as("lastPixTransactionId deve ter sido capturado no teste 1")
            .isNotNull();

        var resp = get("/pix/transactions/" + lastPixTransactionId + "/receipt",
            aliceToken, Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        var body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("transactionId")).isEqualTo(lastPixTransactionId);
        assertThat(body.get("type")).isEqualTo("PIX_ENVIO");
        assertThat(body.get("status")).isEqualTo("CONCLUIDA");
        assertThat(body.get("amount")).isEqualTo("200.00");
        assertThat(body.get("currency")).isEqualTo("BRL");
        assertThat(body.get("endToEndId").toString()).hasSize(32);
    }

    @Test
    @Order(5)
    void comprovante_de_id_inexistente_deve_retornar_404() {
        var resp = get("/pix/transactions/" + UUID.randomUUID() + "/receipt", aliceToken, Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
    }
}
