package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.adapter;

import com.arc_e_tect.examples.mfeadapter.domain.model.Session;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.SessionStorePort;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * No-auth {@link SessionStorePort} for the payload-shaper (no-session) example.
 *
 * <p>Every session lookup returns a synthetic anonymous session so that the
 * proxy pipeline can operate without a real session store.  Save and delete
 * operations are no-ops.
 *
 * <p>Marked {@link Primary} so that it takes precedence over the JPA-backed
 * {@link SessionStoreAdapter} when this bean is present in the context.
 */
@Primary
@Component
public class PayloadShaperSessionStoreAdapter implements SessionStorePort {

    private static final UserToken ANONYMOUS_TOKEN =
            new UserToken("anonymous", Instant.MAX, "anonymous");

    @Override
    public Optional<Session> findById(String sessionId) {
        return Optional.of(new Session(sessionId, ANONYMOUS_TOKEN, Instant.MAX, "anonymous"));
    }

    @Override
    public void save(Session session) {
        // no-op – payload-shaper example has no session persistence
    }

    @Override
    public void deleteById(String sessionId) {
        // no-op – payload-shaper example has no session persistence
    }
}
