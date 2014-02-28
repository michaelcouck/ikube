package ikube.deploy.action;

import ikube.deploy.model.Server;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;

import java.util.Collection;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-13
 */
public class CommandAction extends Action {

    private Collection<String> commands;

    @Override
    public boolean execute(final Server server) {
        SSHClient sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
        logger.info("Ssh exec : " + sshExec + ", " + commands);
        try {
            if (commands != null) {
                for (final String command : commands) {
                    try {
                        logger.info("Running command : {} on machine : {}", new Object[]{command, server.getIp()});

                        Session session = sshExec.startSession();
                        Session.Command sessionCommand = session.exec(command);
                        String message = IOUtils.readFully(sessionCommand.getInputStream()).toString();
                        String error = IOUtils.readFully(sessionCommand.getErrorStream()).toString();

                        Integer exitStatus = sessionCommand.getExitStatus();
                        String errorMessage = sessionCommand.getExitErrorMessage();
                        // boolean coreDump = sessionCommand.getExitWasCoreDumped();

                        Object[] parameters = {errorMessage, error, exitStatus/* , coreDump */};
                        logger.info("Message : {} ", message);
                        logger.info("Error message : {}, error : {}, exit status : {}", parameters);
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
