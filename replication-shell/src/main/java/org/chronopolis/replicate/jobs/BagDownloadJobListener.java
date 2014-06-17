package org.chronopolis.replicate.jobs;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by shake on 6/13/14.
 */
public class BagDownloadJobListener extends JobListenerSupport {
    private final Logger log = LoggerFactory.getLogger(BagDownloadJobListener.class);

    private final String name;
    private final Scheduler scheduler;

    public BagDownloadJobListener(final String name, final Scheduler scheduler) {
        this.name = name;
        this.scheduler = scheduler;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext jobExecutionContext,
                               final JobExecutionException e) {

        if (e == null) {
            JobKey key = jobExecutionContext.getJobDetail().getKey();

            try {
                scheduler.triggerJob(new JobKey(key.getName(), "AceRegister"));
            } catch (SchedulerException e1) {
                log.error("Scheduler Exception! ", e1);
            }

        } else {

        }
    }

}
