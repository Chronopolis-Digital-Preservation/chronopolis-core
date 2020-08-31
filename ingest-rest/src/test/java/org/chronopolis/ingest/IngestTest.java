package org.chronopolis.ingest;

import org.springframework.test.context.ActiveProfiles;

/**
 * Class to force certain properties to be set so that our tests are done
 * in a consistent manner.
 *
 * Created by shake on 3/26/15.
 */
@ActiveProfiles(value="test", resolver=EnvironmentActiveProfileResolver.class)
public abstract class IngestTest {

}
