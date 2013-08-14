package ikube.analytics;

/**
 * 
 * @author u365981
 * 
 * @param <I> the input type
 * @param <O> the output type
 * @param <TI> the training input type
 * @param <TR> the output training type
 */
public interface IClassifier<I, O, TI, TR> {

	O classify(final I input);

	TR train(final TI trainingInput);

}