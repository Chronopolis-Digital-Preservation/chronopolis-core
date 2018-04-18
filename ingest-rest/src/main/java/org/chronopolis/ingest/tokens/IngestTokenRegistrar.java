package org.chronopolis.ingest.tokens;

import com.querydsl.jpa.impl.JPAQueryFactory;
import edu.umiacs.ace.ims.api.IMSUtil;
import edu.umiacs.ace.ims.ws.TokenResponse;
import org.chronopolis.ingest.repository.dao.PagedDAO;
import org.chronopolis.rest.entities.AceToken;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.QAceToken;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.tokenize.ManifestEntry;
import org.chronopolis.tokenize.registrar.TokenRegistrar;
import org.chronopolis.tokenize.supervisor.TokenWorkSupervisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.time.Instant;
import java.util.Map;

/**
 * Registrar which loads Tokens directly into the DB
 *
 * @author shake
 */
public class IngestTokenRegistrar implements TokenRegistrar {

    private final Logger log = LoggerFactory.getLogger(IngestTokenRegistrar.class);

    private static final String IMS_HOST = "ims.umiacs.umd.edu";

    private final PagedDAO dao;
    private final TokenWorkSupervisor supervisor;

    public IngestTokenRegistrar(PagedDAO dao, TokenWorkSupervisor supervisor) {
        this.dao = dao;
        this.supervisor = supervisor;
    }

    @Override
    public void register(Map<ManifestEntry, TokenResponse> tokenResponseMap) {
        tokenResponseMap.forEach((entry, response) -> {
            log.trace("[{}] Registering Token", response.getName());
            Long bagId = entry.getBag().getId();
            Bag bag = dao.findOne(QBag.bag, QBag.bag.id.eq(bagId));
            Instant responseInstant = response.getTimestamp().toGregorianCalendar().toInstant();

            String filename = getFilename(response);
            java.util.Date create = Date.from(responseInstant);
            AceToken token = new AceToken(bag, create, filename,
                    IMSUtil.formatProof(response),
                    IMS_HOST,
                    response.getDigestService(),
                    response.getDigestProvider(),
                    response.getRoundId());
            JPAQueryFactory qf = dao.getJPAQueryFactory();
            long count = qf.selectFrom(QAceToken.aceToken)
                    .where(QAceToken.aceToken.bag.id.eq(bagId)
                            .and(QAceToken.aceToken.filename.eq(filename)))
                    .fetchCount();

            // IMO there's a very large problem with this in that if the dao.save throws an
            // exception, only the single entry will be completed. We probably want to wrap
            // the entire operation in a try/catch/finally and if there is an exception thrown,
            // check if a Entry is being processed and if so retry register on it. Maybe marking the
            // excepted entry for removal.
            try {
                if (bag != null && count == 0) {
                    dao.save(token);
                }
            } finally {
                supervisor.complete(entry);
            }
        });
    }
}
