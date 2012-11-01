package ikube.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import mockit.Mock;
import mockit.MockClass;

@MockClass(realClass = URL.class)
public class URLMock {

	private static ByteArrayOutputStream CONTENTS;

	@Mock
	public static InputStream openStream() {
		return new ByteArrayInputStream(CONTENTS.toByteArray());
	}

	public static void setContents(final ByteArrayOutputStream contents) {
		URLMock.CONTENTS = contents;
	}
}
