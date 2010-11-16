package ikube.cluster;

import ikube.IConstants;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.model.Token;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;

public class LockManager extends ReceiverAdapter implements ILockManager {

	protected static final String CONFIGURATION = "META-INF/cluster/udp.xml";
	/**
	 * The token timeout. This is the maximum time we expect to not get the token. In the case of 3000 milliseconds, the average time to
	 * deliver a message is about 5 milliseconds so we can have many failures before we timeout. We have to set the timer to about 100
	 * milliseconds so that the actions have enough time to do some logic. If the token times out before the actions are finished the logic
	 * then there will be a problem with synchronisation.
	 */
	protected static final long TIMEOUT = 1000;

	private Logger logger;
	/** The messaging channel. */
	private JChannel channel;
	/** The token that will be passed around. */
	private Token token;
	/** This server object. */
	private Server server;

	public LockManager() {
		this.logger = Logger.getLogger(this.getClass());
		IListener listener = new IListener() {
			@Override
			public void handleNotification(Event event) {
				if (event.getType().equals(Event.CLUSTERING)) {
					LockManager.this.handleNotification(event);
				}
			}
		};
		ListenerManager.addListener(listener);
		open();
	}

	public void open() {
		if (channel != null && channel.isOpen() && channel.isConnected()) {
			// logger.warn("Channel already open : " + channel);
			return;
		}
		try {
			this.channel = new JChannel(CONFIGURATION);
			this.channel.enableStats(Boolean.TRUE);
			this.channel.connect(IConstants.IKUBE);
			this.channel.setReceiver(this);

			this.token = new Token();
			this.token.setStart(System.currentTimeMillis() + TIMEOUT);
			this.server = new Server();
			this.server.setIp(InetAddress.getLocalHost().getHostAddress());
			this.server.setAddress(channel.getAddress());
		} catch (Exception e) {
			logger.error("Exception opening channel : " + channel, e);
		}
	}

	protected synchronized void handleNotification(Event event) {
		try {
			// Check that the channel is still open
			if (channel == null || !channel.isOpen() || !channel.isConnected()) {
				// logger.warn("Channel closed : " + channel + ", " + address);
				return;
			}
			if (!event.getType().equals(Event.CLUSTERING)) {
				return;
			}
			// If we don't have the token then we can't send it to anyone. To have the token
			// means that when we receive a broadcast and the ip address of the token is ours
			// then we save the token as ours. If the ip address of the token is not ours then we
			// ignore the message
			Address address = this.server.getAddress();
			if (!address.toString().equals(token.getIp())) {
				// We check to see if the token has timed out
				long age = System.currentTimeMillis() - token.getStart();
				if (age > TIMEOUT) {
					logger.debug("Token has timed out : " + token + ", we will publish again : " + address);
				} else {
					logger.debug("We don't have the token, it is at : " + token + ", " + address);
					return;
				}
			}
			Vector<Address> membersVector = channel.getView().getMembers();
			if (membersVector.size() == 1) {
				// We are the only member in the cluster
				// logger.info("Only ourselves in the cluster : " + address);
				this.token.setIp(address.toString());
				this.token.setServer(this.server);
				return;
			}
			// We add ourselves to the server list in the token, like a refresh
			this.token.setServer(this.server);
			// Verify that all the servers in the token are still alive
			Iterator<Server> servers = this.token.getServers().iterator();
			while (servers.hasNext()) {
				Server server = servers.next();
				if (!membersVector.contains(server.getAddress())) {
					logger.debug("Removing dead server : " + server);
					servers.remove();
				}
			}
			// Create an array so we don't interfere with the view and sort the members according to the name
			Address[] memberArray = membersVector.toArray(new Address[membersVector.size()]);
			Arrays.sort(memberArray);
			// Get the index of ourselves
			int index = Arrays.binarySearch(memberArray, address);
			Address target = null;
			index++;
			if (index == memberArray.length) {
				// We are at the top of the members list so we need the first one
				target = memberArray[0];
			} else {
				// We take the first member 'above' us as the target
				target = memberArray[index];
			}
			logger.debug("Target : " + target + ", index : " + index + ", local address : " + address);
			// We set our ip as we don't have the token anymore, i.e. we are blocked
			this.token.setIp(target.toString());
			// We set the start from the time that we send this token to the next server
			this.token.setStart(System.currentTimeMillis());
			channel.send(target, address, token);
		} catch (Exception e) {
			logger.error("Exception publishing to the cluster : " + token, e);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void receive(Message message) {
		try {
			Address address = this.server.getAddress();
			if (message.getSrc().compareTo(address) == 0) {
				logger.info("Own message : " + message);
				return;
			}
			// Now we have the token
			Object object = message.getObject();
			if (object != null && Token.class.isAssignableFrom(object.getClass())) {
				this.token = (Token) object;
				if (logger.isDebugEnabled()) {
					long duration = System.currentTimeMillis() - this.token.getStart();
					StringBuilder builder = new StringBuilder("Source address : ").append(message.getSrc()).append(
							", destination address : ").append(message.getDest()).append(", local address : ").append(address).append(
							", token : ").append(this.token).append(", duration : ").append(duration);
					logger.info(builder.toString());
				}
			}
		} finally {
			notifyAll();
		}
	}

	public synchronized boolean haveToken() {
		try {
			return this.server.getAddress().toString().equals(token.getIp());
		} finally {
			notifyAll();
		}
	}

	public synchronized void close() {
		try {
			if (channel.isOpen() && channel.isConnected()) {
				this.token = null;
				this.server = null;
				channel.close();
			}
		} catch (Exception e) {
			logger.error("Exception disconnecting from cluster : ", e);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean anyWorking(IndexContext indexContext, String actionName) {
		try {
			for (Server server : token.getServers()) {
				if (this.server.compareTo(server) == 0) {
					continue;
				}
				if (server.isWorking() && !actionName.equals(server.getAction())) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean areWorking(IndexContext indexContext, String actionName) {
		try {
			for (Server server : token.getServers()) {
				if (this.server.compareTo(server) == 0) {
					continue;
				}
				if (server.isWorking() && indexContext.getIndexName().equals(server.getIndex()) && actionName.equals(server.getAction())) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized long getLastWorkingTime(IndexContext indexContext, String actionName) {
		long time = System.currentTimeMillis();
		try {
			logger.info("Servers : " + token.getServers() + ", " + Thread.currentThread().hashCode());
			for (Server server : token.getServers()) {
				if (!server.isWorking()) {
					continue;
				}
				if (!indexContext.getIndexName().equals(server.getIndex())) {
					continue;
				}
				// This means that there is a server working on this index but with a different action
				if (!actionName.equals(server.getAction())) {
					logger.debug("Another action on this index : " + server);
					time = -1;
					break;
				}
				long start = server.getStart();
				// We want the start time of the first server, so if the start time
				// of this server is lower than the existing start time then take that
				if (start < time && start > 0) {
					logger.info("Taking start time of server : " + token);
					time = start;
				}
			}
			this.server.setStart(time);
			this.server.setAction(actionName);
			this.server.setIdNumber(0);
			this.server.setWorking(Boolean.TRUE);
			logger.info("Time : " + time + ", " + token.getServers() + ", " + Thread.currentThread().hashCode());
		} finally {
			notifyAll();
		}
		return time;
	}

	/**
	 * TODO - this method is called from the visitors so we need to extract an action from this method so that the synchronisation aspect
	 * will wrap it in a 'transaction'
	 */
	@Override
	public synchronized long getIdNumber(IndexContext indexContext) {
		long idNumber = 0;
		try {
			String index = indexContext.getIndexName();
			for (Server server : token.getServers()) {
				if (server.isWorking() && index.equals(server.getIndex())) {
					if (server.getIdNumber() > idNumber) {
						idNumber = server.getIdNumber();
					}
				}
			}
			logger.info("Get id number : " + idNumber + ", " + this.server + ", " + Thread.currentThread().hashCode());
		} finally {
			notifyAll();
		}
		return idNumber;
	}

	public synchronized void setIdNumber(IndexContext indexContext, long idNumber) {
		try {
			logger.debug("Setting id number : " + idNumber + ", " + this.server + ", " + Thread.currentThread().hashCode());
			this.server.setIdNumber(idNumber);
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Server getServer(IndexContext indexContext) {
		try {
			return this.server;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Set<Server> getServers(IndexContext indexContext) {
		try {
			return token.getServers();
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean isWorking(IndexContext indexContext) {
		try {
			return server.isWorking();
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean resetWorkings(IndexContext indexContext, String actionName) {
		try {
			// Check that there are no servers working
			for (Server server : token.getServers()) {
				if (server.isWorking()) {
					logger.info("Servers working, not resetting : " + Thread.currentThread().hashCode());
					return Boolean.FALSE;
				}
			}
			logger.info("No server working, resetting : " + token.getServers() + ", " + Thread.currentThread().hashCode());
			for (Server server : token.getServers()) {
				if (server.isWorking()) {
					server.setAction(null);
					server.setIdNumber(0);
					server.setStart(0);
					server.setWorking(Boolean.FALSE);
				}
			}
			return Boolean.TRUE;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void setWorking(IndexContext indexContext, String actionName, boolean isWorking, long start) {
		try {
			server.setAction(actionName);
			server.setIndex(indexContext.getIndexName());
			server.setStart(start);
			server.setWorking(isWorking);
			logger.debug("Set working : " + server + ", " + Thread.currentThread().hashCode());
			// Thread.dumpStack();
		} finally {
			notifyAll();
		}
	}

}