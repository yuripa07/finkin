package com.finkin.infrastructure.adapter.in.web.auth;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que intercepta requisições HTTP e extrai o JWT do header Authorization.
 *
 * Fluxo:
 * 1. Lê "Authorization: Bearer <token>"
 * 2. Valida o token via JwtService
 * 3. Se válido: cria Authentication e injeta no SecurityContext
 * 4. Se inválido: ignora (a autenticação ficará nula; SecurityFilterChain negará o acesso)
 *
 * Por que OncePerRequestFilter: garante execução exatamente uma vez por requisição,
 * mesmo em cenários com forward/dispatch internos do Servlet container.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        extractToken(request)
            .flatMap(jwtService::validateAndExtract)
            .ifPresent(this::setAuthentication);

        chain.doFilter(request, response);
    }

    private java.util.Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return java.util.Optional.of(header.substring(BEARER_PREFIX.length()));
        }
        return java.util.Optional.empty();
    }

    private void setAuthentication(Claims claims) {
        // Papel básico ROLE_USER para todos os usuários autenticados.
        // Refinamento por perfil (ROLE_ADMIN, etc.) pode ser adicionado via claims customizados.
        var auth = new UsernamePasswordAuthenticationToken(
            claims.getSubject(),
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
