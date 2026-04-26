package com.finkin.infrastructure.adapter.in.web.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Injeta um correlationId em cada requisição HTTP.
 *
 * Por que: rastreabilidade de ponta a ponta em logs distribuídos.
 * O cliente pode enviar o header X-Correlation-Id próprio (útil para
 * correlacionar com o frontend ou outro microserviço); caso contrário,
 * geramos um UUID v4 novo.
 *
 * O correlationId é injetado no MDC do Logback (via chave "correlationId")
 * e devolvido no header de resposta para que o cliente possa referenciar
 * a requisição em caso de suporte.
 */
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        String correlationId = request.getHeader(HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            // Limpar MDC é crítico em ambientes com thread pool para evitar
            // vazamento de contexto entre requisições reutilizando a mesma thread.
            // (Java 25 + threads virtuais: cada requisição tem sua própria thread,
            //  mas a limpeza explícita é boa prática defensiva)
            MDC.remove(MDC_KEY);
        }
    }
}
