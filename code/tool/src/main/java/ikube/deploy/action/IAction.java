package ikube.deploy.action;

import ikube.deploy.model.Server;

public interface IAction {

	boolean execute(final Server server);

}
