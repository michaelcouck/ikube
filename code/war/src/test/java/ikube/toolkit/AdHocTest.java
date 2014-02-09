package ikube.toolkit;

import ikube.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class AdHocTest extends BaseTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws IOException {
    }

    @Test
    public void exception() {
        try {
            reThrowException();
        } catch (final Exception e) {
            logger.error("", e);
        }
    }

    private void reThrowException() throws Exception {
        try {
            throwException();
        } catch (final Exception e) {
            throw new RuntimeException("Error...", e);
        }
    }

    private void throwException() throws Exception {
        throw new NullPointerException("Bla, bla, bla...");
    }

}