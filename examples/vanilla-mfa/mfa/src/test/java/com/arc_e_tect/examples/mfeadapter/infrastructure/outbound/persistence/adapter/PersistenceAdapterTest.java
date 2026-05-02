package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.adapter;

import com.arc_e_tect.examples.mfeadapter.domain.model.ReferenceDataEntry;
import com.arc_e_tect.examples.mfeadapter.domain.model.Session;
import com.arc_e_tect.examples.mfeadapter.domain.model.UserToken;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.entity.ReferenceDataEntity;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.entity.ReferenceDataEntity.ReferenceDataId;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.entity.SessionEntity;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.repository.SpringDataReferenceDataRepository;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.repository.SpringDataSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the persistence adapters.
 *
 * <p>Tests verify that adapters correctly translate between the domain model
 * and JPA entities, and that they delegate to the correct repository methods.
 * Repository behaviour is mocked – the ORM itself is not exercised here.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Persistence adapters – unit tests")
class PersistenceAdapterTest {

    @Mock
    private SpringDataSessionRepository sessionRepository;

    @Mock
    private SpringDataReferenceDataRepository referenceDataRepository;

    @InjectMocks
    private SessionStoreAdapter sessionStoreAdapter;

    @InjectMocks
    private ReferenceDataStoreAdapter referenceDataStoreAdapter;

    // -----------------------------------------------------------------
    // SessionStoreAdapter
    // -----------------------------------------------------------------

    @Test
    @DisplayName("save – delegates to repository with correct entity mapping")
    void session_save_delegatesToRepository() {
        Session session = buildSession("sid-001", Instant.now().plusSeconds(1800));

        sessionStoreAdapter.save(session);

        verify(sessionRepository).save(any(SessionEntity.class));
    }

    @Test
    @DisplayName("findById – maps entity back to domain Session")
    void session_findById_mapsToDomain() {
        Instant expiresAt = Instant.now().plusSeconds(1800);
        SessionEntity entity = new SessionEntity(
                "sid-002", "token-value", Instant.now().plusSeconds(3600),
                "user-sub", expiresAt);
        when(sessionRepository.findById("sid-002")).thenReturn(Optional.of(entity));

        Optional<Session> result = sessionStoreAdapter.findById("sid-002");

        assertThat(result).isPresent();
        assertThat(result.get().sessionId()).isEqualTo("sid-002");
        assertThat(result.get().subject()).isEqualTo("user-sub");
    }

    @Test
    @DisplayName("findById – returns empty when session does not exist")
    void session_findById_unknownId_returnsEmpty() {
        when(sessionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThat(sessionStoreAdapter.findById("missing")).isEmpty();
    }

    @Test
    @DisplayName("deleteById – delegates to repository")
    void session_deleteById_delegatesToRepository() {
        sessionStoreAdapter.deleteById("sid-003");

        verify(sessionRepository).deleteById("sid-003");
    }

    // -----------------------------------------------------------------
    // ReferenceDataStoreAdapter
    // -----------------------------------------------------------------

    @Test
    @DisplayName("upsert – delegates to repository with correct entity mapping")
    void referenceData_upsert_delegatesToRepository() {
        ReferenceDataEntry entry = new ReferenceDataEntry(
                "country-codes", "DE", "Germany", Map.of("region", "EU"));

        referenceDataStoreAdapter.upsert(entry);

        verify(referenceDataRepository).save(any(ReferenceDataEntity.class));
    }

    @Test
    @DisplayName("findByTypeAndCode – maps entity back to domain entry")
    void referenceData_findByTypeAndCode_mapsToDomain() {
        ReferenceDataEntity entity = new ReferenceDataEntity(
                "country-codes", "FR", "France", Map.of("capital", "Paris"));
        when(referenceDataRepository.findById(new ReferenceDataId("country-codes", "FR")))
                .thenReturn(Optional.of(entity));

        Optional<ReferenceDataEntry> result =
                referenceDataStoreAdapter.findByTypeAndCode("country-codes", "FR");

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("France");
        assertThat(result.get().attributes()).containsEntry("capital", "Paris");
    }

    @Test
    @DisplayName("delete – delegates to repository with composite key")
    void referenceData_delete_delegatesToRepository() {
        referenceDataStoreAdapter.delete("country-codes", "IT");

        verify(referenceDataRepository).deleteById(new ReferenceDataId("country-codes", "IT"));
    }

    @Test
    @DisplayName("findAllByType – returns mapped list of entries")
    void referenceData_findAllByType_returnsMappedList() {
        List<ReferenceDataEntity> entities = List.of(
                new ReferenceDataEntity("currencies", "EUR", "Euro", Map.of()),
                new ReferenceDataEntity("currencies", "USD", "US Dollar", Map.of()));
        when(referenceDataRepository.findByDataType("currencies")).thenReturn(entities);

        List<ReferenceDataEntry> result = referenceDataStoreAdapter.findAllByType("currencies");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReferenceDataEntry::code)
                .containsExactlyInAnyOrder("EUR", "USD");
    }

    @Test
    @DisplayName("purgeExpiredSessions – delegates to repository.deleteExpiredSessions")
    void session_purgeExpiredSessions_delegatesToRepository() {
        sessionStoreAdapter.purgeExpiredSessions();

        verify(sessionRepository).deleteExpiredSessions(any(java.time.Instant.class));
    }

    @Test
    @DisplayName("SessionEntity – all getters return correctly constructed values")
    void sessionEntity_accessors() {
        Instant tokenExpiry = Instant.now().plusSeconds(3600);
        Instant sessionExpiry = Instant.now().plusSeconds(1800);
        SessionEntity entity = new SessionEntity(
                "sid-x", "tkval", tokenExpiry, "subj", sessionExpiry);

        assertThat(entity.getSessionId()).isEqualTo("sid-x");
        assertThat(entity.getUserTokenValue()).isEqualTo("tkval");
        assertThat(entity.getUserTokenExpiresAt()).isEqualTo(tokenExpiry);
        assertThat(entity.getSubject()).isEqualTo("subj");
        assertThat(entity.getSessionExpiresAt()).isEqualTo(sessionExpiry);
    }

    @Test
    @DisplayName("ReferenceDataEntity + ReferenceDataId – equality and hashCode")
    void referenceDataEntity_andId_accessors() {
        ReferenceDataEntity entity = new ReferenceDataEntity(
                "country-codes", "US", "United States", Map.of("region", "NA"));

        assertThat(entity.getDataType()).isEqualTo("country-codes");
        assertThat(entity.getCode()).isEqualTo("US");
        assertThat(entity.getName()).isEqualTo("United States");
        assertThat(entity.getAttributes()).containsEntry("region", "NA");

        ReferenceDataId idA = new ReferenceDataId("country-codes", "US");
        ReferenceDataId idB = new ReferenceDataId("country-codes", "US");
        ReferenceDataId idC = new ReferenceDataId("country-codes", "DE");
        assertThat(idA).isEqualTo(idB);
        assertThat(idA).isNotEqualTo(idC);
        assertThat(idA.hashCode()).isEqualTo(idB.hashCode());
    }

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    private Session buildSession(String sessionId, Instant expiresAt) {
        UserToken token = new UserToken("token-value", Instant.now().plusSeconds(3600), "user-sub");
        return new Session(sessionId, token, expiresAt, "user-sub");
    }
}
