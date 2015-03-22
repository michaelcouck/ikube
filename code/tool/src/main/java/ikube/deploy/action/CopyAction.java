package ikube.deploy.action;

import ikube.deploy.model.Server;
import ikube.toolkit.FILE;
import ikube.toolkit.THREAD;
import net.schmizz.sshj.xfer.FileSystemFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import net.schmizz.sshj.xfer.scp.SCPUploadClient;

import java.io.File;
import java.util.Map;

/**
 * This action will take files and folders from the machine that it is executing on, and
 * copy them to the target machine using scp. The logic will retry a few times, and then give up.
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
        // sshExec.useCompression();
        getSshExec(server);
        if (directories != null) {
            for (final Map.Entry<String, String> filePair : directories.entrySet()) {
                execute(server, filePair.getKey(), filePair.getValue());
                THREAD.sleep(getSleep());
            }
        }
        if (files != null) {
            for (final Map.Entry<String, String> filePair : files.entrySet()) {
                execute(server, filePair.getKey(), filePair.getValue());
                THREAD.sleep(getSleep());
            }
        }
        return Boolean.TRUE;
    }

    private void execute(final Server server, final String srcFile, final String destFile) {
        int retry = RETRY;
        int returnCode = 1;
        do {
            String source = null;
            try {
                source = getAbsoluteFile(srcFile);
                logger.info("Copying : " + source + ", to : " + destFile + ", on server : " + server.getIp());
                SCPFileTransfer scpFileTransfer = server.getSshExec().newSCPFileTransfer();
                SCPUploadClient scpUploadClient = scpFileTransfer.newSCPUploadClient();
                returnCode = scpUploadClient.copy(new FileSystemFile(source), destFile);
                logger.debug("Return code : " + returnCode);
            } catch (final Exception e) {
                handleException("Exception copying directory to server, from : " + source +
                        ", to : " + destFile +
                        ", server : " + server.getIp() +
                        ", return code : " + returnCode +
                        ", retrying...", e);
            }
        } while (returnCode > 0 && --retry >= 0);
    }

    private String getAbsoluteFile(final String path) {
        logger.info("Looking for file/folder : " + path);
        File relative = new File(path);
        if (!relative.exists()) {
            // Try and look for it from teh dot directory
            relative = FILE.findFileRecursively(new File("."), relative.getName());
            if (relative == null || !relative.exists()) {
                throw new RuntimeException("Couldn't find file : " + relative);
            }
        }
        logger.info("Found file : " + relative);
        return FILE.cleanFilePath(relative.getAbsolutePath());
    }

    public void setFiles(final Map<String, String> files) {
        this.files = files;
    }

    public void setDirectories(final Map<String, String> directories) {
        this.directories = directories;
    }

}
