package com.finkin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base para todos os testes de integração do Finkin Bank.
 *
 * Pattern: Singleton container — Postgres e Redis são iniciados uma vez por JVM
 * e compartilhados entre todas as classes de teste, reduzindo tempo de setup.
 * @DynamicPropertySource injeta as portas mapeadas pelos containers no contexto Spring.
 *
 * Usa RestClient (Spring Framework 7) em vez do TestRestTemplate removido no Spring Boot 4.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("it")
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine")
                    .withDatabaseName("finkin_it")
                    .withUsername("finkin")
                    .withPassword("finkin");

    @SuppressWarnings("resource")
    private static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    static {
        POSTGRES.start();
        REDIS.start();
    }

    @DynamicPropertySource
    static void overrideContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @LocalServerPort
    protected int port;

    @Autowired
    private RestClient.Builder restClientBuilder;

    // ── HTTP helpers ──────────────────────────────────────────────────────

    /** Retorna um RestClient configurado para o servidor de teste.
     *  Status de erro (4xx/5xx) são tratados manualmente nas asserções. */
    protected RestClient client() {
        return restClientBuilder
                .clone()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
                .build();
    }

    protected <T> ResponseEntity<T> post(String url, Object body, Class<T> type) {
        return client().post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(type);
    }

    protected <T> ResponseEntity<T> post(String url, Object body, String token, Class<T> type) {
        return client().post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(token))
                .body(body)
                .retrieve()
                .toEntity(type);
    }

    /** POST com Idempotency-Key — obrigatório em endpoints de transferência e Pix. */
    protected <T> ResponseEntity<T> postWithIdempotency(String url, Object body, String token,
                                                         String idemKey, Class<T> type) {
        return client().post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> {
                    h.setBearerAuth(token);
                    h.set("Idempotency-Key", idemKey);
                })
                .body(body)
                .retrieve()
                .toEntity(type);
    }

    protected <T> ResponseEntity<T> get(String url, String token, Class<T> type) {
        return client().get().uri(url)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toEntity(type);
    }

    // ── Helpers de negócio ────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    protected String doRegister(String cpf, String fullName, String birthDate,
                                String email, String phone, String password) {
        var body = Map.of(
            "cpf", cpf,
            "fullName", fullName,
            "birthDate", birthDate,
            "email", email,
            "phone", phone,
            "password", password
        );
        var resp = post("/auth/register", body, Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        return (String) Objects.requireNonNull(resp.getBody()).get("customerId");
    }

    @SuppressWarnings("unchecked")
    protected String doLogin(String email, String password) {
        var resp = post("/auth/login", Map.of("email", email, "password", password), Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        return (String) Objects.requireNonNull(resp.getBody()).get("token");
    }

    @SuppressWarnings("unchecked")
    protected UUID doOpenAccount(String token) {
        var resp = post("/accounts", Map.of("type", "CORRENTE"), token, Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        return UUID.fromString((String) Objects.requireNonNull(resp.getBody()).get("id"));
    }

    @SuppressWarnings("unchecked")
    protected UUID doRegisterPixKey(String token, UUID accountId, String keyType, String keyValue) {
        var body = (keyValue != null)
            ? Map.of("keyType", keyType, "keyValue", keyValue)
            : Map.of("keyType", keyType);
        var resp = post("/accounts/" + accountId + "/pix-keys", body, token, Map.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        return UUID.fromString((String) Objects.requireNonNull(resp.getBody()).get("id"));
    }
}
