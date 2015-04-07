package ikube.toolkit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import static ikube.toolkit.STRING.isNumeric;
import static ikube.toolkit.THREAD.submit;
import static ikube.toolkit.THREAD.waitForFutures;

/**
 * This class can be used as a test bean to see if udp is supported on a network. The class will start a client and a server that will then
 * talk to each other over multi casted udp. If there are any other instantiations on the network then they will also be involved in the
 * 'communication' and there will be logging between each of the instances on the machines.
 * <p/>
 * If only on one machine then the client and server only talk to each other, sweet no?
 *
 * @author Michael Couck
 * @version 01.00
 * @since 30-07-2012
 */
public class UdpBroadcaster {

    private static final Logger LOGGER = LoggerFactory.getLogger(UdpBroadcaster.class);

    private static final int PORT = 9876;
    private static final String MCAST_ADDR = "224.0.0.1";
    // private static final String MCAST_ADDR = "FF7E:230::1234";

    static int MESSAGES_SENT = 0;
    static int MESSAGES_RECEIVED = 0;

    private static InetAddress GROUP;

    public static void main(final String[] args) {
        List<Future<Object>> futures = Arrays.asList();
        try {
            long timeToWait = 60;
            if (args != null && args.length > 0 && isNumeric(args[0])) {
                timeToWait = Integer.parseInt(args[0]);
            }
            GROUP = InetAddress.getByName(MCAST_ADDR);
            futures = new UdpBroadcaster().initialize();
            waitForFutures(futures, timeToWait);
        } catch (final Exception e) {
            LOGGER.error("Usage : [group-ip] [port]");
        } finally {
            for (Future<Object> future : futures) {
                future.cancel(true);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<Future<Object>> initialize() {
        THREAD.initialize();

        Future<Object> server = (Future<Object>) server();
        THREAD.sleep(3000);
        Future<Object> client = (Future<Object>) client();

        return Arrays.asList(server, client);
    }

    private Future<?> client() {
        return submit(this.getClass().getSimpleName(), new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                try (MulticastSocket multicastSocket = new MulticastSocket(PORT)) {
                    multicastSocket.joinGroup(GROUP);
                    while (true) {
                        byte[] receiveData = new byte[256];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        multicastSocket.receive(receivePacket);
                        UdpBroadcaster.MESSAGES_RECEIVED++;
                        LOGGER.warn("Client received at : " + new Date() + ", from : " + receivePacket.getAddress());
                    }
                } catch (final Exception e) {
                    LOGGER.error(null, e);
                }
            }
        });
    }

    private Future<?> server() {
        return submit(this.getClass().getSimpleName(), new Runnable() {
            @SuppressWarnings("InfiniteLoopStatement")
            public void run() {
                try (DatagramSocket serverSocket = new DatagramSocket()) {
                    while (true) {
                        byte[] sendData = new byte[256];
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, GROUP, PORT);
                        serverSocket.send(sendPacket);
                        UdpBroadcaster.MESSAGES_SENT++;
                        LOGGER.warn("Server sent at : " + new Date());
                        THREAD.sleep(10000);
                    }
                } catch (final Exception e) {
                    LOGGER.error(null, e);
                }
            }
        });
    }

}
