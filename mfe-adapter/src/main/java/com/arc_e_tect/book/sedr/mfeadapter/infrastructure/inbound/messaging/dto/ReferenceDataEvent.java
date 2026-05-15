package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.inbound.messaging.dto;

import java.util.Map;

/**
 * DTO representing a reference-data event received from Kafka.
 *
 * <p>Published by the Reference Data microservice whenever an entry is
 * created, updated, or deleted.  The MFA consumes these events to keep
 * its local reference-data cache up to date.
 *
 * @param eventType  one of {@code CREATE}, {@code UPDATE}, or {@code DELETE}
 * @param dataType   logical category, e.g. {@code "country-codes"}
 * @param code       primary code of the entry, e.g. {@code "NL"}
 * @param name       human-readable name, e.g. {@code "Netherlands"}
 * @param attributes optional additional metadata
 */
public record ReferenceDataEvent(
        String eventType,
        String dataType,
        String code,
        String name,
        Map<String, String> attributes) {
}
