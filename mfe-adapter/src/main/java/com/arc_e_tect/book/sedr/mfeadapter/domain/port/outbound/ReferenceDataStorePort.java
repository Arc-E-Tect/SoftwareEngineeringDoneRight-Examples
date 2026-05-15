package com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ReferenceDataEntry;

import java.util.List;
import java.util.Optional;

/**
 * Secondary port – persistence for the reference-data cache.
 *
 * <p>Entries are inserted or updated when Kafka events arrive from the
 * Reference Data microservice, and read during request validation.
 */
public interface ReferenceDataStorePort {

    /**
     * Insert or update a single reference-data entry.
     *
     * @param entry the entry to upsert
     */
    void upsert(ReferenceDataEntry entry);

    /**
     * Remove a reference-data entry.
     *
     * @param dataType the logical category
     * @param code     the primary code of the entry to remove
     */
    void delete(String dataType, String code);

    /**
     * Look up a reference-data entry by type and code.
     *
     * @param dataType the logical category
     * @param code     the primary code
     * @return an {@link Optional} containing the entry, or empty when not cached
     */
    Optional<ReferenceDataEntry> findByTypeAndCode(String dataType, String code);

    /**
     * Return all cached entries for a given data type.
     *
     * @param dataType the logical category
     * @return an immutable list of all entries for that type
     */
    List<ReferenceDataEntry> findAllByType(String dataType);
}
