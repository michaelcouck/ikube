package ikube.scheduling.schedule;

import ikube.IConstants;
import ikube.scheduling.Schedule;
import ikube.toolkit.THREAD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

/**
 * This schedule will stop the indexing when a certain memory is reached in the Jvm as a licensing feature :)
 *
 * @author Michael Couck
 * @version 01.00
 * @since 28-03-2013
 */
public class MemorySchedule extends Schedule {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemorySchedule.class);

    @Value("${max.memory}")
    private int maxMemory = 2000;

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        logger.warn("Total memory : " + Runtime.getRuntime().totalMemory());
        logger.warn("Free memory : " + Runtime.getRuntime().freeMemory());
        logger.warn("Max memory : " + Runtime.getRuntime().maxMemory());
        Runtime runtime = Runtime.getRuntime();
        if ((runtime.totalMemory() / IConstants.MILLION) > maxMemory) {
            LOGGER.info("Terminating schedules, memory exceeded : " + maxMemory);
            THREAD.destroy();
        }
    }

}