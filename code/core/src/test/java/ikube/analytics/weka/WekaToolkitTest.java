package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.File;

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
        FastVector fastVector = new FastVector(10);
        Attribute attribute = new Attribute("class");
        fastVector.addElement(attribute);
        Instances instances = new Instances("instances", fastVector, 10);
        WekaToolkit.writeToArff(instances, filePath);
        assertTrue(new File(filePath).exists());
    }

    @Test
    public void csvFileToInstances() {
        File file = FileUtilities.findFileRecursively(new File("."), "general.csv");
        String filePath = FileUtilities.cleanFilePath(file.getAbsolutePath());
        Instances instances = WekaToolkit.csvFileToInstances(filePath, 0);
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
        int[] columnsToExclude = {2};
        Instances instances = WekaToolkit.matrixToInstances(matrix, 0, columnsToExclude);
        assertEquals(matrix.length, instances.numInstances());
        assertEquals(matrix[0].length - columnsToExclude.length, instances.numAttributes());
    }

}
