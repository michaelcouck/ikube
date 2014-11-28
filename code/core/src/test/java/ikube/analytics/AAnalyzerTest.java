package ikube.analytics;

import ikube.AbstractTest;
import ikube.model.Context;
import ikube.toolkit.FILE;
import org.junit.Before;
import org.junit.Test;
import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 10-09-2014
 */
public class AAnalyzerTest extends AbstractTest {

    private Context context;
    private AAnalyzer analyzer;

    @Before
    public void before() {
        String algorithm = SMO.class.getName();
        String filter = StringToWordVector.class.getName();
        String[] options = new String[]{"-D", "-V", "100"};
        int maxTraining = 10000;

        analyzer = new AAnalyzer() {
            @Override
            public void init(final Context context) throws Exception {
            }

            @Override
            public boolean train(final Context context, final Object input) throws Exception {
                return false;
            }

            @Override
            public void build(final Context context) throws Exception {
            }

            @Override
            public Object analyze(final Context context, final Object input) throws Exception {
                return null;
            }

            @Override
            public int sizeForClassOrCluster(final Context context, final Object clazz) throws Exception {
                return 0;
            }

            @Override
            public void destroy(final Context context) throws Exception {

            }
        };

        context = new Context();
        context.setName("classification");
        context.setAnalyzer(analyzer);

        context.setAlgorithms(algorithm, algorithm, algorithm);
        context.setFilters(filter, filter, filter);
        context.setOptions(options, options, options);

        context.setFileNames("sentiment-smo-one.arff", "sentiment-smo-two.arff", "sentiment-smo-three.arff");
        context.setMaxTrainings(maxTraining, maxTraining, maxTraining);
    }

    @Test
    public void getDataFiles() throws Exception {
        analyzer.init(context);
        analyzer.build(context);
        File[] dataFiles = analyzer.getDataFiles(context);
        logger.error("Data files : " + Arrays.toString(dataFiles));
        assertEquals(3, dataFiles.length);
        for (final File dataFile : dataFiles) {
            assertTrue(dataFile.exists());
        }

        context.setFileNames("sentiment-smo-extra.arff");
        dataFiles = analyzer.getDataFiles(context);
        assertEquals(1, dataFiles.length);
        for (final File dataFile : dataFiles) {
            try {
                assertTrue(dataFile.exists());
            } finally {
                FILE.deleteFile(dataFile);
            }
        }
    }

}
