package com.arc_e_tect.book.sedr.mfeadapter.domain.model;

import java.util.Map;

/**
 * Domain model representing a single entry in the reference-data cache.
 *
 * <p>Reference data (e.g. ISO country codes, currency codes) is consumed
 * from Kafka events published by the Reference Data microservice and stored
 * in the MFA's local cache.  This allows the MFA to validate incoming
 * request fields without calling a second microservice.
 *
 * @param dataType    logical category of the entry, e.g. {@code "country-codes"}
 * @param code        the primary code, e.g. {@code "NL"}
 * @param name        human-readable name, e.g. {@code "Netherlands"}
 * @param attributes  map of additional key-value metadata for this entry
 */
public record ReferenceDataEntry(
        String dataType,
        String code,
        String name,
        Map<String, String> attributes) {
}
