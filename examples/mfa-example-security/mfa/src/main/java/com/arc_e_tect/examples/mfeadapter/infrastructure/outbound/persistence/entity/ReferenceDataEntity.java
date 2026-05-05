package com.arc_e_tect.examples.mfeadapter.infrastructure.outbound.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JPA entity representing a single entry in the reference-data cache.
 *
 * <p>A composite primary key of {@code (data_type, code)} uniquely identifies
 * each entry within the store.
 */
@Entity
@Table(name = "mfa_reference_data")
@IdClass(ReferenceDataEntity.ReferenceDataId.class)
public class ReferenceDataEntity {

    @Id
    @Column(name = "data_type", nullable = false, length = 100)
    private String dataType;

    @Id
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mfa_reference_data_attributes",
            joinColumns = {
                    @JoinColumn(name = "data_type", referencedColumnName = "data_type"),
                    @JoinColumn(name = "code", referencedColumnName = "code")
            })
    @MapKeyColumn(name = "attr_key", length = 100)
    @Column(name = "attr_value", length = 500)
    private Map<String, String> attributes = new HashMap<>();

    protected ReferenceDataEntity() {
    }

    public ReferenceDataEntity(String dataType, String code, String name,
                               Map<String, String> attributes) {
        this.dataType = dataType;
        this.code = code;
        this.name = name;
        this.attributes = new HashMap<>(attributes);
    }

    public String getDataType() { return dataType; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Map<String, String> getAttributes() { return Map.copyOf(attributes); }

    // -----------------------------------------------------------------
    // Composite primary key
    // -----------------------------------------------------------------

    public static class ReferenceDataId implements Serializable {
        private String dataType;
        private String code;

        public ReferenceDataId() {
        }

        public ReferenceDataId(String dataType, String code) {
            this.dataType = dataType;
            this.code = code;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReferenceDataId that)) return false;
            return Objects.equals(dataType, that.dataType) && Objects.equals(code, that.code);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dataType, code);
        }
    }
}
