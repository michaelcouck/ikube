package ikube.scheduling.schedule;

import ikube.action.IAction;
import ikube.cluster.IMonitorService;
import ikube.model.IndexContext;
import ikube.scheduling.Schedule;
import ikube.toolkit.ThreadUtilities;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Future;

/**
 * This class is the central class for creating indexes.
 * <p/>
 * This class then only looks up the index contexts and executes actions on them. The processing framework
 * registers a listener with the scheduler and responds to the {@link Event#TIMER} type of event. This event schedule can
 * be configured in the configuration, as can most schedules and executors.
 * <p/>
 * Index contexts contain parameters and indexables. Indexables are objects that can be indexed, like files and databases.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public class IndexSchedule extends Schedule {

    private static final Logger LOGGER = Logger.getLogger(IndexSchedule.class);

    @Autowired
    private IMonitorService monitorService;
    @Autowired
    private List<IAction<IndexContext<?>, Boolean>> actions;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void run() {
        Random random = new Random();
        List<IndexContext> indexContexts = new ArrayList<>(monitorService.getIndexContexts().values());
        // We shuffle the contexts so they all get a chance to get processed
        Collections.shuffle(indexContexts);
        for (final IndexContext<?> indexContext : indexContexts) {
            processIndexContext(indexContext, random);
        }
    }

    @SuppressWarnings("rawtypes")
    private void processIndexContext(final IndexContext indexContext, final Random random) {
        // Lets try to shuffel the actions too!
        List<IAction<IndexContext<?>, Boolean>> actions = new ArrayList<>(this.actions);
        Collections.shuffle(actions);
        LOGGER.info("Actions : " + actions.size());
        for (final IAction<IndexContext<?>, Boolean> action : actions) {
            try {
                ThreadUtilities.sleep(random.nextInt(30));
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            // The action will be intercepted by the rule interceptor, and the rules will be evaluated
                            // against the cluster. If they result in a true then the action will be allowed to execute the logic
                            LOGGER.debug("Executing action : " + action);
                            action.execute(indexContext);
                        } catch (final Exception e) {
                            LOGGER.error("Exception executing action : " + action, e);
                        } finally {
                            // We remove ourselves from the schedules in the thread utilities
                            ThreadUtilities.destroy(this.toString());
                        }
                    }
                };
                Future<?> future = ThreadUtilities.submit(runnable.toString(), runnable);
                // We'll wait a few seconds for this action, perhaps it is a fast one
                ThreadUtilities.waitForFuture(future, Math.max(15, random.nextInt(15)));
            } catch (final Exception e) {
                LOGGER.error("Exception executing action : " + action, e);
            }
        }
    }

    public void destroy() {
    }

}