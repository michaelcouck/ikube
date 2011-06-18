package ikube.cluster.discovery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.ILock;
import com.hazelcast.core.IMap;

/**
 * This is a simulation of cluster synchronization logic.
 * 
 * @author 7518871 Michael Couck
 * @version %PR%
 */
@Ignore
public class Discovery {

	private Logger logger = Logger.getLogger(Discovery.class);

	@Test
	public void discover() throws Exception {
		Hazelcast.getTransaction();
		startClusterValidator();
		// We want to do these things, in this order then verify the state:
		// 1) Start and action with a particular index and indexable
		// 2) Finish the action

		// 1) Start and action with a particular index and indexable
		// 2) Change the indexable to another one
		// 3) Finish the action

		// 1) Start and action with a particular index and indexable
		// 2) Increment the id in the indexable
		// 3) Change the indexable to another one
		// 4) Increment the id in the indexable
		// 5) Finish the action

		// At random points in the flow of actions verify that
		// 1) There are never two servers that are doing the same action
		// with the same index
		// 2) That the server is never doing more than one action at a time
		// 3) The increments of the id in the indexable is synchronous and in one direction
		// i.e. that the numbering goes 1, 2, 3, not 1, 3, 2

		final Index[] indexes = new Index[10];
		for (int i = 0; i < indexes.length; i++) {
			Index index = new Index();
			indexes[i] = index;
			index.name = Index.class.getSimpleName() + "." + Integer.toString(i);
		}
		final Indexable[] indexables = new Indexable[10];
		for (int i = 0; i < indexables.length; i++) {
			Indexable indexable = new Indexable();
			indexables[i] = indexable;
			indexable.name = Indexable.class.getSimpleName() + "." + Integer.toString(i);
		}

		List<Thread> threadList = new ArrayList<Thread>();
		final int threads = 10;
		final int iterations = 1000;
		for (int i = 0; i < threads; i++) {
			Thread thread = new Thread(new Runnable() {

				public void run() {
					String serverName = Server.class.getSimpleName() + "." + Long.toString(System.nanoTime());
					for (int i = 0; i < iterations; i++) {
						try {
							Server server = getServer(serverName);
							// Case 1:
							// Check if there are any servers executing an action on this action and
							// index
							// Choose a random index to work on
							int indexOffset = (int) Math.min(Math.random() * indexes.length, indexes.length - 1);
							Index index = indexes[indexOffset];
							// Choose a random indexable to work on
							int indexableOffset = (int) Math.min(Math.random() * indexables.length, indexables.length);
							Indexable indexable = indexables[indexableOffset];
							// Lock the cluster here while we check the working actions and set our
							// own if necessary
							ILock lock = Hazelcast.getLock(Server.class.getSimpleName());
							if (lock.tryLock()) {
								// logger.error("Acquired lock : " + server.name);
								try {
									boolean anyWorking = anyWorking(index.name);
									if (!anyWorking) {
										// Start an action
										setWorking(server, index, indexable, Boolean.TRUE);
									}
								} finally {
									lock.unlock();
								}
							} else {
								// logger.error("Couldn't acquire lock : " + server.name);
							}
							// Sleep for a random time
							Thread.sleep((int) Math.random() * 10000);
							// Increment the id in the indexable
							indexable.id++;
							setWorking(server, index, indexable, Boolean.TRUE);
							// Sleep for a random time
							Thread.sleep((int) Math.random() * 10000);
							// Change the indexable to another one
							indexable = indexables[(int) Math.max(Math.random() * indexables.length, indexables.length - 1)];
							setWorking(server, index, indexable, Boolean.TRUE);
							// Sleep for a random time
							Thread.sleep((int) Math.random() * 10000);
							// Set the action to finished
							setWorking(server, index, indexable, Boolean.FALSE);
							// Sleep for a random time
							Thread.sleep((int) Math.random() * 10000);
						} catch (Exception e) {
							logger.error("Exception executing : ", e);
						}
					}
				}
			});
			thread.start();
			threadList.add(thread);
		}
		waitForThreads(threadList);
	}

	private void setWorking(Server server, Index index, Indexable indexable, boolean working) {
		if (!working) {
			// If we stop working then remove the action from the server
			server.action = null;
		} else {
			// First find the action in this server that is working
			if (server.action == null) {
				server.action = new Action();
				server.action.name = Action.class.getSimpleName() + "." + Long.toString(System.nanoTime());
			}
			server.action.index = index;
			server.action.index.indexable = indexable;
			server.action.working = working;
		}
		logger.info("Set working : " + server.name + ", " + working + ", " + index.name + ", " + indexable.name + ", " + indexable.id
				+ ", " + (server.action != null ? server.action.name : ""));
		setServer(server);
	}

	private Server getServer(String name) {
		IMap<String, Server> map = getServers();
		Server server = map.get(name);
		if (server == null) {
			server = new Server();
			server.name = name;
			map.put(name, server);
		}
		return server;
	}

	private IMap<String, Server> getServers() {
		return Hazelcast.getMap(Server.class.getSimpleName());
	}

	private void setServer(Server server) {
		IMap<String, Server> map = Hazelcast.getMap(Server.class.getSimpleName());
		map.put(server.name, server);
	}

	private boolean anyWorking(String index) {
		IMap<String, Server> servers = Hazelcast.getMap(Server.class.getSimpleName());
		for (Server server : servers.values()) {
			if (server.action == null) {
				continue;
			}
			if (server.action.working) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	private void startClusterValidator() {
		new Thread(new Runnable() {

			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					logger.error("", e);
				}
				while (true) {
					try {
						Thread.sleep(100);
						// Verify that there are no two servers working on the same action and index
						ILock lock = Hazelcast.getLock(Server.class.getSimpleName());
						if (lock.tryLock()) {
							try {
								Set<Action> workingActions = new TreeSet<Action>();
								IMap<String, Server> servers = getServers();
								for (Server server : servers.values()) {
									if (server.action == null) {
										continue;
									}
									if (server.action.working) {
										workingActions.add(server.action);
									}
									// Check that there are no two actions in the actions set
									// that are both on the same index
								}
								logger.info("Actions : " + workingActions.size() + ", " + workingActions);
								for (Action action : workingActions) {
									for (Action subAction : workingActions) {
										if (action.equals(subAction)) {
											continue;
										}
										if (action.index.name.equals(subAction.index.name)) {
											// Two actions working on the same index
											logger.fatal("Actions : " + action + ", " + subAction);
											Thread.sleep(1000);
											System.exit(1);
										}
									}
								}
							} finally {
								lock.unlock();
							}
						} else {
							logger.error("Couldn't acquire lock : ");
						}
					} catch (Exception e) {
						logger.error("Error...", e);
					}
				}
			}
		}).start();
	}

	/**
	 * This method iterates through the list of threads looking for one that is still alive and joins it. Once all the threads have finished
	 * then this method will return to the caller indicating that all the threads have finished.
	 * 
	 * @param threads
	 *            the threads to wait for
	 */
	public void waitForThreads(final List<Thread> threads) {
		outer: while (true) {
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					try {
						thread.join(60000);
					} catch (InterruptedException e) {
						logger.error("Interrupted waiting for thread : " + thread + ", this thread : " + Thread.currentThread(), e);
					}
					continue outer;
				}
			}
			break;
		}
	}

}
