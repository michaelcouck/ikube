package ikube.deploy.action;

import ikube.deploy.model.Server;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the base action class for remote actions. It defines getting the shell executor
 * class from the remote machine. It will try several times to get a remote connection before
 * it gives up.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public abstract class Action implements IAction {

    protected static final int RETRY = 3;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected long sleep;
    protected boolean breakOnError;

    protected SSHClient getSshExec(final Server server) {
        if (server.getSshExec() != null) {
            return server.getSshExec();
        }

        String ip = server.getIp();
        String username = server.getUsername();
        String password = server.getPassword();

        int retry = RETRY;
        do {
            SSHClient sshClient = null;
            try {
                sshClient = new SSHClient();
                logger.info("Connecting to : " + ip + " as " + username);
                sshClient.setTimeout(Integer.MAX_VALUE);
                sshClient.setConnectTimeout(Integer.MAX_VALUE);
                sshClient.addHostKeyVerifier(new PromiscuousVerifier());
                sshClient.connect(ip);
                sshClient.authPassword(username, password.toCharArray());
                // sshExec.loadKnownHosts();
                server.setSshExec(sshClient);
            } catch (final Exception e) {
                handleException("Exception connecting to : " + ip + ", retrying : " + (retry > 0), e);
            } finally {
                if ((server.getSshExec() == null || !server.getSshExec().isConnected()) && sshClient != null) {
                    try {
                        sshClient.disconnect();
                        sshClient.close();
                    } catch (final Exception e) {
                        handleException("Exception dis-connecting : ", e);
                    }
                }
            }
        } while (server.getSshExec() == null && --retry >= 0);
        return server.getSshExec();
    }

    protected void handleException(final String message, final Exception exception) {
        logger.error(message, exception);
        if (isBreakOnError()) {
            throw new RuntimeException(exception);
        }
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public boolean isBreakOnError() {
        return breakOnError;
    }

    public void setBreakOnError(boolean breakOnError) {
        this.breakOnError = breakOnError;
    }

}
