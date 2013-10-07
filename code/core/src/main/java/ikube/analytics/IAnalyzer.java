package ikube.analytics;

import java.io.File;
import java.io.IOException;

import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * TODO Document me...
 * 
 * @author Michael Couck
 * @since 14.08.13
 * @version 01.00
 * 
 * @param <I> the input type
 * @param <O> the output type
 * @param <TI> the training input type
 * @param <TR> the output training type
 */
public interface IAnalyzer<I, O, TrainI, TrainO> {

	/**
	 * TODO Document me...
	 */
	void build() throws Exception;

	/**
	 * TODO Document me...
	 */
	void initialize() throws Exception;

	/**
	 * TODO Document me...
	 * 
	 * @param input
	 * @return
	 */
	O analyze(final I input) throws Exception;

	/**
	 * TODO Document me...
	 * 
	 * @param clazz
	 * @param trainingInput
	 * @return
	 */
	TrainO train(final String clazz, final TrainI trainingInput) throws Exception;

	/**
	 * This method will load the model data from the file(s) specified.
	 * 
	 * @param filePattern the pattern of the files that are to be loaded, typically there will only be on of course.
	 */
	void file(final String filePattern);

	/**
	 * TODO Document me...
	 * 
	 * @author michael
	 * @since 14.08.13
	 * @version 01.00
	 */
	public static class IOUtils {

		/**
		 * TODO Document me...
		 * 
		 * @param instances
		 * @param fileName
		 * @throws IOException
		 */
		public static void writeInstancesToArffFile(final Instances instances, final String fileName) throws IOException {
			ArffSaver saver = new ArffSaver();
			saver.setInstances(instances);
			saver.setFile(new File("./data/", fileName));
			saver.writeBatch();
		}
	}

}