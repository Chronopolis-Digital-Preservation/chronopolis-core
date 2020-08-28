package org.chronopolis.ingest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.test.context.ActiveProfiles;

/**
 * Class to force certain properties to be set so that our tests are done
 * in a consistent manner.
 *
 * Created by shake on 3/26/15.
 */
public class IngestTest {

    private static final String AJP_ENABLED = "ingest.ajp.enabled";
    private static final String FLYWAY_ENABLED = "spring.flyway.enabled";
    private static final String DATASOURCE_INITIALIZE = "spring.datasource.initialize";

    @BeforeClass
    public static void init() {
        System.setProperty(AJP_ENABLED, "false");
        System.setProperty(FLYWAY_ENABLED, "false");
        System.setProperty(DATASOURCE_INITIALIZE, "false");
    }

    @AfterClass
    public static void clearProperties() {
        System.clearProperty(AJP_ENABLED);
        System.clearProperty(FLYWAY_ENABLED);
        System.clearProperty(DATASOURCE_INITIALIZE);
    }

}
