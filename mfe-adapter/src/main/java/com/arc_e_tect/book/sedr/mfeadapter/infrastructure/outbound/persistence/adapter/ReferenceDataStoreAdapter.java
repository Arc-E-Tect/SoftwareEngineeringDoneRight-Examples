package com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.persistence.adapter;

import com.arc_e_tect.book.sedr.mfeadapter.domain.model.ReferenceDataEntry;
import com.arc_e_tect.book.sedr.mfeadapter.domain.port.outbound.ReferenceDataStorePort;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.persistence.entity.ReferenceDataEntity;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.persistence.entity.ReferenceDataEntity.ReferenceDataId;
import com.arc_e_tect.book.sedr.mfeadapter.infrastructure.outbound.persistence.repository.SpringDataReferenceDataRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Adapter that implements {@link ReferenceDataStorePort} using H2 via
 * Spring Data JPA.
 *
 * <p>Replace with a distributed cache implementation (Redis/DataGrid) for
 * production deployments without touching the domain or application layers.
 */
@Component
@Transactional
public class ReferenceDataStoreAdapter implements ReferenceDataStorePort {

    private final SpringDataReferenceDataRepository repository;

    public ReferenceDataStoreAdapter(SpringDataReferenceDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public void upsert(ReferenceDataEntry entry) {
        ReferenceDataEntity entity = new ReferenceDataEntity(
                entry.dataType(),
                entry.code(),
                entry.name(),
                entry.attributes());
        repository.save(entity);
    }

    @Override
    public void delete(String dataType, String code) {
        repository.deleteById(new ReferenceDataId(dataType, code));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReferenceDataEntry> findByTypeAndCode(String dataType, String code) {
        return repository.findById(new ReferenceDataId(dataType, code))
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReferenceDataEntry> findAllByType(String dataType) {
        return repository.findByDataType(dataType).stream()
                .map(this::toDomain)
                .toList();
    }

    // -----------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------

    private ReferenceDataEntry toDomain(ReferenceDataEntity entity) {
        return new ReferenceDataEntry(
                entity.getDataType(),
                entity.getCode(),
                entity.getName(),
                entity.getAttributes());
    }
}
