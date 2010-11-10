package ikube.action.synchronize;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public abstract class Sockets {

	Logger logger = Logger.getLogger(this.getClass());

	protected void close(Socket socket) {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			logger.error("Exception closing socket to target server : ", e);
		}
	}

	protected void close(InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
			logger.error("Exception closing the input stream : " + inputStream, e);
		}
	}

	protected void close(OutputStream outputStream) {
		try {
			if (outputStream != null) {
				outputStream.close();
			}
		} catch (Exception e) {
			logger.error("Exception closing the output stream : " + outputStream, e);
		}
	}

	ServerSocket openSocket(int port) {
		try {
			return new ServerSocket(port);
		} catch (BindException e) {
			logger.error("Bind exception, is this port being used?", e);
		} catch (Exception e) {
			logger.error("Exception opening a socket, could be the firewall : ");
		}
		return null;
	}

}
