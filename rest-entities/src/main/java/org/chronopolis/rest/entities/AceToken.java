package org.chronopolis.rest.entities;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AceToken extends PersistableEntity {

    @EqualsAndHashCode.Include private Long round;
    @EqualsAndHashCode.Include private String proof;
    @EqualsAndHashCode.Include private String algorithm;
    @EqualsAndHashCode.Include private String imsHost;
    @EqualsAndHashCode.Include private String imsService;
    @EqualsAndHashCode.Include private Date createDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Bag bag;

    @OneToOne
    @JoinColumn(name = "file_id")
    private BagFile file;

    public AceToken(String proof,
                    Long round,
                    String imsService,
                    String algorithm,
                    String imsHost,
                    Date date,
                    Bag bag,
                    BagFile bagFile) {
        this.proof = proof;
        this.round = round;
        this.imsService = imsService;
        this.algorithm = algorithm;
        this.imsHost = imsHost;
        this.createDate = date;
        this.bag = bag;
        this.file = bagFile;
    }

    public ZonedDateTime formatDate() {
        return createDate.toInstant().atZone(ZoneOffset.UTC);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("bagId", getBag().getId())
                .add("fileId", getFile().getId())
                .toString();
    }
}
