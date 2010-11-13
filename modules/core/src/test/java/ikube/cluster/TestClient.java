package ikube.cluster;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Vector;

public class TestClient implements ServiceBrowserListener {

	public static final String SERVICE_NAME = "discoveryDemo";

	public static void main(String[] args) {
		new TestClient();
	}

	ServiceBrowser browser;
	Vector<ServiceDescription> descriptors;

	TestClient() {
		descriptors = new Vector<ServiceDescription>();
		browser = new ServiceBrowser();
		browser.addServiceBrowserListener(this);
		browser.setServiceName(SERVICE_NAME);
		browser.startListener();
		browser.startLookup();
		System.out.println("Browser started. Will search for 2 secs.");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			// ignore
		}
		browser.stopLookup();
		browser.stopListener();

		if (descriptors.size() > 0) {
			System.out.println("\n---DEMO SERVERS---");
			for (ServiceDescription descriptor : descriptors) {
				System.out.println(descriptor.toString());
			}

			System.out.println("\n---FIRST SERVER'S TIME IS---");
			ServiceDescription descriptor = descriptors.get(0);
			try {
				Socket socket = new Socket(descriptor.getAddress(), descriptor.getPort());
				InputStreamReader reader = new InputStreamReader(socket.getInputStream());
				BufferedReader bufferedReader = new BufferedReader(reader);
				String line = bufferedReader.readLine();
				System.out.println(line);
				socket.close();
			} catch (IOException ie) {
				System.err.println("Exception: " + ie);
				System.exit(1);
			}
		} else {
			System.out.println("\n---NO DEMO SERVERS FOUND---");
		}

		System.out.println("\nThat's all folks.");
		System.exit(0);
	}

	public void serviceReply(ServiceDescription descriptor) {
		int pos = descriptors.indexOf(descriptor);
		if (pos > -1) {
			descriptors.removeElementAt(pos);
		}
		descriptors.add(descriptor);
	}

}
