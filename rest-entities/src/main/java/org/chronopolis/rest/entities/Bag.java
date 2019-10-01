package org.chronopolis.rest.entities;

import com.google.common.collect.ComparisonChain;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Bag extends UpdatableEntity implements Comparable<Bag> {

    @NaturalId
    @EqualsAndHashCode.Include
    private String name;
    private String creator;

    private Long size;
    private Long totalFiles;

    @Enumerated(value = EnumType.STRING)
    private BagStatus status = BagStatus.DEPOSITED;

    // joins

    @ManyToOne
    @JoinColumn(name = "depositor_id")
    private Depositor depositor;

    @ToString.Exclude
    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = LAZY, orphanRemoval = true)
    private Set<DataFile> files = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = LAZY, orphanRemoval = true)
    private Set<BagDistribution> distributions = new HashSet<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "bag", cascade = {MERGE, PERSIST}, fetch = EAGER, orphanRemoval = true)
    private Set<StagingStorage> storage = new HashSet<>();


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

    public Set<String> getReplicatingNodes() {
        return distributions.stream()
                // ideally this would say who has replicated
                // imo we should change it, as it won't impact operations but will make tests fail
                // .filter(dist -> dist.getStatus() == BagDistributionStatus.REPLICATE)
                .map(dist -> dist.getNode().getUsername())
                .collect(Collectors.toSet());
    }

    public void addFile(DataFile file) {
        files.add(file);
    }

    public void addFiles(Set<DataFile> toAdd) {
        files.addAll(toAdd);
    }

    public void addStagingStorage(StagingStorage staging) {
        storage.add(staging);
    }

    public void addDistribution(BagDistribution distribution) {
        distributions.add(distribution);
    }

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

}
