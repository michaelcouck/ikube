package ikube.cluster;

public interface IAtomicAction {
	
	<T> T execute();
	
}
