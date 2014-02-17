package ikube.deploy.action;

import ikube.deploy.model.Server;
import ikube.toolkit.FileUtilities;
import net.neoremind.sshxcute.core.SSHExec;

import java.io.File;
import java.util.Map;

public class CopyAction extends Action {

    private Map<String, String> files;
    private Map<String, String> directories;

    @Override
    public boolean execute(final Server server) {
        logger.info("Dot folder : " + FileUtilities.cleanFilePath(new File(".").getAbsolutePath()));
        SSHExec sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
        try {
            if (files != null) {
                for (final Map.Entry<String, String> filePair : files.entrySet()) {
                    String source = getAbsoluteFile(filePair.getKey());
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
                    String source = getAbsoluteFile(filePair.getKey());
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

    private String getAbsoluteFile(final String path) {
        File relative = FileUtilities.relative(new File("."), path);
        return FileUtilities.cleanFilePath(relative.getAbsolutePath());
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    public void setDirectories(Map<String, String> directories) {
        this.directories = directories;
    }

}
