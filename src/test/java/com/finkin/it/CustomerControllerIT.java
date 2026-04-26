package com.finkin.it;

import com.finkin.AbstractIntegrationTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração para registro de customer e autenticação JWT.
 *
 * Os testes são ordenados para aproveitar o estado persistido:
 *  1. register → cria o customer no banco
 *  2. login → usa as credenciais criadas no passo 1
 *  3. duplicateCpf → tenta criar o mesmo CPF → 409
 *  4. wrongPassword → tenta login com senha errada → 401
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerControllerIT extends AbstractIntegrationTest {

    // CPF válido (mod 11 verificado): 123.456.789-09
    static final String CPF      = "123.456.789-09";
    static final String EMAIL    = "customer-it@test.dev";
    static final String PHONE    = "+5511999990009";
    static final String PASSWORD = "Test1234";

    @Test
    @Order(1)
    void register_deve_criar_customer_e_retornar_201() {
        var body = Map.of(
            "cpf", CPF,
            "fullName", "CustomerModel IT",
            "birthDate", "1995-03-10",
            "email", EMAIL,
            "phone", PHONE,
            "password", PASSWORD
        );

        var resp = post("/auth/register", body, Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        var responseBody = resp.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).containsKey("customerId");
        assertThat(responseBody.get("kycStatus")).isEqualTo("APPROVED"); // auto-approve=true no IT
        assertThat(responseBody.get("customerId").toString()).hasSize(36); // UUID format
    }

    @Test
    @Order(2)
    void login_com_credenciais_validas_deve_retornar_token_jwt() {
        var resp = post("/auth/login", Map.of("email", EMAIL, "password", PASSWORD), Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        var body = resp.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("tokenType")).isEqualTo("Bearer");
        String token = (String) body.get("token");
        assertThat(token).isNotBlank();
        // JWT tem 3 partes separadas por ponto
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @Order(3)
    void register_com_cpf_duplicado_deve_retornar_409() {
        var body = Map.of(
            "cpf", CPF,            // mesmo CPF do teste 1
            "fullName", "Outro CustomerModel",
            "birthDate", "1990-01-01",
            "email", "outro@test.dev", // email diferente, mas CPF igual
            "phone", "+5511988880099",
            "password", PASSWORD
        );

        var resp = post("/auth/register", body, Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    @Order(4)
    void login_com_senha_errada_deve_retornar_401() {
        var resp = post("/auth/login",
            Map.of("email", EMAIL, "password", "SenhaErrada9"),
            Map.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    @Order(5)
    void register_com_cpf_invalido_deve_retornar_400() {
        var body = Map.of(
            "cpf", "000.000.000-00", // CPF inválido — todos dígitos iguais
            "fullName", "CPF Inválido",
            "birthDate", "1990-01-01",
            "email", "invalido@test.dev",
            "phone", "+5511988880011",
            "password", PASSWORD
        );

        // Bean Validation rejeita antes de chegar ao domínio
        var resp = post("/auth/register", body, Map.class);

        // Pode ser 400 (Bean Validation) ou 422 (DomainException do CpfModel VO)
        assertThat(resp.getStatusCode().value()).isIn(400, 422);
    }
}
