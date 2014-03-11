package ikube.deploy.action;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-2013
 */
public abstract class Action implements IAction {

    protected static final int RETRY = 5;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected boolean breakOnError;

    protected SSHClient getSshExec(final String ip, final String username, final String password) {
        int retry = RETRY;
        SSHClient sshExec = null;
        do {
            try {
                sshExec = new SSHClient();
                logger.info("Connecting to : " + ip + " as " + username);
                sshExec.addHostKeyVerifier(new PromiscuousVerifier());
                sshExec.connect(ip);
                sshExec.authPassword(username, password.toCharArray());
                // sshExec.loadKnownHosts();
            } catch (final Exception e) {
                disconnect(sshExec);
                handleException("Exception connecting to : " + ip, e);
            }
        } while (sshExec == null && retry-- >= 0);
        return sshExec;
    }

    protected void disconnect(final SSHClient sshExec) {
        try {
            if (sshExec != null) {
                sshExec.disconnect();
            }
        } catch (final Exception e) {
            handleException("Exception closing connection : " + sshExec, e);
        }
    }

    protected void handleException(final String message, final Exception exception) {
        logger.error(message, exception);
        if (isBreakOnError()) {
            throw new RuntimeException(exception);
        }
    }

    public boolean isBreakOnError() {
        return breakOnError;
    }

    public void setBreakOnError(boolean breakOnError) {
        this.breakOnError = breakOnError;
    }

}
