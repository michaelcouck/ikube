package ikube.scheduling.schedule;

import ikube.analytics.AnalyzerManager;
import ikube.analytics.IAnalyticsService;
import ikube.scheduling.Schedule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This schedule will build the analyzers.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 11-07-2014
 */
@SuppressWarnings({"unchecked", "SpringJavaAutowiredMembersInspection"})
public class AnalyzerSchedule extends Schedule {

    @Autowired
    private AnalyzerManager analyzerManager;
    @Autowired
    private IAnalyticsService analyticsService;

    @Override
    public void run() {
        try {
            analyzerManager.buildAnalyzers(analyticsService.getContexts());
        } catch (final Exception e) {
            logger.error("Exception building the analyzers : ", e);
        }
    }
}