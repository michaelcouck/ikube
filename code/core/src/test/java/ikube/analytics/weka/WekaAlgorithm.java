package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.classifiers.rules.DTNB;
import weka.classifiers.trees.UserClassifier;
import weka.clusterers.Clusterer;
import weka.core.Instances;
import weka.filters.Filter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

import static ikube.toolkit.FileUtilities.findFileRecursively;
import static ikube.toolkit.FileUtilities.getContent;

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
@SuppressWarnings({"unchecked", "FieldCanBeLocal", "UnusedDeclaration"})
public final class WekaAlgorithm {

    public static void main(final String[] args) throws Exception {
        ThreadUtilities.initialize();
        new WekaAlgorithm().doMain();
    }

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private void doMain() {
        Set<Class<? extends Filter>> filters = new Reflections("weka.filters").getSubTypesOf(Filter.class);
        filters.add(null);

        Set<Class<? extends Clusterer>> clusterers = new Reflections("weka.clusterers").getSubTypesOf(Clusterer.class);

        Set<Class<? extends Classifier>> classifiers = new Reflections("weka.classifiers").getSubTypesOf(Classifier.class);
        classifiers.remove(UserClassifier.class);
        classifiers.remove(DTNB.class);

        File regressionFile = findFileRecursively(new File("."), "regression.arff");
        final String regressionContent = getContent(regressionFile);
        File classificationFile = findFileRecursively(new File("."), "classification.arff");
        final String classificationContent = getContent(classificationFile);

        for (final Class<? extends Clusterer> clusterer : clusterers) {
            doClusterer(clusterer, filters, regressionContent, "regression");
            doClusterer(clusterer, filters, classificationContent, "classification");
        }

        for (final Class<? extends Classifier> classifier : classifiers) {
            doClassifier(classifier, filters, regressionContent, "regression");
            doClassifier(classifier, filters, classificationContent, "classification");
        }

    }

    private void doClusterer(Class<? extends Clusterer> clusterer, Set<Class<? extends Filter>> filters, final String data, final String type) {
        WekaAnalyzer wekaClusterer = new WekaClusterer() {
            @Override
            InputStream getInputStream(final Context context) throws FileNotFoundException {
                return new ByteArrayInputStream(data.getBytes());
            }

            void persist(final Context context, final Instances instances) {
                // Do nothing
            }
        };
        doAnalyzer(wekaClusterer, clusterer, filters, type);
    }

    private void doClassifier(Class<? extends Classifier> classifier, Set<Class<? extends Filter>> filters, final String data, final String type) {
        WekaAnalyzer wekaClassifier = new WekaClassifier() {
            @Override
            InputStream getInputStream(final Context context) throws FileNotFoundException {
                return new ByteArrayInputStream(data.getBytes());
            }

            void persist(final Context context, final Instances instances) {
                // Do nothing
            }
        };
        doAnalyzer(wekaClassifier, classifier, filters, type);
    }

    private void doAnalyzer(final WekaAnalyzer analyzer, final Class<?> algorithm, final Set<Class<? extends Filter>> filters, final String type) {
        System.out.println("* " + algorithm.getName());
        for (final Class<? extends Filter> filter : filters) {
            try {
                doAnalyzer(analyzer, algorithm, filter);
                System.out.println("** " + filter + ", " + type);
            } catch (final Exception e) {
                // System.out.println("Error : " + analyzer + "-" + filter);
                // e.printStackTrace();
            }
        }
    }

    private void doAnalyzer(final WekaAnalyzer wekaAnalyzer, final Class<?> algorithm, final Class<? extends Filter> filter)
            throws Exception {
        Context context = new Context<>();
        context.setMaxTraining(1000);
        context.setAlgorithm(algorithm.newInstance());
        if (filter != null) {
            context.setFilter(filter.newInstance());
        }

        wekaAnalyzer.init(context);
        wekaAnalyzer.build(context);

        Analysis<Object, Object> analysis = new Analysis<>();
        analysis.setInput("189900,2397,14156,4,1,0");
        wekaAnalyzer.analyze(analysis);

        // logger.error("Analysis : " + analysis.getClazz() + ", " + analysis.getOutput());
    }

}