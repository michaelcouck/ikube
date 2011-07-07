package ikube.index.content;

import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * This class allows writing the data returned from a file to the output stream specified in the parameter list.
 * 
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
public class FileContentProvider implements IContentProvider<IndexableFileSystem> {

	private static final Logger LOGGER = Logger.getLogger(FileContentProvider.class);

	@Override
	public void getContent(final IndexableFileSystem indexable, final OutputStream outputStream) {
		try {
			File file = indexable.getCurrentFile();
			// TODO - if this is a zip or jar or whatever then we have to
			// unpack the file and recursively index the contents one by one
			InputStream inputStream = new FileInputStream(file);
			FileUtilities.getContents(inputStream, outputStream, indexable.getMaxReadLength());
		} catch (Exception e) {
			LOGGER.error("Exception accessing file : " + indexable.getCurrentFile(), e);
		}
	}

}