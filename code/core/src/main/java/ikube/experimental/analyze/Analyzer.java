package ikube.experimental.analyze;

import ikube.experimental.listener.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class will call and execute analyzers on the input data.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 24-08-2015
 */
@Component
public class Analyzer implements IConsumer<AnalysisEvent>, IProducer<AnalysisEvent> {

    @SuppressWarnings("UnusedDeclaration")
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void notify(final AnalysisEvent analysisEvent) {
    }

    @Override
    public void fire(final AnalysisEvent analysisEvent) {
    }

}