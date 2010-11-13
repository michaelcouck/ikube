package ikube.cluster;

import ikube.BaseTest;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import org.junit.Test;

public class TestServer extends BaseTest {

	public static final String SERVICE_NAME = "discoveryDemo";
	public static final String SERVICE_INSTANCE_NAME = "Demo_Server";

	private ServiceResponder responder;

	@Test
	public void start() {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
		} catch (IOException ioe) {
			System.err.println("Could not bind a server socket to a free port: " + ioe);

		}

		/*
		 * Create a descriptor for the service you are providing.
		 */
		ServiceDescription descriptor = new ServiceDescription();
		descriptor.setAddress(serverSocket.getInetAddress());
		descriptor.setPort(serverSocket.getLocalPort());
		descriptor.setInstanceName(SERVICE_INSTANCE_NAME);
		System.out.println("Service details: " + descriptor.toString());

		/*
		 * In an ideal server code, you should first do your own 'query' at this point to see if the service instance name already exists on
		 * the local network. If it does, you'll need to abort/exit, or retry with a modified service instance name, possibly by using a
		 * suffix such as "(2)" on the end. For this demo, we'll assume we're the only one, and keep going.
		 */

		/*
		 * We're okay so far. Now set up a responder and give it the descriptor we want to publish. Also to attempt graceful handling of
		 * ctrl-C, add a shutdown hook which tries to alert the network we're no longer providing this service (in case someone's watching a
		 * dynamic display of available servers). Finally start the responder (which works in its own thread).
		 */
		responder = new ServiceResponder(SERVICE_NAME);
		responder.setDescriptor(descriptor);
		responder.addShutdownHandler();
		responder.startResponder();

		/*
		 * Now we'll just hang out for a while until you stop the program. Any incoming connections will just have this machine's current
		 * time spit at them, and the connection will be closed.
		 */
		System.out.println("Responder listening. Now taking connections...");
		while (true) {
			try {
				Socket socket = serverSocket.accept();
				System.out.println("Connection from: " + socket.getInetAddress());
				OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
				writer.write(new Date().toString() + "\r\n");
				writer.flush();
				socket.close();
			} catch (IOException ie) {
				System.err.println("Exception: " + ie);
			}
		}

	}
}
