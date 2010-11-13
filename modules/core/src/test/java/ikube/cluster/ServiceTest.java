package ikube.cluster;

import ikube.ATest;

import org.junit.Test;

public class ServiceTest extends ATest {

	@Test
	public void publishSubscribe() throws Exception {
		new ServicePublisher();
		new ServiceSubscriber();
		Thread.sleep(10000);
	}

}
