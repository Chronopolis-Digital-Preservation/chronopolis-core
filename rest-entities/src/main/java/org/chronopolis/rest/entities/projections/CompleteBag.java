package org.chronopolis.rest.entities.projections;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;
import org.chronopolis.rest.models.enums.BagStatus;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author shake
 */
@Getter
@ToString
public class CompleteBag extends PartialBag {

    private final Map<String, StagingView> storage;

    @QueryProjection
    public CompleteBag(Long id,
                       String name,
                       String creator,
                       Long size,
                       Long totalFiles,
                       BagStatus status,
                       ZonedDateTime createdAt,
                       ZonedDateTime updatedAt,
                       String depositor,
                       Set<String> replicatingNodes,
                       Map<String, StagingView> storage) {
        super(
                id,
                name,
                creator,
                size,
                totalFiles,
                status,
                createdAt,
                updatedAt,
                depositor,
                replicatingNodes
        );
        this.storage = storage;
    }
}
