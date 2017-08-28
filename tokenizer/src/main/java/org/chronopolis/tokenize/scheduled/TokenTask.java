package org.chronopolis.tokenize.scheduled;

import com.google.common.collect.ImmutableMap;
import org.chronopolis.common.ace.AceConfiguration;
import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.common.storage.BagStagingProperties;
import org.chronopolis.common.util.Filter;
import org.chronopolis.rest.api.IngestAPI;
import org.chronopolis.rest.api.TokenAPI;
import org.chronopolis.rest.models.Bag;
import org.chronopolis.rest.models.BagStatus;
import org.chronopolis.tokenize.BagProcessor;
import org.chronopolis.tokenize.batch.ChronopolisTokenRequestBatch;
import org.chronopolis.tokenize.filter.HttpFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.PageImpl;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

/**
 * Basic task to submit bags for tokenization
 * <p>
 * Created by shake on 2/6/2015.
 */
@Component
@EnableScheduling
@EnableConfigurationProperties(AceConfiguration.class)
public class TokenTask {
    private final Logger log = LoggerFactory.getLogger(TokenTask.class);

    private final TokenAPI tokens;
    private final IngestAPI ingest;
    private final BagStagingProperties properties;
    private final ChronopolisTokenRequestBatch batch;
    private final TrackingThreadPoolExecutor<Bag> executor;

    @Autowired
    public TokenTask(TokenAPI tokens,
                     IngestAPI ingest,
                     BagStagingProperties properties,
                     ChronopolisTokenRequestBatch batch,
                     TrackingThreadPoolExecutor<Bag> executor) {
        this.tokens = tokens;
        this.ingest = ingest;
        this.properties = properties;
        this.batch = batch;
        this.executor = executor;
    }

    @Scheduled(cron = "${ingest.cron.tokens:0 */30 * * * *}")
    public void tokenize() {
        log.info("Searching for bags to tokenize");

        // Query ingest API
        // Maybe getMyBags? Can work this out later
        Call<PageImpl<Bag>> bags = ingest.getBags(
                ImmutableMap.of("status", BagStatus.DEPOSITED,
                        "region_id", properties.getPosix().getId()));
        try {
            Response<PageImpl<Bag>> response = bags.execute();
            if (response.isSuccessful()) {
                for (Bag bag : response.body()) {
                    Filter<String> filter = new HttpFilter(bag.getId(), tokens);
                    executor.submitIfAvailable(new BagProcessor(bag, filter, properties, batch), bag);
                }
            }
        } catch (IOException e) {
            log.error("Error communicating with the ingest server", e);
        }

    }

}
