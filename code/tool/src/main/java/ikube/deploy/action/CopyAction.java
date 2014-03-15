package ikube.deploy.action;

import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import ikube.toolkit.ThreadUtilities;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import net.schmizz.sshj.xfer.scp.SCPUploadClient;

import java.io.File;
import java.util.Map;

/**
 * This action will take files and folders from the machine that it is executing on, and
 * copy them to the target machine using scp.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 18-06-13
 */
public class CopyAction extends Action {

    private Map<String, String> files;
    private Map<String, String> directories;

    @Override
    public boolean execute(final Server server) {
        String dotFolder = FileUtilities.cleanFilePath(new File(".").getAbsolutePath());
        logger.info("Dot folder : " + dotFolder);
        SSHClient sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
        // sshExec.useCompression();
        if (directories != null) {
            for (final Map.Entry<String, String> filePair : directories.entrySet()) {
                execute(sshExec, dotFolder, server, filePair.getKey(), filePair.getValue());
            }
        }
        if (files != null) {
            for (final Map.Entry<String, String> filePair : files.entrySet()) {
                execute(sshExec, dotFolder, server, filePair.getKey(), filePair.getValue());
            }
        }
        return Boolean.TRUE;
    }

    private void execute(final SSHClient sshExec, final String dotFolder, final Server server, final String srcFile, final String destFile) {
        int retry = RETRY;
        boolean mustRetry;
        int returnCode;
        do {
            String source = getAbsoluteFile(dotFolder, srcFile);
            try {
                ThreadUtilities.sleep(getSleep());
                logger.info("Copying : " + source + ", to : " + destFile + ", on server : " + server.getIp());
                SCPFileTransfer scpFileTransfer = sshExec.newSCPFileTransfer();
                SCPUploadClient scpUploadClient = scpFileTransfer.newSCPUploadClient();
                returnCode = scpUploadClient.copy(new FileSystemFile(source), destFile);
                logger.info("Return code : " + returnCode);
            } catch (final Exception e) {
                returnCode = 1;
                handleException("Exception copying directory to server, from : " + source + ", to : " + destFile + ", server : " + server.getIp(), e);
            }
            mustRetry = returnCode > 0 && retry-- >= 0;
            if (mustRetry) {
                logger.info("Retrying : " + source + ", " + destFile);
            }
        } while (mustRetry);
    }

    private String getAbsoluteFile(final String dotFolder, final String path) {
        File relative = new File(dotFolder, path);
        if (!relative.exists()) {
            relative = FileUtilities.findFileRecursively(new File(dotFolder), relative.getName());
            logger.info("Found file : " + relative);
        }
        return FileUtilities.cleanFilePath(relative.getAbsolutePath());
    }

    public void setFiles(final Map<String, String> files) {
        this.files = files;
    }

    public void setDirectories(final Map<String, String> directories) {
        this.directories = directories;
    }

}
