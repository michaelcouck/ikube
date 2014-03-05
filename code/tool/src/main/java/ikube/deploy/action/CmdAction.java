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
    @SuppressWarnings("EmptyFinallyBlock")
    public boolean execute(final Server server) throws Exception {
        final SSHClient sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
        logger.debug("Ssh exec : " + sshExec + ", " + commands);
        try {
            if (commands != null) {
                // Session session = sshExec.startSession();
                // session.allocateDefaultPTY();
                // Session.Shell shell = session.startShell();
                // OutputStream outputStream = shell.getOutputStream();
                for (final String command : commands) {
                    try {
                        logger.info("Running command : {} on machine : {}", new Object[]{command, server.getIp()});
                        // Allows sudo apparently
                        // session.allocateDefaultPTY();

                        /*outputStream.write(command.getBytes());
                        outputStream.write("\n".getBytes());*/
                        // shell.join(10, TimeUnit.SECONDS);

                        Session session = sshExec.startSession();
                        Session.Command sessionCommand = session.exec(command);
                        sessionCommand.join(10, TimeUnit.SECONDS);

                        Integer exitStatus = sessionCommand.getExitStatus();
                        String errorMessage = sessionCommand.getExitErrorMessage();
                        // boolean coreDump = sessionCommand.getExitWasCoreDumped();

                        if (exitStatus != null && exitStatus > 0) {
                            // NOTE TO SELF!!!: Do not consume the output, this acts like consuming the nohup
                            // for example, and the script terminates when the shell exits, i.e. it doesn't start the
                            // tomcat. We only read from the output if there is an error otherwise the process
                            // started, perhaps by a shell script, terminates *****immediately***** !!!!!
                            String message = IOUtils.readFully(sessionCommand.getInputStream()).toString();
                            String error = IOUtils.readFully(sessionCommand.getErrorStream()).toString();
                            Object[] parameters = {message, errorMessage, error, exitStatus};
                            logger.info("Message : {}, error message : {}, error : {}, exit status : {}", parameters);
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
