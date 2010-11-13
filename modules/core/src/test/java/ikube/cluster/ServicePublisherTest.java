package ikube.cluster;

import ikube.ATest;

import org.junit.Test;

public class ServicePublisherTest extends ATest {

	@Test
	public void start() throws Exception {
		ServicePublisher publisher = new ServicePublisher();

		publisher.receive();
		publisher.publish();

		Thread.sleep(10000);
	}

}
