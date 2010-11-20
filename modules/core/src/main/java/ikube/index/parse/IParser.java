package ikube.index.parse;

import java.io.InputStream;
import java.io.OutputStream;

public interface IParser {

	public OutputStream parse(InputStream inputStream) throws Exception;

}
