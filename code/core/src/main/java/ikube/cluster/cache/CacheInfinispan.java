package ikube.cluster.cache;

import ikube.IConstants;
import ikube.toolkit.HashUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.apache.log4j.Logger;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * @see ICache
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
public class CacheInfinispan implements ICache {

	private static final Logger		LOGGER	= Logger.getLogger(CacheInfinispan.class);

	private String					configurationFile;
	private EmbeddedCacheManager	manager;

	public void initialise() throws Exception {
		System.setProperty("jgroups.bind_addr", "localhost");
		System.setProperty("java.net.preferIPv4Stack", "true");
		// GlobalConfiguration.getClusteredDefault().
		manager = new DefaultCacheManager(configurationFile);
		Set<String> cacheNames = manager.getCacheNames();
		manager.startCaches(cacheNames.toArray(new String[cacheNames.size()]));

		// We need to sleep here to give Infinispan a chance to start the caches
		// or the whole thing just goes to shit!
		Thread.sleep(10000);
		manager.start();
		// Cache<?, ?> cache = manager.getCache(Server.class.getName());
		// AdvancedCache<?, ?> advancedCache = cache.getAdvancedCache();
		// advancedCache.addInterceptor(new CommandInterceptor() {
		// @Override
		// protected Object handleDefault(InvocationContext ctx, VisitableCommand command) throws Throwable {
		// // LOGGER.info("Context : " + ctx + ", " + command);
		// return super.handleDefault(ctx, command);
		// }
		// }, 1);
	}

	public void destroy() {
		if (manager != null) {
			try {
				manager.stop();
			} catch (Exception e) {
				LOGGER.error("Exception shutting down Infinispan : " + manager, e);
			}
		}
	}

	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size(final String name) {
		return getMap(name).size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(final String name, final Long id) {
		return (T) getMap(name).get(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> void set(final String name, final Long id, final T object) {
		getMap(name).put(id, object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final String name, final Long id) {
		getMap(name).remove(id);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Object> List<T> get(final String name, final ICriteria<T> criteria, final IAction<T> action, final int size) {
		List<T> result = new ArrayList<T>();
		Map<Long, Object> map = getMap(name);
		for (Map.Entry<Long, Object> mapEntry : map.entrySet()) {
			if (result.size() >= size) {
				break;
			}
			T t = (T) mapEntry.getValue();
			if (criteria == null) {
				result.add(t);
			} else {
				if (criteria.evaluate(t)) {
					result.add(t);
				}
			}
			if (action != null) {
				action.execute(t);
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final String name) {
		getMap(name).clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> T get(final String name, final String sql) {
		throw new RuntimeException("This operation is not really required : ");
	}

	public boolean lock(final String name) {
		Cache<Long, Object> cache = manager.getCache(name);
		long lockHash = HashUtilities.hash(IConstants.ID_LOCK);
		AdvancedCache<Long, Object> advancedCache = cache.getAdvancedCache();
		TransactionManager transactionManager = advancedCache.getTransactionManager();
		boolean locked = Boolean.FALSE;
		try {
			transactionManager.begin();
			// advancedCache.lock(lockHash);
			Object lock = advancedCache.get(lockHash);
			if (lock != null) {
				LOGGER.info("Cache locked : " + name + ", " + lock);
			} else {
				advancedCache.put(lockHash, IConstants.ID_LOCK);
				locked = Boolean.TRUE;
			}
		} catch (Exception e) {
			LOGGER.error("Exception acquiring the transaction and the lock on : " + name, e);
			try {
				transactionManager.getTransaction().setRollbackOnly();
			} catch (Exception ex) {
				LOGGER.error("Exception setting rollback on transaction : ", e);
			}
		} finally {
			try {
				if (transactionManager.getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
					transactionManager.getTransaction().rollback();
				} else {
					transactionManager.commit();
				}
			} catch (Exception e) {
				LOGGER.error("Exception rolling back or comitting the transaction : ", e);
			}
		}
		return locked;
	}

	public boolean unlock(String name) {
		Cache<Long, Object> cache = manager.getCache(name);
		long lockHash = HashUtilities.hash(IConstants.ID_LOCK);
		AdvancedCache<Long, Object> advancedCache = cache.getAdvancedCache();
		TransactionManager transactionManager = advancedCache.getTransactionManager();
		boolean unlocked = Boolean.FALSE;
		try {
			transactionManager.begin();
			// advancedCache.lock(lockHash);
			Object lock = advancedCache.get(lockHash);
			if (lock == null) {
				LOGGER.info("Cache not locked : " + name + ", " + advancedCache.getLockManager().printLockInfo());
			} else {
				advancedCache.evict(lockHash);
				unlocked = Boolean.TRUE;
			}
		} catch (Exception e) {
			LOGGER.error("Exception comitting the transaction and removing the lock on : " + name, e);
			try {
				transactionManager.getTransaction().setRollbackOnly();
			} catch (Exception ex) {
				LOGGER.error("Exception setting rollback on transaction : ", e);
			}
		} finally {
			try {
				if (transactionManager.getTransaction().getStatus() == Status.STATUS_MARKED_ROLLBACK) {
					transactionManager.getTransaction().rollback();
				} else {
					transactionManager.commit();
				}
			} catch (Exception e) {
				LOGGER.error("Exception rolling back or comitting the transaction : ", e);
			}
		}
		return unlocked;
	}

	private Map<Long, Object> getMap(String name) {
		return manager.getCache(name);
	}

}