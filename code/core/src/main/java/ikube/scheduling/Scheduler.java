package ikube.scheduling;

import ikube.IConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class just schedules threads to execute the run methods.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);

    /**
     * The list of schedules.
     */
    private List<Schedule> schedules = new ArrayList<>();
    /**
     * The scheduler.
     */
    private ScheduledExecutorService service;

    /**
     * Iterates over the schedules scheduling them for execution.
     */
    public void initialize() {
        if (service != null) {
            shutdown();
        }
        service = Executors.newScheduledThreadPool(IConstants.THREAD_POOL_SIZE);
        for (final Schedule schedule : schedules) {
            schedule(schedule);
        }
    }

    public ScheduledFuture<?> schedule(final Schedule schedule) {
        if (schedule.isSingle()) {
            return service.schedule(schedule, schedule.getDelay(), TimeUnit.MILLISECONDS);
        } else {
            return service.scheduleAtFixedRate(schedule, schedule.getDelay(), schedule.getPeriod(), TimeUnit.MILLISECONDS);
        }
    }

    public void shutdown() {
        this.schedules.clear();
        this.service.shutdown();
        List<Runnable> runnables = this.service.shutdownNow();
        LOGGER.info("Shutdown schedules : " + runnables);
    }

    public void setSchedule(final Schedule schedule) {
        this.schedules.add(schedule);
    }

    public void setSchedules(final List<Schedule> schedules) {
        this.schedules.addAll(schedules);
    }

}