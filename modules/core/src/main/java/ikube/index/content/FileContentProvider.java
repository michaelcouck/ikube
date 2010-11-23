package ikube.index.content;

import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * @author Michael Couck
 * @since 23.11.10
 * @version 01.00
 */
public class FileContentProvider implements IContentProvider<IndexableFileSystem> {

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * TODO - we need to parse the data here and return a file input stream if it is too large.
	 */
	public Object getContent(IndexableFileSystem indexable) {
		try {
			File file = indexable.getCurrentFile();
			if (file == null) {
				return new ByteArrayInputStream(new byte[0]);
			}
			InputStream inputStream = new FileInputStream(file);
			ByteArrayOutputStream byteArrayOutputStream = FileUtilities.getContents(inputStream, Integer.MAX_VALUE);
			return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		} catch (Exception e) {
			logger.error("Exception accessing file : " + indexable.getCurrentFile(), e);
		}
		return null;
	}

}