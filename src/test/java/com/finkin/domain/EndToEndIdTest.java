package com.finkin.domain;

import com.finkin.domain.model.transaction.EndToEndId;
import com.finkin.shared.BankConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.*;

class EndToEndIdTest {

    @Test
    void shouldHaveExactly32Chars() {
        var id = EndToEndId.generate();
        assertThat(id.getValue()).hasSize(BankConstants.END_TO_END_ID_LENGTH);
    }

    @Test
    void shouldStartWithPrefixAndIspb() {
        var id = EndToEndId.generate();
        assertThat(id.getValue()).startsWith("E" + BankConstants.ISPB);
    }

    @Test
    void shouldEncodeTimestamp() {
        var fixed = ZonedDateTime.parse("2026-04-25T18:30:00Z");
        var id = EndToEndId.generate(fixed);
        // datetime parte: yyyyMMddHHmm → 202604251830
        assertThat(id.getValue()).contains("202604251830");
    }

    @RepeatedTest(10)
    void shouldBeUniqueOnEachGeneration() {
        var a = EndToEndId.generate();
        var b = EndToEndId.generate();
        assertThat(a.getValue()).isNotEqualTo(b.getValue());
    }

    @Test
    void shouldRejectInvalidLength() {
        assertThatThrownBy(() -> new EndToEndId("tooshort"))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
