package org.chronopolis.rest.entities.projections;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;

/**
 * @author shake
 */
@Getter
@ToString(onlyExplicitlyIncluded = true)
public class AceToken {
    @ToString.Include private final Long id;
    @ToString.Include private final Long bagId;
    @ToString.Include private final String filename;
    private final Long round;
    private final String imsHost;
    private final String imsService;
    private final String algorithm;
    private final String proof;
    private final Date createDate;

    @QueryProjection
    public AceToken(Long id,
                    Long bagId,
                    Long round,
                    String imsHost,
                    String imsService,
                    String algorithm,
                    String proof,
                    Date createDate,
                    String filename) {
        this.id = id;
        this.bagId = bagId;
        this.round = round;
        this.imsHost = imsHost;
        this.imsService = imsService;
        this.algorithm = algorithm;
        this.proof = proof;
        this.createDate = createDate;
        this.filename = filename;
    }
}
