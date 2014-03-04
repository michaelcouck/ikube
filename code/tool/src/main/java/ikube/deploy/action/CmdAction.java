package ikube.deploy.action;

import ikube.deploy.model.Server;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * This action will connect to a remote machine over ssh, and execute a pre defined set of commands
 * on the remote machine. This action can potentially work in a windows environment if there is an ssh
 * server running on the target machine.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public class CmdAction extends Action {

    private Collection<String> commands;

    @Override
    public boolean execute(final Server server) {
        SSHClient sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
        logger.debug("Ssh exec : " + sshExec + ", " + commands);
        try {
            if (commands != null) {
                for (final String command : commands) {
                    try {
                        logger.info("Running command : {} on machine : {}", new Object[]{command, server.getIp()});

                        Session session = sshExec.startSession();
                        // Allows sudo apparently
                        session.allocateDefaultPTY();
                        // Session.Shell shell = session.startShell();

                        Session.Command sessionCommand = session.exec(command);
                        sessionCommand.join(60, TimeUnit.SECONDS);
                        String message = IOUtils.readFully(sessionCommand.getInputStream()).toString();
                        String error = IOUtils.readFully(sessionCommand.getErrorStream()).toString();

                        Integer exitStatus = sessionCommand.getExitStatus();
                        String errorMessage = sessionCommand.getExitErrorMessage();
                        // boolean coreDump = sessionCommand.getExitWasCoreDumped();

                        if (exitStatus != null && exitStatus > 0) {
                            Object[] parameters = {message, errorMessage, error, exitStatus/* , coreDump */};
                            logger.info("Message : {}, error message : {}, error : {}, exit status : {}", parameters);
                        } else if (StringUtils.isNotEmpty(message)) {
                            logger.info("Message : {}", message);
                        } else if (StringUtils.isNotEmpty(errorMessage)) {
                            logger.info("Error message : " + errorMessage);
                        }
                    } catch (final Exception e) {
                        handleException("Exception executing command on server : " + command + ", server : " + server.getIp(), e);
                    }
                }
            }
        } finally {
            disconnect(sshExec);
        }
        return Boolean.TRUE;
    }

    public void setCommands(Collection<String> commands) {
        this.commands = commands;
    }

}
