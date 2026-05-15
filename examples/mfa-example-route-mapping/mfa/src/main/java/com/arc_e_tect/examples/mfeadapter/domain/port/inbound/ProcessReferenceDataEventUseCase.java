package com.arc_e_tect.examples.mfeadapter.domain.port.inbound;

import com.arc_e_tect.examples.mfeadapter.domain.model.ReferenceDataEntry;

/**
 * Primary port – processes a reference-data event received from Kafka.
 *
 * <p>The MFA subscribes to Kafka topics published by the Reference Data
 * microservice.  Each event either creates, updates, or removes an entry
 * in the local reference-data cache.
 */
public interface ProcessReferenceDataEventUseCase {

    /**
     * Apply a reference-data change to the local cache.
     *
     * @param eventType one of {@code "CREATE"}, {@code "UPDATE"}, or {@code "DELETE"}
     * @param entry     the reference-data entry that was created, updated, or deleted
     */
    void process(String eventType, ReferenceDataEntry entry);
}
