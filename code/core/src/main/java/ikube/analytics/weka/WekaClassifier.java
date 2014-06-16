package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.Timer;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

/**
 * This is a wrapper for the Weka classifiers. It is essentially a holder with some methods for
 * building and training and using the underlying Weka classification(any one, for example {@link weka.classifiers.functions.SMO})
 * algorithm.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 14-08-2013
 */
public class WekaClassifier extends WekaAnalyzer {

	private volatile Classifier classifier;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(final Context context) throws Exception {
		super.init(context);
		instances.setClassIndex(0);
		classifier = (Classifier) context.getAlgorithm();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void build(final Context context) {
		double duration = Timer.execute(new Timer.Timed() {
			@Override
			@SuppressWarnings("unchecked")
			public void execute() {
				try {
					analyzeLock.lock();
					log(instances);
					persist(context, instances);
					logger.info("Building classifier : " + instances.numInstances());

					Instances filteredData = filter(instances, filter);
					filteredData.setRelationName("filtered-instances");
					classifier.buildClassifier(filteredData);

					log(filteredData);
					evaluate(filteredData);
				} catch (final Exception e) {
					instances.delete();
					throw new RuntimeException(e);
				} finally {
					// As soon as we are finished training with the data, we can
					// release the memory of all the training instances
					if (instances.numInstances() >= context.getMaxTraining()) {
						logger.info("Not deleting instances : " + instances.numInstances());
						// instances.delete();
					}
					analyzeLock.unlock();
				}
			}
		});
		logger.info("Built classifier in : " + duration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean train(final Analysis<Object, Object> analysis) throws Exception {
		try {
			analyzeLock.lock();
			Instance instance = instance(analysis.getInput(), instances);
			instance.setClassValue(analysis.getClazz());
			instances.add(instance);
			return Boolean.TRUE;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		} finally {
			analyzeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Analysis<Object, Object> analyze(final Analysis<Object, Object> analysis) throws Exception {
		try {
			if (!analyzeLock.tryLock(10, TimeUnit.MILLISECONDS)) {
				return analysis;
			}
			// Create the instance from the data
			Object input = analysis.getInput();
			Instance instance = instance(input, instances);
			// Classify the instance
			double classification = classOrCluster(instance);
			// Set the output for the client
			String clazz = instances.classAttribute().value((int) classification);
			Object distributionForInstance = distributionForInstance(instance);

			analysis.setClazz(clazz);
			analysis.setOutput(distributionForInstance);

			if (analysis.isAlgorithm()) {
				analysis.setAlgorithmOutput(classifier.toString());
			}
			log(clazz, input, distributionForInstance);

			if (analysis.isCorrelation()) {
				analysis.setCorrelationCoefficients(getCorrelationCoefficients(instances));
			}
			if (analysis.isDistribution()) {
				analysis.setDistributionForInstances(getDistributionForInstances(instances));
			}
			return analysis;
		} catch (final Exception e) {
			Object content = analysis.getInput();
			logger.error("Exception classifying content : " + content, e);
			throw new RuntimeException(e);
		} finally {
			if (analyzeLock.isHeldByCurrentThread()) {
				analyzeLock.unlock();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] classesOrClusters() {
		try {
			analyzeLock.lock();
			Object[] classes = new Object[instances.numClasses()];
			Attribute attribute = instances.classAttribute();
			Enumeration enumeration = attribute.enumerateValues();
			for (int i = 0; enumeration.hasMoreElements(); i++) {
				Object value = enumeration.nextElement();
				classes[i] = value;
			}
			return classes;
		} finally {
			analyzeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	double classOrCluster(final Instance instance) throws Exception {
		try {
			analyzeLock.lock();
			Instance filteredInstance = filter(instance, filter);
			return classifier.classifyInstance(filteredInstance);
		} finally {
			analyzeLock.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	double[] distributionForInstance(final Instance instance) throws Exception {
		try {
			analyzeLock.lock();
			Instance filteredInstance = filter(instance, filter);
			return classifier.distributionForInstance(filteredInstance);
		} finally {
			analyzeLock.unlock();
		}
	}

	private void evaluate(final Instances instances) throws Exception {
		Evaluation evaluation = new Evaluation(instances);
		evaluation.evaluateModel(classifier, instances);
		String evaluationReport = evaluation.toSummaryString();
		logger.debug("Classifier evaluation : " + evaluationReport);
	}

	private void log(final Object clazz, final Object input, final Object output) {
		logger.debug("Class : " + clazz + ", " + input + ", " + output);
	}

	private void log(final Instances instances) throws Exception {
		if (instances != null && instances.numInstances() > 0 && instances.numClasses() > 0 && instances.numAttributes() > 0) {
			int numClasses = instances.numClasses();
			int numAttributes = instances.numAttributes();
			int numInstances = instances.numInstances();
			String expression = //
					"Building classifier, classesOrClusters : " + //
							numClasses + //
							", attributes : " + //
							numAttributes + //
							", instances : " + //
							numInstances + //
							", classifier :  " + //
							classifier.hashCode();
			logger.info(expression);
		}
	}

}