package ikube.cluster.udp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpClient extends Base implements Runnable {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private InetAddress inetAddressGroup;
	private MulticastSocket multicastSocket;

	public UdpClient() {
		new Thread(this).start();
	}

	public void run() {
		try {
			multicastSocket = new MulticastSocket(port);
			inetAddressGroup = InetAddress.getByName("230.0.0.1");
			multicastSocket.joinGroup(inetAddressGroup);
		} catch (Exception e) {
			logger.error(null, e);
			return;
		}
		DatagramPacket packet;
		try {
			while (true) {
				try {
					byte[] buf = new byte[256];
					packet = new DatagramPacket(buf, buf.length);
					multicastSocket.receive(packet);
					String received = new String(packet.getData());
					logger.info("Recieved : " + received);
				} catch (Exception e) {
					logger.error(null, e);
				}
			}
		} catch (Exception e) {
			logger.error(null, e);
		} finally {
			try {
				multicastSocket.leaveGroup(inetAddressGroup);
				multicastSocket.close();
			} catch (Exception e) {
				logger.error(null, e);
			}
		}
	}

}
