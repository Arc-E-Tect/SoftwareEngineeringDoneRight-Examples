package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.http;

import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PayloadShaperInnerTokenServiceAdapter")
class PayloadShaperInnerTokenServiceAdapterTest {

    private final PayloadShaperInnerTokenServiceAdapter adapter = new PayloadShaperInnerTokenServiceAdapter();

    @Test
    @DisplayName("swapForInnerToken → returns null (no auth service in vanilla)")
    void swapForInnerToken_alwaysReturnsNull() {
        UserToken userToken = new UserToken("tok", Instant.now().plusSeconds(300), "sub-1");

        assertThat(adapter.swapForInnerToken(userToken)).isNull();
    }
}
