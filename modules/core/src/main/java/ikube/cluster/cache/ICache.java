package ikube.cluster.cache;

import java.util.List;

public interface ICache {

	public interface IAction<T> {
		public void execute(T t);
	}

	public interface ICriteria<T> {
		public boolean evaluate(T t);
	}

	public <T> int size(Class<T> klass);

	public <T> void clear(Class<T> klass);

	public <T> T get(Class<T> klass, Long id);

	public <T> T get(Class<T> klass, String sql);

	public <T> void set(Class<T> klass, Long id, T t);

	public <T> void remove(Class<T> klass, Long id);

	public <T> List<T> get(Class<T> klass, ICriteria<T> criteria, IAction<T> action, int size);

}
