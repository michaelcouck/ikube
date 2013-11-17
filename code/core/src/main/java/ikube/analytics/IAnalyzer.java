package ikube.analytics;

/**
 * TODO Document me...
 * 
 * @author Michael Couck
 * @since 14.08.13
 * @version 01.00
 * 
 * @param <I> the input type
 * @param <O> the output type
 */
public interface IAnalyzer<I, O> {

	void initialize() throws Exception;

	@SuppressWarnings({ "unchecked" })
	boolean train(final I... input) throws Exception;

	void build() throws Exception;

	O analyze(final I input) throws Exception;

}