package org.chronopolis.rest.entities.projections;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;

/**
 * @author shake
 */
@Getter
@ToString
public class StagingView {
    private final Long id;
    private final String path;
    private final String type;
    private final Long region;
    private final Boolean active;
    private final Long totalFiles;

    @QueryProjection
    public StagingView(Long id,
                       String path,
                       String type,
                       Long region,
                       Boolean active,
                       Long totalFiles) {
        this.id = id;
        this.path = path;
        this.type = type;
        this.region = region;
        this.active = active;
        this.totalFiles = totalFiles;
    }
}
