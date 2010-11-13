package ikube.cluster;

import ikube.ATest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Vector;

import org.junit.Test;

public class TestClient extends ATest implements ServiceBrowserListener {

	public static final String SERVICE_NAME = "discoveryDemo";

	@Test
	public void main() {
		new TestClient();
	}

	ServiceBrowser browser;
	Vector<ServiceDescription> descriptors;

	public TestClient() {
		descriptors = new Vector<ServiceDescription>();
		browser = new ServiceBrowser();
		browser.addServiceBrowserListener(this);
		browser.setServiceName(SERVICE_NAME);
		while (true) {
			browser.startListener();
			browser.startLookup();
			logger.info("Browser started. Will search for 2 secs.");
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ie) {
				// ignore
			}
			browser.stopLookup();
			browser.stopListener();
			if (descriptors.size() > 0) {
				logger.info("\n---DEMO SERVERS---");
				for (ServiceDescription descriptor : descriptors) {
					logger.info(descriptor.toString());
				}
				logger.info("\n---FIRST SERVER'S TIME IS---");
				ServiceDescription descriptor = descriptors.get(0);
				try {
					Socket socket = new Socket(descriptor.getAddress(), descriptor.getPort());
					InputStreamReader reader = new InputStreamReader(socket.getInputStream());
					BufferedReader bufferedReader = new BufferedReader(reader);
					String line = bufferedReader.readLine();
					System.out.println(line);
					socket.close();
				} catch (IOException ie) {
					logger.info("Exception: " + ie);
				}
			} else {
				logger.info("\n---NO DEMO SERVERS FOUND---");
			}
		}
		// logger.info("\nThat's all folks.");
	}

	public void serviceReply(ServiceDescription descriptor) {
		int pos = descriptors.indexOf(descriptor);
		if (pos > -1) {
			descriptors.removeElementAt(pos);
		}
		descriptors.add(descriptor);
	}

}
