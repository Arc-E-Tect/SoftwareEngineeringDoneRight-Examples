package com.arc_e_tect.book.sedr.mfeadapter.application.service;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ReferenceDataEntry;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.inbound.ProcessReferenceDataEventUseCase;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.ReferenceDataStorePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Application service responsible for maintaining the reference-data cache.
 *
 * <p>Kafka events from the Reference Data microservice are routed here via
 * the {@link com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.messaging.ReferenceDataEventConsumer}.
 * Each event is translated into an upsert or delete on the
 * {@link ReferenceDataStorePort}.
 */
@Service
public class ReferenceDataApplicationService implements ProcessReferenceDataEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataApplicationService.class);

    static final String EVENT_CREATE = "CREATE";
    static final String EVENT_UPDATE = "UPDATE";
    static final String EVENT_DELETE = "DELETE";

    private final ReferenceDataStorePort referenceDataStore;

    public ReferenceDataApplicationService(ReferenceDataStorePort referenceDataStore) {
        this.referenceDataStore = referenceDataStore;
    }

    @Override
    public void process(String eventType, ReferenceDataEntry entry) {
        switch (eventType.toUpperCase()) {
            case EVENT_CREATE, EVENT_UPDATE -> {
                log.debug("Upserting reference data [{}/{}]", entry.dataType(), entry.code());
                referenceDataStore.upsert(entry);
            }
            case EVENT_DELETE -> {
                log.debug("Deleting reference data [{}/{}]", entry.dataType(), entry.code());
                referenceDataStore.delete(entry.dataType(), entry.code());
            }
            default -> log.warn("Unknown reference-data event type '{}' – ignoring", eventType);
        }
    }
}
