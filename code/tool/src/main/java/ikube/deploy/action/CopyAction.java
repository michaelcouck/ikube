package ikube.deploy.action;

import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import net.neoremind.sshxcute.core.SSHExec;

import java.io.File;
import java.util.Map;

/**
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
        SSHExec sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
        try {
            if (files != null) {
                for (final Map.Entry<String, String> filePair : files.entrySet()) {
                    String source = getAbsoluteFile(dotFolder, filePair.getKey());
                    String target = filePair.getValue();
                    try {
                        logger.info("Copying file : " + source + ", to : " + target + ", on server : " + server.getIp());
                        sshExec.uploadSingleDataToServer(source, target);
                    } catch (final Exception e) {
                        handleException("Exception copying file to server, from : " + source + ", to : " + target + ", server : " + server.getIp(), e);
                    }
                }
            }
            if (directories != null) {
                for (final Map.Entry<String, String> filePair : directories.entrySet()) {
                    String source = getAbsoluteFile(dotFolder, filePair.getKey());
                    String target = filePair.getValue();
                    try {
                        logger.info("Copying directory : " + source + ", to : " + target + ", on server : " + server.getIp());
                        sshExec.uploadAllDataToServer(source, target);
                    } catch (final Exception e) {
                        handleException("Exception copying directory to server, from : " + source + ", to : " + target + ", server : " + server.getIp(), e);
                    }
                }
            }
        } finally {
            disconnect(sshExec);
        }
        return Boolean.TRUE;
    }

    private String getAbsoluteFile(final String dotFolder, final String path) {
        File relative = new File(dotFolder, path);
        if (!relative.exists()) {
            relative = FileUtilities.findFileRecursively(new File(dotFolder), relative.getName());
            logger.info("Found file : " + relative);
        }
        return FileUtilities.cleanFilePath(relative.getAbsolutePath());
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public void setDirectories(Map<String, String> directories) {
        this.directories = directories;
    }

}
