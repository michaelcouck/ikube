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

	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void getContent(IndexableFileSystem indexable, OutputStream outputStream) {
		try {
			File file = indexable.getCurrentFile();
			// TODO - if this is a zip or jar or whatever then we have to
			// unpack the file and recursively index the contents one by one
			InputStream inputStream = new FileInputStream(file);
			FileUtilities.getContents(inputStream, outputStream, Integer.MAX_VALUE);
		} catch (Exception e) {
			logger.error("Exception accessing file : " + indexable.getCurrentFile(), e);
		}
	}

}