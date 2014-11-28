package ikube.cluster;

import ikube.AbstractTest;
import ikube.IConstants;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.model.Indexable;
import ikube.model.IndexableEmail;
import ikube.model.IndexableFileSystem;
import ikube.toolkit.FILE;
import mockit.Mockit;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neuroph.core.NeuralNetwork;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 21-11-2012
 */
@SuppressWarnings("deprecation")
public class MonitorServiceTest extends AbstractTest {

    private IMonitorService monitorService;

    @Before
    public void before() {
        monitorService = new MonitorService();

        List<Indexable> indexables = new ArrayList<Indexable>(Arrays.asList(new IndexableFileSystem()));
        indexContext.setChildren(indexables);

        Mockit.setUpMocks(ApplicationContextManagerMock.class);
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);

        ApplicationContextManagerMock.setBean(IndexContext.class, indexContext);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ApplicationContextManagerMock.class);
        FILE.deleteFile(new File(indexContext.getIndexDirectoryPath()), 1);
        System.getProperties().remove(IConstants.IKUBE_CONFIGURATION);
    }

    @Test
    public void getFieldNames() {
        String[] fieldNames = monitorService.getFieldNames(IndexableEmail.class);
        assertEquals(
                "[idField, titleField, contentField, mailHost, username, password, " +
                        "port, protocol, secureSocketLayer, name, address, stored, analyzed, " +
                        "vectored, omitNorms, tokenized, boost, maxExceptions, threads, id]",
                Arrays.deepToString(fieldNames));
    }

    @Test
    public void getFieldDescriptions() {
        String[] descriptions = monitorService.getFieldDescriptions(IndexContext.class);
        assertTrue(Arrays.deepToString(descriptions).contains("This is the throttle in mili seconds that will slow " +
                "down the indexing"));
    }

    @Test
    public void getIndexFieldNames() throws Exception {
        IndexSearcher multiSearcher = null;
        try {
            File indexDirectory = createIndexFileSystem(indexContext, "Hello world");

            Directory directory = FSDirectory.open(indexDirectory);
            IndexReader indexReader = new MultiReader(DirectoryReader.open(directory));
            multiSearcher = new IndexSearcher(indexReader);
            Mockito.when(indexContext.getMultiSearcher()).thenReturn(multiSearcher);

            String[] fieldNames = monitorService.getIndexFieldNames(indexContext.getIndexName());
            assertTrue(fieldNames.length > 0);
        } finally {
            if (multiSearcher != null && multiSearcher.getIndexReader() != null) {
                multiSearcher.getIndexReader().close();
            }
        }
    }

    @Test
    public void getSetProperties() throws IOException {
        File propertiesFile = null;
        try {
            File file = FILE.findFileRecursively(new File("."), "spring.properties");
            String contents = FILE.getContents(file, Integer.MAX_VALUE).toString();

            propertiesFile = FILE.getOrCreateFile(IConstants.IKUBE_DIRECTORY + "/properties/spring.properties");
            FILE.setContents(propertiesFile, contents.getBytes());

            Map<String, String> filesAndProperties = monitorService.getProperties();
            String cleanPath = FILE.cleanFilePath(propertiesFile.getAbsolutePath());
            assertTrue(filesAndProperties.containsKey(cleanPath));

            filesAndProperties.clear();
            String propertiesFileContents = "my-property=my-value\nanother-property=another-value";
            filesAndProperties.put(propertiesFile.getAbsolutePath(), propertiesFileContents);
            monitorService.setProperties(filesAndProperties);

            String propertiesFileContentsRead = FILE.getContents(propertiesFile,
                    Integer.MAX_VALUE).toString();
            assertEquals("The properties file should contain the contents in the map : ", propertiesFileContents,
                    propertiesFileContentsRead);
        } finally {
            if (propertiesFile != null) {
                FILE.deleteFile(propertiesFile.getParentFile());
            }
        }
    }

	@Test
	public void subTypesOf() {
		String[] subTypesOfNeuralNetwork = monitorService.subTypesOf(NeuralNetwork.class.getName(), "org.neuroph.nnet");
        assertEquals(18, subTypesOfNeuralNetwork.length);
	}

}