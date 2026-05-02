package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.adapter;

import com.arc_e_tect.examples.mfeadapter.domain.model.Session;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("VanillaSessionStoreAdapter")
class VanillaSessionStoreAdapterTest {

    private final VanillaSessionStoreAdapter adapter = new VanillaSessionStoreAdapter();

    @Test
    @DisplayName("findById – always returns an anonymous session with the given id")
    void findById_alwaysReturnsAnonymousSession() {
        Optional<Session> result = adapter.findById("any-id");

        assertThat(result).isPresent();
        Session session = result.get();
        assertThat(session.sessionId()).isEqualTo("any-id");
        assertThat(session.subject()).isEqualTo("anonymous");
        assertThat(session.userToken().tokenValue()).isEqualTo("anonymous");
        assertThat(session.isExpired()).isFalse();
    }

    @Test
    @DisplayName("save – is a no-op and does not throw")
    void save_isNoOp() {
        Session session = adapter.findById("sid").orElseThrow();
        assertThatCode(() -> adapter.save(session)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deleteById – is a no-op and does not throw")
    void deleteById_isNoOp() {
        assertThatCode(() -> adapter.deleteById("any-id")).doesNotThrowAnyException();
    }
}
