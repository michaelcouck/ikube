package ikube.scheduling.schedule;

import ikube.analytics.AnalyzerManager;
import ikube.analytics.IAnalyticsService;
import ikube.scheduling.Schedule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This schedule will build the analyzers. Generally we want the analyzers to be built
 * once the rest of the system is up and running so we can have a stable fully instantiated
 * application context for any resources that we need for the build.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
@SuppressWarnings({"unchecked", "SpringJavaAutowiredMembersInspection", "SpringJavaAutowiringInspection"})
public class AnalyzerSchedule extends Schedule {

    @Autowired
    private AnalyzerManager analyzerManager;
    @Autowired
    private IAnalyticsService analyticsService;

    @Override
    public void run() {
        try {
            logger.info("Starting to build the analyzers : ");
            analyzerManager.buildAnalyzers(analyticsService.getContexts());
        } catch (final Exception e) {
            logger.error("Exception building the analyzers : ", e);
        }
    }
}