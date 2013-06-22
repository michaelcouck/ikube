package ikube.deploy;

import static junit.framework.Assert.assertTrue;
import ikube.AbstractTest;
import ikube.deploy.action.CommandAction;
import ikube.deploy.action.CopyAction;
import ikube.deploy.model.Server;

import java.util.concurrent.atomic.AtomicInteger;

import mockit.Mock;
import mockit.MockClass;
import mockit.Mockit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployerTest extends AbstractTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DeployerTest.class);

	private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

	@MockClass(realClass = CopyAction.class)
	public static class CopyActionMock {
		@Mock
		public boolean execute(final Server server) {
			LOGGER.info("Copy : " + server.getIp());
			ATOMIC_INTEGER.getAndIncrement();
			return Boolean.TRUE;
		}
	}

	@MockClass(realClass = CommandAction.class)
	public static class CommandActionMock {
		@Mock
		public boolean execute(final Server server) {
			LOGGER.info("Command : " + server.getIp());
			ATOMIC_INTEGER.getAndIncrement();
			return Boolean.TRUE;
		}
	}

	@Before
	public void before() {
		Mockit.setUpMocks(CommandActionMock.class, CopyActionMock.class);
	}

	@After
	public void after() {
		Mockit.tearDownMocks();
	}

	@Test
	public void main() {
		Deployer.main(null);
		assertTrue("Servers and actions : ", ATOMIC_INTEGER.get() >= 3);
	}

}