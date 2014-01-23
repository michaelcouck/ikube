package ikube.analytics;

/**
 * Created by michael on 1/23/14.
 */
public abstract class Analyzer<I, O> implements IAnalyzer<I, O> {

    public static IAnalyzer instance(final String clazz) throws Exception {
        return (IAnalyzer) Class.forName(clazz).newInstance();
    }

}
