package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexableColumn;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TODO Comments... And complete this strategy of course. And a test...
 * 
 * @author Michael Couck
 * @since 22.12.12
 * @version 01.00
 */
public class AddFileToIndexableColumnStrategy extends AStrategy {

	private static final byte[] SPACE = " ".getBytes();

	public AddFileToIndexableColumnStrategy(IStrategy nextStrategy) {
		super(nextStrategy);
	}

	@Override
	@SuppressWarnings("null")
	public boolean aroundProcess(final Object... parameters) throws Exception {
		IndexableColumn indexableColumn = null;
		Object content = indexableColumn.getContent();
		if (content != null && String.class.isAssignableFrom(content.getClass())) {
			try {
				addFileContentToColumnContent(indexableColumn, content.toString());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return super.aroundProcess(parameters);
	}

	protected void addFileContentToColumnContent(final IndexableColumn indexableColumn, final String filePath) throws Exception {
		InputStream inputStream = null;
		ByteArrayInputStream byteInputStream = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			File file = new File(filePath);
			inputStream = new FileInputStream(file);
			long length = getIndexContext(indexableColumn).getMaxReadLength();
			byte[] byteBuffer = new byte[(int) length];
			int read = inputStream.read(byteBuffer, 0, byteBuffer.length);

			byteInputStream = new ByteArrayInputStream(byteBuffer, 0, read);
			byteOutputStream = new ByteArrayOutputStream();
			byteOutputStream.write(filePath.getBytes());
			byteOutputStream.write(" ".getBytes());

			IParser parser = ParserProvider.getParser(filePath, byteBuffer);
			OutputStream contentOutputStream = parser.parse(byteInputStream, byteOutputStream);
			contentOutputStream.write(SPACE);
			contentOutputStream.write(filePath.getBytes());
			contentOutputStream.write(SPACE);
			contentOutputStream.write(Long.toString(file.lastModified()).getBytes());

			indexableColumn.setContent(contentOutputStream.toString());
		} finally {
			FileUtilities.close(inputStream);
			FileUtilities.close(byteInputStream);
			FileUtilities.close(byteOutputStream);
		}
	}

}