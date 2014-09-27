package ikube.analytics.weka;

import org.kohsuke.args4j.Option;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-09-2014
 */
public class WekaForecasterClassifierOption extends ikube.analytics.Option {

    @Option(name = "-fieldsToForecast")
    private String fieldsToForecast;
    @Option(name = "-timeStampField")
    private String timeStampField;
    @Option(name = "-minLag")
    private int minLag;
    @Option(name = "-maxLag")
    private int maxLag;
    @Option(name = "-forecasts")
    private int forecasts;

    public WekaForecasterClassifierOption(final Object[] options) {
        super(options);
    }

    public String getFieldsToForecast() {
        return fieldsToForecast;
    }

    public String getTimeStampField() {
        return timeStampField;
    }

    public int getMinLag() {
        return minLag;
    }

    public int getMaxLag() {
        return maxLag;
    }

    public int getForecasts() {
        return forecasts;
    }
}