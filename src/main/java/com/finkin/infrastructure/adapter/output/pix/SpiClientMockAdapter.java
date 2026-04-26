package com.finkin.infrastructure.adapter.output.pix;

import com.finkin.domain.port.output.IExternalPixGateway;
import com.finkin.infrastructure.config.PixSpiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock do SPI (Sistema de Pagamentos Instantâneos) para fase 1.
 *
 * Pattern: Adapter — adapta a interface IExternalPixGateway ao protocolo
 * simulado do SPI. Em fase 2, este adapter é substituído por SpiHttpClientAdapter
 * que faz chamada HTTP real com mTLS e certificado ICP-Brasil.
 *
 * Configurável via application.yml (finkin.pix.spi.*):
 * - delay-ms: simula latência de liquidação no SPI (padrão 2000ms)
 * - failure-rate: probabilidade de falha simulada (0.0 = nunca falha)
 *
 * Java 25 virtual threads: Thread.sleep() não bloqueia uma thread de plataforma
 * quando executado em virtual thread — o carrier thread é liberado durante a espera.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpiClientMockAdapter implements IExternalPixGateway {

    private final PixSpiProperties properties;

    @Override
    public boolean sendExternal(String endToEndId, String targetIspb,
                                 String targetAccount, BigDecimal amount) {
        simulateLatency();

        boolean success = ThreadLocalRandom.current().nextDouble() > properties.getFailureRate();
        log.info("SPI mock: e2eId={}, ispb={}, success={}", endToEndId, targetIspb, success);
        return success;
    }

    private void simulateLatency() {
        if (properties.getDelayMs() > 0) {
            try {
                Thread.sleep(properties.getDelayMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
