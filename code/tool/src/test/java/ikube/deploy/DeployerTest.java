package ikube.deploy;

import ikube.AbstractTest;
import ikube.deploy.action.CmdAction;
import ikube.deploy.action.CopyAction;
import ikube.deploy.model.Server;
import ikube.toolkit.ThreadUtilities;
import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public class DeployerTest extends AbstractTest {

    private static AtomicInteger ATOMIC_INTEGER;

    @MockClass(realClass = CopyAction.class)
    public static class CopyActionMock {
        @Mock
        public boolean execute(final Server server) {
            ATOMIC_INTEGER.getAndIncrement();
            return Boolean.TRUE;
        }
    }

    @MockClass(realClass = CmdAction.class)
    public static class CmdActionMock {
        @Mock
        public boolean execute(final Server server) {
            ATOMIC_INTEGER.getAndIncrement();
            return Boolean.TRUE;
        }
    }

    @Before
    public void before() {
        ThreadUtilities.initialize();
        ATOMIC_INTEGER = new AtomicInteger();
        System.setProperty("username", "username");
        System.setProperty("password", "password");
        System.setProperty("production-username", "username");
        System.setProperty("production-password", "password");
        Mockit.setUpMocks(CmdActionMock.class, CopyActionMock.class);
    }

    @After
    public void after() {
        Mockit.tearDownMocks(CmdActionMock.class, CopyActionMock.class);
    }

    @Test
    public void main() {
        Deployer.main(null);
        assertEquals("Servers and actions : ", 16, ATOMIC_INTEGER.get());
    }

    @Test
    public void mainSome() {
        System.setProperty(Deployer.DEPLOY_TO_IPS, "192.168.1.20");
        Deployer.main(null);
        assertEquals("Servers and actions : ", 4, ATOMIC_INTEGER.get());
    }

    @Test
    public void mainNone() {
        System.setProperty(Deployer.DEPLOY_TO_IPS, "192.168.1.30");
        Deployer.main(null);
        assertEquals("Servers and actions : ", 0, ATOMIC_INTEGER.get());
    }

}