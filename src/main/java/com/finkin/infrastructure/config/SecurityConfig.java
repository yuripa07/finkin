package com.finkin.infrastructure.config;

import com.finkin.infrastructure.adapter.in.web.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    /**
     * Filtro de segurança principal.
     *
     * Decisões arquiteturais:
     * 1. STATELESS: JWT é auto-contido — não mantemos sessão no servidor.
     *    Isso alinha com a natureza horizontal do Open Finance (múltiplos microserviços).
     *
     * 2. CSRF desabilitado: APIs REST stateless com JWT não precisam de CSRF
     *    porque o token não é enviado automaticamente pelo browser (não é cookie).
     *
     * 3. Endpoints públicos: apenas /auth/** e documentação técnica.
     *    Todo o resto exige Bearer token válido.
     *
     * 4. Headers de segurança: proteção contra ataques comuns em APIs bancárias.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                // Autenticação: registro e login públicos
                .requestMatchers("/auth/**").permitAll()
                // Documentação técnica (restringir em produção se necessário)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Actuator health: público; outros endpoints exigem autenticação
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").authenticated()
                // Todo o resto requer autenticação
                .anyRequest().authenticated()
            )

            // JWT filter executado antes do filtro de autenticação padrão do Spring Security
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

            // Headers de segurança padrão para APIs bancárias
            .headers(headers -> headers
                // HSTS: força HTTPS por 1 ano (em produção)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000))
                // Impede que o browser infira o Content-Type da resposta
                .contentTypeOptions(ct -> {})
                // Evita vazamento de referrer para origens externas
                .referrerPolicy(rp ->
                    rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                // Desabilitar features desnecessárias no contexto bancário
                // (Permissions-Policy header adicionado manualmente em fase 2 via Filter)
            );

        return http.build();
    }

    /**
     * BCrypt com fator de custo 12.
     * Por que 12: custo computacional suficiente para resistir a ataques de força bruta
     * em 2025/2026, sem impacto perceptível para usuários legítimos (<100ms por hash).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
