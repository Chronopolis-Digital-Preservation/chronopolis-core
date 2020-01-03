package org.chronopolis.ingest.models;

import org.chronopolis.rest.models.enums.ReplicationStatus;

/**
 * This is pretty much the same as the ${link ReplicationRequest}, but with
 * a list of Nodes instead of a single
 *
 * Created by lsitu on 12/18/2019.
 */
public class ReplicationUpdate {

    private String collection;
    private String node;
    private ReplicationStatus status;

    public ReplicationUpdate() {
    }

    public ReplicationUpdate(String collection, ReplicationStatus status, String node) {
        this.collection = collection;
        this.status = status;
        this.node = node;
    }

    public String getCollection() {
        return this.collection;
    }

    public ReplicationUpdate setCollection(String collection) {
        this.collection = collection;
        return this;
    }

    public ReplicationStatus getStatus() {
        return this.status;
    }

    public ReplicationUpdate setStatus(ReplicationStatus status) {
        this.status = status;
        return this;
    }

    public String getNode() {
        return this.node;
    }

    public ReplicationUpdate setNode(String node) {
        this.node = node;
        return this;
    }
}
