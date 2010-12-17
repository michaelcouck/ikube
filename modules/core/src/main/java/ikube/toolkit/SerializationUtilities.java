package ikube.toolkit;

import ikube.IConstants;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 21.11.10
 * @version 01.00
 */
public class SerializationUtilities {

	private static Logger LOGGER = Logger.getLogger(SerializationUtilities.class);

	private static ExceptionListener exceptionListener = new ExceptionListener() {
		@Override
		public void exceptionThrown(Exception e) {
			LOGGER.error("General exception : ", e);
		}
	};

	public static String serialize(Object object) {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XMLEncoder xmlEncoder = new XMLEncoder(byteArrayOutputStream);
			xmlEncoder.setExceptionListener(exceptionListener);
			xmlEncoder.writeObject(object);
			xmlEncoder.flush();
			xmlEncoder.close();
			return byteArrayOutputStream.toString(IConstants.ENCODING);
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding : ", e);
		}
		return null;
	}

	public static Object deserialize(String xml) {
		byte[] bytes = new byte[0];
		try {
			bytes = xml.getBytes(IConstants.ENCODING);
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			XMLDecoder xmlDecoder = new XMLDecoder(byteArrayInputStream);
			xmlDecoder.setExceptionListener(exceptionListener);
			return xmlDecoder.readObject();
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding : ", e);
		}
		return null;
	}

	public static Object clone(Object object) {
		return SerializationUtilities.deserialize(SerializationUtilities.serialize(object));
	}

}
