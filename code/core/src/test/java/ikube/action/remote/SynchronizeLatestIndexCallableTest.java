package ikube.action.remote;

import ikube.AbstractTest;
import ikube.mock.ApplicationContextManagerMock;
import ikube.model.IndexContext;
import ikube.toolkit.ApplicationContextManager;
import ikube.toolkit.FileUtilities;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 05-04-2014
 */
public class SynchronizeLatestIndexCallableTest extends AbstractTest {

	@Before
	public void before() {
		Mockit.setUpMocks(ApplicationContextManagerMock.class);
		ApplicationContextManagerMock.setBean(IndexContext.class, indexContext);
	}

    @After
    public void after() {
		Mockit.tearDownMocks(ApplicationContextManager.class);
        FileUtilities.deleteFile(new File(indexContext.getIndexDirectoryPath()));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void call() throws Exception {
        String[] strings = {"create an", "index with", "something in it"};
        File indexDirectory = createIndexFileSystem(indexContext, strings);
        SynchronizeLatestIndexCallable callable = new SynchronizeLatestIndexCallable(indexContext);
        List<String> indexFiles = new ArrayList<>(Arrays.asList(callable.call()));
        File[] files = indexDirectory.listFiles();
        logger.info("Files : " + files.length);
        logger.info("Index files : " + indexFiles.size());
        for (final File file : files) {
            indexFiles.remove(file.getAbsolutePath());
        }
        assertEquals("There should be no more files in the array : ", 0, indexFiles.size());
    }

}