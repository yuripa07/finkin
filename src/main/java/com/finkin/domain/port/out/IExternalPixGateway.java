package com.finkin.domain.port.out;

import java.math.BigDecimal;

/**
 * Port de saída para comunicação com o SPI (Sistema de Pagamentos Instantâneos) externo.
 *
 * Fase 1: implementado por SpiClientMockAdapter (simulação com delay).
 * Fase 2: implementado por SpiHttpClientAdapter (chamada HTTP real com mTLS).
 *
 * Padrão: Adapter — desacopla o domínio do protocolo de comunicação do SPI.
 */
public interface IExternalPixGateway {

    /**
     * Envia Pix para uma conta externa (fora do Finkin).
     * @return true se liquidado com sucesso; false se o SPI retornou falha.
     */
    boolean sendExternal(String endToEndId, String targetIspb,
                         String targetAccount, BigDecimal amount);
}
