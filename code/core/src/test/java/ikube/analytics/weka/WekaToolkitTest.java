package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.Instances;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 02-06-2014
 */
public class WekaToolkitTest extends AbstractTest {

    private String filePath;

    @Before
    public void before() {
        filePath = "./instances.arff";
    }

    @After
    public void after() {
        FileUtilities.deleteFile(new File(filePath));
    }

    @Test
    public void writeToArff() throws Exception {
        ArrayList<Attribute> fastVector = new ArrayList<>();
        Attribute attribute = new Attribute("class");
        fastVector.add(attribute);
        Instances instances = new Instances("instances", fastVector, 10);
        WekaToolkit.writeToArff(instances, filePath);
        assertTrue(new File(filePath).exists());
    }

    @Test
    public void csvFileToInstances() {
        File file = FileUtilities.findFileRecursively(new File("."), "general.csv");
        String filePath = FileUtilities.cleanFilePath(file.getAbsolutePath());
        Instances instances = WekaToolkit.csvFileToInstances(filePath, 0, Double.class);
        assertNotNull(instances);
        assertTrue(instances.numAttributes() > 10);
        assertTrue(instances.numInstances() > 100);
    }

    @Test
    public void matrixToInstances() {
        Object[][] matrix = new Object[3][];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new Object[]{1, "2", "3.5"};
        }
        Instances instances = WekaToolkit.matrixToInstances(matrix, 0, Double.class);
        assertEquals(matrix.length, instances.numInstances());
    }

}
