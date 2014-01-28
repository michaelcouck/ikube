package ikube.analytics;

import ikube.model.Context;

/**
 * TODO Document me...
 *
 * @param <I> the input type
 * @param <O> the output type
 * @author Michael Couck
 * @version 01.00
 * @since 14.08.13
 */
public interface IAnalyzer<I, O> {

    void init(final Context context) throws Exception;

    boolean train(final I input) throws Exception;

    void build(final Context context) throws Exception;

    O analyze(final I input) throws Exception;

    void destroy(final Context context) throws Exception;

}