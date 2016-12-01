package org.chronopolis.intake.duracloud.batch;

import org.chronopolis.bag.core.Bag;
import org.chronopolis.bag.core.BagInfo;
import org.chronopolis.bag.core.BagIt;
import org.chronopolis.bag.core.Digest;
import org.chronopolis.bag.core.OnDiskTagFile;
import org.chronopolis.bag.core.PayloadManifest;
import org.chronopolis.bag.core.Unit;
import org.chronopolis.bag.writer.DirectoryPackager;
import org.chronopolis.bag.writer.SimpleNamingSchema;
import org.chronopolis.bag.writer.SimpleWriter;
import org.chronopolis.bag.writer.TarPackager;
import org.chronopolis.bag.writer.UUIDNamingSchema;
import org.chronopolis.bag.writer.Writer;
import org.chronopolis.intake.duracloud.batch.support.DpnWriter;
import org.chronopolis.intake.duracloud.batch.support.DuracloudMD5;
import org.chronopolis.intake.duracloud.config.IntakeSettings;
import org.chronopolis.intake.duracloud.config.props.Chron;
import org.chronopolis.intake.duracloud.config.props.Duracloud;
import org.chronopolis.intake.duracloud.model.BaggingHistory;
import org.chronopolis.intake.duracloud.remote.BridgeAPI;
import org.chronopolis.intake.duracloud.remote.model.HistorySummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import retrofit2.Call;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tasklet to handle bagging and updating of history to duracloud
 * <p/>
 * Created by shake on 11/12/15.
 */
public class BaggingTasklet implements Tasklet {

    private final Logger log = LoggerFactory.getLogger(BaggingTasklet.class);

    public static final String SNAPSHOT_CONTENT_PROPERTIES = "content-properties.json";
    public static final String SNAPSHOT_COLLECTION_PROPERTIES = ".collection-snapshot.properties";
    public static final String SNAPSHOT_MD5 = "manifest-md5.txt";
    public static final String SNAPSHOT_SHA = "manifest-sha256.txt";

    private final char DATA_BAG = 'D';
    private final String PARAM_PAGE_SIZE = "page_size";
    private final String PROTOCOL = "rsync";
    private final String ALGORITHM = "sha256";

    private String snapshotId;
    private String collectionName;
    private String depositor;
    private IntakeSettings settings;

    private BridgeAPI bridge;

    public BaggingTasklet(String snapshotId,
                          String collectionName,
                          String depositor,
                          IntakeSettings settings,
                          BridgeAPI bridge) {
        this.snapshotId = snapshotId;
        this.collectionName = collectionName;
        this.depositor = depositor;
        this.settings = settings;
        this.bridge = bridge;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        Chron opolis = settings.getChron();
        Duracloud dc = settings.getDuracloud();
        BaggingHistory history = new BaggingHistory(snapshotId, false);

        Path duraBase = Paths.get(dc.getSnapshots());
        Path out = Paths.get(opolis.getBags(), depositor);
        Path snapshotBase = duraBase.resolve(snapshotId);
        String manifestName = dc.getManifest();

        PayloadManifest manifest = PayloadManifest.loadFromStream(
                Files.newInputStream(snapshotBase.resolve(manifestName)),
                snapshotBase);

        // TODO: fill out with what...?
        // TODO: EXTERNAL-IDENTIFIER: snapshot.description
        BagInfo info = new BagInfo()
                .includeMissingTags(true)
                .withInfo(BagInfo.Tag.INFO_SOURCE_ORGANIZATION, depositor);

        Writer writer = settings.pushDPN() ? buildDpnWriter(out) : buildWriter(out);
        writer.withBagInfo(info)
                .withBagIt(new BagIt())
                .withDigest(Digest.SHA_256)
                .withPayloadManifest(manifest)
                .withTagFile(new OnDiskTagFile(snapshotBase.resolve(SNAPSHOT_COLLECTION_PROPERTIES)))
                .withTagFile(new OnDiskTagFile(snapshotBase.resolve(SNAPSHOT_CONTENT_PROPERTIES)))
                .withTagFile(new DuracloudMD5(snapshotBase.resolve(SNAPSHOT_MD5)));

        List<Bag> bags = writer.write();
        boolean valid = true;

        for (Bag bag : bags) {
            log.info("Bag {} is valid? {}; receipt={}", new Object[]{bag.getName(), bag.isValid(), bag.getReceipt()});
            history.addBaggingData(bag.getName(), bag.getReceipt());
            valid = valid && bag.isValid();
        }

        // Save the bag to duracloud
        if (valid) {
            Call<HistorySummary> historyCall = bridge.postHistory(snapshotId, history);
            historyCall.execute();
        } else {
            throw new RuntimeException("Error bagging snapshot " + snapshotId);
        }

        return RepeatStatus.FINISHED;
    }

    private Writer buildWriter(Path out) {
        return new SimpleWriter()
                .withPackager(new DirectoryPackager(out))
                .withNamingSchema(new SimpleNamingSchema(snapshotId));
    }

    private Writer buildDpnWriter(Path out) {
        return new DpnWriter()
                .withDepositor(depositor)
                .withMaxSize(245, Unit.GIGABYTE)
                .withPackager(new TarPackager(out))
                .withNamingSchema(new UUIDNamingSchema());
    }

}
