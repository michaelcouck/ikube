package ikube.mock;

import ikube.toolkit.FILE;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import mockit.Mock;
import mockit.MockClass;

@MockClass(realClass = FILE.class)
public class FILEMock {

	private static ByteArrayOutputStream CONTENTS;

	@Mock
	public static ByteArrayOutputStream getContents(final InputStream inputStream, final long maxLength) {
		return CONTENTS;
	}

	public static void setContents(final ByteArrayOutputStream contents) {
		FILEMock.CONTENTS = contents;
	}
}
