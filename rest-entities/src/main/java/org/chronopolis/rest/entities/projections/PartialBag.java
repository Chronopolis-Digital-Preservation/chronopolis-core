package org.chronopolis.rest.entities.projections;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;
import org.chronopolis.rest.models.enums.BagStatus;

import java.time.ZonedDateTime;
import java.util.Set;

/**
 * @author shake
 */
@Getter
@ToString
public class PartialBag {
    private final Long id;
    private final Long size;
    private final Long totalFiles;
    private final String name;
    private final String creator;
    private final String depositor;
    private final BagStatus status;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime updatedAt;
    private final Set<String> replicatingNodes;

    @QueryProjection
    public PartialBag(Long id,
                      String name,
                      String creator,
                      Long size,
                      Long totalFiles,
                      BagStatus status,
                      ZonedDateTime createdAt,
                      ZonedDateTime updatedAt,
                      String depositor,
                      Set<String> replicatingNodes) {
        this.id = id;
        this.name = name;
        this.creator = creator;
        this.size = size;
        this.totalFiles = totalFiles;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.depositor = depositor;
        this.replicatingNodes = replicatingNodes;
    }
}
