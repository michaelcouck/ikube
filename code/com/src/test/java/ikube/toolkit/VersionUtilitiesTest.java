package ikube.toolkit;

import ikube.AbstractTest;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-07-2013
 */
public class VersionUtilitiesTest extends AbstractTest {

    @Before
    public void before() {
        Mockit.setUpMocks(ClassPathResourceMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(ClassPathResource.class);
    }

    @Test
    public void version() {
        String version = VersionUtilities.version();
        Assert.assertNotNull(version);
    }

    @Test
    public void timestamp() {
        String timestamp = VersionUtilities.timestamp();
        Assert.assertNotNull(timestamp);
    }

    @MockClass(realClass = ClassPathResource.class)
    public static class ClassPathResourceMock {

        @Mock
        @SuppressWarnings("UnusedDeclaration")
        public void $init(final String path, final ClassLoader classLoader) {
        }

        @Mock
        public InputStream getInputStream() throws IOException {
            File file = FileUtilities.findFileRecursively(new File("."), 1, "pom.properties");
            return new ByteArrayInputStream(FileUtilities.getContent(file).getBytes());
        }
    }

}
