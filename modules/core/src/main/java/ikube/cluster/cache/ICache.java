package ikube.cluster.cache;

import java.util.List;

public interface ICache {

	public interface IAction<T> {
		public void execute(T t);
	}

	public interface ICriteria<T> {
		public boolean evaluate(T t);
	}

	public <T> int size(String name);

	public <T> void clear(String name);

	public <T> T get(String name, Long id);

	public <T> T get(String name, String sql);

	public <T> void set(String name, Long id, T t);

	public <T> void remove(String name, Long id);

	public <T> List<T> get(String name, ICriteria<T> criteria, IAction<T> action, int size);

}
