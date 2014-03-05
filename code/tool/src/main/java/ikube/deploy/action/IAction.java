package ikube.deploy.action;

import ikube.deploy.model.Server;

/**
 * @author Michael Couck
 * @since 18-06-13
 * @version 01.00
 */
public interface IAction {

	boolean execute(final Server server) throws Exception;

}
