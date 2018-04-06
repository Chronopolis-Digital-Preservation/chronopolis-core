package org.chronopolis.rest.entities.storage;

import org.chronopolis.rest.ZonedDateTimeConverter;
import org.chronopolis.rest.entities.PersistableEntity;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Track fixity values for objects in Chronopolis
 *
 * This can be applied to bags and token stores at the moment,
 * with the ability to push multiple algorithms to a single object
 * (useful in the case we need to migrate to a stronger algorithm)
 *
 * Created by shake on 7/14/17.
 */
@Entity
public class Fixity extends PersistableEntity {

    /**
     * There's the possibility we want to make a M2M relationship in a
     * separate table instead of the M2O here. If we store each file's
     * fixity in the future, it may be something useful to have. For now,
     * when testing, we'll keep it as as M2O
     *
     */
    @ManyToOne
    @JoinColumn(name = "storage_id", nullable = false)
    private StagingStorage storage;

    @Convert(converter = ZonedDateTimeConverter.class)
    private ZonedDateTime createdAt;

    private String value;
    private String algorithm;

    public Fixity() {
        // yadda yadda jpa yadda yadda
    }

    public StagingStorage getStorage() {
        return storage;
    }

    public Fixity setStorage(StagingStorage storage) {
        this.storage = storage;
        return this;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public Fixity setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Fixity setValue(String value) {
        this.value = value;
        return this;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public Fixity setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fixity fixity = (Fixity) o;
        return Objects.equals(value, fixity.value) &&
                Objects.equals(algorithm, fixity.algorithm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, algorithm);
    }
}
