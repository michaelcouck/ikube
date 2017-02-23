package ikube.toolkit;

import ikube.AbstractTest;
import mockit.Mock;
import mockit.MockUp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 08-07-2013
 */
public class VERSIONTest extends AbstractTest {

    private MockUp<ClassPathResource> classPathResourceMockUp;

    @Before
    public void before() {
        classPathResourceMockUp = new MockUp<ClassPathResource>() {
            @Mock
            @SuppressWarnings("UnusedDeclaration")
            public void $init(final String path, final ClassLoader classLoader) {
            }

            @Mock
            public InputStream getInputStream() throws IOException {
                String pomProperties = "#Generated by Maven\n\r" +
                        "#Sat Apr 04 14:10:30 CEST 2015\n\r" +
                        "version=5.3.0\n\r" +
                        "groupId=ikube\n\r" +
                        "artifactId=ikube-core";
                return new ByteArrayInputStream(pomProperties.getBytes());
            }
        };
    }

    @After
    public void after() {
        classPathResourceMockUp.tearDown();
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