package ikube.analytics.weka;

import ikube.model.Context;
import ikube.toolkit.FileUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.clusterers.Clusterer;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

import java.io.File;

/**
 * This class contains general methods for manipulating the Weka data, and for writing
 * the models to the file system.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 10-04-2013
 */
@SuppressWarnings("ALL")
public final class WekaToolkit {

    private static final Logger LOGGER = LoggerFactory.getLogger(WekaToolkit.class);

    /**
     * Writes the instanes to a file that can be loaded again and used to train algorithms.
     *
     * @param instances the instances data to write to the file
     * @param filePath  the absolute path to the output file
     */
    public static void writeToArff(final Instances instances, final String filePath) {
        try {
            ArffSaver arffSaverInstance = new ArffSaver();
            arffSaverInstance.setInstances(instances);
            File file = FileUtilities.getOrCreateFile(filePath);
            arffSaverInstance.setFile(file);
            arffSaverInstance.writeBatch();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method converts and instances object to a sparse instance, which will then
     * be more memory efficient.
     *
     * @param instances the instances to convert to a sparse instances model
     * @return the sparse instances, more memory efficient
     */
    public static Instances nonSparseToSparse(final Instances instances) {
        try {
            NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse();
            nonSparseToSparseInstance.setInputFormat(instances);
            return Filter.useFilter(instances, nonSparseToSparseInstance);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void printClusterInstances(final Context context) throws Exception {
        Object[] clusterers = context.getAlgorithms();
        Object[] instanceses = context.getModels();
        for (int i = 0; i < clusterers.length; i++) {
            Clusterer clusterer = (Clusterer) clusterers[i];
            Instances instances = (Instances) instanceses[i];
            LOGGER.warn("Num clusters : " + clusterer.numberOfClusters());
            for (int j = 0; j < instances.numAttributes(); j++) {
                Attribute attribute = instances.attribute(j);
                LOGGER.warn("Attribute : " + attribute.name() + ", " + attribute.type());
                for (int k = 0; k < attribute.numValues(); k++) {
                    LOGGER.warn("          : " + attribute.value(k));
                }
            }
        }
    }

    private WekaToolkit() {
    }

}
