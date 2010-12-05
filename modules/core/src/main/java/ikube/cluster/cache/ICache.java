package ikube.cluster.cache;

import java.util.List;

public interface ICache<T> {
	
	public interface IAction<T> {
		public void execute(T t);
	}

	public T get(Long hash);

	public void set(Long hash, T t);

	public List<T> getBatch(Class<T> tClass, IAction<T> action);
	
	public int size();
	
	public void clear();

}
