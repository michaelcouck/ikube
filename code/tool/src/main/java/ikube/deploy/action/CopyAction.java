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
						sshExec.uploadSingleDataToServer(filePair.getKey(), filePair.getValue());
					} catch (Exception e) {
						logger.error(
								"Exception copying file to server, from : " + filePair.getKey() + ", to : " + filePair.getValue() + ", server : "
										+ server.getIp(), e);
						if (isBreakOnError()) {
							throw new RuntimeException(e);
						}
					}
				}
			}
			if (directories != null) {
				for (final Map.Entry<String, String> filePair : directories.entrySet()) {
					try {
						sshExec.uploadAllDataToServer(filePair.getKey(), filePair.getValue());
					} catch (Exception e) {
						logger.error("Exception copying directory to server, from : " + filePair.getKey() + ", to : " + filePair.getValue() + ", server : "
								+ server.getIp(), e);
						if (isBreakOnError()) {
							throw new RuntimeException(e);
						}
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
