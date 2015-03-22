package ikube.toolkit;

import ikube.AbstractTest;
import mockit.Mock;
import mockit.MockClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static mockit.Mockit.setUpMocks;
import static mockit.Mockit.tearDownMocks;
import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-07-2013
 */
public class VERSIONTest extends AbstractTest {

    @MockClass(realClass = ClassPathResource.class)
    public static class ClassPathResourceMock {

        @Mock
        @SuppressWarnings("UnusedDeclaration")
        public void $init(final String path, final ClassLoader classLoader) {
        }

        @Mock
        public InputStream getInputStream() throws IOException {
            File file = FILE.findFileRecursively(new File("."), 1, "pom.properties");
            return new ByteArrayInputStream(FILE.getContent(file).getBytes());
        }
    }

    @Before
    public void before() {
        setUpMocks(ClassPathResourceMock.class);
    }

    @After
    public void after() {
        tearDownMocks(ClassPathResource.class);
    }

    @Test
    public void version() {
        String version = VERSION.version();
        assertNotNull(version);
    }

    @Test
    public void timestamp() {
        String timestamp = VERSION.timestamp();
        assertNotNull(timestamp);
    }

}