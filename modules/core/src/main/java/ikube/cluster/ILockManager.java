package ikube.cluster;

public interface ILockManager {

	public void open();

	public boolean haveToken();

	public void close();

}
