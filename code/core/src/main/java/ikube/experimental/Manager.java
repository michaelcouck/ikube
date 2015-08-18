package ikube.experimental;

import ikube.IConstants;
import ikube.cluster.gg.ClusterManagerGridGain;
import ikube.experimental.listener.IEvent;
import ikube.experimental.listener.StartDatabaseProcessingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Document me when implemented.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 09-07-2015
 */
@Component
@EnableAsync
@Configuration
@EnableScheduling
public class Manager {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    @Qualifier("ikube.cluster.gg.ClusterManagerGridGain")
    @SuppressWarnings({"SpringJavaAutowiringInspection", "SpringJavaAutowiredMembersInspection"})
    private ClusterManagerGridGain clusterManager;

    @Autowired
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Context> contexts;

    @SuppressWarnings("ConstantConditions")
    @Scheduled(initialDelay = 10000, fixedRate = 5000)
    public void process() throws Exception {
        // Start the database(s) processing
        for (final Context context : contexts) {
            IEvent<?, ?> event = new StartDatabaseProcessingEvent(context);
            clusterManager.send(IConstants.IKUBE, event);
        }
    }

}