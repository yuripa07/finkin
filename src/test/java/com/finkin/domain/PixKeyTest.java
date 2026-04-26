package com.finkin.domain;

import com.finkin.domain.exception.InvalidPixKeyException;
import com.finkin.domain.model.pix.PixKey;
import com.finkin.domain.model.pix.PixKeyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class PixKeyTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @Test
    void shouldCreateCpfKey() {
        var key = PixKey.create(ACCOUNT_ID, PixKeyType.CPF, "529.982.247-25");
        assertThat(key.getKeyValue()).isEqualTo("52998224725");
        assertThat(key.getKeyType()).isEqualTo(PixKeyType.CPF);
    }

    @Test
    void shouldRejectInvalidCpfKey() {
        assertThatThrownBy(() -> PixKey.create(ACCOUNT_ID, PixKeyType.CPF, "111.111.111-11"))
            .isInstanceOf(InvalidPixKeyException.class);
    }

    @Test
    void shouldCreateEmailKey() {
        var key = PixKey.create(ACCOUNT_ID, PixKeyType.EMAIL, "usuario@finkin.dev");
        assertThat(key.getKeyValue()).isEqualTo("usuario@finkin.dev");
    }

    @ParameterizedTest
    @ValueSource(strings = {"@invalido", "sem-arroba.com", ""})
    void shouldRejectInvalidEmailKey(String email) {
        assertThatThrownBy(() -> PixKey.create(ACCOUNT_ID, PixKeyType.EMAIL, email))
            .isInstanceOf(InvalidPixKeyException.class);
    }

    @Test
    void shouldCreatePhoneKey() {
        var key = PixKey.create(ACCOUNT_ID, PixKeyType.PHONE, "+5511999999999");
        assertThat(key.getKeyValue()).isEqualTo("+5511999999999");
    }

    @ParameterizedTest
    @ValueSource(strings = {"+5500999999999", "+1511999999999", "11999999999"})
    void shouldRejectInvalidPhoneKey(String phone) {
        assertThatThrownBy(() -> PixKey.create(ACCOUNT_ID, PixKeyType.PHONE, phone))
            .isInstanceOf(InvalidPixKeyException.class);
    }

    @Test
    void shouldCreateRandomKey() {
        var key = PixKey.createRandom(ACCOUNT_ID);
        assertThat(key.getKeyType()).isEqualTo(PixKeyType.RANDOM);
        // Valor deve ser um UUID v4 válido
        assertThatNoException().isThrownBy(() -> UUID.fromString(key.getKeyValue()));
    }

    @Test
    void shouldAssignId() {
        var key = PixKey.createRandom(ACCOUNT_ID);
        assertThat(key.getId()).isNotNull();
    }
}
