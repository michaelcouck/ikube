package ikube.index.content;

import ikube.model.IndexableInternet;
import ikube.toolkit.FileUtilities;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * This class allows writing the data returned from a url to the output stream specified in the parameter list.
 * 
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
public class InternetContentProvider implements IContentProvider<IndexableInternet> {

	private static final Logger LOGGER = Logger.getLogger(InternetContentProvider.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void getContent(final IndexableInternet indexable, final OutputStream outputStream) {
		try {
			InputStream inputStream = indexable.getCurrentInputStream();
			// TODO This value for the maximum read length must be configurable
			FileUtilities.getContents(inputStream, outputStream, Integer.MAX_VALUE);
		} catch (Exception e) {
			LOGGER.error("Exception accessing url : " + indexable, e);
		}
	}

}