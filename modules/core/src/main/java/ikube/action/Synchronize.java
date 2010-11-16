package ikube.action;

import ikube.model.IndexContext;
import ikube.model.Server;
import ikube.service.ISynchronizationWebService;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 * @author Michael Couck
 * @since 31.10.10
 * @version 01.00
 */
public class Synchronize extends AAction<IndexContext, Boolean> {

	private String endpointUri;
	private String serviceName;
	private String targetNamespace;
	private Pattern pattern;

	@Override
	public Boolean execute(IndexContext indexContext) {
		try {
			String actionName = getClass().getName();
			if (getLockManager().anyWorking(indexContext, actionName)) {
				return Boolean.FALSE;
			}
			getLockManager().setWorking(indexContext, actionName, Boolean.TRUE, System.currentTimeMillis());
			File baseDirectory = FileUtilities.getFile(indexContext.getIndexDirectoryPath(), Boolean.TRUE);
			File latestDirectory = FileUtilities.getLatestIndexDirectory(indexContext.getIndexDirectoryPath());
			logger.info("Base directory : " + baseDirectory + ", " + latestDirectory);
			if (latestDirectory == null) {
				return Boolean.FALSE;
			}
			Server thisServer = getLockManager().getServer(indexContext);
			Set<Server> tokens = getLockManager().getServers(indexContext);
			logger.info("Servers : " + tokens);
			for (Server server : tokens) {
				logger.info("Server : " + server);
				if (server.getAddress().compareTo(thisServer.getAddress()) == 0) {
					continue;
				}
				String ip = server.getIp();
				writeToServer(ip, baseDirectory, latestDirectory);
			}
		} finally {
			getLockManager().setWorking(indexContext, null, Boolean.FALSE, 0);
		}
		return Boolean.TRUE;
	}

	protected void writeToServer(String serverIpAddress, File baseDirectory, File latestDirectory) {
		try {
			// Get the web service for this server
			ISynchronizationWebService synchronizationWebService = getSynchronizationWebService(serverIpAddress);
			// Get the latest index directory and try to write them to the server
			File[] serverIndexDirectories = latestDirectory.listFiles();
			for (File serverDirectory : serverIndexDirectories) {
				logger.info("Checking server directory : " + serverDirectory);
				File[] indexFiles = serverDirectory.listFiles();
				for (File file : indexFiles) {
					logger.info("Sending file : " + file);
					Boolean wantsFile = synchronizationWebService.wantsFile(baseDirectory.getName(), latestDirectory.getName(),
							serverDirectory.getName(), file.getName());
					if (!wantsFile) {
						continue;
					}
					writeFile(synchronizationWebService, baseDirectory.getName(), latestDirectory.getName(), serverDirectory.getName(),
							file);
				}
			}
		} catch (Exception e) {
			logger.error("Exception synchronizing : " + serverIpAddress, e);
		}
	}

	protected void writeFile(ISynchronizationWebService synchronizationWebService, String baseDirectory, String latestDirectory,
			String serverDirectory, File file) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			byte[] bytes = new byte[0];
			int read = -1;
			Boolean writeMore = Boolean.TRUE;
			while ((read = inputStream.read(bytes)) > -1 && writeMore) {
				if (read < bytes.length) {
					byte[] holder = new byte[read];
					System.arraycopy(bytes, 0, holder, 0, holder.length);
					bytes = holder;
				}
				writeMore = synchronizationWebService
						.writeIndexFile(baseDirectory, latestDirectory, serverDirectory, file.getName(), bytes);
			}
		} catch (Exception e) {
			logger.error("Exception writing file to server : " + file, e);
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				logger.error("Exception closing the index file : ", e);
			}
		}
	}

	protected ISynchronizationWebService getSynchronizationWebService(String ipAddress) throws Exception {
		logger.info("Accessing the web service on : " + ipAddress);
		QName serviceName = new QName(this.targetNamespace, this.serviceName);
		String replacedEndpointUri = pattern.matcher(this.endpointUri).replaceAll(ipAddress);
		URL wsdlURL = new URL(replacedEndpointUri);
		Service service = Service.create(wsdlURL, serviceName);
		ISynchronizationWebService synchronizationService = service.getPort(ISynchronizationWebService.class);
		return synchronizationService;
	}

	public void setEndpointUri(String endpointUri) {
		this.endpointUri = endpointUri;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
	}

	public void setIpAddressPattern(String ipAddressPattern) {
		pattern = Pattern.compile(ipAddressPattern);
	}

}
