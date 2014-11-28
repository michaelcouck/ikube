package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.toolkit.FILE;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 02-06-2014
 */
public class WekaToolkitTest extends AbstractTest {

    private String filePath;
    private Object[][] matrix;

    @Before
    public void before() {
        filePath = "./instances.arff";
        matrix = new Object[3][];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = new Object[]{1, "2", "3.5", "Michael Couck", "birth date", "1971-05-27", "{nominal}"};
        }
    }

    @After
    public void after() {
        FILE.deleteFile(new File(filePath));
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
    public void matrixToInstances() throws ParseException {
        Instances instances = WekaToolkit.matrixToInstances(matrix, 0);
        assertEquals(matrix.length, instances.numInstances());
    }

    @Test
    public void getAttribute() {
        Attribute attribute = WekaToolkit.getAttribute(0, String.class, "nominal");
        assertEquals(Attribute.NOMINAL, attribute.type());
        attribute = WekaToolkit.getAttribute(0, String.class);
        assertEquals(Attribute.STRING, attribute.type());
        attribute = WekaToolkit.getAttribute(0, Double.class);
        assertEquals(Attribute.NUMERIC, attribute.type());
        attribute = WekaToolkit.getAttribute(0, Date.class);
        assertEquals(Attribute.DATE, attribute.type());
    }

    @Test
    public void getInstance() throws ParseException {
        Instances instances = WekaToolkit.matrixToInstances(matrix, 0);
        Instance instance = WekaToolkit.getInstance(instances, matrix[0]);
        assertEquals(Attribute.NUMERIC, instance.attribute(0).type());
        assertEquals(Attribute.STRING, instance.attribute(3).type());
        assertEquals(Attribute.DATE, instance.attribute(5).type());
        assertEquals(Attribute.NOMINAL, instance.attribute(6).type());
    }

    @Test
    public void csvToInstances() {
        String input = "1,2,3\n\r4,5,6\n\r7,8,9";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        Instances instances = WekaToolkit.csvToInstances(inputStream);
        assertNotNull(instances);
        assertEquals(3, instances.numAttributes());
        assertEquals(3, instances.numInstances());
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            assertEquals(3, instance.numAttributes());
            assertEquals(3, instance.numValues());
        }
    }

    @Test
    public void stringToWordVector() throws Exception {

        // Logic is as follows:
        // Create the instances
        // Create the instance from the instances' attributes
        // Filter the instances and use the filtered instances to build the classifier
        // Filter the instance and use it to perform the analysis in the classifier

        // Note: Do not use the filtered instances to build the instance!!!

        File file = FILE.findFileRecursively(new File("."), "sentiment-smo.arff");
        try (InputStream inputStream = new FileInputStream(file)) {
            Instances instances = WekaToolkit.arffToInstances(inputStream);
            StringToWordVector stringToWordVector = new StringToWordVector(Integer.MAX_VALUE);

            Instance instance = WekaToolkit.getInstance(instances, new Object[]{"to-be-predicted", "Have no idea what to write here"});
            // Note: The instances still needs to be filtered, this sets the input format
            // for the instance to be created. Without this the creation of the instance will throw
            // 'No input instance format defined' exception
            WekaToolkit.filter(instances, stringToWordVector);

            instance = WekaToolkit.filter(instance, stringToWordVector);
            assertEquals(22, instance.numAttributes());
        }
    }

}