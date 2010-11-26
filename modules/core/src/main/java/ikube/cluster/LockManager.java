package ikube.cluster;

import ikube.IConstants;
import ikube.listener.IListener;
import ikube.listener.ListenerManager;
import ikube.model.Event;
import ikube.model.Server;
import ikube.model.Token;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class LockManager extends ReceiverAdapter implements ILockManager {

	protected static final String CONFIGURATION = "META-INF/cluster/udp.xml";
	/**
	 * The token timeout. This is the maximum time we expect to not get the token. In the case of 3000 milliseconds, the average time to
	 * deliver a message is about 5 milliseconds so we can have many failures before we timeout. We have to set the timer to about 500
	 * milliseconds so that the actions have enough time to do some logic. If the token times out before the actions are finished the logic
	 * then there will be a problem with synchronisation.
	 */
	private long timeout = 3000;

	private Logger logger;
	/** The messaging channel. */
	private JChannel channel;
	/** The token that will be passed around. */
	private Token token;
	/** This server object. */
	private Server server;
	/** The listener for timing events. */
	private IListener listener;
	/** The message to the whole cluster. */
	private Message message;

	public LockManager() {
		this.logger = Logger.getLogger(this.getClass());
		this.message = new Message();
		open();
	}

	@Override
	public synchronized void open() {
		try {
			if (channel != null && channel.isOpen() && channel.isConnected()) {
				logger.info("Channel already open : " + channel);
				return;
			}
			this.listener = new IListener() {
				@Override
				public void handleNotification(Event event) {
					if (event.getType().equals(Event.CLUSTERING)) {
						LockManager.this.handleNotification(event);
					}
				}
			};
			ListenerManager.addListener(this.listener);
			try {
				this.server = new Server();
				this.server.setIp(InetAddress.getLocalHost().getHostAddress());

				this.token = new Token();
				this.token.setStart(System.nanoTime() + timeout);
				this.token.setServer(this.server);

				this.channel = new JChannel(CONFIGURATION);
				this.channel.enableStats(Boolean.TRUE);
				this.channel.connect(IConstants.IKUBE);
				this.channel.setReceiver(this);

				this.server.setAddress(channel.getAddress());
			} catch (Exception e) {
				logger.error("Exception opening channel : " + channel, e);
			}
		} finally {
			notifyAll();
		}
	}

	protected synchronized void handleNotification(Event event) {
		try {
			// Check that the channel is still open
			if (channel == null || !channel.isOpen() || !channel.isConnected()) {
				logger.info("Channel closed : " + channel + ", " + this.server.getAddress());
				return;
			}
			if (!event.getType().equals(Event.CLUSTERING)) {
				return;
			}
			List<Address> membersList = getMembersList(channel.getView());
			// If we don't have the token then we can't send it to anyone. To have the token
			// means that when we receive a broadcast and the ip address of the token is ours
			// then we save the token as ours. If the ip address of the token is not ours then we
			// ignore the message
			if (!this.server.getAddress().equals(token.getAddress())) {
				// We check to see if the token has timed out
				long age = System.nanoTime() - token.getStart();
				long thread = Thread.currentThread().hashCode();
				if (age > timeout) {
					Address firstAddress = membersList.get(0);
					if (logger.isDebugEnabled()) {
						StringBuilder builder = new StringBuilder("Token has timed out : ").append(this.token).append(", this : ").append(
								this.server.getAddress()).append(", first : ").append(firstAddress).append(", thread : ").append(thread);
						logger.debug(builder);
					}
					this.token.setServer(this.server);
					this.token.setAddress(firstAddress);
					this.token.setStart(System.nanoTime());
					message.setObject(this.token);
					channel.send(message);
					// NOTE : This doesn't work because two servers can send to two
					// different individual targets and the holders can then be different and
					// never normalise
					// channel.send(firstAddress, this.server.getAddress(), this.token);
					return;
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug(new StringBuilder("We don't have the token, it is at : ").append(token).append(", ").append(
								this.server.getAddress()).append(", ").append(thread));
					}
					return;
				}
			}
			publishToTarget(membersList);
		} catch (Exception e) {
			logger.error("Exception publishing to the cluster : " + token, e);
		} finally {
			notifyAll();
		}
	}

	protected synchronized void publishToTarget(List<Address> membersList) throws Exception {
		// We will publish this token so add ourselves
		this.token.setServer(this.server);
		// Get the index of ourselves in the list
		int index = Collections.binarySearch(membersList, this.server.getAddress());
		Address targetAddress = null;
		index++;
		if (index == membersList.size()) {
			// We are at the top of the members list so we need the first one
			targetAddress = membersList.get(0);
		} else {
			// We take the first member 'above' us as the target
			targetAddress = membersList.get(index);
		}
		if (logger.isDebugEnabled()) {
			logger.debug(new StringBuilder("Target : ").append(targetAddress).append(", index : ").append(index).append(
					", local address : ").append(this.server.getAddress()));
		}
		this.token.setServer(this.server);
		// We set the target address as we don't have the token anymore, i.e. we are blocked
		this.token.setAddress(targetAddress);
		// We set the start from the time that we send this token to the next server
		this.token.setStart(System.nanoTime());
		message.setObject(this.token);
		channel.send(message);
		// NOTE : This doesn't work because two servers can send to two
		// different individual targets and the holders can then be different and
		// never normalise
		// channel.send(targetAddress, this.server.getAddress(), this.token);
	}

	protected synchronized List<Address> getMembersList(View view) {
		Vector<Address> addresses = view.getMembers();
		List<Address> membersList = Arrays.asList(addresses.toArray(new Address[addresses.size()]));
		Collections.sort(membersList);
		return membersList;
	}

	@Override
	public synchronized void receive(Message message) {
		try {
			if (this.server == null || this.token == null) {
				// Means that we have been removed from the cluster but
				// JGroups hasn't really removed this listener so we will still get
				// the message, and of course the server is null already
				logger.info("Received message, but we are closed for business : " + this.channel);
				return;
			}
			Token token = (Token) message.getObject();
			// Update the properties in 'this' token from the one sent
			this.token.setStart(token.getStart());
			// This is 'our' address, the target from the source server
			this.token.setAddress(token.getAddress());
			// Populate the token on this server with the servers in the token
			this.token.getServers().addAll(token.getServers());
			// We add ourselves back because we want the live object in this Jvm
			this.token.setServer(this.server);
			if (logger.isDebugEnabled()) {
				long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - this.token.getStart());
				StringBuilder builder = new StringBuilder("Target : ").append(token.getAddress()).append(", local : ").append(
						this.server.getAddress()).append(", source : ").append(message.getSrc()).append(", duration : ").append(duration);
				logger.info(builder);
			}
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void suspect(Address address) {
		try {
			if (this.token == null) {
				// We are the dead server
				return;
			}
			// Remove the dead server if it still in the list
			Iterator<Server> servers = this.token.getServers().iterator();
			while (servers.hasNext()) {
				Server server = servers.next();
				if (address.equals(server.getAddress())) {
					if (logger.isDebugEnabled()) {
						logger.debug("Removing dead server : " + server);
					}
					servers.remove();
				}
			}
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized boolean haveToken() {
		try {
			if (this.token == null || this.server == null) {
				return Boolean.FALSE;
			}
			return this.server.getAddress().equals(token.getAddress());
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Token getToken() {
		try {
			while (!haveToken()) {
				long thread = Thread.currentThread().hashCode();
				try {
					logger.debug("Going into wait : " + thread);
					wait(timeout);
				} catch (Exception e) {
					logger.warn("", e);
				}
				logger.debug("Waking up : " + thread);
			}
			return this.token;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized Server getServer() {
		try {
			return this.server;
		} finally {
			notifyAll();
		}
	}

	@Override
	public synchronized void close() {
		try {
			if (this.listener != null) {
				ListenerManager.removeListener(this.listener);
			}
			if (channel != null && channel.isOpen() && channel.isConnected()) {
				channel.close();
			}
		} catch (Exception e) {
			logger.error("Exception disconnecting from cluster : ", e);
		} finally {
			this.listener = null;
			this.channel = null;
			this.token = null;
			this.server = null;
			notifyAll();
		}
	}

	public void setTimeout(long timeout) {
		this.timeout = TimeUnit.MILLISECONDS.toNanos(timeout);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		builder.append(this.server);
		builder.append(", ");
		builder.append(this.token);
		builder.append("]");
		return builder.toString();
	}

}