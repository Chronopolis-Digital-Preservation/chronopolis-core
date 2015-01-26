package org.chronopolis.rest.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

/**
 * Representation of a bag in chronopolis
 *
 * TODO: Flesh out status and how to reflect that in chronopolis
 *
 * Created by shake on 11/5/14.
 */
@Entity
public class Bag {

    @Id
    @GeneratedValue
    @JsonProperty("id")
    // Because the replication class has a ManyToOne relationship, we need to
    // define ID as uppercase so it can join correctly
    private Long ID;

    private String name;
    private String depositor;

    // Both locations are relative
    // TODO: It would be better to have them be Paths
    private String location;
    private String tokenLocation;

    @JsonIgnore
    private String tokenDigest;

    @JsonIgnore
    private String tagManifestDigest;

    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private BagStatus status;

    private String fixityAlgorithm;
    private long size;

    protected Bag() { // JPA
    }

    public Bag(String name, String depositor) {
        this.name = name;
        this.depositor = depositor;
        this.status = BagStatus.STAGED;
    }

    public Long getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDepositor() {
        return depositor;
    }

    public void setDepositor(final String depositor) {
        this.depositor = depositor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public String getTokenLocation() {
        return tokenLocation;
    }

    public void setTokenLocation(final String tokenLocation) {
        this.tokenLocation = tokenLocation;
    }

    public String getTokenDigest() {
        return tokenDigest;
    }

    public void setTokenDigest(final String tokenDigest) {
        this.tokenDigest = tokenDigest;
    }

    public String getTagManifestDigest() {
        return tagManifestDigest;
    }

    public void setTagManifestDigest(final String tagManifestDigest) {
        this.tagManifestDigest = tagManifestDigest;
    }

    public String getFixityAlgorithm() {
        return fixityAlgorithm;
    }

    public void setFixityAlgorithm(final String fixityAlgorithm) {
        this.fixityAlgorithm = fixityAlgorithm;
    }

    public long getSize() {
        return size;
    }

    public void setSize(final long size) {
        this.size = size;
    }

    public String resourceID() {
        return "bag/" + ID;
    }

}