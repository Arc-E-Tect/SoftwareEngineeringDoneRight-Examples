package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.repository;

import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.entity.ReferenceDataEntity;
import com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.entity.ReferenceDataEntity.ReferenceDataId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link ReferenceDataEntity}.
 */
@Repository
public interface SpringDataReferenceDataRepository
        extends JpaRepository<ReferenceDataEntity, ReferenceDataId> {

    /**
     * Retrieve all cached entries for a given data type.
     *
     * @param dataType the logical category
     * @return list of matching entities
     */
    List<ReferenceDataEntity> findByDataType(String dataType);
}
