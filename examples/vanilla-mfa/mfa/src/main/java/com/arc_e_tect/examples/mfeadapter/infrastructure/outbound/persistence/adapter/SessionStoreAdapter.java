package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.adapter;

import com.arc_e_tect.examples.mfeadapter.domain.model.Session;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.SessionStorePort;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.entity.SessionEntity;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.repository.SpringDataSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Adapter that implements {@link SessionStorePort} using H2 via Spring Data JPA.
 *
 * <p>Replace this adapter with a Redis/DataGrid implementation for production
 * deployments – the domain and application layers are not affected because
 * they depend only on the port interface.
 *
 * <p>A scheduled task purges expired sessions every 5 minutes to prevent
 * unbounded growth of the H2 store.
 */
@Component
@Transactional
public class SessionStoreAdapter implements SessionStorePort {

    private final SpringDataSessionRepository repository;

    public SessionStoreAdapter(SpringDataSessionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Session session) {
        SessionEntity entity = new SessionEntity(
                session.sessionId(),
                session.userToken().tokenValue(),
                session.userToken().expiresAt(),
                session.subject(),
                session.expiresAt());
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> findById(String sessionId) {
        return repository.findById(sessionId)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(String sessionId) {
        repository.deleteById(sessionId);
    }

    /**
     * Purge expired sessions from H2.
     * Runs every 5 minutes; adjust the cron expression in {@code application.yml}
     * if needed.
     */
    @Scheduled(fixedRateString = "${mfe-adapter.session.cleanup-interval-ms:300000}")
    public void purgeExpiredSessions() {
        repository.deleteExpiredSessions(Instant.now());
    }

    // -----------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------

    private Session toDomain(SessionEntity entity) {
        UserToken userToken = new UserToken(
                entity.getUserTokenValue(),
                entity.getUserTokenExpiresAt(),
                entity.getSubject());
        return new Session(
                entity.getSessionId(),
                userToken,
                entity.getSessionExpiresAt(),
                entity.getSubject());
    }
}
