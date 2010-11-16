package ikube.cluster;

import ikube.IConstants;
import ikube.model.IndexContext;
import ikube.model.Token;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

//public class ClusterManager extends ReceiverAdapter implements IClusterManager {
//
//	protected static final String CONFIGURATION = "META-INF/cluster/mping.xml";
//
//	private Logger logger = Logger.getLogger(ClusterManager.class);
//	private JChannel channel;
//	private Map<String, Token> tokens = new HashMap<String, Token>();
//
//	public ClusterManager() {
//		try {
//			channel = new JChannel(CONFIGURATION);
//			channel.enableStats(Boolean.TRUE);
//			channel.connect(IConstants.IKUBE);
//			channel.setReceiver(this);
//		} catch (Exception e) {
//			logger.error("", e);
//		}
//	}
//
//	@Override
//	public void receive(Message message) {
//		Object object = message.getObject();
//		logger.info("Message : " + object);
//		if (object != null) {
//			if (Token.class.isAssignableFrom(object.getClass())) {
//				Token token = (Token) object;
//				tokens.put(token.getIp(), token);
//			}
//		}
//	}
//
//	public void viewAccepted(View view) {
//		logger.info("View : " + view);
//		for (Token token : tokens.values()) {
//			publish(token);
//		}
//	}
//
//	public void suspect(Address address) {
//		logger.info("Suspected address : " + address);
//	}
//
//	@Override
//	public Token getServer(IndexContext indexContext) {
//		Token thisServer = tokens.get(indexContext.getServerName());
//		if (thisServer == null) {
//			thisServer = new Token();
//			thisServer.setIp(indexContext.getServerName());
//			tokens.put(thisServer.getIp(), thisServer);
//			// publish(thisServer);
//		}
//		return thisServer;
//	}
//
//	@Override
//	public synchronized boolean anyWorking(IndexContext indexContext, String actionName) {
//		try {
//			logger.info("Action : " + actionName + ", " + Thread.currentThread().hashCode());
//			for (Token token : tokens.values()) {
//				if (token.isWorking() && !actionName.equals(token.getAction())) {
//					return Boolean.TRUE;
//				}
//			}
//		} finally {
//			notifyAll();
//		}
//		return Boolean.FALSE;
//	}
//
//	@Override
//	public synchronized boolean areWorking(IndexContext indexContext, String actionName) {
//		try {
//			logger.info("Action : " + actionName + ", " + Thread.currentThread().hashCode());
//			for (Token token : tokens.values()) {
//				if (token.isWorking() && indexContext.getIndexName().equals(token.getIndex()) && actionName.equals(token.getAction())) {
//					return Boolean.TRUE;
//				}
//			}
//		} finally {
//			notifyAll();
//		}
//		return Boolean.FALSE;
//	}
//
//	@Override
//	public synchronized long getLastWorkingTime(IndexContext indexContext, String actionName) {
//		long time = System.currentTimeMillis();
//		try {
//			logger.info("Servers : " + tokens + ", " + Thread.currentThread().hashCode());
//			Token thisServer = getServer(indexContext);
//			for (Token token : tokens.values()) {
//				if (!token.isWorking()) {
//					continue;
//				}
//				if (!indexContext.getIndexName().equals(token.getIndex())) {
//					continue;
//				}
//				// This means that there is a server working on this index but with
//				// a different action
//				if (!actionName.equals(token.getAction())) {
//					time = -1;
//					break;
//				}
//				long start = token.getStart();
//				// We want the start time of the first server, so if the start time
//				// of this server is lower than the existing start time then take that
//				if (start < time && start > 0) {
//					logger.info("Taking start time of server : " + token);
//					time = start;
//				}
//			}
//			thisServer.setStart(time);
//			thisServer.setAction(actionName);
//			thisServer.setBatch(0);
//			thisServer.setWorking(Boolean.TRUE);
//			// publish(thisServer);
//			logger.info("Time : " + time + ", " + tokens + ", " + Thread.currentThread().hashCode());
//		} finally {
//			notifyAll();
//		}
//		return time;
//	}
//
//	@Override
//	public synchronized int getNextBatchNumber(IndexContext indexContext) {
//		int batch = 0;
//		try {
//			String index = indexContext.getIndexName();
//			Token thisServer = getServer(indexContext);
//			for (Token token : tokens.values()) {
//				if (token.isWorking() && index.equals(token.getIndex())) {
//					if (token.getBatch() > batch) {
//						batch = token.getBatch();
//					}
//				}
//			}
//			logger.info("Batch : " + batch + ", " + thisServer + ", " + Thread.currentThread().hashCode());
//			// Set the batch for this server to the batch plus
//			// the increment in the context
//			thisServer.setBatch(batch + (int) indexContext.getBatchSize());
//			// Publish this server to the cluster
//			// publish(thisServer);
//		} finally {
//			notifyAll();
//		}
//		return batch;
//	}
//
//	@Override
//	public Map<String, Token> getServers(IndexContext indexContext) {
//		return tokens;
//	}
//
//	@Override
//	public synchronized Boolean isWorking(IndexContext indexContext) {
//		try {
//			Token thisServer = getServer(indexContext);
//			logger.info("This server : " + thisServer + ", " + Thread.currentThread().hashCode());
//			return thisServer != null && thisServer.isWorking();
//		} finally {
//			notifyAll();
//		}
//	}
//
//	@Override
//	public synchronized boolean resetWorkings(IndexContext indexContext, String actionName) {
//		try {
//			// Check that there are no servers working
//			for (Token token : tokens.values()) {
//				if (token.isWorking()) {
//					logger.info("Servers working, not resetting : " + Thread.currentThread().hashCode());
//					return Boolean.FALSE;
//				}
//			}
//			logger.info("No server working, resetting : " + tokens + ", " + Thread.currentThread().hashCode());
//			List<Token> synchronizedServers = Arrays.asList(tokens.values().toArray(new Token[tokens.size()]));
//			for (Token token : synchronizedServers) {
//				token.setAction(null);
//				token.setBatch(0);
//				token.setStart(0);
//				token.setWorking(Boolean.FALSE);
//				// publish(server);
//			}
//			return Boolean.TRUE;
//		} finally {
//			notifyAll();
//		}
//	}
//
//	@Override
//	public synchronized void setWorking(IndexContext indexContext, String actionName, boolean isWorking, long start) {
//		try {
//			Token thisServer = getServer(indexContext);
//			thisServer.setIndex(indexContext.getIndexName());
//			thisServer.setWorking(isWorking);
//			thisServer.setAction(actionName);
//			thisServer.setStart(start);
//			// publish(thisServer);
//			logger.info("This server : " + thisServer + ", " + tokens + ", " + Thread.currentThread().hashCode());
//		} finally {
//			notifyAll();
//		}
//	}
//
//	protected synchronized void publish(Serializable serializable) {
//		try {
//			Message message = new Message();
//			message.setObject(serializable);
//			channel.send(message);
//		} catch (Exception e) {
//			logger.error("Exception publishing to the cluster : " + serializable, e);
//		}
//	}
//
//}