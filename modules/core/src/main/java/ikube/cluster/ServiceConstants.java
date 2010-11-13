package ikube.cluster;

public class ServiceConstants {

	// the server and client must match up on the following values...
	public static final String MULTICAST_ADDRESS_GROUP = "230.0.0.1";
	public static final int MULTICAST_PORT = 4321;
	public static final int DATAGRAM_LENGTH = 1024;

	// the rest of these values can be changed/tuned as needed...
	public static final int RESPONDER_SOCKET_TIMEOUT = 1000;
	public static final int BROWSER_SOCKET_TIMEOUT = 1000;
	public static final int BROWSER_QUERY_INTERVAL = 1000;

}
