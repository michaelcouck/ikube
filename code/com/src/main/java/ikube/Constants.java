package ikube;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Field;

/**
 * Constants for Ikube.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2010
 */
public interface Constants {

    /**
     * This class is to be added to {@link com.google.gson.Gson} so that it doesn't complain when the
     * sub class over rides a field in the super class, for example in the Search class
     * that over rides the annotations in the super class Persistable, the Persistable#id
     * field.
     *
     * @author Michael Couck
     * @version 01.00
     * @since 07-06-2014
     */
    class IdExclusionStrategy implements ExclusionStrategy {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean shouldSkipField(final FieldAttributes fieldAttributes) {
            String fieldName = fieldAttributes.getName();
            Class<?> theClass = fieldAttributes.getDeclaringClass();
            return isFieldInSuperclass(theClass, fieldName);
        }

        private boolean isFieldInSuperclass(final Class<?> subclass, final String fieldName) {
            Field field;
            Class<?> superclass = subclass.getSuperclass();
            while (superclass != null) {
                field = getField(superclass, fieldName);
                if (field != null) {
                    return true;
                }
                superclass = superclass.getSuperclass();
            }
            return false;
        }

        private Field getField(final Class<?> theClass, final String fieldName) {
            try {
                return theClass.getDeclaredField(fieldName);
            } catch (final Exception e) {
                return null;
            }
        }

        @Override
        public boolean shouldSkipClass(final Class<?> aClass) {
            return false;
        }

    }

    /**
     * Application name.
     */
    String IKUBE = "ikube";
    /**
     * The name of the log file.
     */
    String IKUBE_LOG = IKUBE + ".log";
    /**
     * The file separator for the system.
     */
    String SEP = "/";
    String META_INF = SEP + "META-INF";
    /**
     * The default logging properties.
     */
    String LOG_4_J_PROPERTIES = META_INF + SEP + "log4j.properties";
    /**
     * System encoding.
     */
    String ENCODING = "UTF-8";

    /**
     * The property for the configuration location.
     */
    String IKUBE_CONFIGURATION = IKUBE + ".configuration";

    String APPLICATION_JSON = "application/json";

    String DELIMITER_CHARACTERS = ";,|:";
    String STRIP_CHARACTERS = "|!,[]{};:/\\.-_";

    String POSITIVE = "positive";
    String NEGATIVE = "negative";

    String SHORT_DATE_FORMAT = "yyyy-MM-dd";
    String ANALYTICS_DATE_FORMAT = "yyyy-MM-dd-HH-mm-ss";

    Gson GSON = new GsonBuilder()
        .addSerializationExclusionStrategy(new IdExclusionStrategy())
        .addDeserializationExclusionStrategy(new IdExclusionStrategy())
        .create();
}