package com.finkin.domain;

import com.finkin.domain.model.customer.Cpf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class CpfTest {

    // CPFs gerados com o algoritmo oficial (válidos)
    @ParameterizedTest
    @ValueSource(strings = {
        "529.982.247-25",   // formatado
        "52998224725",      // numérico (mesmo CPF acima)
        "111.444.777-35",
        "123.456.789-09"    // verificado manualmente com o algoritmo da Receita
    })
    void shouldAcceptValidCpf(String cpf) {
        assertThatNoException().isThrownBy(() -> new Cpf(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "111.111.111-11",   // todos iguais — inválido pelo algoritmo
        "000.000.000-00",
        "999.999.999-99",
        "123.456.789-00",   // DV errado
        "1234567890",       // 10 dígitos
        "123456789012",     // 12 dígitos
        "",
        "abc.def.ghi-jk"
    })
    void shouldRejectInvalidCpf(String cpf) {
        assertThatThrownBy(() -> new Cpf(cpf))
            .isInstanceOf(Cpf.InvalidCpfException.class);
    }

    @Test
    void shouldNormalizePunctuation() {
        var cpf = new Cpf("529.982.247-25");
        assertThat(cpf.getValue()).isEqualTo("52998224725");
    }

    @Test
    void shouldFormatCorrectly() {
        var cpf = new Cpf("52998224725");
        assertThat(cpf.formatted()).isEqualTo("529.982.247-25");
    }

    @Test
    void shouldBeEqualByValue() {
        var a = new Cpf("529.982.247-25");
        var b = new Cpf("52998224725");
        assertThat(a).isEqualTo(b);
    }
}
