package ikube.cluster;

import ikube.model.Server;
import ikube.model.Token;

public interface ILockManager {

	public void open();

	public Server getServer();

	public boolean haveToken();

	public Token getToken();

	public void close();

}
