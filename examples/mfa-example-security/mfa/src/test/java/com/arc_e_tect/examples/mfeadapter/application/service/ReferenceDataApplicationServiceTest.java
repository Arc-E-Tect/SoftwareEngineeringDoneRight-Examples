package com.arc_e_tect.examples.mfeadapter.application.service;

import com.arc_e_tect.examples.mfeadapter.domain.model.ReferenceDataEntry;
import com.arc_e_tect.examples.mfeadapter.domain.port.outbound.ReferenceDataStorePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReferenceDataApplicationService")
class ReferenceDataApplicationServiceTest {

    @Mock
    private ReferenceDataStorePort referenceDataStore;

    private ReferenceDataApplicationService service;

    private static final ReferenceDataEntry NL_ENTRY = new ReferenceDataEntry(
            "country-codes", "NL", "Netherlands", Map.of("region", "EU"));

    @BeforeEach
    void setUp() {
        service = new ReferenceDataApplicationService(referenceDataStore);
    }

    @Test
    @DisplayName("CREATE event → entry is upserted")
    void process_createEvent_upsertsEntry() {
        service.process("CREATE", NL_ENTRY);

        verify(referenceDataStore).upsert(NL_ENTRY);
        verify(referenceDataStore, never()).delete(any(), any());
    }

    @Test
    @DisplayName("UPDATE event → entry is upserted")
    void process_updateEvent_upsertsEntry() {
        service.process("UPDATE", NL_ENTRY);

        verify(referenceDataStore).upsert(NL_ENTRY);
        verify(referenceDataStore, never()).delete(any(), any());
    }

    @Test
    @DisplayName("DELETE event → entry is deleted")
    void process_deleteEvent_deletesEntry() {
        service.process("DELETE", NL_ENTRY);

        verify(referenceDataStore).delete("country-codes", "NL");
        verify(referenceDataStore, never()).upsert(any());
    }

    @Test
    @DisplayName("lowercase event type is accepted")
    void process_lowercaseEventType_isHandled() {
        service.process("create", NL_ENTRY);

        verify(referenceDataStore).upsert(NL_ENTRY);
    }

    @Test
    @DisplayName("unknown event type is silently ignored – no store interaction")
    void process_unknownEventType_ignored() {
        service.process("MERGE", NL_ENTRY);

        verify(referenceDataStore, never()).upsert(any());
        verify(referenceDataStore, never()).delete(any(), any());
    }
}
