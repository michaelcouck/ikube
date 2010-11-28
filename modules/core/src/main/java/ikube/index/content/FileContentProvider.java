package ikube.index.content;

import ikube.model.IndexableFileSystem;
import ikube.toolkit.FileUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
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
			InputStream inputStream = new FileInputStream(file);
			outputStream = FileUtilities.getContents(inputStream, Integer.MAX_VALUE);
		} catch (Exception e) {
			logger.error("Exception accessing file : " + indexable.getCurrentFile(), e);
		}
	}

}