package ikube.cluster.cache;

import java.util.List;

/**
 * Can't find any documentation for a simple distributed map. Pity.
 * 
 * @see ICache
 * @author Michael Couck
 * @since 01.10.11
 * @version 01.00
 */
public class CacheEhcache implements ICache {

	public void initialise() throws Exception {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size(final String name) {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> T get(final String name, final Long id) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> void set(final String name, final Long id, final T object) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(final String name, final Long id) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> List<T> get(final String name, final ICriteria<T> criteria, final IAction<T> action, final int size) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear(final String name) {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Object> T get(final String name, final String sql) {
		return null;
	}

	@Override
	public boolean lock(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unlock(String name) {
		// TODO Auto-generated method stub
		return false;
	}

}