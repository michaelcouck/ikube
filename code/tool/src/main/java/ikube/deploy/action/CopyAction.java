package ikube.deploy.action;

import ikube.deploy.model.Server;

import java.util.Map;

import net.neoremind.sshxcute.core.SSHExec;

public class CopyAction extends Action {

	private Map<String, String> files;
	private Map<String, String> directories;

	@Override
	public boolean execute(final Server server) {
		SSHExec sshExec = getSshExec(server.getIp(), server.getUsername(), server.getPassword());
		try {
			if (files != null) {
				for (final Map.Entry<String, String> filePair : files.entrySet()) {
					try {
						logger.info("Copying file : " + filePair.getKey() + ", to : " + filePair.getValue() + ", on server : " + server.getIp());
						sshExec.uploadSingleDataToServer(filePair.getKey(), filePair.getValue());
					} catch (Exception e) {
						handleException("Exception copying file to server, from : " + filePair.getKey() + ", to : " + filePair.getValue() + ", server : "
								+ server.getIp(), e);
					}
				}
			}
			if (directories != null) {
				for (final Map.Entry<String, String> filePair : directories.entrySet()) {
					try {
						logger.info("Copying directory : " + filePair.getKey() + ", to : " + filePair.getValue() + ", on server : " + server.getIp());
						sshExec.uploadAllDataToServer(filePair.getKey(), filePair.getValue());
					} catch (Exception e) {
						handleException("Exception copying directory to server, from : " + filePair.getKey() + ", to : " + filePair.getValue() + ", server : "
								+ server.getIp(), e);
					}
				}
			}
		} finally {
			disconnect(sshExec);
		}
		return Boolean.TRUE;
	}

	public void setFiles(Map<String, String> files) {
		this.files = files;
	}

	public void setDirectories(Map<String, String> directories) {
		this.directories = directories;
	}

}
