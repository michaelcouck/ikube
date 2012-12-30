package ikube.index.handler.strategy;

import ikube.index.handler.IStrategy;
import ikube.index.parse.IParser;
import ikube.index.parse.ParserProvider;
import ikube.model.IndexableColumn;
import ikube.toolkit.FileUtilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * TODO Comments... And complete this strategy of course. And a test...
 * 
 * @author Michael Couck
 * @since 22.12.12
 * @version 01.00
 */
public class AddFileToIndexableColumnStrategy extends AStrategy {

	public AddFileToIndexableColumnStrategy(IStrategy nextStrategy) {
		super(nextStrategy);
	}

	@Override
	@SuppressWarnings("null")
	public boolean preProcess(final Object... parameters) {
		IndexableColumn indexableColumn = null;
		Object content = indexableColumn.getContent();
		if (content != null && String.class.isAssignableFrom(content.getClass())) {
			try {
				addFileContentToColumnContent(indexableColumn, content.toString());
			} catch (Exception e) {
				LOGGER.warn("Error processing file : " + content);
				LOGGER.debug(null, e);
			}
		}
		return super.preProcess(parameters);
	}

	@Override
	public boolean postProcess(final Object... parameters) {
		return super.postProcess(parameters);
	}

	protected void addFileContentToColumnContent(final IndexableColumn indexableColumn, final String filePath) throws Exception {
		InputStream inputStream = null;
		ByteArrayInputStream byteInputStream = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			inputStream = new FileInputStream(filePath);
			int length = 1000000;
			byte[] byteBuffer = new byte[length];
			int read = inputStream.read(byteBuffer, 0, byteBuffer.length);

			byteInputStream = new ByteArrayInputStream(byteBuffer, 0, read);
			byteOutputStream = new ByteArrayOutputStream();
			byteOutputStream.write(filePath.getBytes());
			byteOutputStream.write(" ".getBytes());

			IParser parser = ParserProvider.getParser(filePath, byteBuffer);
			String parsedContent = parser.parse(byteInputStream, byteOutputStream).toString();

			indexableColumn.setContent(parsedContent);
		} finally {
			FileUtilities.close(inputStream);
			FileUtilities.close(byteInputStream);
			FileUtilities.close(byteOutputStream);
		}
	}

}