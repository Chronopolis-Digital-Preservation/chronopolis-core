package org.chronopolis.rest.lombok;

import com.google.common.collect.ComparisonChain;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.chronopolis.rest.lombok.depositor.Depositor;
import org.chronopolis.rest.lombok.storage.StagingStorage;
import org.chronopolis.rest.models.enums.BagStatus;
import org.hibernate.annotations.NaturalId;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static org.chronopolis.rest.lombok.BagDistributionStatus.REPLICATE;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
public class Bag extends UpdatableEntity implements Comparable<Bag> {

    @NaturalId
    @EqualsAndHashCode.Include
    private String name;
    private String creator;

    private Long size;
    private Long totalFiles;
    private BagStatus status;

    // joins

    @ManyToOne
    @JoinColumn(name = "depositor_id")
    private Depositor depositor;

    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = LAZY, orphanRemoval = true)
    private Set<DataFile> files = new HashSet<>();

    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = LAZY, orphanRemoval = true)
    private Set<BagDistribution> distributions = new HashSet<>();

    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = EAGER, orphanRemoval = true)
    private Set<StagingStorage> storage = new HashSet<>();

    // persistence helpers

    private Set<String> getReplicatingNodes() {
        return new HashSet<>();
    }

    private void addFile(DataFile file) {
        files.add(file);
    }

    private void addFiles(Set<DataFile> toAdd) {
        files.addAll(toAdd);
    }

    private void addStagingStorage(StagingStorage staging) {
        storage.add(staging);
    }

    private void addDistribution(BagDistribution distribution) {
        distributions.add(distribution);
    }

    private void addDistribution(Node node, BagDistributionStatus status) {
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


}
