package ikube.service;

import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.ejb.Remote;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.apache.log4j.Logger;

@Remote(ISynchronizationWebService.class)
@SOAPBinding(style = SOAPBinding.Style.RPC)
@WebService(name = ISynchronizationWebService.NAME, targetNamespace = ISynchronizationWebService.TARGET_NAMESPACE, serviceName = ISynchronizationWebService.SERVICE_NAME)
public class SynchronizationWebService implements ISynchronizationWebService {

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public Boolean wantsFile(String baseDirectory, String latestDirectory, String serverDirectory, String contextDirectory, String file) {
		File indexFile = getIndexFile(baseDirectory, latestDirectory, serverDirectory, contextDirectory, file);
		return !indexFile.exists();
	}

	@Override
	public Boolean writeIndexFile(String baseDirectory, String latestDirectory, String serverDirectory, String contextDirectory,
			String file, byte[] bytes) {
		File indexFile = getIndexFile(baseDirectory, latestDirectory, serverDirectory, contextDirectory, file);
		if (!indexFile.exists()) {
			FileUtilities.getFile(indexFile.getAbsolutePath(), Boolean.FALSE);
		}
		// Append the bytes to the file
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(indexFile, Boolean.TRUE);
			logger.info("Writing bytes to index file : " + bytes.length + ", " + indexFile);
			outputStream.write(bytes);
		} catch (Exception e) {
			logger.error("Exception writing the data to the index file : " + indexFile, e);
			FileUtilities.deleteFile(indexFile, 1);
			return Boolean.FALSE;
		} finally {
			try {
				outputStream.close();
			} catch (Exception e) {
				logger.error("Exception closing the output stream on the index file : " + indexFile, e);
			}
		}
		return Boolean.TRUE;
	}

	protected File getIndexFile(String baseDirectory, String latestDirectory, String serverDirectory, String contextDirectory, String file) {
		StringBuilder builder = new StringBuilder();

		builder.append(baseDirectory);
		builder.append(File.separator);
		builder.append(latestDirectory);
		builder.append(File.separator);
		builder.append(serverDirectory);
		builder.append(File.separator);
		builder.append(contextDirectory);
		builder.append(File.separator);
		builder.append(file);

		File indexFile = new File(builder.toString());
		logger.info("Index file : " + indexFile + ", " + builder);
		return indexFile;
	}

}