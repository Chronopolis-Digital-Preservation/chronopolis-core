package org.chronopolis.rest.entities.projections;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;
import org.chronopolis.rest.models.enums.ReplicationStatus;

import java.time.ZonedDateTime;

/**
 * @author shake
 */
@Getter
@ToString
public class ReplicationView {
    private final Long id;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final ReplicationStatus status;
    private final String bagLink;
    private final String tokenLink;
    private final String protocol;
    private final String node;
    private final CompleteBag bag;
    private String receivedTagFixity;
    private String receivedTokenFixity;

    @QueryProjection
    public ReplicationView(Long id,
                           ZonedDateTime createdAt,
                           ZonedDateTime updatedAt,
                           ReplicationStatus status,
                           String bagLink,
                           String tokenLink,
                           String protocol,
                           String node,
                           CompleteBag bag) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.bagLink = bagLink;
        this.tokenLink = tokenLink;
        this.protocol = protocol;
        this.node = node;
        this.bag = bag;
    }

    @QueryProjection
    public ReplicationView(Long id,
                           ZonedDateTime createdAt,
                           ZonedDateTime updatedAt,
                           ReplicationStatus status,
                           String bagLink,
                           String tokenLink,
                           String protocol,
                           String receivedTagFixity,
                           String receivedTokenFixity,
                           String node,
                           CompleteBag bag) {
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.bagLink = bagLink;
        this.tokenLink = tokenLink;
        this.protocol = protocol;
        this.receivedTagFixity = receivedTagFixity;
        this.receivedTokenFixity = receivedTokenFixity;
        this.node = node;
        this.bag = bag;
    }
}
