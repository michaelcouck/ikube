package ikube.example;

import java.util.List;

/**
 * Contains any common code that the sub classes do not have duplicate code.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 05-10-2014
 */
public abstract class Base {

    /**
     * Ths class to hold the information for the analyzer to be able to create it.
     */
    public static class Context {
        /* The name of your specific analyzer */
        String name;
        /* The class of analyzer to use, perhaps WekaForecastClassifier, fully qualified name */
        String analyzer;
        /* The training data, in this case a couple of years data, closing price of the stock,
        and some other features, please have a look at the forecast.csv for exactly the contents */
        String[] trainingDatas;
    }

    /**
     * This class just holds the name of the analyzer that is just created, and the input
     * options for the forecaster.
     */
    public static class Analysis {
        /* The analyzer name, forecast-classifier */
        String context;
        /* The input options, either string or array,
         * '-fieldsToForecast,6,-timeStampField,0,-minLag,1,-maxLag,1,-forecasts,5' */
        String input;
        @SuppressWarnings("UnusedDeclaration")
        Object output;
    }

    public static class Coordinate {
        double latitude;
        double longitude;

        Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static class Search {
        String indexName;
        List<String> searchStrings;
        List<String> typeFields;
        List<String> searchFields;
        List<String> occurrenceFields;
        Coordinate coordinate;

        int distance;
        int firstResult;
        int maxResults;
        boolean fragment;
        boolean distributed;
    }

}