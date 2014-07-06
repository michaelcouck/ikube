package ikube.analytics.weka;

import ikube.model.Analysis;
import ikube.model.Context;
import ikube.toolkit.ThreadUtilities;
import org.reflections.Reflections;
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
public class WekaAlgorithm {

    public static void main(final String[] args) throws Exception {
        ThreadUtilities.initialize();
        Set<Class<? extends Clusterer>> clusterers = new Reflections("weka.clusterers").getSubTypesOf(Clusterer.class);
        Set<Class<? extends Classifier>> classifiers = new Reflections("weka.classifiers").getSubTypesOf(Classifier.class);
        classifiers.remove(UserClassifier.class);
        classifiers.remove(DTNB.class);
        for (final Class<? extends Classifier> classifier : classifiers) {
            new WekaAlgorithm().doClassifiers(classifier);
        }
    }

    @SuppressWarnings("unchecked")
    private void doClassifiers(final Class<? extends Classifier> classifier) throws Exception {
        File regressionFile = findFileRecursively(new File("."), "regression.arff");
        String regressionContent = getContent(regressionFile);

        File classificationFile = findFileRecursively(new File("."), "classification.arff");
        String classificationContent = getContent(classificationFile);

        System.out.println(classifier.getName());

        Set<Class<? extends Filter>> filters = new Reflections("weka.filters").getSubTypesOf(Filter.class);
        filters.add(null);
        for (final Class<? extends Filter> filter : filters) {
            try {
                doClassifier(classifier, "regression", filter, new ByteArrayInputStream(regressionContent.getBytes()));
                System.out.println("        : " + filter);
            } catch (final Exception e) {
                // System.out.println("Error : " + classifier + "-" + filter);
            }
            try {
                doClassifier(classifier, "classification", filter, new ByteArrayInputStream(classificationContent.getBytes()));
                System.out.println("        : " + filter);
            } catch (final Exception e) {
                // System.out.println("Error : " + classifier + "-" + filter);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doClassifier(
            final Class<? extends Classifier> classifier,
            final String name,
            final Class<? extends Filter> filter,
            final InputStream inputStream) throws Exception {
        Context context = new Context<>();
        context.setMaxTraining(1000);
        context.setName(name);
        context.setAlgorithm(classifier.newInstance());
        if (filter != null) {
            context.setFilter(filter.newInstance());
        }

        WekaClassifier wekaClassifier = new WekaClassifier() {

            @Override
            InputStream getInputStream(Context context) throws FileNotFoundException {
                return inputStream;
            }

            void persist(final Context context, final Instances instances) {
                // Do nothing
            }
        };
        wekaClassifier.init(context);
        wekaClassifier.build(context);
        Analysis<Object, Object> analysis = new Analysis<>();
        analysis.setInput("189900,2397,14156,4,1,0");
        wekaClassifier.analyze(analysis);

        // logger.error("Analysis : " + analysis.getClazz() + ", " + analysis.getOutput());
    }

}