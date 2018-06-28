package org.chronopolis.ingest.tokens;

import com.querydsl.jpa.impl.JPAQueryFactory;
import edu.umiacs.ace.ims.api.IMSUtil;
import edu.umiacs.ace.ims.ws.TokenResponse;
import org.chronopolis.ingest.repository.dao.PagedDAO;
import org.chronopolis.ingest.support.Loggers;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Registrar which loads Tokens directly into the DB
 *
 * @author shake
 */
public class IngestTokenRegistrar implements TokenRegistrar, Runnable {

    private final Logger log = LoggerFactory.getLogger(Loggers.LOCAL_TOKENIZATION_LOG);

    private static final String IMS_HOST = "ims.umiacs.umd.edu";

    private final PagedDAO dao;
    private final TokenWorkSupervisor supervisor;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public IngestTokenRegistrar(PagedDAO dao, TokenWorkSupervisor supervisor) {
        this.dao = dao;
        this.supervisor = supervisor;
    }

    @Override
    public void run() {
        log.info("[TokenRegistrar] Starting");
        int size = 1000;
        int timeout = 500;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        while (running.get()) {
            Map<ManifestEntry, TokenResponse> responses
                    = supervisor.tokenizedEntries(size, timeout, timeUnit);
            register(responses);
        }
        log.info("[TokenRegistrar] Stopping");
    }

    public void close() {
        running.set(false);
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
            // The TokenClassName and DigestService don't really map well to what we store
            // maybe we store it wrong I'm not sure need to look into it further
            AceToken token = new AceToken(bag, create, filename,
                    IMSUtil.formatProof(response),
                    IMS_HOST,
                    response.getTokenClassName(),
                    response.getDigestService(),
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