package ikube.cluster;

import ikube.model.Server;
import ikube.model.Token;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public interface ILockManager {

	public void open();

	public Server getServer();

	public boolean haveToken();

	public Token getToken();

	public void close();

}
