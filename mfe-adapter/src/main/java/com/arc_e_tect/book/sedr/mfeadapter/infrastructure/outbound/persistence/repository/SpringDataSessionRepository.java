package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.persistence.repository;

import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.persistence.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Spring Data JPA repository for {@link SessionEntity}.
 */
@Repository
public interface SpringDataSessionRepository extends JpaRepository<SessionEntity, String> {

    /**
     * Remove all sessions whose expiry timestamp is in the past.
     * Called by a scheduled task to keep the H2 store clean.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SessionEntity s WHERE s.sessionExpiresAt < :now")
    void deleteExpiredSessions(@Param("now") Instant now);
}
