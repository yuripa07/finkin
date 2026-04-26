package com.finkin.domain;

import com.finkin.domain.model.transaction.EndToEndIdModel;
import com.finkin.shared.BankConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class EndToEndIdModelTest {

    @Test
    void shouldHaveExactly32Chars() {
        var id = EndToEndIdModel.generate();
        assertThat(id.getValue()).hasSize(BankConstants.END_TO_END_ID_LENGTH);
    }

    @Test
    void shouldStartWithPrefixAndIspb() {
        var id = EndToEndIdModel.generate();
        assertThat(id.getValue()).startsWith("E" + BankConstants.ISPB);
    }

    @Test
    void shouldEncodeTimestamp() {
        var fixed = ZonedDateTime.parse("2026-04-25T18:30:00Z");
        var id = EndToEndIdModel.generate(fixed);
        // datetime parte: yyyyMMddHHmm → 202604251830
        assertThat(id.getValue()).contains("202604251830");
    }

    @RepeatedTest(10)
    void shouldBeUniqueOnEachGeneration() {
        var a = EndToEndIdModel.generate();
        var b = EndToEndIdModel.generate();
        assertThat(a.getValue()).isNotEqualTo(b.getValue());
    }

    @Test
    void shouldRejectInvalidLength() {
        assertThatThrownBy(() -> new EndToEndIdModel("tooshort"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
