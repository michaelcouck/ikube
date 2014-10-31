package ikube.analytics;

import java.io.Serializable;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 27-09-2014
 */
public interface IOption extends Serializable {

    <T> T getOption(final String name);

    <T> T getOption(final String name, final Class<T> type);

    <T> T getOption(final Class<?>... type);

}