package org.chronopolis.ingest.task;

import org.chronopolis.common.concurrent.TrackingThreadPoolExecutor;
import org.chronopolis.ingest.IngestTest;
import org.chronopolis.ingest.TestApplication;
import org.chronopolis.rest.entities.Bag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Test for the thread pool executor
 *
 * Created by shake on 5/22/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class TokenThreadPoolExecutorTest extends IngestTest {
    private final Logger log = LoggerFactory.getLogger(TokenThreadPoolExecutorTest.class);

    TrackingThreadPoolExecutor<Bag> trackingExecutor;

    Bag b0 = new Bag("test-name-0", "test-depositor");
    Bag b1 = new Bag("test-name-1", "test-depositor");

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        trackingExecutor = new TrackingThreadPoolExecutor<>(4,
                4,
                4,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        MockitoAnnotations.initMocks(this);

        // ensure the ids are not null
        b0.setId(0L);
        b1.setId(1L);
    }

    @Test
    public void testTrackingPoolSubmitBag() throws Exception {
        Runnable r = new SleepRunnable();
        log.info("Submitting initial bag");
        Optional<FutureTask<Bag>> future = trackingExecutor.submitIfAvailable(r, b0);
        Assert.assertTrue(future.isPresent());
        for (int i = 0; i < 10; i++) {
            log.info("Submitting duplicate bag");
            future = trackingExecutor.submitIfAvailable(r, b0);
            Assert.assertFalse(future.isPresent());
        }
    }

    @Test
    public void testTrackingPoolExceptionRunnable() throws Exception {
        Runnable r = new ExceptionRunnable();
        log.info("Submitting exception");
        Optional<FutureTask<Bag>> future = trackingExecutor.submitIfAvailable(r, b1);
        Assert.assertTrue(future.isPresent());
        // give it time to execute
        TimeUnit.SECONDS.sleep(1);
        Assert.assertFalse(trackingExecutor.contains(b1));
    }

    private class ExceptionRunnable implements Runnable {
        @Override
        public void run() {
            log.info("Throwing exception");
            throw new RuntimeException("Test exception");
        }
    }

    private class SleepRunnable implements Runnable {
        @Override
        public void run() {
            try {
                // 2 seconds to have some buffer
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException ignored) {
            }
        }
    }

}