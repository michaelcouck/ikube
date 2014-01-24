package ikube.analytics.weka;

import ikube.AbstractTest;
import ikube.toolkit.FileUtilities;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.io.File;

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
        Assert.assertTrue(new File(filePath).exists());
    }

    @Test
    public void nonSparseToSparse() throws Exception {
    }
}
