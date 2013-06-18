package ikube.deploy.action;

import ikube.deploy.model.Directory;
import ikube.deploy.model.File;
import ikube.deploy.model.Server;

import java.util.Map;

import net.neoremind.sshxcute.core.SSHExec;

public class CopyAction extends Action {

	private Map<File, File> files;
	private Map<Directory, Directory> directories;

	@Override
	public boolean execute(final Server server) {
		SSHExec sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
		if (files != null) {
			for (final Map.Entry<File, File> filePair : files.entrySet()) {
				try {
					sshExec.uploadSingleDataToServer(filePair.getKey().getFile(), filePair.getValue().getFile());
				} catch (Exception e) {
					logger.error("Exception copying file to server, from : " + filePair.getKey().getFile() + ", to : " + filePair.getValue().getFile()
							+ ", server : " + server.getIp(), e);
					if (isBreakOnError()) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		if (directories != null) {
			for (final Map.Entry<Directory, Directory> filePair : directories.entrySet()) {
				try {
					sshExec.uploadAllDataToServer(filePair.getKey().getDirectory(), filePair.getValue().getDirectory());
				} catch (Exception e) {
					logger.error("Exception copying directory to server, from : " + filePair.getKey().getDirectory() + ", to : "
							+ filePair.getValue().getDirectory() + ", server : " + server.getIp(), e);
					if (isBreakOnError()) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		return Boolean.TRUE;
	}

	public Map<File, File> getFiles() {
		return files;
	}

	public void setFiles(Map<File, File> files) {
		this.files = files;
	}

	public Map<Directory, Directory> getDirectories() {
		return directories;
	}

	public void setDirectories(Map<Directory, Directory> directories) {
		this.directories = directories;
	}

}
