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
 * AceToken connected to a single {@link BagFile}
 *
 * Equality is determined by the contents of the token:
 * round, proof, algorithm, host, service, and the createDate all must match
 *
 * @author shake
 */
@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AceToken extends PersistableEntity {

    /**
     * The round return by the IMS
     */
    @EqualsAndHashCode.Include private Long round;

    /**
     * The proof returned by the IMS
     */
    @EqualsAndHashCode.Include private String proof;

    /**
     * The digest algorithm used by the IMS
     */
    @EqualsAndHashCode.Include private String algorithm;

    /**
     * The fqdn of the IMS
     */
    @EqualsAndHashCode.Include private String imsHost;

    /**
     * The digest service used by the IMS
     */
    @EqualsAndHashCode.Include private String imsService;

    /**
     * The create date of the ACE Token returned by the IMS
     */
    @EqualsAndHashCode.Include private Date createDate;

    /**
     * The {@link Bag} which the {@link AceToken#file} belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    private Bag bag;

    /**
     * The {@link BagFile} which this ACE Token is for
     */
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
                .add("bagId", getBag() != null ? getBag().getId() : "null")
                .add("fileId", getFile() != null ? getFile().getId() : "null")
                .toString();
    }
}
