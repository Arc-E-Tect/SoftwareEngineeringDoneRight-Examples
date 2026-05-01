package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * JPA entity representing an MFA session in the H2 store.
 *
 * <p>In production deployments this table should be replaced by a
 * distributed cache (Redis / DataGrid) configured via the
 * {@link com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.SessionStorePort} adapter.
 */
@Entity
@Table(name = "mfa_sessions")
public class SessionEntity {

    @Id
    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "user_token_value", nullable = false, columnDefinition = "TEXT")
    private String userTokenValue;

    @Column(name = "user_token_expires_at", nullable = false)
    private Instant userTokenExpiresAt;

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Column(name = "session_expires_at", nullable = false)
    private Instant sessionExpiresAt;

    protected SessionEntity() {
    }

    public SessionEntity(String sessionId, String userTokenValue,
                         Instant userTokenExpiresAt, String subject,
                         Instant sessionExpiresAt) {
        this.sessionId = sessionId;
        this.userTokenValue = userTokenValue;
        this.userTokenExpiresAt = userTokenExpiresAt;
        this.subject = subject;
        this.sessionExpiresAt = sessionExpiresAt;
    }

    public String getSessionId() { return sessionId; }
    public String getUserTokenValue() { return userTokenValue; }
    public Instant getUserTokenExpiresAt() { return userTokenExpiresAt; }
    public String getSubject() { return subject; }
    public Instant getSessionExpiresAt() { return sessionExpiresAt; }
}
