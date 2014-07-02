package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.reflections.Reflections;
import weka.classifiers.Classifier;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.filters.Filter;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

/**
 * This class is for generally testing the different algorithms in Weka.
 * <p/>
 * The matrix for the algorithms is:
 * <p/>
 * <pre>
 *      Algorithm : filter : data type : options : result(best output on data)
 * </pre>
 *
 * @author Michael Couck
 * @version 01.00
 * @since 02-06-2014
 */
public class WekaAlgorithm {

    public static void main(final String[] args) throws Exception {
        ThreadUtilities.initialize();
        Set<Class<? extends Clusterer>> clusterers = new Reflections("weka.clusterers").getSubTypesOf(Clusterer.class);
        Set<Class<? extends Classifier>> classifiers = new Reflections("weka.classifiers").getSubTypesOf(Classifier.class);
        for (final Class<? extends Classifier> classifier : classifiers) {
            new WekaAlgorithm().doClassifiers(classifier);
        }
    }

    @SuppressWarnings("unchecked")
    private void doClassifiers(final Class<? extends Classifier> classifier) throws Exception {
        System.out.println("Classifier : " + classifier.getName());
        try {
            doClassifier(classifier, "regression", null);
        } catch (Exception e) {
            // logger.error("Error : " + classifier.getName() + ", " + null + " : " + e.getMessage());
            // logger.error("Error : ", e);
        }
        try {
            doClassifier(classifier, "classification", null);
        } catch (Exception e) {
            // logger.error("Error : " + classifier.getName() + ", " + null + " : " + e.getMessage());
            // logger.error("Error : ", e);
        }
        Set<Class<? extends Filter>> filters = new Reflections("weka.filters").getSubTypesOf(Filter.class);
        for (final Class<? extends Filter> filter : filters) {
            try {
                doClassifier(classifier, "regression", filter);
            } catch (Exception e) {
                // logger.error("Error : " + classifier.getName() + ", " + filter + " : " + e.getMessage());
                // logger.error("Error : ", e);
            }
            try {
                doClassifier(classifier, "classification", filter);
            } catch (Exception e) {
                // logger.error("Error : " + classifier.getName() + ", " + filter + " : " + e.getMessage());
                // logger.error("Error : ", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doClassifier(final Class<? extends Classifier> classifier, final String name, final Class<? extends Filter> filter) throws Exception {
        String classifierName = classifier.getName();
        if (classifierName.contains("HNB")
                || classifierName.contains("UserClassifier")
                /*|| classifierName.contains("ThresholdSelector")
                || classifierName.contains("VotedPerceptron")
                || classifierName.contains("ClassificationViaRegression")
                || classifierName.contains("SimpleLogistic")*/
                ) {
            return;
        }
        Context context = new Context<>();
        context.setMaxTraining(1000);
        context.setName(name);
        // context.setFileName(name);
        context.setAlgorithm(classifier.newInstance());
        if (filter != null) {
/*            String filterName = filter.getName();
            if (filterName.contains("RemoveFolds") || filterName.contains("SparseToNonSparse")) {
                return;
            }*/
            context.setFilter(filter.newInstance());
        }

        WekaClassifier wekaClassifier = new WekaClassifier() {

            @Override
            InputStream getInputStream(Context context) throws FileNotFoundException {
                return super.getInputStream(context);
            }

            void persist(final Context context, final Instances instances) {
                // Do nothing
            }
        };
        wekaClassifier.init(context);
        wekaClassifier.build(context);
        Analysis<Object, Object> analysis = new Analysis<>();
        analysis.setInput("189900,2397,14156,4,1,0");

        // wekaClassifier.analyze(analysis);

        System.out.println("      : " + filter);
        // logger.error("Analysis : " + analysis.getClazz() + ", " + analysis.getOutput());
    }

}