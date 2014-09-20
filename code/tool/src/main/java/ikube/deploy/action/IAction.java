package ikube.deploy.action;

import ikube.deploy.model.Server;

/**
 * This interface is for any action that can be executed on a machine. Typical implementations would be
 * for executing ssh commands on a remote server and or copying files and folders to a remote server.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public interface IAction {

    /**
     * This method will execute a particular logic or command on the specified server.
     *
     * @param server the server to execute the command on
     * @return whether the execution was successful or not
     * @throws Exception any exception that can be experienced during the execution of the logic
     */
    boolean execute(final Server server) throws Exception;

}
