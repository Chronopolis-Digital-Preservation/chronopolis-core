package org.chronopolis.rest.entities;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.entities.depositor.Depositor;
import org.chronopolis.rest.entities.storage.StagingStorage;
import org.chronopolis.rest.models.enums.BagStatus;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static org.chronopolis.rest.entities.BagDistributionStatus.REPLICATE;

/**
 * A BagIt Bag stored by Chronopolis. Soon to be generalized into collection.
 *
 * Each Bag is required to have a unique {@link Bag#name}, and as such it is the natural id for the
 * entity and used for equality checks.
 *
 * todo: we might want {@link Bag#creator} to map to the user table
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Bag extends UpdatableEntity implements Comparable<Bag> {

    /**
     * A unique identifier for the {@link Bag}
     */
    @NaturalId
    @EqualsAndHashCode.Include
    private String name;

    /**
     * The user who created the {@link Bag}
     */
    private String creator;

    /**
     * The size of the {@link Bag} in bytes
     */
    private Long size;

    /**
     * The number of files contained within the {@link Bag}
     */
    private Long totalFiles;

    /**
     * The status of the {@link Bag} for use in determining where in its lifecycle it is in the
     * Chronopolis Network
     */
    @Enumerated(value = EnumType.STRING)
    private BagStatus status = BagStatus.DEPOSITED;

    // joins

    /**
     * The {@link Depositor} who owns the payload data of this {@link Bag}
     */
    @ManyToOne
    @JoinColumn(name = "depositor_id")
    private Depositor depositor;

    /**
     * The {@link DataFile}s contained by this {@link Bag}
     *
     * The number of {@link BagFile}s must match {@link Bag#totalFiles}
     */
    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = LAZY, orphanRemoval = true)
    private Set<DataFile> files = new HashSet<>();

    /**
     * The {@link StagingStorage} used for distribution of the {@link Bag}
     */
    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = LAZY, orphanRemoval = true)
    private Set<StagingStorage> storage = new HashSet<>();

    /**
     * The {@link BagDistribution}s determining which {@link Node} this {@link Bag} will be
     * distributed to
     */
    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = EAGER, orphanRemoval = true)
    private Set<BagDistribution> distributions = new HashSet<>();

    public Bag(String name,
               String creator,
               Depositor depositor,
               Long size,
               Long totalFiles,
               BagStatus status) {
        this.name = name;
        this.creator = creator;
        this.depositor = depositor;
        this.size = size;
        this.totalFiles = totalFiles;
        this.status = status;
    }


    // persistence helpers

    /**
     * Retrieve all {@link Node#username}s which this {@link Bag} will be distributed to
     *
     * @return a {@link Set} of {@link Node#username}s
     */
    public Set<String> getReplicatingNodes() {
        return distributions.stream()
                // ideally this would say who has replicated
                // imo we should change it, as it won't impact operations but will make tests fail
                // .filter(dist -> dist.getStatus() == BagDistributionStatus.REPLICATE)
                .map(dist -> dist.getNode().getUsername())
                .collect(Collectors.toSet());
    }

    /**
     * Create a {@link BagDistribution} for a {@link Bag} by providing the {@link Node} and
     * {@link BagDistributionStatus}
     *
     * todo: this can probably just provide the {@link Node}
     *
     * @param node the {@link Node} to distribute the {@link Bag} to
     * @param status the {@link BagDistributionStatus} of the distribution
     */
    public void addDistribution(Node node, BagDistributionStatus status) {
        distributions.add(new BagDistribution(this, node, status));
    }

    @PreUpdate
    protected void onUpdateBag() {
        AtomicBoolean match = new AtomicBoolean(true);
        distributions.forEach(dist -> match.set(match.get() && dist.getStatus() == REPLICATE));
        if (match.get() && !distributions.isEmpty()) {
            status = BagStatus.PRESERVED;
        }
    }

    @Override
    public int compareTo(@NotNull Bag bag) {
        return ComparisonChain.start()
                .compare(depositor, bag.depositor)
                .compare(name, bag.name)
                .result();
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .toString();
    }

}
