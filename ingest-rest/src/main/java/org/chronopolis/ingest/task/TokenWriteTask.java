package org.chronopolis.ingest.task;

import com.querydsl.jpa.JPAExpressions;

import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.common.storage.TokenStagingProperties;
import org.chronopolis.ingest.repository.dao.TokenDao;
import org.chronopolis.ingest.tokens.TokenStoreWriter;
import org.chronopolis.rest.entities.Bag;
import org.chronopolis.rest.entities.QAceToken;
import org.chronopolis.rest.entities.QBag;
import org.chronopolis.rest.entities.storage.QStagingStorage;
import org.chronopolis.rest.entities.storage.QStorageRegion;
import org.chronopolis.rest.entities.storage.StorageRegion;
import org.chronopolis.rest.models.enums.BagStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Task to spawn new TokenWriter threads or whatever
 *
 * @author shake
 */
@Component
@EnableScheduling
public class TokenWriteTask {
    private final Logger log = LoggerFactory.getLogger(TokenWriteTask.class);

    private final TokenDao dao;
    private final TokenStagingProperties properties;
    private final TrackingThreadPoolExecutor<Bag> tokenExecutor;

    @Autowired
    public TokenWriteTask(TokenStagingProperties properties,
                          TrackingThreadPoolExecutor<Bag> tokenExecutor,
                          TokenDao dao) {
        this.properties = properties;
        this.tokenExecutor = tokenExecutor;
        this.dao = dao;
    }

    @Scheduled(cron = "${ingest.cron.tokens: 0 */10 * * * *}")
    public void searchForTokenizedBags() {
        StorageRegion region = dao.findOne(QStorageRegion.storageRegion,
                QStorageRegion.storageRegion.id.eq(properties.getPosix().getId()));

        if (region == null) {
            log.error("Unable to write tokens to staging area! Storage region (id={}) does not exist.",
                    properties.getPosix().getId());
            return;
        }

        getBagsWithAllTokens().forEach(bag -> {
            log.debug("Writing tokens to storage region (id={}) for Bag {} (id={}).",
                    region.getId(), bag.getName(), bag.getId());

            TokenStoreWriter writer = new TokenStoreWriter(bag, region, properties, dao);
            tokenExecutor.submitIfAvailable(writer, bag);
        });
    }

    /**
     * Retrieve a List of INITIALIZED Bags which have the same amount of ACE Tokens registered
     * as total files. Also want to make sure at least one staging option is active.
     * <p>
     * The query is equivalent to:
     * SELECT * FROM bag b
     * WHERE status = 'INITIALIZED' AND
     * total_files = (SELECT count(id) FROM ace_token WHERE bag_id = b.id);
     *
     * @return the List of Bags matching the query
     */
    private List<Bag> getBagsWithAllTokens() {
        QBag bag = QBag.bag;
        QAceToken token = QAceToken.aceToken;
        return dao.getJPAQueryFactory().selectFrom(bag)
                .innerJoin(bag.storage, QStagingStorage.stagingStorage)
                .fetchJoin()
                .where(bag.status.eq(BagStatus.INITIALIZED),
                        bag.totalFiles.eq(
                                JPAExpressions.select(token.id.count())
                                        .from(token)
                                        .where(token.bag.id.eq(bag.id))))
                .fetch();
    }

}
