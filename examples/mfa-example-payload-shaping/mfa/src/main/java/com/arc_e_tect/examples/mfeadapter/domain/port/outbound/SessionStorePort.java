package com.arc_e_tect.examples.mfeadapter.domain.port.outbound;

import com.arc_e_tect.examples.mfeadapter.domain.model.Session;

import java.util.Optional;

/**
 * Secondary port – persistence for {@link Session} objects.
 *
 * <p>Implementations back this port with the configured store
 * (H2 for development, Redis/DataGrid for production).
 */
public interface SessionStorePort {

    /**
     * Persist a new session or overwrite an existing one with the same id.
     *
     * @param session the session to store
     */
    void save(Session session);

    /**
     * Look up a session by its identifier.
     *
     * @param sessionId the opaque session identifier
     * @return an {@link Optional} containing the session, or empty when not found
     */
    Optional<Session> findById(String sessionId);

    /**
     * Remove a session from the store (logout / expiry clean-up).
     *
     * @param sessionId the opaque session identifier
     */
    void deleteById(String sessionId);
}
