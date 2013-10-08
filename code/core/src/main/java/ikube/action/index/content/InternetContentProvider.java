package ikube.action.index.content;

import ikube.model.IndexableInternet;
import ikube.toolkit.FileUtilities;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class allows writing the data returned from a url to the output stream specified in the parameter list.
 * 
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
public class InternetContentProvider implements IContentProvider<IndexableInternet> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(InternetContentProvider.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void getContent(final IndexableInternet indexableInternet, final OutputStream outputStream) {
		try {
			InputStream inputStream = indexableInternet.getCurrentInputStream();
			FileUtilities.getContents(inputStream, outputStream, indexableInternet.getMaxReadLength());
			LOGGER.info("Content : " + outputStream.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}